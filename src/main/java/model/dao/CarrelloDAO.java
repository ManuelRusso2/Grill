package model.dao;

import java.sql.SQLException;
import model.bean.CarrelloBean;

public interface CarrelloDAO {

    void doSave(CarrelloBean carrello) throws SQLException;

    CarrelloBean doRetrieveByUtente(int idUtente) throws SQLException;

    CarrelloBean doRetrieveById(int idCarrello) throws SQLException;

    void doUpdate(CarrelloBean carrello) throws SQLException;

    boolean doEmpty(int idCarrello) throws SQLException;

    boolean doDelete(int idCarrello) throws SQLException;
}
