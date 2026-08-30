package control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.bean.CollezioneBean;
import model.bean.ProdottoBean;
import model.dao.CollezioneDAO;
import model.dao.ProdottoDAO;
import model.dao.impl.CollezioneDAOImpl;
import model.dao.impl.ProdottoDAOImpl;

/**
 * Servlet per la gestione e la visualizzazione delle collezioni di prodotti.
 * Mappa l'URL '/CollezioniServlet' e recupera tutte le collezioni attive con 
 * i relativi prodotti associati per mostrarli nella vista dedicata.
 */
@WebServlet("/CollezioniServlet")
public class CollezioniServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Oggetti DAO per l'accesso ai dati nel database
    private CollezioneDAO collezioneDAO;
    private ProdottoDAO prodottoDAO;

    /**
     * Inizializza le istanze dei DAO necessarie per il recupero di collezioni e prodotti.
     */
    @Override
    public void init() throws ServletException {
        this.collezioneDAO = new CollezioneDAOImpl();
        this.prodottoDAO = new ProdottoDAOImpl();
    }

    /**
     * Gestisce le richieste GET per recuperare le collezioni e i prodotti associati.
     * Mappa ed elabora i dati in modo efficiente prima di inoltrarli alla JSP.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Recupera dal DB tutte le collezioni e i prodotti raggruppati per il catalogo clienti
            List<CollezioneBean> collezioni = collezioneDAO.doRetrieveAll();
            List<ProdottoBean> prodotti = prodottoDAO.doRetrieveAllProdottiRaggruppati();

            // 1. Raggruppa preventivamente i prodotti attivi per ID Collezione.
            //    Utilizza una HashMap per ridurre la complessità computazionale a O(M) [M = num prodotti],
            //    evitando query ripetute sul DB o cicli annidati non efficienti.
            Map<Integer, List<ProdottoBean>> prodottiPerCollezioneId = new HashMap<>();
            
            if (prodotti != null) {
                for (ProdottoBean p : prodotti) {
                    // Considera solo i prodotti attivi e collegati a una collezione valida
                    if (p.isAttivo() && p.getIdCollezione() != null) {
                        prodottiPerCollezioneId
                            .computeIfAbsent(p.getIdCollezione(), k -> new ArrayList<>())
                            .add(p);
                    }
                }
            }

            // 2. Associa le collezioni ai propri prodotti mantenendo l'ordine di inserimento/recupero.
            //    Si usa una LinkedHashMap per preservare l'ordine originale delle collezioni.
            Map<CollezioneBean, List<ProdottoBean>> collezioniMap = new LinkedHashMap<>();

            if (collezioni != null) {
                for (CollezioneBean col : collezioni) {
                    List<ProdottoBean> prodottiCollezione = prodottiPerCollezioneId.get(col.getIdCollezione());
                    
                    // Inserisce nella mappa solo le collezioni che contengono almeno un prodotto attivo
                    if (prodottiCollezione != null && !prodottiCollezione.isEmpty()) {
                        collezioniMap.put(col, prodottiCollezione);
                    }
                }
            }

            // Imposta la mappa strutturata come attributo della richiesta
            request.setAttribute("collezioniMap", collezioniMap);
            
            // Inoltra la richiesta alla vista JSP
            request.getRequestDispatcher("/jsp/common/collezioni.jsp").forward(request, response);

        } catch (SQLException e) {
            // Gestione errori DB con log sul server e risposta HTTP 500
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gestisce le richieste POST reindirizzandole al metodo doGet.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}