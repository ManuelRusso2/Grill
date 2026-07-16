<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Importiamo JSTL Core e JSTL Format per formattare i prezzi in Euro --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Carrello</h1>
    
    <c:if test="${empty prodottiCarrello}">
        <p>Il carrello è vuoto.</p>
    </c:if>
    
    <c:if test="${not empty prodottiCarrello}">
        <table>
            <thead>
                <tr>
                    <th>Prodotto</th>
                    <th>Prezzo</th>
                    <th>Quantità</th>
                    <th>Azione</th>
                </tr>
            </thead>
            <tbody>
                <%-- Iteriamo direttamente sulla mappa "prodottiCarrello" --%>
                <%-- In JSTL, iterando su una Map, 'entry.key' è il ProdottoBean e 'entry.value' è la quantità --%>
                <c:forEach var="entry" items="${prodottiCarrello}">
                    <c:set var="prodotto" value="${entry.key}" />
                    <c:set var="quantita" value="${entry.value}" />
                    <tr>
                        <td>
                            <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${prodotto.idProdotto}">
                                <c:out value="${prodotto.nome}"/>
                            </a>
                        </td>
                        <td>
                            <%-- Formatta automaticamente il prezzo in valuta € --%>
                            <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€"/>
                        </td>
                        <td><c:out value="${quantita}"/></td>
                        <td>
                            <form method="post" action="${pageContext.request.contextPath}/CarrelloServlet">
                                <input type="hidden" name="id" value="${prodotto.idProdotto}">
                                <button name="action" value="remove">Rimuovi</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </c:if>

    <a href="${pageContext.request.contextPath}/CheckoutServlet">Vai al checkout</a>
</main>

<%@ include file="/jsp/common/footer.jspf" %>