<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 1. Importiamo la taglib JSTL Core per la gestione dei cicli e delle condizioni --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Gestione Utenti</h1>

    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Nome</th>
                <th>Cognome</th>
                <th>Email</th>
                <th>Username</th>
                <th>Azioni</th>
            </tr>
        </thead>
        <tbody>
            <%-- 2. Ciclo sulla lista dei clienti inviata dalla AdminUtentiServlet --%>
            <c:if test="${not empty clienti}">
                <c:forEach var="c" items="${clienti}">
                    <tr>
                        <td><c:out value="${c.idUtente}" /></td>
                        
                        <%-- 🌟 Protezione XSS sistematica sui dati anagrafici inseriti dagli utenti --%>
                        <td><c:out value="${c.nome}" /></td>
                        <td><c:out value="${c.cognome}" /></td>
                        <td><c:out value="${c.email}" /></td>
                        <td><c:out value="${c.username}" /></td>
                        
                        <td>
                            <%-- 3. Form delle azioni amministrative con Context Path dinamico --%>
                            <form method="post" action="${pageContext.request.contextPath}/AdminUtentiServlet">
                                <input type="hidden" name="id" value="${c.idUtente}">
                                <button type="submit" name="action" value="view">Dettaglio</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </c:if>

            <%-- 4. Feedback nel caso in cui la lista dei clienti sia vuota --%>
            <c:if test="${empty clienti}">
                <tr>
                    <td colspan="6" style="text-align: center; font-style: italic; color: #777;">
                        Nessun cliente registrato nel sistema.
                    </td>
                </tr>
            </c:if>
        </tbody>
    </table>
</main>

<%@ include file="/jsp/common/footer.jspf" %>