<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container error-page">
	<div class="error-card">
		<h1 class="error-code">403</h1>
		<h2>Accesso negato</h2>
		<p>Mi dispiace, non hai i permessi necessari per visualizzare questa pagina.</p>
		<p>Se pensi che si tratti di un errore, contatta l'amministratore o effettua il logout.</p>
		<div class="error-actions">
			<a class="btn" href="${pageContext.request.contextPath}/CatalogoServlet">Torna al Catalogo</a>
			<a class="btn btn-secondary" href="${pageContext.request.contextPath}/LogoutServlet">Logout</a>
		</div>
	</div>
</main>

<%@ include file="/jsp/common/footer.jspf" %>