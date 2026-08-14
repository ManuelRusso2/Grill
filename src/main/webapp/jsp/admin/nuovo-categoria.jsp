<%-- 
    Pagina di creazione/modifica categoria.
    Questa pagina utilizza una logica condizionale per gestire due stati:
    1. Creazione di una nuova categoria (action="save").
    2. Modifica di una categoria esistente (action="update").
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Inclusione della Tag Library JSTL Core per la gestione del flusso --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Inclusione dei frammenti statici per header e menu --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <%-- Verifica se l'oggetto 'categoria' è presente per determinare se siamo in modalità edit o crea --%>
    <c:set var="isEdit" value="${not empty categoria}" />
    <h1>${isEdit ? 'Modifica Categoria' : 'Aggiungi Categoria'}</h1>

    <form method="post" action="${pageContext.request.contextPath}/AdminCategoriaServlet" class="admin-form">
        <%-- L'action del Servlet cambia dinamicamente in base allo stato della form --%>
        <input type="hidden" name="action" value="${isEdit ? 'update' : 'save'}" />
        
        <%-- Inserisce l'ID solo in caso di modifica, necessario per il backend per identificare il record --%>
        <c:if test="${isEdit}">
            <input type="hidden" name="id" value="${categoria.idCategoria}" />
        </c:if>

        <div class="form-group">
            <label for="nome">Nome:</label>
            <%-- Pre-popola il campo nome se siamo in edit, altrimenti rimane vuoto --%>
            <input type="text" id="nome" name="nome" required value="<c:out value='${categoria.nome}' />">
        </div>

        <div class="form-group">
            <label for="descrizione">Descrizione:</label>
            <%-- Pre-popola la textarea se siamo in edit --%>
            <textarea id="descrizione" name="descrizione" rows="4"><c:out value="${categoria.descrizione}" /></textarea>
        </div>

        <div class="form-actions">
            <%-- Il bottone di submit cambia etichetta in base all'operazione --%>
            <button type="submit" class="btn">${isEdit ? 'Salva Modifiche' : 'Crea Categoria'}</button>
            <a href="${pageContext.request.contextPath}/AdminCategoriaServlet" class="btn btn-secondary">Annulla</a>
        </div>
    </form>
</main>

<%-- Inclusione del piè di pagina --%>
<%@ include file="/jsp/common/footer.jspf" %>