<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Gestione Categorie</h1>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            <c:out value="${successMessage}" />
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <div class="admin-toolbar">
        <h2>Elenco Categorie</h2>
        <a href="${pageContext.request.contextPath}/AdminCategoriaServlet?action=new" class="btn btn-small">➕ Nuova Categoria</a>
    </div>

    <div class="admin-table-wrapper">
        <table class="admin-table">
            <thead>
                <tr>
                    <th style="width: 80px;">ID</th>
                    <th style="width: 250px;">Nome</th>
                    <th>Descrizione</th>
                    <th style="width: 180px; text-align: right;">Azioni</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty categorie}">
                        <c:forEach var="cat" items="${categorie}">
                            <tr>
                                <td><strong>#${cat.idCategoria}</strong></td>
                                <td><c:out value="${cat.nome}"/></td>
                                <td><c:out value="${cat.descrizione}"/></td>
                                <td style="text-align: right;">
                                    <div class="action-cell" style="justify-content: flex-end;">
                                        <a href="${pageContext.request.contextPath}/AdminCategoriaServlet?action=edit&id=${cat.idCategoria}" class="btn-edit">Modifica</a>
                                        <form method="post" action="${pageContext.request.contextPath}/AdminCategoriaServlet" class="action-form" onsubmit="return confirm('Eliminare la categoria ${cat.nome}?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${cat.idCategoria}">
                                            <button type="submit" class="btn-delete">Elimina</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="4" class="empty-table-msg">Nessuna categoria presente nel database.</td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</main>

<%@ include file="/jsp/common/footer.jspf" %>