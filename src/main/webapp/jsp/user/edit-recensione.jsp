<%-- 
    Pagina per la modifica di una recensione esistente.
    Consente all'utente di aggiornare il punteggio (stelle) e il testo descrittivo.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Inclusione delle librerie di tag JSTL per la logica di controllo e la formattazione --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Inclusione dei frammenti statici per l'intestazione e il menu --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Modifica Recensione</h1>

    <%-- ── MESSAGGI DI ERRORE / FEEDBACK ──────────────────────────────────── --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- ── STRUTTURA CONDIZIONALE PRINCIPALE ────────────────────────────────── --%>
    <c:choose>
        <%-- Ramo 1: L'oggetto recensione esiste, viene mostrato il modulo di modifica --%>
        <c:when test="${not empty recensione}">
            <div class="review-edit-card">
                
                <%-- Visualizzazione del nome del prodotto se presente nel Bean --%>
                <c:if test="${not empty recensione.nomeProdotto}">
                    <p class="review-product-info">
                        Stai modificando la recensione per: <strong><c:out value="${recensione.nomeProdotto}" /></strong>
                    </p>
                </c:if>

                <%-- Form inviato via POST a ModificaRecensioneServlet --%>
				<form action="${pageContext.request.contextPath}/ModificaRecensioneServlet?idRecensione=${recensione.idRecensione}" method="post" class="form-edit-review">
				
				    <%-- Sezione per la selezione della valutazione in stelle (da 1 a 5) --%>
				    <div class="form-group">
				        <label for="valutazione">Valutazione:</label>
				        <select name="valutazione" id="valutazione" required class="form-control">
				            <c:forEach begin="1" end="5" var="i">
				                <option value="${i}" ${i == recensione.valutazione ? 'selected' : ''}>
				                    ${i} ★ 
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
				
				    <%-- Sezione per la modifica del testo descrittivo --%>
				    <div class="form-group">
				        <label for="descrizione">Descrizione della recensione:</label>
				        <textarea name="descrizione" id="descrizione" rows="6" placeholder="Scrivi qui la tua recensione..." required class="form-control"><c:out value="${recensione.descrizione}" /></textarea>
				    </div>
				
				    <%-- Pulsanti di azione --%>
				    <div class="form-actions">
				        <button type="submit" class="btn btn-submit">Salva modifiche</button>
				        <a href="${pageContext.request.contextPath}/ProfiloServlet" class="btn btn-cancel">Annulla</a>
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

<%-- Inclusione del piè di pagina --%>
<%@ include file="/jsp/common/footer.jspf" %>