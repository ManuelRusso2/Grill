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

@WebServlet("/ProfiloServlet")
public class ProfiloServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private AcquistoDAO acquistoDAO;

	@Override
	public void init() throws ServletException {
		this.acquistoDAO = new AcquistoDAOImpl();
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

		try {
			List<AcquistoBean> acquisti = acquistoDAO.doRetrieveByUtente(utente.getIdUtente());
			request.setAttribute("acquisti", acquisti);
			request.getRequestDispatcher("/jsp/user/profilo.jsp").forward(request, response);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}