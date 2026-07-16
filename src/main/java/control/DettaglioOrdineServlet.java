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

import model.bean.AcquistoBean;
import model.bean.OrdineBean;
import model.bean.UtenteBean;
import model.dao.AcquistoDAO;
import model.dao.OrdineDAO;
import model.dao.impl.AcquistoDAOImpl;
import model.dao.impl.OrdineDAOImpl;

/**
 * Servlet che si occupa di mostrare i dettagli specifici di un singolo acquisto.
 * Viene utilizzata sia dall'utente nel suo storico ordini, sia dall'admin nella gestione complessiva.
 */
@WebServlet("/DettaglioOrdineServlet")
public class DettaglioOrdineServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private AcquistoDAO acquistoDAO;
	private OrdineDAO ordineDAO;

	@Override
	public void init() throws ServletException {
		// Inizializziamo i DAO per recuperare la testata dell'acquisto e le singole righe d'ordine (i dettagli)
		this.acquistoDAO = new AcquistoDAOImpl();
		this.ordineDAO = new OrdineDAOImpl();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 1. Verifica dell'autenticazione: solo gli utenti registrati possono vedere i dettagli di un ordine
		HttpSession session = request.getSession(false);
		UtenteBean utente = session != null ? (UtenteBean) session.getAttribute("utente") : null;
		if (utente == null) {
			// Se non è loggato, reindirizziamo alla pagina di login
			response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
			return;
		}

		// 2. Recupero del parametro ID dell'acquisto dall'URL
		String idParam = request.getParameter("id");
		if (idParam == null || idParam.trim().isEmpty()) {
			// Se manca l'ID, lo riportiamo alla pagina del profilo/storico principale
			response.sendRedirect(request.getContextPath() + "/ProfiloServlet");
			return;
		}

		try {
			int idAcquisto = Integer.parseInt(idParam);
			
			// 3. Recupero della testata dell'acquisto dal database
			AcquistoBean acquisto = acquistoDAO.doRetrieveById(idAcquisto);

			// Se l'acquisto cercato non esiste, restituiamo un errore 404 (Resource Not Found)
			if (acquisto == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND); // Gestito nel web.xml
				return;
			}

			// 4. CONTROLLO DI SICUREZZA DI ACCESSO:
			// Un utente può visualizzare il dettaglio SOLO se:
			// - È un amministratore (deve poter controllare tutti gli ordini)
			// - È il proprietario effettivo dell'acquisto (l'ID utente registrato sull'acquisto coincide con il suo)
			if (!utente.isAdmin() && acquisto.getIdUtente() != utente.getIdUtente()) {
				// Se un utente normale prova a inserire a mano l'ID dell'ordine di un altro utente, blocchiamo con un 403 (Forbidden)
				response.sendError(HttpServletResponse.SC_FORBIDDEN); // Gestito nel web.xml
				return;
			}

			// 5. Recupero delle singole righe d'ordine associate a quell'acquisto (prodotti, prezzi dell'epoca, IVA)
			List<OrdineBean> dettagli = ordineDAO.doRetrieveByAcquisto(idAcquisto);
			
			// 6. Prepariamo i dati da passare alla JSP
			request.setAttribute("acquisto", acquisto);
			request.setAttribute("dettagliOrdine", dettagli); // Lista delle righe d'ordine contenenti l'integrità storica
			
			// Inoltriamo il flusso di esecuzione alla pagina di visualizzazione
			request.getRequestDispatcher("/jsp/dettaglio-ordine.jsp").forward(request, response);
			
		} catch (NumberFormatException e) {
			// Se l'ID passato nell'URL non è un numero valido, restituiamo un errore 400 (Bad Request)
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} catch (SQLException e) {
			// Gestione anomalie del database: logghiamo l'errore ed emettiamo un errore 500
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Le richieste POST vengono redirette sul metodo doGet per una gestione unificata
		doGet(request, response);
	}
}