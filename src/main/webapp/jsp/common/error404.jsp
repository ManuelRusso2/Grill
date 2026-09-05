<%-- Pagina di errore HTTP 404 (Pagina Non Trovata): mostrata quando l'URL richiesto non esiste o la risorsa è stata rimossa --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Importazione della libreria JSTL Core per il controllo di flusso, cicli e condizioni --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Inclusione del frammento statico per l'intestazione HTML e le risorse della pagina (head, CSS, JS) --%>
<%@ include file="/jsp/common/header.jspf" %>

<%-- Inclusione del frammento statico per la barra di navigazione principale (menu / navbar) --%>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale della scheda d'errore 404 --%>
<main class="container error-page">
    <div class="error-card">
        <%-- Codice identificativo dell'errore e titolo della pagina --%>
        <h1 class="error-code">404</h1>
        <h2>Pagina non trovata</h2>

        <%-- Messaggi esplicativi e indicazioni di navigazione per l'utente --%>
        <p>La risorsa richiesta non esiste o &egrave; stata rimossa.</p>
        <p>Controlla l'URL per continuare la navigazione.</p>

        <%-- Azione di ripristino navigazione --%>
        <div class="error-actions">
            <%-- Link per tornare alla pagina principale del catalogo prodotti --%>
            <a class="btn btn-primary btn-md" href="${pageContext.request.contextPath}/CatalogoServlet">Torna al Catalogo</a>
        </div>
    </div>
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>