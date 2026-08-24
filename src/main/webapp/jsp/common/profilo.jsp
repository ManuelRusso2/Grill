<%-- Impostazione del tipo di contenuto della pagina e della codifica dei caratteri (UTF-8) --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Tag Library JSTL per il controllo di flusso e la formattazione di date/valute --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Inclusione dei frammenti di codice statici per l'intestazione (header) e la barra di navigazione (menu) --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale dell'area personale utente --%>
<main class="container">
    <h1>Profilo Utente</h1>
    
	<%-- ── SEZIONE 1: DETTAGLI PROFILO UTENTE ─────────────────────────────── --%>
    <%-- Mostra la scheda informativa solo se l'utente è autenticato in sessione --%>
    <c:if test="${not empty sessionScope.utente}">
        <div class="profile-card">
            <h2>Informazioni Personali</h2>
            <div class="profile-details">
                
                <%-- Nome dell'utente --%>
                <div class="detail-row">
                    <span class="detail-label">Nome:</span>
                    <span class="detail-value"><c:out value="${sessionScope.utente.nome}" /></span>
                </div>

                <%-- Cognome dell'utente --%>
                <div class="detail-row">
                    <span class="detail-label">Cognome:</span>
                    <span class="detail-value"><c:out value="${sessionScope.utente.cognome}" /></span>
                </div>

                <%-- Indirizzo Email --%>
                <div class="detail-row">
                    <span class="detail-label">Email:</span>
                    <span class="detail-value"><c:out value="${sessionScope.utente.email}" /></span>
                </div>

                <%-- Numero di telefono (mostrato opzionalmente se presente) --%>
                <c:if test="${not empty sessionScope.utente.telefono}">
                    <div class="detail-row">
                        <span class="detail-label">Telefono:</span>
                        <span class="detail-value"><c:out value="${sessionScope.utente.telefono}" /></span>
                    </div>
                </c:if>

            </div>
        </div>
    </c:if>

    <%-- ── SEZIONE 2: STORICO ORDINI ───────────────────────────────────────── --%>
    <div class="orders-section">
        <h2 class="section-title">Storico Ordini</h2>
        <p class="section-subtitle">Qui trovi lo storico dei tuoi acquisti effettuati su Grill.</p>

        <%-- Tabella riassuntiva degli ordini effettuati --%>
        <table class="cart-table orders-table">
            <thead>
                <tr>
                    <th>ID Ordine</th>
                    <th>Data</th>
                    <th>Totale</th>
                    <th>Dettaglio</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <%-- CASO 1: L'utente possiede almeno un ordine nello storico --%>
                    <c:when test="${not empty acquisti}">
                        <c:forEach var="a" items="${acquisti}">
                            <tr>
                                <%-- Identificativo univoco dell'ordine --%>
                                <td>#<c:out value="${a.idAcquisto}" /></td>
                                
                                <%-- Data dell'acquisto formattata nel formato italiano --%>
                                <td>
                                    <fmt:formatDate value="${a.dataAcquisto}" pattern="dd/MM/yyyy HH:mm" />
                                </td>
                                
                                <%-- Prezzo totale dell'ordine formattato in Euro (€) --%>
                                <td class="order-price">
                                    <fmt:formatNumber value="${a.prezzoTotale}" type="currency" currencySymbol="€" />
                                </td>
                                
                                <%-- Link alla pagina di dettaglio del singolo ordine --%>
                                <td>
                                    <a href="${pageContext.request.contextPath}/DettaglioOrdineServlet?id=${a.idAcquisto}" class="order-link">
                                        Visualizza ➔
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>

                    <%-- CASO 2: Nessun ordine presente nello storico --%>
                    <c:otherwise>
                        <tr>
                            <td colspan="4" class="empty-table-msg">
                                Non hai ancora effettuato nessun ordine su Grill.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>

    <%-- ── SEZIONE 3: RECENSIONI PERSONALI ────────────────────────────────── --%>
    <div class="reviews-section">
        <h2 class="section-title">Le tue Recensioni</h2>
        <p class="section-subtitle">Qui trovi tutte le recensioni che hai lasciato sui prodotti di Grill. Puoi modificarle o rimuoverle.</p>

        <c:choose>
            <%-- CASO 1: L'utente ha pubblicato una o più recensioni --%>
            <c:when test="${not empty recensioniUtente}">
                <c:forEach var="rec" items="${recensioniUtente}">
                    <div class="user-review">
                        
                        <%-- Intestazione della scheda recensione (Nome Prodotto e Data) --%>
                        <div class="review-header">
                            <strong><c:out value="${rec.nomeProdotto}" /></strong>
                            <span class="review-date">
                                <fmt:formatDate value="${rec.dataRecensione}" pattern="dd/MM/yyyy HH:mm" />
                            </span>
                        </div>

                        <%-- Corpo della recensione (Valutazione in stelle e Testo) --%>
                        <div class="review-body">
                            <div class="review-rating">
                                <c:forEach begin="1" end="${rec.valutazione}">★</c:forEach>
                            </div>
                            <p class="review-text"><c:out value="${rec.descrizione}" /></p>
                        </div>

                        <%-- Pulsanti di azione riservati all'autore della recensione o all'Amministratore --%>
						<div class="review-actions">
						    <c:if test="${not empty sessionScope.utente && (sessionScope.utente.idUtente == rec.idUtente || sessionScope.utente.admin)}">
						        
						        <%-- Link diretto per la modifica --%>
						        <a href="${pageContext.request.contextPath}/ModificaRecensioneServlet?idRecensione=${rec.idRecensione}" class="btn-action">
						            Modifica
						        </a>
						
						        <%-- Form POST per l'eliminazione con parametro nell'action --%>
						        <form action="${pageContext.request.contextPath}/EliminaRecensioneServlet?idRecensione=${rec.idRecensione}" method="post" onsubmit="return confirm('Sei sicuro di voler eliminare questa recensione?');">
						            <button type="submit" class="btn-action btn-delete">Elimina</button>
						        </form>
						
						    </c:if>
						</div>
						
                    </div>
                </c:forEach>
            </c:when>

            <%-- CASO 2: L'utente non ha ancora recensito alcun prodotto --%>
            <c:otherwise>
                <p class="no-reviews-msg">Non hai ancora lasciato recensioni.</p>
            </c:otherwise>
        </c:choose>
    </div>
    
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>