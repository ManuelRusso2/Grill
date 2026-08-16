<%-- Impostazione del tipo di contenuto della pagina e della codifica dei caratteri (UTF-8) --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Inclusione delle librerie di tag JSTL per la logica condizionale e la formattazione di numeri e valute --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Inclusione dei frammenti di codice statici per l'intestazione (header) e la barra di navigazione (menu) --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale della pagina di conferma o errore dell'ordine --%>
<main class="container">
    <div class="success-page-container">
        
        <%-- ── STRUTTURA CONDIZIONALE PRINCIPALE ────────────────────────────────── --%>
        <%-- Verifica la presenza dell'ID ordine per mostrare la conferma o la schermata di errore --%>
        <c:choose>
            
            <%-- ── CASO 1: ORDINE ELABORATO CON SUCCESSO ────────────────────────────── --%>
            <c:when test="${not empty ordineId}">
                <div class="success-card">
                    
                    <%-- Icona grafica di spunta per confermare visivamente il buon esito dell'operazione --%>
                    <div class="success-icon-wrapper">
                        <span class="success-checkmark">✓</span>
                    </div>
                    
                    <%-- Intestazione e messaggio di ringraziamento principale --%>
                    <h1>Ordine Confermato</h1>
                    <p class="success-subtitle">Grazie per il vostro acquisto! Il vostro ordine è stato elaborato con successo.</p>

                    <%-- Box contenente i dettagli sintetici dell'ordine appena completato --%>
                    <div class="order-summary-box">
                        <h2>Riepilogo Ordine</h2>
                        
                        <%-- Codice identificativo univoco dell'ordine generato dal sistema --%>
                        <div class="order-detail-row">
                            <span class="order-label">ID Ordine</span>
                            <span class="order-value">#<c:out value="${ordineId}"/></span>
                        </div>
                        
                        <%-- Importo totale pagato formattato con simbolo di valuta Euro (€) e due decimali --%>
                        <div class="order-detail-row">
                            <span class="order-label">Totale</span>
                            <span class="order-value order-price">
                                €<fmt:formatNumber value="${totaleOrdine}" pattern="#,##0.00" />
                            </span>
                        </div>
                        
                        <%-- Tipologia di pagamento utilizzata (es. Carta di Credito, Conto Bancario) --%>
                        <div class="order-detail-row">
                            <span class="order-label">Metodo di Pagamento</span>
                            <span class="order-value"><c:out value="${metodoPagamento}"/></span>
                        </div>
                        
                        <%-- Indirizzo di spedizione inserito in fase di checkout --%>
                        <div class="order-detail-row">
                            <span class="order-label">Indirizzo di Consegna</span>
                            <span class="order-value"><c:out value="${indirizzoConsegna}"/></span>
                        </div>
                    </div>

                    <%-- Testo informativo per il tracciamento dell'ordine tramite la pagina profilo dell'utente --%>
                    <p class="success-tracking-text">
                        Potete tracciare lo stato del vostro ordine nella sezione 
                        <a href="${pageContext.request.contextPath}/ProfiloServlet" class="order-link">Profilo</a>.
                    </p>

                    <%-- Pulsanti di azione rapida per il download della fattura in PDF o per proseguire la navigazione --%>
                    <div class="success-actions">
                        <%-- Link che richiama la FatturaServlet passando l'ID dell'ordine per la generazione del PDF --%>
                        <a href="${pageContext.request.contextPath}/FatturaServlet?id=${ordineId}" target="_blank" class="btn btn-catalog">SCARICA FATTURA PDF</a>
                        
                        <%-- Link di reindirizzamento al catalogo prodotti per continuare lo shopping --%>
                        <a href="${pageContext.request.contextPath}/CatalogoServlet" class="btn btn-catalog">CONTINUA LO SHOPPING</a>
                    </div>
                </div>
            </c:when>

            <%-- ── CASO 2: ERRORE / SESSIONE SCADUTA ────────────────────────────────── --%>
            <%-- Ramo mostrato quando l'ID dell'ordine non è presente nel Request Scope --%>
            <c:otherwise>
                <div class="error-card">
                    <%-- Icona di avviso per segnalare l'impossibilità di recuperare le informazioni --%>
                    <div class="error-code">⚠️</div>
                    <h2>Errore</h2>
                    <p class="error-message-text">Dati dell'ordine non disponibili o sessione scaduta.</p>
                    
                    <%-- Pulsante per ritornare alla pagina principale del catalogo prodotti --%>
                    <div class="error-actions">
                        <a href="${pageContext.request.contextPath}/CatalogoServlet" class="btn btn-catalog">Torna al Catalogo</a>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>