<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">

    <c:if test="${empty prodotto}">
        <div class="empty-state">
            <p>Prodotto non trovato.</p>
        </div>
    </c:if>

    <c:if test="${not empty prodotto}">
        <h1>
            <c:choose>
                <c:when test="${not empty nomeBase}"><c:out value="${nomeBase}" /></c:when>
                <c:otherwise><c:out value="${prodotto.nome}" /></c:otherwise>
            </c:choose>
        </h1>
        
        <c:if test="${not empty prodotto.immagine}">
            <div class="product-image-wrapper">
                <img class="product-image" src="${pageContext.request.contextPath}/${prodotto.immagine}" alt="${prodotto.nome}" />
            </div>
        </c:if>

        <div class="product-details-info">
            <p class="product-description"><c:out value="${prodotto.descrizione}" /></p>
            <p class="product-price">Prezzo: <span><fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" /></span></p>

            <%-- Selettore varianti colore --%>
            <c:if test="${not empty varianti}">
                <div class="varianti-wrapper">
                    <p class="varianti-label">Colore:</p>
                    <div class="varianti-list">
                        <c:forEach var="v" items="${varianti}">
                            <c:set var="colore" value="${fn:contains(v.nome, ' - ') ? fn:substringAfter(v.nome, ' - ') : v.nome}" />
                            <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${v.idProdotto}"
                               class="variante-btn ${v.idProdotto == prodotto.idProdotto ? 'active' : ''} ${v.quantita <= 0 ? 'esaurito' : ''}">
                                <c:out value="${colore}" />
                            </a>
                        </c:forEach>
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty prodotto.categorie}">
                <p class="product-categories">Categorie:
                    <c:forEach var="cat" items="${prodotto.categorie}" varStatus="s">
                        <c:out value="${cat.nome}" /><c:if test="${!s.last}">, </c:if>
                    </c:forEach>
                </p>
            </c:if>

            <%-- Form di Acquisto / Notifica Admin --%>
            <div class="product-actions-wrapper">
                <c:choose>
                    <c:when test="${isAdmin}">
                        <div class="admin-notice">
                            🔒 Gli amministratori non possono acquistare prodotti dal catalogo.
                        </div>
                    </c:when>
                    <c:when test="${prodotto.quantita > 0}">
                        <form method="post" action="${pageContext.request.contextPath}/CarrelloServlet" id="add-to-cart-form" class="add-to-cart-form">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="idProdotto" value="${prodotto.idProdotto}">
                            <input type="number" name="quantita" value="1" min="1" max="${prodotto.quantita}" class="input-qty">
                            <button type="submit" class="btn">Aggiungi al carrello</button>
                        </form>

                        <div id="cart-toast" class="toast"></div>

                        <script>
                        document.addEventListener("DOMContentLoaded", function () {
                            const form = document.getElementById("add-to-cart-form");
                            const toast = document.getElementById("cart-toast");

                            function showToast(message, isSuccess) {
                                toast.textContent = message;
                                toast.className = "toast " + (isSuccess ? "toast-success" : "toast-error");
                                toast.classList.add("show");
                                
                                setTimeout(() => {
                                    toast.classList.remove("show");
                                }, 2500);
                            }

                            form.addEventListener("submit", function (e) {
                                e.preventDefault();
                                const params = new URLSearchParams(new FormData(form));
                                fetch(form.getAttribute("action"), {
                                    method: "POST",
                                    headers: {
                                        "Content-Type": "application/x-www-form-urlencoded",
                                        "X-Requested-With": "XMLHttpRequest"
                                    },
                                    body: params.toString()
                                })
                                .then(async r => {
                                    const text = await r.text();
                                    let data;
                                    try { data = JSON.parse(text); } catch(err) { showToast("Errore di risposta dal server.", false); return; }
                                    if (r.status === 401) { window.location.href = data.redirect || "${pageContext.request.contextPath}/jsp/common/login.jsp"; return; }
                                    if (r.ok && data.success) {
                                        showToast(data.message || "Prodotto aggiunto al carrello!", true);
                                        const badges = document.querySelectorAll("#cart-count, .cart-badge, .badge-cart");
                                        badges.forEach(badge => {
                                            const c = parseInt(data.cartCount, 10) || 0;
                                            badge.textContent = c > 0 ? c : "";
                                        });
                                    } else {
                                        showToast(data.message || "Impossibile aggiungere il prodotto.", false);
                                    }
                                })
                                .catch(() => showToast("Errore di connessione.", false));
                            });
                        });
                        </script>
                    </c:when>
                    <c:otherwise>
                        <button class="btn btn-secondary" disabled>Esaurito</button>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <%-- SEZIONE RECENSIONI --%>
        <hr class="section-divider">
        
        <section class="reviews-section">
            <h2>Recensioni del prodotto</h2>

            <%-- Form per inviare una recensione --%>
            <c:choose>
                <c:when test="${not empty sessionScope.utente && !isAdmin}">
                    <div class="review-form-card">
                        <h3>Lascia una recensione</h3>
                        <form action="${pageContext.request.contextPath}/AggiungiRecensioneServlet" method="post">
                            <input type="hidden" name="idProdotto" value="${prodotto.idProdotto}">
                            
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

                            <div class="form-group">
                                <label for="descrizione">La tua opinione:</label>
                                <textarea name="descrizione" id="descrizione" class="form-control" rows="4" required placeholder="Scrivi una recensione..."></textarea>
                            </div>

                            <button type="submit" class="btn-submit-review">Invia Recensione</button>
                        </form>
                    </div>
                </c:when>
                <c:when test="${empty sessionScope.utente}">
                    <p class="review-login-prompt">
                        <a href="${pageContext.request.contextPath}/jsp/common/login.jsp">Accedi</a> per inserire una recensione.
                    </p>
                </c:when>
            </c:choose>

            <%-- Lista delle recensioni già presenti --%>
            <c:choose>
                <c:when test="${not empty recensioni}">
                    <div class="reviews-list">
                        <c:forEach var="rec" items="${recensioni}">
                            <div class="review-item">
                                <div class="review-meta">
                                    <span class="review-author">
                                        <c:out value="${rec.nomeUtente}" /> <c:out value="${rec.cognomeUtente}" />
                                        <small class="review-email">(<c:out value="${rec.emailUtente}" />)</small>
                                    </span>
                                    <span class="review-stars">
                                        <c:forEach begin="1" end="${rec.valutazione}">★</c:forEach>
                                    </span>
                                    <span class="review-date">
                                        <fmt:formatDate value="${rec.dataRecensione}" pattern="dd/MM/yyyy HH:mm" />
                                    </span>

                                    <%-- TASTO ELIMINA VISIBILE SOLO AGLI ADMIN --%>
                                    <c:if test="${isAdmin}">
                                        <form action="${pageContext.request.contextPath}/EliminaRecensioneServlet" method="post" class="delete-review-form" onsubmit="return confirm('Sei sicuro di voler eliminare questa recensione?');">
                                            <input type="hidden" name="idRecensione" value="${rec.idRecensione}">
                                            <input type="hidden" name="idProdotto" value="${prodotto.idProdotto}">
                                            <button type="submit" class="btn-delete-review">🗑️ Elimina</button>
                                        </form>
                                    </c:if>
                                </div>
                                <p class="review-text"><c:out value="${rec.descrizione}" /></p>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="no-reviews-msg">Nessuna recensione presente per questo prodotto. Sii il primo a recensirlo!</p>
                </c:otherwise>
            </c:choose>
        </section>

    </c:if>

</main>

<%@ include file="/jsp/common/footer.jspf" %>