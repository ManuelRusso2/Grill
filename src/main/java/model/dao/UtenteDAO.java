import java.sql.SQLException;
import java.util.List;

public interface UtenteDAO {

    /**
     * Salva un nuovo utente nel database.
     * Requisito: Registrazione utente.
     * * @param utente l'oggetto Utente da salvare (con password già cifrata)
     * @throws SQLException in caso di errore di accesso al database
     */
    void doSave(Utente utente) throws SQLException;

    /**
     * Cerca un utente tramite la sua email.
     * Requisiti: Login e Verifica AJAX email duplicata.
     * * @param email l'email da cercare
     * @return l'oggetto Utente se trovato, null se l'email non è presente
     * @throws SQLException in caso di errore di accesso al database
     */
    Utente doRetrieveByEmail(String email) throws SQLException;

    /**
     * Cerca un utente tramite il suo ID (Chiave Primaria).
     * Requisiti: Storico ordini ed estrazione dati cliente partendo dalla Foreign Key dell'ordine.
     * * @param id l'identificativo numerico dell'utente
     * @return l'oggetto Utente se trovato, null altrimenti
     * @throws SQLException in caso di errore di accesso al database
     */
    Utente doRetrieveById(int id) throws SQLException;

    /**
     * Restituisce la lista di tutti gli utenti con ruolo 'Cliente'.
     * Requisito: Filtro ordini per cliente (Area Admin).
     * * @return una List di oggetti Utente (esclusi gli admin)
     * @throws SQLException in caso di errore di accesso al database
     */
    List<Utente> doRetrieveAllClienti() throws SQLException;

}