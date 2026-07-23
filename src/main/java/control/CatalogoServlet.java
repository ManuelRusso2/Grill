package control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.bean.ProdottoBean;
import model.bean.UtenteBean;
import model.bean.CarrelloBean;
import model.dao.ProdottoDAO;
import model.dao.CarrelloDAO;
import model.dao.ContenutoDAO;
import model.dao.impl.ProdottoDAOImpl;
import model.dao.impl.CarrelloDAOImpl;
import model.dao.impl.ContenutoDAOImpl;


@WebServlet("/CatalogoServlet")
public class CatalogoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private ProdottoDAO prodottoDAO;

    
    @Override
    public void init() throws ServletException {
        this.prodottoDAO = new ProdottoDAOImpl();
    }

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 1. Recuperiamo tutti i prodotti attivi dal database
            List<ProdottoBean> prodotti = prodottoDAO.doRetrieveAllClienti(); // Usa il metodo corretto del tuo ProdottoDAO

            // 2. Salviamo la lista come attributo della richiesta HTTP
            request.setAttribute("prodotti", prodotti);

            // 2b. Se l'utente è autenticato, calcoliamo il numero di articoli nel carrello per mostrare il badge
            HttpSession session = request.getSession(false);
            if (session != null) {
                UtenteBean utente = (UtenteBean) session.getAttribute("utente");
                if (utente != null) {
                    CarrelloDAO carrelloDAO = new CarrelloDAOImpl();
                    ContenutoDAO contenutoDAO = new ContenutoDAOImpl();
                    CarrelloBean carrello = carrelloDAO.doRetrieveByUtente(utente.getIdUtente());
                    if (carrello != null) {
                        java.util.Map<ProdottoBean, Integer> prodottiInCarrello = contenutoDAO.doRetrieveProdottiInCarrello(carrello.getIdCarrello());
                        int totalItems = 0;
                        if (prodottiInCarrello != null) {
                            for (Integer q : prodottiInCarrello.values()) {
                                if (q != null) totalItems += q;
                            }
                        }
                        request.setAttribute("cartCount", totalItems);
                    }
                }
            }

            // 3. Inoltriamo la richiesta alla JSP del catalogo (che visualizzerà i dati)
            request.getRequestDispatcher("/jsp/common/catalogo.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            // Invia l'errore 500 che mostrerà la pagina di errore personalizzata configurata in web.xml
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Solitamente la visualizzazione del catalogo risponde solo a richieste GET, rimandiamo lì
        doGet(request, response);
    }
}