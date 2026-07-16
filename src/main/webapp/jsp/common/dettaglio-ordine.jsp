<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>
<%
    Object acquisto = request.getAttribute("acquisto");
    java.util.List dettagli = (java.util.List) request.getAttribute("dettagliOrdine");
%>
<main class="container">
    <h1>Dettaglio Ordine</h1>

    <% if (acquisto == null) { %>
        <p>Ordine non trovato.</p>
    <% } else { %>
        <%
            int id = (Integer) acquisto.getClass().getMethod("getIdAcquisto").invoke(acquisto);
            java.sql.Timestamp data = (java.sql.Timestamp) acquisto.getClass().getMethod("getDataAcquisto").invoke(acquisto);
            double totale = (Double) acquisto.getClass().getMethod("getPrezzoTotale").invoke(acquisto);
            String indirizzo = (String) acquisto.getClass().getMethod("getIndirizzoConsegna").invoke(acquisto);
        %>
        <p>ID ordine: <%=id%></p>
        <p>Data: <%=data%></p>
        <p>Totale: €<%=String.format("%.2f", totale)%></p>
        <p>Indirizzo di consegna: <%=indirizzo%></p>

        <h2>Articoli</h2>
        <table>
            <thead><tr><th>Prodotto</th><th>Prezzo unitario</th><th>IVA</th><th>Quantità</th><th>Stato</th></tr></thead>
            <tbody>
            <%
                if (dettagli != null) {
                    for (Object oObj : dettagli) {
                        Object o = oObj;
                        int idProd = (Integer) o.getClass().getMethod("getIdProdotto").invoke(o);
                        double prezzoUnit = (Double) o.getClass().getMethod("getPrezzoUnitario").invoke(o);
                        double iva = (Double) o.getClass().getMethod("getIva").invoke(o);
                        int q = (Integer) o.getClass().getMethod("getQuantitaAcquistata").invoke(o);
                        String stato = (String) o.getClass().getMethod("getStatoSpedizione").invoke(o);
            %>
                <tr>
                    <td><a href="<%=request.getContextPath()%>/DettaglioProdottoServlet?id=<%=idProd%>"><%=idProd%></a></td>
                    <td>€<%=String.format("%.2f", prezzoUnit)%></td>
                    <td><%=iva%> %</td>
                    <td><%=q%></td>
                    <td><%=stato%></td>
                </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    <% } %>
</main>

<%@ include file="/jsp/common/footer.jspf" %>