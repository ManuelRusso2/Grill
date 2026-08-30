package control;

import java.io.IOException;
import java.sql.SQLException;
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
import model.dao.CarrelloDAO;
import model.dao.ContenutoDAO;
import model.dao.ProdottoDAO;
import model.dao.impl.CarrelloDAOImpl;
import model.dao.impl.ContenutoDAOImpl;
import model.dao.impl.ProdottoDAOImpl;

/**
 * Servlet per la gestione completa delle operazioni sul Carrello acquisti.
 * Supporta sia la visualizzazione via GET (Read-Only) sia le modifiche via POST 
 * (aggiunta, rimozione, aggiornamento quantità, svuotamento) gestendo sia chiamate
 * sincrone standard che asincrone tramite AJAX.
 */
@WebServlet("/CarrelloServlet")
public class CarrelloServlet extends HttpServlet {
    
    // Identificatore univoco per la serializzazione
    private static final long serialVersionUID = 1L;

    // Riferimenti ai DAO necessari per la gestione di prodotti, carrello e relative voci di contenuto
    private ProdottoDAO prodottoDAO;
    private CarrelloDAO carrelloDAO;
    private ContenutoDAO contenutoDAO;

    /**
     * Inizializza la servlet istanziando le implementazioni concrete dei DAO.
     */
    @Override
    public void init() throws ServletException {
        this.prodottoDAO = new ProdottoDAOImpl();
        this.carrelloDAO = new CarrelloDAOImpl();
        this.contenutoDAO = new ContenutoDAOImpl();
    }

