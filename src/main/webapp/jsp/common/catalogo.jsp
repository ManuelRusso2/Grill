<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 1. Importiamo le taglib JSTL necessarie per i cicli e la formattazione --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Catalogo</h1>

    <div class="grid">
        <%-- 2. Controlliamo se la lista dei prodotti non è vuota --%>
        <c:if test="${not empty prodotti}">
            <%-- 3. Cicliamo sui prodotti usando JSTL (Addio Raw Type e Reflection!) --%>
            <c:forEach var="p" items="${prodotti}">
                
                <%-- 4. Il catalogo pubblico mostra solo i prodotti attivi --%>
                <c:if test="${p.attivo}">
                    <div class="card">
                        <h3>
                            <%-- Usiamo c:out per visualizzare il testo in sicurezza contro attacchi XSS --%>
                            <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${p.idProdotto}">
                                <c:out value="${p.nome}" />
                            </a>
                        </h3>
                        <p>
                            <%-- Formattiamo il prezzo in Euro in modo elegante --%>
                            <fmt:formatNumber value="${p.costo}" type="currency" currencySymbol="€" />
                        </p>
                    </div>
                </c:if>
                
            </c:forEach>
        </c:if>
    </div>
</main>

<%@ include file="/jsp/common/footer.jspf" %>