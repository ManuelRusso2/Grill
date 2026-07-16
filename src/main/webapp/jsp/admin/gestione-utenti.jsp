<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>
<%
    java.util.List clienti = (java.util.List) request.getAttribute("clienti");
%>
<main class="container">
    <h1>Gestione Utenti</h1>

    <table>
        <thead><tr><th>ID</th><th>Nome</th><th>Cognome</th><th>Email</th><th>Username</th><th>Azioni</th></tr></thead>
        <tbody>
        <%
            if (clienti != null) {
                for (Object cObj : clienti) {
                    Object c = cObj;
                    int id = (Integer) c.getClass().getMethod("getIdUtente").invoke(c);
                    String nome = (String) c.getClass().getMethod("getNome").invoke(c);
                    String cognome = (String) c.getClass().getMethod("getCognome").invoke(c);
                    String email = (String) c.getClass().getMethod("getEmail").invoke(c);
                    String username = (String) c.getClass().getMethod("getUsername").invoke(c);
        %>
            <tr>
                <td><%=id%></td>
                <td><%=nome%></td>
                <td><%=cognome%></td>
                <td><%=email%></td>
                <td><%=username%></td>
                <td>
                    <!-- Azioni amministrative: dettaglio, cancellazione ecc. -->
                    <form method="post" action="<%=request.getContextPath()%>/AdminUtentiServlet">
                        <input type="hidden" name="id" value="<%=id%>">
                        <button name="action" value="view">Dettaglio</button>
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