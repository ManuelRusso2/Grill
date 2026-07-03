package model.dao;

import java.sql.SQLException;
import java.util.List;
import model.bean.CategoriaBean;

public interface CategoriaDAO {

    void doSave(CategoriaBean categoria) throws SQLException;

    void doUpdate(CategoriaBean categoria) throws SQLException;

    CategoriaBean doRetrieveById(int idCategoria) throws SQLException;

    List<CategoriaBean> doRetrieveAll() throws SQLException;

    boolean doDelete(int idCategoria) throws SQLException;
}
