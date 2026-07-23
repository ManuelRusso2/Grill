<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <c:set var="isEdit" value="${not empty categoria}" />
    <h1>${isEdit ? 'Modifica Categoria' : 'Aggiungi Categoria'}</h1>

    <form method="post" action="${pageContext.request.contextPath}/AdminCategoriaServlet" style="max-width:600px;">
        <input type="hidden" name="action" value="${isEdit ? 'update' : 'save'}" />
        <c:if test="${isEdit}">
            <input type="hidden" name="id" value="${categoria.idCategoria}" />
        </c:if>

        <div style="margin-bottom:12px;">
            <label style="display:block; font-weight:600;">Nome:</label>
            <input type="text" name="nome" required value="${isEdit ? categoria.nome : ''}" style="width:100%; padding:8px; border:1px solid #ccc; border-radius:4px;">
        </div>

        <div style="margin-bottom:12px;">
            <label style="display:block; font-weight:600;">Descrizione:</label>
            <textarea name="descrizione" style="width:100%; padding:8px; min-height:120px; border:1px solid #ccc; border-radius:4px;">${isEdit ? categoria.descrizione : ''}</textarea>
        </div>

        <div style="display:flex; gap:10px;">
            <button type="submit" class="btn-add">${isEdit ? 'Salva Modifiche' : 'Crea Categoria'}</button>
            <a href="${pageContext.request.contextPath}/AdminCategoriaServlet" class="btn-edit">Annulla</a>
        </div>
    </form>
</main>

<%@ include file="/jsp/common/footer.jspf" %>