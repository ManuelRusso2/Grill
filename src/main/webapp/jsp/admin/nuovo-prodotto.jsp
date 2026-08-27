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
    <%-- Flag booleano: 'true' se si sta modificando un prodotto esistente (oggetto 'prodotto' presente nella Request), 'false' per nuovo inserimento --%>
    <c:set var="isEdit" value="${not empty prodotto}" />
    
    <%-- Titolo dinamico della pagina in base alla modalità corrente --%>
    <h1>${isEdit ? 'Modifica Prodotto' : 'Nuovo Prodotto'}</h1>

    <div class="admin-edit-grid">
        
        <%-- ── COLONNA SINISTRA: Form di configurazione e dati prodotto ────────────────────────── --%>
        <div class="admin-form-card">
            <h2>${isEdit ? 'Dettagli Prodotto' : 'Inserisci Dati'}</h2>

            <%-- Form dinamico: reindirizza alla servlet passando action=update (con ID) o action=save --%>
            <form method="post" action="${pageContext.request.contextPath}/AdminProdottoServlet?action=${isEdit ? 'update' : 'save'}${isEdit ? '&id=' : ''}${isEdit ? prodotto.idProdotto : ''}">
                
                <%-- Campo: Nome Prodotto --%>
                <div class="form-group">
                    <label for="nome">Nome Prodotto:</label>
                    <input type="text" id="nome" name="nome" class="form-control"
                           value="<c:out value='${isEdit ? prodotto.nome : ""}'/>" required>
                </div>
            
                <%-- Riga a tre colonne: Prezzo, Aliquota IVA e Quantità in Stock --%>
                <div class="form-row">
                    <%-- Prezzo base unitario --%>
                    <div class="form-group">
                        <label for="costo">Prezzo (€):</label>
                        <input type="number" id="costo" name="costo" class="form-control" step="0.01" min="0"
                               value="${isEdit ? prodotto.costo : ''}" required>
                    </div>

                    <%-- Aliquota IVA percentuale (Default: 22%) --%>
                    <div class="form-group">
                        <label for="iva">IVA (%):</label>
                        <input type="number" id="iva" name="iva" class="form-control" step="0.1" min="0" max="100"
                               value="${isEdit ? prodotto.iva : '22.0'}" required>
                    </div>
            
                    <%-- Giacenza in magazzino --%>
                    <div class="form-group">
                        <label for="quantita">Quantità in Stock:</label>
                        <input type="number" id="quantita" name="quantita" class="form-control" min="0"
                               value="${isEdit ? prodotto.quantita : ''}" required>
                    </div>
                </div>
                
                <%-- Campo opzionale: Taglie abbigliamento/calzature --%>
                <div class="form-group form-group-spaced">
                    <label for="taglie">Taglie disponibili (opzionale):</label>
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
                                            <input type="checkbox" name="idCategoria" value="${cat.idCategoria}" ${selezionata ? 'checked' : ''}>
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
                    <label for="descrizione">Descrizione:</label>
                    <textarea id="descrizione" name="descrizione" class="form-control" rows="4" required><c:out value="${isEdit ? prodotto.descrizione : ''}" /></textarea>
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
                           ${(!isEdit || prodotto.attivo) ? 'checked' : ''}>
                    <label for="attivo" class="checkbox-label">Prodotto Attivo (Visibile nel catalogo)</label>
                </div>
            
                <%-- Pulsanti d'azione (Conferma o Annullamento) --%>
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
// Registra un ascoltatore sull'evento 'DOMContentLoaded': assicura che il codice JS venga eseguito
// solo dopo che l'intera struttura dell'albero HTML della pagina è stata caricata completamente in memoria.
document.addEventListener('DOMContentLoaded', function() {

    // =========================================================================
    // 1. GESTIONE DELL'ANTEPRIMA IMMAGINE LIVE
    // =========================================================================
    
    // Recupera dall'HTML i riferimenti al campo di testo del percorso e all'elemento tag <img> di anteprima
    const inputImmagine = document.getElementById('immagine');
    const previewImg = document.getElementById('live-preview-img');
    
    // Recupera il percorso base del progetto (Context Path) tramite Expression Language di JSP (EL)
    const basePath = '${pageContext.request.contextPath}/';
    
    // Esegue il blocco solo se entrambi gli elementi esistono nell'interfaccia HTML
    if (inputImmagine && previewImg) {

        /**
         * Funzione che calcola il percorso corretto dell'immagine e ne aggiorna la sorgente (src)
         */
        function updatePreview() {
            // Elimina gli spazi bianchi e rimuove eventuali caratteri '/' inseriti all'inizio dall'utente
            let path = inputImmagine.value.trim().replace(/^\/+/, '');
            
            // CASO A: Il campo di testo è completamente vuoto
            if (!path) {
                // Imposta l'immagine predefinita di fallback del sito
                previewImg.src = basePath + 'images/default.jpg';
            } 
            // CASO B: L'utente ha inserito un URL completo da un sito esterno (http:// o https://)
            else if (path.startsWith('http://') || path.startsWith('https://')) {
                // Assegna direttamente l'URL esterno all'attributo 'src' dell'immagine
                previewImg.src = path;
            } 
            // CASO C: L'utente ha inserito un percorso relativo locale (es: "images/prodotti/foto.jpg")
            else {
                // Concatena il Context Path del progetto per costruire il percorso assoluto valido sul server
                previewImg.src = basePath + path;
            }
        }
        
        // Collega la funzione all'evento 'input': scatta istantaneamente a ogni singola lettera digitata o incollata
        inputImmagine.addEventListener('input', updatePreview);
        
        // Invoca la funzione al caricamento iniziale della pagina (fondamentale in modalità "Modifica" per mostrare la foto esistente)
        updatePreview();
    }

    // =========================================================================
    // 2. GESTIONE CAMBIO STATO VISIBILITÀ (PRODOTTO ATTIVO / NASCOSTO)
    // =========================================================================
    
    // Recupera la checkbox dello stato 'attivo' e lo span in cui stampare il badge di visibilità
    const inputAttivo = document.getElementById('attivo');
    const badgeStatus = document.getElementById('live-status-badge');
    
    if (inputAttivo && badgeStatus) {
        
        // Ascolta l'evento 'change' che scatta quando la checkbox viene spuntata o deselezionata dall'utente
        inputAttivo.addEventListener('change', function(e) {
            
            // Verifica lo stato del campo spuntato
            if (e.target.checked) {
                // Se la spunta è presente, mostra il badge verde "Pubblicato"
                badgeStatus.innerHTML = '<span class="badge-disponibile">Pubblicato</span>';
            } else {
                // Se la spunta è assente, mostra il badge rosso "Nascosto"
                badgeStatus.innerHTML = '<span class="badge-esaurito">Nascosto</span>';
            }
        });
    }

    // =========================================================================
    // 3. GESTIONE DINAMICA DEL BADGE STOCK (CALCOLO SOGLIE GIACENZA)
    // =========================================================================
    
    // Recupera l'input numerico della quantità e lo span per il badge di giacenza
    const inputQuantita = document.getElementById('quantita');
    const badgeStock = document.getElementById('live-stock-badge');
    
    if (inputQuantita && badgeStock) {
        
        // Ascolta la digitazione nel campo numerico delle quantità in magazzino
        inputQuantita.addEventListener('input', function(e) {
            
            // Converte il valore letto dall'input di testo in un numero intero (in base 10)
            const qty = parseInt(e.target.value, 10);
            
            // VALUTAZIONE DELLE SOGLIE LOGICHE DI STOCK:
            
            // 1. Se il valore non è un numero valido oppure è minore o uguale a zero
            if (isNaN(qty) || qty <= 0) {
                badgeStock.innerHTML = '<span class="badge-esaurito">Esaurito</span>';
            } 
            // 2. Se la quantità è compresa tra 1 e 5 pezzi (Soglia di avviso scorte basse)
            else if (qty > 0 && qty <= 5) {
                badgeStock.innerHTML = '<span class="badge-scarso">In Esaurimento</span>';
            } 
            // 3. Se la quantità è superiore a 5 pezzi (Disponibilità piena)
            else {
                badgeStock.innerHTML = '<span class="badge-disponibile">Disponibile</span>';
            }
        });
    }
});
</script>

<%@ include file="/jsp/common/footer.jspf" %>