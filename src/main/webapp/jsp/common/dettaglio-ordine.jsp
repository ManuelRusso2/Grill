<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 1. Importiamo le taglib JSTL per la logica di controllo e la formattazione --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Dettaglio Ordine</h1>

    <%-- 2. Struttura condizionale pulita per verificare l'esistenza dell'acquisto --%>
    <c:choose>
        <c:when test="${empty acquisto}">
            <p class="alert-error">Ordine non trovato.</p>
        </c:when>
        
        <c:otherwise>
            <%-- Dati generali della testata dell'ordine --%>
            <div class="ordine-info">
                <p><strong>ID ordine:</strong> <c:out value="${acquisto.idAcquisto}" /></p>
                <p><strong>Data:</strong> <fmt:formatDate value="${acquisto.dataAcquisto}" pattern="dd/MM/yyyy HH:mm" /></p>
                <p><strong>Totale:</strong> <fmt:formatNumber value="${acquisto.prezzoTotale}" type="currency" currencySymbol="€" /></p>
                <p><strong>Indirizzo di consegna:</strong> <c:out value="${acquisto.indirizzoConsegna}" /></p>
            </div>

            <h2>Articoli</h2>
            <table>
                <thead>
                    <tr>
                        <th>Prodotto</th>
                        <th>Prezzo unitario</th>
                        <th>IVA</th>
                        <th>Quantità</th>
                        <th>Stato</th>
                    </tr>
                </thead>
                <tbody>
                    <%-- 3. Controllo e ciclo sulle righe di dettaglio dell'ordine --%>
                    <c:if test="${not empty dettagliOrdine}">
                        <c:forEach var="item" items="${dettagliOrdine}">
                            <tr>
                                <td>
                                    <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${item.idProdotto}">
                                        Codice: <c:out value="${item.idProdotto}" />
                                    </a>
                                </td>
                                <td>
                                    <fmt:formatNumber value="${item.prezzoUnitario}" type="currency" currencySymbol="€" />
                                </td>
                                <td>
                                    <fmt:formatNumber value="${item.iva}" pattern="#,##0.##"/> %
                                </td>
                                <td><c:out value="${item.quantitaAcquistata}" /></td>
                                <td>
                                    <span class="stato-spedizione"><c:out value="${item.statoSpedizione}" /></span>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:if>
                    
                    <%-- Gestione del caso limite in cui non ci siano righe associate --%>
                    <c:if test="${empty dettagliOrdine}">
                        <tr>
                            <td colspan="5" style="text-align: center; font-style: italic;">
                                Nessun articolo presente in questo ordine.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="/jsp/common/footer.jspf" %>