<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>
        <c:choose>
            <c:when test="${not empty categoriaAttiva}">
                <c:out value="${categoriaAttiva.nome}" />
            </c:when>
            <c:otherwise>Catalogo Prodotti</c:otherwise>
        </c:choose>
    </h1>

    <%-- Griglia Catalogo --%>
    <div class="grid">
        <c:choose>
            <c:when test="${not empty prodotti}">
                <c:forEach var="p" items="${prodotti}">
                    <c:if test="${p.attivo}">
                        <div class="card">
                            <a href="<c:url value='/DettaglioProdottoServlet?id=${p.idProdotto}'/>">
                                <img class="product-thumb" 
                                     src="<c:url value='/${p.immagine.startsWith("/") ? p.immagine.substring(1) : p.immagine}'/>" 
                                     alt="<c:out value='${p.nome}'/>" />
                            </a>
                            <h3>
                                <a href="<c:url value='/DettaglioProdottoServlet?id=${p.idProdotto}'/>">
                                    <c:out value="${p.nome}" />
                                </a>
                            </h3>
                            <p class="price">
                                <fmt:formatNumber value="${p.costo}" type="currency" currencySymbol="€" />
                            </p>
                            <c:choose>
                                <c:when test="${p.quantita <= 0}">
                                    <span class="badge badge-esaurito">Esaurito</span>
                                </c:when>
                                <c:when test="${p.quantita <= 5}">
                                    <span class="badge badge-scarso">Ultimi ${p.quantita} disponibili</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge badge-disponibile">Disponibile (${p.quantita})</span>
                                </c:otherwise>
                            </c:choose>
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