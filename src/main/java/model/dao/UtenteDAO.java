package model.dao;

import java.sql.SQLException;
import java.util.List;
import model.bean.UtenteBean;

public interface UtenteDAO {

    /**
     * Salva un nuovo utente nel database.
     * Requisito: Registrazione utente.
     * * @param utente l'oggetto UtenteBean da salvare (con password già cifrata)
     * @throws SQLException in caso di errore di accesso al database
     */
    void doSave(UtenteBean utente) throws SQLException;

    /**
     * Recupera un utente verificando le credenziali di accesso (email e password).
     * Requisiti: Login, Gestione sessione e Cifratura.
     * * @param email l'email inserita nel form di login
     * @param password la password in chiaro inserita nel form di login
     * @return l'oggetto UtenteBean se le credenziali sono corrette, null altrimenti
     * @throws SQLException in caso di errore di accesso al database
     */
    UtenteBean doRetrieveByLogin(String email, String password) throws SQLException;
    
    /**
     * Cerca un utente tramite la sua email.
     * Requisiti: Login e Verifica AJAX email duplicata.
     * * @param email l'email da cercare
     * @return l'oggetto UtenteBean se trovato, null se l'email non è presente
     * @throws SQLException in caso di errore di accesso al database
     */
    UtenteBean doRetrieveByEmail(String email) throws SQLException;

    /**
     * Cerca un utente tramite username.
     * Requisiti: Registrazione e vincoli di unicità.
     */
    UtenteBean doRetrieveByUsername(String username) throws SQLException;

    /**
     * Cerca un utente tramite il suo ID (Chiave Primaria).
     * Requisiti: Storico ordini ed estrazione dati cliente partendo dalla Foreign Key dell'ordine.
     * * @param id l'identificativo numerico dell'utente
     * @return l'oggetto UtenteBean se trovato, null altrimenti
     * @throws SQLException in caso di errore di accesso al database
     */
    UtenteBean doRetrieveById(int id) throws SQLException;

    /**
     * Restituisce la lista di tutti gli utenti con ruolo 'Cliente'.
     * Requisito: Filtro ordini per cliente (Area Admin).
     * * @return una List di oggetti UtenteBean (esclusi gli admin)
     * @throws SQLException in caso di errore di accesso al database
     */
    List<UtenteBean> doRetrieveAllClienti() throws SQLException;
}