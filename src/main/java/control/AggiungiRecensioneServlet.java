package control;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.bean.RecensioneBean;
import model.bean.UtenteBean;
import model.dao.RecensioneDAO;
import model.dao.impl.RecensioneDAOImpl;

/**
 * Servlet per la gestione della creazione e pubblicazione delle recensioni sui prodotti.
 */
@WebServlet("/AggiungiRecensioneServlet")
public class AggiungiRecensioneServlet extends HttpServlet {
    
    // Identificatore univoco per la serializzazione della classe HttpServlet
    private static final long serialVersionUID = 1L;
    
    // Interfaccia DAO per accedere alle operazioni di persistenza delle recensioni
    private RecensioneDAO recensioneDAO;

    /**
     * Metodo di inizializzazione della Servlet.
     * Viene eseguito una sola volta all'atto della prima invocazione.
     * Inizializza il DAO delle recensioni.
     */
    @Override
    public void init() throws ServletException {
        this.recensioneDAO = new RecensioneDAOImpl();
    }

    /**
     * Gestisce le richieste HTTP POST inviate dai form di inserimento recensione.
     * 
     * @param request  L'oggetto HttpServletRequest contenente i dati della richiesta
     * @param response L'oggetto HttpServletResponse per inviare risposte/redirect
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // Recupera la sessione corrente senza crearne una nuova se non esiste (false)
        HttpSession session = request.getSession(false);
        
        // Estrae l'oggetto utente dalla sessione (se la sessione esiste)
        UtenteBean utente = (session != null) ? (UtenteBean) session.getAttribute("utente") : null;

        // Controllo autenticazione e autorizzazione: 
        // Se l'utente non è loggato o è un amministratore (gli admin non votano i prodotti)
        if (utente == null || utente.isAdmin()) {
            // Reindirizza l'utente alla pagina di Login
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return; // Interrompe l'esecuzione del metodo
        }

        // Estrazione e pulizia dei parametri inviati dal form tramite metodo helper
        String idProdottoParam = getTrimmedParam(request, "idProdotto");
        String descrizione = getTrimmedParam(request, "descrizione");
        String valutazioneParam = getTrimmedParam(request, "valutazione");

        // Verifica che tutti i parametri necessari siano stati inviati e non siano vuoti
        if (idProdottoParam != null && valutazioneParam != null && descrizione != null) {
            
            // Inizializza l'ID prodotto con valore sentinella
            int idProdotto = -1;
            
            try {
                // Parsing delle stringhe nei rispettivi tipi numerici
                idProdotto = Integer.parseInt(idProdottoParam);
                double valutazione = Double.parseDouble(valutazioneParam);

                // Controllo del range valido per il punteggio espresso (es. scala da 1.0 a 5.0)
                if (valutazione < 1.0 || valutazione > 5.0) {
                    // Imposta il messaggio d'errore in sessione
                    setSessionAttribute(request, "errorMessage", "La valutazione deve essere compresa tra 1 e 5.");
                    
                    // Reindirizza alla pagina di dettaglio dello specifico prodotto
                    response.sendRedirect(request.getContextPath() + "/DettaglioProdottoServlet?id=" + idProdotto);
                    return; // Interrompe l'esecuzione
                }

                // Creazione del Javabean RecensioneBean con i dati ricevuti
                RecensioneBean recensione = new RecensioneBean();
                recensione.setDescrizione(descrizione);
                recensione.setValutazione(valutazione);
                recensione.setIdProdotto(idProdotto);
                recensione.setIdUtente(utente.getIdUtente()); // Associa l'ID dell'utente in sessione

                // Persiste la recensione nel database tramite il DAO
                recensioneDAO.doSave(recensione);

                // Notifica l'utente del successo dell'operazione salvando un messaggio in sessione
                setSessionAttribute(request, "successMessage", "Recensione pubblicata con successo!");
                
                // Reindirizza l'utente alla pagina del dettaglio del prodotto recensito
                response.sendRedirect(request.getContextPath() + "/DettaglioProdottoServlet?id=" + idProdotto);
                return;

            } catch (NumberFormatException e) {
                // Gestione eccezione se idProdotto o valutazione non sono numeri validi
                setSessionAttribute(request, "errorMessage", "Formato dei dati della recensione non valido.");
                
                // Se l'ID prodotto era valido, reindirizza alla pagina del prodotto
                if (idProdotto > 0) {
                    response.sendRedirect(request.getContextPath() + "/DettaglioProdottoServlet?id=" + idProdotto);
                    return;
                }
            } catch (SQLException e) {
                // Gestione eccezioni relative all'accesso o salvataggio sul Database
                e.printStackTrace(); // Log dell'errore a console
                setSessionAttribute(request, "errorMessage", "Errore durante il salvataggio della recensione.");
                
                // Se l'ID prodotto era valido, reindirizza alla pagina del prodotto
                if (idProdotto > 0) {
                    response.sendRedirect(request.getContextPath() + "/DettaglioProdottoServlet?id=" + idProdotto);
                    return;
                }
            }
        } else if (idProdottoParam != null) {
            // Se l'ID del prodotto c'era ma mancava la descrizione o la valutazione
            setSessionAttribute(request, "errorMessage", "Compilare tutti i campi della recensione.");
            response.sendRedirect(request.getContextPath() + "/DettaglioProdottoServlet?id=" + idProdottoParam);
            return;
        }

        // Fallback: Se mancava persino l'ID del prodotto o si sono verificati errori irrecuperabili,
        // reindirizza l'utente al catalogo generale dei prodotti
        response.sendRedirect(request.getContextPath() + "/CatalogoServlet");
    }

    // =========================================================================
    // METODI HELPER
    // =========================================================================

    /**
     * Estrae un parametro dalla richiesta HTTP eliminando gli spazi bianchi iniziali e finali.
     * 
     * @param request La richiesta HTTP
     * @param name    Il nome del parametro da recuperare
     * @return La stringa pulita o null se assente/vuota
     */
    private String getTrimmedParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        // Se il parametro esiste e non è composto solo da spazi bianchi restituisce la stringa con trim(), altrimenti null
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        return null;
    }

    /**
     * Salva un messaggio/attributo temporaneo all'interno della sessione utente.
     * 
     * @param request La richiesta HTTP
     * @param key     La chiave identificativa dell'attributo in sessione
     * @param value   Il messaggio/valore da memorizzare
     */
    private void setSessionAttribute(HttpServletRequest request, String key, String value) {
        // Ottiene la sessione (creandola se non esiste ancora) e vi assegna l'attributo
        request.getSession(true).setAttribute(key, value);
    }
}