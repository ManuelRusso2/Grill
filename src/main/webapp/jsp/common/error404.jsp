<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container error-page">
	<div class="error-card">
		<h1 class="error-code">404</h1>
		<h2>Pagina non trovata</h2>
		<p>La risorsa richiesta non esiste o &egrave; stata rimossa.</p>
		<p>Controlla l'URL per continuare la navigazione.</p>
		<div class="error-actions">
			<a class="btn" href="${pageContext.request.contextPath}/CatalogoServlet">Torna al Catalogo</a>
		</div>
	</div>
</main>

<%@ include file="/jsp/common/footer.jspf" %>