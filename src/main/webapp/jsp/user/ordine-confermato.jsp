<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<%
    Integer ordineId = (Integer) request.getAttribute("ordineId");
    Double totaleOrdine = (Double) request.getAttribute("totaleOrdine");
    String metodoPagamento = (String) request.getAttribute("metodoPagamento");
    String indirizzoConsegna = (String) request.getAttribute("indirizzoConsegna");
%>

<main class="container">
    <div class="success-page-container">
        <% if (ordineId != null) { %>
            <div class="success-card">
                <div class="success-icon-wrapper">
                    <span class="success-checkmark">✓</span>
                </div>
                
                <h1>Ordine Confermato</h1>
                <p class="success-subtitle">Grazie per il vostro acquisto! Il vostro ordine è stato elaborato con successo.</p>

                <div class="order-summary-box">
                    <h2>Riepilogo Ordine</h2>
                    
                    <div class="order-detail-row">
                        <span class="order-label">ID Ordine</span>
                        <span class="order-value">#<%=ordineId%></span>
                    </div>
                    <div class="order-detail-row">
                        <span class="order-label">Totale</span>
                        <span class="order-value order-price">€<%=String.format("%.2f", totaleOrdine)%></span>
                    </div>
                    <div class="order-detail-row">
                        <span class="order-label">Metodo di Pagamento</span>
                        <span class="order-value"><%=metodoPagamento%></span>
                    </div>
                    <div class="order-detail-row">
                        <span class="order-label">Indirizzo di Consegna</span>
                        <span class="order-value"><%=indirizzoConsegna%></span>
                    </div>
                </div>

                <p class="success-tracking-text">
                    Potete tracciare lo stato del vostro ordine nella sezione <a href="<%=request.getContextPath()%>/ProfiloServlet" class="order-link">Profilo</a>.
                </p>

                <div class="success-actions">
                    <a href="<%=request.getContextPath()%>/CatalogoServlet" class="btn btn-catalog">Continua lo Shopping</a>
                </div>
            </div>
        <% } else { %>
            <div class="error-card">
                <div class="error-code">⚠️</div>
                <h2>Errore</h2>
                <p class="error-message-text">Dati dell'ordine non disponibili o sessione scaduta.</p>
                <div class="error-actions">
                    <a href="<%=request.getContextPath()%>/CatalogoServlet" class="btn btn-catalog">Torna al Catalogo</a>
                </div>
            </div>
        <% } %>
    </div>
</main>

<%@ include file="/jsp/common/footer.jspf" %>