<%-- 
    Pagina di visualizzazione dettagliata del singolo prodotto.
    Mostra le informazioni principali del prodotto (immagine, nome, prezzo, descrizione),
    le varianti di colore e taglia, il form di aggiunta al carrello (gestito via AJAX),
    e la sezione delle recensioni degli utenti con form di inserimento ed eliminazione per gli admin.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Tag Library JSTL per il controllo di flusso, la formattazione e le funzioni su stringhe --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- Inclusione dei frammenti statici di Header e Barra di Navigazione --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container product-details-container">

    <c:choose>
        <%-- CASO 1: Il prodotto è stato caricato correttamente --%>
        <c:when test="${not empty prodotto}">
            
            <%-- ── CARD PRINCIPALE DETTAGLI PRODOTTO ───────────────────────────── --%>
            <div class="product-details-card">
                
                <%-- COLONNA SINISTRA: Visualizzazione dell'immagine prodotto --%>
                <div class="product-image-wrapper">
                    <%-- Risoluzione dinamica del percorso immagine (default, URL assoluto o relativo) --%>
                    <c:choose>
                        <c:when test="${empty prodotto.immagine}">
                            <c:set var="imgSrc" value="${pageContext.request.contextPath}/images/default.jpg" />
                        </c:when>
                        <c:when test="${prodotto.immagine.startsWith('http')}">
                            <c:set var="imgSrc" value="${prodotto.immagine}" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="imgPath" value="${prodotto.immagine.startsWith('/') ? prodotto.immagine.substring(1) : prodotto.immagine}" />
                            <c:set var="imgSrc" value="${pageContext.request.contextPath}/${imgPath}" />
                        </c:otherwise>
                    </c:choose>

                    <%-- Immagine con fallback su default.jpg in caso di errore di caricamento --%>
                    <img class="product-image" 
                         src="${imgSrc}" 
                         alt="<c:out value='${prodotto.nome}' />"
                         onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/default.jpg';" />
                </div>

                <%-- COLONNA DESTRA: Dettagli informativi e selettori d'acquisto --%>
                <div class="product-details-info">
                    <%-- Titolo principale (mostra 'nomeBase' se presente, altrimenti il nome del prodotto) --%>
                    <h1>
                        <c:out value="${not empty nomeBase ? nomeBase : prodotto.nome}" />
                    </h1>

                    <%-- Descrizione estesa del prodotto --%>
                    <p class="product-description"><c:out value="${prodotto.descrizione}" /></p>
                    
                    <%-- Prezzo di vendita formattato in Euro (€) --%>
                    <p class="product-price">
                        Prezzo: <span><fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" /></span>
                    </p>

                    <%-- Elenco delle varianti di colore disponibili --%>
                    <c:if test="${not empty varianti}">
                        <div class="varianti-wrapper">
                            <p class="varianti-label">Colore:</p>
                            <div class="varianti-list">
                                <c:forEach var="v" items="${varianti}">
                                    <%-- Estrazione del colore dal nome composto --%>
                                    <c:set var="colore" value="${fn:contains(v.nome, ' - ') ? fn:substringAfter(v.nome, ' - ') : v.nome}" />
                                    <%-- Pulsante della variante con stato attivo ed eventuale evidenziazione esaurito --%>
                                    <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${v.idProdotto}"
                                       class="variante-btn ${v.idProdotto == prodotto.idProdotto ? 'active' : ''} ${v.quantita <= 0 ? 'esaurito' : ''}">
                                        <c:out value="${colore}" />
                                    </a>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>

                    <%-- Elenco delle categorie associate al prodotto --%>
                    <c:if test="${not empty prodotto.categorie}">
                        <p class="product-categories">Categorie: 
                            <c:forEach var="cat" items="${prodotto.categorie}" varStatus="s">
                                <c:out value="${cat.nome}" /><c:if test="${!s.last}">, </c:if>
                            </c:forEach>
                        </p>
                    </c:if>

                    <%-- ── FORM DI ACQUISTO E NOTIFICHE ────────────────────────────── --%>
                    <div class="product-actions-wrapper">
                        <c:choose>
                            <%-- Gli utenti amministratori non possono effettuare ordini dal catalogo --%>
                            <c:when test="${isAdmin}">
                                <div class="admin-notice">
                                    🔒 Gli amministratori non possono acquistare prodotti dal catalogo.
                                </div>
                            </c:when>

                            <%-- Prodotto disponibile a magazzino --%>
                            <c:when test="${prodotto.quantita > 0}">
                                <form method="post" action="${pageContext.request.contextPath}/CarrelloServlet" id="add-to-cart-form" class="add-to-cart-form">
                                    <input type="hidden" name="action" value="add">
                                    <input type="hidden" name="idProdotto" value="${prodotto.idProdotto}">
                                    
                                    <%-- Selettore taglie dinamico se presenti sul prodotto --%>
                                    <c:if test="${not empty prodotto.taglie}">
                                        <div class="size-selector-wrapper">
                                            <label for="taglia">Seleziona Taglia:</label>
                                            <select name="taglia" id="taglia" required class="size-dropdown">
                                                <option value="">-- Scegli --</option>
                                                <%-- Suddivisione delle taglie dalla stringa separata da virgole --%>
                                                <c:forEach var="t" items="${fn:split(prodotto.taglie, ',')}">
                                                    <c:set var="cleanTaglia" value="${fn:trim(t)}" />
                                                    <option value="<c:out value='${cleanTaglia}' />"><c:out value="${cleanTaglia}" /></option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                    </c:if>

                                    <%-- Campo quantità e pulsante di inserimento nel carrello --%>
                                    <div class="qty-submit-wrapper">
                                        <label for="quantita">Quantità:</label>
                                        <input type="number" id="quantita" name="quantita" value="1" min="1" max="${prodotto.quantita}" class="input-qty">
                                        <button type="submit" class="btn">Aggiungi al carrello</button>
                                    </div>
                                </form>

                                <%-- Contenitore per le notifiche popup (Toast) --%>
                                <div id="cart-toast" class="toast"></div>
                            </c:when>

                            <%-- Prodotto esaurito --%>
                            <c:otherwise>
                                <button class="btn btn-secondary" disabled>Esaurito</button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

            </div>

            <hr class="section-divider">
            
            <%-- ── SEZIONE RECENSIONI UTENTI ────────────────────────────────────── --%>
            <section class="reviews-section">
                <h2>Recensioni del prodotto</h2>

                <%-- Form per la creazione di una nuova recensione --%>
                <c:choose>
                    <%-- Utente autenticato non admin: mostra la scheda di inserimento --%>
                    <c:when test="${not empty sessionScope.utente && !isAdmin}">
                        <div class="review-form-card">
                            <h3>Lascia una recensione</h3>
                            <form action="${pageContext.request.contextPath}/AggiungiRecensioneServlet" method="post">
                                <input type="hidden" name="idProdotto" value="${prodotto.idProdotto}">
                                
                                <%-- Selezione punteggio in stelle --%>
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

                                <%-- Testo descrittivo della recensione --%>
                                <div class="form-group">
                                    <label for="descrizione">La tua opinione:</label>
                                    <textarea name="descrizione" id="descrizione" class="form-control" rows="4" required placeholder="Scrivi una recensione..."></textarea>
                                </div>

                                <button type="submit" class="btn-submit-review">Invia Recensione</button>
                            </form>
                        </div>
                    </c:when>

                    <%-- Utente non autenticato: prompt per l'accesso --%>
                    <c:when test="${empty sessionScope.utente}">
                        <p class="review-login-prompt">
                            <a href="${pageContext.request.contextPath}/jsp/common/login.jsp">Accedi</a> per inserire una recensione.
                        </p>
                    </c:when>
                </c:choose>

                <%-- Elenco delle recensioni salvate --%>
                <c:choose>
                    <c:when test="${not empty recensioni}">
                        <div class="reviews-list">
                            <c:forEach var="rec" items="${recensioni}">
                                <div class="review-item">
                                    <div class="review-meta">
                                        <%-- Dati dell'autore della recensione --%>
                                        <span class="review-author">
                                            <c:out value="${rec.nomeUtente}" /> <c:out value="${rec.cognomeUtente}" />
                                            <small class="review-email">(<c:out value="${rec.emailUtente}" />)</small>
                                        </span>

                                        <%-- Visualizzazione delle stelle di valutazione --%>
                                        <span class="review-stars">
                                            <c:forEach begin="1" end="${rec.valutazione}">★</c:forEach>
                                        </span>

                                        <%-- Data di pubblicazione formattata --%>
                                        <span class="review-date">
                                            <fmt:formatDate value="${rec.dataRecensione}" pattern="dd/MM/yyyy HH:mm" />
                                        </span>

                                        <%-- Azione di cancellazione riservata all'Amministratore --%>
                                        <c:if test="${isAdmin}">
                                            <form action="${pageContext.request.contextPath}/EliminaRecensioneServlet" method="post" class="delete-review-form" onsubmit="return confirm('Sei sicuro di voler eliminare questa recensione?');">
                                                <input type="hidden" name="idRecensione" value="${rec.idRecensione}">
                                                <input type="hidden" name="idProdotto" value="${prodotto.idProdotto}">
                                                <button type="submit" class="btn-delete-review">🗑️ Elimina</button>
                                            </form>
                                        </c:if>
                                    </div>
                                    <%-- Messaggio della recensione --%>
                                    <p class="review-text"><c:out value="${rec.descrizione}" /></p>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>

                    <%-- Messaggio di stato vuoto per le recensioni --%>
                    <c:otherwise>
                        <p class="no-reviews-msg">Nessuna recensione presente per questo prodotto. Sii il primo a recensirlo!</p>
                    </c:otherwise>
                </c:choose>
            </section>
        </c:when>

        <%-- CASO 2: Prodotto non trovato o ID non valido --%>
        <c:otherwise>
            <div class="empty-state">
                <p>Prodotto non trovato.</p>
            </div>
        </c:otherwise>
    </c:choose>

