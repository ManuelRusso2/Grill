<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Gestione Prodotti</h1>

    <%-- 1. BLOCCO FEEDBACK UTENTE --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            ✓ <c:out value="${successMessage}" />
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            ✗ <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- 2. TABELLA PRODOTTI ESAURITI (Mostrata SOLO se ci sono prodotti con quantita <= 0) --%>
    <c:set var="hasEsauriti" value="false" />
    <c:forEach var="p" items="${prodottiAdmin}">
        <c:if test="${p.quantita <= 0}">
            <c:set var="hasEsauriti" value="true" />
        </c:if>
    </c:forEach>

    <c:if test="${hasEsauriti}">
        <div class="alert-box-out-of-stock" style="margin-bottom: 30px;">
            <h2 style="color: #ff4a4a; margin-bottom: 15px;">⚠️ Prodotti Esauriti</h2>
            <div class="admin-table-wrapper">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nome</th>
                            <th>Prezzo</th>
                            <th class="text-center">Quantità</th>
                            <th class="text-center">Stato</th>
                            <th class="text-center">Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="prodotto" items="${prodottiAdmin}">
                            <c:if test="${prodotto.quantita <= 0}">
                                <tr>
                                    <td>#<c:out value="${prodotto.idProdotto}" /></td>
                                    <td><strong><c:out value="${prodotto.nome}" /></strong></td>
                                    <td>
                                        <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" />
                                    </td>
                                    <td class="text-center" style="color: #ff4a4a; font-weight: bold;">0</td>
                                    <td class="text-center">
                                        <span class="badge-esaurito">Esaurito</span>
                                    </td>
                                    <td class="text-center">
                                        <div class="action-cell" style="justify-content: center;">
                                            <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=edit&id=${prodotto.idProdotto}" 
                                               class="btn-edit">
                                                ✏️ Rifornisci / Modifica
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:if>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>

    <%-- 3. BARRA SUPERIORE E TABELLA CATALOGO COMPLETO --%>
    <c:choose>
        <c:when test="${not empty prodottiAdmin}">
            <div class="admin-toolbar">
                <h2>Tutti i Prodotti</h2>
                <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=new" class="btn btn-add">
                    + Aggiungi Nuovo Prodotto
                </a>
            </div>

            <div class="admin-table-wrapper">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nome</th>
                            <th>Descrizione</th>
                            <th>Prezzo</th>
                            <th class="text-center">Quantità</th>
                            <th class="text-center">Categorie</th>
                            <th class="text-center">Stato</th>
                            <th class="text-center">Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="prodotto" items="${prodottiAdmin}">
                            <tr>
                                <td>#<c:out value="${prodotto.idProdotto}" /></td>
                                <td><strong><c:out value="${prodotto.nome}" /></strong></td>
                                <td style="max-width: 300px; color: var(--text-gray);">
                                    <c:out value="${prodotto.descrizione}" />
                                </td>
                                <td>
                                    <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" />
                                </td>
                                <td class="text-center">
                                    <c:out value="${prodotto.quantita}" />
                                </td>
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${not empty prodotto.categorie}">
                                            <c:forEach var="cat" items="${prodotto.categorie}" varStatus="s">
                                                <c:out value="${cat.nome}" /><c:if test="${!s.last}">, </c:if>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">
                                    <span class="${prodotto.attivo ? 'status-attivo' : 'badge-esaurito'}">
                                        ${prodotto.attivo ? 'Attivo' : 'Inattivo'}
                                    </span>
                                </td>
                                <td class="text-center">
                                    <div class="action-cell" style="justify-content: center;">
                                        <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=edit&id=${prodotto.idProdotto}" class="btn-edit">
                                            Modifica
                                        </a>

                                        <form method="post" action="${pageContext.request.contextPath}/AdminProdottoServlet" 
                                              onsubmit="return confirm('Sei sicuro di voler eliminare il prodotto \'${fn:escapeXml(prodotto.nome)}\'?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${prodotto.idProdotto}">
                                            <button type="submit" class="btn-delete">
                                                Elimina
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:when>

        <c:otherwise>
            <div style="text-align: center; padding: 50px 20px; background: var(--bg-card); border-radius: 8px;">
                <p style="margin-bottom: 20px; color: var(--text-gray);">Il catalogo è attualmente vuoto. Inizia ad aggiungere prodotti!</p>
                <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=new" class="btn btn-add">
                    + Aggiungi il Primo Prodotto
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="/jsp/common/footer.jspf" %>