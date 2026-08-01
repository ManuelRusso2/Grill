<%@ page isErrorPage="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container error-page">
	<div class="error-card">
		<h1 class="error-code">500</h1>
		<h2>Errore interno del server</h2>
		<p>Si &egrave; verificato un errore inaspettato. Stiamo gi&agrave; lavorando per risolverlo.</p>
		<c:if test="${not empty exception}">
			<div class="error-details">
				<strong>Dettagli tecnici (solo a scopo diagnostico):</strong>
				<pre style="white-space:pre-wrap; background:var(--bg-input); padding:15px; border-radius:6px; margin-top:10px; overflow:auto;">${exception}</pre>
			</div>
		</c:if>
		<div class="error-actions">
			<a class="btn" href="${pageContext.request.contextPath}/CatalogoServlet">Torna al Catalogo</a>
		</div>
	</div>
</main>

<%@ include file="/jsp/common/footer.jspf" %>