    /**
     * Gestisce le richieste HTTP GET.
     * Si occupa ESCLUSIVAMENTE della lettura e visualizzazione del carrello dell'utente.
     * 
     * @param request  La richiesta HTTP
     * @param response La risposta HTTP
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // Recupera la sessione corrente senza crearne una nuova se non esiste
        HttpSession session = request.getSession(false);
        // Estrae l'utente autenticato dalla sessione
        UtenteBean utente = null;

        if (session != null) {
            utente = (UtenteBean) session.getAttribute("utente");
        }

        // Controllo autenticazione: se l'utente non è loggato viene reindirizzato al login
        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return; // Interrompe l'esecuzione
        }

        // Controllo di sicurezza: impedisce il carrello agli utenti con ruolo Amministratore
        if (utente.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Gli amministratori non possono accedere al carrello.");
            return; // Interrompe l'esecuzione
        }

        try {
            // Recupera il carrello dal DB o ne crea uno nuovo se non esiste
            CarrelloBean carrello = ottieniOCreaCarrello(utente.getIdUtente());

            // Consuma i messaggi temporanei (Flash Messages) dalla sessione portandoli sulla request
            moveSessionAttributeToRequest(session, request, "successMessage");
            moveSessionAttributeToRequest(session, request, "errorMessage");

            // Recupera dal DB la mappa contenente i prodotti presenti nel carrello con le relative quantità
            Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());
            
            // Inserisce i prodotti recuperati come attributo di request per la JSP
            request.setAttribute("prodottiCarrello", prodottiInCarrello);

            // Inoltra la gestione alla pagina JSP del carrello
            request.getRequestDispatcher("/jsp/user/carrello.jsp").forward(request, response);

        } catch (SQLException e) {
            // Log dell'eccezione SQL a console
            e.printStackTrace();
            // Restituisce codice errore 500 in caso di anomalie sul DB
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gestisce le richieste HTTP POST.
     * Si occupa ESCLUSIVAMENTE delle modifiche dello stato del carrello (Aggiungi, Rimuovi, Aggiorna, Svuota).
     * 
     * @param request  La richiesta HTTP
     * @param response La risposta HTTP
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // Recupera la sessione senza crearne una nuova se non presente
        HttpSession session = request.getSession(false);
        // Estrae l'utente autenticato
        UtenteBean utente = null;

        if (session != null) {
            utente = (UtenteBean) session.getAttribute("utente");
        }
        
        // Determina se la richiesta proviene da una chiamata asincrona JavaScript (AJAX)
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));

        // Se l'utente non è autenticato
        if (utente == null) {
            if (isAjax) {
                // Risposta JSON 401 Unauthorized per la gestione via JS client-side
                String json = "{\"error\": \"login_required\", \"redirect\": \"" + request.getContextPath() + "/jsp/common/login.jsp\"}";
                sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, json);
                return;
            }
            // Reindirizzamento standard per richieste sincrone
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return;
        }

        // Se l'utente è un Amministratore
        if (utente.isAdmin()) {
            if (isAjax) {
                // Risposta JSON 403 Forbidden per chiamate AJAX
                String json = "{\"success\": false, \"message\": \"Gli amministratori non possono aggiungere prodotti al carrello.\"}";
                sendJsonResponse(response, HttpServletResponse.SC_FORBIDDEN, json);
                return;
            }
            // Errore HTTP 403 per richieste sincrone
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Gli amministratori non possono aggiungere prodotti al carrello.");
            return;
        }

        // Recupera l'azione richiesta (es. "add", "remove", "update", "empty")
        String action = getTrimmedParam(request, "action");
        if (action == null) action = "";

        try {
            // Ottiene il carrello associato all'utente
            CarrelloBean carrello = ottieniOCreaCarrello(utente.getIdUtente());

            // Smistamento dell'azione richiesta tramite switch-case
            switch (action.toLowerCase()) {
                case "add": // Aggiunta di un prodotto al carrello
                    aggiungiProdotto(request, session, carrello);
                    break;

                case "remove": // Rimozione di un singolo elemento dal carrello
                    rimuoviProdotto(request, session, carrello);
                    break;

                case "update": // Modifica della quantità di un prodotto
                    aggiornaQuantita(request, session, carrello);
                    break;

                case "empty": // Svuotamento completo del carrello
                case "svuota":
                    carrelloDAO.doEmpty(carrello.getIdCarrello());
                    session.setAttribute("successMessage", "Carrello svuotato con successo.");
                    break;

                default: // Azione sconosciuta o vuota
                    break;
            }

            // Calcola il totale complessivo dei pezzi attualmente presenti nel carrello
            Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());
            int totalItems = 0;
            if (prodottiInCarrello != null) {
                for (Integer qta : prodottiInCarrello.values()) {
                    if (qta != null) totalItems += qta;
                }
            }

            // Risposta formattata in JSON per le chiamate AJAX
            if (isAjax) {
                // Legge i messaggi imposti nelle logiche interne
                String errorMessage = (String) session.getAttribute("errorMessage");
                String successMessage = (String) session.getAttribute("successMessage");

                // Pulisce la sessione rimuovendo subito i messaggi
                session.removeAttribute("errorMessage");
                session.removeAttribute("successMessage");

                if (errorMessage != null) {
                    // Restituisce codice errore 400 Bad Request se l'operazione è fallita
                    String json = "{\"success\": false, \"message\": \"" + escapeJson(errorMessage) + "\"}";
                    sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, json);
                } else {
                    // Restituisce codice 200 OK con il conteggio aggiornato del carrello
                    String cleanSucc = (successMessage != null) ? escapeJson(successMessage) : "";
                    String json = "{\"success\": true, \"cartCount\": " + totalItems + ", \"message\": \"" + cleanSucc + "\"}";
                    sendJsonResponse(response, HttpServletResponse.SC_OK, json);
                }
                return;
            }

            response.sendRedirect(request.getContextPath() + "/CarrelloServlet");

        } catch (SQLException e) {
            e.printStackTrace();
            if (isAjax) {
                // Risposta di errore in formato JSON se si verifica un'eccezione DB su AJAX
                String json = "{\"success\": false, \"message\": \"Errore interno del server.\"}";
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, json);
                return;
            }
            // Errore HTTP 500 generico per richieste sincrone
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

 // =========================================================================
    // METODI PRIVATI PER LA LOGICA DI DOMINIO (CARRELLO)
    // =========================================================================

    /**
     * Recupera il carrello associato all'utente o ne crea uno nuovo a DB se non ancora presente.
     * 
     * @param idUtente L'ID dell'utente autenticato
     * @return CarrelloBean Il carrello valido (esistente o appena creato)
     * @throws SQLException In caso di errore durante l'accesso al database
     */
    private CarrelloBean ottieniOCreaCarrello(int idUtente) throws SQLException {
        // 1. Cerca il carrello esistente associato all'ID utente
        CarrelloBean carrello = carrelloDAO.doRetrieveByUtente(idUtente);
        
        // 2. Se l'utente non possiede ancora un carrello, ne crea uno nuovo
        if (carrello == null) {
            carrello = new CarrelloBean();
            carrello.setIdUtente(idUtente);
            
            // Persiste il nuovo carrello nel DB
            carrelloDAO.doSave(carrello); 
            
            // Rilegge il carrello dal DB per ottenere l'ID autogenerato dalla chiave primaria
            carrello = carrelloDAO.doRetrieveByUtente(idUtente); 
        }
        return carrello;
    }

