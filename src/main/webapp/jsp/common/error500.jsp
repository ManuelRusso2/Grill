<%-- Pagina di gestione dell'errore HTTP 500 (Internal Server Error): configurata tramite isErrorPage="true" per intercettare ed esporre eccezioni non gestite dal server --%>
<%@ page isErrorPage="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Inclusione della libreria di tag JSTL Core per il controllo condizionale --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Inclusione dei frammenti statici per l'intestazione (header) e il menu di navigazione --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale della scheda di errore lato server --%>
<main class="container error-page">
    <div class="error-card">
        <%-- Codice identificativo dell'errore e messaggio principale --%>
        <h1 class="error-code">500</h1>
        <h2>Errore interno del server</h2>
        
        <%-- Messaggio di cortesia per l'utente finale --%>
        <p>Si &egrave; verificato un errore inaspettato. Stiamo gi&agrave; lavorando per risolverlo.</p>

        <%-- Mostra la traccia dell'eccezione solo se presente (resa disponibile dall'attributo isErrorPage="true") --%>
        <c:if test="${not empty exception}">
            <div class="error-details">
                <strong>Dettagli tecnici (solo a scopo diagnostico):</strong>
                <%-- Blocco formattato per la visualizzazione leggibile del tracciato dello stack (stacktrace) --%>
                <pre style="white-space:pre-wrap; background:var(--bg-input); padding:15px; border-radius:6px; margin-top:10px; overflow:auto;">${exception}</pre>
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