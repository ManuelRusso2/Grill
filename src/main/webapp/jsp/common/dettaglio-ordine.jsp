<%-- 
    Pagina di visualizzazione dettagliata del singolo ordine/acquisto.
    Include la vista web standard e la struttura per la stampa della fattura.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Tag Library JSTL per il controllo di flusso e la formattazione dei dati --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Inclusione dei frammenti statici di Header e Barra di Navigazione --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <div class="order-detail-container">
        <div class="order-detail-card">
            
            <c:choose>
                <%-- CASO 1: L'ordine è stato caricato correttamente --%>
                <c:when test="${not empty acquisto}">

                    <%-- ── INTESTAZIONE DEDICATA SOLO ALLA STAMPA (LAYOUT FATTURA) ── --%>
                    <div class="print-only">
                        <h1 class="invoice-title-print">GRILL</h1>
                        <p class="invoice-sub-print">
                            <strong>FATTURA N. <c:out value="${acquisto.idAcquisto}"/></strong><br>
                            Data: <fmt:formatDate value="${acquisto.dataAcquisto}" pattern="dd/MM/yyyy HH:mm:ss" />
                        </p>
                        
                        <div class="invoice-grid-print">
                            <div>
                                <strong>Venditore:</strong><br>
                                Grill Store<br>
                                Via Roma 10, Salerno<br>
                                P.IVA: 01234567890
                            </div>
                            <div>
                                <strong>Cliente:</strong><br>
                                <c:out value="${sessionScope.utente.nome}"/> <c:out value="${sessionScope.utente.cognome}"/><br>
                                Email: <c:out value="${sessionScope.utente.email}"/><br>
                                Indirizzo Consegna: <c:out value="${acquisto.indirizzoConsegna}"/>
                            </div>
                        </div>
                        <hr class="invoice-hr-print">
                    </div>

                    <%-- ── INTESTAZIONE SCHEDA ORDINE (SOLO VISTA WEB) ──────────────── --%>
                    <div class="order-detail-header web-only">
                        <h1>Dettaglio Ordine</h1>
                        <span class="order-meta-value">#<c:out value="${acquisto.idAcquisto}"/></span>
                    </div>
                    
                    <%-- ── RIEPILOGO METADATI ORDINE (SOLO VISTA WEB) ───────────────── --%>
                    <div class="order-meta-grid web-only">
                        <div class="order-meta-item">
                            <span class="order-meta-label">Data Ordine</span>
                            <span class="order-meta-value">
                                <fmt:formatDate value="${acquisto.dataAcquisto}" pattern="dd/MM/yyyy HH:mm" />
                            </span>
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

                    <h2 class="order-section-title web-only">Articoli Acquistati</h2>

                    <%-- ── TABELLA ELENCO ARTICOLI (CONDIVISA WEB / STAMPA) ────────── --%>
                    <table class="cart-table">
                        <thead>
                            <tr>
                                <th>Prodotto</th>
                                <th>Taglia</th>
                                <th>Prezzo Unitario</th>
                                <th>IVA</th>
                                <th>Quantità</th>
                                <th class="web-only">Stato Spedizione</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty dettagliOrdine}">
                                    <c:forEach var="item" items="${dettagliOrdine}">
                                        <tr>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${item.idProdotto}" class="cart-product-title">
                                                    <c:choose>
                                                        <c:when test="${not empty item.nomeProdotto}">
                                                            <c:out value="${item.nomeProdotto}"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            Prodotto #<c:out value="${item.idProdotto}"/>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </a>
                                            </td>
                                            
                                            <td>
                                                <span class="order-size-badge">
                                                    <c:out value="${empty item.taglia ? 'Unica' : item.taglia}"/>
                                                </span>
                                            </td>

                                            <td>
                                                <fmt:formatNumber value="${item.prezzoUnitario}" type="currency" currencySymbol="€"/>
                                            </td>
                                            
                                            <td><c:out value="${item.iva}"/>%</td>
                                            
                                            <td><c:out value="${item.quantitaAcquistata}"/></td>
                                            
                                            <td class="web-only">
                                                <span class="order-value"><c:out value="${item.statoSpedizione}"/></span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                
                                <c:otherwise>
                                    <tr>
                                        <td colspan="6" class="empty-table-msg">Nessun articolo trovato per questo ordine.</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>

                    <%-- ── PIÈ DI PAGINA FATTURA (DEDICATO SOLO ALLA STAMPA) ───────── --%>
                    <div class="print-only invoice-footer-print">
                        <p class="invoice-total-line">
                            Totale Complessivo: <fmt:formatNumber value="${acquisto.prezzoTotale}" type="currency" currencySymbol="€"/>
                        </p>
                        <p class="invoice-thanks-print">
                            Grazie per il vostro acquisto!<br>
                            <small>Grill - Progetto Java EE</small>
                        </p>
                    </div>

                    <%-- ── AZIONI E PULSANTI DI NAVIGAZIONE (SOLO VISTA WEB) ────────── --%>
                    <div class="order-actions-footer web-only">
                        <p class="success-tracking-text success-tracking-spacing">
                            Puoi consultare tutti i tuoi acquisti nella sezione dedicata.
                        </p>
                        <div class="order-actions-group">
                            <button type="button" onclick="window.print()" class="btn btn-catalog">STAMPA FATTURA</button>
                            <a href="${pageContext.request.contextPath}/FatturaServlet?id=${acquisto.idAcquisto}" target="_blank" class="btn btn-catalog">SCARICA PDF</a>
                            
                            <c:choose>
                                <c:when test="${not empty sessionScope.utente && sessionScope.utente.admin}">
                                    <a href="${pageContext.request.contextPath}/AdminOrdiniServlet" class="btn btn-catalog">TORNA A GESTIONE ORDINI</a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/ProfiloServlet" class="btn btn-catalog">TORNA AL PROFILO</a>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                </c:when>
                
                <%-- CASO 2: L'oggetto 'acquisto' è nullo o si è verificato un errore --%>
                <c:otherwise>
                    <div class="error-card error-card-inline">
                        <h2>Errore</h2>
                        <p class="error-message-text">Impossibile caricare i dettagli dell'ordine selezionato.</p>
                        <div class="error-actions">
                            <c:choose>
                                <c:when test="${not empty sessionScope.utente && sessionScope.utente.admin}">
                                    <a href="${pageContext.request.contextPath}/AdminOrdiniServlet" class="btn btn-catalog">Torna a Gestione Ordini</a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/ProfiloServlet" class="btn btn-catalog">Torna al Profilo</a>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>
    </div>
</main>

<%-- Inclusione del piè di pagina --%>
<%@ include file="/jsp/common/footer.jspf" %>