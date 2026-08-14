<%-- 
    Pagina di gestione/amministrazione degli utenti registrati (clienti).
    Consente all'amministratore di visualizzare l'elenco completo degli utenti e
    accedere alle informazioni dettagliate di ciascun profilo.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Inclusione della Tag Library JSTL Core per la gestione del flusso (cicli, condizioni) --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Inclusione dei frammenti statici per l'intestazione HTML e la barra di navigazione --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Gestione Utenti</h1>

    <%-- ── TABELLA ELENCO CLIENTI ─────────────────────────────────────────── --%>
    <div class="admin-table-wrapper">
        <table class="admin-table">
            <thead>
                <tr>
                    <th class="col-id">ID</th>
                    <th>Nome</th>
                    <th>Cognome</th>
                    <th>Email</th>
                    <th class="text-right col-actions">Azioni</th>
                </tr>
            </thead>
            <tbody>
                <%-- Utilizzo di c:choose per gestire il caso di lista vuota o popolata --%>
                <c:choose>
                    <c:when test="${not empty clienti}">
                        <c:forEach var="c" items="${clienti}">
                            <tr>
                                <td><strong>#<c:out value="${c.idUtente}" /></strong></td>
                                <td><c:out value="${c.nome}" /></td>
                                <td><c:out value="${c.cognome}" /></td>
                                <td><c:out value="${c.email}" /></td>
                                
                                <%-- Form per navigare al dettaglio specifico dell'utente selezionato --%>
                                <td class="text-right">
                                    <form method="post" action="${pageContext.request.contextPath}/AdminUtentiServlet" class="action-form">
                                        <input type="hidden" name="id" value="${c.idUtente}">
                                        <button type="submit" name="action" value="view" class="btn-view">Dettaglio</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    
                    <%-- Messaggio visualizzato in assenza di utenti nel database --%>
                    <c:otherwise>
                        <tr>
                            <td colspan="5" class="empty-table-msg">
                                Nessun cliente registrato nel sistema.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</main>

<%-- Inclusione del piè di pagina --%>
<%@ include file="/jsp/common/footer.jspf" %>