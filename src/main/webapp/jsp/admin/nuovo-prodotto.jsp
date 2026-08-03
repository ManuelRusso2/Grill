<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <%-- Stabilisco se sono in modalità creazione o modifica --%>
    <c:set var="isEdit" value="${not empty prodotto}" />
    
    <h1>${isEdit ? 'Modifica Prodotto' : 'Aggiungi Nuovo Prodotto'}</h1>

    <form method="post" action="${pageContext.request.contextPath}/AdminProdottoServlet" class="form-container">
        <%-- Campo nascosto per l'azione (save o update) --%>
        <input type="hidden" name="action" value="${isEdit ? 'update' : 'save'}">
        
        <%-- Se in modalità edit, includo l'ID del prodotto --%>
        <c:if test="${isEdit}">
            <input type="hidden" name="id" value="${prodotto.idProdotto}">
        </c:if>

        <div class="form-group">
            <label for="nome" class="form-label">Nome Prodotto:</label>
            <input type="text" id="nome" name="nome" 
                   value="${isEdit ? prodotto.nome : ''}" 
                   required class="form-control">
        </div>

        <div class="form-group">
            <label for="descrizione" class="form-label">Descrizione:</label>
            <textarea id="descrizione" name="descrizione" 
                      required class="form-control form-textarea">${isEdit ? prodotto.descrizione : ''}</textarea>
        </div>

        <div class="form-group">
            <label for="immagine" class="form-label">Percorso Immagine (es. images/prodotto.jpg):</label>
            <input type="text" id="immagine" name="immagine"
                   value="${isEdit ? prodotto.immagine : 'images/default.jpg'}" 
                   class="form-control">
        </div>
        <div class="form-group">
            <label for="costo" class="form-label">Prezzo (€):</label>
            <input type="number" id="costo" name="costo" step="0.01" min="0"
                   value="${isEdit ? prodotto.costo : ''}" 
                   required class="form-control">
        </div>

        <div class="form-group">
            <label for="quantita" class="form-label">Quantità:</label>
            <input type="number" id="quantita" name="quantita" min="0"
                   value="${isEdit ? prodotto.quantita : ''}" 
                   required class="form-control">
        </div>

        <div class="form-group">
            <label class="form-label">Categorie:</label>
            <div class="category-input-group">
                <div class="category-select-wrapper">
                    <select id="idCategoria" name="idCategoria" multiple class="form-control form-select-multiple">
                        <c:if test="${not empty categorie}">
                            <c:forEach var="cat" items="${categorie}">
                                <c:set var="selezionata" value="false" />
                                <c:if test="${isEdit}">
                                    <c:forEach var="catProd" items="${prodotto.categorie}">
                                        <c:if test="${catProd.idCategoria == cat.idCategoria}">
                                            <c:set var="selezionata" value="true" />
                                        </c:if>
                                    </c:forEach>
                                </c:if>
                                <option value="${cat.idCategoria}" ${selezionata ? 'selected' : ''}>
                                    <c:out value="${cat.nome}" />
                                </option>
                            </c:forEach>
                        </c:if>
                    </select>
                    <small class="form-help-text">Tieni premuto Ctrl (o Cmd su Mac) per selezionare più categorie.</small>
                </div>
                <a href="${pageContext.request.contextPath}/AdminCategoriaServlet?action=new" class="btn-secondary">
                    ➕ Nuova Categoria
                </a>
            </div>
        </div>

        <div class="form-group-lg">
            <label class="checkbox-label">
                <input type="checkbox" name="attivo" 
                       ${isEdit ? (prodotto.attivo ? 'checked' : '') : 'checked'} 
                       class="form-checkbox">
                <span>Prodotto Attivo</span>
            </label>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn-add btn-submit">
                ${isEdit ? '💾 Salva Modifiche' : '➕ Crea Prodotto'}
            </button>
            <a href="${pageContext.request.contextPath}/AdminProdottoServlet" class="btn-edit btn-cancel">
                ← Annulla
            </a>
        </div>
    </form>
</main>

<%@ include file="/jsp/common/footer.jspf" %>