<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Profilo Utente</h1>
    
    <%-- SEZIONE: Dettagli Profilo Utente --%>
    <c:if test="${not empty sessionScope.utente}">
        <div class="profile-card">
            <h2>Informazioni Personali</h2>
            <div class="profile-details">
                <div class="detail-row">
                    <span class="detail-label">Nome:</span>
                    <span class="detail-value"><c:out value="${sessionScope.utente.nome}" /></span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Cognome:</span>
                    <span class="detail-value"><c:out value="${sessionScope.utente.cognome}" /></span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Email:</span>
                    <span class="detail-value"><c:out value="${sessionScope.utente.email}" /></span>
                </div>
                <c:if test="${not empty sessionScope.utente.telefono}">
                    <div class="detail-row">
                        <span class="detail-label">Telefono:</span>
                        <span class="detail-value"><c:out value="${sessionScope.utente.telefono}" /></span>
                    </div>
                </c:if>
                <c:if test="${not empty sessionScope.utente.dataRegistrazione}">
                    <div class="detail-row">
                        <span class="detail-label">Registrato dal:</span>
                        <span class="detail-value">
                            <fmt:formatDate value="${sessionScope.utente.dataRegistrazione}" pattern="dd/MM/yyyy HH:mm" />
                        </span>
                    </div>
                </c:if>
            </div>
        </div>
    </c:if>

    <%-- SEZIONE: Storico Ordini --%>
    <div class="orders-section">
        <h2 class="section-title">Storico Ordini</h2>
        <p class="section-subtitle">Qui trovi lo storico dei tuoi acquisti effettuati su Grill.</p>

        <table class="cart-table orders-table">
            <thead>
                <tr>
                    <th>ID Ordine</th>
                    <th>Data</th>
                    <th>Totale</th>
                    <th>Dettaglio</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty acquisti}">
                        <c:forEach var="a" items="${acquisti}">
                            <tr>
                                <td>#<c:out value="${a.idAcquisto}" /></td>
                                <td>
                                    <fmt:formatDate value="${a.dataAcquisto}" pattern="dd/MM/yyyy HH:mm" />
                                </td>
                                <td class="order-price">
                                    <fmt:formatNumber value="${a.prezzoTotale}" type="currency" currencySymbol="€" />
                                </td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/DettaglioOrdineServlet?id=${a.idAcquisto}" class="order-link">
                                        Visualizza ➔
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="4" class="empty-table-msg">
                                Non hai ancora effettuato nessun ordine su Grill.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</main>

<%@ include file="/jsp/common/footer.jspf" %>