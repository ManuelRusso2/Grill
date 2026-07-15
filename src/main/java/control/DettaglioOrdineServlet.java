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

@WebServlet("/DettaglioOrdineServlet")
public class DettaglioOrdineServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private AcquistoDAO acquistoDAO;
	private OrdineDAO ordineDAO;

	@Override
	public void init() throws ServletException {
		this.acquistoDAO = new AcquistoDAOImpl();
		this.ordineDAO = new OrdineDAOImpl();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);
		UtenteBean utente = session != null ? (UtenteBean) session.getAttribute("utente") : null;
		if (utente == null) {
			response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
			return;
		}

		String idParam = request.getParameter("id");
		if (idParam == null || idParam.trim().isEmpty()) {
			response.sendRedirect(request.getContextPath() + "/ProfiloServlet");
			return;
		}

		try {
			int idAcquisto = Integer.parseInt(idParam);
			AcquistoBean acquisto = acquistoDAO.doRetrieveById(idAcquisto);

			if (acquisto == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			if (!utente.isAdmin() && acquisto.getIdUtente() != utente.getIdUtente()) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			List<OrdineBean> dettagli = ordineDAO.doRetrieveByAcquisto(idAcquisto);
			request.setAttribute("acquisto", acquisto);
			request.setAttribute("dettagliOrdine", dettagli);
			request.getRequestDispatcher("/jsp/dettaglio-ordine.jsp").forward(request, response);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}