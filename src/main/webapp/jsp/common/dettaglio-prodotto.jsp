<%-- 
    Pagina di visualizzazione dettagliata del singolo prodotto.
    Gestisce la presentazione delle informazioni prodotto, le varianti di colore/taglia,
    l'aggiunta al carrello asincrona (AJAX) e il sistema completo di recensioni utente.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Importazione della libreria JSTL Core per il controllo di flusso, cicli e condizioni --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Importazione della libreria JSTL Formatting per la formattazione di date, numeri e valute --%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Importazione della libreria JSTL Functions per la manipolazione di stringhe e collezioni --%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- Inclusione del frammento statico per l'intestazione HTML e le risorse della pagina (head, CSS, JS) --%>
<%@ include file="/jsp/common/header.jspf" %>

<%-- Inclusione del frammento statico per la barra di navigazione principale (menu / navbar) --%>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container product-details-container">

    <%-- Controllo sull'esistenza e validità del bean prodotto passato dalla Servlet --%>
    <c:choose>
        <%-- ========================================================================= --%>
        <%-- CASO 1: Il prodotto esiste ed è stato caricato correttamente              --%>
        <%-- ========================================================================= --%>
        <c:when test="${not empty prodotto}">
            
            <!-- SCHEDA DETTAGLI PRODOTTO -->
            <div class="product-details-card">
                
                <!-- COLONNA SINISTRA: Gestione dell'immagine del prodotto -->
                <div class="product-image-wrapper">
                    <%-- Imposta l'immagine del prodotto o il fallback senza operatore ternario --%>
                    <c:choose>
                        <c:when test="${not empty prodotto.immagine}">
                            <c:set var="imgSrc" value="${prodotto.immagine}" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="imgSrc" value="images/default.jpg" />
                        </c:otherwise>
                    </c:choose>

                    <%-- Tag Immagine con gestore d'errore Client (onerror) per evitare broken images --%>
                    <img class="product-image" 
                         src="${pageContext.request.contextPath}/${imgSrc}" 
                         alt="<c:out value='${prodotto.nome}' />"
                         onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/default.jpg';" />
                </div>

                <!-- COLONNA DESTRA: Informazioni generali e azioni d'acquisto -->
                <div class="product-details-info">
                    <%-- Titolo: Mostra il nome radice/base se è una variante, altrimenti il nome completo --%>
                    <h1>
                        <c:choose>
                            <c:when test="${not empty nomeBase}">
                                <c:out value="${nomeBase}" />
                            </c:when>
                            <c:otherwise>
                                <c:out value="${prodotto.nome}" />
                            </c:otherwise>
                        </c:choose>
                    </h1>

                    <%-- Descrizione estesa del prodotto --%>
                    <p class="product-description"><c:out value="${prodotto.descrizione}" /></p>
                    
                    <%-- Formattazione valuta per il prezzo (€) --%>
                    <p class="product-price">
                        Prezzo: <span><fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" /></span>
                    </p>

                    <%-- Sezione Selezione Varianti (es. Colori correlati) --%>
                    <c:if test="${not empty varianti}">
                        <div class="varianti-wrapper">
                            <p class="varianti-label">Colore:</p>
                            <div class="varianti-list">
                                <c:forEach var="v" items="${varianti}">
                                    <%-- Bottone selettore variante senza ternario nelle classi --%>
                                    <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${v.idProdotto}"
                                       class="variante-btn <c:if test='${v.idProdotto == prodotto.idProdotto}'>active</c:if> <c:if test='${v.quantita <= 0}'>esaurito</c:if>">
                                        <c:out value="${fn:substringAfter(v.nome, ' - ')}" />
                                    </a>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>

                    <%-- Lista delle Categorie di appartenenza separata da virgole --%>
                    <c:if test="${not empty prodotto.categorie}">
                        <p class="product-categories">Categorie: 
                            <c:forEach var="cat" items="${prodotto.categorie}" varStatus="s">
                                <c:out value="${cat.nome}" /><c:if test="${!s.last}">, </c:if>
                            </c:forEach>
                        </p>
                    </c:if>

                    <!-- BLOCCO FORM D'ACQUISTO -->
                    <div class="product-actions-wrapper">
                        <c:choose>
                            <%-- CASO 1.1: L'utente loggato è un Amministratore (acquisto inibito) --%>
                            <c:when test="${isAdmin}">
                                <div class="admin-notice">
                                    🔒 Gli amministratori non possono acquistare prodotti dal catalogo.
                                </div>
                            </c:when>

                            <%-- CASO 1.2: Il prodotto è in stock e acquistabile --%>
                            <c:when test="${prodotto.quantita > 0}">
                                <%-- Parametri inviati direttamente nell'URL della form (senza hidden) --%>
                                <form method="post" action="${pageContext.request.contextPath}/CarrelloServlet?action=add&idProdotto=${prodotto.idProdotto}" id="add-to-cart-form" class="form-unstyled">
                                    
                                    <%-- Menu a tendina per la selezione della taglia (se il prodotto le supporta) --%>
                                    <c:if test="${not empty prodotto.taglie}">
                                        <div class="size-selector-wrapper">
                                            <label for="taglia">Seleziona Taglia:</label>
                                            <select name="taglia" id="taglia" required class="size-dropdown form-control">
                                                <option value="">-- Scegli --</option>
                                                <%-- Suddivisione della stringa taglie (es. "S,M,L") in un array interabile --%>
                                                <c:forEach var="t" items="${fn:split(prodotto.taglie, ',')}">
                                                    <c:set var="cleanTaglia" value="${fn:trim(t)}" />
                                                    <option value="<c:out value='${cleanTaglia}' />"><c:out value="${cleanTaglia}" /></option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                    </c:if>

                                    <%-- Input Quantità (limitato alla giacenza di magazzino) e pulsante d'invio --%>
                                    <div class="qty-submit-wrapper">
                                        <label for="quantita">Quantità:</label>
                                        <input type="number" id="quantita" name="quantita" value="1" min="1" max="${prodotto.quantita}" class="input-qty">
                                        <button type="submit" class="btn btn-md btn-primary btn-full">Aggiungi al carrello</button>
                                    </div>
                                </form>

                                <!-- Elemento per notifica toast di conferma/errore aggiunta carrello -->
                                <div id="cart-toast" class="toast"></div>
                            </c:when>

                            <%-- CASO 1.3: Prodotto Esaurito a magazzino --%>
                            <c:otherwise>
                                <button class="btn btn-md btn-secondary" disabled>Esaurito</button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

            </div>

            <hr class="section-divider">
            
            <!-- SEZIONE RECENSIONI -->
            <section class="reviews-section">
                <h2>Recensioni del prodotto</h2>

                <!-- FORM DI AGGIUNTA RECENSIONE -->
                <c:choose>
                    <%-- Abilitato per gli utenti registrati che NON sono admin --%>
                    <c:when test="${not empty sessionScope.utente && !isAdmin}">
                        <div class="review-form-card">
                            <h3>Lascia una recensione</h3>
                            <form action="${pageContext.request.contextPath}/AggiungiRecensioneServlet?idProdotto=${prodotto.idProdotto}" method="post">
                                
                                <%-- Selezione Valutazione in Stelle --%>
                                <div class="form-group">
                                    <label for="valutazione">Voto:</label>
                                    <select name="valutazione" id="valutazione" class="form-control" required>
                                        <option value="5">⭐⭐⭐⭐⭐ (5/5)</option>
                                        <option value="4">⭐⭐⭐⭐ (4/5)</option>
                                        <option value="3">⭐⭐⭐ (3/5)</option>
                                        <option value="2">⭐⭐ (2/5)</option>
                                        <option value="1">⭐ (1/5)</option>
                                    </select>
                                </div>

                                <%-- Testo della recensione --%>
                                <div class="form-group">
                                    <label for="descrizione">La tua opinione:</label>
                                    <textarea name="descrizione" id="descrizione" class="form-control" rows="4" required placeholder="Scrivi una recensione..."></textarea>
                                </div>

                                <button type="submit" class="btn btn-md btn-primary">Invia Recensione</button>
                            </form>
                        </div>
                    </c:when>

                    <%-- Prompt d'invito al login se l'utente è un ospite --%>
                    <c:when test="${empty sessionScope.utente}">
                        <p class="review-login-prompt">
                            <a href="${pageContext.request.contextPath}/jsp/common/login.jsp">Accedi</a> per inserire una recensione.
                        </p>
                    </c:when>
                </c:choose>

                <!-- ELENCO DELLE RECENSIONI PUBBLICATE -->
                <c:choose>
                    <c:when test="${not empty recensioni}">
                        <div class="reviews-list">
                            <c:forEach var="rec" items="${recensioni}">
                                <div class="review-item">
                                    <div class="review-meta">
                                        <%-- Info Autore --%>
                                        <span class="review-author">
                                            <c:out value="${rec.nomeUtente}" /> <c:out value="${rec.cognomeUtente}" />
                                            <small class="review-email">(<c:out value="${rec.emailUtente}" />)</small>
                                        </span>

                                        <%-- Render stelle numeriche in simboli grafici --%>
                                        <span class="review-stars stars">
                                            <c:forEach begin="1" end="${rec.valutazione}">★</c:forEach>
                                        </span>

                                        <%-- Data e ora di pubblicazione --%>
                                        <span class="review-date">
                                            <fmt:formatDate value="${rec.dataRecensione}" pattern="dd/MM/yyyy HH:mm" />
                                        </span>

                                        <%-- Pulsante di eliminazione (mostrato solo se Admin o se l'utente è l'autore della recensione) --%>
                                        <c:if test="${isAdmin || (not empty sessionScope.utente && sessionScope.utente.idUtente == rec.idUtente)}">
                                            <form action="${pageContext.request.contextPath}/EliminaRecensioneServlet?idRecensione=${rec.idRecensione}&idProdotto=${prodotto.idProdotto}" method="post" class="form-unstyled" onsubmit="return confirm('Sei sicuro di voler eliminare questa recensione?');">
                                                <button type="submit" class="btn btn-sm btn-outline-danger">🗑️ Elimina</button>
                                            </form>
                                        </c:if>
                                    </div>
                                    
                                    <%-- Testo del commento --%>
                                    <p class="review-text"><c:out value="${rec.descrizione}" /></p>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>

                    <%-- Nessuna recensione trovata per il prodotto --%>
                    <c:otherwise>
                        <p class="no-reviews-msg">Nessuna recensione presente per questo prodotto. Sii il primo a recensirlo!</p>
                    </c:otherwise>
                </c:choose>
            </section>
        </c:when>

        <%-- ========================================================================= --%>
        <%-- CASO 2: Prodotto non trovato nel DB o disattivato                        --%>
        <%-- ========================================================================= --%>
        <c:otherwise>
            <div class="empty-state">
                <div class="empty-state-icon">
                    <img src="${pageContext.request.contextPath}/images/icons/search.svg" alt="Prodotto non trovato" onerror="this.style.display='none';" />
                </div>
                <h2>Prodotto non trovato</h2>
                <p>Il prodotto richiesto non è disponibile o è stato rimosso.</p>
            </div>
        </c:otherwise>
    </c:choose>

