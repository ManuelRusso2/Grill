<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>
<%
    java.util.List acquisti = (java.util.List) request.getAttribute("acquisti");
%>
<main class="container">
    <h1>Profilo</h1>
    <p>Benvenuto! Qui trovi lo storico dei tuoi ordini.</p>

    <table>
        <thead>
            <tr><th>ID</th><th>Data</th><th>Totale</th><th>Dettaglio</th></tr>
        </thead>
        <tbody>
        <%
            if (acquisti != null) {
                for (Object aObj : acquisti) {
                    Object a = aObj;
                    int id = (Integer) a.getClass().getMethod("getIdAcquisto").invoke(a);
                    java.sql.Timestamp ts = (java.sql.Timestamp) a.getClass().getMethod("getDataAcquisto").invoke(a);
                    double totale = (Double) a.getClass().getMethod("getPrezzoTotale").invoke(a);
        %>
            <tr>
                <td><%=id%></td>
                <td><%=ts%></td>
                <td>€<%=String.format("%.2f", totale)%></td>
                <td><a href="<%=request.getContextPath()%>/DettaglioOrdineServlet?id=<%=id%>">Visualizza</a></td>
            </tr>
        <%
                }
            }
        %>
        </tbody>
    </table>
</main>

<%@ include file="/jsp/common/footer.jspf" %>