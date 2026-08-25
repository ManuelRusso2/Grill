<%-- 
    Pagina di gestione/amministrazione degli ordini.
    Consente agli amministratori di visualizzare lo storico ordini complessivo,
    filtrando i risultati per cliente specifico e/o intervallo di date, 
    nonché di accedere al dettaglio del singolo ordine.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Importazione della libreria JSTL Core per il controllo di flusso, cicli e condizioni --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Importazione della libreria JSTL Formatting per la formattazione di date, numeri e valute --%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Inclusione del frammento statico per l'intestazione HTML e le risorse della pagina (head, CSS, JS) --%>
<%@ include file="/jsp/common/header.jspf" %>

<%-- Inclusione del frammento statico per la barra di navigazione principale (menu / navbar) --%>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container admin-orders-section">
    <h2>Gestione Ordini</h2>

    <%-- ── FORM DI FILTRAGGIO ORDINI ────────────────────────────────────────── --%>
    <%-- Invia i parametri tramite GET alla Servlet di amministrazione ordini per applicare i filtri --%>
    <form class="admin-orders-filter" method="get" action="${pageContext.request.contextPath}/AdminOrdiniServlet">
        
        <%-- Selezione del cliente specifico --%>
        <label for="clienteSelect">Cliente:</label>
        <select id="clienteSelect" name="clienteId">
            <option value="">Tutti i clienti</option>
            <c:if test="${not empty clienti}">
                <c:forEach var="c" items="${clienti}">
                    <%-- Mantiene la selezione attiva in base al parametro inviato precedentemente --%>
                    <option value="${c.idUtente}" ${clienteSelezionato == c.idUtente ? 'selected' : ''}>
                        <c:out value="${c.nome} ${c.cognome}" />
                    </option>
                </c:forEach>
            </c:if>
        </select>
        
        <%-- Filtro intervallo temporale: Data Inizio --%>
        <label for="dataDa">Data da:</label>
        <input type="date" id="dataDa" name="dataDa" value="<c:out value='${dataDa}'/>">

        <%-- Filtro intervallo temporale: Data Fine --%>
        <label for="dataA">Data a:</label>
        <input type="date" id="dataA" name="dataA" value="<c:out value='${dataA}'/>">

        <button type="submit" class="btn btn-md btn-primary">Filtra</button>
    </form>

    <%-- ── TABELLA ELENCO ORDINI AMMINISTRAZIONE ───────────────────────────── --%>
    <div class="admin-table-wrapper">
        <table class="admin-table">
            <thead>
                <tr>
                    <th class="col-id">ID</th>
                    <th>Data</th>
                    <th>Totale</th>
                    <th>ID Cliente</th>
                    <th class="text-right col-actions">Dettaglio</th>
                </tr>
            </thead>
            <tbody>
                <%-- Verifica della presenza di ordini restituiti dal controller --%>
                <c:choose>
                    <c:when test="${not empty ordiniAdmin}">
                        <%-- Ciclo di iterazione sulla lista degli ordini --%>
                        <c:forEach var="o" items="${ordiniAdmin}">
                            <tr>
                                <%-- ID identificativo dell'ordine --%>
                                <td><strong>#<c:out value="${o.idAcquisto}" /></strong></td>
                                
                                <%-- Formattazione della data e dell'ora dell'acquisto --%>
                                <td>
                                    <fmt:formatDate value="${o.dataAcquisto}" pattern="dd/MM/yyyy HH:mm" />
                                </td>
                                
                                <%-- Formattazione del prezzo totale dell'ordine in valuta Euro (€) --%>
                                <td>
                                    <fmt:formatNumber value="${o.prezzoTotale}" type="currency" currencySymbol="€" />
                                </td>
                                
                                <%-- ID dell'utente/cliente associato --%>
                                <td><c:out value="${o.idUtente}" /></td>
                                
                                <%-- Pulsante per accedere alla pagina di dettaglio dell'ordine --%>
                                <td class="text-right">
                                    <a href="${pageContext.request.contextPath}/DettaglioOrdineServlet?id=${o.idAcquisto}" class="btn btn-sm btn-outline-warning">Apri</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    
                    <%-- Messaggio visualizzato in assenza di ordini corrispondenti ai criteri --%>
                    <c:otherwise>
                        <tr>
                            <td colspan="5" class="empty-table-msg">
                                Nessun ordine trovato con i criteri di ricerca selezionati.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</main>

<%-- Inclusione del frammento statico del piè di pagina --%>
<%@ include file="/jsp/common/footer.jspf" %>