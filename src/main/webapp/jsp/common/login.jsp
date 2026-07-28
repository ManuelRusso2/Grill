<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 1. Importiamo la taglib JSTL per gestire le condizioni e l'output sicuro --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Login</h1>

    <%-- 2. Feedback di successo (es. reindirizzamento post-registrazione) --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            <c:out value="${successMessage}" />
        </div>
    </c:if>

    <%-- 3. Feedback di errore (es. credenziali errate o accesso negato) --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- 4. Form di login con Context Path in Expression Language --%>
    <form method="post" action="${pageContext.request.contextPath}/LoginServlet">
        <label>Email: <input type="email" name="email" required></label><br>
        <label>Password: <input type="password" name="password" required></label><br>
        <button type="submit">Accedi</button>
    </form>
</main>

<%@ include file="/jsp/common/footer.jspf" %>