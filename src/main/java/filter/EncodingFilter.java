package filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * EncodingFilter
 * Filtro globale di sistema applicato a tutte le rotte dell'applicazione ({@code /*}).
 * 
 * Garantisce che l'encoding dei caratteri per tutte le richieste HTTP in ingresso
 * e le relative risposte in uscita sia uniformemente impostato su UTF-8.
 * 
 * Previene la corruzione o il malfunzionamento dei dati contenenti caratteri accentati,
 * simboli o maiuscole speciali inoltrati tramite form di input (es. descrizioni prodotti,
 * recensioni, dati anagrafici e indirizzi di spedizione).
 */
@WebFilter(urlPatterns = "/*")
public class EncodingFilter implements Filter {

    private String encoding;

    /**
     * Inizializza il filtro impostando il set di caratteri predefinito a UTF-8.
     * Metodo eseguito dal Servlet Container una sola volta all'avvio del contesto.
     * 
     * @param filterConfig La configurazione del filtro fornita dal Servlet Container
     * @throws ServletException Se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.encoding = "UTF-8";
    }

    /**
     * Intercetta ogni richiesta e risposta HTTP applicando la codifica UTF-8
     * prima dell'elaborazione dei parametri di input da parte delle Servlet target.
     * 
     * @param request  L'oggetto {@link ServletRequest} generico
     * @param response L'oggetto {@link ServletResponse} generico
     * @param chain    La catena dei filtri {@link FilterChain} per inoltrare la richiesta
     * @throws IOException      Se si verifica un errore di I/O
     * @throws ServletException Se si verifica un errore a livello di Servlet/Filter
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // =========================================================================
        // 1. CASTING DEGLI OGGETTI DI RICHIESTA E RISPOSTA ALL'AMBITO HTTP
        // =========================================================================
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // =========================================================================
        // 2. FORZATURA DELLA CODIFICA UTF-8 SULLA RICHIESTA
        // Imposta l'encoding soltanto se non è già stato definito explicitamente,
        // garantendo la corretta lettura dei parametri inviati tramite POST/GET.
        // =========================================================================
        if (httpRequest.getCharacterEncoding() == null) {
            httpRequest.setCharacterEncoding(this.encoding);
        }

        // =========================================================================
        // 3. FORZATURA DELLA CODIFICA UTF-8 SULLA RISPOSTA
        // Garantisce che il payload generato (HTML, JSON, Text) utilizzi UTF-8.
        // =========================================================================
        httpResponse.setCharacterEncoding(this.encoding);

        // =========================================================================
        // 4. PROSEGUIMENTO NELLA CATENA DI FILTRI
        // =========================================================================
        chain.doFilter(request, response);
    }

    /**
     * Rilascia le risorse allocate dal filtro prima della sua dismissione.
     */
    @Override
    public void destroy() {
        // Nessuna risorsa esplicita da rilasciare
    }
}