<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 1. Importiamo la taglib JSTL per gestire le condizioni e l'output sicuro --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Login</h1>

    <%-- 2. Feedback di successo (es. reindirizzamento post-registrazione) --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success" style="color: #065f46; background-color: #d1fae5; border: 1px solid #a7f3d0; padding: 10px; border-radius: 4px; margin-bottom: 15px;">
            <c:out value="${successMessage}" />
        </div>
    </c:if>

    <%-- 3. Feedback di errore (es. credenziali errate o accesso negato) --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger" style="color: #991b1b; background-color: #fee2e2; border: 1px solid #fecaca; padding: 10px; border-radius: 4px; margin-bottom: 15px;">
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