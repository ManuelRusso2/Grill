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

/**
 * Servlet per la gestione dell'area amministrativa riservata alle categorie.
 * Gestisce le operazioni di CRUD (Create, Read, Update, Delete) sia tramite 
 * richieste HTTP tradizionali (richiesta/risposta con reindirizzamento) 
 * sia tramite chiamate asincrone AJAX.
 */
@WebServlet("/AdminCategoriaServlet")
public class AdminCategoriaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Riferimento all'interfaccia DAO per la manipolazione dei dati delle categorie
    private CategoriaDAO categoriaDAO;

    /**
     * Inizializzazione della Servlet: istanzia l'implementazione del DAO per le categorie.
     */
    @Override
    public void init() throws ServletException {
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    /**
     * Gestisce le richieste di tipo GET (recupero ed visualizzazione delle pagine/dati).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Recupera la sessione corrente (se esiste) per estrarre eventuali messaggi di feedback
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Sposta i messaggi dalla sessione alla Request per mostrarli nella JSP e pulisce la sessione
            moveSessionAttributeToRequest(session, request, "successMessage");
            moveSessionAttributeToRequest(session, request, "errorMessage");
        }

        // Recupera l'azione richiesta dall'URL (es. ?action=new o ?action=edit)
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        try {
            // Valuta il tipo di azione richiesta in modalita lettura
            switch (action.toLowerCase()) {
                
                // Caso per la creazione di una nuova categoria: inoltra al form vuoto
                case "new":
                    request.getRequestDispatcher("/jsp/admin/nuovo-categoria.jsp").forward(request, response);
                    return;

                // Caso per la modifica di una categoria esistente
                case "edit":
                    String idParam = request.getParameter("id");
                    if (idParam != null && !idParam.isEmpty()) {
                        int id = Integer.parseInt(idParam);
                        CategoriaBean categoria = categoriaDAO.doRetrieveById(id);
                        
                        // Se la categoria viene trovata, la imposta come attributo di request e apre il form
                        if (categoria != null) {
                            request.setAttribute("categoria", categoria);
                            request.getRequestDispatcher("/jsp/admin/nuovo-categoria.jsp").forward(request, response);
                            return;
                        } else if (session != null) {
                            session.setAttribute("errorMessage", "Categoria non trovata.");
                        }
                    }
                    break;

                default:
                    break;
            }

            // Flusso di default: recupera l'elenco completo di tutte le categorie e le mostra nella tabella di gestione
            List<CategoriaBean> categorie = categoriaDAO.doRetrieveAll();
            request.setAttribute("categorie", categorie);
            request.getRequestDispatcher("/jsp/admin/gestione-categorie.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            // Errore nella conversione dell'ID numerico
            if (session != null) {
                session.setAttribute("errorMessage", "ID categoria non valido.");
            }
            response.sendRedirect(request.getContextPath() + "/AdminCategoriaServlet");
        } catch (SQLException e) {
            // Gestione dell'errore di accesso al Database con log e invio codice HTTP 500
            getServletContext().log("Errore SQL in doGet AdminCategoriaServlet", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno durante il recupero dei dati.");
        }
    }

    /**
     * Gestisce le richieste di tipo POST (inserimento, modifica ed eliminazione dati).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Ottiene o crea la sessione HTTP per memorizzare i messaggi esito delle operazioni
        HttpSession session = request.getSession(true);
        
        // Recupera l'azione richiesta inviata nel corpo della request
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        try {
            switch (action) {
                
                // Gestione della creazione rapida di una categoria via AJAX
                case "newCategory":
                    handleNewCategoryAjax(request, response);
                    return;

                // Gestione dell'eliminazione rapida via AJAX
                case "deleteCategory":
                    handleDeleteCategoryAjax(request, response);
                    return;

                // Salvataggio di una nuova categoria da form tradizionale
                case "save":
                    String nomeSave = request.getParameter("nome");
                    if (nomeSave == null || nomeSave.trim().isEmpty()) {
                        session.setAttribute("errorMessage", "Il nome della categoria è obbligatorio.");
                    } else {
                        CategoriaBean nuovaCat = new CategoriaBean();
                        nuovaCat.setNome(nomeSave.trim());
                        nuovaCat.setDescrizione(request.getParameter("descrizione"));
                        categoriaDAO.doSave(nuovaCat);
                        session.setAttribute("successMessage", "Categoria inserita con successo.");
                    }
                    break;

                // Aggiornamento dei dati di una categoria da form tradizionale
                case "update":
                    String nomeUpdate = request.getParameter("nome");
                    if (nomeUpdate == null || nomeUpdate.trim().isEmpty()) {
                        session.setAttribute("errorMessage", "Il nome della categoria è obbligatorio.");
                    } else {
                        CategoriaBean catUpdate = new CategoriaBean();
                        catUpdate.setIdCategoria(Integer.parseInt(request.getParameter("id")));
                        catUpdate.setNome(nomeUpdate.trim());
                        catUpdate.setDescrizione(request.getParameter("descrizione"));
                        categoriaDAO.doUpdate(catUpdate);
                        session.setAttribute("successMessage", "Categoria aggiornata con successo.");
                    }
                    break;

                // Eliminazione da azione form tradizionale
                case "delete":
                    int id = Integer.parseInt(request.getParameter("id"));
                    categoriaDAO.doDelete(id);
                    session.setAttribute("successMessage", "Categoria eliminata con successo.");
                    break;

                default:
                    break;
            }
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Errore nei dati inseriti: ID non valido.");
        } catch (SQLException e) {
            // Scrive il dettaglio dell'errore nel log del server ed evita di esporre dati sensibili del DB all'utente
            getServletContext().log("Errore SQL in doPost AdminCategoriaServlet", e);
            session.setAttribute("errorMessage", "Errore nell'elaborazione della richiesta sul database.");
        }

        // Se la risposta non è già stata gestita (es. inviata tramite AJAX), effettua un redirect per evitare il re-invio dei form
        if (!response.isCommitted()) {
            response.sendRedirect(request.getContextPath() + "/AdminCategoriaServlet");
        }
    }

    // =========================================================================
    // HELPER METHODS (METODI DI SUPPORTO)
    // =========================================================================

    /**
     * Trasferisce un attributo memorizzato temporaneamente in Sessione all'interno del Request Scope,
     * rimuovendolo poi dalla Sessione (utile per i messaggi di "flash notification").
     */
    private void moveSessionAttributeToRequest(HttpSession session, HttpServletRequest request, String attributeName) {
        if (session.getAttribute(attributeName) != null) {
            request.setAttribute(attributeName, session.getAttribute(attributeName));
            session.removeAttribute(attributeName);
        }
    }

    /**
     * Gestisce la creazione di una nuova categoria inviata tramite chiamata asincrona AJAX.
     * Risponde inviando una stringa formattata in JSON.
     */
    private void handleNewCategoryAjax(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        
        String nome = request.getParameter("nome");
        String descrizione = request.getParameter("descrizione");

        // Validazione: controlla che il nome non sia vuoto
        if (nome != null && !nome.trim().isEmpty()) {
            CategoriaBean categoria = new CategoriaBean();
            categoria.setNome(nome.trim());
            categoria.setDescrizione(descrizione != null ? descrizione.trim() : "");
            
            // Salvataggio nel Database (il DAO popolerà l'ID generato automaticamente)
            categoriaDAO.doSave(categoria);

            // Costruzione della risposta JSON di successo
            String json = String.format(
                "{\"success\": true, \"message\": \"Categoria creata\", \"id\": %d, \"nome\": \"%s\"}",
                categoria.getIdCategoria(),
                escapeJson(categoria.getNome())
            );
            sendJson(response, json);
        } else {
            // Risposta JSON in caso di validazione fallita
            sendJson(response, "{\"success\": false, \"message\": \"Nome categoria obbligatorio\"}");
        }
    }

    /**
     * Gestisce l'eliminazione di una categoria inviata tramite chiamata asincrona AJAX.
     */
    private void handleDeleteCategoryAjax(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            sendJson(response, "{\"success\": false, \"message\": \"ID mancante\"}");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            boolean ok = categoriaDAO.doDelete(id);
            
            if (ok) {
                sendJson(response, "{\"success\": true, \"message\": \"Categoria eliminata\", \"id\": " + id + "}");
            } else {
                sendJson(response, "{\"success\": false, \"message\": \"Categoria non trovata\"}");
            }
        } catch (NumberFormatException e) {
            sendJson(response, "{\"success\": false, \"message\": \"ID non valido\"}");
        } catch (SQLException sqle) {
            // Se fallisce l'eliminazione per vincolo di chiave esterna (es. prodotti associati)
            getServletContext().log("Errore SQL durante eliminazione AJAX categoria", sqle);
            sendJson(response, "{\"success\": false, \"message\": \"Impossibile eliminare la categoria: esistono prodotti collegati\"}");
        }
    }

    /**
     * Configura l'header HTTP della risposta impostando il tipo di contenuto come JSON
     * e scrive la stringa JSON nello stream di output.
     */
    private void sendJson(HttpServletResponse response, String jsonContent) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonContent);
    }

    /**
     * Esegue l'escaping elementare dei caratteri speciali per evitare errori di sintassi
     * nella generazione manuale delle stringhe JSON.
     */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}