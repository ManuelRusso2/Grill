<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1 class="auth-title">Registrazione</h1>

    <form id="registerForm" class="auth-card" method="post" action="${pageContext.request.contextPath}/RegistrationServlet">

        <div class="form-group">
            <label for="nome">Nome *</label>
            <input id="nome" type="text" name="nome" value="<c:out value='${formNome}'/>" class="<c:if test='${not empty errNome}'>input-error</c:if>">
            <c:if test="${not empty errNome}">
                <span class="field-error"><c:out value="${errNome}"/></span>
            </c:if>
        </div>

        <div class="form-group">
            <label for="cognome">Cognome *</label>
            <input id="cognome" type="text" name="cognome" value="<c:out value='${formCognome}'/>" class="<c:if test='${not empty errCognome}'>input-error</c:if>">
            <c:if test="${not empty errCognome}">
                <span class="field-error"><c:out value="${errCognome}"/></span>
            </c:if>
        </div>

        <div class="form-group">
            <label for="email">Email *</label>
            <input id="email" type="email" name="email" value="<c:out value='${formEmail}'/>" class="<c:if test='${not empty errEmail}'>input-error</c:if>" autocomplete="off">
            <span id="emailError" class="field-error" style="display:none;"></span>
            <c:if test="${not empty errEmail}">
                <span class="field-error"><c:out value="${errEmail}"/></span>
            </c:if>
        </div>

        <div class="form-group">
            <label for="password">Password *</label>
            <input id="password" type="password" name="password" class="<c:if test='${not empty errPassword}'>input-error</c:if>">
            <span id="passwordHint" class="field-hint">Minimo 6 caratteri, almeno una maiuscola e un numero.</span>
            <c:if test="${not empty errPassword}">
                <span class="field-error"><c:out value="${errPassword}"/></span>
            </c:if>
        </div>

        <div class="form-group">
            <label for="telefono">Telefono <span class="optional">(opzionale)</span></label>
            <input id="telefono" type="text" name="telefono" value="<c:out value='${formTelefono}'/>" class="<c:if test='${not empty errTelefono}'>input-error</c:if>">
            <c:if test="${not empty errTelefono}">
                <span class="field-error"><c:out value="${errTelefono}"/></span>
            </c:if>
        </div>

        <button type="submit" class="btn-submit">Registrati</button>
    </form>

    <script src="${pageContext.request.contextPath}/js/validazione.js"></script>
</main>

<%@ include file="/jsp/common/footer.jspf" %>