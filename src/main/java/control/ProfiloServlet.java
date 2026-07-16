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
import model.bean.UtenteBean;
import model.dao.AcquistoDAO;
import model.dao.impl.AcquistoDAOImpl;

/**
 * Servlet che gestisce la visualizzazione del profilo utente e del suo storico ordini.
 * È accessibile solo agli utenti registrati ed autenticati.
 */
@WebServlet("/ProfiloServlet")
public class ProfiloServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private AcquistoDAO acquistoDAO;

	@Override
	public void init() throws ServletException {
		// Inizializziamo il DAO per recuperare la lista degli acquisti dell'utente
		this.acquistoDAO = new AcquistoDAOImpl();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 1. Controllo di autenticazione: recuperiamo la sessione senza crearne una nuova (false)
		HttpSession session = request.getSession(false);
		UtenteBean utente = session != null ? (UtenteBean) session.getAttribute("utente") : null;
		
		if (utente == null) {
			// Se l'utente non è autenticato, viene reindirizzato alla pagina di login
			response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
			return;
		}

		try {
			// 2. Recupero dello storico degli acquisti mirato direttamente tramite query SQL sul DB
			// Usiamo l'id dell'utente memorizzato nell'oggetto utente della sessione
			List<AcquistoBean> acquisti = acquistoDAO.doRetrieveByUtente(utente.getIdUtente());
			
			// 3. Salviamo la lista come attributo di richiesta per renderla visibile alla JSP
			request.setAttribute("acquisti", acquisti);
			
			// 4. Inoltriamo la richiesta (Forward) alla JSP del profilo utente
			request.getRequestDispatcher("/jsp/user/profilo.jsp").forward(request, response);
			
		} catch (SQLException e) {
			// Log dell'errore SQL sulla console di Tomcat ed invio dell'errore 500 gestito nel web.xml
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}