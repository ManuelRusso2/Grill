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
		"INSERT INTO ordine (id_acquisto, id_prodotto, taglia, prezzo_unitario, iva, quantita_acquistata, stato_spedizione) VALUES (?, ?, ?, ?, ?, ?, ?)";
	
	private static final String EMPTY_CARRELLO =
		"DELETE FROM contenuto WHERE id_carrello = ?";

	private CarrelloDAO carrelloDAO;
	private ContenutoDAO contenutoDAO;

	@Override
	public void init() throws ServletException {
		this.carrelloDAO = new CarrelloDAOImpl();
		this.contenutoDAO = new ContenutoDAOImpl();
	}

	private boolean luhnCheck(String number) {
		if (number == null || !number.matches("\\d+")) return false;
		int sum = 0;
		boolean alternate = false;
		for (int i = number.length() - 1; i >= 0; i--) {
			int n = Integer.parseInt(number.substring(i, i + 1));
			if (alternate) {
				n *= 2;
				if (n > 9) n -= 9;
			}
			sum += n;
			alternate = !alternate;
		}
		return (sum % 10) == 0;
	}

	private boolean validateIBAN(String iban) {
		if (iban == null) return false;
		String value = iban.replaceAll("\\s+", "").toUpperCase();
		return value.matches("^[A-Z]{2}[0-9A-Z]{13,32}$");
	}

	private boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);
		UtenteBean utente = session != null ? (UtenteBean) session.getAttribute("utente") : null;
		
		if (utente == null) {
			response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
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
			
			request.getRequestDispatcher("/jsp/user/checkout.jsp").forward(request, response);
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
			response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
			return;
		}

		String metodoPagamento = safeTrim(request.getParameter("metodoPagamento"));
		String indirizzoConsegna = safeTrim(request.getParameter("indirizzoConsegna"));

		String cartaNumero = safeTrim(request.getParameter("cartaNumero"));
		String cartaNome = safeTrim(request.getParameter("cartaNome"));
		String cartaCognome = safeTrim(request.getParameter("cartaCognome"));
		String cartaScadenza = safeTrim(request.getParameter("cartaScadenza"));
		String cartaCVV = safeTrim(request.getParameter("cartaCVV"));

		String contoNome = safeTrim(request.getParameter("contoNome"));
		String contoCognome = safeTrim(request.getParameter("contoCognome"));
		String contoIBAN = safeTrim(request.getParameter("contoIBAN"));

		if (metodoPagamento == null || metodoPagamento.isEmpty() || indirizzoConsegna == null || indirizzoConsegna.isEmpty()) {
			request.setAttribute("errorMessage", "Compila metodo di pagamento e indirizzo di consegna.");
			doGet(request, response);
			return;
		}

		if ("Carta".equals(metodoPagamento)) {
			if (isEmpty(cartaNumero) || isEmpty(cartaNome) || isEmpty(cartaCognome) || isEmpty(cartaScadenza) || isEmpty(cartaCVV)) {
				request.setAttribute("errorMessage", "Compila tutti i dati della carta richiesti.");
				doGet(request, response);
				return;
			}

			String numOnly = cartaNumero.replaceAll("\\s+", "");
			if (!numOnly.matches("\\d{13,19}") || !luhnCheck(numOnly)) {
				request.setAttribute("errorMessage", "Numero carta non valido.");
				doGet(request, response);
				return;
			}
			if (!cartaScadenza.matches("^(0[1-9]|1[0-2])/(\\d{2})$")) {
				request.setAttribute("errorMessage", "Formato data scadenza non valido (MM/AA).");
				doGet(request, response);
				return;
			}
			if (!cartaCVV.matches("^\\d{3,4}$")) {
				request.setAttribute("errorMessage", "CVV non valido.");
				doGet(request, response);
				return;
			}
		} else if ("Conto_bancario".equals(metodoPagamento)) {
			if (isEmpty(contoIBAN) || isEmpty(contoNome) || isEmpty(contoCognome)) {
				request.setAttribute("errorMessage", "Compila tutti i dati del conto bancario richiesti.");
				doGet(request, response);
				return;
			}

			if (!validateIBAN(contoIBAN)) {
				request.setAttribute("errorMessage", "IBAN non valido.");
				doGet(request, response);
				return;
			}
		} else {
			request.setAttribute("errorMessage", "Metodo di pagamento non valido.");
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
			
			request.getRequestDispatcher("/jsp/user/ordine-confermato.jsp").forward(request, response);
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
                        // INSERIMENTO DELLA TAGLIA NEL DATABASE
						psOrdine.setString(3, prodotto.getTagliaSelezionata() != null ? prodotto.getTagliaSelezionata() : "Unica");
						psOrdine.setDouble(4, prodotto.getCosto());
						psOrdine.setDouble(5, IVA_STANDARD);
						psOrdine.setInt(6, quantita);
						psOrdine.setString(7, "In elaborazione"); 
						
						psOrdine.addBatch();
					}
					psOrdine.executeBatch(); 

					String sqlUpdateQty = "UPDATE prodotto SET quantita = quantita - ? WHERE id_prodotto = ?";
					String sqlSelectQty = "SELECT quantita, nome FROM prodotto WHERE id_prodotto = ?";

					try (PreparedStatement psUpdateQty = con.prepareStatement(sqlUpdateQty);
					     PreparedStatement psSelectQty = con.prepareStatement(sqlSelectQty)) {

					    for (Map.Entry<ProdottoBean, Integer> entry : prodottiInCarrello.entrySet()) {
					        ProdottoBean prodotto = entry.getKey();
					        int quantitaAcquistata = entry.getValue();

					        psUpdateQty.setInt(1, quantitaAcquistata);
					        psUpdateQty.setInt(2, prodotto.getIdProdotto());
					        psUpdateQty.executeUpdate();

					        psSelectQty.setInt(1, prodotto.getIdProdotto());
					        try (ResultSet rsQ = psSelectQty.executeQuery()) {
					            if (rsQ.next()) {
					                int nuovaQ = rsQ.getInt("quantita");
					                String nomeProd = rsQ.getString("nome");

					                if (nuovaQ <= 0) {
					                    String alert = "Prodotto esaurito: " + nomeProd + " (ID: " + prodotto.getIdProdotto() + ")";
					                    
					                    synchronized (getServletContext()) {
					                        @SuppressWarnings("unchecked")
					                        java.util.List<String> alerts = (java.util.List<String>) getServletContext().getAttribute("adminAlerts");
					                        
					                        if (alerts == null) {
					                            alerts = new java.util.ArrayList<>();
					                            getServletContext().setAttribute("adminAlerts", alerts);
					                        }
					                        alerts.add(alert);
					                    }
					                }
					            }
					        }
					    }
					}
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