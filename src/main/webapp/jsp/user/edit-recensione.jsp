<%-- 
    Pagina di modifica per una recensione precedentemente pubblicata.
    Consente all'utente di aggiornare la propria valutazione numerica (da 1 a 5 stelle)
    e il testo descrittivo del commento, oppure di annullare l'operazione ritornando al profilo.
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

<%-- Contenitore principale di layout per la modifica recensione --%>
<main class="container">
    <h1>Modifica Recensione</h1>

    <%-- ── MESSAGGI DI ERRORE / FEEDBACK LATO SERVER ────────────────────────── --%>
    <%-- Mostra una notifica visiva se la Servlet ha riscontrato anomalie nella validazione --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            ✗ <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- ── STRUTTURA CONDIZIONALE PRINCIPALE ────────────────────────────────── --%>
    <c:choose>
        <%-- Ramo 1: L'oggetto 'recensione' è presente nel Request Scope --%>
        <c:when test="${not empty recensione}">
            <div class="review-edit-card">
                
                <%-- Intestazione con il nome del prodotto associato alla recensione --%>
                <c:if test="${not empty recensione.nomeProdotto}">
                    <p class="review-product-info">
                        Stai modificando la recensione per: <strong><c:out value="${recensione.nomeProdotto}" /></strong>
                    </p>
                </c:if>

                <%-- Form inviato via POST alla ModificaRecensioneServlet passandole l'ID recensione --%>
                <form action="${pageContext.request.contextPath}/ModificaRecensioneServlet?idRecensione=${recensione.idRecensione}" method="post" class="form-edit-review">
                
                    <%-- Campo per la selezione della nuova valutazione a stelle --%>
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
                
                    <%-- Campo area di testo per la modifica del commento --%>
                    <div class="form-group">
                        <label for="descrizione">Descrizione della recensione:</label>
                        <textarea name="descrizione" id="descrizione" rows="6" placeholder="Scrivi qui la tua recensione..." required class="form-control"><c:out value="${recensione.descrizione}" /></textarea>
                    </div>
                
                    <%-- Pulsanti d'azione affiancati --%>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-md btn-primary">Salva modifiche</button>
                        <a href="${pageContext.request.contextPath}/ProfiloServlet" class="btn btn-md btn-outline-purple">Annulla</a>
                    </div>
                </form>
            </div>
        </c:when>

        <%-- Ramo 2: Nessuna recensione trovata (es. ID non valido o recensione inesistente) --%>
        <c:otherwise>
            <div class="alert alert-danger">
                ✗ Nessuna recensione trovata da modificare. 
                <a href="${pageContext.request.contextPath}/ProfiloServlet">Torna al Profilo</a>.
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>