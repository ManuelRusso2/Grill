<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>
<%
    // Attributo passato da CarrelloServlet: Map<ProdottoBean,Integer> prodottiCarrello
    Map prodotti = (Map) request.getAttribute("prodottiCarrello");
%>
<main class="container">
    <h1>Carrello</h1>
    <c:if test="${empty prodotti}">
        <p>Il carrello è vuoto.</p>
    </c:if>
    <table>
        <thead>
            <tr>
                <th>Prodotto</th>
                <th>Prezzo</th>
                <th>Quantità</th>
                <th>Azione</th>
            </tr>
        </thead>
        <tbody>
        <%
            if (prodotti != null && !prodotti.isEmpty()) {
                for (Object entryObj : produtos.entrySet()) {
                    java.util.Map.Entry entry = (java.util.Map.Entry) entryObj;
                    Object p = entry.getKey();
                    Integer q = (Integer) entry.getValue();
                    // Riflettiamo in modo semplice sui bean per ottenere i campi utili
                    int id = (Integer) p.getClass().getMethod("getIdProdotto").invoke(p);
                    String nome = (String) p.getClass().getMethod("getNome").invoke(p);
                    double prezzo = (Double) p.getClass().getMethod("getCosto").invoke(p);
        %>
            <tr>
                <td><a href="<%=request.getContextPath()%>/DettaglioProdottoServlet?id=<%=id%>"><%=nome%></a></td>
                <td>€<%=String.format("%.2f", prezzo)%></td>
                <td><%=q%></td>
                <td>
                    <form method="post" action="<%=request.getContextPath()%>/CarrelloServlet">
                        <input type="hidden" name="id" value="<%=id%>">
                        <button name="action" value="remove">Rimuovi</button>
                    </form>
                </td>
            </tr>
        <%
                }
            }
        %>
        </tbody>
    </table>

    <a href="<%=request.getContextPath()%>/CheckoutServlet">Vai al checkout</a>
</main>

<%@ include file="/jsp/common/footer.jspf" %>