    /**
     * Gestisce l'inserimento di un prodotto nel carrello dell'utente.
     * Effettua i controlli di validità dell'input, presenza del prodotto a DB e 
     * verifica che la quantità totale richiesta non superi lo stock di magazzino.
     * 
     * @param request  La richiesta HTTP contenente i parametri dell'articolo
     * @param session  La sessione utente per la gestione dei messaggi di esito (Flash Messages)
     * @param carrello Il carrello dell'utente a cui aggiungere il prodotto
     * @throws SQLException In caso di errore durante le query al database
     */
    private void aggiungiProdotto(HttpServletRequest request, HttpSession session, CarrelloBean carrello) throws SQLException {
        // 1. Estrazione e sanitizzazione dei parametri dalla richiesta
        int idProdotto = estraiIdProdotto(request);
        String taglia = getTrimmedParam(request, "taglia");
        if (taglia == null) taglia = "";

        // Imposta la quantità di default ad 1 se non espressamente specificata
        int quantitaDaAggiungere = 1; 
        String qtyParam = getTrimmedParam(request, "quantita");
        if (qtyParam != null) {
            try {
                quantitaDaAggiungere = Integer.parseInt(qtyParam);
            } catch (NumberFormatException e) {
                session.setAttribute("errorMessage", "Formato quantità non valido.");
                return;
            }
        }

        // 2. Validazione di base dei parametri di input
        if (idProdotto <= 0 || quantitaDaAggiungere <= 0) {
            session.setAttribute("errorMessage", "Dati prodotto o quantità non validi.");
            return;
        }

        // 3. Recupero delle informazioni aggiornate del prodotto dal database
        ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);

