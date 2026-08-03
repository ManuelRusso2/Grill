<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <c:set var="isEdit" value="${not empty categoria}" />
    <h1>${isEdit ? 'Modifica Categoria' : 'Aggiungi Categoria'}</h1>

    <form method="post" action="${pageContext.request.contextPath}/AdminCategoriaServlet" class="admin-form">
        <input type="hidden" name="action" value="${isEdit ? 'update' : 'save'}" />
        <c:if test="${isEdit}">
            <input type="hidden" name="id" value="${categoria.idCategoria}" />
        </c:if>

        <div class="form-group">
            <label for="nome">Nome:</label>
            <input type="text" id="nome" name="nome" required value="${isEdit ? categoria.nome : ''}">
        </div>

        <div class="form-group">
            <label for="descrizione">Descrizione:</label>
            <textarea id="descrizione" name="descrizione" rows="4">${isEdit ? categoria.descrizione : ''}</textarea>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn">${isEdit ? 'Salva Modifiche' : 'Crea Categoria'}</button>
            <a href="${pageContext.request.contextPath}/AdminCategoriaServlet" class="btn btn-secondary">Annulla</a>
        </div>
    </form>
</main>

<%@ include file="/jsp/common/footer.jspf" %>