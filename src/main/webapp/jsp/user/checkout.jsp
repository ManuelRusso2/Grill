<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Importiamo JSTL --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Nota: Se usi Tomcat 10 cambia gli URI in "jakarta.tags.core" e "jakarta.tags.fmt" --%>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Checkout</h1>

    <table>
        <thead>
            <tr><th>Prodotto</th><th>Prezzo</th><th>Quantità</th></tr>
        </thead>
        <tbody>
            <%-- Iteriamo sulla mappa passata dalla servlet --%>
            <c:forEach var="entry" items="${prodottiCarrello}">
                <c:set var="prodotto" value="${entry.key}" />
                <c:set var="quantita" value="${entry.value}" />
                <tr>
                    <td>
                        <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${prodotto.idProdotto}">
                            <c:out value="${prodotto.nome}"/>
                        </a>
                    </td>
                    <td>
                        <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€"/>
                    </td>
                    <td><c:out value="${quantita}"/></td>
                </tr>
            </c:forEach>
        </tbody>
    </table>

    <%-- Mostriamo il totale formattato in euro. Se è vuoto o null, mostra €0,00 --%>
    <p>
        Totale: 
        <fmt:formatNumber value="${not empty totaleCarrello ? totaleCarrello : 0.0}" type="currency" currencySymbol="€"/>
    </p>

    <form method="post" action="${pageContext.request.contextPath}/CheckoutServlet">
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