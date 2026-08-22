package control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.bean.CategoriaBean;
import model.bean.ProdottoBean;
import model.bean.RecensioneBean;
import model.bean.UtenteBean;
import model.dao.CategoriaDAO;
import model.dao.ProdottoDAO;
import model.dao.RecensioneDAO;
import model.dao.impl.CategoriaDAOImpl;
import model.dao.impl.ProdottoDAOImpl;
import model.dao.impl.RecensioneDAOImpl;

/**
 * Servlet per la visualizzazione dettagliata di un singolo prodotto del catalogo.
 * Recupera ed elabora le informazioni relative al prodotto selezionato, incluse le sue 
 * eventuali varianti (es. taglia/colore), l'elenco delle categorie disponibili per il menu di navigazione, 
 * le recensioni lasciate dagli utenti e il ruolo dell'utente correntemente autenticato.
 */
@WebServlet("/DettaglioProdottoServlet")
public class DettaglioProdottoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Oggetti DAO per l'accesso ai dati persistenti
    private ProdottoDAO prodottoDAO;
    private CategoriaDAO categoriaDAO;
    private RecensioneDAO recensioneDAO;

    /**
     * Inizializza le istanze dei DAO necessari al funzionamento della Servlet
     * per recuperare prodotti, categorie e recensioni dal database.
     */
    @Override
    public void init() throws ServletException {
        this.prodottoDAO = new ProdottoDAOImpl();
        this.categoriaDAO = new CategoriaDAOImpl();
        this.recensioneDAO = new RecensioneDAOImpl();
    }

    /**
     * Gestisce le richieste HTTP GET per il recupero e la visualizzazione del dettaglio prodotto.
     * 
     * @param request  L'oggetto {@link HttpServletRequest} contenente la richiesta del client (es. parametro "id")
     * @param response L'oggetto {@link HttpServletResponse} per inviare la risposta al client
     * @throws ServletException Se si verifica un errore durante il dispatch della richiesta
     * @throws IOException      Se si verifica un errore di I/O durante il rendirizzamento o dispatch
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Validazione del parametro "id" fornito nell'URL
        String idParam = getTrimmedParam(request, "id");

        if (idParam == null) {
            // Se il parametro 'id' manca o è vuoto, reindirizza l'utente al catalogo generale
            response.sendRedirect(request.getContextPath() + "/CatalogoServlet");
            return;
        }

        try {
            // Conversione del parametro stringa dell'ID in intero
            int idProdotto = Integer.parseInt(idParam);

            // 2. Recupero del prodotto selezionato dal database
            ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);

            // Se il prodotto non esiste oppure è disattivato (soft-delete), restituisce errore HTTP 404 (Not Found)
            if (prodotto == null || !prodotto.isAttivo()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Salva il prodotto nello scope della richiesta
            request.setAttribute("prodotto", prodotto);

            // 3. Recupero di tutte le categorie per popolare il menu di navigazione o filtri
            List<CategoriaBean> allCategorie = categoriaDAO.doRetrieveAll();
            request.setAttribute("categorie", allCategorie);

            // 4. Gestione delle varianti di prodotto (es. "Maglietta - Rossa", "Maglietta - Blu")
            String nomeBase = estraiNomeBase(prodotto.getNome());
            List<ProdottoBean> varianti = prodottoDAO.doRetrieveVarianti(nomeBase);
            
            // Imposta le varianti nella richiesta solo se ne esiste più di una
            if (varianti != null && varianti.size() > 1) {
                request.setAttribute("varianti", varianti);
                request.setAttribute("nomeBase", nomeBase);
            }

            // 5. Recupero delle recensioni degli utenti per il prodotto specifico
            List<RecensioneBean> recensioni = recensioneDAO.doRetrieveByProdotto(idProdotto);
            request.setAttribute("recensioni", recensioni);

            // 6. Verifica dei permessi dell'utente loggato (se presente) per abilitare funzioni admin sulla JSP
            UtenteBean utente = getLoggedUser(request);
            if (utente != null) {
                request.setAttribute("isAdmin", utente.isAdmin());
            }

            // Inoltro della richiesta e dei dati raccolti alla vista JSP per il rendering
            request.getRequestDispatcher("/jsp/common/dettaglio-prodotto.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            // Se l'ID fornito nell'URL non è un numero intero valido -> HTTP 400 Bad Request
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            // Log dell'eccezione SQL e restituzione di un errore generico -> HTTP 500 Internal Server Error
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gestisce le richieste HTTP POST reindirizzandole internamente al metodo {@link #doGet}.
     * 
     * @param request  La richiesta HTTP
     * @param response La risposta HTTP
     * @throws ServletException Se si verifica un errore durante l'elaborazione
     * @throws IOException      Se si verifica un errore di I/O
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    // =========================================================================
    // METODI HELPER
    // =========================================================================

    /**
     * Estrae la radice del nome di un prodotto rimuovendo il suffisso della variante.
     * 
     * Se il nome contiene la sequenza " - " (es. "Scarpe Nike - 42"), restituisce solo
     * la porzione precedente ("Scarpe Nike"). Altrimenti restituisce l'intero nome.
     * 
     * @param nome Il nome completo del prodotto
     * @return La stringa contenente il nome base del prodotto
     */
    private String estraiNomeBase(String nome) {
        // 1. Se il nome è nullo, restituisco una stringa vuota per evitare errori
        if (nome == null) {
            return "";
        }

        // 2. Se nel nome c'è il separatore " - ", tagliamo la stringa
        if (nome.contains(" - ")) {
            int posizioneTrattino = nome.indexOf(" - ");
            return nome.substring(0, posizioneTrattino); // Prende tutto quello che c'è prima del trattino
        }

        // 3. Se non c'è alcun trattino, restituisco il nome così com'è
        return nome;
    }

    /**
     * Recupera l'utente attualmente autenticato dalla sessione HTTP, se esistente.
     * 
     * @param request La richiesta HTTP corrente
     * @return L'oggetto {@link UtenteBean} dell'utente loggato, oppure {@code null} se non autenticato
     */
    private UtenteBean getLoggedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (UtenteBean) session.getAttribute("utente") : null;
    }

    /**
     * Estrae un parametro dalla richiesta HTTP eliminando gli spazi bianchi iniziali e finali.
     * 
     * @param request La richiesta HTTP
     * @param name    Il nome del parametro da recuperare
     * @return Il valore del parametro ripulito, oppure {@code null} se il parametro non esiste o è vuoto
     */
    private String getTrimmedParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }
}