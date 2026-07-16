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
	
	// Aliquota IVA fissa da applicare al momento dell'acquisto per l'integrità storica
	private static final double IVA_STANDARD = 22.0;

	// QUERY SQL: Gestite tramite PreparedStatement per prevenire SQL Injection
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
		// Inizializzazione dei DAO per interagire con le tabelle carrello e contenuto
		this.carrelloDAO = new CarrelloDAOImpl();
		this.contenutoDAO = new ContenutoDAOImpl();
	}

	/**
	 * doGet: Gestisce l'accesso alla pagina di checkout, mostrando il riepilogo dell'ordine.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 1. Verifichiamo se l'utente è loggato controllando la sessione
		HttpSession session = request.getSession(false);
		UtenteBean utente = session != null ? (UtenteBean) session.getAttribute("utente") : null;
		
		if (utente == null) {
			// Se non è loggato, reindirizziamo alla pagina di login
			response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
			return;
		}

		try {
			// 2. Recuperiamo o creiamo il carrello associato all'utente registrato
			CarrelloBean carrello = ottieniOCreaCarrello(utente.getIdUtente());
			// Recuperiamo la mappa dei prodotti presenti nel carrello con le relative quantità
			Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());

			// Se il carrello è vuoto, non ha senso fare il checkout: rimandiamo alla pagina del carrello
			if (prodottiInCarrello.isEmpty()) {
				response.sendRedirect(request.getContextPath() + "/CarrelloServlet");
				return;
			}

			// 3. Prepariamo i dati per la JSP di checkout
			request.setAttribute("prodottiCarrello", prodottiInCarrello);
			request.setAttribute("totaleCarrello", calcolaTotale(prodottiInCarrello));
			
			// Inoltriamo la richiesta alla pagina di inserimento dati spedizione/pagamento
			request.getRequestDispatcher("/jsp/checkout.jsp").forward(request, response);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * doPost: Elabora la conferma dell'ordine salvando i dati sul database ed effettuando la transazione.
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 1. Controllo di sicurezza sull'utente in sessione
		HttpSession session = request.getSession(false);
		UtenteBean utente = session != null ? (UtenteBean) session.getAttribute("utente") : null;
		if (utente == null) {
			response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
			return;
		}

		// 2. Recupero dei dati inseriti nel form di checkout dall'utente
		String metodoPagamento = safeTrim(request.getParameter("metodoPagamento"));
		String indirizzoConsegna = safeTrim(request.getParameter("indirizzoConsegna"));

		// Validazione minima dei campi lato server
		if (metodoPagamento == null || metodoPagamento.isEmpty() || indirizzoConsegna == null || indirizzoConsegna.isEmpty()) {
			request.setAttribute("errorMessage", "Compila metodo di pagamento e indirizzo di consegna.");
			doGet(request, response); // Ricarica la pagina mostrando l'errore
			return;
		}

		try {
			// 3. Recuperiamo il carrello dell'utente e i relativi prodotti
			CarrelloBean carrello = ottieniOCreaCarrello(utente.getIdUtente());
			Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());

			if (prodottiInCarrello.isEmpty()) {
				response.sendRedirect(request.getContextPath() + "/CarrelloServlet");
				return;
			}

			// 4. Calcoliamo il costo totale complessivo dell'ordine
			double totale = calcolaTotale(prodottiInCarrello);
			
			// 5. Avviamo la procedura di salvataggio dell'ordine
			int idAcquisto = confermaOrdine(carrello.getIdCarrello(), utente.getIdUtente(), metodoPagamento, indirizzoConsegna, prodottiInCarrello, totale);

			// 6. Salviamo i dati dell'ordine appena creato come attributi per mostrarli nella ricevuta
			request.setAttribute("ordineId", idAcquisto);
			request.setAttribute("totaleOrdine", totale);
			request.setAttribute("metodoPagamento", metodoPagamento);
			request.setAttribute("indirizzoConsegna", indirizzoConsegna);
			
			// Reindirizziamo l'utente alla schermata di successo
			request.getRequestDispatcher("/jsp/ordine-confermato.jsp").forward(request, response);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * METODO HELPER: Recupera il carrello associato all'utente; se non esiste sul DB, lo crea.
	 */
	private CarrelloBean ottieniOCreaCarrello(int idUtente) throws SQLException {
		CarrelloBean carrello = carrelloDAO.doRetrieveByUtente(idUtente);
		if (carrello == null) {
			carrello = new CarrelloBean();
			carrello.setIdUtente(idUtente);
			carrelloDAO.doSave(carrello);
			carrello = carrelloDAO.doRetrieveByUtente(idUtente); // Ricarica per ottenere l'ID generato dal DB
		}
		return carrello;
	}

	/**
	 * METODO HELPER: Calcola la somma totale dei prodotti moltiplicati per le rispettive quantità.
	 */
	private double calcolaTotale(Map<ProdottoBean, Integer> prodottiInCarrello) {
		double totale = 0.0;
		for (Map.Entry<ProdottoBean, Integer> entry : prodottiInCarrello.entrySet()) {
			totale += entry.getKey().getCosto() * entry.getValue();
		}
		return totale;
	}

	/**
	 * METODO TRANSAZIONALE: Salva l'acquisto, le righe d'ordine associate e svuota il carrello.
	 * Utilizza il meccanismo di Autocommit disattivato per garantire la consistenza dei dati.
	 */
	private int confermaOrdine(int idCarrello, int idUtente, String metodoPagamento, String indirizzoConsegna,
							   Map<ProdottoBean, Integer> prodottiInCarrello, double totale) throws SQLException {

		try (Connection con = ConnessioneDB.getConnection()) {
			// DISATTIVIAMO L'AUTOCOMMIT: Inizia la transazione. 
			// Qualsiasi query eseguita da adesso non sarà salvata finché non chiameremo con.commit().
			con.setAutoCommit(false);

			try {
				int idAcquisto;
				
				// 1. Inserimento della testata dell'acquisto (Tabella 'acquisto')
				// Specifichiamo 'PreparedStatement.RETURN_GENERATED_KEYS' per ottenere l'ID autoincrementale generato dal DB
				try (PreparedStatement ps = con.prepareStatement(INSERT_ACQUISTO, PreparedStatement.RETURN_GENERATED_KEYS)) {
					ps.setDouble(1, totale);
					ps.setTimestamp(2, new Timestamp(System.currentTimeMillis())); // Timestamp con data e ora corrente
					ps.setString(3, metodoPagamento);
					ps.setString(4, indirizzoConsegna);
					ps.setInt(5, idUtente);
					ps.executeUpdate();

					// Recuperiamo l'ID generato dal database per l'acquisto corrente
					try (ResultSet rs = ps.getGeneratedKeys()) {
						if (!rs.next()) {
							throw new SQLException("Impossibile recuperare l'id dell'acquisto.");
						}
						idAcquisto = rs.getInt(1); // Questo ID servirà come chiave esterna per le righe d'ordine
					}
				}

				// 2. Inserimento delle singole righe d'ordine (Tabella 'ordine')
				// Utilizziamo un sistema BATCH (esecuzione in blocco) per massimizzare le performance ed evitare troppi viaggi sul DB
				try (PreparedStatement psOrdine = con.prepareStatement(INSERT_ORDINE)) {
					for (Map.Entry<ProdottoBean, Integer> entry : prodottiInCarrello.entrySet()) {
						ProdottoBean prodotto = entry.getKey();
						int quantita = entry.getValue();

						psOrdine.setInt(1, idAcquisto);
						psOrdine.setInt(2, prodotto.getIdProdotto());
						psOrdine.setDouble(3, prodotto.getCosto()); // Congeliamo il prezzo corrente per l'integrità storica
						psOrdine.setDouble(4, IVA_STANDARD);       // Congeliamo l'IVA per l'integrità storica
						psOrdine.setInt(5, quantita);
						psOrdine.setString(6, "In elaborazione"); // Stato iniziale della spedizione
						
						psOrdine.addBatch(); // Aggiungiamo la singola query al blocco di esecuzione
					}
					psOrdine.executeBatch(); // Eseguiamo tutte le inserzioni delle righe in un solo colpo
				}

				// 3. Svuotamento del carrello dell'utente (Tabella 'contenuto')
				try (PreparedStatement psEmpty = con.prepareStatement(EMPTY_CARRELLO)) {
					psEmpty.setInt(1, idCarrello);
					psEmpty.executeUpdate();
				}

				// SE TUTTO È ANDATO A BUON FINE: Confermiamo definitivamente la scrittura dei dati sul DB
				con.commit();
				return idAcquisto;
				
			} catch (SQLException e) {
				// IN CASO DI ERRORE: Annulliamo qualsiasi operazione eseguita all'interno di questo try.
				// Il DB tornerà esattamente allo stato precedente all'avvio della servlet
				con.rollback();
				throw e; // Rilanciamo l'eccezione per farla gestire al chiamante (la servlet)
			} finally {
				// Ripristiniamo lo stato di Autocommit predefinito della connessione prima di rilasciarla nel pool
				con.setAutoCommit(true);
			}
		}
	}

	/**
	 * METODO HELPER: Evita errori di NullPointerException ed elimina gli spazi vuoti superflui.
	 */
	private String safeTrim(String value) {
		return value != null ? value.trim() : null;
	}
}