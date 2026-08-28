package control;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.bean.UtenteBean;
import model.dao.UtenteDAO;
import model.dao.impl.UtenteDAOImpl;

/**
 *LoginServlet
 * Servlet controller responsabile della gestione dell'autenticazione degli utenti (processo di Login).
 * Implementa l'architettura MVC (Model-View-Controller):
 *   GET: Verifica l'eventuale sessione già attiva e smista l'utente verso la vista di Login o la Home.
 *   POST: Riceve le credenziali dal form, le valida, le verifica sul database tramite DAO e gestisce la sessione in sicurezza.
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Riferimento all'interfaccia DAO per l'accesso ai dati utente
    private UtenteDAO utenteDAO;

    /**
     * Metodo di inizializzazione della Servlet.
     * Viene eseguito una sola volta dal Servlet Container durante il ciclo di vita iniziale.
     * Istanzia l'implementazione concreta del data access object {@link UtenteDAO}.
     * 
     * @throws ServletException Se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void init() throws ServletException {
        this.utenteDAO = new UtenteDAOImpl();
    }

    /**
     * Gestisce le richieste HTTP GET.
     * Mostra il form di login oppure reindirizza l'utente alla home se risulta già autenticato.
     * 
     * @param request  L'oggetto {@link HttpServletRequest} contenente la richiesta del client
     * @param response L'oggetto {@link HttpServletResponse} per inviare la risposta al client
     * @throws ServletException Se si verifica un errore durante il dispatching della richiesta
     * @throws IOException      Se si verifica un errore di I/O durante il redirect/forward
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // =========================================================================
        // 1. CONTROLLO SESSIONE ESISTENTE
        // Richiede la sessione corrente senza crearne una nuova (false)
        // =========================================================================
        HttpSession session = request.getSession(false);
        
        // Se l'utente è già loggato (attributo "utente" presente in sessione),
        // evita di mostrare nuovamente il form e reindirizza direttamente alla pagina principale
        if (session != null && session.getAttribute("utente") != null) {
            response.sendRedirect(request.getContextPath() + "/jsp/common/home.jsp");
            return; // Interrompe l'esecuzione del metodo
        }

        // =========================================================================
        // 2. DISPATCHING ALLA VISTA
        // Se l'utente non è loggato, inoltra la richiesta alla pagina JSP del form di login
        // =========================================================================
        request.getRequestDispatcher("/jsp/common/login.jsp").forward(request, response);
    }

    /**
     * Gestisce le richieste HTTP POST.
     * Processa le credenziali inviate dal form di login, effettua le verifiche sul DB
     * e stabilisce una nuova sessione utente in caso di successo.
     * 
     * @param request  L'oggetto {@link HttpServletRequest} contenente i parametri 'email' e 'password'
     * @param response L'oggetto {@link HttpServletResponse} per inviare la risposta o eseguire un reindirizzamento
     * @throws ServletException Se si verifica un errore durante il dispatching della richiesta
     * @throws IOException      Se si verifica un errore di I/O durante il redirect/forward
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // =========================================================================
        // 1. ESTRAZIONE PARAMETRI DAL FORM
        // =========================================================================
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // =========================================================================
        // 2. VALIDAZIONE FORMALE DEGLI INPUT
        // Controlla che i campi non siano nulli, vuoti o composti solo da spazi
        // =========================================================================
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Email e password sono obbligatorie.");
            request.getRequestDispatcher("/jsp/common/login.jsp").forward(request, response);
            return; // Interrompe il flusso per impedire chiamate al DB non necessarie
        }

        // Normalizzazione dell'email (rimozione degli spazi bianchi a capo e coda)
        email = email.trim();

        try {
            // =========================================================================
            // 3. VERIFICA CREDENZIALI TRAMITE DAO
            // Richiama il layer di persistenza per verificare la corrispondenza delle credenziali
            // =========================================================================
            UtenteBean utente = utenteDAO.doRetrieveByLogin(email, password);

            if (utente != null) {
                // =========================================================================
                // 4. SICUREZZA: PREVENZIONE SESSION FIXATION
                // Se esiste una sessione precedente anonima o non autenticata, viene invalidata
                // per forzare la generazione di un nuovo Session ID sicuro dopo il login.
                // =========================================================================
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }

                // =========================================================================
                // 5. CREAZIONE NUOVA SESSIONE AUTENTICATA
                // =========================================================================
                HttpSession session = request.getSession(true);
                
                // Salvataggio del JavaBean dell'utente all'interno della sessione
                session.setAttribute("utente", utente);

                // Reindirizzamento alla Home per evitare 
                // l'invio duplicato dei dati del form in caso di refresh della pagina
                response.sendRedirect(request.getContextPath() + "/jsp/common/home.jsp");
                
            } else {
                // =========================================================================
                // 6. GESTIONE AUTENTICAZIONE FALLITA
                // SICUREZZA: Si utilizza un messaggio generico per prevenire l'enumeration
                // degli account (impedisce ad un attaccante di sapere se l'email esiste).
                // =========================================================================
                request.setAttribute("errorMessage", "Email o password errate. Riprova.");
                request.getRequestDispatcher("/jsp/common/login.jsp").forward(request, response);
            }

        } catch (SQLException e) {
            // Log dell'eccezione sul lato server per scopi di debugging
            e.printStackTrace();
            
            // Invio di un errore HTTP 500 (Internal Server Error) al client
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno durante il login.");
        }
    }
}