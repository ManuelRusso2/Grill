<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>
<%
    Map prodotti = (Map) request.getAttribute("prodottiCarrello");
    Double totale = (Double) request.getAttribute("totaleCarrello");
    if (totale == null) totale = 0.0;
%>
<main class="container">
    <h1>Checkout</h1>

    <table>
        <thead>
            <tr><th>Prodotto</th><th>Prezzo</th><th>Quantità</th></tr>
        </thead>
        <tbody>
        <%
            if (prodotti != null) {
                for (Object entryObj : prodotti.entrySet()) {
                    java.util.Map.Entry entry = (java.util.Map.Entry) entryObj;
                    Object p = entry.getKey();
                    Integer q = (Integer) entry.getValue();
                    int id = (Integer) p.getClass().getMethod("getIdProdotto").invoke(p);
                    String nome = (String) p.getClass().getMethod("getNome").invoke(p);
                    double prezzo = (Double) p.getClass().getMethod("getCosto").invoke(p);
        %>
            <tr>
                <td><a href="<%=request.getContextPath()%>/DettaglioProdottoServlet?id=<%=id%>"><%=nome%></a></td>
                <td>€<%=String.format("%.2f", prezzo)%></td>
                <td><%=q%></td>
            </tr>
        <%
                }
            }
        %>
        </tbody>
    </table>

    <p>Totale: €<%=String.format("%.2f", totale)%></p>

    <form method="post" action="<%=request.getContextPath()%>/CheckoutServlet">
        <label>Metodo pagamento:
            <select name="metodoPagamento">
                <option value="Carta">Carta</option>
                <option value="Contrassegno">Contrassegno</option>
            </select>
        </label>
        <br>
        <label>Indirizzo di consegna:
            <input type="text" name="indirizzoConsegna" required>
        </label>
        <br>
        <button type="submit">Conferma ordine</button>
    </form>
</main>

<%@ include file="/jsp/common/footer.jspf" %>