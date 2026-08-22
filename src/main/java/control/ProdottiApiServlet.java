package control;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
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
 * ProdottiApiServlet
 * API REST Servlet per la fornitura dinamica di un elenco casuale di prodotti in formato JSON.
 * Viene utilizzata principalmente da chiamate AJAX lato client per popolare widget dinamici
 * o sezioni in evidenza (come caroselli o vetrine prodotti nella homepage).
 */
@WebServlet("/api/prodotti")
public class ProdottiApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Riferimento al DAO per la gestione dell'accesso ai dati del catalogo prodotti
    private ProdottoDAO prodottoDAO;

    /**
     * Inizializza la servlet e le relative dipendenze.
     * Istanzia l'implementazione del DAO per l'accesso al database.
     * 
     * @throws ServletException Se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void init() throws ServletException {
        this.prodottoDAO = new ProdottoDAOImpl();
    }

    /**
     * Gestisce le richieste HTTP GET per restituire un sottoinsieme casuale di prodotti in formato JSON.
     * 
     * @param request  L'oggetto {@link HttpServletRequest} contenente la richiesta del client
     * @param response L'oggetto {@link HttpServletResponse} configurato con MIME type JSON e headers anti-cache
     * @throws ServletException Se si verifica un errore a livello di Servlet
     * @throws IOException      Se si verifica un errore di I/O nella scrittura della risposta
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // =========================================================================
        // 1. CONFIGURAZIONE HEADERS HTTP DI RISPOSTA
        // Imposta il tipo di contenuto come JSON e codifica UTF-8
        // Disabilita la cache lato client per garantire dati sempre aggiornati ad ogni chiamata AJAX
        // =========================================================================
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        
        PrintWriter out = response.getWriter();
        
        try {
            // =========================================================================
            // 2. RECUPERO E ELABORAZIONE DATI DAL DAO
            // =========================================================================
            List<ProdottoBean> prodotti = prodottoDAO.doRetrieveAllProdottiRaggruppati();
            
            // Gestione di sicurezza in caso di lista nulla dal DB
            if (prodotti == null) {
                prodotti = new ArrayList<>();
            }
            
            // Mescola casualmente la lista per variare i prodotti presentati al client
            Collections.shuffle(prodotti);
            
            // Calcola il limite massimo di elementi da restituire (max 10) in modo sicuro
            int limite = Math.min(prodotti.size(), 10);
            List<ProdottoBean> prodottiLimitati = new ArrayList<>(prodotti.subList(0, limite));
            
            // =========================================================================
            // 3. SERIALIZZAZIONE E COSTROZIONE DEL PAYLOAD JSON
            // Costruzione manuale della stringa JSON con sanitizzazione dei caratteri speciali
            // =========================================================================
            StringBuilder json = new StringBuilder("[");
            
            for (int i = 0; i < prodottiLimitati.size(); i++) {
                ProdottoBean p = prodottiLimitati.get(i);
                
                // Aggiunge la virgola di separazione tra gli oggetti JSON (escludendo il primo)
                if (i > 0) {
                    json.append(",");
                }
                
                // Escape delle stringhe per prevenire malformazioni nella sintassi JSON
                String nomeSanitized = escapeJson(p.getNome());
                String immagineSanitized = escapeJson(p.getImmagine());
                
                // Costruzione dell'oggetto JSON
                json.append("{")
                   .append("\"idProdotto\":").append(p.getIdProdotto()).append(",")
                   .append("\"nome\":\"").append(nomeSanitized).append("\",")
                   .append("\"costo\":").append(p.getCosto()).append(",")
                   .append("\"immagine\":\"").append(immagineSanitized).append("\"")
                   .append("}");
            }
            json.append("]");
            
            // Invio della risposta JSON al client
            out.print(json.toString());
            out.flush();
            
        } catch (Exception e) {
            // =========================================================================
            // 4. GESTIONE ECCEZIONI
            // Log dell'errore lato server e restituzione di un array JSON vuoto con HTTP 500
            // =========================================================================
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("[]");
            out.flush();
        }
    }

    /**
     * Gestisce le richieste HTTP POST delegandole al metodo {@link #doGet(HttpServletRequest, HttpServletResponse)}.
     * Permette il recupero dei prodotti anche tramite invii in POST.
     * 
     * @param request  L'oggetto {@link HttpServletRequest}
     * @param response L'oggetto {@link HttpServletResponse}
     * @throws ServletException Se si verifica un errore a livello di Servlet
     * @throws IOException      Se si verifica un errore di I/O
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    /**
     * Metodo di utility privato per eseguire l'escape dei caratteri di controllo
     * e speciali all'interno delle stringhe, garantendo che l'output sia un JSON valido.
     * 
     * @param input La stringa di testo da sanitizzare
     * @return La stringa con i caratteri speciali convertiti nel formato JSON escape, 
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