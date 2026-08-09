<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <div class="order-detail-container">
        <div class="order-detail-card">
            
            <div class="order-detail-header">
                <h1>Dettaglio Ordine</h1>
                <span class="order-meta-value">#<c:out value="${acquisto.idAcquisto}"/></span>
            </div>

            <c:choose>
                <c:when test="${not empty acquisto}">
                    <!-- Griglia Riepilogo Informazioni Ordine -->
                    <div class="order-meta-grid">
                        <div class="order-meta-item">
                            <span class="order-meta-label">Data Ordine</span>
                            <span class="order-meta-value"><c:out value="${acquisto.dataAcquisto}"/></span>
                        </div>
                        <div class="order-meta-item">
                            <span class="order-meta-label">Totale Complessivo</span>
                            <span class="order-meta-value highlight-price">
                                <fmt:formatNumber value="${acquisto.prezzoTotale}" type="currency" currencySymbol="€"/>
                            </span>
                        </div>
                        <div class="order-meta-item">
                            <span class="order-meta-label">Indirizzo di Consegna</span>
                            <span class="order-meta-value"><c:out value="${acquisto.indirizzoConsegna}"/></span>
                        </div>
                    </div>

                    <h2 class="order-section-title">Articoli Acquistati</h2>

                    <!-- Tabella Prodotti -->
                    <table class="cart-table">
                        <thead>
                            <tr>
                                <th>ID Prodotto</th>
                                <th>Prezzo Unitario</th>
                                <th>IVA</th>
                                <th>Quantità</th>
                                <th>Stato Spedizione</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty dettagliOrdine}">
                                    <c:forEach var="item" items="${dettagliOrdine}">
                                        <tr>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${item.idProdotto}" class="cart-product-title">
                                                    Prodotto #<c:out value="${item.idProdotto}"/>
                                                </a>
                                            </td>
                                            <td><fmt:formatNumber value="${item.prezzoUnitario}" type="currency" currencySymbol="€"/></td>
                                            <td><c:out value="${item.iva}"/>%</td>
                                            <td><c:out value="${item.quantitaAcquistata}"/></td>
                                            <td><span class="order-value"><c:out value="${item.statoSpedizione}"/></span></td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="5" class="empty-table-msg">Nessun articolo trovato per questo ordine.</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>

                    <div class="order-actions-footer">
                        <p class="success-tracking-text success-tracking-spacing">
                            Puoi consultare tutti i tuoi acquisti nella sezione dedicata.
                        </p>
                        <a href="${pageContext.request.contextPath}/ProfiloServlet" class="btn btn-catalog">Torna al Profilo</a>
                    </div>

                </c:when>
                <c:otherwise>
                    <div class="error-card error-card-inline">
                        <h2>Errore</h2>
                        <p class="error-message-text">Impossibile caricare i dettagli dell'ordine selezionato.</p>
                        <div class="error-actions">
                            <a href="${pageContext.request.contextPath}/ProfiloServlet" class="btn btn-catalog">Torna al Profilo</a>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>
    </div>
</main>

<%@ include file="/jsp/common/footer.jspf" %>