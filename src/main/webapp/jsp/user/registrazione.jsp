<%-- 
    Pagina di Registrazione Nuovo Utente.
    Fornisce il modulo completo per la creazione di un account utente (Nome, Cognome, Email,
    Password e Telefono opzionale), gestisce la persistenza dei dati immessi in caso di errore,
    mostra gli errori lato server/client e include la validazione JS esterna.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Importazione della libreria JSTL Core per il controllo di flusso, cicli e condizioni --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Inclusione del frammento statico per l'intestazione HTML e le risorse della pagina (head, CSS, JS) --%>
<%@ include file="/jsp/common/header.jspf" %>

<%-- Inclusione del frammento statico per la barra di navigazione principale (menu / navbar) --%>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale di layout per la pagina di registrazione utente --%>
<main class="container">
    <%-- Titolo principale della schermata di registrazione --%>
    <h1 class="auth-title">Crea un nuovo Account</h1>

    <%-- Form per l'invio dei dati di registrazione via POST alla Servlet 'RegistrationServlet' --%>
    <form id="registerForm" class="auth-card" method="post" 
          action="${pageContext.request.contextPath}/RegistrationServlet"
          data-contextpath="${pageContext.request.contextPath}">

        <%-- ── CAMPO: NOME ────────────────────────────────────────────────── --%>
        <div class="form-group">
            <label for="nome">Nome *</label>
            <%-- Input testo con persistenza del valore e gestione dell'evidenziazione in caso di errore --%>
            <input id="nome" type="text" name="nome" value="<c:out value='${formNome}'/>" 
                   class="<c:if test='${not empty errNome}'>input-error</c:if>" placeholder="Mario" required>
            <%-- Visualizzazione del messaggio di errore specifico restituito dalla Servlet --%>
            <c:if test="${not empty errNome}">
                <span class="field-error-span"><c:out value="${errNome}"/></span>
            </c:if>
        </div>

        <%-- ── CAMPO: COGNOME ─────────────────────────────────────────────── --%>
        <div class="form-group">
            <label for="cognome">Cognome *</label>
            <%-- Input testo per il cognome con gestione dell'evidenziazione in caso di errore --%>
            <input id="cognome" type="text" name="cognome" value="<c:out value='${formCognome}'/>" 
                   class="<c:if test='${not empty errCognome}'>input-error</c:if>" placeholder="Rossi" required>
            <%-- Visualizzazione dinamica dell'errore relativo al cognome --%>
            <c:if test="${not empty errCognome}">
                <span class="field-error-span"><c:out value="${errCognome}"/></span>
            </c:if>
        </div>

        <%-- ── CAMPO: EMAIL ───────────────────────────────────────────────── --%>
        <div class="form-group">
            <label for="email">Indirizzo Email *</label>
            <%-- Input email con autocompletamento e gestione visiva degli errori server-side/client-side --%>
            <input id="email" type="email" name="email" value="<c:out value='${formEmail}'/>" 
                   class="<c:if test='${not empty errEmail}'>input-error</c:if>" placeholder="nome@esempio.it" autocomplete="email" required>
            <%-- Visualizzazione dell'errore lato server o dinamico via JS --%>
            <c:if test="${not empty errEmail}">
                <span class="field-error-span"><c:out value="${errEmail}"/></span>
            </c:if>
        </div>

        <%-- ── CAMPO: PASSWORD ────────────────────────────────────────────── --%>
        <div class="form-group">
            <label for="password">Password *</label>
            <%-- Input masked per la password con classe di errore condizionale --%>
            <input id="password" type="password" name="password" 
                   class="<c:if test='${not empty errPassword}'>input-error</c:if>" placeholder="Crea una password" required>
            <%-- Testo guida informativo con i requisiti minimi di sicurezza --%>
            <span id="passwordHint" class="field-hint">Minimo 6 caratteri, almeno una maiuscola e un numero.</span>
            <%-- Messaggio di errore restituito dalla Servlet in caso di password non conforme --%>
            <c:if test="${not empty errPassword}">
                <span class="field-error-span"><c:out value="${errPassword}"/></span>
            </c:if>
        </div>

        <%-- ── CAMPO: TELEFONO ────────────────────────────────────────────── --%>
        <div class="form-group">
            <label for="telefono">Telefono <span class="optional">(opzionale)</span></label>
            <%-- Input opzionale per il numero telefonico con evidenziazione in caso di errore --%>
            <input id="telefono" type="tel" name="telefono" value="<c:out value='${formTelefono}'/>" 
                   class="<c:if test='${not empty errTelefono}'>input-error</c:if>" placeholder="+39 333 1234567">
            <%-- Messaggio di errore lato server se il formato del telefono non è valido --%>
            <c:if test="${not empty errTelefono}">
                <span class="field-error-span"><c:out value="${errTelefono}"/></span>
            </c:if>
        </div>

        <%-- Pulsante di invio del modulo a tutta larghezza --%>
        <button type="submit" class="btn btn-md btn-primary btn-full">Registrati</button>

        <%-- Link di reindirizzamento per utenti già in possesso di un account --%>
        <p class="auth-footer-text">
            Hai già un account? 
            <a href="${pageContext.request.contextPath}/jsp/common/login.jsp">Accedi qui</a>
        </p>
    </form>

    <%-- Inclusione dello script JavaScript esterno per la validazione client-side dei campi --%>
    <script src="${pageContext.request.contextPath}/js/validazione.js"></script>
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>