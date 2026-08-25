<%-- Pagina di errore HTTP 403 (Accesso Negato): mostrata quando l'utente non possiede i permessi per accedere alla risorsa --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Importazione della libreria JSTL Core per il controllo di flusso, cicli e condizioni --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Inclusione del frammento statico per l'intestazione HTML e le risorse della pagina (head, CSS, JS) --%>
<%@ include file="/jsp/common/header.jspf" %>

<%-- Inclusione del frammento statico per la barra di navigazione principale (menu / navbar) --%>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale della scheda d'errore --%>
<main class="container error-page">
    <div class="error-card">
        <%-- Codice identificativo dell'errore e titolo principale --%>
        <h1 class="error-code">403</h1>
        <h2>Accesso negato</h2>

        <%-- Messaggio esplicativo di supporto per l'utente --%>
        <p>Mi dispiace, non hai i permessi necessari per visualizzare questa pagina.</p>
        <p>Se pensi che si tratti di un errore, contatta l'amministratore o effettua il logout.</p>

        <%-- Pulsanti di reindirizzamento e ripristino della navigazione --%>
        <div class="error-actions">
            <%-- Link per tornare alla visualizzazione del catalogo prodotti --%>
            <a class="btn" href="${pageContext.request.contextPath}/CatalogoServlet">Torna al Catalogo</a>
            
            <%-- Link per disconnettere la sessione attuale --%>
            <a class="btn btn-md btn-secondary" href="${pageContext.request.contextPath}/LogoutServlet">Logout</a>
        </div>
    </div>
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>