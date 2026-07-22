<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Catalogo Prodotti</h1>

    <%-- 1. Feedback Utente (Messaggi inviati da CatalogoServlet o altre operazioni) --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            <c:out value="${successMessage}" />
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- 2. Griglia Catalogo --%>
    <div class="grid">
        <c:choose>
            <c:when test="${not empty prodotti}">
                <c:forEach var="p" items="${prodotti}">
                    <c:if test="${p.attivo}">
                        <div class="card">
                            <h3>
                                <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${p.idProdotto}">
                                    <c:out value="${p.nome}" />
                                </a>
                            </h3>
                            <p class="price">
                                <fmt:formatNumber value="${p.costo}" type="currency" currencySymbol="€" />
                            </p>
                            
                            <%-- 3. Form Aggiunta Rapida al Carrello (Invia in POST a CarrelloServlet) --%>
                            <form action="${pageContext.request.contextPath}/CarrelloServlet" method="post" class="add-to-cart-form">
                                <input type="hidden" name="action" value="add">
                                <input type="hidden" name="idProdotto" value="${p.idProdotto}">
                                
                                <c:choose>
                                    <c:when test="${p.quantita > 0}">
                                        <input type="number" name="quantita" value="1" min="1" max="${p.quantita}" class="input-qty">
                                        <button type="submit" class="btn btn-primary">Aggiungi al Carrello</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="button" class="btn btn-disabled" disabled>Esaurito</button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                        </div>
                    </c:if>
                </c:forEach>
            </c:when>
            
            <%-- Gestione caso nessun prodotto nel sistema --%>
            <c:otherwise>
                <p class="empty-state">Al momento non ci sono prodotti disponibili nel catalogo.</p>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<%@ include file="/jsp/common/footer.jspf" %>