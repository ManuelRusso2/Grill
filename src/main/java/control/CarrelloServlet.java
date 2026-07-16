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

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(true);
        UtenteBean utente = (UtenteBean) session.getAttribute("utente");

        // Se l'utente non è loggato, lo rimandiamo al login (Requisito di sicurezza)
        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/jsp/common/login.jsp");
            return;
        }

        String action = request.getParameter("action");

        try {
            // Recuperiamo o creiamo il carrello associato all'utente loggato
            CarrelloBean carrello = ottieniOCreaCarrello(utente.getIdUtente());

            if (action != null) {
                if (action.equalsIgnoreCase("add")) {
                    aggiungiProdotto(request, carrello);
                } else if (action.equalsIgnoreCase("remove")) {
                    rimuoviProdotto(request, carrello);
                } else if (action.equalsIgnoreCase("update")) {
                    aggiornaQuantita(request, carrello);
                }
                // Dopo qualsiasi operazione di modifica, facciamo un redirect per evitare doppi invii in caso di refresh
                response.sendRedirect(request.getContextPath() + "/CarrelloServlet");
                return;
            }
            
            // Se l'azione è null, recuperiamo gli elementi correnti dal DB e li mostriamo nella JSP
            Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());
            request.setAttribute("prodottiCarrello", prodottiInCarrello);
            
            request.getRequestDispatcher("/jsp/user/carrello.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private CarrelloBean ottieniOCreaCarrello(int idUtente) throws SQLException {
        CarrelloBean carrello = carrelloDAO.doRetrieveByUtente(idUtente);
        if (carrello == null) {
            // Se non ha ancora un carrello sul DB, lo creiamo
            carrello = new CarrelloBean();
            carrello.setIdUtente(idUtente);
            carrelloDAO.doSave(carrello);
            // Recuperiamo l'oggetto appena creato comprensivo del suo ID generato
            carrello = carrelloDAO.doRetrieveByUtente(idUtente);
        }
        return carrello;
    }

    private void aggiungiProdotto(HttpServletRequest request, CarrelloBean carrello) throws SQLException {
        int idProdotto = Integer.parseInt(request.getParameter("id"));
        ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);
        
        if (prodotto != null && prodotto.isAttivo() && prodotto.getQuantita() > 0) {
            // Recupera la quantità attuale nel carrello per questo prodotto
            Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());
            int quantitaAttuale = 0;
            for (ProdottoBean p : prodottiInCarrello.keySet()) {
                if (p.getIdProdotto() == idProdotto) {
                    quantitaAttuale = prodottiInCarrello.get(p);
                    break;
                }
            }
            
            // Verifica che la quantità totale non superi lo stock
            if (quantitaAttuale + 1 <= prodotto.getQuantita()) {
                contenutoDAO.doAddProduct(carrello.getIdCarrello(), prodotto.getIdProdotto(), 1);
            }
        }
    }

    private void rimuoviProdotto(HttpServletRequest request, CarrelloBean carrello) throws SQLException {
        int idProdotto = Integer.parseInt(request.getParameter("id"));
        // Rimuove completamente il prodotto dal carrello
        contenutoDAO.doRemoveProduct(carrello.getIdCarrello(), idProdotto);
    }

    private void aggiornaQuantita(HttpServletRequest request, CarrelloBean carrello) throws SQLException {
        int idProdotto = Integer.parseInt(request.getParameter("id"));
        int nuovaQuantita = Integer.parseInt(request.getParameter("quantita"));
        
        ProdottoBean prodotto = prodottoDAO.doRetrieveByKey(idProdotto);
        if (prodotto != null && nuovaQuantita > 0 && nuovaQuantita <= prodotto.getQuantita()) {
            // Aggiorna la quantità alla cifra esatta passata dal form
            contenutoDAO.doUpdateQuantity(carrello.getIdCarrello(), idProdotto, nuovaQuantita);
        } else if (nuovaQuantita <= 0) {
            // Se la quantità scende a 0 o meno, lo rimuoviamo direttamente
            contenutoDAO.doRemoveProduct(carrello.getIdCarrello(), idProdotto);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}