</main>

<%-- ── SCRIPT JS PER GESTIONE AJAX AGGIUNTA AL CARRELLO ──────────────────────── --%>
<script>
/**
 * Attende il completo caricamento del DOM prima di eseguire il codice,
 * garantendo che gli elementi HTML siano accessibili.
 */
document.addEventListener("DOMContentLoaded", function () {
    
    // Riferimenti agli elementi principali del DOM
    const form = document.getElementById("add-to-cart-form");
    const toast = document.getElementById("cart-toast");

    <%-- ── FUNZIONE DI NOTIFICA TOAST ────────────────────────────────────────── --%>
    /**
     * Mostra un messaggio di notifica temporaneo sullo schermo.
     * @param {string} message - Il messaggio di testo da visualizzare nel toast.
     * @param {boolean} isSuccess - Determina lo stile visivo (true = successo/verde, false = errore/rosso).
     */
    function showToast(message, isSuccess) {
        // Se l'elemento toast non esiste nella pagina, interrompe l'esecuzione
        if (!toast) return;

        // Imposta il contenuto del messaggio e applica la classe CSS appropriata
        toast.textContent = message;
        toast.className = "toast " + (isSuccess ? "toast-success" : "toast-error");
        
        // Aggiunge la classe per attivare l'animazione di comparsa in CSS
        toast.classList.add("show");
        
        // Timer asincrono per nascondere la notifica rimuovendo la classe dopo 2.5 secondi (2500 ms)
        setTimeout(() => {
            toast.classList.remove("show");
        }, 2500);
    }

    <%-- ── GESTIONE EVENTO DI SUBMIT DEL FORM ────────────────────────────────── --%>
    if (form) {
        /**
         * Intercetta l'invio del form per sostituire la richiesta HTTP standard
         * con una chiamata asincrona AJAX via Fetch API.
         */
        form.addEventListener("submit", function (e) {
            // Blocca il comportamento predefinito del browser (evita il ricaricamento intero della pagina)
            e.preventDefault();

            <%-- Validazione lato client nativa HTML5 (es. taglia non selezionata o quantità errata) --%>
            if (!form.checkValidity()) {
                // Mostra i tooltip nativi del browser per i campi non validi
                form.reportValidity();
                return;
            }

            // Converte i campi e i valori del modulo nel formato 'application/x-www-form-urlencoded'
            const params = new URLSearchParams(new FormData(form));

            <%-- ── CHIAMATA ASINCRONA ALLA SERVLET (FETCH API) ──────────────────── --%>
            fetch(form.getAttribute("action"), {
                method: "POST",
                headers: {
                    // Specifica il formato dati inviato e l'header per identificare la richiesta come AJAX
                    "Content-Type": "application/x-www-form-urlencoded",
                    "X-Requested-With": "XMLHttpRequest"
                },
                body: params.toString()
            })
            .then(async response => {
                // Legge la risposta del server in formato testo
                const text = await response.text();
                let data;

                // Tenta di effettuare il parsing della risposta come oggetto JSON
                try { 
                    data = JSON.parse(text); 
                } catch (err) { 
                    // Fallback se la risposta della Servlet non è un JSON valido
                    showToast("Errore di risposta dal server.", false); 
                    return; 
                }

                <%-- Gestione Utente Non Autenticato / Sessione Scaduta (HTTP 401 Unauthorized) --%>
                if (response.status === 401) { 
                    // Reindirizza l'utente alla pagina di Login definita nel JSON di risposta o a quella predefinita
                    window.location.href = data.redirect || "${pageContext.request.contextPath}/jsp/common/login.jsp"; 
                    return; 
                }

                <%-- Gestione Risposta Positiva (HTTP 200 OK e data.success == true) --%>
                if (response.ok && data.success) {
                    // Notifica l'utente dell'avvenuto inserimento nel carrello
                    showToast(data.message || "Prodotto aggiunto al carrello!", true);

                    // Cerca tutti i badge contatore nell'Header o Navbar (supporta selettori multipli)
                    const badges = document.querySelectorAll("#cart-count, .cart-badge, .badge-cart");
                    
                    // Aggiorna dinamicamente il conteggio totale degli articoli mostrato sui badge
                    badges.forEach(badge => {
                        const count = parseInt(data.cartCount, 10) || 0;
                        badge.textContent = count > 0 ? count : "";
                    });
                } else {
                    // Notifica un errore di logica di business (es. quantità richiesta superiore alla giacenza)
                    showToast(data.message || "Impossibile aggiungere il prodotto.", false);
                }
            })
            <%-- Gestione Errori di Rete o di Connessione al Server --%>
            .catch(() => showToast("Errore di connessione.", false));
        });
    }
});
</script>

<%-- Inclusione del piè di pagina --%>
<%@ include file="/jsp/common/footer.jspf" %>