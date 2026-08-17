<%-- 
    Pagina di visualizzazione dettagliata del singolo prodotto.
    Gestisce la presentazione delle informazioni prodotto, le varianti di colore/taglia,
    l'aggiunta al carrello asincrona (AJAX) e il sistema completo di recensioni utente.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Direttive Tag Library JSTL (Core, Formattazione e Funzioni per stringhe) --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- Inclusione dei frammenti di layout condivisi: Header e Menu di Navigazione --%>
<%@ include file="/jsp/common/header.jspf" %>
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
                    <%-- Calcolo dinamico del percorso sorgente (URL assoluto vs relativo vs fallback) --%>
                    <c:choose>
                        <%-- Se il campo immagine è vuoto o nullo, imposta l'immagine placeholder predefinita --%>
                        <c:when test="${empty prodotto.immagine}">
                            <c:set var="imgSrc" value="${pageContext.request.contextPath}/images/default.jpg" />
                        </c:when>
                        <%-- Se l'immagine contiene un URL web completo (es. HTTP/HTTPS) --%>
                        <c:when test="${prodotto.immagine.startsWith('http')}">
                            <c:set var="imgSrc" value="${prodotto.immagine}" />
                        </c:when>
                        <%-- Se il percorso è relativo al server web --%>
                        <c:otherwise>
                            <c:set var="imgPath" value="${prodotto.immagine.startsWith('/') ? prodotto.immagine.substring(1) : prodotto.immagine}" />
                            <c:set var="imgSrc" value="${pageContext.request.contextPath}/${imgPath}" />
                        </c:otherwise>
                    </c:choose>

                    <%-- Tag Immagine con gestore d'errore Client (onerror) per evitare broken images --%>
                    <img class="product-image" 
                         src="${imgSrc}" 
                         alt="<c:out value='${prodotto.nome}' />"
                         onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/default.jpg';" />
                </div>

                <!-- COLONNA DESTRA: Informazioni generali e azioni d'acquisto -->
                <div class="product-details-info">
                    <%-- Titolo: Mostra il nome radice/base se è una variante, altrimenti il nome completo --%>
                    <h1>
                        <c:out value="${not empty nomeBase ? nomeBase : prodotto.nome}" />
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
                                    <%-- Estrazione del colore rimuovendo il prefisso del nome base dal nome completo della variante --%>
                                    <c:set var="colore" value="${fn:contains(v.nome, ' - ') ? fn:substringAfter(v.nome, ' - ') : v.nome}" />
                                    
                                    <%-- Bottone selettore variante con gestione stilistica (selezionato/esaurito) --%>
                                    <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${v.idProdotto}"
                                       class="variante-btn ${v.idProdotto == prodotto.idProdotto ? 'active' : ''} ${v.quantita <= 0 ? 'esaurito' : ''}">
                                        <c:out value="${colore}" />
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
                                <form method="post" action="${pageContext.request.contextPath}/CarrelloServlet" id="add-to-cart-form" class="add-to-cart-form">
                                    <input type="hidden" name="action" value="add">
                                    <input type="hidden" name="idProdotto" value="${prodotto.idProdotto}">
                                    
                                    <%-- Menu a tendina per la selezione della taglia (se il prodotto le supporta) --%>
                                    <c:if test="${not empty prodotto.taglie}">
                                        <div class="size-selector-wrapper">
                                            <label for="taglia">Seleziona Taglia:</label>
                                            <select name="taglia" id="taglia" required class="size-dropdown">
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
                                        <button type="submit" class="btn">Aggiungi al carrello</button>
                                    </div>
                                </form>

                                <!-- Elemento per notifica toast di conferma/errore aggiunta carrello -->
                                <div id="cart-toast" class="toast"></div>
                            </c:when>

                            <%-- CASO 1.3: Prodotto Esaurito a magazzino --%>
                            <c:otherwise>
                                <button class="btn btn-secondary" disabled>Esaurito</button>
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
                            <form action="${pageContext.request.contextPath}/AggiungiRecensioneServlet" method="post">
                                <input type="hidden" name="idProdotto" value="${prodotto.idProdotto}">
                                
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

                                <button type="submit" class="btn-submit-review">Invia Recensione</button>
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
                                        <span class="review-stars">
                                            <c:forEach begin="1" end="${rec.valutazione}">★</c:forEach>
                                        </span>

                                        <%-- Data e ora di pubblicazione --%>
                                        <span class="review-date">
                                            <fmt:formatDate value="${rec.dataRecensione}" pattern="dd/MM/yyyy HH:mm" />
                                        </span>

                                        <%-- Pulsante di eliminazione (mostrato solo se Admin o se l'utente è l'autore della recensione) --%>
                                        <c:if test="${isAdmin || (not empty sessionScope.utente && sessionScope.utente.idUtente == rec.idUtente)}">
                                            <form action="${pageContext.request.contextPath}/EliminaRecensioneServlet" method="post" class="delete-review-form" onsubmit="return confirm('Sei sicuro di voler eliminare questa recensione?');">
                                                <input type="hidden" name="idRecensione" value="${rec.idRecensione}">
                                                <input type="hidden" name="idProdotto" value="${prodotto.idProdotto}">
                                                <button type="submit" class="btn-delete-review">🗑️ Elimina</button>
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
                <p>Prodotto non trovato.</p>
            </div>
        </c:otherwise>
    </c:choose>

