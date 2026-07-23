<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Catalogo Prodotti</h1>

    <%-- Toast Notifica per aggiunta asincrona via AJAX --%>
    <div id="cart-toast" style="display:none; position: fixed; top: 20px; right: 20px; z-index: 9999; padding: 12px 20px; border-radius: 6px; font-weight: bold; color: white; box-shadow: 0 4px 10px rgba(0,0,0,0.15); transition: opacity 0.3s ease;">
    </div>

    <%-- 1. Feedback Utente (Messaggi tradizionali via Servlet/Sessione) --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            <c:out value="${successMessage}" />
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- 2. Griglia Catalogo --%>
    <div class="grid">
        <c:choose>
            <c:when test="${not empty prodotti}">
                <c:forEach var="p" items="${prodotti}">
                    <c:if test="${p.attivo}">
                        <div class="card">
                            <h3>
                                <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${p.idProdotto}">
                                    <c:out value="${p.nome}" />
                                </a>
                            </h3>
                            <p class="price">
                                <fmt:formatNumber value="${p.costo}" type="currency" currencySymbol="€" />
                            </p>
                            
                            <%-- 3. Form Aggiunta Rapida al Carrello (Gestito via AJAX) --%>
                            <form action="${pageContext.request.contextPath}/CarrelloServlet" method="post" class="add-to-cart-form">
                                <input type="hidden" name="action" value="add">
                                <input type="hidden" name="idProdotto" value="${p.idProdotto}">
                                
                                <c:choose>
                                    <c:when test="${p.quantita > 0}">
                                        <input type="number" name="quantita" value="1" min="1" max="${p.quantita}" class="input-qty">
                                        <button type="submit" class="btn btn-primary">Aggiungi al Carrello</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="button" class="btn btn-disabled" disabled>Esaurito</button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                        </div>
                    </c:if>
                </c:forEach>
            </c:when>
            
            <%-- Gestione caso nessun prodotto nel sistema --%>
            <c:otherwise>
                <p class="empty-state">Al momento non ci sono prodotti disponibili nel catalogo.</p>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<script>
document.addEventListener("DOMContentLoaded", function () {
    const forms = document.querySelectorAll('.add-to-cart-form');
    const toast = document.getElementById('cart-toast');

    function showToast(message, isSuccess = true) {
        if (!toast) return;
        toast.textContent = message;
        toast.style.backgroundColor = isSuccess ? "#10B981" : "#EF4444";
        toast.style.display = "block";
        toast.style.opacity = "1";

        setTimeout(() => {
            toast.style.opacity = "0";
            setTimeout(() => { toast.style.display = "none"; }, 300);
        }, 2500);
    }

    forms.forEach(form => {
        form.addEventListener('submit', function (e) {
            e.preventDefault(); // Blocca l'invio tradizionale

            // Usiamo getAttribute('action') per evitare che confligga con l'input hidden name="action"
            const targetUrl = form.getAttribute('action'); 
            const formData = new FormData(form);
            const params = new URLSearchParams(formData);

            fetch(targetUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: params.toString()
            })
            .then(async response => {
                const textResponse = await response.text();
                let data;

                try {
                    data = JSON.parse(textResponse);
                } catch (jsonErr) {
                    console.error("Risposta non JSON dal server:", textResponse);
                    showToast("Errore di risposta dal server.", false);
                    return;
                }

                if (response.status === 401) {
                    window.location.href = data.redirect || "${pageContext.request.contextPath}/jsp/common/login.jsp";
                    return;
                }

                if (response.ok && data.success) {
                    showToast(data.message || "Prodotto aggiunto al carrello!", true);
                    
                    // Aggiornamento sicuro ed esplicito del badge nel menu
                    const cartCountSpan = document.getElementById('cart-count');
                    if (cartCountSpan) {
                        const count = parseInt(data.cartCount, 10) || 0;
                            // Aggiorniamo direttamente
                            cartCountSpan.textContent = count > 0 ? `(${count})` : "";
                            // Notifichiamo anche eventuali listener globali (es. altre pagine o script)
                            try { window.dispatchEvent(new CustomEvent('cartUpdated', { detail: { count: count } })); } catch (e) { /* ignore */ }
                    }
                } else {
                    showToast(data.message || "Impossibile aggiungere il prodotto.", false);
                }
            })
            .catch(error => {
                console.error("Errore durante la chiamata AJAX:", error);
                showToast("Errore di connessione.", false);
            });
        });
    });
});
</script>

<%@ include file="/jsp/common/footer.jspf" %>