        // 4. Verifica di disponibilità: il prodotto deve esistere, essere attivo e avere giacenza > 0
        if (prodotto != null && prodotto.isAttivo() && prodotto.getQuantita() > 0) {
            
            // Calcola la somma di tutti i pezzi di questo articolo già presente nel carrello (tutte le taglie)
            int quantitaGiaInCarrello = contenutoDAO.getQuantitaTotaleProdottoInCarrello(carrello.getIdCarrello(), idProdotto);

            // Controlla che (pezzi già in carrello + nuovi pezzi) non superi lo stock fisico in magazzino
            if (quantitaGiaInCarrello + quantitaDaAggiungere <= prodotto.getQuantita()) {
                contenutoDAO.doAddProduct(carrello.getIdCarrello(), prodotto.getIdProdotto(), quantitaDaAggiungere, taglia);
                session.setAttribute("successMessage", "Prodotto aggiunto al carrello!");
            } else {
                session.setAttribute("errorMessage", "Impossibile aggiungere: quantità richiesta superiore alla disponibilità in magazzino.");
            }
        } else {
            session.setAttribute("errorMessage", "Il prodotto selezionato non è al momento disponibile.");
        }
    }

    /**
     * Rimuove un articolo specificato (per ID e taglia) dal carrello.
     * 
     * @param request  La richiesta HTTP contenente l'ID del prodotto e la taglia
     * @param session  La sessione per impostare il messaggio di conferma
     * @param carrello Il carrello dell'utente
     * @throws SQLException In caso di errore durante la cancellazione da DB
     */
    private void rimuoviProdotto(HttpServletRequest request, HttpSession session, CarrelloBean carrello) throws SQLException {
        int idProdotto = estraiIdProdotto(request);
        String taglia = getTrimmedParam(request, "taglia");
        if (taglia == null) taglia = "";

        if (idProdotto > 0) {
            contenutoDAO.doRemoveProduct(carrello.getIdCarrello(), idProdotto, taglia);
            session.setAttribute("successMessage", "Prodotto rimosso dal carrello.");
        }
    }

    /**
     * Aggiorna la quantità di un elemento già presente nel carrello.
     * Se la nuova quantità impostata è minore o uguale a zero, l'articolo viene rimosso dal carrello.
     * 
     * @param request  La richiesta HTTP contenente la nuova quantità
     * @param session  La sessione per notificare eventuali errori o conferme
     * @param carrello Il carrello su cui operare
     * @throws SQLException In caso di errore di aggiornamento a DB
     */
    private void aggiornaQuantita(HttpServletRequest request, HttpSession session, CarrelloBean carrello) throws SQLException {
        int idProdotto = estraiIdProdotto(request);
        String taglia = getTrimmedParam(request, "taglia");
        if (taglia == null) taglia = "";

        int nuovaQuantita;
        try {
            nuovaQuantita = Integer.parseInt(request.getParameter("quantita"));
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Formato quantità non valido.");
            return;
        }

        if (idProdotto <= 0) {
            session.setAttribute("errorMessage", "Prodotto non valido.");
            return;
        }

        ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);

        // Caso 1: Nuova quantità positiva ed entro i limiti del magazzino -> Aggiorna la riga
        if (prodotto != null && nuovaQuantita > 0 && nuovaQuantita <= prodotto.getQuantita()) {
            contenutoDAO.doUpdateQuantity(carrello.getIdCarrello(), idProdotto, nuovaQuantita, taglia);
            session.setAttribute("successMessage", "Quantità aggiornata con successo.");
        } 
        // Caso 2: Quantità imposta <= 0 -> Rimuove direttamente il prodotto dal carrello
        else if (nuovaQuantita <= 0) {
            contenutoDAO.doRemoveProduct(carrello.getIdCarrello(), idProdotto, taglia);
            session.setAttribute("successMessage", "Prodotto rimosso dal carrello.");
        } 
        // Caso 3: Quantità richiesta superiore alla giacenza a magazzino -> Notifica l'errore
        else {
            session.setAttribute("errorMessage", "Quantità non disponibile a magazzino.");
        }
    }

    // =========================================================================
    // METODI DI UTILITÀ E SUPPORTO (HELPER METHODS)
    // =========================================================================

    /**
     * Trasferisce un attributo di notifica dalla Sessione alla Request (Pattern Flash Attribute).
     * Garantisce che i messaggi sopravvivano al reindirizzamento (PRG) e vengano subito rimossi 
     * dalla sessione per evitare che compaiano nuovamente nelle pagine successive.
     * 
     * @param session       La sessione HTTP da cui prelevare l'attributo
     * @param request       La richiesta HTTP in cui iniettare l'attributo per la JSP
     * @param attributeName Il nome dell'attributo (es. "successMessage", "errorMessage")
     */
    private void moveSessionAttributeToRequest(HttpSession session, HttpServletRequest request, String attributeName) {
        if (session != null && session.getAttribute(attributeName) != null) {
            request.setAttribute(attributeName, session.getAttribute(attributeName));
            session.removeAttribute(attributeName); // Rimuove il messaggio per consumarlo una sola volta
        }
    }

    /**
     * Estrae un parametro dalla richiesta HTTP rimuovendo spazi bianchi iniziali e finali.
     * 
     * @param request La richiesta HTTP
     * @param name    Il nome del parametro da recuperare
     * @return String La stringa ripulita, oppure {@code null} se il parametro è assente o vuoto
     */
    private String getTrimmedParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        return null;
    }

    /**
     * Tenta l'estrazione dell'ID prodotto supportando sia il parametro "id" che "idProdotto".
     * Garantisce la compatibilità con chiamate arrivate da form o script JS differenti.
     * 
     * @param request La richiesta HTTP
     * @return int L'ID del prodotto convertito, oppure -1 in caso di errore/assenza
     */
    private int estraiIdProdotto(HttpServletRequest request) {
        String idStr = getTrimmedParam(request, "id");
        if (idStr == null) {
            idStr = getTrimmedParam(request, "idProdotto");
        }
        if (idStr != null) {
            try {
                return Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Scrive direttamente nello stream di risposta un payload in formato JSON.
     * Configura lo status code HTTP, il MIME type "application/json" e la codifica UTF-8.
     * 
     * @param response    La risposta HTTP
     * @param status      Lo stato HTTP da impostare (es. 200 OK, 400 Bad Request, 401 Unauthorized)
     * @param jsonContent La stringa JSON da inviare al client
     * @throws IOException In caso di errore durante la scrittura sullo stream di output
     */
    private void sendJsonResponse(HttpServletResponse response, int status, String jsonContent) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonContent);
    }

    /**
     * Effettua l'escape dei caratteri speciali nei testi per evitare la rottura 
     * della sintassi nelle risposte JSON composte manualmente.
     * 
     * @param input La stringa di testo da ripulire
     * @return String La stringa formattata con l'escape per JSON
     */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}