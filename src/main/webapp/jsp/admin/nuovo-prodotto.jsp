<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>
<main class="container">
    <h1>Nuovo Prodotto</h1>
    <form method="post" action="<%=request.getContextPath()%>/AdminProdottoServlet">
        <input type="hidden" name="action" value="save">
        <label>Nome: <input type="text" name="nome" required></label><br>
        <label>Descrizione: <textarea name="descrizione" required></textarea></label><br>
        <label>Prezzo: <input type="number" step="0.01" name="costo" required></label><br>
        <label>Quantità: <input type="number" name="quantita" required></label><br>
        <label>Tipo: <input type="text" name="tipo"></label><br>
        <label>Attivo: <input type="checkbox" name="attivo" checked></label><br>
        <button type="submit">Salva</button>
    </form>
</main>
<%@ include file="/jsp/common/footer.jspf" %>