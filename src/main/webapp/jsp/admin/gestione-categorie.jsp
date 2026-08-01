<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Gestione Categorie</h1>

    <c:if test="${not empty successMessage}">
        <div class="alert-success">
            <c:out value="${successMessage}" />
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert-error">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <div class="actions-bar">
        <a href="${pageContext.request.contextPath}/AdminCategoriaServlet?action=new" class="btn-add">➕ Nuova Categoria</a>
    </div>

    <table class="table-custom">
        <thead>
            <tr>
                <th>ID</th>
                <th>Nome</th>
                <th>Descrizione</th>
                <th class="text-center">Azioni</th>
            </tr>
        </thead>
        <tbody>
            <c:if test="${not empty categorie}">
                <c:forEach var="cat" items="${categorie}">
                    <tr class="table-row">
                        <td><c:out value="${cat.idCategoria}"/></td>
                        <td><c:out value="${cat.nome}"/></td>
                        <td class="col-desc"><c:out value="${cat.descrizione}"/></td>
                        <td class="col-actions text-center">
                            <a href="${pageContext.request.contextPath}/AdminCategoriaServlet?action=edit&id=${cat.idCategoria}" class="btn-edit">✏️ Modifica</a>
                            <form method="post" action="${pageContext.request.contextPath}/AdminCategoriaServlet" class="inline-form" onsubmit="return confirm('Eliminare la categoria ${cat.nome}?');">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="id" value="${cat.idCategoria}">
                                <button type="submit" class="btn-delete">🗑️ Elimina</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </c:if>
            <c:if test="${empty categorie}">
                <tr>
                    <td colspan="4" class="empty-row">Nessuna categoria presente.</td>
                </tr>
            </c:if>
        </tbody>
    </table>
</main>

<%@ include file="/jsp/common/footer.jspf" %>