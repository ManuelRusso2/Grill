package control;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.bean.UtenteBean;
import model.dao.UtenteDAO;
import model.dao.impl.UtenteDAOImpl;

/**
 * VerificaEmailServlet
 * Servlet controller asincrona (AJAX) preposta alla verifica dell'esistenza
 * di un indirizzo email all'interno del database.
 * Viene utilizzata principalmente nella fase di compilazione in tempo reale
 * della form di registrazione lato client per fornire un feedback immediato all'utente
 * sulla disponibilità dell'email scelta.
 */
@WebServlet("/VerificaEmailServlet")
public class VerificaEmailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Riferimento all'interfaccia DAO per le operazioni di lettura sugli utenti
    private UtenteDAO utenteDAO;

    /**
     * Inizializza la Servlet creando l'istanza concreta di {@link UtenteDAO}.
     * Eseguito una sola volta dal Servlet Container all'avvio del ciclo di vita.
     * 
     * @throws ServletException Se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void init() throws ServletException {
        this.utenteDAO = new UtenteDAOImpl();
    }

    /**
     * Gestisce le richieste HTTP GET per la verifica asincrona dell'email.
     * Legge l'indirizzo email dai parametri della richiesta, interroga il database 
     * e restituisce una risposta formattata in JSON con la chiave {@code exists} (boolean).
     * 
     * @param request  L'oggetto {@link HttpServletRequest} contenente il parametro "email"
     * @param response L'oggetto {@link HttpServletResponse} configurato per restituire un payload JSON
     * @throws ServletException Se si verifica un errore generico della Servlet
     * @throws IOException      Se si verifica un errore di I/O nella scrittura della risposta
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // =========================================================================
        // 1. CONFIGURAZIONE DEGLI HEADER HTTP E DISABILITAZIONE CACHE
        // Impostazione del MIME-Type application/json e UTF-8.
        // I disabilitatori di cache garantiscono risposte sempre fresche da parte dei browser.
        // =========================================================================
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        
        // Estraggo il parametro 'email' inviato dalla chiamata client
        String email = request.getParameter("email");
        boolean exists = false;

        PrintWriter out = response.getWriter();

        try {
            // =========================================================================
            // 2. VALIDAZIONE DELL'INPUT ED INTERROGAZIONE DEL DATABASE
            // Se la stringa è valida e non vuota, verifica la presenza dell'utente via DAO
            // =========================================================================
            if (email != null && !email.trim().isEmpty()) {
                UtenteBean utente = utenteDAO.doRetrieveByEmail(email.trim());
                if (utente != null) {
                    exists = true; // Email già presente a sistema
                }
            }
            
            // =========================================================================
            // 3. GENERAZIONE ED INVIO DELLA RISPOSTA JSON
            // Es: {"exists": true} oppure {"exists": false}
            // =========================================================================
            out.print("{\"exists\": " + exists + "}");
            out.flush();

        } catch (SQLException e) {
            // =========================================================================
            // 4. GESTIONE DEGLI ERRORI DI DATABASE
            // Log dell'eccezione lato server, impostazione del codice HTTP 500
            // e restituzione di una risposta JSON esplicativa
            // =========================================================================
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"exists\": false, \"error\": \"Errore di connessione al database\"}");
            out.flush();
        }
    }

    /**
     * Gestisce le richieste HTTP POST inoltrandole direttamente al metodo {@link #doGet}.
     * Permette alla Servlet di rispondere trasparentemente sia a chiamate in GET che in POST.
     * 
     * @param request  L'oggetto {@link HttpServletRequest}
     * @param response L'oggetto {@link HttpServletResponse}
     * @throws ServletException Se si verifica un errore durante il passaggio del controllo
     * @throws IOException      Se si verifica un errore di I/O
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}