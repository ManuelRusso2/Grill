<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>
<%
    java.util.List prodotti = (java.util.List) request.getAttribute("prodotti");
%>
<main class="container">
    <h1>Catalogo</h1>

    <div class="grid">
    <%
        if (prodotti != null) {
            for (Object pObj : prodotti) {
                Object p = pObj;
                int id = (Integer) p.getClass().getMethod("getIdProdotto").invoke(p);
                String nome = (String) p.getClass().getMethod("getNome").invoke(p);
                double prezzo = (Double) p.getClass().getMethod("getCosto").invoke(p);
                boolean attivo = (Boolean) p.getClass().getMethod("isAttivo").invoke(p);
                if (!attivo) continue; // il catalogo pubblico mostra solo prodotti attivi
    %>
        <div class="card">
            <h3><a href="<%=request.getContextPath()%>/DettaglioProdottoServlet?id=<%=id%>"><%=nome%></a></h3>
            <p>€<%=String.format("%.2f", prezzo)%></p>
        </div>
    <%
            }
        }
    %>
    </div>
</main>

<%@ include file="/jsp/common/footer.jspf" %>