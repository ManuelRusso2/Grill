<%-- 
    Pagina di gestione/amministrazione delle recensioni degli utenti.
    Consente all'amministratore di filtrare le recensioni per utente specifico,
    visualizzarne i dettagli e rimuovere quelle inappropriate.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Inclusione delle Tag Library JSTL per controllo flusso e formattazione date --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Inclusione dei frammenti statici per l'intestazione HTML e la barra di navigazione --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Gestione Recensioni</h1>

    <%-- ── MESSAGGI DI FEEDBACK ─────────────────────────────────────────────── --%>
    <%-- Banner di successo dopo eliminazione --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success"><c:out value="${successMessage}" /></div>
    </c:if>
    <%-- Banner di errore in caso di fallimento operazione --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger"><c:out value="${errorMessage}" /></div>
    </c:if>

    <div class="admin-reviews-section">
        <p class="section-subtitle">Visualizza e gestisci le recensioni rilasciate dagli utenti.</p>

        <%-- ── FILTRO UTENTI ────────────────────────────────────────────────── --%>
        <div class="filter-container">
            <form action="${pageContext.request.contextPath}/AdminRecensioniServlet" method="get" class="filter-form">
                <label for="filtroUtente">Filtra per Utente:</label>
                <%-- 'onchange' invia il form automaticamente quando l'utente cambia selezione --%>
                <select name="idUtente" id="filtroUtente" onchange="this.form.submit()">
                    <option value="">-- Tutti gli utenti --</option>
                    <c:forEach var="u" items="${tuttiUtenti}">
                        <option value="${u.idUtente}" ${param.idUtente == u.idUtente ? 'selected' : ''}>
                            <c:out value="${u.nome} ${u.cognome} (${u.email})" />
                        </option>
                    </c:forEach>
                </select>
                
                <%-- Mostra pulsante reset se un filtro è attualmente attivo --%>
                <c:if test="${not empty param.idUtente}">
                    <a href="${pageContext.request.contextPath}/AdminRecensioniServlet" class="btn-reset-filter">Mostra Tutti</a>
                </c:if>
            </form>
        </div>

        <%-- ── TABELLA ELENCO RECENSIONI ────────────────────────────────────── --%>
        <c:choose>
            <c:when test="${not empty tuteRecensioni}">
                <div class="admin-table-wrapper">
                    <table class="admin-table admin-reviews-table">
                        <thead>
                            <tr>
                                <th class="col-id">ID</th>
                                <th>Utente</th>
                                <th>Email</th>
                                <th>Prodotto</th>
                                <th class="text-center">Valutazione</th>
                                <th>Data</th>
                                <th>Descrizione</th>
                                <th class="text-right col-actions">Azioni</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="rec" items="${tuteRecensioni}">
                                <tr>
                                    <td><strong>#<c:out value="${rec.idRecensione}" /></strong></td>
                                    <td><c:out value="${rec.nomeUtente} ${rec.cognomeUtente}" default="N/D" /></td>
                                    <td><c:out value="${rec.emailUtente}" default="N/D" /></td>
                                    <td><c:out value="${rec.nomeProdotto}" default="N/D" /></td>
                                    
                                    <%-- Generazione dinamica della valutazione a stelle --%>
                                    <td class="text-center">
                                        <div class="stars">
                                            <c:forEach begin="1" end="${rec.valutazione}">★</c:forEach>
                                        </div>
                                    </td>
                                    
                                    <%-- Formattazione data recensione --%>
                                    <td>
                                        <fmt:formatDate value="${rec.dataRecensione}" pattern="dd/MM/yyyy HH:mm" />
                                    </td>
                                    
                                    <td class="review-description">
                                        <c:out value="${rec.descrizione}" />
                                    </td>
                                    
                                    <%-- Form per eliminazione singola recensione --%>
                                    <td class="text-right">
                                        <form action="${pageContext.request.contextPath}/AdminRecensioniServlet" method="post" class="action-form" onsubmit="return confirm('Sei sicuro di voler eliminare questa recensione?');">
                                            <input type="hidden" name="action" value="delete" />
                                            <input type="hidden" name="idRecensione" value="${rec.idRecensione}" />
                                            <%-- Mantiene il filtro utente attivo anche dopo l'eliminazione --%>
                                            <input type="hidden" name="idUtente" value="${param.idUtente}" />
                                            <button type="submit" class="btn-delete">Elimina</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            
            <%-- Stato visualizzato se non ci sono recensioni --%>
            <c:otherwise>
                <div class="empty-state">
                    <p class="empty-table-msg">Nessuna recensione trovata per il criterio selezionato.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<%-- Inclusione del piè di pagina --%>
<%@ include file="/jsp/common/footer.jspf" %>