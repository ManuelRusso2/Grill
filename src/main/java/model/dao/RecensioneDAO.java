package model.dao;

import java.sql.SQLException;
import java.util.List;
import model.bean.RecensioneBean;

public interface RecensioneDAO {

    void doSave(RecensioneBean recensione) throws SQLException;

    void doUpdate(RecensioneBean recensione) throws SQLException;

    RecensioneBean doRetrieveById(int idRecensione) throws SQLException;

    List<RecensioneBean> doRetrieveByProdotto(int idProdotto) throws SQLException;

    List<RecensioneBean> doRetrieveByUtente(int idUtente) throws SQLException;

    List<RecensioneBean> doRetrieveAll() throws SQLException;

    boolean doDelete(int idRecensione) throws SQLException;
}
