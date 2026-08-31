<%-- 
    Pagina di creazione/modifica prodotti del catalogo (Area Amministrativa).
    Gestisce in modo dinamico:
    - Lo stato 'Modifica' (Edit) vs 'Nuovo Prodotto' (popolamento automatico campi e action form).
    - L'impostazione dei prezzi e dell'aliquota IVA associata.
    - L'associazione Molti-a-Molti (N:M) delle Categorie tramite chip di selezione interattivi.
    - L'anteprima client-side in tempo reale per l'immagine, lo stato dello stock e la visibilità.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Importazione della libreria JSTL Core per il controllo di flusso, cicli e condizioni --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Inclusione del frammento statico per l'intestazione HTML e i fogli di stile (head, CSS, JS) --%>
<%@ include file="/jsp/common/header.jspf" %>

<%-- Inclusione del frammento statico per la barra di navigazione principale (navbar / menu) --%>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <%-- Flag booleano: 'true' se si sta modificando un prodotto esistente, 'false' per nuovo inserimento --%>
    <c:set var="isEdit" value="${not empty prodotto}" />
    
    <%-- Titolo dinamico della pagina --%>
    <c:choose>
        <c:when test="${isEdit}">
            <h1>Modifica Prodotto</h1>
        </c:when>
        <c:otherwise>
            <h1>Nuovo Prodotto</h1>
        </c:otherwise>
    </c:choose>

    <%-- ── MESSAGGI DI ERRORE LATO SERVER ───────────────────────────────── --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            ✗ <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <div class="admin-edit-grid">
        
        <%-- ── COLONNA SINISTRA: Form di configurazione e dati prodotto ────────────────────────── --%>
        <div class="admin-form-card">
            <c:choose>
                <c:when test="${isEdit}">
                    <h2>Dettagli Prodotto</h2>
                </c:when>
                <c:otherwise>
                    <h2>Inserisci Dati</h2>
                </c:otherwise>
            </c:choose>

            <%-- Form dinamico con parametri nell'URL per evitare input hidden --%>
            <form method="post" 
                  action="${pageContext.request.contextPath}/AdminProdottoServlet?<c:choose><c:when test="${isEdit}">action=update&id=${prodotto.idProdotto}</c:when><c:otherwise>action=save</c:otherwise></c:choose>">
                
                <%-- Campo: Nome Prodotto --%>
                <div class="form-group">
                    <label for="nome">Nome Prodotto *</label>
                    <input type="text" id="nome" name="nome" class="form-control"
                           value="<c:out value='${isEdit ? prodotto.nome : ""}'/>" required placeholder="Inserisci il nome">
                </div>
            
                <%-- Riga a tre colonne: Prezzo, Aliquota IVA e Quantità in Stock --%>
                <div class="form-row">
                    <%-- Prezzo base unitario --%>
                    <div class="form-group">
                        <label for="costo">Prezzo (€) *</label>
                        <input type="number" id="costo" name="costo" class="form-control" step="0.01" min="0"
                               value="${isEdit ? prodotto.costo : ''}" required placeholder="0.00">
                    </div>

                    <%-- Aliquota IVA percentuale (Default: 22%) --%>
                    <div class="form-group">
                        <label for="iva">IVA (%) *</label>
                        <input type="number" id="iva" name="iva" class="form-control" step="0.1" min="0" max="100"
                               value="${isEdit ? prodotto.iva : '22.0'}" required>
                    </div>
            
                    <%-- Giacenza in magazzino --%>
                    <div class="form-group">
                        <label for="quantita">Quantità in Stock *</label>
                        <input type="number" id="quantita" name="quantita" class="form-control" min="0"
                               value="${isEdit ? prodotto.quantita : ''}" required placeholder="0">
                    </div>
                </div>
                
                <%-- Campo opzionale: Taglie abbigliamento/calzature --%>
                <div class="form-group form-group-spaced">
                    <label for="taglie">Taglie disponibili <span class="optional">(opzionale)</span></label>
                    <input type="text" id="taglie" name="taglie" class="form-control"
                           value="<c:out value='${isEdit ? prodotto.taglie : ""}'/>" placeholder="Es: S, M, L, XL, 42, 44">
                    <span class="field-hint">Inserisci le taglie separate da una virgola.</span>
                </div>
            
                <%-- GRIGLIA CATEGORIE A CHIP: Associazione dinamica M:N --%>
                <div class="form-group">
                    <label>Categorie associate:</label>
                    <div class="categories-wrapper">
                        <div class="categories-list">
                            <div class="categories-checkbox-grid">
                                <c:if test="${not empty categorie}">
                                    <c:forEach var="cat" items="${categorie}">
                                        <%-- Determina se la categoria corrente fa già parte delle categorie del prodotto --%>
                                        <c:set var="selezionata" value="false" />
                                        <c:if test="${isEdit}">
                                            <c:forEach var="catProd" items="${prodotto.categorie}">
                                                <c:if test="${catProd.idCategoria == cat.idCategoria}">
                                                    <c:set var="selezionata" value="true" />
                                                </c:if>
                                            </c:forEach>
                                        </c:if>
                                        
                                        <%-- Badge selezionabile per ogni categoria --%>
                                        <label class="category-chip">
                                            <input type="checkbox" name="idCategoria" value="${cat.idCategoria}" <c:if test="${selezionata}">checked</c:if>>
                                            <span><c:out value="${cat.nome}" /></span>
                                        </label>
                                    </c:forEach>
                                </c:if>
                            </div>
                            <span class="field-hint">Seleziona una o più categorie cliccando sui badge.</span>
                        </div>
                        <%-- Collegamento rapido per aggiungere una nuova categoria al database --%>
                        <a href="${pageContext.request.contextPath}/AdminCategoriaServlet?action=new" class="btn btn-sm btn-outline-warning">
                            ➕ Nuova
                        </a>
                    </div>
                </div>
            
                <%-- Campo: Descrizione estesa del prodotto --%>
                <div class="form-group">
                    <label for="descrizione">Descrizione *</label>
                    <textarea id="descrizione" name="descrizione" class="form-control" rows="4" required placeholder="Inserisci una descrizione dettagliata"><c:out value="${isEdit ? prodotto.descrizione : ''}" /></textarea>
                </div>
            
                <%-- Campo: Percorso file immagine --%>
                <div class="form-group">
                    <label for="immagine">Percorso Immagine:</label>
                    <input type="text" id="immagine" name="immagine" class="form-control"
                           value="<c:out value='${isEdit ? prodotto.immagine : "images/default.jpg"}'/>">
                    <span class="field-hint">Es: images/prodotto.jpg. L'anteprima si aggiornerà in tempo reale.</span>
                </div>
            
                <%-- Checkbox: Stato di visibilità/attivazione nel catalogo pubblico --%>
                <div class="form-group checkbox-card">
                    <input type="checkbox" id="attivo" name="attivo" class="checkbox-large"
                           <c:if test="${!isEdit || prodotto.attivo}">checked</c:if>>
                    <label for="attivo" class="checkbox-label">Prodotto Attivo (Visibile nel catalogo)</label>
                </div>
            
                <%-- Pulsanti d'azione (Conferma o Annullamento) --%>
                <div class="form-actions">
                    <c:choose>
                        <c:when test="${isEdit}">
                            <button type="submit" class="btn btn-md btn-primary">Salva Modifiche</button>
                        </c:when>
                        <c:otherwise>
                            <button type="submit" class="btn btn-md btn-primary">Crea Prodotto</button>
                        </c:otherwise>
                    </c:choose>
                    <a href="${pageContext.request.contextPath}/AdminProdottoServlet" class="btn btn-md btn-secondary">
                        Annulla
                    </a>
                </div>
            </form>
        </div>

        <%-- ── COLONNA DESTRA: Anteprima Visiva e Riepilogo dinamico (Client-Side) ────────────── --%>
        <div class="admin-preview-card">
            <h2>Anteprima Visiva</h2>
            
            <%-- Riquadro immagine di anteprima --%>
            <div class="preview-image-wrapper">
                <img id="live-preview-img" 
                     src="${pageContext.request.contextPath}/images/default.jpg" 
                     alt="Anteprima Prodotto" 
                     class="preview-image"
                     onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/default.jpg';">
            </div>

            <%-- Dettagli sintetici di anteprima --%>
            <div class="preview-details">
                <c:if test="${isEdit}">
                    <div class="detail-row">
                        <span class="detail-label">ID Prodotto</span>
                        <span class="detail-value">#<c:out value="${prodotto.idProdotto}" /></span>
                    </div>
                </c:if>
                
                <%-- Indicatore dello stato delle scorte in magazzino --%>
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

                <%-- Indicatore dello stato di pubblicazione sul sito --%>
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

            <%-- Link per visualizzare la scheda prodotto direttamente nello store (solo in modifica) --%>
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

