<%-- Impostazione del tipo di contenuto della pagina e della codifica dei caratteri (UTF-8) --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Inclusione delle librerie di tag JSTL per la logica di controllo e la formattazione di numeri/date --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Inclusione dei frammenti di codice statici per l'intestazione (header) e la barra di navigazione (menu) --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale della pagina per la modifica della recensione --%>
<main class="container">
    <h1>Modifica Recensione</h1>

    <%-- ── MESSAGGI DI ERRORE / FEEDBACK ──────────────────────────────────── --%>
    <%-- Mostra un alert di errore se presente un messaggio impostato dalla Servlet nel Request Scope --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- ── STRUTTURA CONDIZIONALE PRINCIPALE ────────────────────────────────── --%>
    <%-- Verifica se l'oggetto 'recensione' da modificare è presente ed è valido nel contesto --%>
    <c:choose>
        <%-- Ramo 1: L'oggetto recensione esiste, viene mostrato il modulo di modifica --%>
        <c:when test="${not empty recensione}">
            <div class="review-edit-card">
                
                <%-- Form inviato via POST al servlet ModificaRecensioneServlet per elaborare l'aggiornamento --%>
                <form action="${pageContext.request.contextPath}/ModificaRecensioneServlet" method="post" class="form-edit-review">
                    
                    <%-- Campo nascosto per trasmettere l'ID univoco della recensione da aggiornare nel database --%>
                    <input type="hidden" name="idRecensione" value="<c:out value='${recensione.idRecensione}'/>" />

                    <%-- Sezione per la selezione della valutazione in stelle (da 1 a 5) --%>
                    <div class="form-group">
                        <label for="valutazione">Valutazione</label>
                        <select name="valutazione" id="valutazione">
                            <%-- Ciclo JSTL per generare dinamicamente le opzioni da 1 a 5 stelle --%>
                            <c:forEach begin="1" end="5" var="i">
                                <%-- Imposta l'attributo 'selected' se il valore corrisponde alla valutazione attuale --%>
                                <option value="${i}" ${i == recensione.valutazione ? 'selected' : ''}>
                                    ${i} ★ 
                                    <%-- Etichetta descrittiva associata al punteggio numerico --%>
                                    <c:choose>
                                        <c:when test="${i == 1}">(Pessimo)</c:when>
                                        <c:when test="${i == 2}">(Scarso)</c:when>
                                        <c:when test="${i == 3}">(Sufficiente)</c:when>
                                        <c:when test="${i == 4}">(Buono)</c:when>
                                        <c:when test="${i == 5}">(Eccellente)</c:when>
                                    </c:choose>
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <%-- Sezione per la modifica del testo descrittivo della recensione --%>
                    <div class="form-group">
                        <label for="descrizione">Descrizione della recensione</label>
                        <%-- Pre-popolazione dell'area di testo con la descrizione corrente usando c:out per prevenire attacchi XSS --%>
                        <textarea name="descrizione" id="descrizione" rows="6" placeholder="Scrivi qui la tua recensione..." required><c:out value="${recensione.descrizione}" /></textarea>
                    </div>

                    <%-- Pulsanti di azione per il submit o l'annullamento dell'operazione --%>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">Salva modifiche</button>
                        <%-- Link per annullare le modifiche e ritornare alla pagina del profilo utente --%>
                        <a href="${pageContext.request.contextPath}/ProfiloServlet" class="btn btn-secondary">Annulla</a>
                    </div>
                </form>
            </div>
        </c:when>

        <%-- Ramo 2: Nessuna recensione trovata --%>
        <c:otherwise>
            <div class="alert alert-warning">
                Nessuna recensione trovata da modificare. 
                <a href="${pageContext.request.contextPath}/ProfiloServlet">Torna al Profilo</a>.
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>