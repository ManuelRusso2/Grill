<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <%-- Stabilisco se sono in modalità creazione o modifica --%>
    <c:set var="isEdit" value="${not empty prodotto}" />
    
    <h1>${isEdit ? 'Modifica Prodotto' : 'Aggiungi Nuovo Prodotto'}</h1>

    <form method="post" action="<%=request.getContextPath()%>/AdminProdottoServlet" style="max-width: 600px; margin: 0 auto;">
        <%-- Campo nascosto per l'azione (save o update) --%>
        <input type="hidden" name="action" value="${isEdit ? 'update' : 'save'}">
        
        <%-- Se in modalità edit, includo l'ID del prodotto --%>
        <c:if test="${isEdit}">
            <input type="hidden" name="id" value="${prodotto.idProdotto}">
        </c:if>

        <div style="margin-bottom: 15px;">
            <label for="nome" style="display: block; margin-bottom: 5px; font-weight: 600;">Nome Prodotto:</label>
            <input type="text" id="nome" name="nome" 
                   value="${isEdit ? prodotto.nome : ''}" 
                   required style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px;">
        </div>

        <div style="margin-bottom: 15px;">
            <label for="descrizione" style="display: block; margin-bottom: 5px; font-weight: 600;">Descrizione:</label>
            <textarea id="descrizione" name="descrizione" 
                      required style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px; min-height: 100px; font-family: inherit; resize: vertical;">${isEdit ? prodotto.descrizione : ''}</textarea>
        </div>

        <div style="margin-bottom: 15px;">
            <label for="costo" style="display: block; margin-bottom: 5px; font-weight: 600;">Prezzo (€):</label>
            <input type="number" id="costo" name="costo" step="0.01" min="0"
                   value="${isEdit ? prodotto.costo : ''}" 
                   required style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px;">
        </div>

        <div style="margin-bottom: 15px;">
            <label for="quantita" style="display: block; margin-bottom: 5px; font-weight: 600;">Quantità:</label>
            <input type="number" id="quantita" name="quantita" min="0"
                   value="${isEdit ? prodotto.quantita : ''}" 
                   required style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px;">
        </div>

        <div style="margin-bottom: 15px;">
            <label for="tipo" style="display: block; margin-bottom: 5px; font-weight: 600;">Categoria:</label>
            <div style="display: flex; gap: 10px; align-items: flex-end;">
                <div style="flex: 1;">
                    <select id="tipo" name="tipo" style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 4px;">
                        <option value="">-- Seleziona una categoria --</option>
                        <c:if test="${not empty categorie}">
                            <c:forEach var="cat" items="${categorie}">
                                <option value="${cat.nome}" data-id="${cat.idCategoria}" 
                                        ${isEdit && prodotto.tipo == cat.nome ? 'selected' : ''}>
                                    <c:out value="${cat.nome}" />
                                </option>
                            </c:forEach>
                        </c:if>
                    </select>
                </div>
                <a href="${pageContext.request.contextPath}/AdminCategoriaServlet?action=new" class="btn-edit" style="padding: 8px 15px; background-color: #6b7280; color: white; border: none; border-radius: 4px; text-decoration: none; display: inline-block; white-space: nowrap;">
                    ➕ Nuova Categoria
                </a>
            </div>
        </div>

        <!-- gestione categorie spostata nella sezione dedicata AdminCategoriaServlet -->

        <!-- Gestione categorie rimossa da questa pagina; usare AdminCategoriaServlet per operazioni complete -->

        <div style="margin-bottom: 20px;">
            <label style="display: flex; align-items: center; gap: 8px; cursor: pointer;">
                <input type="checkbox" name="attivo" 
                       ${isEdit ? (prodotto.attivo ? 'checked' : '') : 'checked'} 
                       style="width: 18px; height: 18px; cursor: pointer;">
                <span style="font-weight: 600;">Prodotto Attivo</span>
            </label>
        </div>

        <div style="display: flex; gap: 10px;">
            <button type="submit" class="btn-add" style="padding: 10px 25px; border-radius: 4px; cursor: pointer; font-weight: 600;">
                ${isEdit ? '💾 Salva Modifiche' : '➕ Crea Prodotto'}
            </button>
            <a href="${pageContext.request.contextPath}/AdminProdottoServlet" class="btn-edit" style="padding: 10px 25px; border-radius: 4px; text-decoration: none; display: inline-flex; align-items: center; font-weight: 600;">
                ← Annulla
            </a>
        </div>
    </form>
</main>

<%@ include file="/jsp/common/footer.jspf" %>