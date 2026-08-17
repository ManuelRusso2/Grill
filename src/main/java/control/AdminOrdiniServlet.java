package control;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.bean.AcquistoBean;
import model.dao.AcquistoDAO;
import model.dao.CategoriaDAO;
import model.dao.UtenteDAO;
import model.dao.impl.AcquistoDAOImpl;
import model.dao.impl.CategoriaDAOImpl;
import model.dao.impl.UtenteDAOImpl;

/**
 * Servlet per la gestione degli ordini d'acquisto da parte dell'amministratore.
 * Consente di visualizzare la lista globale degli ordini oppure di filtrarla 
 * per specifico cliente, per intervallo di date o per entrambi.
 */
@WebServlet("/AdminOrdiniServlet")
public class AdminOrdiniServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Riferimenti ai DAO necessari per accedere ai dati di ordini, utenti e categorie
    private AcquistoDAO acquistoDAO;
    private UtenteDAO utenteDAO;
    private CategoriaDAO categoriaDAO;

    /**
     * Inizializzazione della Servlet: istanzia le implementazioni concrete dei DAO.
     */
    @Override
    public void init() throws ServletException {
        this.acquistoDAO = new AcquistoDAOImpl();
        this.utenteDAO = new UtenteDAOImpl();
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    /**
     * Gestisce le richieste HTTP GET per la consultazione e il filtraggio degli ordini.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // 1. Carica i dati di supporto necessari per popolare i menu a tendina e le viste (Categorie e Clienti)
            request.setAttribute("categorie", categoriaDAO.doRetrieveAll());
            request.setAttribute("clienti", utenteDAO.doRetrieveAllClienti());

            // 2. Recupera e sanifica i parametri inviati nella Query String per il filtraggio
            String clienteParam = getTrimmedParam(request, "clienteId");
            String dataDaParam = getTrimmedParam(request, "dataDa");
            String dataAParam = getTrimmedParam(request, "dataA");

            // 3. Esegue la logica di filtraggio delegando al metodo helper dedicato
            List<AcquistoBean> ordini = fetchFilteredOrdini(request, clienteParam, dataDaParam, dataAParam);

            // 4. Salva la lista risultante negli attributi della request e inoltra alla pagina JSP di gestione
            request.setAttribute("ordiniAdmin", ordini);
            request.getRequestDispatcher("/jsp/admin/gestione-ordini.jsp").forward(request, response);

        } catch (IllegalArgumentException e) {
            // Cattura eventuali errori di formattazione nei parametri (es. ID non numerico o data invalida) e risponde con HTTP 400 Bad Request
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            // Gestisce gli errori di comunicazione o query verso il Database stampando la traccia e rispondendo con HTTP 500 Internal Server Error
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Reindirizza le richieste HTTP POST alla gestione del doGet per supportare eventuali invii da form di ricerca.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    // =========================================================================
    // HELPER METHODS (METODI DI SUPPORTO)
    // =========================================================================

    /**
     * Estrae un parametro dalla richiesta HTTP eliminando gli spazi bianchi iniziali e finali (trim).
     * 
     * @param request la richiesta HTTP da cui estrarre il parametro
     * @param name    il nome del parametro da cercare
     * @return la stringa sanificata, oppure null se il parametro è assente o vuoto
     */
    private String getTrimmedParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }

    /**
     * Applica la logica condizionale di filtraggio (per Cliente, per Intervallo di Date, o Entrambi)
     * e imposta gli attributi nella Request per mantenere lo stato dei filtri nella vista JSP.
     * 
     * @param request      la richiesta HTTP attiva
     * @param clienteParam l'ID del cliente sotto forma di stringa (opzionale)
     * @param dataDaParam  la data di inizio ricerca AAAA-MM-GG (opzionale)
     * @param dataAParam   la data di fine ricerca AAAA-MM-GG (opzionale)
     * @return la lista di ordini filtrati
     * @throws SQLException in caso di errore nell'esecuzione delle query DAO
     * @throws IllegalArgumentException in caso di parsing errato di ID o Date
     */
    private List<AcquistoBean> fetchFilteredOrdini(HttpServletRequest request, String clienteParam, String dataDaParam, String dataAParam) 
            throws SQLException {
        
        List<AcquistoBean> ordini;
        // Verifica se è stato specificato un intervallo completo di date (entrambi i parametri presenti)
        boolean hasDateRange = (dataDaParam != null && dataAParam != null);

        if (clienteParam != null) {
            // CASO A: È presente il filtro per Cliente
            int clienteId = Integer.parseInt(clienteParam);
            
            // Recupera dal DAO tutti gli ordini effettuati da quel cliente specifico
            ordini = new ArrayList<>(acquistoDAO.doRetrieveByUtente(clienteId));
            
            // Mantiene selezionato l'ID cliente nel form di filtraggio della JSP
            request.setAttribute("clienteSelezionato", clienteId);

            // Applica un ulteriore filtro in memoria per l'intervallo di date se presente
            if (hasDateRange) {
                Date dataDa = Date.valueOf(dataDaParam);
                Date dataA = Date.valueOf(dataAParam);

                // Rimuove dalla lista gli ordini con data precedente a "dataDa" o successiva a "dataA"
                ordini.removeIf(acquisto -> {
                    Date dataAcquisto = new Date(acquisto.getDataAcquisto().getTime());
                    return dataAcquisto.before(dataDa) || dataAcquisto.after(dataA);
                });

                // Mantiene le date inserite nel form della JSP
                request.setAttribute("dataDa", dataDaParam);
                request.setAttribute("dataA", dataAParam);
            }

        } else if (hasDateRange) {
            // CASO B: Nessun cliente specificato, ma è impostato un intervallo di date globale
            Date dataDa = Date.valueOf(dataDaParam);
            Date dataA = Date.valueOf(dataAParam);

            // Recupera direttamente dal DB gli ordini compresi nell'intervallo tramite query DAO
            ordini = new ArrayList<>(acquistoDAO.doRetrieveByDateInterval(dataDa, dataA));
            
            // Mantiene le date inserite nel form della JSP
            request.setAttribute("dataDa", dataDaParam);
            request.setAttribute("dataA", dataAParam);

        } else {
            // CASO C: Nessun filtro applicato -> recupera la totalità degli ordini presenti nel sistema
            ordini = new ArrayList<>(acquistoDAO.doRetrieveAll());
        }

        return ordini;
    }
}