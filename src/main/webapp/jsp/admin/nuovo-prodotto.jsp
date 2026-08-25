<%-- 
    Pagina di creazione/modifica prodotti del catalogo.
    Gestisce in modo dinamico:
    - Lo stato 'Edit' vs 'Nuovo' (popolamento campi e action del form).
    - L'associazione N:M delle Categorie tramite chip di selezione.
    - L'anteprima in tempo reale dell'immagine e dello stato di visibilità/stock tramite JS.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Importazione della libreria JSTL Core per il controllo di flusso, cicli e condizioni --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Inclusione del frammento statico per l'intestazione HTML e le risorse della pagina (head, CSS, JS) --%>
<%@ include file="/jsp/common/header.jspf" %>

<%-- Inclusione del frammento statico per la barra di navigazione principale (menu / navbar) --%>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <%-- Flag booleano per distinguere tra modalità Modifica e Inserimento --%>
    <c:set var="isEdit" value="${not empty prodotto}" />
    
    <h1>${isEdit ? 'Modifica Prodotto' : 'Nuovo Prodotto'}</h1>

    <div class="admin-edit-grid">
        
        <%-- ── COLONNA SINISTRA: Form di modifica e configurazione ────────── --%>
        <div class="admin-form-card">
            <h2>${isEdit ? 'Dettagli Prodotto' : 'Inserisci Dati'}</h2>

            <form method="post" action="${pageContext.request.contextPath}/AdminProdottoServlet?action=${isEdit ? 'update' : 'save'}${isEdit ? '&id=' : ''}${isEdit ? prodotto.idProdotto : ''}">
                
                <div class="form-group">
                    <label for="nome">Nome Prodotto:</label>
                    <input type="text" id="nome" name="nome" 
                           value="<c:out value='${isEdit ? prodotto.nome : ""}'/>" required>
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
                
                <div class="form-group form-group-spaced">
                    <label for="taglie">Taglie disponibili (opzionale):</label>
                    <input type="text" id="taglie" name="taglie" 
                           value="<c:out value='${isEdit ? prodotto.taglie : ""}'/>" placeholder="Es: S, M, L, XL, 42, 44">
                    <span class="field-hint">Inserisci le taglie separate da una virgola.</span>
                </div>
            
                <%-- GRIGLIA CATEGORIE A CHIP: Associazione dinamica --%>
                <div class="form-group">
                    <label>Categorie associate:</label>
                    <div class="categories-wrapper">
                        <div class="categories-list">
                            <div class="categories-checkbox-grid">
                                <c:if test="${not empty categorie}">
                                    <c:forEach var="cat" items="${categorie}">
                                        <%-- Logica di controllo per selezionare la checkbox se la categoria è già associata --%>
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
                        <a href="${pageContext.request.contextPath}/AdminCategoriaServlet?action=new" class="btn btn-sm btn-outline-warning">
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
                           value="<c:out value='${isEdit ? prodotto.immagine : "images/default.jpg"}'/>">
                    <span class="field-hint">Es: images/prodotto.jpg. L'anteprima si aggiornerà in tempo reale.</span>
                </div>
            
                <div class="form-group checkbox-card">
                    <input type="checkbox" id="attivo" name="attivo" class="checkbox-large"
                           ${(!isEdit || prodotto.attivo) ? 'checked' : ''}>
                    <label for="attivo" class="checkbox-label">Prodotto Attivo (Visibile nel catalogo)</label>
                </div>
            
                <div class="form-actions">
                    <button type="submit" class="btn btn-md btn-primary">
                        ${isEdit ? 'Salva Modifiche' : 'Crea Prodotto'}
                    </button>
                    <a href="${pageContext.request.contextPath}/AdminProdottoServlet" class="btn btn-md btn-secondary">
                        Annulla
                    </a>
                </div>
            </form>
        </div>

        <%-- ── COLONNA DESTRA: Anteprima Visiva e Riepilogo (Client-Side) ──── --%>
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
                        <span class="detail-value">#<c:out value="${prodotto.idProdotto}" /></span>
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
                    <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${prodotto.idProdotto}" target="_blank" class="btn btn-sm btn-outline-warning">
                        Vedi nel Negozio ↗
                    </a>
                </div>
            </c:if>
        </div>
    </div>
</main>

<%-- ── SCRIPT JS PER INTERAZIONE DINAMICA E ANTEPRIME LIVE ────────────── --%>
<script>
document.addEventListener('DOMContentLoaded', () => {

    // ── 1. GESTIONE ANTEPRIMA IMMAGINE LIVE ──────────────────────────────────
    const inputImmagine = document.getElementById('immagine');
    const previewImg = document.getElementById('live-preview-img');
    // Recupera dinamicamente il context path dell'applicazione web tramite EL (Expression Language)
    const basePath = '${pageContext.request.contextPath}/';
    
    if (inputImmagine && previewImg) {
        /**
         * Funzione interna che calcola e aggiorna l'attributo `src` dell'immagine di anteprima
         * normalizzando il percorso inserito dall'utente.
         */
        const updatePreview = () => {
            // Pulisce gli spazi bianchi e rimuove eventuali slash iniziali superflui
            let path = inputImmagine.value.trim().replace(/^\/+/, '');
            
            if (!path) {
                // Se il campo è vuoto, imposta l'immagine di fallback predefinita
                previewImg.src = basePath + 'images/default.jpg';
            } else if (path.startsWith('http://') || path.startsWith('https://')) {
                // Se è un URL assoluto esterno, lo usa direttamente
                previewImg.src = path;
            } else {
                // Se è un percorso relativo locale, lo concatena correttamente al context path
                previewImg.src = basePath + path;
            }
        };
        
        // Ascolta l'evento di digitazione (input) nel campo di testo del percorso immagine
        inputImmagine.addEventListener('input', updatePreview);
        
        // Esegue la funzione immediatamente al caricamento della pagina (utile in modalità 'Edit')
        updatePreview();
    }

    // ── 2. GESTIONE CAMBIO VISIBILITÀ (ATTIVO/INATTIVO) ─────────────────────
    const inputAttivo = document.getElementById('attivo');
    const badgeStatus = document.getElementById('live-status-badge');
    
    if (inputAttivo && badgeStatus) {
        // Ascolta l'evento 'change' sulla checkbox dello stato di attivazione del prodotto
        inputAttivo.addEventListener('change', (e) => {
            // Modifica dinamicamente l'HTML del badge a seconda che la checkbox sia spuntata o meno
            badgeStatus.innerHTML = e.target.checked 
                ? '<span class="badge-disponibile">Pubblicato</span>' 
                : '<span class="badge-esaurito">Nascosto</span>';
        });
    }

    // ── 3. GESTIONE DINAMICA DEL BADGE STOCK (QUANTITÀ) ──────────────────────
    const inputQuantita = document.getElementById('quantita');
    const badgeStock = document.getElementById('live-stock-badge');
    
    if (inputQuantita && badgeStock) {
        // Ascolta l'evento di digitazione nel campo numerico della quantità in stock
        inputQuantita.addEventListener('input', (e) => {
            // Converte il valore inserito in un intero in base 10
            const qty = parseInt(e.target.value, 10);
            
            // Valuta le soglie di stock per aggiornare visivamente il badge di avviso
            if (isNaN(qty) || qty <= 0) {
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