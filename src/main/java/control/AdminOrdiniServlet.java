package control;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
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
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        return null;
    }

    /**
     * Applica la logica condizionale di filtraggio flessibile sugli ordini.
     * Consente di filtrare per singolo cliente, per intervallo di date (anche parziale, 
     * compilando solo "Data Da" o solo "Data A"), o per entrambi i criteri contemporaneamente.
     * 
     * @param request      la richiesta HTTP da cui recuperare e su cui impostare gli attributi per la JSP
     * @param clienteParam l'ID del cliente da filtrare (opzionale, formato stringa)
     * @param dataDaParam  la data di inizio ricerca nel formato AAAA-MM-GG (opzionale)
     * @param dataAParam   la data di fine ricerca nel formato AAAA-MM-GG (opzionale)
     * @return la lista di ordini (AcquistoBean) opportunamente filtrata
     * @throws SQLException             in caso di errore nelle query verso il database
     * @throws IllegalArgumentException in caso di formato errato dell'ID o delle date
     */
    private List<AcquistoBean> fetchFilteredOrdini(HttpServletRequest request, String clienteParam, String dataDaParam, String dataAParam) 
            throws SQLException {
        
        List<AcquistoBean> ordini;

        // -------------------------------------------------------------------------
        // 1. RECUPERO INIZIALE DAL DATABASE
        // -------------------------------------------------------------------------
        // Se è stato selezionato un cliente specifico, recupera solo i suoi ordini;
        // altrimenti carica l'elenco completo di tutti gli ordini registrati a sistema.
        if (clienteParam != null) {
            int clienteId = Integer.parseInt(clienteParam);
            ordini = new ArrayList<>(acquistoDAO.doRetrieveByUtente(clienteId));
            
            // Mantiene selezionato l'ID del cliente nel menu a tendina della vista JSP
            request.setAttribute("clienteSelezionato", clienteId);
        } else {
            ordini = new ArrayList<>(acquistoDAO.doRetrieveAll());
        }

        // -------------------------------------------------------------------------
        // 2. PARSING E CONVERSIONE DELLE DATE
        // -------------------------------------------------------------------------
        // Conversioni da String a java.sql.Date con ripopolamento dei campi form
        Date dataDa = null;
        if (dataDaParam != null) {
            dataDa = Date.valueOf(dataDaParam);
            request.setAttribute("dataDa", dataDaParam);
        }

        Date dataA = null;
        if (dataAParam != null) {
            // Aggiunge 1 giorno a dataA per includere gli ordini effettuati fino alle 23:59:59 della giornata selezionata
            dataA = Date.valueOf(java.time.LocalDate.parse(dataAParam).plusDays(1).toString());
            request.setAttribute("dataA", dataAParam);
        }

     // -------------------------------------------------------------------------
     // 3. FILTRAGGIO IN MEMORIA PER DATE (CON CICLO CLASSICO SENZA LAMBDA)
     // -------------------------------------------------------------------------
     if (dataDa != null || dataA != null) {
         Iterator<AcquistoBean> iterator = ordini.iterator();
         
         while (iterator.hasNext()) {
             AcquistoBean acquisto = iterator.next();
             Timestamp d = acquisto.getDataAcquisto();

             // Se l'ordine è stato fatto PRIMA della "Data Da", lo rimuove
             if (dataDa != null && d.before(dataDa)) {
                 iterator.remove();
                 continue;
             }

             // Se l'ordine è stato fatto DOPO la "Data A", lo rimuove
             if (dataA != null && !d.before(dataA)) {
                 iterator.remove();
             }
         }
     }

        return ordini;
    }
}