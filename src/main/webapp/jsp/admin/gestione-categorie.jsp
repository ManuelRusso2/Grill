<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Gestione Categorie</h1>

    <c:if test="${not empty successMessage}">
        <div style="color: #065f46; background-color: #d1fae5; padding: 10px; border-radius:4px; margin-bottom:12px;">
            <c:out value="${successMessage}" />
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div style="color: #991b1b; background-color: #fee2e2; padding: 10px; border-radius:4px; margin-bottom:12px;">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <div style="margin-bottom: 18px;">
        <a href="${pageContext.request.contextPath}/AdminCategoriaServlet?action=new" class="btn-add" style="padding:10px 18px; text-decoration:none;">➕ Nuova Categoria</a>
    </div>

    <table style="width:100%; border-collapse:collapse;">
        <thead>
            <tr style="background:#f3f4f6;">
                <th style="padding:10px; text-align:left;">ID</th>
                <th style="padding:10px; text-align:left;">Nome</th>
                <th style="padding:10px; text-align:left;">Descrizione</th>
                <th style="padding:10px; text-align:center;">Azioni</th>
            </tr>
        </thead>
        <tbody>
            <c:if test="${not empty categorie}">
                <c:forEach var="cat" items="${categorie}">
                    <tr style="border-bottom:1px solid #e5e7eb;">
                        <td style="padding:10px;"><c:out value="${cat.idCategoria}"/></td>
                        <td style="padding:10px;"><c:out value="${cat.nome}"/></td>
                        <td style="padding:10px; color:#6b7280; max-width:400px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;"><c:out value="${cat.descrizione}"/></td>
                        <td style="padding:10px; text-align:center; white-space:nowrap;">
                            <a href="${pageContext.request.contextPath}/AdminCategoriaServlet?action=edit&id=${cat.idCategoria}" class="btn-edit" style="margin-right:6px;">✏️ Modifica</a>
                            <form method="post" action="${pageContext.request.contextPath}/AdminCategoriaServlet" style="display:inline;" onsubmit="return confirm('Eliminare la categoria ${cat.nome}?');">
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
                    <td colspan="4" style="text-align:center; padding:20px; color:#64748b;">Nessuna categoria presente.</td>
                </tr>
            </c:if>
        </tbody>
    </table>
</main>

<%@ include file="/jsp/common/footer.jspf" %>