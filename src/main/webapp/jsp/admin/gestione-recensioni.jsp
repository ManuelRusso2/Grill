<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Gestione Recensioni</h1>

    <%-- Messaggi di feedback --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success"><c:out value="${successMessage}" /></div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger"><c:out value="${errorMessage}" /></div>
    </c:if>

    <div class="admin-reviews-section">
        <p class="section-subtitle">Qui puoi visualizzare tutte le recensioni lasciate dagli utenti e eliminarle se necessario.</p>

        <%-- Form del Filtro Utenti --%>
        <div class="filter-container">
            <form action="${pageContext.request.contextPath}/AdminRecensioniServlet" method="get" class="filter-form">
                <label for="filtroUtente">Filtra per Utente:</label>
                <select name="idUtente" id="filtroUtente" onchange="this.form.submit()">
                    <option value="">-- Tutti gli utenti --</option>
                    <c:forEach var="u" items="${tuttiUtenti}">
                        <option value="${u.idUtente}" ${param.idUtente == u.idUtente ? 'selected' : ''}>
                            <c:out value="${u.nome} ${u.cognome} (${u.email})" />
                        </option>
                    </c:forEach>
                </select>
                <c:if test="${not empty param.idUtente}">
                    <a href="${pageContext.request.contextPath}/AdminRecensioniServlet" class="btn-reset-filter">Mostra Tutti</a>
                </c:if>
            </form>
        </div>

        <c:choose>
            <c:when test="${not empty tuteRecensioni}">
                <div class="admin-table-wrapper">
                    <table class="admin-table admin-reviews-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Utente</th>
                                <th>Email</th>
                                <th>Prodotto</th>
                                <th>Valutazione</th>
                                <th>Data</th>
                                <th>Descrizione</th>
                                <th>Azioni</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="rec" items="${tuteRecensioni}">
                                <tr>
                                    <td><c:out value="${rec.idRecensione}" /></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty rec.nomeUtente || not empty rec.cognomeUtente}">
                                                <c:out value="${rec.nomeUtente}" /> <c:out value="${rec.cognomeUtente}" />
                                            </c:when>
                                            <c:otherwise><span class="text-muted">N/D</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty rec.emailUtente}">
                                                <c:out value="${rec.emailUtente}" />
                                            </c:when>
                                            <c:otherwise><span class="text-muted">N/D</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty rec.nomeProdotto}">
                                                <c:out value="${rec.nomeProdotto}" />
                                            </c:when>
                                            <c:otherwise><span class="text-muted">N/D</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="stars">
                                            <c:forEach begin="1" end="${rec.valutazione}">★</c:forEach>
                                        </div>
                                    </td>
                                    <td>
                                        <fmt:formatDate value="${rec.dataRecensione}" pattern="dd/MM/yyyy HH:mm" />
                                    </td>
                                    <td class="review-description">
                                        <c:out value="${rec.descrizione}" />
                                    </td>
                                    <td>
                                        <form action="${pageContext.request.contextPath}/AdminRecensioniServlet" method="post" class="action-form" onsubmit="return confirm('Sei sicuro di voler eliminare questa recensione?');">
                                            <input type="hidden" name="action" value="delete" />
                                            <input type="hidden" name="idRecensione" value="${rec.idRecensione}" />
                                            <input type="hidden" name="idUtente" value="${param.idUtente}" />
                                            <button type="submit" class="btn-delete">Elimina</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <p class="no-reviews-msg">
                    Nessuna recensione trovata per il criterio selezionato.
                </p>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<%@ include file="/jsp/common/footer.jspf" %>