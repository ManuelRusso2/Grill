<%-- 
    Pagina di creazione/modifica categoria.
    Le informazioni di stato (action e id) vengono inviate direttamente nell'URL
    senza utilizzare campi input hidden nel form.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Importazione della libreria JSTL Core per il controllo di flusso, cicli e condizioni --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Inclusione del frammento statico per l'intestazione HTML e le risorse della pagina (head, CSS, JS) --%>
<%@ include file="/jsp/common/header.jspf" %>

<%-- Inclusione del frammento statico per la barra di navigazione principale (menu / navbar) --%>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <%-- Determinazione dello stato di modifica o creazione --%>
    <c:set var="isEdit" value="${not empty categoria}" />

    <c:choose>
        <c:when test="${isEdit}">
            <h1>Modifica Categoria</h1>
        </c:when>
        <c:otherwise>
            <h1>Aggiungi Categoria</h1>
        </c:otherwise>
    </c:choose>

    <%-- ── MESSAGGI DI ERRORE LATO SERVER ───────────────────────────────── --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            ✗ <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- I parametri 'action' e 'id' vengono passati direttamente nella Query String dell'URL del form --%>
    <form method="post" 
          action="${pageContext.request.contextPath}/AdminCategoriaServlet?<c:choose><c:when test="${isEdit}">action=update&id=${categoria.idCategoria}</c:when><c:otherwise>action=save</c:otherwise></c:choose>" 
          class="admin-form">

        <div class="form-group">
            <label for="nome">Nome *</label>
            <%-- Pre-popola il campo nome se siamo in edit, altrimenti rimane vuoto --%>
            <input type="text" id="nome" name="nome" required value="<c:out value='${categoria.nome}' />" placeholder="Inserisci il nome">
        </div>

        <div class="form-group">
            <label for="descrizione">Descrizione</label>
            <%-- Pre-popola la textarea se siamo in edit --%>
            <textarea id="descrizione" name="descrizione" rows="4" placeholder="Inserisci una descrizione opzionale"><c:out value="${categoria.descrizione}" /></textarea>
        </div>

        <div class="form-actions">
            <%-- Il bottone di submit cambia etichetta in base all'operazione --%>
            <c:choose>
                <c:when test="${isEdit}">
                    <button type="submit" class="btn btn-md btn-primary">Salva Modifiche</button>
                </c:when>
                <c:otherwise>
                    <button type="submit" class="btn btn-md btn-primary">Crea Categoria</button>
                </c:otherwise>
            </c:choose>
            <a href="${pageContext.request.contextPath}/AdminCategoriaServlet" class="btn btn-md btn-secondary">Annulla</a>
        </div>
    </form>
</main>

<%-- Inclusione del piè di pagina --%>
<%@ include file="/jsp/common/footer.jspf" %>