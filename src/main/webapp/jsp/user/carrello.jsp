<%-- Impostazione del tipo di contenuto della pagina e della codifica dei caratteri (UTF-8) --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Inclusione delle librerie di tag JSTL per la logica di controllo e la formattazione di valute/numeri --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Inclusione dei frammenti di codice statici per l'intestazione (header) e la barra di navigazione (menu) --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale della pagina del carrello --%>
<main class="container">
    <h1>Il tuo Carrello</h1>

    <%-- ── 1. BLOCCO FEEDBACK UTENTE (Flash Messages) ─────────────────────── --%>
    
    <%-- Messaggio di successo (es. prodotto aggiornato o rimosso correttamente) --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            <c:out value="${successMessage}" />
        </div>
    </c:if>

    <%-- Messaggio di errore (es. quantità richiesta non disponibile a magazzino) --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- ── 2. GESTIONE STATO DEL CARRELLO ────────────────────────────────── --%>
    <c:choose>
        <%-- CASO 1: Il carrello contiene uno o più prodotti --%>
        <c:when test="${not empty prodottiCarrello}">
            
            <%-- Inizializzazione della variabile di scope pagina per il calcolo del totale complessivo --%>
            <c:set var="totaleCarrello" value="0" scope="page" />

            <%-- Tabella riepilogativa degli articoli presenti nel carrello --%>
            <table class="cart-table">
                <thead>
                    <tr>
                        <th>Prodotto</th>
                        <th>Prezzo Unitario</th>
                        <th>Quantità</th>
                        <th>Subtotale</th>
                        <th>Azioni</th>
                    </tr>
                </thead>
                <tbody>
                    <%-- Iterazione sulla mappa contenente le coppie <ProdottoBean, Integer (Quantità)> --%>
                    <c:forEach var="entry" items="${prodottiCarrello}">
                        <c:set var="prodotto" value="${entry.key}" />
                        <c:set var="quantita" value="${entry.value}" />
                        
                        <%-- Calcolo del subtotale per la singola riga (prezzo unitario * quantità) --%>
                        <c:set var="subtotale" value="${prodotto.costo * quantita}" />
                        
                        <%-- Accumulo progressivo per il calcolo del totale complessivo dell'ordine --%>
                        <c:set var="totaleCarrello" value="${totaleCarrello + subtotale}" />

                        <tr>
                            <%-- Colonna Nome Prodotto e Taglia Selezionata --%>
                            <td>
                                <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${prodotto.idProdotto}" class="cart-product-title">
                                    <c:out value="${prodotto.nome}" />
                                </a>
                                <%-- Mostra la taglia scelta sotto il titolo, se presente --%>
                                <c:if test="${not empty prodotto.tagliaSelezionata}">
                                    <span class="cart-product-size">Taglia: <c:out value="${prodotto.tagliaSelezionata}" /></span>
                                </c:if>
                            </td>

                            <%-- Colonna Prezzo Unitario formattato in Euro (€) --%>
                            <td>
                                <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" />
                            </td>

                            <%-- Colonna Modifica Quantità --%>
							<td>
							    <%-- Form per l'invio dell'aggiornamento della quantità alla CarrelloServlet --%>
							    <form method="post" action="${pageContext.request.contextPath}/CarrelloServlet?action=update&idProdotto=${prodotto.idProdotto}&taglia=${prodotto.tagliaSelezionata}" class="cart-update-form">
							        <input type="number" name="quantita" value="${quantita}" min="1" max="${prodotto.quantita}" class="input-qty" aria-label="Quantità">
							        <button type="submit" class="btn-update">Aggiorna</button>
							    </form>
							</td>

                            <%-- Colonna Subtotale di riga formattato in Euro (€) --%>
                            <td>
                                <fmt:formatNumber value="${subtotale}" type="currency" currencySymbol="€" />
                            </td>

                            <%-- Colonna Azioni (Rimozione del singolo articolo) --%>
							<td>
							    <form method="post" action="${pageContext.request.contextPath}/CarrelloServlet?action=remove&idProdotto=${prodotto.idProdotto}&taglia=${prodotto.tagliaSelezionata}" class="cart-remove-form">
							        <button type="submit" class="btn-delete" onclick="return confirm('Rimuovere questo prodotto dal carrello?');">
							            Rimuovi
							        </button>
							    </form>
							</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <%-- Riquadro di riepilogo del totale e azioni globali sul carrello --%>
            <div class="cart-summary">
                <h2>Totale Ordine: <span><fmt:formatNumber value="${totaleCarrello}" type="currency" currencySymbol="€" /></span></h2>
                
                <div class="cart-actions-group">
                    <%-- Pulsante per procedere alla fase di checkout dell'ordine --%>
                    <a href="${pageContext.request.contextPath}/CheckoutServlet" class="btn btn-checkout">
                        Procedi al Checkout
                    </a>

                    <%-- Form per svuotare interamente il carrello con conferma JavaScript --%>
					<form method="post" action="${pageContext.request.contextPath}/CarrelloServlet?action=empty">
					    <button type="submit" class="btn-empty-cart" onclick="return confirm('Sei sicuro di voler svuotare completamente il carrello?');">
					        Svuota Carrello
					    </button>
					</form>
                </div>
            </div>
        </c:when>

        <%-- CASO 2: Il carrello è vuoto --%>
        <c:otherwise>
            <div class="empty-state">
                <div class="empty-state-icon">
                    <%-- Immagine di fallback in caso di errore di caricamento dell'icona del carrello vuoto --%>
                    <img src="${pageContext.request.contextPath}/images/carrello.png" 
                         alt="Carrello Vuoto" 
                         onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/default.jpg';" />
                </div>
                <h2>Il tuo carrello è vuoto</h2>
                <p>Non hai ancora aggiunto nessun articolo. Esplora il nostro catalogo per scoprire tutti i prodotti disponibili!</p>
                <a href="${pageContext.request.contextPath}/CatalogoServlet" class="btn btn-catalog">
                    Torna al Catalogo &rarr;
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>