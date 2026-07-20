<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Importiamo le taglib JSTL necessarie per cicli, condizioni e formattazione --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Profilo</h1>
    <p>Benvenuto! Qui trovi lo storico dei tuoi ordini.</p>

    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Data</th>
                <th>Totale</th>
                <th>Dettaglio</th>
            </tr>
        </thead>
        <tbody>
            <%-- 1. Se la lista degli acquisti non è vuota, mostriamo i dati --%>
            <c:if test="${not empty acquisti}">
                <c:forEach var="a" items="${acquisti}">
                    <tr>
                        <%-- Utilizziamo c:out per l'ID per garantire l'escape di sicurezza --%>
                        <td><c:out value="${a.idAcquisto}" /></td>
                        
                        <td>
                            <%-- 2. Formattiamo la data del database (Timestamp) in formato italiano leggibile --%>
                            <fmt:formatDate value="${a.dataAcquisto}" pattern="dd/MM/yyyy HH:mm" />
                        </td>
                        
                        <td>
                            <%-- 3. Formattiamo il prezzo totale nativamente in Euro --%>
                            <fmt:formatNumber value="${a.prezzoTotale}" type="currency" currencySymbol="€" />
                        </td>
                        
                        <td>
                            <%-- 4. Generiamo il link pulito senza scriptlet --%>
                            <a href="${pageContext.request.contextPath}/DettaglioOrdineServlet?id=${a.idAcquisto}">
                                Visualizza
                            </a>
                        </td>
                    </tr>
                </c:forEach>
            </c:if>
            
            <%-- 5. Gestione del caso in cui l'utente sia nuovo e non abbia ancora ordini --%>
            <c:if test="${empty acquisti}">
                <tr>
                    <td colspan="4" style="text-align: center; font-style: italic; color: #666;">
                        Non hai ancora effettuato nessun ordine su Grill.
                    </td>
                </tr>
            </c:if>
        </tbody>
    </table>
</main>

<%@ include file="/jsp/common/footer.jspf" %>