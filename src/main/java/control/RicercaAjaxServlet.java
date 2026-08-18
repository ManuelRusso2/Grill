package control;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.bean.ProdottoBean;
import model.dao.ProdottoDAO;
import model.dao.impl.ProdottoDAOImpl;

/**
 * RicercaAjaxServlet
 * Servlet controller progettata per gestire richieste asincrone (AJAX) relative alla 
 * ricerca in tempo reale dei prodotti (live search / autocompletamento).
 * Funzionalità principali:
 *   Riceve una stringa di ricerca inviata dal client.
 *   Esegue una pre-validazione della lunghezza della query per evitare interrogazioni onerose al DB.
 *   Recupera i prodotti corrispondenti tramite il pattern DAO.
 *   Costruisce manualmente una risposta formattata in JSON applicando l'escape dei caratteri speciali.
 *   Disabilita la memorizzazione in cache sul browser client per garantire dati sempre aggiornati.
 */
@WebServlet("/RicercaAjaxServlet")
public class RicercaAjaxServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Riferimento all'interfaccia DAO per l'accesso ai dati relativi ai prodotti
    private ProdottoDAO prodottoDAO;

    /**
     * Inizializza la Servlet creando l'istanza concreta di {@link ProdottoDAO}.
     * Eseguito una sola volta dal Servlet Container all'avvio.
     * 
     * @throws ServletException Se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void init() throws ServletException {
        this.prodottoDAO = new ProdottoDAOImpl();
    }

    /**
     * Gestisce le richieste HTTP GET inviate via AJAX per la ricerca automatica.
     * 
     * @param request  L'oggetto {@link HttpServletRequest} contenente il parametro "query"
     * @param response L'oggetto {@link HttpServletResponse} configurato per restituire un payload JSON
     * @throws ServletException Se si verifica un errore durante la gestione della richiesta
     * @throws IOException      Se si verifica un errore di I/O nella scrittura della risposta
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // =========================================================================
        // 1. CONFIGURAZIONE DEGLI HEADER DELLA RISPOSTA HTTP
        // Impostazione del MIME-Type application/json e codifica UTF-8
        // =========================================================================
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Disabilitazione della cache del client per evitare di mostrare risultati AJAX obsoleti
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        
        // =========================================================================
        // 2. LETTURA ED ESTRAZIONE DEL PARAMETRO DI RICERCA
        // =========================================================================
        String query = request.getParameter("query");
        
        // =========================================================================
        // 3. VALIDAZIONE DI BASE SULLA QUERY DI INGRESSO
        // Se la stringa è null, vuota o inferiore a 2 caratteri non interroga il DB 
        // e restituisce subito un array JSON vuoto "[]" per ottimizzare le prestazioni.
        // =========================================================================
        if (query == null || query.trim().length() < 2) {
            response.getWriter().print("[]");
            return;
        }

        PrintWriter out = response.getWriter();
        try {
            // =========================================================================
            // 4. RECUPERO DEI DATI TRAMITE DAO
            // Ricerca sul database dei prodotti il cui nome/descrizione soddisfa il parametro
            // =========================================================================
            List<ProdottoBean> prodottiTrovati = prodottoDAO.doRetrieveBySearch(query.trim());
            
            // =========================================================================
            // 5. SERIALIZZAZIONE E COSTRUZIONE MANUALE DEL PAYLOAD JSON
            // Utilizzo di StringBuilder per la concatenazione efficiente dei dati
            // =========================================================================
            StringBuilder json = new StringBuilder("[");
            
            if (prodottiTrovati != null) {
                for (int i = 0; i < prodottiTrovati.size(); i++) {
                    ProdottoBean p = prodottiTrovati.get(i);
                    
                    // Aggiunge la virgola separatrice tra gli oggetti JSON tranne che prima del primo
                    if (i > 0) {
                        json.append(",");
                    }
                    
                    // Sanitizzazione e sanitizzazione sintattica dei campi di testo (Escape JSON)
                    String nomeSanitized = escapeJson(p.getNome());
                    String immagineSanitized = escapeJson(p.getImmagine());
                    
                    // Costruzione dell'oggetto JSON del singolo prodotto
                    json.append("{")
                       .append("\"id\":").append(p.getIdProdotto()).append(",")
                       .append("\"nome\":\"").append(nomeSanitized).append("\",")
                       .append("\"prezzo\":").append(p.getCosto()).append(",")
                       .append("\"immagine\":\"").append(immagineSanitized).append("\"")
                       .append("}");
                }
            }
            json.append("]");
            
            // =========================================================================
            // 6. INVIO DELLA RISPOSTA JSON AL CLIENT
            // =========================================================================
            out.print(json.toString());
            out.flush();
            
        } catch (Exception e) {
            // Log dell'eccezione lato server e invio di stato HTTP 500 in caso di fallimento del DB
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("[]");
            out.flush();
        }
    }

    /**
     * Gestisce le richieste HTTP POST inoltrandole direttamente al metodo {@link #doGet}.
     * Permette alla Servlet di rispondere indifferentemente sia a chiamate GET che POST.
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

    /**
     * Metodo helper privato per eseguire l'escape dei caratteri speciali e di controllo
     * all'interno delle stringhe destinate ad essere serializzate in formato JSON.
     * 
     * Previene errori di sintassi nel client JavaScript ed evita problemi di parsing
     * in presenza di virgolette, backslash o caratteri di nuova riga nei nomi/percorsi dei prodotti.
     * 
     * @param input La stringa di testo originale da verificare
     * @return La stringa convertita con i caratteri speciali protetti tramite backslash (escape),
     *         oppure una stringa vuota se l'input è {@code null}.
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}