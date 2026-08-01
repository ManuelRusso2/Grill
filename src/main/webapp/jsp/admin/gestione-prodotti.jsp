<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

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
        <div class="alert-box-out-of-stock">
            <h2>⚠️ Prodotti Esauriti</h2>
            <table class="table-custom table-out-of-stock">
                <thead>
                    <tr>
                        <th class="text-left">ID</th>
                        <th class="text-left">Nome</th>
                        <th class="text-right">Prezzo</th>
                        <th class="text-center">Quantità</th>
                        <th class="text-center">Stato</th>
                        <th class="text-center">Azioni</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="prodotto" items="${prodottiAdmin}">
                        <c:if test="${prodotto.quantita <= 0}">
                            <tr>
                                <td><c:out value="${prodotto.idProdotto}" /></td>
                                <td class="font-semibold"><c:out value="${prodotto.nome}" /></td>
                                <td class="text-right">
                                    <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" />
                                </td>
                                <td class="text-center text-out-of-stock">0</td>
                                <td class="text-center">
                                    <span class="badge badge-danger">Esaurito</span>
                                </td>
                                <td class="text-center">
                                    <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=edit&id=${prodotto.idProdotto}" 
                                       class="btn-edit btn-edit-sm">
                                        ✏️ Rifornisci / Modifica
                                    </a>
                                </td>
                            </tr>
                        </c:if>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

    <%-- 3. PULSANTE AGGIUNGI NUOVO PRODOTTO --%>
    <div class="actions-bar">
        <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=new" class="btn-add">
            ➕ Aggiungi Nuovo Prodotto
        </a>
    </div>

    <%-- 4. TABELLA TUTTI I PRODOTTI (CATALOGO COMPLETO) --%>
    <c:choose>
        <c:when test="${not empty prodottiAdmin}">
            <h2>Tutti i Prodotti</h2>
            <table class="table-custom">
                <thead>
                    <tr>
                        <th class="text-left">ID</th>
                        <th class="text-left">Nome</th>
                        <th class="text-left">Descrizione</th>
                        <th class="text-right">Prezzo</th>
                        <th class="text-center">Quantità</th>
                        <th class="text-center">Categorie</th>
                        <th class="text-center">Stato</th>
                        <th class="text-center">Azioni</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="prodotto" items="${prodottiAdmin}" varStatus="status">
                        <tr class="${status.index % 2 == 0 ? 'row-zebra' : ''}">
                            <td>
                                <c:out value="${prodotto.idProdotto}" />
                            </td>
                            <td class="font-medium">
                                <c:out value="${prodotto.nome}" />
                            </td>
                            <td class="col-desc">
                                <c:out value="${prodotto.descrizione}" />
                            </td>
                            <td class="text-right font-semibold">
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
                                <span class="badge ${prodotto.attivo ? 'badge-active' : 'badge-inactive'}">
                                    ${prodotto.attivo ? 'Attivo' : 'Inattivo'}
                                </span>
                            </td>
                            <td class="text-center nowrap">
                                <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=edit&id=${prodotto.idProdotto}" class="btn-edit">
                                    ✏️ Modifica
                                </a>

								<form method="post" action="${pageContext.request.contextPath}/AdminProdottoServlet" 
								      class="inline-form" 
								      onsubmit="return confirm('Sei sicuro di voler eliminare il prodotto \'${fn:escapeXml(prodotto.nome)}\'?');">
								    <input type="hidden" name="action" value="delete">
								    <input type="hidden" name="id" value="${prodotto.idProdotto}">
								    <button type="submit" class="btn-delete">
								        🗑️ Elimina
								    </button>
								</form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>

        <c:otherwise>
            <div class="empty-state">
                <p>Il catalogo è attualmente vuoto. Inizia ad aggiungere prodotti!</p>
                <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=new" class="btn-add">
                    ➕ Aggiungi il Primo Prodotto
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="/jsp/common/footer.jspf" %>