<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Modifica Recensione</h1>

    <c:if test="${not empty recensione}">
        <form action="${pageContext.request.contextPath}/ModificaRecensioneServlet" method="post" class="form-edit-review">
            <input type="hidden" name="idRecensione" value="${recensione.idRecensione}" />

            <div class="form-group">
                <label for="valutazione">Valutazione</label>
                <select name="valutazione" id="valutazione">
                    <c:forEach begin="1" end="5" var="i">
                        <option value="${i}" ${i == recensione.valutazione ? 'selected' : ''}>${i} ★</option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label for="descrizione">Descrizione</label>
                <textarea name="descrizione" id="descrizione" rows="6" required>${recensione.descrizione}</textarea>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn-submit">Salva modifiche</button>
                <a href="${pageContext.request.contextPath}/ProfiloServlet" class="btn-cancel">Annulla</a>
            </div>
        </form>
    </c:if>

</main>

<%@ include file="/jsp/common/footer.jspf" %>