</main>

<%-- ── SCRIPT CLIENT: Gestione asincrona (AJAX / Fetch API) dell'aggiunta al carrello ──────────────── --%>
<script>
// Attende che il DOM sia completamente caricato prima di eseguire lo script
document.addEventListener("DOMContentLoaded", function () {

    // 1. SELEZIONE DEGLI ELEMENTI DEL DOM
    const form = document.getElementById("add-to-cart-form"); // Form di aggiunta al carrello
    const toast = document.getElementById("cart-toast");       // Elemento HTML per i messaggi popup (toast)

    /**
     * Mostra una notifica popup temporanea (Toast) a schermo.
     * 
     * @param {string} message - Il testo del messaggio da visualizzare
     * @param {boolean} isSuccess - true per notifica di successo (verde), false per errore (rosso)
     */
    function showToast(message, isSuccess) {
        if (!toast) return; // Se l'elemento toast non esiste nel DOM, esce subito

        // Inserisce il testo del messaggio nel contenitore
        toast.textContent = message;

        // Imposta le classi CSS per lo stile dinamico senza operatore ternario
        if (isSuccess) {
            toast.className = "toast toast-success";
        } else {
            toast.className = "toast toast-error";
        }

        // Rende visibile il toast aggiungendo la classe .show (avvia animazione CSS)
        toast.classList.add("show");

        // Programma la scomparsa del toast dopo 2,5 secondi (2500 millisecondi)
        setTimeout(() => {
            toast.classList.remove("show");
        }, 2500);
    }

    // 2. GESTIONE DELL'EVENTO DI INVIO DEL FORM (SUBMIT)
    if (form) {
        form.addEventListener("submit", function (e) {
            // Impedisce l'invio standard della form che ricaricherebbe l'intera pagina
            e.preventDefault();

            // Esegue la validazione client-side HTML5 (es. campo taglia obbligatorio)
            if (!form.checkValidity()) {
                form.reportValidity(); // Mostra i fumetti d'errore nativi del browser
                return;                // Interrompe l'invio
            }

            // Converte i campi inseriti nella form nel formato URL-encoded (chiave=valore&chiave2=valore2)
            const params = new URLSearchParams(new FormData(form));

            // 3. INVIO ASINCRONO DEI DATI AL SERVER VIA FETCH API (AJAX)
            fetch(form.getAttribute("action"), {
                method: "POST",
                headers: {
                    // Specifica la codifica dei dati inviati
                    "Content-Type": "application/x-www-form-urlencoded",
                    // Segnala lato server (alla Servlet) che si tratta di una richiesta AJAX
                    "X-Requested-With": "XMLHttpRequest"
                },
                body: params.toString() // Inserisce i parametri nel corpo della richiesta HTTP
            })
            .then(async response => {
                // Legge il testo grezzo restituito dalla Servlet
                const text = await response.text();
                let data;

                // Tenta di convertire la stringa ricevuta in un oggetto JSON valido
                try { 
                    data = JSON.parse(text); 
                } catch (err) { 
                    // Mostra errore se la risposta dal server non è in formato JSON
                    showToast("Errore di risposta dal server.", false); 
                    return; 
                }

                // GESTIONE UTENTE NON LOGGATO (Codice HTTP 401 Unauthorized)
                if (response.status === 401) { 
                    // Reindirizza l'utente alla pagina di login senza ternario
                    let redirectUrl = "${pageContext.request.contextPath}/jsp/common/login.jsp";
                    if (data && data.redirect) {
                        redirectUrl = data.redirect;
                    }
                    window.location.href = redirectUrl; 
                    return; 
                }

                // GESTIONE ESITO POSITIVO (Codice HTTP 200 OK e success = true)
                if (response.ok && data.success) {
                    // Mostra notifica di successo
                    let msg = "Prodotto aggiunto al carrello!";
                    if (data.message) {
                        msg = data.message;
                    }
                    showToast(msg, true);

                    // Aggiorna l'elemento badge del carrello presente nell'header
                    const badge = document.getElementById("cart-count");
                    if (badge) {
                        const count = parseInt(data.cartCount, 10) || 0;
                        if (count > 0) {
                            badge.textContent = count;
                        } else {
                            badge.textContent = "";
                        }
                    }
                } else {
                    // Mostra il messaggio d'errore inviato dalla Servlet (es. quantità esaurita)
                    let errorMsg = "Impossibile aggiungere il prodotto.";
                    if (data && data.message) {
                        errorMsg = data.message;
                    }
                    showToast(errorMsg, false);
                }
            })
            .catch(() => {
                // Gestisce eventuali cadute di connessione o problemi di rete
                showToast("Errore di connessione.", false);
            });
        });
    }
});
</script>

<%-- Inclusione del Footer aziendale --%>
<%@ include file="/jsp/common/footer.jspf" %>