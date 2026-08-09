<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1 style="text-align: center; margin-bottom: 40px;">Le Nostre Collezioni</h1>

    <c:choose>
        <c:when test="${not empty collezioniMap}">
            <c:forEach var="entry" items="${collezioniMap}">
                <section class="collection-block" style="margin-bottom: 55px;">
                    <%-- Nome della Collezione sopra la griglia --%>
                    <h2 style="font-size: 24px; color: var(--accent-yellow); margin-bottom: 20px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 2px solid var(--border-color); padding-bottom: 10px;">
                        <c:out value="${entry.key.nomeCollezione}" />
                    </h2>

                    <c:if test="${not empty entry.key.descrizione}">
                        <p style="color: var(--text-gray); margin-top: -10px; margin-bottom: 20px; font-size: 14px;">
                            <c:out value="${entry.key.descrizione}" />
                        </p>
                    </c:if>

                    <%-- Griglia prodotti identica a catalogo.jsp --%>
                    <div class="grid">
                        <c:forEach var="p" items="${entry.value}">
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
                        </c:forEach>
                    </div>
                </section>
            </c:forEach>
        </c:when>
        
        <c:otherwise>
            <p class="empty-state">Al momento non ci sono collezioni attive con prodotti disponibili.</p>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="/jsp/common/footer.jspf" %>