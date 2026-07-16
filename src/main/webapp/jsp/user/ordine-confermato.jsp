<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>
<%
    Integer ordineId = (Integer) request.getAttribute("ordineId");
    Double totaleOrdine = (Double) request.getAttribute("totaleOrdine");
    String metodoPagamento = (String) request.getAttribute("metodoPagamento");
    String indirizzoConsegna = (String) request.getAttribute("indirizzoConsegna");
%>
<main class="container">
    <h1>Ordine Confermato</h1>

    <% if (ordineId != null) { %>
        <div class="success-message">
            <p><strong>Grazie per il vostro acquisto!</strong></p>
            <p>Il vostro ordine è stato confermato con successo.</p>
        </div>

        <div class="order-details">
            <h2>Riepilogo Ordine</h2>
            <table>
                <tr>
                    <th>ID Ordine:</th>
                    <td><%=ordineId%></td>
                </tr>
                <tr>
                    <th>Totale:</th>
                    <td>€<%=String.format("%.2f", totaleOrdine)%></td>
                </tr>
                <tr>
                    <th>Metodo di Pagamento:</th>
                    <td><%=metodoPagamento%></td>
                </tr>
                <tr>
                    <th>Indirizzo di Consegna:</th>
                    <td><%=indirizzoConsegna%></td>
                </tr>
            </table>
        </div>

        <div class="actions">
            <p>Potete tracciare lo stato del vostro ordine nella sezione <a href="<%=request.getContextPath()%>/ProfiloServlet">Profilo</a>.</p>
            <a href="<%=request.getContextPath()%>/CatalogoServlet" class="btn">Continua lo Shopping</a>
        </div>
    <% } else { %>
        <p style="color: red;">Errore: Dati dell'ordine non disponibili.</p>
    <% } %>
</main>

<%@ include file="/jsp/common/footer.jspf" %>
