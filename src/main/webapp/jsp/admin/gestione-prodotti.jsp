<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Il tuo Carrello</h1>

    <%-- 1. BLOCCO FEEDBACK UTENTE (Flash Messages da CarrelloServlet) --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success" style="color: #065f46; background-color: #d1fae5; border: 1px solid #a7f3d0; padding: 10px; border-radius: 4px; margin-bottom: 15px;">
            <c:out value="${successMessage}" />
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger" style="color: #991b1b; background-color: #fee2e2; border: 1px solid #fecaca; padding: 10px; border-radius: 4px; margin-bottom: 15px;">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- 2. CONTROLLO PRESENZA PRODOTTI IN CARRELLO --%>
    <c:choose>
        <c:when test="${not empty prodottiCarrello}">
            <%-- Inizializziamo la variabile per il calcolo del totale complessivo --%>
            <c:set var="totaleCarrello" value="0" scope="page" />

            <table>
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
                    <%-- Iterazione sulla Map<ProdottoBean, Integer>: entry.key è il ProdottoBean, entry.value è la quantità --%>
                    <c:forEach var="entry" items="${prodottiCarrello}">
                        <c:set var="prodotto" value="${entry.key}" />
                        <c:set var="quantita" value="${entry.value}" />
                        <c:set var="subtotale" value="${prodotto.costo * quantita}" />
                        
                        <%-- Somma progressiva al totale --%>
                        <c:set var="totaleCarrello" value="${totaleCarrello + subtotale}" />

                        <tr>
                            <td>
                                <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${prodotto.idProdotto}">
                                    <c:out value="${prodotto.nome}" />
                                </a>
                            </td>
                            <td>
                                <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" />
                            </td>
                            <td>
                                <%-- Form POST per aggiornare la quantità del singolo prodotto --%>
                                <form method="post" action="${pageContext.request.contextPath}/CarrelloServlet" style="display:inline-flex; gap: 5px; align-items: center;">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="idProdotto" value="${prodotto.idProdotto}">
                                    <input type="number" name="quantita" value="${quantita}" min="1" max="${prodotto.quantita}" style="width: 65px; padding: 4px;">
                                    <button type="submit" class="btn-edit">Aggiorna</button>
                                </form>
                            </td>
                            <td>
                                <fmt:formatNumber value="${subtotale}" type="currency" currencySymbol="€" />
                            </td>
                            <td>
                                <%-- Form POST per rimuovere il prodotto dal carrello --%>
                                <form method="post" action="${pageContext.request.contextPath}/CarrelloServlet" style="display:inline;">
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

            <%-- Riquadro Totale e pulsante Checkout --%>
            <div style="margin-top: 25px; text-align: right; background-color: #f8fafc; padding: 20px; border-radius: 6px; border: 1px solid #e2e8f0;">
                <h2 style="margin-bottom: 15px;">
                    Totale Ordine: <fmt:formatNumber value="${totaleCarrello}" type="currency" currencySymbol="€" />
                </h2>
                <a href="${pageContext.request.contextPath}/CheckoutServlet" class="btn-add" style="padding: 12px 24px; font-size: 1.1em; text-decoration: none;">
                    Procedi al Checkout
                </a>
            </div>
        </c:when>

        <%-- Stato carrello vuoto --%>
        <c:otherwise>
            <div style="text-align: center; padding: 50px 20px;">
                <p style="font-size: 1.2em; color: #64748b; margin-bottom: 20px;">Il tuo carrello è attualmente vuoto.</p>
                <a href="${pageContext.request.contextPath}/CatalogoServlet" class="btn-add" style="text-decoration: none;">
                    Sfoglia il Catalogo
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="/jsp/common/footer.jspf" %>