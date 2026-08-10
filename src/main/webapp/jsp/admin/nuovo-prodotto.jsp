<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <c:set var="isEdit" value="${not empty prodotto}" />
    
    <h1>${isEdit ? 'Modifica Prodotto' : 'Nuovo Prodotto'}</h1>

    <div class="admin-edit-grid">
        
        <!-- COLONNA SINISTRA: Form di modifica -->
        <div class="admin-form-card">
            <h2>${isEdit ? 'Dettagli Prodotto' : 'Inserisci Dati'}</h2>

            <form method="post" action="${pageContext.request.contextPath}/AdminProdottoServlet" class="form-container">
                <input type="hidden" name="action" value="${isEdit ? 'update' : 'save'}">
                
                <c:if test="${isEdit}">
                    <input type="hidden" name="id" value="${prodotto.idProdotto}">
                </c:if>

                <div class="form-group">
                    <label for="nome">Nome Prodotto:</label>
                    <input type="text" id="nome" name="nome" 
                           value="${isEdit ? prodotto.nome : ''}" required>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="costo">Prezzo (€):</label>
                        <input type="number" id="costo" name="costo" step="0.01" min="0"
                               value="${isEdit ? prodotto.costo : ''}" required>
                    </div>

                    <div class="form-group">
                        <label for="quantita">Quantità in Stock:</label>
                        <input type="number" id="quantita" name="quantita" min="0"
                               value="${isEdit ? prodotto.quantita : ''}" required>
                    </div>
                </div>
                
                <div class="form-group" style="margin-top: 15px;">
                    <label for="taglie">Taglie disponibili (opzionale):</label>
                    <input type="text" id="taglie" name="taglie" 
                           value="${isEdit ? prodotto.taglie : ''}" placeholder="Es: S, M, L, XL, 42, 44">
                    <span class="field-hint">Inserisci le taglie separate da una virgola.</span>
                </div>

                <!-- GRIGLIA CATEGORIE A CHIP -->
                <div class="form-group">
                    <label>Categorie associate:</label>
                    <div style="display: flex; gap: 10px; align-items: flex-start;">
                        <div style="flex-grow: 1;">
                            <div class="categories-checkbox-grid">
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
                                        
                                        <label class="category-chip">
                                            <input type="checkbox" name="idCategoria" value="${cat.idCategoria}" ${selezionata ? 'checked' : ''}>
                                            <span><c:out value="${cat.nome}" /></span>
                                        </label>
                                    </c:forEach>
                                </c:if>
                            </div>
                            <span class="field-hint">Seleziona una o più categorie cliccando sui badge.</span>
                        </div>
                        <a href="${pageContext.request.contextPath}/AdminCategoriaServlet?action=new" class="btn btn-secondary btn-small" style="white-space: nowrap;">
                            ➕ Nuova
                        </a>
                    </div>
                </div>

                <div class="form-group">
                    <label for="descrizione">Descrizione:</label>
                    <textarea id="descrizione" name="descrizione" rows="4" required><c:out value="${isEdit ? prodotto.descrizione : ''}" /></textarea>
                </div>

                <div class="form-group">
                    <label for="immagine">Percorso Immagine:</label>
                    <input type="text" id="immagine" name="immagine"
                           value="${isEdit ? prodotto.immagine : 'images/default.jpg'}">
                    <span class="field-hint">Es: images/prodotto.jpg. L'anteprima si aggiornerà in tempo reale.</span>
                </div>

                <div class="form-group" style="flex-direction: row; align-items: center; gap: 10px; margin-top: 25px; padding: 15px; background: rgba(255,255,255,0.02); border: 1px solid var(--border-color); border-radius: 6px;">
                    <input type="checkbox" id="attivo" name="attivo" 
                           ${isEdit ? (prodotto.attivo ? 'checked' : '') : 'checked'} 
                           style="width: 20px; height: 20px; accent-color: var(--primary-purple); cursor: pointer;">
                    <label for="attivo" style="margin: 0; cursor: pointer; color: var(--text-light); font-size: 15px;">Prodotto Attivo (Visibile nel catalogo)</label>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn">
                        ${isEdit ? 'Salva Modifiche' : 'Crea Prodotto'}
                    </button>
                    <a href="${pageContext.request.contextPath}/AdminProdottoServlet" class="btn btn-secondary">
                        Annulla
                    </a>
                </div>
            </form>
        </div>

        <!-- COLONNA DESTRA: Anteprima Immagine e Info -->
        <div class="admin-preview-card">
            <h2>Anteprima Visiva</h2>
            
            <div class="preview-image-wrapper">
                <img id="live-preview-img" 
                     src="${pageContext.request.contextPath}/images/default.jpg" 
                     alt="Anteprima Prodotto" 
                     class="preview-image"
                     onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/default.jpg';">
            </div>

            <div class="preview-details">
                <c:if test="${isEdit}">
                    <div class="detail-row">
                        <span class="detail-label">ID Prodotto</span>
                        <span class="detail-value">#${prodotto.idProdotto}</span>
                    </div>
                </c:if>
                
                <div class="detail-row">
                    <span class="detail-label">Stato Stock</span>
                    <span class="detail-value" id="live-stock-badge">
                        <c:choose>
                            <c:when test="${not empty prodotto && prodotto.quantita > 5}">
                                <span class="badge-disponibile">Disponibile</span>
                            </c:when>
                            <c:when test="${not empty prodotto && prodotto.quantita > 0}">
                                <span class="badge-scarso">In Esaurimento</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge-esaurito">Esaurito / N.D.</span>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>

                <div class="detail-row">
                    <span class="detail-label">Stato Visibilità</span>
                    <span class="detail-value" id="live-status-badge">
                        <c:choose>
                            <c:when test="${not empty prodotto && !prodotto.attivo}">
                                <span class="badge-esaurito">Nascosto</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge-disponibile">Pubblicato</span>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>

            <c:if test="${isEdit}">
                <div class="preview-actions">
                    <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${prodotto.idProdotto}" target="_blank" class="btn btn-view">
                        Vedi nel Negozio ↗
                    </a>
                </div>
            </c:if>
        </div>

    </div>