</main>

<%-- ── SCRIPT CLIENT: Gestione asincrona (AJAX / Fetch API) dell'aggiunta al carrello ──────────────── --%>
<script>
/**
 * Attende che il DOM sia completamente caricato e analizzato prima di
 * agganciare gli event listener agli elementi della pagina.
 */
document.addEventListener("DOMContentLoaded", function () {

    // 1. SELEZIONE DEGLI ELEMENTI DOM
    // Form di aggiunta al carrello
    const form = document.getElementById("add-to-cart-form");
    // Elemento contenitore per le notifiche popup temporanee (Toast)
    const toast = document.getElementById("cart-toast");

    /**
     * Helper per la gestione delle notifiche visive Toast temporanee.
     * Mostra un messaggio di successo o di errore e lo nasconde automaticamente.
     * 
     * @param {string} message - Il testo da visualizzare all'interno del toast
     * @param {boolean} isSuccess - Flag booleano: true per stile successo (verde), false per errore (rosso)
     */
    function showToast(message, isSuccess) {
        // Se l'elemento toast non è presente nel DOM, interrompe l'esecuzione per evitare errori
        if (!toast) return;

        // Imposta il contenuto testuale del toast
        toast.textContent = message;

        // Assegna la classe CSS base e quella specifica per lo stato (successo/errore)
        toast.className = "toast " + (isSuccess ? "toast-success" : "toast-error");

        // Aggiunge la classe che rende visibile il toast a schermo (es. animazione fade-in / slide-in)
        toast.classList.add("show");

        // Imposta un timer di 2.5 secondi (2500 ms) per rimuovere la classe visibile e nascondere il toast
        setTimeout(() => {
            toast.classList.remove("show");
        }, 2500);
    }

    // 2. GESTIONE EVENTO SUBMIT DEL FORM
    // Esegue il codice solo se il form d'acquisto è effettivamente presente nella pagina
    if (form) {
        form.addEventListener("submit", function (e) {
            // Blocco del comportamento predefinito del form (evita il ricaricamento completo della pagina)
            e.preventDefault();

            // Validazione client-side HTML5 (es. controlla se è stata selezionata una taglia o se la quantità è valida)
            if (!form.checkValidity()) {
                // Se la form non è valida, mostra i messaggi d'errore nativi del browser e interrompe l'invio
                form.reportValidity();
                return;
            }

            // Estrazione e formattazione dei dati del form nel formato URL-encoded (key=value&key2=value2)
            const params = new URLSearchParams(new FormData(form));

            // 3. ESECUZIONE DELLA RICHIESTA ASINCRONA (AJAX) VIA FETCH API
            fetch(form.getAttribute("action"), {
                method: "POST", // Metodo HTTP utilizzato per l'invio
                headers: {
                    // Specifica al server il tipo di contenuto inviato nel corpo della richiesta
                    "Content-Type": "application/x-www-form-urlencoded",
                    // Header personalizzato per informare lato Servlet che si tratta di una chiamata AJAX
                    "X-Requested-With": "XMLHttpRequest"
                },
                // Inserimento dei dati formattati nel corpo (body) della richiesta HTTP
                body: params.toString()
            })
            .then(async response => {
                // Estrazione del testo grezzo dalla risposta HTTP
                const text = await response.text();
                let data;

                // Tentativo di parsing del testo ricevuto in formato JSON
                try { 
                    data = JSON.parse(text); 
                } catch (err) { 
                    // Se la risposta non è un JSON valido (es. pagina d'errore HTML del server)
                    showToast("Errore di risposta dal server.", false); 
                    return; 
                }

                // GESTIONE STATO HTTP 401 (UNAUTHORIZED)
                // Se l'utente non è autenticato o la sessione è scaduta
                if (response.status === 401) { 
                    // Effettua il reindirizzamento alla pagina di login specificata dal server o a quella predefinita
                    window.location.href = data.redirect || "${pageContext.request.contextPath}/jsp/common/login.jsp"; 
                    return; 
                }

                // GESTIONE RISPOSTA DI SUCCESSO (STATO HTTP 200-299)
                if (response.ok && data.success) {
                    // Mostra la notifica di conferma operazione
                    showToast(data.message || "Prodotto aggiunto al carrello!", true);

                    // Aggiornamento dinamico dei contatori/badge del carrello presenti nell'header
                    const badges = document.querySelectorAll("#cart-count, .cart-badge, .badge-cart");
                    badges.forEach(badge => {
                        // Converte il numero di articoli ricevuti in intero
                        const count = parseInt(data.cartCount, 10) || 0;
                        // Aggiorna il testo del badge (lo svuota se il contatore è 0)
                        badge.textContent = count > 0 ? count : "";
                    });
                } else {
                    // Mostra la notifica di errore restituita dal server (es. quantità insufficiente a magazzino)
                    showToast(data.message || "Impossibile aggiungere il prodotto.", false);
                }
            })
            .catch(() => {
                // Gestione degli errori di rete o mancata risposta del server (es. offline, timeout)
                showToast("Errore di connessione.", false);
            });
        });
    }
});
</script>

<%-- Inclusione del Footer aziendale --%>
<%@ include file="/jsp/common/footer.jspf" %>