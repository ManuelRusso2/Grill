<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 1. Importiamo le taglib JSTL per la logica dei cicli e la formattazione monetaria --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Gestione Prodotti</h1>

    <%-- Link per l'aggiunta di un nuovo elemento usando il context path dinamico --%>
    <a href="${pageContext.request.contextPath}/jsp/admin/nuovo-prodotto.jsp" class="btn-add">Aggiungi nuovo prodotto</a>

    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Nome</th>
                <th>Prezzo</th>
                <th>Quantità</th>
                <th>Attivo</th>
                <th>Azioni</th>
            </tr>
        </thead>
        <tbody>
            <%-- 2. Ciclo sulla lista dei prodotti inviata dal controller Admin --%>
            <c:if test="${not empty prodottiAdmin}">
                <c:forEach var="p" items="${prodottiAdmin}">
                    <tr>
                        <td><c:out value="${p.idProdotto}" /></td>
                        <td><c:out value="${p.nome}" /></td>
                        <td>
                            <%-- Formattazione nativa del costo in Euro --%>
                            <fmt:formatNumber value="${p.costo}" type="currency" currencySymbol="€" />
                        </td>
                        <td><c:out value="${p.quantita}" /></td>
                        <td>
                            <%-- Rende graficamente più chiaro lo stato del Soft Delete per l'admin --%>
                            <c:choose>
                                <c:when test="${p.attivo}">
                                    <span class="status-active">Sì</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="status-inactive">No (Disattivato)</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <%-- 3. Form delle azioni: Modifica / Elimina --%>
                            <form method="post" action="${pageContext.request.contextPath}/AdminProdottoServlet">
                                <input type="hidden" name="id" value="${p.idProdotto}">
                                
                                <button type="submit" name="action" value="update" class="btn-edit">Modifica</button>
                                
                                <%-- Il confirm JavaScript rimane integrato all'evento onclick --%>
                                <button type="submit" name="action" value="delete" class="btn-delete"
                                        onclick="return confirm('Sei sicuro di voler cambiare lo stato o eliminare questo prodotto?')">
                                    Elimina
                                </button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </c:if>

            <%-- 4. Feedback nel caso in cui il catalogo sia momentaneamente vuoto --%>
            <c:if test="${empty prodottiAdmin}">
                <tr>
                    <td colspan="6" style="text-align: center; font-style: italic; color: #777;">
                        Nessun prodotto presente nel database.
                    </td>
                </tr>
            </c:if>
        </tbody>
    </table>
</main>

<%@ include file="/jsp/common/footer.jspf" %>