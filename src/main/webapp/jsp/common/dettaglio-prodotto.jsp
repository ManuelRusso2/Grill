<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>
<%
    Object prodotto = request.getAttribute("prodotto");
%>
<main class="container">
    <h1>Dettaglio Prodotto</h1>

    <% if (prodotto == null) { %>
        <p>Prodotto non trovato.</p>
    <% } else { %>
        <%
            int id = (Integer) prodotto.getClass().getMethod("getIdProdotto").invoke(prodotto);
            String nome = (String) prodotto.getClass().getMethod("getNome").invoke(prodotto);
            String descr = (String) prodotto.getClass().getMethod("getDescrizione").invoke(prodotto);
            double prezzo = (Double) prodotto.getClass().getMethod("getCosto").invoke(prodotto);
            int quantita = (Integer) prodotto.getClass().getMethod("getQuantita").invoke(prodotto);
            boolean attivo = (Boolean) prodotto.getClass().getMethod("isAttivo").invoke(prodotto);
        %>
        <h2><%=nome%></h2>
        <p><%=descr%></p>
        <p>Prezzo: €<%=String.format("%.2f", prezzo)%></p>
        <p>Disponibilità: <%=quantita%></p>

        <form method="get" action="<%=request.getContextPath()%>/CarrelloServlet">
            <input type="hidden" name="id" value="<%=id%>">
            <button type="submit" name="action" value="add">Aggiungi al carrello</button>
        </form>
    <% } %>
</main>

<%@ include file="/jsp/common/footer.jspf" %>