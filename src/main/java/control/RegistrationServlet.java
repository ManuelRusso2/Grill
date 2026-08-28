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
 * RegistrationServlet
 * Servlet controller responsabile della registrazione dei nuovi utenti nel sistema.
 * Implementa le seguenti funzionalità operative e di sicurezza:
 *   GET: Reindirizza alla Home se l'utente è già autenticato; in caso contrario, mostra la form di registrazione.
 *   POST: Riceve i dati dal modulo, li sanitizza, esegue una complessa validazione tramite Regex, 
 *       verifica la disponibilità dell'email sul database, persiste il nuovo utente e gestisce l'auto-login in sicurezza.
 */
@WebServlet("/RegistrationServlet")
public class RegistrationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Riferimento all'interfaccia DAO per le operazioni di lettura e scrittura degli utenti
    private UtenteDAO utenteDAO;

    /**
     * Inizializza la Servlet creando l'istanza concreta di {@link UtenteDAO}.
     * Eseguito una sola volta dal Servlet Container all'avvio.
     * 
     * @throws ServletException Se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void init() throws ServletException {
        this.utenteDAO = new UtenteDAOImpl();
    }

    /**
     * Gestisce le richieste HTTP GET.
     * Controlla lo stato della sessione per evitare che un utente già connesso possa accedere nuovamente al form di registrazione.
     * 
     * @param request  L'oggetto {@link HttpServletRequest}
     * @param response L'oggetto {@link HttpServletResponse}
     * @throws ServletException Se si verifica un errore durante il dispatching della richiesta
     * @throws IOException      Se si verifica un errore d'I/O durante il redirect o forward
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // =========================================================================
        // 1. VERIFICA SESSIONE ESISTENTE
        // Evita di mostrare la registrazione agli utenti già autenticati
        // =========================================================================
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("utente") != null) {
            response.sendRedirect(request.getContextPath() + "/jsp/common/home.jsp");
            return;
        }

        // =========================================================================
        // 2. INOLTRO ALLA VISTA
        // Mostra il form di registrazione tramite dispatching interno
        // =========================================================================
        request.getRequestDispatcher("/jsp/user/registrazione.jsp").forward(request, response);
    }

    /**
     * Gestisce le richieste HTTP POST per processare l'invio del form di registrazione.
     * Effettua la sanitizzazione, la validazione, il controllo di unicità email e la registrazione.
     * 
     * @param request  L'oggetto {@link HttpServletRequest} contenente i campi inviati dal form
     * @param response L'oggetto {@link HttpServletResponse}
     * @throws ServletException Se si verifica un errore durante l'inoltro
     * @throws IOException      Se si verifica un errore di I/O o durante l'invio dell'errore HTTP
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // =========================================================================
        // 1. ESTRAZIONE E SANITIZZAZIONE DEI PARAMETRI
        // Trim per rimuovere spazi vuoti iniziali/finali e prevenzione dei riferimenti null
        // =========================================================================
        String nome     = request.getParameter("nome");
        String cognome  = request.getParameter("cognome");
        String email    = request.getParameter("email");
        String password = request.getParameter("password");
        String telefono = request.getParameter("telefono");

        nome     = nome     != null ? nome.trim()     : "";
        cognome  = cognome  != null ? cognome.trim()  : "";
        email    = email    != null ? email.trim()    : "";
        password = password != null ? password.trim() : "";
        telefono = telefono != null ? telefono.trim() : "";

        // =========================================================================
        // 2. DEFINIZIONE DELLE REGOLE DI VALIDAZIONE (REGEX)
        // =========================================================================
        
     // =========================================================================
     // REGEX VALIDAZIONE EMAIL
     // =========================================================================
     // ^                  : Inizio della stringa
     // [A-Za-z0-9+_.-]+   : Nome utente (lettere, numeri e caratteri +, _, ., -)
     // @                  : Simbolo chiocciola obbligatorio
     // [A-Za-z0-9.-]+     : Nome del dominio (lettere, numeri, punti e trattini)
     // \\.                : Punto letterale che separa dominio ed estensione (serve \\ per l'escape)
     // [a-zA-Z]{2,}       : Estensione del dominio (es. .it, .com) con almeno 2 lettere
     // $                  : Fine della stringa
     String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$";


     // =========================================================================
     // REGEX VALIDAZIONE NUMERO DI TELEFONO
     // =========================================================================
     // ^                  : Inizio della stringa
     // [0-9+\\s\\-]       : Caratteri consentiti: cifre (0-9), prefisso (+), spazi (\\s) o trattini (\\-)
     // {7,20}             : Il numero totale di caratteri deve essere compreso tra 7 e 20
     // $                  : Fine della stringa
     String telefonoRegex = "^[0-9+\\s\\-]{7,20}$";
        boolean hasErrors = false;

        // =========================================================================
        // 3. VALIDAZIONE LATO SERVER DEI SINGOLI CAMPI
        // =========================================================================
        // Validazione Nome
        if (nome.isEmpty()) {
            request.setAttribute("errNome", "Il nome è obbligatorio.");
            hasErrors = true;
        } else if (nome.length() < 2) {
            request.setAttribute("errNome", "Il nome deve contenere almeno 2 caratteri.");
            hasErrors = true;
        }

        // Validazione Cognome
        if (cognome.isEmpty()) {
            request.setAttribute("errCognome", "Il cognome è obbligatorio.");
            hasErrors = true;
        } else if (cognome.length() < 2) {
            request.setAttribute("errCognome", "Il cognome deve contenere almeno 2 caratteri.");
            hasErrors = true;
        }

        // Validazione Email (Obbligatorietà e formato sintetico corretto)
        if (email.isEmpty()) {
            request.setAttribute("errEmail", "L'email è obbligatoria.");
            hasErrors = true;
        } else if (!email.matches(emailRegex)) {
            request.setAttribute("errEmail", "Inserisci un indirizzo email valido (es. nome@dominio.it).");
            hasErrors = true;
        }

        // Validazione Password (Obbligatorietà, lunghezza minima, presenza di maiuscole e numeri)
        if (password.isEmpty()) {
            request.setAttribute("errPassword", "La password è obbligatoria.");
            hasErrors = true;
        } else if (password.length() < 6) {
            request.setAttribute("errPassword", "La password deve contenere almeno 6 caratteri.");
            hasErrors = true;
        } else if (!password.matches(".*[A-Z].*")) {
            request.setAttribute("errPassword", "La password deve contenere almeno una lettera maiuscola.");
            hasErrors = true;
        } else if (!password.matches(".*[0-9].*")) {
            request.setAttribute("errPassword", "La password deve contenere almeno un numero.");
            hasErrors = true;
        }

        // Validazione Telefono (Campo opzionale: validato solo se valorizzato)
        if (!telefono.isEmpty() && !telefono.matches(telefonoRegex)) {
            request.setAttribute("errTelefono", "Inserisci un numero di telefono valido (es. 333 1234567).");
            hasErrors = true;
        }

        // Se sono stati riscontrati errori nei dati inviati, ricarica la form mostrando i messaggi di errore
        if (hasErrors) {
            ripopolaFormEInoltra(request, response, nome, cognome, email, telefono);
            return;
        }

        try {
            // =========================================================================
            // 4. VERIFICA UNICITÀ EMAIL SUL DATABASE
            // Evita la registrazione di due account con il medesimo indirizzo email
            // =========================================================================
            if (utenteDAO.doRetrieveByEmail(email) != null) {
                request.setAttribute("errEmail", "Questa email è già registrata.");
                ripopolaFormEInoltra(request, response, nome, cognome, email, telefono);
                return;
            }

            // =========================================================================
            // 5. CREAZIONE E PERSISTENZA DEL NUOVO UTENTE
            // Mappatura delle proprietà all'interno del Javabean UtenteBean
            // =========================================================================
            UtenteBean nuovoUtente = new UtenteBean();
            nuovoUtente.setNome(nome);
            nuovoUtente.setCognome(cognome);
            nuovoUtente.setEmail(email);
            nuovoUtente.setPassword(password);
            nuovoUtente.setTelefono(telefono.isEmpty() ? null : telefono);
            nuovoUtente.setAdmin(false); // Di default, ogni nuova registrazione crea un utente standard (non admin)

            // Salvataggio nel database tramite DAO
            utenteDAO.doSave(nuovoUtente);

            // =========================================================================
            // 6. AUTO-LOGIN POST REGISTRAZIONE E PREVENZIONE SESSION FIXATION
            // Invalida l'eventuale sessione preesistente prima di crearne una nuova autenticata
            // =========================================================================
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            // Creazione della nuova sessione e salvataggio dell'oggetto utente
            HttpSession session = request.getSession(true);
            session.setAttribute("utente", nuovoUtente);

            // Reindirizzamento alla Home Page
            response.sendRedirect(request.getContextPath() + "/jsp/common/home.jsp");

        } catch (SQLException e) {
            // Log dell'errore lato server e restituzione del codice HTTP 500
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante la registrazione dell'utente.");
        }
    }

    /**
     * Metodo helper privato per la gestione del flusso d'errore della form.
     * Inserisce i valori immessi dall'utente tra gli attributi della richiesta (evitando che debba reinserirli da capo)
     * e inoltra la richiesta alla vista della registrazione per la notifica degli errori.
     * 
     * @param request  L'oggetto {@link HttpServletRequest}
     * @param response L'oggetto {@link HttpServletResponse}
     * @param nome     Il nome inserito dall'utente
     * @param cognome  Il cognome inserito dall'utente
     * @param email    L'email inserita dall'utente
     * @param telefono Il numero di telefono inserito dall'utente
     * @throws ServletException Se si verifica un errore durante il dispatching
     * @throws IOException      Se si verifica un errore di I/O
     */
    private void ripopolaFormEInoltra(HttpServletRequest request, HttpServletResponse response,
                                     String nome, String cognome, String email, String telefono)
            throws ServletException, IOException {
        request.setAttribute("formNome", nome);
        request.setAttribute("formCognome", cognome);
        request.setAttribute("formEmail", email);
        request.setAttribute("formTelefono", telefono);
        request.getRequestDispatcher("/jsp/user/registrazione.jsp").forward(request, response);
    }
}