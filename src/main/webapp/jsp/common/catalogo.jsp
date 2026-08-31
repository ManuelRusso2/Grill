<%-- 
    Pagina di visualizzazione del catalogo prodotti (lato utente/front-end).
    Consente agli utenti di esplorare i prodotti attivi, filtrati per categoria (se selezionata),
    visualizzarne il prezzo formattato, l'immagine di anteprima e la disponibilità a magazzino.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Importazione della libreria JSTL Core per il controllo di flusso, cicli e condizioni --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Importazione della libreria JSTL Formatting per la formattazione di date, numeri e valute --%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Inclusione del frammento statico per l'intestazione HTML e le risorse della pagina (head, CSS, JS) --%>
<%@ include file="/jsp/common/header.jspf" %>

<%-- Inclusione del frammento statico per la barra di navigazione principale (menu / navbar) --%>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <%-- Titolo dinamico: mostra il nome della categoria selezionata oppure 'Catalogo Prodotti' --%>
    <c:choose>
        <c:when test="${not empty categoriaAttiva}">
            <h1><c:out value="${categoriaAttiva.nome}" /></h1>
        </c:when>
        <c:otherwise>
            <h1>Catalogo Prodotti</h1>
        </c:otherwise>
    </c:choose>

    <%-- ── PRE-CHECK DISPONIBILITÀ PRODOTTI ────────────────────────────────── --%>
    <%-- Scansione preliminare per verificare se esiste almeno un prodotto attivo/visibile --%>
    <c:set var="hasProdottiAttivi" value="false" />
    <c:forEach var="checkP" items="${prodotti}">
        <c:if test="${checkP.attivo}">
            <c:set var="hasProdottiAttivi" value="true" />
        </c:if>
    </c:forEach>

    <%-- ── GRIGLIA CATALOGO ────────────────────────────────────────────────── --%>
    <c:choose>
        <%-- Caso 1: È presente almeno un prodotto attivo da mostrare --%>
        <c:when test="${hasProdottiAttivi}">
            <div class="grid">
                <c:forEach var="p" items="${prodotti}">
                    <%-- Filtra solo i prodotti impostati come 'attivo' nel database --%>
                    <c:if test="${p.attivo}">
                        <%-- Creazione URL dinamico per la pagina di dettaglio --%>
                        <c:set var="detailUrl" value="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${p.idProdotto}" />
                        
                        <%-- Aggiunge il context path dell'applicazione (percorso completo) al percorso dell'immagine --%>
                        <c:set var="imgSrc" value="${pageContext.request.contextPath}/${p.immagine}" />
                        
                        <div class="card">
                            <%-- Anteprima Immagine del prodotto con fallback gestito da 'onerror' --%>
                            <a href="${detailUrl}">
                                <img class="product-thumb" 
                                     src="${imgSrc}" 
                                     alt="<c:out value='${p.nome}'/>" 
                                     onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/default.jpg';" />
                            </a>
                            
                            <%-- Titolo del prodotto con link alla scheda dettaglio --%>
                            <h3>
                                <a href="${detailUrl}">
                                    <c:out value="${p.nome}" />
                                </a>
                            </h3>
                            
                            <%-- Prezzo formattato in Valuta Euro (€) --%>
                            <p class="price">
                                <fmt:formatNumber value="${p.costo}" type="currency" currencySymbol="€" />
                            </p>

                            <%-- Badge dinamico sullo stato dello stock di magazzino --%>
                            <c:choose>
                                <c:when test="${p.quantita <= 0}">
                                    <span class="badge badge-esaurito">Esaurito</span>
                                </c:when>
                                <c:when test="${p.quantita <= 5}">
                                    <span class="badge badge-scarso">Ultimi <c:out value="${p.quantita}" /> disponibili</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge badge-disponibile">Disponibile (<c:out value="${p.quantita}" />)</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>
                </c:forEach>
            </div>
        </c:when>
        
        <%-- Caso 2: Nessun prodotto attivo presente nella categoria o nel catalogo --%>
        <c:otherwise>
            <div class="empty-state">
                <div class="empty-state-icon">
                    <img src="${pageContext.request.contextPath}/images/icons/search.svg" alt="Catalogo vuoto" onerror="this.style.display='none';" />
                </div>
                <h2>Nessun Prodotto Trovato</h2>
                <p>Al momento non ci sono prodotti disponibili nel catalogo o nella categoria selezionata.</p>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%-- Inclusione del piè di pagina --%>
<%@ include file="/jsp/common/footer.jspf" %>