<%-- ── SCRIPT JS PER INTERAZIONE DINAMICA E ANTEPRIME LIVE ──────────────────────────────── --%>
<script>
/**
 * Script per la gestione reattiva lato client dell'interfaccia utente (UI).
 * Permette l'aggiornamento in tempo reale dei badge di stato e dell'anteprima dell'immagine
 * durante la digitazione e l'interazione con i campi del modulo.
 */
document.addEventListener('DOMContentLoaded', function() {

    // =========================================================================
    // 1. GESTIONE REATTIVA DELL'ANTEPRIMA IMMAGINE LIVE
    // =========================================================================
    
    // Recupero dei riferimenti agli elementi del DOM necessari per l'anteprima dell'immagine
    const inputImmagine = document.getElementById('immagine');
    const previewImg = document.getElementById('live-preview-img');
    
    // Generazione del percorso radice dell'applicazione web tramite la tag EL di JSP
    const basePath = '${pageContext.request.contextPath}/';
    
    // Verifica la presenza di entrambi gli elementi nel DOM prima di configurare i listener
    if (inputImmagine && previewImg) {
        
        /**
         * Calcola e aggiorna la sorgente (src) dell'elemento <img> dell'anteprima
         * analizzando il formato dell'input inserito dall'utente.
         */
        function updatePreview() {
            // Rimuove gli spazi vuoti agli estremi e pulisce eventuali slash iniziali superflui
            let path = inputImmagine.value.trim().replace(/^\/+/, '');
            
            // CASO A: Campo di testo vuoto -> Imposta l'immagine predefinita di fallback
            if (!path) {
                previewImg.src = basePath + 'images/default.jpg';
            } 
            // CASO B: URL assoluto esterno (es. http:// o https://) -> Usa l'indirizzo originale
            else if (path.startsWith('http://') || path.startsWith('https://')) {
                previewImg.src = path;
            } 
            // CASO C: Percorso relativo locale -> Concatena la radice dell'applicazione (contextPath)
            else {
                previewImg.src = basePath + path;
            }
        }
        
        // Listener per l'aggiornamento istantaneo a ogni lettera digitata, incollata o modificata
        inputImmagine.addEventListener('input', updatePreview);
        
        // Esecuzione immediata al caricamento iniziale della pagina per mostrare l'immagine esistente
        updatePreview();
    }

    // =========================================================================
    // 2. GESTIONE DEL BADGE DI STATO VISIBILITÀ (PRODOTTO ATTIVO / NASCOSTO)
    // =========================================================================
    
    // Recupero del campo di spunta "attivo" e del relativo contenitore del badge visivo
    const inputAttivo = document.getElementById('attivo');
    const badgeStatus = document.getElementById('live-status-badge');
    
    // Procede solo se gli elementi di controllo della visibilità sono presenti nella pagina
    if (inputAttivo && badgeStatus) {
        
        // Listener per l'evento 'change', scatenato all'attivazione/disattivazione della checkbox
        inputAttivo.addEventListener('change', function(e) {
            
            // Se la spunta è attiva, applica la classe e il testo per il prodotto pubblicato
            if (e.target.checked) {
                badgeStatus.innerHTML = '<span class="badge-disponibile">Pubblicato</span>';
            } 
            // Se la spunta è assente, applica lo stato visivo per il prodotto nascosto
            else {
                badgeStatus.innerHTML = '<span class="badge-esaurito">Nascosto</span>';
            }
        });
    }

    // =========================================================================
    // 3. GESTIONE DINAMICA DEL BADGE STOCK (GIACENZA DI MAGAZZINO)
    // =========================================================================
    
    // Recupero dell'input numerico della quantità e dello span destinato al badge dello stock
    const inputQuantita = document.getElementById('quantita');
    const badgeStock = document.getElementById('live-stock-badge');
    
    // Configura i controlli di giacenza solo se entrambi gli elementi esistono nel DOM
    if (inputQuantita && badgeStock) {
        
        // Listener che monitora le variazioni numeriche nel campo quantità in tempo reale
        inputQuantita.addEventListener('input', function(e) {
            
            // Converte il testo dell'input in un numero intero base 10
            const qty = parseInt(e.target.value, 10);
            
            // SOGLIA 1: Quantità non valida, vuota o minore/uguale a zero -> Prodotto Esaurito
            if (isNaN(qty) || qty <= 0) {
                badgeStock.innerHTML = '<span class="badge-esaurito">Esaurito</span>';
            } 
            // SOGLIA 2: Giacenza limitata compresa tra 1 e 5 pezzi -> Avviso Scorta in Esaurimento
            else if (qty > 0 && qty <= 5) {
                badgeStock.innerHTML = '<span class="badge-scarso">In Esaurimento</span>';
            } 
            // SOGLIA 3: Giacenza superiore a 5 pezzi -> Prodotto Disponibile
            else {
                badgeStock.innerHTML = '<span class="badge-disponibile">Disponibile</span>';
            }
        });
    }
});
</script>

<%@ include file="/jsp/common/footer.jspf" %>

<%@ include file="/jsp/common/footer.jspf" %>