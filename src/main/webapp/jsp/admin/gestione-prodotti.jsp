<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>
<%
    java.util.List prodotti = (java.util.List) request.getAttribute("prodottiAdmin");
%>
<main class="container">
    <h1>Gestione Prodotti</h1>

    <a href="<%=request.getContextPath()%>/jsp/admin/nuovo-prodotto.jsp">Aggiungi nuovo prodotto</a>

    <table>
        <thead><tr><th>ID</th><th>Nome</th><th>Prezzo</th><th>Quantità</th><th>Attivo</th><th>Azioni</th></tr></thead>
        <tbody>
        <%
            if (prodotti != null) {
                for (Object pObj : prodotti) {
                    Object p = pObj;
                    int id = (Integer) p.getClass().getMethod("getIdProdotto").invoke(p);
                    String nome = (String) p.getClass().getMethod("getNome").invoke(p);
                    double prezzo = (Double) p.getClass().getMethod("getCosto").invoke(p);
                    int quantita = (Integer) p.getClass().getMethod("getQuantita").invoke(p);
                    boolean attivo = (Boolean) p.getClass().getMethod("isAttivo").invoke(p);
        %>
            <tr>
                <td><%=id%></td>
                <td><%=nome%></td>
                <td>€<%=String.format("%.2f", prezzo)%></td>
                <td><%=quantita%></td>
                <td><%=attivo%></td>
                <td>
                    <form method="post" action="<%=request.getContextPath()%>/AdminProdottoServlet">
                        <input type="hidden" name="id" value="<%=id%>">
                        <button name="action" value="update">Modifica</button>
                        <button name="action" value="delete" onclick="return confirm('Sei sicuro di voler cancellare questo prodotto?')">Elimina</button>
                    </form>
                </td>
            </tr>
        <%
                }
            }
        %>
        </tbody>
    </table>
</main>

<%@ include file="/jsp/common/footer.jspf" %>