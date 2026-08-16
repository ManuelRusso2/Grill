<%-- Impostazione del tipo di contenuto della pagina e della codifica dei caratteri (UTF-8) --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Inclusione della libreria di tag JSTL Core per la gestione della logica condizionale ed estrazione valori --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Inclusione dei frammenti di codice statici per l'intestazione (header) e la barra di navigazione (menu) --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale della pagina di registrazione utente --%>
<main class="container">
    <%-- Titolo principale della schermata di registrazione --%>
    <h1 class="auth-title">Registrazione</h1>

    <%-- Form per l'invio dei dati di registrazione via POST alla Servlet 'RegistrationServlet' --%>
    <form id="registerForm" class="auth-card" method="post" action="${pageContext.request.contextPath}/RegistrationServlet">

        <%-- ── CAMPO: NOME ────────────────────────────────────────────────── --%>
        <div class="form-group">
            <label for="nome">Nome *</label>
            <%-- Input testo con mantenimento del valore precedentemente inserito e classe dinamica di errore --%>
            <input id="nome" type="text" name="nome" value="<c:out value='${formNome}'/>" class="${not empty errNome ? 'input-error' : ''}">
            <%-- Visualizzazione del messaggio di errore specifico restituito dalla Servlet --%>
            <c:if test="${not empty errNome}">
                <span class="field-error"><c:out value="${errNome}"/></span>
            </c:if>
        </div>

        <%-- ── CAMPO: COGNOME ─────────────────────────────────────────────── --%>
        <div class="form-group">
            <label for="cognome">Cognome *</label>
            <%-- Input testo per il cognome con persistenza dei dati e gestione dell'evidenziazione in caso di errore --%>
            <input id="cognome" type="text" name="cognome" value="<c:out value='${formCognome}'/>" class="${not empty errCognome ? 'input-error' : ''}">
            <%-- Visualizzazione dinamica dell'errore relativo al cognome --%>
            <c:if test="${not empty errCognome}">
                <span class="field-error"><c:out value="${errCognome}"/></span>
            </c:if>
        </div>

        <%-- ── CAMPO: EMAIL ───────────────────────────────────────────────── --%>
        <div class="form-group">
            <label for="email">Email *</label>
            <%-- Input email con autocompletamento attivato e gestione visiva degli errori server-side --%>
            <input id="email" type="email" name="email" value="<c:out value='${formEmail}'/>" class="${not empty errEmail ? 'input-error' : ''}" autocomplete="email">
            <%-- Elemento span per la visualizzazione dinamica degli errori via JavaScript client-side --%>
            <span id="emailError" class="field-error" style="display:none;"></span>
            <%-- Visualizzazione dell'errore di validazione lato server (es. email già esistente nel DB) --%>
            <c:if test="${not empty errEmail}">
                <span class="field-error"><c:out value="${errEmail}"/></span>
            </c:if>
        </div>

        <%-- ── CAMPO: PASSWORD ────────────────────────────────────────────── --%>
        <div class="form-group">
            <label for="password">Password *</label>
            <%-- Input masked per la password con classe di errore condizionale --%>
            <input id="password" type="password" name="password" class="${not empty errPassword ? 'input-error' : ''}">
            <%-- Testo guida informativo con i requisiti minimi di sicurezza per la password --%>
            <span id="passwordHint" class="field-hint">Minimo 6 caratteri, almeno una maiuscola e un numero.</span>
            <%-- Messaggio di errore restituito dalla Servlet in caso di password non conforme --%>
            <c:if test="${not empty errPassword}">
                <span class="field-error"><c:out value="${errPassword}"/></span>
            </c:if>
        </div>

        <%-- ── CAMPO: TELEFONO ────────────────────────────────────────────── --%>
        <div class="form-group">
            <label for="telefono">Telefono <span class="optional">(opzionale)</span></label>
            <%-- Input opzionale per il numero telefonico con ripristino del valore inserito --%>
            <input id="telefono" type="tel" name="telefono" value="<c:out value='${formTelefono}'/>" class="${not empty errTelefono ? 'input-error' : ''}">
            <%-- Messaggio di errore lato server se il formato del telefono non è valido --%>
            <c:if test="${not empty errTelefono}">
                <span class="field-error"><c:out value="${errTelefono}"/></span>
            </c:if>
        </div>

        <%-- Pulsante di invio del modulo per sottomettere i dati di registrazione --%>
        <button type="submit" class="btn-submit">Registrati</button>
    </form>

    <%-- Inclusione dello script JavaScript esterno per la validazione client-side dei campi --%>
    <script src="${pageContext.request.contextPath}/js/validazione.js"></script>
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>