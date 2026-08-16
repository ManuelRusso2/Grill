<%-- Impostazione del tipo di contenuto della pagina e della codifica dei caratteri (UTF-8) --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Inclusione della libreria di tag JSTL Core per i controlli condizionali e il sanificamento delle stringhe --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Inclusione dei frammenti di codice statici per l'intestazione (header) e la barra di navigazione (menu) --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale della pagina di autenticazione --%>
<main class="container">
    <%-- Titolo della schermata di accesso --%>
    <h1 class="auth-title">Login</h1>

    <%-- ── MESSAGGI DI FEEDBACK ALL'UTENTE ────────────────────────────────── --%>

    <%-- Feedback di successo (es. reindirizzamento avvenuto dopo la registrazione dell'account) --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            <c:out value="${successMessage}" />
        </div>
    </c:if>

    <%-- Feedback di errore (es. credenziali di accesso non valide o sessione scaduta) --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- ── FORM DI AUTENTICAZIONE ────────────────────────────────────────── --%>
    
    <%-- Form per la trasmissione sicura delle credenziali via HTTP POST alla LoginServlet --%>
    <form class="auth-card" method="post" action="${pageContext.request.contextPath}/LoginServlet">
        
        <%-- Campo di inserimento Email dell'utente --%>
        <div class="form-group">
            <label for="email">Email</label>
            <%-- Mantiene precompilata l'email in caso di errore di login per migliorare l'esperienza utente --%>
            <input id="email" type="email" name="email" 
                   value="<c:out value='${param.email}' />" 
                   required autocomplete="email">
        </div>

        <%-- Campo di inserimento Password dell'utente --%>
        <div class="form-group">
            <label for="password">Password</label>
            <input id="password" type="password" name="password" 
                   required autocomplete="current-password">
        </div>

        <%-- Pulsante di invio del modulo per avviare il processo di autenticazione --%>
        <button type="submit" class="btn-submit">Accedi</button>
    </form>
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>