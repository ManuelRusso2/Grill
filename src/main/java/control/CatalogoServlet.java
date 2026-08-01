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
import model.bean.CategoriaBean;
import model.dao.ProdottoDAO;
import model.dao.CarrelloDAO;
import model.dao.ContenutoDAO;
import model.dao.CategoriaDAO;
import model.dao.impl.ProdottoDAOImpl;
import model.dao.impl.CarrelloDAOImpl;
import model.dao.impl.ContenutoDAOImpl;
import model.dao.impl.CategoriaDAOImpl;


@WebServlet("/CatalogoServlet")
public class CatalogoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private ProdottoDAO prodottoDAO;
    private CategoriaDAO categoriaDAO;

    
    @Override
    public void init() throws ServletException {
        this.prodottoDAO = new ProdottoDAOImpl();
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 0. Carica tutte le categorie per il menu
            List<CategoriaBean> allCategorie = categoriaDAO.doRetrieveAll();
            request.setAttribute("categorie", allCategorie);
            
            // 1. Filtro per categoria se presente il parametro
            String categoriaParam = request.getParameter("categoria");
            List<ProdottoBean> prodotti;
            if (categoriaParam != null && !categoriaParam.trim().isEmpty()) {
                int idCategoria = Integer.parseInt(categoriaParam);
                prodotti = prodottoDAO.doRetrieveByCategoria(idCategoria);
                CategoriaBean categoriaAttiva = categoriaDAO.doRetrieveById(idCategoria);
                request.setAttribute("categoriaAttiva", categoriaAttiva);
            } else {
                prodotti = prodottoDAO.doRetrieveAllClientiRaggruppati();
            }
            request.setAttribute("prodotti", prodotti);

            // 2b. Se l'utente è autenticato, calcoliamo il numero di articoli nel carrello per mostrare il badge
            HttpSession session = request.getSession(false);
            if (session != null) {
                UtenteBean utente = (UtenteBean) session.getAttribute("utente");
                if (utente != null) {
                    // Passiamo al JSP se l'utente è admin
                    request.setAttribute("isAdmin", utente.isAdmin());
                    
                    // Se l'utente è admin, non calcoliamo il carrello
                    if (!utente.isAdmin()) {
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