package control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.bean.UtenteBean;
import model.bean.CarrelloBean;
import model.bean.ProdottoBean;
import model.dao.CarrelloDAO;
import model.dao.ContenutoDAO;
import model.dao.ProdottoDAO;
import model.dao.impl.CarrelloDAOImpl;
import model.dao.impl.ContenutoDAOImpl;
import model.dao.impl.ProdottoDAOImpl;

@WebServlet("/CarrelloServlet")
public class CarrelloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private ProdottoDAO prodottoDAO;
    private CarrelloDAO carrelloDAO;
    private ContenutoDAO contenutoDAO;

    @Override
    public void init() throws ServletException {
        this.prodottoDAO = new ProdottoDAOImpl();
        this.carrelloDAO = new CarrelloDAOImpl();
        this.contenutoDAO = new ContenutoDAOImpl();
    }

    /**
     * GET: Gestisce ESCLUSIVAMENTE la visualizzazione del carrello (Read-Only)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UtenteBean utente = (session != null) ? (UtenteBean) session.getAttribute("utente") : null;

        // Controllo autenticazione utente
        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return;
        }

        try {
            CarrelloBean carrello = ottieniOCreaCarrello(utente.getIdUtente());

            // Consumo dei "Flash Messages" dalla sessione per la request della JSP
            if (session.getAttribute("successMessage") != null) {
                request.setAttribute("successMessage", session.getAttribute("successMessage"));
                session.removeAttribute("successMessage");
            }
            if (session.getAttribute("errorMessage") != null) {
                request.setAttribute("errorMessage", session.getAttribute("errorMessage"));
                session.removeAttribute("errorMessage");
            }

            // Recupera il contenuto aggiornato del carrello
            Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());
            request.setAttribute("prodottiCarrello", prodottiInCarrello);
            
            request.getRequestDispatcher("/jsp/user/carrello.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * POST: Gestisce ESCLUSIVAMENTE le modifiche al carrello (Add, Remove, Update)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UtenteBean utente = (session != null) ? (UtenteBean) session.getAttribute("utente") : null;

        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        // Se l'utente non è autenticato
        if (utente == null) {
            if (isAjax) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\": \"login_required\", \"redirect\": \"" + request.getContextPath() + "/jsp/common/login.jsp\"}");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return;
        }

        String action = request.getParameter("action");

        try {
            CarrelloBean carrello = ottieniOCreaCarrello(utente.getIdUtente());

            if (action != null) {
                if (action.equalsIgnoreCase("add")) {
                    aggiungiProdotto(request, session, carrello);
                } else if (action.equalsIgnoreCase("remove")) {
                    rimuoviProdotto(request, session, carrello);
                } else if (action.equalsIgnoreCase("update")) {
                    aggiornaQuantita(request, session, carrello);
                }
            }

            // Calcolo totale pezzi nel carrello con controllo null-safe
            Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());
            int totalItems = 0;
            if (prodottiInCarrello != null) {
                for (Map.Entry<ProdottoBean, Integer> entry : prodottiInCarrello.entrySet()) {
                    if (entry.getValue() != null) {
                        totalItems += entry.getValue();
                    }
                }
            }

            // Risposta per chiamate AJAX
            if (isAjax) {
                String errorMessage = (String) session.getAttribute("errorMessage");
                String successMessage = (String) session.getAttribute("successMessage");
                
                // Consumo messaggi di sessione
                session.removeAttribute("errorMessage");
                session.removeAttribute("successMessage");

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                if (errorMessage != null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    String cleanErr = errorMessage.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
                    response.getWriter().write("{\"success\": false, \"message\": \"" + cleanErr + "\"}");
                } else {
                    String cleanSucc = (successMessage != null) 
                            ? successMessage.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ") 
                            : "";
                    response.getWriter().write("{\"success\": true, \"cartCount\": " + totalItems + ", \"message\": \"" + cleanSucc + "\"}");
                }
                return;
            }

            // Fallback sottomissione standard senza AJAX (PRG Pattern)
            response.sendRedirect(request.getContextPath() + "/CarrelloServlet");

        } catch (SQLException e) {
            e.printStackTrace();
            if (isAjax) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Errore interno del server.\"}");
                return;
            }
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

    private void aggiungiProdotto(HttpServletRequest request, HttpSession session, CarrelloBean carrello) throws SQLException {
        int idProdotto = estraiIdProdotto(request);
        
        int quantitaDaAggiungere = 1;
        try {
            String qtyParam = request.getParameter("quantita");
            if (qtyParam != null && !qtyParam.trim().isEmpty()) {
                quantitaDaAggiungere = Integer.parseInt(qtyParam.trim());
            }
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Formato quantità non valido.");
            return;
        }

        if (idProdotto <= 0 || quantitaDaAggiungere <= 0) {
            session.setAttribute("errorMessage", "Dati prodotto o quantità non validi.");
            return;
        }

        ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);
        
        if (prodotto != null && prodotto.isAttivo() && prodotto.getQuantita() > 0) {
            Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());
            
            int quantitaAttuale = 0;
            if (prodottiInCarrello != null) {
                for (ProdottoBean p : prodottiInCarrello.keySet()) {
                    if (p.getIdProdotto() == idProdotto) {
                        quantitaAttuale = prodottiInCarrello.get(p);
                        break;
                    }
                }
            }
            
            if (quantitaAttuale + quantitaDaAggiungere <= prodotto.getQuantita()) {
                contenutoDAO.doAddProduct(carrello.getIdCarrello(), prodotto.getIdProdotto(), quantitaDaAggiungere);
                session.setAttribute("successMessage", "Prodotto \"" + prodotto.getNome() + "\" aggiunto al carrello!");
            } else {
                session.setAttribute("errorMessage", "Impossibile aggiungere: quantità richiesta superiore alla disponibilità in magazzino.");
            }
        } else {
            session.setAttribute("errorMessage", "Il prodotto selezionato non è al momento disponibile.");
        }
    }

    private void rimuoviProdotto(HttpServletRequest request, HttpSession session, CarrelloBean carrello) throws SQLException {
        int idProdotto = estraiIdProdotto(request);
        if (idProdotto > 0) {
            contenutoDAO.doRemoveProduct(carrello.getIdCarrello(), idProdotto);
            session.setAttribute("successMessage", "Prodotto rimosso dal carrello.");
        }
    }

    private void aggiornaQuantita(HttpServletRequest request, HttpSession session, CarrelloBean carrello) throws SQLException {
        int idProdotto = estraiIdProdotto(request);
        int nuovaQuantita = -1;

        try {
            nuovaQuantita = Integer.parseInt(request.getParameter("quantita"));
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Formato quantità non valido.");
            return;
        }

        if (idProdotto <= 0) {
            session.setAttribute("errorMessage", "Prodotto non valido.");
            return;
        }
        
        ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);
        
        if (prodotto != null && nuovaQuantita > 0 && nuovaQuantita <= prodotto.getQuantita()) {
            contenutoDAO.doUpdateQuantity(carrello.getIdCarrello(), idProdotto, nuovaQuantita);
            session.setAttribute("successMessage", "Quantità aggiornata con successo.");
        } else if (nuovaQuantita <= 0) {
            contenutoDAO.doRemoveProduct(carrello.getIdCarrello(), idProdotto);
            session.setAttribute("successMessage", "Prodotto rimosso dal carrello.");
        } else {
            session.setAttribute("errorMessage", "Quantità non disponibile a magazzino.");
        }
    }

    private int estraiIdProdotto(HttpServletRequest request) {
        try {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                idStr = request.getParameter("idProdotto");
            }
            return (idStr != null && !idStr.isEmpty()) ? Integer.parseInt(idStr) : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}