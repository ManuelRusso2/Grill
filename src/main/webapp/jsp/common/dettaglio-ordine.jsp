<%-- 
    Pagina di visualizzazione dettagliata del singolo ordine/acquisto.
    Mostra i dati riepilogativi dell'ordine (ID, data, totale, indirizzo di spedizione),
    la tabella degli articoli acquistati con prezzi, IVA, quantità e stato di spedizione,
    e fornisce azioni come il download della fattura PDF e il ritorno al profilo o alla gestione ordini.
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
            
            <%-- ── INTESTAZIONE SCHEDA ORDINE ──────────────────────────────────── --%>
            <div class="order-detail-header">
                <h1>Dettaglio Ordine</h1>
                <%-- Mostra l'ID univoco dell'acquisto se l'oggetto è presente --%>
                <c:if test="${not empty acquisto}">
                    <span class="order-meta-value">#<c:out value="${acquisto.idAcquisto}"/></span>
                </c:if>
            </div>

            <c:choose>
                <%-- CASO 1: L'ordine è stato caricato correttamente --%>
                <c:when test="${not empty acquisto}">
                    
                    <%-- ── RIEPILOGO METADATI ORDINE ───────────────────────────── --%>
                    <div class="order-meta-grid">
                        <%-- Data di effettuazione dell'ordine formattata --%>
                        <div class="order-meta-item">
                            <span class="order-meta-label">Data Ordine</span>
                            <span class="order-meta-value">
                                <fmt:formatDate value="${acquisto.dataAcquisto}" pattern="dd/MM/yyyy HH:mm" />
                            </span>
                        </div>
                        
                        <%-- Prezzo totale complessivo formattato in Euro (€) --%>
                        <div class="order-meta-item">
                            <span class="order-meta-label">Totale Complessivo</span>
                            <span class="order-meta-value highlight-price">
                                <fmt:formatNumber value="${acquisto.prezzoTotale}" type="currency" currencySymbol="€"/>
                            </span>
                        </div>
                        
                        <%-- Indirizzo di destinazione per la spedizione --%>
                        <div class="order-meta-item">
                            <span class="order-meta-label">Indirizzo di Consegna</span>
                            <span class="order-meta-value"><c:out value="${acquisto.indirizzoConsegna}"/></span>
                        </div>
                    </div>

                    <h2 class="order-section-title">Articoli Acquistati</h2>

                    <%-- ── TABELLA ELENCO ARTICOLI ─────────────────────────────── --%>
                    <table class="cart-table">
                        <thead>
                            <tr>
                                <th>Prodotto</th>
                                <th>Taglia</th>
                                <th>Prezzo Unitario</th>
                                <th>IVA</th>
                                <th>Quantità</th>
                                <th>Stato Spedizione</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <%-- Se la lista dei dettagli articolo contiene elementi --%>
                                <c:when test="${not empty dettagliOrdine}">
                                    <c:forEach var="item" items="${dettagliOrdine}">
                                        <tr>
                                            <%-- Link al dettaglio prodotto con nome o ID dinamico --%>
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
                                            
                                            <%-- Taglia dell'articolo (con valore di fallback 'Unica') --%>
                                            <td>
                                                <span class="order-size-badge">
                                                    <c:out value="${empty item.taglia ? 'Unica' : item.taglia}"/>
                                                </span>
                                            </td>

                                            <%-- Prezzo singolo articolo formattato in Euro --%>
                                            <td>
                                                <fmt:formatNumber value="${item.prezzoUnitario}" type="currency" currencySymbol="€"/>
                                            </td>
                                            
                                            <%-- Percentuale IVA applicata --%>
                                            <td><c:out value="${item.iva}"/>%</td>
                                            
                                            <%-- Unità acquistate --%>
                                            <td><c:out value="${item.quantitaAcquistata}"/></td>
                                            
                                            <%-- Stato di avanzamento/spedizione del singolo articolo --%>
                                            <td>
                                                <span class="order-value"><c:out value="${item.statoSpedizione}"/></span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                
                                <%-- Messaggio in caso di assenza di righe dettaglio --%>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="6" class="empty-table-msg">Nessun articolo trovato per questo ordine.</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>

                    <%-- ── AZIONI E PULSANTI DI NAVIGAZIONE ────────────────────── --%>
                    <div class="order-actions-footer">
                        <p class="success-tracking-text success-tracking-spacing">
                            Puoi consultare tutti i tuoi acquisti nella sezione dedicata.
                        </p>
                        <div class="order-actions-group">
                            <%-- Generazione e Download PDF della fattura --%>
                            <a href="${pageContext.request.contextPath}/FatturaServlet?id=${acquisto.idAcquisto}" target="_blank" class="btn btn-catalog">SCARICA FATTURA PDF</a>
                            
                            <%-- Pulsante di ritorno differenziato in base al ruolo dell'utente (Admin vs Utente Standard) --%>
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
                
                <%-- CASO 2: L'oggetto 'acquisto' è nullo o si è verificato un errore di caricamento --%>
                <c:otherwise>
                    <div class="error-card error-card-inline">
                        <h2>Errore</h2>
                        <p class="error-message-text">Impossibile caricare i dettagli dell'ordine selezionato.</p>
                        <div class="error-actions">
                            <%-- Pulsante di reindirizzamento in base al ruolo in caso di errore --%>
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