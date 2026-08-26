<%-- 
    Pagina di conferma dell'ordine (Success Page).
    Visualizza il riepilogo sintetico dell'acquisto appena completato (ID ordine, totale,
    metodo di pagamento, indirizzo di consegna) e fornisce le azioni rapide per scaricare
    la fattura in PDF o tornare allo shopping. Gestisce anche lo stato d'errore.
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

<%-- Contenitore principale di layout della pagina di conferma o errore dell'ordine --%>
<main class="container">
    <div class="success-page-container">
        
        <%-- ── STRUTTURA CONDIZIONALE PRINCIPALE ────────────────────────────────── --%>
        <%-- Verifica la presenza dell'ID ordine in request scope per mostrare la conferma o l'errore --%>
        <c:choose>
            
            <%-- ── CASO 1: ORDINE ELABORATO CON SUCCESSO ────────────────────────────── --%>
            <c:when test="${not empty ordineId}">
                <div class="success-card">
                    
                    <%-- Cerchio grafico con spunta verde per confermare visivamente l'operazione --%>
                    <div class="success-icon-wrapper">
                        <span class="success-checkmark">✓</span>
                    </div>
                    
                    <%-- Titolo di conferma e messaggio di ringraziamento principale --%>
                    <h1>Ordine Confermato</h1>
                    <p class="success-subtitle">Grazie per il vostro acquisto! Il vostro ordine è stato elaborato con successo.</p>

                    <%-- Box contenente i dettagli sintetici del riepilogo dell'ordine appena registrato --%>
                    <div class="order-summary-box">
                        <h2>Riepilogo Ordine</h2>
                        
                        <%-- Codice identificativo univoco dell'ordine --%>
                        <div class="order-detail-row">
                            <span class="order-label">ID Ordine</span>
                            <span class="order-value">#<c:out value="${ordineId}"/></span>
                        </div>
                        
                        <%-- Importo totale pagato formattato in valuta Euro (€) --%>
                        <div class="order-detail-row">
                            <span class="order-label">Totale</span>
                            <span class="order-value order-price">
                                <fmt:formatNumber value="${totaleOrdine}" type="currency" currencySymbol="€" />
                            </span>
                        </div>
                        
                        <%-- Tipologia di pagamento utilizzata (es. Carta di Credito, Conto Bancario) --%>
                        <div class="order-detail-row">
                            <span class="order-label">Metodo di Pagamento</span>
                            <span class="order-value"><c:out value="${metodoPagamento}"/></span>
                        </div>
                        
                        <%-- Indirizzo di destinazione della spedizione inserito nel checkout --%>
                        <div class="order-detail-row">
                            <span class="order-label">Indirizzo di Consegna</span>
                            <span class="order-value"><c:out value="${indirizzoConsegna}"/></span>
                        </div>
                    </div>

                    <%-- Nota informativa per il tracciamento dell'ordine tramite l'area personale --%>
                    <p class="success-tracking-text">
                        Potete tracciare lo stato del vostro ordine nella sezione 
                        <a href="${pageContext.request.contextPath}/ProfiloServlet" class="order-link">Profilo</a>.
                    </p>

                    <%-- Pulsanti d'azione rapida per il download della fattura PDF o prosecuzione dello shopping --%>
                    <div class="success-actions">
                        <%-- Link alla FatturaServlet con ID ordine per la generazione/download del PDF --%>
                        <a href="${pageContext.request.contextPath}/FatturaServlet?id=${ordineId}" target="_blank" class="btn btn-md btn-primary">SCARICA FATTURA PDF</a>
                        
                        <%-- Link di reindirizzamento al catalogo prodotti per continuare la navigazione --%>
                        <a href="${pageContext.request.contextPath}/CatalogoServlet" class="btn btn-md btn-primary">CONTINUA LO SHOPPING</a>
                    </div>
                </div>
            </c:when>

            <%-- ── CASO 2: ERRORE / SESSIONE SCADUTA ────────────────────────────────── --%>
            <%-- Ramo eseguito se l'ID ordine è assente o in caso di accesso diretto non autorizzato --%>
            <c:otherwise>
                <div class="error-card">
                    <%-- Icona di avviso per segnalare l'impossibilità di recuperare l'ordine --%>
                    <div class="error-code">⚠️</div>
                    <h2>Errore</h2>
                    <p class="error-message-text">Dati dell'ordine non disponibili o sessione scaduta.</p>
                    
                    <%-- Pulsante di ritorno al catalogo principale --%>
                    <div class="error-actions">
                        <a href="${pageContext.request.contextPath}/CatalogoServlet" class="btn btn-md btn-primary">Torna al Catalogo</a>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>