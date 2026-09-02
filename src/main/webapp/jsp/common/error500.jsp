<%-- Pagina di gestione dell'errore HTTP 500 (Internal Server Error): configurata tramite isErrorPage="true" per intercettare ed esporre eccezioni non gestite dal server --%>
<%@ page isErrorPage="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Importazione della libreria JSTL Core per il controllo di flusso, cicli e condizioni --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Inclusione del frammento statico per l'intestazione HTML e le risorse della pagina (head, CSS, JS) --%>
<%@ include file="/jsp/common/header.jspf" %>

<%-- Inclusione del frammento statico per la barra di navigazione principale (menu / navbar) --%>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale della scheda di errore lato server --%>
<main class="container error-page">
    <div class="error-card">
        <%-- Codice identificativo dell'errore e messaggio principale --%>
        <h1 class="error-code">500</h1>
        <h2>Errore interno del server</h2>
        
        <%-- Messaggio di cortesia per l'utente finale --%>
        <p>Si &egrave; verificato un errore inaspettato. Stiamo gi&agrave; lavorando per risolverlo.</p>

        <%-- Mostra la traccia dell'eccezione sanitizzata tramite c:out per prevenire XSS --%>
        <c:if test="${not empty exception}">
            <div class="error-details">
                <strong>Dettagli tecnici (solo a scopo diagnostico):</strong>
                <%-- Blocco formattato e protetto con c:out per la visualizzazione sicura dell'eccezione --%>
                <pre style="white-space:pre-wrap; background:var(--bg-input); padding:15px; border-radius:6px; margin-top:10px; overflow:auto;"><c:out value="${exception}" /></pre>
            </div>
        </c:if>

        <%-- Azione di ripristino navigazione --%>
        <div class="error-actions">
            <%-- Link per ritornare in sicurezza al catalogo prodotti --%>
            <a class="btn" href="${pageContext.request.contextPath}/CatalogoServlet">Torna al Catalogo</a>
        </div>
    </div>
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>