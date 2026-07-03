package model.dao;

import java.sql.SQLException;
import java.util.List;
import model.bean.AcquistoBean;

public interface AcquistoDAO {

    void doSave(AcquistoBean acquisto) throws SQLException;

    void doUpdate(AcquistoBean acquisto) throws SQLException;

    AcquistoBean doRetrieveById(int idAcquisto) throws SQLException;

    List<AcquistoBean> doRetrieveByUtente(int idUtente) throws SQLException;

    List<AcquistoBean> doRetrieveAll() throws SQLException;

    boolean doDelete(int idAcquisto) throws SQLException;
}
