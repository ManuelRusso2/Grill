<%-- 
    Pagina di Autenticazione Utente (Login).
    Fornisce il modulo per l'inserimento delle credenziali (Email e Password),
    gestisce i messaggi di errore/conferma inviati dalla LoginServlet e 
    offre un collegamento rapido per i nuovi utenti verso la pagina di registrazione.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Importazione della libreria JSTL Core per il controllo di flusso, cicli e condizioni --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Inclusione del frammento statico per l'intestazione HTML e le risorse della pagina (head, CSS, JS) --%>
<%@ include file="/jsp/common/header.jspf" %>

<%-- Inclusione del frammento statico per la barra di navigazione principale (menu / navbar) --%>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale della pagina di autenticazione --%>
<main class="container">
    <%-- Titolo della schermata di accesso --%>
    <h1 class="auth-title">Accedi al tuo Account</h1>

    <%-- ── MESSAGGI DI FEEDBACK LATO SERVER ────────────────────────────────── --%>

    <%-- Feedback di successo (es. reindirizzamento avvenuto con successo dopo la registrazione) --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            ✓ <c:out value="${successMessage}" />
        </div>
    </c:if>

    <%-- Feedback di errore (es. credenziali errate o utente non abilitato) --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            ✗ <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- ── SCHEDA FORM DI AUTENTICAZIONE ────────────────────────────────────────── --%>
    
    <%-- Form per la trasmissione sicura delle credenziali via HTTP POST alla LoginServlet --%>
    <form class="auth-card" method="post" action="${pageContext.request.contextPath}/LoginServlet">
        
        <%-- Campo di inserimento Email dell'utente --%>
        <div class="form-group">
            <label for="email">Indirizzo Email</label>
            <%-- Mantiene precompilata l'email inserita in precedenza in caso di errore di autenticazione --%>
            <input id="email" type="email" name="email" class="form-control"
                   value="<c:out value='${param.email}' />" 
                   required placeholder="nome@esempio.it" autocomplete="email">
        </div>

        <%-- Campo di inserimento Password dell'utente --%>
        <div class="form-group">
            <label for="password">Password</label>
            <input id="password" type="password" name="password" class="form-control"
                   required placeholder="Inserisci la password" autocomplete="current-password">
        </div>

        <%-- Pulsante principale di sottomissione a tutta larghezza --%>
        <button type="submit" class="btn btn-md btn-primary btn-full">Accedi</button>

        <%-- Link di reindirizzamento per utenti non ancora registrati --%>
        <p class="auth-footer-text">
            Non hai ancora un account? 
            <a href="${pageContext.request.contextPath}/jsp/user/registrazione.jsp">Registrati qui</a>
        </p>
    </form>
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>