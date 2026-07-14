package control;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Recuperiamo la sessione corrente, se esiste, senza crearne una nuova
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // 2. Invalidiamo la sessione (elimina tutti gli attributi salvati, compreso l'utente)
            session.invalidate();
        }
        
        // 3. Reindirizziamo l'utente alla servlet del catalogo o alla homepage
        response.sendRedirect(request.getContextPath() + "/CatalogoServlet");
    }

    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Spesso il logout può essere chiamato anche via POST per sicurezza, lo gestiamo allo stesso modo
        doGet(request, response);
    }
}