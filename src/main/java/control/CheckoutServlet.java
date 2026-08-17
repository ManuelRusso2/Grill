package control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.bean.CarrelloBean;
import model.bean.ProdottoBean;
import model.bean.UtenteBean;
import model.dao.AcquistoDAO;
import model.dao.CarrelloDAO;
import model.dao.ContenutoDAO;
import model.dao.impl.AcquistoDAOImpl;
import model.dao.impl.CarrelloDAOImpl;
import model.dao.impl.ContenutoDAOImpl;

/**
 * Servlet per la gestione del processo di checkout degli ordini.
 * Mappa l'URL '/CheckoutServlet' e gestisce sia la visualizzazione della pagina
 * di checkout (GET) che la conferma dell'ordine con pagamento (POST).
 */
@WebServlet("/CheckoutServlet")
public class CheckoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Oggetti DAO per l'accesso ai dati nel database
    private CarrelloDAO carrelloDAO;
    private ContenutoDAO contenutoDAO;
    private AcquistoDAO acquistoDAO;

    /**
     * Inizializza le istanze dei DAO necessarie per la servlet.
     */
    @Override
    public void init() throws ServletException {
        this.carrelloDAO = new CarrelloDAOImpl();
        this.contenutoDAO = new ContenutoDAOImpl();
        this.acquistoDAO = new AcquistoDAOImpl();
    }

    /**
     * Gestisce le richieste GET per visualizzare il riepilogo del checkout.
     * Verifica l'autenticazione dell'utente, recupera il carrello e reindirizza alla JSP.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Verifica se l'utente è autenticato
        UtenteBean utente = getLoggedUser(request);
        if (utente == null) {
            // Reindirizza al login se la sessione non è valida o non contiene l'utente
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return;
        }

        try {
            // 2. Recupera (o crea) il carrello attivo dell'utente
            CarrelloBean carrello = ottieniOCreaCarrello(utente.getIdUtente());
            Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());

            // 3. Se il carrello è vuoto, reindirizza alla pagina del carrello
            if (prodottiInCarrello == null || prodottiInCarrello.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/CarrelloServlet");
                return;
            }

            // 4. Imposta i dati necessari per la vista JSP
            request.setAttribute("prodottiCarrello", prodottiInCarrello);
            request.setAttribute("totaleCarrello", calcolaTotale(prodottiInCarrello));

            // 5. Inoltra la richiesta alla JSP di checkout
            request.getRequestDispatcher("/jsp/user/checkout.jsp").forward(request, response);

        } catch (SQLException e) {
            // Log dell'errore lato server e invio codice HTTP 500
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gestisce le richieste POST per elaborare il pagamento e completare l'ordine.
     * Effettua la validazione dei dati inviati e finalizza la transazione d'acquisto.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Verifica l'autenticazione dell'utente
        UtenteBean utente = getLoggedUser(request);
        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return;
        }

        // 2. Estrazione e pulizia dei parametri base
        String metodoPagamento = getTrimmedParam(request, "metodoPagamento");
        String indirizzoConsegna = getTrimmedParam(request, "indirizzoConsegna");

        // Controllo presenza dati minimi richiesti
        if (metodoPagamento == null || indirizzoConsegna == null) {
            request.setAttribute("errorMessage", "Compila metodo di pagamento e indirizzo di consegna.");
            doGet(request, response);
            return;
        }

        // 3. Validazione specifica dei dati in base al metodo di pagamento scelto
        String errorMessage = null;
        if ("Carta".equalsIgnoreCase(metodoPagamento)) {
            errorMessage = validaCarta(request);
        } else if ("Conto_bancario".equalsIgnoreCase(metodoPagamento)) {
            errorMessage = validaConto(request);
        } else {
            errorMessage = "Metodo di pagamento non valido.";
        }

        // Se la validazione fallisce, ricarica la pagina mostrato l'errore
        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
            doGet(request, response);
            return;
        }

        try {
            // 4. Recupero articoli dal carrello per completare l'ordine
            CarrelloBean carrello = ottieniOCreaCarrello(utente.getIdUtente());
            Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());

            if (prodottiInCarrello == null || prodottiInCarrello.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/CarrelloServlet");
                return;
            }

            // 5. Calcolo del totale dell'ordine
            double totale = calcolaTotale(prodottiInCarrello);

            // 6. Esecuzione della transazione di acquisto atomica a livello di DAO
            List<String> newAlerts = new ArrayList<>();
            int idAcquisto = acquistoDAO.completaAcquisto(
                carrello.getIdCarrello(),
                utente.getIdUtente(),
                metodoPagamento,
                indirizzoConsegna,
                prodottiInCarrello,
                totale,
                newAlerts
            );

            // 7. Registrazione nel contesto applicativo di eventuali alert sul magazzino (es. scorte esaurite)
            for (String alert : newAlerts) {
                addAdminAlert(alert);
            }

            // 8. Impostazione dei dati di riepilogo per la schermata di conferma
            request.setAttribute("ordineId", idAcquisto);
            request.setAttribute("totaleOrdine", totale);
            request.setAttribute("metodoPagamento", metodoPagamento);
            request.setAttribute("indirizzoConsegna", indirizzoConsegna);

            // Forward alla pagina di successo
            request.getRequestDispatcher("/jsp/user/ordine-confermato.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================================
    // METODI HELPER E VALIDATORI
    // =========================================================================

    /**
     * Cerca il carrello dell'utente nel database; se non esiste, ne crea uno nuovo.
     * 
     * @param idUtente ID dell'utente
     * @return Il bean del carrello associato
     * @throws SQLException in caso di errore di accesso al DB
     */
    private CarrelloBean ottieniOCreaCarrello(int idUtente) throws SQLException {
        CarrelloBean carrello = carrelloDAO.doRetrieveByUtente(idUtente);
        if (carrello == null) {
            carrello = new CarrelloBean();
            carrello.setIdUtente(idUtente);
            carrelloDAO.doSave(carrello);
            carrello = carrelloDAO.doRetrieveByUtente(idUtente);
        }
        return carrello;
    }

    /**
     * Calcola la somma totale dei costi dei prodotti nel carrello moltiplicati per le relative quantità.
     * 
     * @param prodottiInCarrello Mappa contenente i prodotti e le rispettive quantità
     * @return Importo totale in double
     */
    private double calcolaTotale(Map<ProdottoBean, Integer> prodottiInCarrello) {
        double totale = 0.0;
        if (prodottiInCarrello != null) {
            for (Map.Entry<ProdottoBean, Integer> entry : prodottiInCarrello.entrySet()) {
                totale += entry.getKey().getCosto() * entry.getValue();
            }
        }
        return totale;
    }

    /**
     * Valida i parametri relativi alla carta di credito (numero, intestatario, scadenza e CVV).
     * 
     * @param request La richiesta HTTP contenente i parametri della form
     * @return Una stringa contenente l'errore, oppure null se la validazione ha successo
     */
    private String validaCarta(HttpServletRequest request) {
        String cartaNumero = getTrimmedParam(request, "cartaNumero");
        String cartaNome = getTrimmedParam(request, "cartaNome");
        String cartaCognome = getTrimmedParam(request, "cartaCognome");
        String cartaScadenza = getTrimmedParam(request, "cartaScadenza");
        String cartaCVV = getTrimmedParam(request, "cartaCVV");

        // Verifico la presenza di tutti i campi
        if (cartaNumero == null || cartaNome == null || cartaCognome == null || cartaScadenza == null || cartaCVV == null) {
            return "Compila tutti i dati della carta richiesti.";
        }

        // Rimuovo spazi dal numero carta
        String numOnly = cartaNumero.replaceAll("\\s+", "");
        
        // Controllo lunghezza, cifre e algoritmo di Luhn
        if (!numOnly.matches("\\d{13,19}") || !luhnCheck(numOnly)) {
            return "Numero carta non valido.";
        }
        // Controllo formato scadenza MM/AA
        if (!cartaScadenza.matches("^(0[1-9]|1[0-2])/(\\d{2})$")) {
            return "Formato data scadenza non valido (MM/AA).";
        }
        // Controllo formato CVV (3 o 4 cifre)
        if (!cartaCVV.matches("^\\d{3,4}$")) {
            return "CVV non valido.";
        }

        return null; // Nessun errore
    }

    /**
     * Valida i parametri relativi al conto bancario (intestatario ed IBAN).
     * 
     * @param request La richiesta HTTP contenente i parametri della form
     * @return Una stringa contenente l'errore, oppure null se la validazione ha successo
     */
    private String validaConto(HttpServletRequest request) {
        String contoNome = getTrimmedParam(request, "contoNome");
        String contoCognome = getTrimmedParam(request, "contoCognome");
        String contoIBAN = getTrimmedParam(request, "contoIBAN");

        if (contoIBAN == null || contoNome == null || contoCognome == null) {
            return "Compila tutti i dati del conto bancario richiesti.";
        }

        if (!validateIBAN(contoIBAN)) {
            return "IBAN non valido.";
        }

        return null;
    }

    /**
     * Implementa l'algoritmo di Luhn per verificare la validità del numero di carta.
     * 
     * @param number Stringa numerica da verificare
     * @return true se il numero è valido secondo l'algoritmo, false altrimenti
     */
    private boolean luhnCheck(String number) {
        if (number == null || !number.matches("\\d+")) return false;
        int sum = 0;
        boolean alternate = false;
        
        // Calcola la somma partendo dall'ultima cifra a destra
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10) == 0;
    }

    /**
     * Effettua un controllo sintattico di base del codice IBAN tramite Regex.
     * 
     * @param iban Il codice IBAN da validare
     * @return true se rispetta il formato, false altrimenti
     */
    private boolean validateIBAN(String iban) {
        if (iban == null) return false;
        String value = iban.replaceAll("\\s+", "").toUpperCase();
        // Regex per IBAN generico (2 lettere nazione + 2 cifre controllo + fino a 30 caratteri alfanumerici)
        return value.matches("^[A-Z]{2}[0-9A-Z]{13,32}$");
    }

    /**
     * Aggiunge in modo thread-safe un messaggio d'avviso per l'amministratore
     * all'interno del ServletContext (es. esaurimento scorte).
     * 
     * @param alertMessage Il messaggio da notificare all'admin
     */
    private void addAdminAlert(String alertMessage) {
        synchronized (getServletContext()) {
            @SuppressWarnings("unchecked")
            List<String> alerts = (List<String>) getServletContext().getAttribute("adminAlerts");

            if (alerts == null) {
                alerts = new ArrayList<>();
                getServletContext().setAttribute("adminAlerts", alerts);
            }
            alerts.add(alertMessage);
        }
    }

    /**
     * Recupera l'utente correntemente autenticato dalla sessione HTTP.
     * 
     * @param request La richiesta HTTP
     * @return L'oggetto UtenteBean se presente in sessione, altrimenti null
     */
    private UtenteBean getLoggedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (UtenteBean) session.getAttribute("utente") : null;
    }

    /**
     * Estrae un parametro dalla request applicando il trim degli spazi ed 
     * evitando valori vuoti o spazi bianchi.
     * 
     * @param request La richiesta HTTP
     * @param name Il nome del parametro
     * @return La stringa formattata oppure null se vuota o non presente
     */
    private String getTrimmedParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }
}