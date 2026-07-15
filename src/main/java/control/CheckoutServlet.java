package control;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.bean.CarrelloBean;
import model.bean.OrdineBean;
import model.bean.ProdottoBean;
import model.bean.UtenteBean;
import model.dao.CarrelloDAO;
import model.dao.ContenutoDAO;
import model.dao.impl.CarrelloDAOImpl;
import model.dao.impl.ContenutoDAOImpl;
import utility.ConnessioneDB;

@WebServlet("/CheckoutServlet")
public class CheckoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final double IVA_STANDARD = 22.0;

	private static final String INSERT_ACQUISTO =
		"INSERT INTO acquisto (prezzo_totale, data_acquisto, metodo_pagamento, indirizzo_consegna, id_utente) VALUES (?, ?, ?, ?, ?)";
	private static final String INSERT_ORDINE =
		"INSERT INTO ordine (id_acquisto, id_prodotto, prezzo_unitario, iva, quantita_acquistata, stato_spedizione) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String EMPTY_CARRELLO =
		"DELETE FROM contenuto WHERE id_carrello = ?";

	private CarrelloDAO carrelloDAO;
	private ContenutoDAO contenutoDAO;

	@Override
	public void init() throws ServletException {
		this.carrelloDAO = new CarrelloDAOImpl();
		this.contenutoDAO = new ContenutoDAOImpl();
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
			CarrelloBean carrello = ottieniOCreaCarrello(utente.getIdUtente());
			Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());

			if (prodottiInCarrello.isEmpty()) {
				response.sendRedirect(request.getContextPath() + "/CarrelloServlet");
				return;
			}

			request.setAttribute("prodottiCarrello", prodottiInCarrello);
			request.setAttribute("totaleCarrello", calcolaTotale(prodottiInCarrello));
			request.getRequestDispatcher("/jsp/checkout.jsp").forward(request, response);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);
		UtenteBean utente = session != null ? (UtenteBean) session.getAttribute("utente") : null;
		if (utente == null) {
			response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
			return;
		}

		String metodoPagamento = safeTrim(request.getParameter("metodoPagamento"));
		String indirizzoConsegna = safeTrim(request.getParameter("indirizzoConsegna"));

		if (metodoPagamento == null || metodoPagamento.isEmpty() || indirizzoConsegna == null || indirizzoConsegna.isEmpty()) {
			request.setAttribute("errorMessage", "Compila metodo di pagamento e indirizzo di consegna.");
			doGet(request, response);
			return;
		}

		try {
			CarrelloBean carrello = ottieniOCreaCarrello(utente.getIdUtente());
			Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());

			if (prodottiInCarrello.isEmpty()) {
				response.sendRedirect(request.getContextPath() + "/CarrelloServlet");
				return;
			}

			double totale = calcolaTotale(prodottiInCarrello);
			int idAcquisto = confermaOrdine(carrello.getIdCarrello(), utente.getIdUtente(), metodoPagamento, indirizzoConsegna, prodottiInCarrello, totale);

			request.setAttribute("ordineId", idAcquisto);
			request.setAttribute("totaleOrdine", totale);
			request.setAttribute("metodoPagamento", metodoPagamento);
			request.setAttribute("indirizzoConsegna", indirizzoConsegna);
			request.getRequestDispatcher("/jsp/ordine-confermato.jsp").forward(request, response);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private CarrelloBean ottieniOCreaCarrello(int idUtente) throws SQLException {
		CarrelloBean carrello = carrelloDAO.doRetrieveByUtente(idUtente);
		if (carrello == null) {
			carrello = new CarrelloBean();
			carrello.setIdUtente(idUtente);
			carrelloDAO.doSave(carrello);
			carrello = carrelloDAO.doRetrieveByUtente(idUtente);
		}
		return carrello;
	}

	private double calcolaTotale(Map<ProdottoBean, Integer> prodottiInCarrello) {
		double totale = 0.0;
		for (Map.Entry<ProdottoBean, Integer> entry : prodottiInCarrello.entrySet()) {
			totale += entry.getKey().getCosto() * entry.getValue();
		}
		return totale;
	}

	private int confermaOrdine(int idCarrello, int idUtente, String metodoPagamento, String indirizzoConsegna,
							   Map<ProdottoBean, Integer> prodottiInCarrello, double totale) throws SQLException {

		try (Connection con = ConnessioneDB.getConnection()) {
			con.setAutoCommit(false);

			try {
				int idAcquisto;
				try (PreparedStatement ps = con.prepareStatement(INSERT_ACQUISTO, PreparedStatement.RETURN_GENERATED_KEYS)) {
					ps.setDouble(1, totale);
					ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
					ps.setString(3, metodoPagamento);
					ps.setString(4, indirizzoConsegna);
					ps.setInt(5, idUtente);
					ps.executeUpdate();

					try (ResultSet rs = ps.getGeneratedKeys()) {
						if (!rs.next()) {
							throw new SQLException("Impossibile recuperare l'id dell'acquisto.");
						}
						idAcquisto = rs.getInt(1);
					}
				}

				try (PreparedStatement psOrdine = con.prepareStatement(INSERT_ORDINE)) {
					for (Map.Entry<ProdottoBean, Integer> entry : prodottiInCarrello.entrySet()) {
						ProdottoBean prodotto = entry.getKey();
						int quantita = entry.getValue();

						psOrdine.setInt(1, idAcquisto);
						psOrdine.setInt(2, prodotto.getIdProdotto());
						psOrdine.setDouble(3, prodotto.getCosto());
						psOrdine.setDouble(4, IVA_STANDARD);
						psOrdine.setInt(5, quantita);
						psOrdine.setString(6, "In elaborazione");
						psOrdine.addBatch();
					}
					psOrdine.executeBatch();
				}

				try (PreparedStatement psEmpty = con.prepareStatement(EMPTY_CARRELLO)) {
					psEmpty.setInt(1, idCarrello);
					psEmpty.executeUpdate();
				}

				con.commit();
				return idAcquisto;
			} catch (SQLException e) {
				con.rollback();
				throw e;
			} finally {
				con.setAutoCommit(true);
			}
		}
	}

	private String safeTrim(String value) {
		return value != null ? value.trim() : null;
	}
}