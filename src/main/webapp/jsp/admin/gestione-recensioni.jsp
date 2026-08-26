<%-- 
    Pagina di gestione e amministrazione delle recensioni degli utenti.
    Area riservata agli amministratori (Admin) per filtrare, monitorare 
    ed eventualmente rimuovere (moderare) le recensioni inappropriate.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Importazione della libreria JSTL Core per il controllo di flusso (cicli, if-else, ecc.) --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Importazione della libreria JSTL Formatting per la formattazione localizzata di date e valute --%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Inclusione del frammento statico per l'intestazione HTML e il caricamento risorse (CSS, JS) --%>
<%@ include file="/jsp/common/header.jspf" %>

<%-- Inclusione del frammento statico per la barra di navigazione principale (menu / navbar) --%>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale della pagina del pannello di amministrazione --%>
<main class="container">
    <h1>Gestione Recensioni</h1>

    <%-- ── MESSAGGI DI FEEDBACK (NOTIFICHE LATO SERVER) ─────────────────────── --%>
    <%-- Banner di successo: mostrato, ad esempio, dopo l'eliminazione di una recensione --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            ✓ <c:out value="${successMessage}" />
        </div>
    </c:if>
    <%-- Banner d'errore: mostrato in caso di fallimento di un'operazione sul database --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            ✗ <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <div class="admin-reviews-section">
        <p class="section-subtitle">Visualizza, filtra e gestisci i feedback rilasciati dagli utenti sui prodotti.</p>

        <%-- ── FILTRO DI RICERCA PER UTENTE ─────────────────────────────────── --%>
        <div class="filter-container">
            <%-- Il form invia i parametri in GET alla stessa Servlet per filtrare la tabella --%>
            <form action="${pageContext.request.contextPath}/AdminRecensioniServlet" method="get" class="filter-form">
                
                <label for="filtroUtente">Filtra per Utente:</label>
                
                <%-- Selettore dinamico: 'onchange' scatena l'invio automatico (submit) del form al cambio di valore --%>
                <select name="idUtente" id="filtroUtente" class="form-control" style="max-width: 300px; display: inline-block; margin: 0 10px;" onchange="this.form.submit()">
                    <option value="">-- Tutti gli utenti --</option>
                    <%-- Iterazione sulla lista globale degli utenti per popolare le opzioni del filtro --%>
                    <c:forEach var="u" items="${tuttiUtenti}">
                        <option value="${u.idUtente}" ${param.idUtente == u.idUtente ? 'selected' : ''}>
                            <c:out value="${u.nome} ${u.cognome} (${u.email})" />
                        </option>
                    </c:forEach>
                </select>
                
                <%-- Se è attivo un filtro (param.idUtente non vuoto), mostra il pulsante di Reset --%>
                <c:if test="${not empty param.idUtente}">
                    <a href="${pageContext.request.contextPath}/AdminRecensioniServlet" class="btn btn-sm btn-secondary">Mostra Tutti</a>
                </c:if>
            </form>
        </div>

        <%-- ── TABELLA ELENCO RECENSIONI ────────────────────────────────────── --%>
        <c:choose>
            <%-- CASO 1: Esistono recensioni (eventualmente filtrate) da mostrare --%>
            <c:when test="${not empty tutteRecensioni}">
                <div class="admin-table-wrapper">
                    <table class="admin-table">
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
                            <%-- Iterazione sulla lista dei feedback da mostrare nella tabella --%>
                            <c:forEach var="rec" items="${tutteRecensioni}">
                                <tr>
                                    <%-- Identificativo univoco della recensione --%>
                                    <td><strong>#<c:out value="${rec.idRecensione}" /></strong></td>
                                    
                                    <%-- Dati anagrafici e di contatto dell'utente che ha lasciato la recensione --%>
                                    <td><c:out value="${rec.nomeUtente} ${rec.cognomeUtente}" default="N/D" /></td>
                                    <td><c:out value="${rec.emailUtente}" default="N/D" /></td>
                                    
                                    <%-- Nome del prodotto oggetto della recensione --%>
                                    <td><c:out value="${rec.nomeProdotto}" default="N/D" /></td>
                                    
                                    <%-- Valutazione numerica convertita visivamente in stelle piene --%>
                                    <td class="text-center">
                                        <div class="review-stars stars">
                                            <c:forEach begin="1" end="${rec.valutazione}">★</c:forEach>
                                        </div>
                                    </td>
                                    
                                    <%-- Data e ora in cui è stata rilasciata la recensione --%>
                                    <td>
                                        <fmt:formatDate value="${rec.dataRecensione}" pattern="dd/MM/yyyy HH:mm" />
                                    </td>
                                    
                                    <%-- Testo del commento rilasciato dall'utente --%>
                                    <td class="review-description">
                                        <c:out value="${rec.descrizione}" />
                                    </td>
                                    
                                    <%-- Colonna Azioni: Modulo indipendente per l'eliminazione forzata della recensione --%>
                                    <td class="text-right">
                                        <div class="action-cell">
                                            <%-- Il form mantiene il parametro del filtro attivo 'idUtente' per non perdere la selezione dopo il riavvio della pagina --%>
                                            <form action="${pageContext.request.contextPath}/AdminRecensioniServlet?action=delete&idRecensione=${rec.idRecensione}&idUtente=${param.idUtente}" method="post" class="form-unstyled" onsubmit="return confirm('Sei sicuro di voler eliminare questa recensione in modo permanente?');">
                                                <button type="submit" class="btn btn-sm btn-outline-danger">Elimina</button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            
            <%-- CASO 2: La lista recensioni è vuota (perchè non ce ne sono o a causa dei filtri stringenti) --%>
            <c:otherwise>
                <div class="empty-state">
                    <p class="empty-table-msg">Nessuna recensione trovata per il criterio selezionato.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>