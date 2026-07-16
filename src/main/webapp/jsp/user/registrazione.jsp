<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Registrazione</h1>

    <form id="registerForm" method="post" action="<%=request.getContextPath()%>/RegistrationServlet">
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

    <script src="<%=request.getContextPath()%>/js/validazione.js"></script>
</main>

<%@ include file="/jsp/common/footer.jspf" %>