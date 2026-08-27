<%-- 
    Pagina di gestione/amministrazione dei prodotti del catalogo (Area Amministrativa).
    Consente agli utenti autorizzati (Admin) di:
    - Rilevare subito una sezione di avviso per i prodotti esauriti (quantità <= 0).
    - Consultare la lista completa dei prodotti con categorie, prezzi, aliquota IVA (%) e stato (attivo/inattivo).
    - Cercare e filtrare i prodotti in tempo reale tramite uno script JavaScript integrato.
    - Creare, modificare, rifornire ed eliminare articoli.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Importazione della libreria JSTL Core per il controllo di flusso, cicli e condizioni --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Importazione della libreria JSTL Formatting per la formattazione di date, numeri e valute --%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Importazione della libreria JSTL Functions per la manipolazione di stringhe e collezioni --%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- Inclusione del frammento statico per l'intestazione HTML e le risorse della pagina (head, CSS, JS) --%>
<%@ include file="/jsp/common/header.jspf" %>

<%-- Inclusione del frammento statico per la barra di navigazione principale (menu / navbar) --%>
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
                            <th>ID</th>
                            <th>Nome</th>
                            <th>Taglie</th>
                            <th>Prezzo</th>
                            <th>IVA</th>
                            <th>Quantità</th>
                            <th>Stato</th>
                            <th>Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="prodotto" items="${prodottiAdmin}">
                            <c:if test="${prodotto.quantita <= 0}">
                                <tr>
                                    <td><strong>#<c:out value="${prodotto.idProdotto}" /></strong></td>
                                    <td><strong><c:out value="${prodotto.nome}" /></strong></td>
                                    <td>
                                        <c:out value="${empty prodotto.taglie ? 'Unica' : prodotto.taglie}" />
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" />
                                    </td>
                                    <%-- Aliquota IVA percentuale del prodotto --%>
                                    <td>
                                        <c:out value="${prodotto.iva}" />%
                                    </td>
                                    <td>0</td>
                                    <td>
                                        <span class="badge-esaurito">Esaurito</span>
                                    </td>
                                    <td>
                                        <div class="action-cell">
                                            <%-- Reindirizzamento diretto al form di modifica per velocizzare il rifornimento --%>
                                            <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=edit&id=${prodotto.idProdotto}" class="btn btn-sm btn-outline-purple">
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
                <h2><strong>Tutti i Prodotti</strong></h2>
                
                <div class="admin-toolbar-actions">
                    <input type="text" id="searchProductInput" placeholder="🔍 Cerca per nome, ID..." class="admin-search-input">
                    <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=new" class="btn btn-md btn-primary">
                        + Aggiungi Nuovo Prodotto
                    </a>
                </div>
            </div>

            <div class="admin-table-wrapper">
                <table id="mainProductsTable" class="admin-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nome</th>
                            <th>Descrizione</th>
                            <th>Taglie</th>
                            <th>Prezzo</th>
                            <th>IVA</th>
                            <th>Quantità</th>
                            <th>Categorie</th>
                            <th>Stato</th>
                            <th>Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="prodotto" items="${prodottiAdmin}">
                            <tr>
                                <%-- ID e Nome del prodotto --%>
                                <td><strong>#<c:out value="${prodotto.idProdotto}" /></strong></td>
                                <td><strong><c:out value="${prodotto.nome}" /></strong></td>
                                
                                <%-- Descrizione sintetica del prodotto --%>
                                <td>
                                    <c:out value="${prodotto.descrizione}" />
                                </td>
                                
                                <%-- Gestione visualizzazione taglia singola o taglia unica di default --%>
                                <td>
                                    <c:out value="${empty prodotto.taglie ? 'Unica' : prodotto.taglie}" />
                                </td>
                                
                                <%-- Prezzo di vendita formattato come valuta --%>
                                <td>
                                    <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" />
                                </td>

                                <%-- Aliquota IVA percentuale del prodotto --%>
                                <td>
                                    <c:out value="${prodotto.iva}" />%
                                </td>
                                
                                <%-- Quantità attualmente disponibile in magazzino --%>
                                <td>
                                    <c:out value="${prodotto.quantita}" />
                                </td>
                                
                                <%-- Formattazione elenco delle categorie collegate tramite virgole --%>
                                <td>
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
                                <td>
                                    <span class="${prodotto.attivo ? 'status-attivo' : 'badge-esaurito'}">
                                        ${prodotto.attivo ? 'Attivo' : 'Inattivo'}
                                    </span>
                                </td>
                                
                                <%-- Azioni disponibili per la riga --%>
                                <td>
                                    <div class="action-cell">
                                        <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=edit&id=${prodotto.idProdotto}" class="btn btn-sm btn-outline-purple">
                                            Modifica
                                        </a>

                                        <%-- Form per l'eliminazione con escape dei caratteri speciali nel nome prima di passarli a JS --%>
                                        <form method="post" action="${pageContext.request.contextPath}/AdminProdottoServlet?action=delete&id=${prodotto.idProdotto}" 
                                              class="form-unstyled"
                                              onsubmit="return confirm('Sei sicuro di voler eliminare il prodotto \'${fn:escapeXml(prodotto.nome)}\'?');">
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

        <%-- Stato vuoto quando non sono presenti prodotti a sistema --%>
        <c:otherwise>
            <div class="empty-state">
                <p>Il catalogo è attualmente vuoto. Inizia ad aggiungere prodotti!</p>
                <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=new" class="btn btn-md btn-primary">
                    + Aggiungi il Primo Prodotto
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%-- ── SCRIPT JS PER IL FILTRAGGIO CLIENT-SIDE DELLE RIGHE DELLA TABELLA ───────── --%>
<script>
    // Attende il caricamento completo della struttura DOM dell'HTML prima di eseguire lo script
    document.addEventListener('DOMContentLoaded', function() {
        // Recupera i riferimenti all'input di ricerca e alla tabella principale dei prodotti
        const input = document.getElementById('searchProductInput');
        const table = document.getElementById('mainProductsTable');

        // Controlla la presenza di entrambi gli elementi per evitare errori JavaScript in console
        if (!input || !table) return;

        // Registra l'ascoltatore per l'evento 'input', che scatta istantaneamente a ogni digitazione
        input.addEventListener('input', function() {
            // Normalizza la stringa cercata: converte in minuscolo e rimuove gli spazi iniziali/finali
            const filter = this.value.toLowerCase().trim();

            // Seleziona tutte le righe contenute all'interno del corpo della tabella (tbody)
            const rows = table.querySelectorAll('tbody tr');

            // Scorre tutte le righe della tabella una alla volta
            for (let i = 0; i < rows.length; i++) {
                let row = rows[i];
                let testoRiga = row.textContent.toLowerCase();

                // Se il testo della riga contiene la parola cercata
                if (testoRiga.includes(filter)) {
                    row.style.display = '';      // Mostra la riga
                } else {
                    row.style.display = 'none';  // Nascondi la riga
                }
            }
        });
    });
</script>

<%-- Inclusione del frammento statico per il piè di pagina --%>
<%@ include file="/jsp/common/footer.jspf" %>