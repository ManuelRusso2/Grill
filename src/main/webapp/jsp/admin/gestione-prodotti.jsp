<%-- 
    Pagina di gestione/amministrazione dei prodotti del catalogo.
    Consente agli utenti autorizzati (Admin) di:
    - Rilevare subito una sezione di avviso per i prodotti esauriti (quantità <= 0).
    - Consultare la lista completa dei prodotti con categorie, prezzi e stato (attivo/inattivo).
    - Cercare e filtrare i prodotti in tempo reale tramite uno script JavaScript integrato.
    - Creare, modificare, rifornire ed eliminare articoli.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Inclusione delle Tag Library JSTL per controllo flusso (core), formattazione numeri/valute e funzioni di utilità --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- Inclusione dei frammenti statici per l'intestazione HTML e la barra di navigazione --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Gestione Prodotti</h1>

    <%-- ── MESSAGGI DI NOTIFICA ED ESITO OPERAZIONI ────────────────────────────── --%>
    
    <%-- Banner di conferma operazione (es. prodotto aggiornato, inserito o eliminato con successo) --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            ✓ <c:out value="${successMessage}" />
        </div>
    </c:if>

    <%-- Banner di errore (es. fallimento salvataggio o vincoli di database) --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            ✗ <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- ── SEZIONE PRODOTTI ESAURITI (SOTTO SCORTA) ────────────────────────── --%>
    
    <%-- Calcolo preventivo della presenza di almeno un prodotto con quantità <= 0 --%>
    <c:set var="hasEsauriti" value="false" />
    <c:forEach var="p" items="${prodottiAdmin}">
        <c:if test="${p.quantita <= 0}">
            <c:set var="hasEsauriti" value="true" />
        </c:if>
    </c:forEach>

    <%-- Rendering del box di avviso solo in presenza di articoli esauriti --%>
    <c:if test="${hasEsauriti}">
        <div class="alert-box-out-of-stock">
            <h2 class="text-danger">⚠️ Prodotti Esauriti</h2>
            <div class="admin-table-wrapper">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th class="col-id">ID</th>
                            <th>Nome</th>
                            <th class="text-center">Taglie</th>
                            <th>Prezzo</th>
                            <th class="text-center">Quantità</th>
                            <th class="text-center">Stato</th>
                            <th class="text-center col-actions">Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="prodotto" items="${prodottiAdmin}">
                            <c:if test="${prodotto.quantita <= 0}">
                                <tr>
                                    <td><strong>#<c:out value="${prodotto.idProdotto}" /></strong></td>
                                    <td><strong><c:out value="${prodotto.nome}" /></strong></td>
                                    <td class="text-center">
                                        <c:out value="${empty prodotto.taglie ? 'Unica' : prodotto.taglie}" />
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" />
                                    </td>
                                    <td class="text-center text-danger font-bold">0</td>
                                    <td class="text-center">
                                        <span class="badge-esaurito">Esaurito</span>
                                    </td>
                                    <td class="text-center">
                                        <div class="action-cell action-cell-center">
                                            <%-- Reindirizzamento diretto alla form di modifica per velocizzare il rifornimento --%>
                                            <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=edit&id=${prodotto.idProdotto}" class="btn-edit">
                                                ✏️ Rifornisci / Modifica
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:if>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>

    <%-- ── CATALOGO COMPLETO PRODOTTI ──────────────────────────────────────── --%>
    <c:choose>
        <c:when test="${not empty prodottiAdmin}">
            <%-- Barra degli strumenti con ricerca client-side e pulsante nuovo prodotto --%>
            <div class="admin-toolbar">
                <h2>Tutti i Prodotti</h2>
                
                <div class="admin-toolbar-actions">
                    <input type="text" id="searchProductInput" placeholder="🔍 Cerca per nome, ID..." class="admin-search-input">
                    <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=new" class="btn btn-add">
                        + Aggiungi Nuovo Prodotto
                    </a>
                </div>
            </div>

            <div class="admin-table-wrapper">
                <table class="admin-table" id="mainProductsTable">
                    <thead>
                        <tr>
                            <th class="col-id">ID</th>
                            <th>Nome</th>
                            <th class="col-desc">Descrizione</th>
                            <th class="text-center">Taglie</th>
                            <th>Prezzo</th>
                            <th class="text-center">Quantità</th>
                            <th class="text-center">Categorie</th>
                            <th class="text-center">Stato</th>
                            <th class="text-center col-actions">Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="prodotto" items="${prodottiAdmin}">
                            <tr>
                                <%-- ID e Nome del prodotto --%>
                                <td><strong>#<c:out value="${prodotto.idProdotto}" /></strong></td>
                                <td><strong><c:out value="${prodotto.nome}" /></strong></td>
                                
                                <%-- Descrizione sintetica del prodotto --%>
                                <td class="col-desc text-muted">
                                    <c:out value="${prodotto.descrizione}" />
                                </td>
                                
                                <%-- Gestione visualizzazione taglia singola o taglia unica di default --%>
                                <td class="text-center">
                                    <c:out value="${empty prodotto.taglie ? 'Unica' : prodotto.taglie}" />
                                </td>
                                
                                <%-- Prezzo di vendita formattato come valuta --%>
                                <td>
                                    <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" />
                                </td>
                                
                                <%-- Quantità attualmente disponibile in magazzino --%>
                                <td class="text-center">
                                    <c:out value="${prodotto.quantita}" />
                                </td>
                                
                                <%-- Formattazione elenco delle categorie collegate tramite virgole --%>
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${not empty prodotto.categorie}">
                                            <c:forEach var="cat" items="${prodotto.categorie}" varStatus="s">
                                                <c:out value="${cat.nome}" /><c:if test="${!s.last}">, </c:if>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </td>
                                
                                <%-- Badge di stato (Attivo/Inattivo) --%>
                                <td class="text-center">
                                    <span class="${prodotto.attivo ? 'status-attivo' : 'badge-esaurito'}">
                                        ${prodotto.attivo ? 'Attivo' : 'Inattivo'}
                                    </span>
                                </td>
                                
                                <%-- Azioni disponibili per la riga --%>
                                <td class="text-center">
                                    <div class="action-cell action-cell-center">
                                        <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=edit&id=${prodotto.idProdotto}" class="btn-edit">
                                            Modifica
                                        </a>

                                        <%-- Form per l'eliminazione con escape dei caratteri speciali nel nome del prodotto prima di darlo in pasto a JS --%>
                                        <form method="post" action="${pageContext.request.contextPath}/AdminProdottoServlet" 
                                              onsubmit="return confirm('Sei sicuro di voler eliminare il prodotto \'${fn:escapeXml(prodotto.nome)}\'?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${prodotto.idProdotto}">
                                            <button type="submit" class="btn-delete">Elimina</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:when>

        <%-- Stato vuoto quando non sono presenti prodotti a sistema --%>
        <c:otherwise>
            <div class="empty-state">
                <p>Il catalogo è attualmente vuoto. Inizia ad aggiungere prodotti!</p>
                <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=new" class="btn btn-add">
                    + Aggiungi il Primo Prodotto
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%-- ── SCRIPT JS PER IL FILTRAGGIO CLIENT-SIDE DELLE RIGHE DELLA TABELLA ───────── --%>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        const input = document.getElementById('searchProductInput');
        const table = document.getElementById('mainProductsTable');
        if (!input || !table) return;

        // Ascolta l'evento di digitazione nell'input di ricerca per nascondere/mostrare le righe corrispondenti
        input.addEventListener('input', function() {
            const filter = this.value.toLowerCase().trim();
            const rows = table.querySelectorAll('tbody tr');

            rows.forEach(row => {
                row.style.display = row.textContent.toLowerCase().includes(filter) ? '' : 'none';
            });
        });
    });
</script>

<%-- Inclusione del frammento statico per il piè di pagina --%>
<%@ include file="/jsp/common/footer.jspf" %>