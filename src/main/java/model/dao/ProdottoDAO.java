package model.dao;

import java.sql.SQLException;
import java.util.List;
import model.bean.ProdottoBean;

public interface ProdottoDAO {

    void doSave(ProdottoBean prodotto) throws SQLException;

    void doUpdate(ProdottoBean prodotto) throws SQLException;

    boolean doDelete(int idProdotto) throws SQLException;

    ProdottoBean doRetrieveByKey(int idProdotto) throws SQLException;

    List<ProdottoBean> doRetrieveAllClienti() throws SQLException;

    List<ProdottoBean> doRetrieveAllAdmin() throws SQLException;

    List<ProdottoBean> doRetrieveByCategoria(int idCategoria) throws SQLException;

    List<ProdottoBean> doRetrieveBySearch(String query) throws SQLException;

    List<ProdottoBean> doRetrieveAllClientiRaggruppati() throws SQLException;

    List<ProdottoBean> doRetrieveVarianti(String nomeBase) throws SQLException;
}
