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
import model.dao.UtenteDAO;
import model.dao.impl.AcquistoDAOImpl;
import model.dao.impl.UtenteDAOImpl;

@WebServlet("/AdminOrdiniServlet")
public class AdminOrdiniServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private AcquistoDAO acquistoDAO;
    private UtenteDAO utenteDAO;

    @Override
    public void init() throws ServletException {
        // Inizializziamo i DAO necessari per gestire gli ordini e recuperare i clienti
        this.acquistoDAO = new AcquistoDAOImpl();
        this.utenteDAO = new UtenteDAOImpl();
    }

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			// 1. Recupero dei parametri di filtro inviati dal form della JSP
			String clienteParam = request.getParameter("clienteId");
			String dataDaParam = request.getParameter("dataDa");
			String dataAParam = request.getParameter("dataA");

			List<AcquistoBean> ordini;

			// 2. FILTRAGGIO
			if (clienteParam != null && !clienteParam.trim().isEmpty()) {
				// CASO A: È stato selezionato un cliente specifico
				int clienteId = Integer.parseInt(clienteParam.trim());
				
				// Interroghiamo il DB estraendo SOLO gli ordini di questo utente
				ordini = new ArrayList<>(acquistoDAO.doRetrieveByUtente(clienteId));
				request.setAttribute("clienteSelezionato", clienteId);

				// SOTTO-CASO A.1: Il cliente ha inserito ANCHE un intervallo di date per questo utente
				if (dataDaParam != null && !dataDaParam.trim().isEmpty() && dataAParam != null && !dataAParam.trim().isEmpty()) {
					Date dataDa = Date.valueOf(dataDaParam.trim());
					Date dataA = Date.valueOf(dataAParam.trim());
					
					// Filtriamo in memoria, ma solo sulla lista già ridotta di quel singolo cliente
					ordini.removeIf(acquisto -> {
						Date dataAcquisto = new Date(acquisto.getDataAcquisto().getTime());
						return dataAcquisto.before(dataDa) || dataAcquisto.after(dataA);
					});
					
					request.setAttribute("dataDa", dataDaParam.trim());
					request.setAttribute("dataA", dataAParam.trim());
				}

			} else if (dataDaParam != null && !dataDaParam.trim().isEmpty() && dataAParam != null && !dataAParam.trim().isEmpty()) {
				// CASO B: Nessun utente selezionato, ma c'è un filtro date globale
				Date dataDa = Date.valueOf(dataDaParam.trim());
				Date dataA = Date.valueOf(dataAParam.trim());
				
				// Interroghiamo il DB estraendo SOLO gli ordini in quell'intervallo di tempo
				ordini = new ArrayList<>(acquistoDAO.doRetrieveByDateInterval(dataDa, dataA));
				
				request.setAttribute("dataDa", dataDaParam.trim());
				request.setAttribute("dataA", dataAParam.trim());

			} else {
				// CASO C: Nessun filtro attivo, carichiamo tutti gli ordini
				ordini = new ArrayList<>(acquistoDAO.doRetrieveAll());
			}

			// 3. Prepariamo i dati per la JSP (View)
			// Passiamo la lista degli ordini
			request.setAttribute("ordiniAdmin", ordini);
			// Passiamo la lista di tutti i clienti per popolare la Select dei filtri
			request.setAttribute("clienti", utenteDAO.doRetrieveAllClienti());

			// 4. Forward alla pagina JSP di gestione dell'admin
			request.getRequestDispatcher("/jsp/admin/gestione-ordini.jsp").forward(request, response);

		} catch (IllegalArgumentException e) {
			// Cattura ID non numerici o formati data ("yyyy-MM-dd") non validi inviando un errore 400
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} catch (SQLException e) {
			// Log dell'errore SQL sulla console del server e ritorno dell'errore 500
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirigiamo le chiamate POST al GET per gestire i filtri in un unico punto
        doGet(request, response);
    }
}