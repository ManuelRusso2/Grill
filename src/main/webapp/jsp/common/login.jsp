<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1 class="auth-title">Login</h1>

    <%-- Feedback di successo (es. reindirizzamento post-registrazione) --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            <c:out value="${successMessage}" />
        </div>
    </c:if>

    <%-- Feedback di errore (es. credenziali errate o accesso negato) --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- Form di login strutturato --%>
    <form class="auth-card" method="post" action="${pageContext.request.contextPath}/LoginServlet">
        <div class="form-group">
            <label for="email">Email</label>
            <input id="email" type="email" name="email" required autocomplete="email">
        </div>

        <div class="form-group">
            <label for="password">Password</label>
            <input id="password" type="password" name="password" required>
        </div>

        <button type="submit" class="btn-submit">Accedi</button>
    </form>
</main>

<%@ include file="/jsp/common/footer.jspf" %>