<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 1. Importiamo le taglib JSTL per la logica dei cicli, controlli e formattazione --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Gestione Ordini</h1>

    <%-- 2. Form di filtraggio: URL generato dinamicamente con il contesto dell'applicazione --%>
    <form method="get" action="${pageContext.request.contextPath}/AdminOrdiniServlet">
        <label>Cliente:
            <select name="clienteId">
                <option value="">Tutti</option>
                
                <%-- Ciclo sulla lista dei clienti --%>
                <c:if test="${not empty clienti}">
                    <c:forEach var="c" items="${clienti}">
                        <%-- Il tag valuta al volo se l'ID del cliente corrente coincide con quello selezionato nei filtri --%>
                        <option value="${c.idUtente}" ${clienteSelezionato == c.idUtente ? 'selected' : ''}>
                            <c:out value="${c.nome} ${c.cognome}" />
                        </option>
                    </c:forEach>
                </c:if>
            </select>
        </label>
        
        <%-- L'Expression Language gestisce automaticamente i valori nulli stampando una stringa vuota --%>
        <label>Data da: <input type="date" name="dataDa" value="${dataDa}"></label>
        <label>Data a: <input type="date" name="dataA" value="${dataA}"></label>
        <button type="submit">Filtra</button>
    </form>

    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Data</th>
                <th>Totale</th>
                <th>Cliente</th>
                <th>Dettaglio</th>
            </tr>
        </thead>
        <tbody>
            <%-- 3. Ciclo sullo storico degli ordini globali del negozio --%>
            <c:if test="${not empty ordiniAdmin}">
                <c:forEach var="o" items="${ordiniAdmin}">
                    <tr>
                        <td><c:out value="${o.idAcquisto}" /></td>
                        
                        <td>
                            <%-- Formattazione europea della data (Giorno/Mese/Anno Ore:Minuti) --%>
                            <fmt:formatDate value="${o.dataAcquisto}" pattern="dd/MM/yyyy HH:mm" />
                        </td>
                        
                        <td>
                            <%-- Formattazione della valuta --%>
                            <fmt:formatNumber value="${o.prezzoTotale}" type="currency" currencySymbol="€" />
                        </td>
                        
                        <td><c:out value="${o.idUtente}" /></td>
                        
                        <td>
                            <a href="${pageContext.request.contextPath}/DettaglioOrdineServlet?id=${o.idAcquisto}">Apri</a>
                        </td>
                    </tr>
                </c:forEach>
            </c:if>
            
            <%-- 4. Feedback all'amministratore se la ricerca non produce risultati --%>
            <c:if test="${empty ordiniAdmin}">
                <tr>
                    <td colspan="5" style="text-align: center; font-style: italic; color: #777;">
                        Nessun ordine trovato con i criteri di ricerca selezionati.
                    </td>
                </tr>
            </c:if>
        </tbody>
    </table>
</main>

<%@ include file="/jsp/common/footer.jspf" %>