<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Importiamo JSTL per gestire la visualizzazione condizionale dei messaggi --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Registrazione</h1>

    <%-- 🌟 BLOCCO FEEDBACK: Mostra gli errori di validazione o duplicati inviati dalla Servlet --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger" style="color: red; background-color: #fee2e2; padding: 10px; border-radius: 4px; margin-bottom: 15px;">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- Form aggiornato con action in Expression Language --%>
    <form id="registerForm" method="post" action="${pageContext.request.contextPath}/RegistrationServlet">
        <label>Nome: <input type="text" name="nome" required></label><br>
        <label>Cognome: <input type="text" name="cognome" required></label><br>
        
        <label>Email: <input id="email" type="email" name="email" required></label>
        <span id="emailError" class="error-message" style="display:none;color:red"></span><br>
        
        <label>Username: <input id="username" type="text" name="username" required></label>
        <span id="usernameError" class="error-message" style="display:none;color:red"></span><br>
        
        <label>Password: <input type="password" name="password" required></label><br>
        <label>Telefono: <input type="text" name="telefono"></label><br>
        
        <button type="submit">Registrati</button>
    </form>

    <script src="${pageContext.request.contextPath}/js/validazione.js"></script>
</main>

<%@ include file="/jsp/common/footer.jspf" %>