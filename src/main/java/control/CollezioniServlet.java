package control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.bean.CollezioneBean;
import model.bean.ProdottoBean;
import model.dao.CollezioneDAO;
import model.dao.ProdottoDAO;
import model.dao.impl.CollezioneDAOImpl;
import model.dao.impl.ProdottoDAOImpl;

@WebServlet("/CollezioniServlet")
public class CollezioniServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private CollezioneDAO collezioneDAO;
    private ProdottoDAO prodottoDAO;

    @Override
    public void init() throws ServletException {
        this.collezioneDAO = new CollezioneDAOImpl();
        this.prodottoDAO = new ProdottoDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            List<CollezioneBean> collezioni = collezioneDAO.doRetrieveAll();
            List<ProdottoBean> prodotti = prodottoDAO.doRetrieveAllClientiRaggruppati();

            // Raggruppa i prodotti per ciascuna collezione
            Map<CollezioneBean, List<ProdottoBean>> collezioniMap = new LinkedHashMap<>();

            for (CollezioneBean col : collezioni) {
                List<ProdottoBean> prodottiCollezione = new ArrayList<>();
                for (ProdottoBean p : prodotti) {
                    if (p.isAttivo() && p.getIdCollezione() != null && p.getIdCollezione() == col.getIdCollezione()) {
                        prodottiCollezione.add(p);
                    }
                }
                if (!prodottiCollezione.isEmpty()) {
                    collezioniMap.put(col, prodottiCollezione);
                }
            }

            request.setAttribute("collezioniMap", collezioniMap);
            
            request.getRequestDispatcher("/jsp/common/collezioni.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}