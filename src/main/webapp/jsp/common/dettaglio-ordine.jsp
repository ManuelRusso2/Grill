<%-- 
    Pagina di visualizzazione dettagliata del singolo ordine/acquisto.
    Gestisce la vista Web interattiva per l'utente/admin e integra la struttura 
    CSS/HTML per la stampa diretta della fattura cartacea (vista print-only).
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Importazione della libreria JSTL Core per il controllo di flusso, cicli e condizioni --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Importazione della libreria JSTL Formatting per la formattazione di date, numeri e valute --%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Inclusione del frammento statico per l'intestazione HTML e le risorse della pagina (head, CSS, JS) --%>
<%@ include file="/jsp/common/header.jspf" %>

<%-- Inclusione del frammento statico per la barra di navigazione principale (menu / navbar) --%>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale di layout della pagina dettagli ordine --%>
<main class="container">
    <div class="order-detail-container">
        <div class="order-detail-card">
            
            <%-- ── STRUTTURA CONDIZIONALE PRINCIPALE ────────────────────────────────── --%>
            <%-- Controlla se l'oggetto 'acquisto' è stato correttamente popolato dalla Servlet --%>
            <c:choose>
                
                <%-- CASO 1: L'ordine esiste ed è stato caricato correttamente dal database --%>
                <c:when test="${not empty acquisto}">

                    <%-- ── INTESTAZIONE DEDICATA SOLO ALLA STAMPA (LAYOUT FATTURA CARTACEA) ── --%>
                    <%-- Blocco visibile unicamente durante la fase di stampa del browser (media print) --%>
                    <div class="print-only">
                        <h1 class="invoice-title-print">GRILL</h1>
                        <p class="invoice-sub-print">
                            <strong>FATTURA N. <c:out value="${acquisto.idAcquisto}"/></strong><br>
                            Data: <fmt:formatDate value="${acquisto.dataAcquisto}" pattern="dd/MM/yyyy HH:mm:ss" />
                        </p>
                        
                        <%-- Griglia bidirezionale per i dati aziendali del Venditore e i dati anagrafici del Cliente --%>
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

                    <%-- ── INTESTAZIONE SCHEDA ORDINE (SOLO VISTA WEB STANDARD) ──────────────── --%>
                    <div class="order-detail-header web-only">
                        <h1>Dettaglio Ordine</h1>
                        <span class="order-meta-value">#<c:out value="${acquisto.idAcquisto}"/></span>
                    </div>
                    
                    <%-- ── RIEPILOGO METADATI ORDINE (SOLO VISTA WEB STANDARD) ───────────────── --%>
                    <div class="order-meta-grid web-only">
                        <%-- Data e ora di effettuazione dell'ordine --%>
                        <div class="order-meta-item">
                            <span class="order-meta-label">Data Ordine</span>
                            <span class="order-meta-value">
                                <fmt:formatDate value="${acquisto.dataAcquisto}" pattern="dd/MM/yyyy HH:mm" />
                            </span>
                        </div>
                        
                        <%-- Importo totale speso con formattazione valuta Euro (€) --%>
                        <div class="order-meta-item">
                            <span class="order-meta-label">Totale Complessivo</span>
                            <span class="order-meta-value highlight-price">
                                <fmt:formatNumber value="${acquisto.prezzoTotale}" type="currency" currencySymbol="€"/>
                            </span>
                        </div>
                        
                        <%-- Indirizzo di spedizione memorizzato per l'acquisto --%>
                        <div class="order-meta-item">
                            <span class="order-meta-label">Indirizzo di Consegna</span>
                            <span class="order-meta-value"><c:out value="${acquisto.indirizzoConsegna}"/></span>
                        </div>
                    </div>

                    <h2 class="order-section-title web-only">Articoli Acquistati</h2>

                    <%-- ── TABELLA ELENCO ARTICOLI (CONDIVISA TRA VISTA WEB E STAMPA) ────────── --%>
                    <div class="cart-table-wrapper">
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
                                <%-- Verifica la presenza della lista dei singoli articoli acquistati --%>
                                <c:choose>
                                    <c:when test="${not empty dettagliOrdine}">
                                        <%-- Iterazione sul ciclo di prodotti appartenenti all'ordine --%>
                                        <c:forEach var="item" items="${dettagliOrdine}">
                                            <tr>
                                                <%-- Titolo del prodotto con fallback standard sul codice ID --%>
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
                                                
                                                <%-- Badge visivo per la taglia selezionata --%>
                                                <td>
                                                    <span class="order-size-badge">
                                                        <c:out value="${empty item.taglia ? 'Unica' : item.taglia}"/>
                                                    </span>
                                                </td>

                                                <%-- Prezzo singolo unitario al momento dell'acquisto --%>
                                                <td>
                                                    <fmt:formatNumber value="${item.prezzoUnitario}" type="currency" currencySymbol="€"/>
                                                </td>
                                                
                                                <%-- Aliquota IVA applicata --%>
                                                <td><c:out value="${item.iva}"/>%</td>
                                                
                                                <%-- Quantità di pezzi ordinati --%>
                                                <td><c:out value="${item.quantitaAcquistata}"/></td>
                                                
                                                <%-- Stato di avanzamento della spedizione dell'articolo (solo vista web) --%>
                                                <td class="web-only">
                                                    <span class="order-value"><c:out value="${item.statoSpedizione}"/></span>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    
                                    <%-- Messaggio di fallback nel caso la lista dettagli risulti vuota --%>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="6" class="empty-table-msg">Nessun articolo trovato per questo ordine.</td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <%-- ── PIÈ DI PAGINA FATTURA (DEDICATO SOLO ALLA STAMPA) ───────── --%>
                    <div class="print-only invoice-footer-print">
                        <p class="invoice-total-line">
                            Totale Complessivo: <fmt:formatNumber value="${acquisto.prezzoTotale}" type="currency" currencySymbol="€"/>
                        </p>
                        <p class="invoice-thanks-print">
                            Grazie per il vostro acquisto!<br>
                            <small>2026 Grill. Tutti i diritti riservati.</small>
                        </p>
                    </div>

                    <%-- ── AZIONI E PULSANTI DI NAVIGAZIONE (SOLO VISTA WEB) ────────── --%>
                    <div class="order-actions-footer web-only">
                        <p class="success-tracking-text success-tracking-spacing">
                            Puoi consultare tutti i tuoi acquisti nella sezione dedicata.
                        </p>
                        <div class="order-actions-group">
                            <%-- Trigger per l'avvio della funzione di stampa nativa del browser --%>
                            <button type="button" onclick="window.print()" class="btn btn-md btn-primary">STAMPA FATTURA</button>
                            
                            <%-- Generazione e download diretto della fattura in formato PDF --%>
                            <a href="${pageContext.request.contextPath}/FatturaServlet?id=${acquisto.idAcquisto}" target="_blank" class="btn btn-md btn-primary">SCARICA PDF</a>
                            
                            <%-- Reindirizzamento dinamico differenziato tra Amministratore e Cliente --%>
                            <c:choose>
                                <c:when test="${not empty sessionScope.utente && sessionScope.utente.admin}">
                                    <a href="${pageContext.request.contextPath}/AdminOrdiniServlet" class="btn btn-md btn-primary">TORNA A GESTIONE ORDINI</a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/ProfiloServlet" class="btn btn-md btn-primary">TORNA AL PROFILO</a>
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
                            <c:choose>
                                <c:when test="${not empty sessionScope.utente && sessionScope.utente.admin}">
                                    <a href="${pageContext.request.contextPath}/AdminOrdiniServlet" class="btn btn-md btn-primary">Torna a Gestione Ordini</a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/ProfiloServlet" class="btn btn-md btn-primary">Torna al Profilo</a>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>
    </div>
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>