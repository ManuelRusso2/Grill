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

import model.bean.CategoriaBean;
import model.dao.CategoriaDAO;
import model.dao.impl.CategoriaDAOImpl;

@WebServlet("/AdminCategoriaServlet")
public class AdminCategoriaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CategoriaDAO categoriaDAO;

    @Override
    public void init() throws ServletException {
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            if (session.getAttribute("successMessage") != null) {
                request.setAttribute("successMessage", session.getAttribute("successMessage"));
                session.removeAttribute("successMessage");
            }
            if (session.getAttribute("errorMessage") != null) {
                request.setAttribute("errorMessage", session.getAttribute("errorMessage"));
                session.removeAttribute("errorMessage");
            }
        }

        String action = request.getParameter("action");

        try {
            if ("new".equalsIgnoreCase(action)) {
                request.getRequestDispatcher("/jsp/admin/nuovo-categoria.jsp").forward(request, response);
                return;
            }

            if ("edit".equalsIgnoreCase(action)) {
                String idParam = request.getParameter("id");
                if (idParam != null && !idParam.isEmpty()) {
                    int id = Integer.parseInt(idParam);
                    CategoriaBean categoria = categoriaDAO.doRetrieveById(id);
                    if (categoria != null) {
                        request.setAttribute("categoria", categoria);
                        request.getRequestDispatcher("/jsp/admin/nuovo-categoria.jsp").forward(request, response);
                        return;
                    } else {
                        if (session != null) session.setAttribute("errorMessage", "Categoria non trovata.");
                    }
                }
            }

            List<CategoriaBean> categorie = categoriaDAO.doRetrieveAll();
            request.setAttribute("categorie", categorie);
            request.getRequestDispatcher("/jsp/admin/gestione-categorie.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            if (session != null) session.setAttribute("errorMessage", "ID categoria non valido.");
            response.sendRedirect(request.getContextPath() + "/AdminCategoriaServlet");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        String action = request.getParameter("action");

        try {
            if (action != null) {
                if ("newCategory".equalsIgnoreCase(action)) {
                    String nome = request.getParameter("nome");
                    String descrizione = request.getParameter("descrizione");

                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    if (nome != null && !nome.trim().isEmpty()) {
                        CategoriaBean categoria = new CategoriaBean();
                        categoria.setNome(nome.trim());
                        categoria.setDescrizione(descrizione != null ? descrizione.trim() : "");
                        categoriaDAO.doSave(categoria);
                        String json = String.format("{\"success\": true, \"message\": \"Categoria creata\", \"id\": %d, \"nome\": \"%s\"}",
                                categoria.getIdCategoria(), categoria.getNome().replaceAll("\"", "'"));
                        response.getWriter().write(json);
                    } else {
                        response.getWriter().write("{\"success\": false, \"message\": \"Nome categoria obbligatorio\"}");
                    }
                    return;
                }

                if ("deleteCategory".equalsIgnoreCase(action)) {
                    String idParam = request.getParameter("id");
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    if (idParam != null && !idParam.isEmpty()) {
                        try {
                            int id = Integer.parseInt(idParam);
                            try {
                                boolean ok = categoriaDAO.doDelete(id);
                                if (ok) {
                                    response.getWriter().write("{\"success\": true, \"message\": \"Categoria eliminata\", \"id\": " + id + "}");
                                } else {
                                    response.getWriter().write("{\"success\": false, \"message\": \"Categoria non trovata\"}");
                                }
                            } catch (SQLException sqle) {
                                response.getWriter().write("{\"success\": false, \"message\": \"Impossibile eliminare la categoria: esistono prodotti collegati\"}");
                            }
                        } catch (NumberFormatException e) {
                            response.getWriter().write("{\"success\": false, \"message\": \"ID non valido\"}");
                        }
                    } else {
                        response.getWriter().write("{\"success\": false, \"message\": \"ID mancante\"}");
                    }
                    return;
                }

                // Form submissions (non-AJAX)
                if ("save".equalsIgnoreCase(action)) {
                    CategoriaBean categoria = new CategoriaBean();
                    categoria.setNome(request.getParameter("nome"));
                    categoria.setDescrizione(request.getParameter("descrizione"));
                    categoriaDAO.doSave(categoria);
                    session.setAttribute("successMessage", "Categoria inserita con successo");

                } else if ("update".equalsIgnoreCase(action)) {
                    CategoriaBean categoria = new CategoriaBean();
                    categoria.setIdCategoria(Integer.parseInt(request.getParameter("id")));
                    categoria.setNome(request.getParameter("nome"));
                    categoria.setDescrizione(request.getParameter("descrizione"));
                    categoriaDAO.doUpdate(categoria);
                    session.setAttribute("successMessage", "Categoria aggiornata con successo");

                } else if ("delete".equalsIgnoreCase(action)) {
                    int id = Integer.parseInt(request.getParameter("id"));
                    categoriaDAO.doDelete(id);
                    session.setAttribute("successMessage", "Categoria eliminata");
                }
            }

        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Errore nei dati inseriti: ID non valido.");
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di persistenza nel Database: " + e.getMessage());
        }

        if (!response.isCommitted()) {
            response.sendRedirect(request.getContextPath() + "/AdminCategoriaServlet");
        }
    }
}
