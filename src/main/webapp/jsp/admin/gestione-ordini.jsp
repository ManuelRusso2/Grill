<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>
<%
    java.util.List ordini = (java.util.List) request.getAttribute("ordiniAdmin");
    java.util.List clienti = (java.util.List) request.getAttribute("clienti");
    Integer clienteSelezionato = (Integer) request.getAttribute("clienteSelezionato");
    String dataDa = (String) request.getAttribute("dataDa");
    String dataA = (String) request.getAttribute("dataA");
%>
<main class="container">
    <h1>Gestione Ordini</h1>

    <form method="get" action="<%=request.getContextPath()%>/AdminOrdiniServlet">
        <label>Cliente:
            <select name="clienteId">
                <option value="">Tutti</option>
                <%
                    if (clienti != null) {
                        for (Object cObj : clienti) {
                            Object c = cObj;
                            int id = (Integer) c.getClass().getMethod("getIdUtente").invoke(c);
                            String nome = (String) c.getClass().getMethod("getNome").invoke(c);
                            String cognome = (String) c.getClass().getMethod("getCognome").invoke(c);
                %>
                    <option value="<%=id%>" <%= (clienteSelezionato!=null && clienteSelezionato==id)?"selected":"" %>><%=nome%> <%=cognome%></option>
                <%
                        }
                    }
                %>
            </select>
        </label>
        <label>Data da: <input type="date" name="dataDa" value="<%= dataDa != null ? dataDa : "" %>"></label>
        <label>Data a: <input type="date" name="dataA" value="<%= dataA != null ? dataA : "" %>"></label>
        <button type="submit">Filtra</button>
    </form>

    <table>
        <thead><tr><th>ID</th><th>Data</th><th>Totale</th><th>Cliente</th><th>Dettaglio</th></tr></thead>
        <tbody>
        <%
            if (ordini != null) {
                for (Object oObj : ordini) {
                    Object o = oObj;
                    int id = (Integer) o.getClass().getMethod("getIdAcquisto").invoke(o);
                    java.sql.Timestamp ts = (java.sql.Timestamp) o.getClass().getMethod("getDataAcquisto").invoke(o);
                    double totale = (Double) o.getClass().getMethod("getPrezzoTotale").invoke(o);
                    int idUtente = (Integer) o.getClass().getMethod("getIdUtente").invoke(o);
        %>
            <tr>
                <td><%=id%></td>
                <td><%=ts%></td>
                <td>€<%=String.format("%.2f", totale)%></td>
                <td><%=idUtente%></td>
                <td><a href="<%=request.getContextPath()%>/DettaglioOrdineServlet?id=<%=id%>">Apri</a></td>
            </tr>
        <%
                }
            }
        %>
        </tbody>
    </table>
</main>

<%@ include file="/jsp/common/footer.jspf" %>