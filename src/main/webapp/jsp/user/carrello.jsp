<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Il tuo Carrello</h1>

    <%-- 1. BLOCCO FEEDBACK UTENTE (Flash Messages) --%>
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

    <%-- 2. GESTIONE STATO CARRELLO --%>
    <c:choose>
        <c:when test="${not empty prodottiCarrello}">
            <%-- Inizializziamo il totale del carrello --%>
            <c:set var="totaleCarrello" value="0" scope="page" />

            <table class="cart-table">
                <thead>
                    <tr>
                        <th>Prodotto</th>
                        <th>Prezzo Unitario</th>
                        <th>Quantità</th>
                        <th>Subtotale</th>
                        <th>Azioni</th>
                    </tr>
                </thead>
                <tbody>
                    <%-- Iterazione sulla Map<ProdottoBean, Integer> --%>
                    <c:forEach var="entry" items="${prodottiCarrello}">
                        <c:set var="prodotto" value="${entry.key}" />
                        <c:set var="quantita" value="${entry.value}" />
                        <c:set var="subtotale" value="${prodotto.costo * quantita}" />
                        
                        <%-- Calcolo del totale progressivo --%>
                        <c:set var="totaleCarrello" value="${totaleCarrello + subtotale}" />

                        <tr>
                            <td>
                                <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${prodotto.idProdotto}" class="cart-product-title">
                                    <c:out value="${prodotto.nome}" />
                                </a>
                            </td>
                            <td>
                                <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" />
                            </td>
                            <td>
                                <%-- Form per AGGIORNARE la quantità --%>
                                <form method="post" action="${pageContext.request.contextPath}/CarrelloServlet" class="cart-update-form">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="idProdotto" value="${prodotto.idProdotto}">
                                    <input type="number" name="quantita" value="${quantita}" min="1" max="${prodotto.quantita}" class="input-qty">
                                    <button type="submit" class="btn btn-secondary btn-small">Aggiorna</button>
                                </form>
                            </td>
                            <td>
                                <fmt:formatNumber value="${subtotale}" type="currency" currencySymbol="€" />
                            </td>
                            <td>
                                <%-- Form per RIMUOVERE il prodotto --%>
                                <form method="post" action="${pageContext.request.contextPath}/CarrelloServlet" class="cart-remove-form">
                                    <input type="hidden" name="action" value="remove">
                                    <input type="hidden" name="idProdotto" value="${prodotto.idProdotto}">
                                    <button type="submit" class="btn-delete" onclick="return confirm('Rimuovere questo prodotto dal carrello?')">
                                        Rimuovi
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

			<%-- Riquadro Totale e azioni carrello --%>
            <div class="cart-summary">
                <h2>Totale Ordine: <span><fmt:formatNumber value="${totaleCarrello}" type="currency" currencySymbol="€" /></span></h2>
                
                <div class="cart-actions-group">
                    <a href="${pageContext.request.contextPath}/CheckoutServlet" class="btn btn-checkout">
                        Procedi al Checkout
                    </a>

                    <%-- Form per svuotare il carrello con stile secondario (Rosso tenue) --%>
                    <form method="post" action="${pageContext.request.contextPath}/CarrelloServlet">
                        <input type="hidden" name="action" value="empty" />
                        <button type="submit" class="btn-empty-cart" onclick="return confirm('Sei sicuro di voler svuotare completamente il carrello?');">
                            Svuota Carrello
                        </button>
                    </form>
                </div>
            </div>
        </c:when>

        <%-- Stato carrello vuoto --%>
        <c:otherwise>
            <div class="empty-state">
                <p>Il tuo carrello è attualmente vuoto.</p>
                <a href="${pageContext.request.contextPath}/CatalogoServlet" class="btn btn-checkout">
                    Torna al Catalogo
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="/jsp/common/footer.jspf" %>