</main>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const inputImmagine = document.getElementById('immagine');
    const previewImg = document.getElementById('live-preview-img');
    const basePath = '${pageContext.request.contextPath}/';
    
    function updatePreview() {
        if (!inputImmagine || !previewImg) return;

        let path = inputImmagine.value.trim();
        if (!path) {
            previewImg.src = basePath + 'images/default.jpg';
            return;
        }

        // Rimuove eventuali slash iniziali per evitare percorsi errati
        while (path.startsWith('/')) {
            path = path.substring(1);
        }

        if (path.startsWith('http://') || path.startsWith('https://')) {
            previewImg.src = path;
        } else {
            previewImg.src = basePath + path;
        }
    }

    if (inputImmagine && previewImg) {
        inputImmagine.addEventListener('input', updatePreview);
        // Esegue subito la funzione all'avvio per caricare l'immagine salvata
        updatePreview();
    }

    const inputAttivo = document.getElementById('attivo');
    const badgeStatus = document.getElementById('live-status-badge');
    if (inputAttivo && badgeStatus) {
        inputAttivo.addEventListener('change', function() {
            if (this.checked) {
                badgeStatus.innerHTML = '<span class="badge-disponibile">Pubblicato</span>';
            } else {
                badgeStatus.innerHTML = '<span class="badge-esaurito">Nascosto</span>';
            }
        });
    }

    const inputQuantita = document.getElementById('quantita');
    const badgeStock = document.getElementById('live-stock-badge');
    if (inputQuantita && badgeStock) {
        inputQuantita.addEventListener('input', function() {
            const qty = parseInt(this.value);
            if (isNaN(qty) || qty === 0) {
                badgeStock.innerHTML = '<span class="badge-esaurito">Esaurito</span>';
            } else if (qty > 0 && qty <= 5) {
                badgeStock.innerHTML = '<span class="badge-scarso">In Esaurimento</span>';
            } else {
                badgeStock.innerHTML = '<span class="badge-disponibile">Disponibile</span>';
            }
        });
    }
});
</script>

<%@ include file="/jsp/common/footer.jspf" %>