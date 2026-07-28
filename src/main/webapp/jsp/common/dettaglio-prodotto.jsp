<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">

    <c:if test="${empty prodotto}">
        <p>Prodotto non trovato.</p>
    </c:if>

    <c:if test="${not empty prodotto}">
        <h1>
            <c:choose>
                <c:when test="${not empty nomeBase}"><c:out value="${nomeBase}" /></c:when>
                <c:otherwise><c:out value="${prodotto.nome}" /></c:otherwise>
            </c:choose>
        </h1>
        <p><c:out value="${prodotto.descrizione}" /></p>
        <p>Prezzo: <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" /></p>

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
            <p>Categorie:
                <c:forEach var="cat" items="${prodotto.categorie}" varStatus="s">
                    <c:out value="${cat.nome}" /><c:if test="${!s.last}">, </c:if>
                </c:forEach>
            </p>
        </c:if>

        <c:choose>
            <c:when test="${prodotto.quantita > 0}">
                <form method="post" action="${pageContext.request.contextPath}/CarrelloServlet" id="add-to-cart-form">
                    <input type="hidden" name="action" value="add">
                    <input type="hidden" name="idProdotto" value="${prodotto.idProdotto}">
                    <input type="number" name="quantita" value="1" min="1" max="${prodotto.quantita}">
                    <button type="submit">Aggiungi al carrello</button>
                </form>
                <div id="cart-toast" style="display:none; position:fixed; top:20px; right:20px; z-index:9999; padding:12px 20px; border-radius:6px; font-weight:bold; color:white; box-shadow:0 4px 10px rgba(0,0,0,0.15); transition:opacity 0.3s ease;"></div>
                <script>
                document.addEventListener("DOMContentLoaded", function () {
                    const form = document.getElementById("add-to-cart-form");
                    const toast = document.getElementById("cart-toast");

                    function showToast(message, isSuccess) {
                        toast.textContent = message;
                        toast.style.backgroundColor = isSuccess ? "#10B981" : "#EF4444";
                        toast.style.display = "block";
                        toast.style.opacity = "1";
                        setTimeout(() => {
                            toast.style.opacity = "0";
                            setTimeout(() => { toast.style.display = "none"; }, 300);
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
                                const badge = document.getElementById("cart-count");
                                if (badge) { const c = parseInt(data.cartCount, 10) || 0; badge.textContent = c > 0 ? "(" + c + ")" : ""; }
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
                <button disabled>Esaurito</button>
            </c:otherwise>
        </c:choose>
    </c:if>

</main>

<%@ include file="/jsp/common/footer.jspf" %>
