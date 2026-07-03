package model.dao;

import java.sql.SQLException;
import java.util.List;
import model.bean.CollezioneBean;

public interface CollezioneDAO {

    void doSave(CollezioneBean collezione) throws SQLException;

    void doUpdate(CollezioneBean collezione) throws SQLException;

    CollezioneBean doRetrieveById(int idCollezione) throws SQLException;

    List<CollezioneBean> doRetrieveAll() throws SQLException;

    boolean doDelete(int idCollezione) throws SQLException;
}
