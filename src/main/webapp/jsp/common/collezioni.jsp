<%-- 
    Pagina di visualizzazione delle Collezioni (lato utente/front-end).
    Organizza i prodotti raggruppandoli per collezioni dinamiche (struttura Map<Collezione, List<Prodotto>>).
    Per ciascuna collezione attiva mostra titolo, descrizione facoltativa e la griglia dei prodotti associati.
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
    <%-- Titolo principale della pagina --%>
    <h1 class="text-center page-title">LE NOSTRE COLLEZIONI</h1>

    <%-- ── CONTROLLO PRESENZA COLLEZIONI ───────────────────────────────────── --%>
    <c:choose>
        <%-- Caso 1: La mappa 'collezioniMap' esiste e contiene almeno una collezione --%>
        <c:when test="${not empty collezioniMap}">
            
            <%-- Iterazione su ogni Entry (chiave: Oggetto Collezione, valore: Lista di Prodotti) --%>
            <c:forEach var="entry" items="${collezioniMap}">
                <section class="collection-block">
                    
                    <%-- Intestazione della singola Collezione (Estratta dalla chiave entry.key) --%>
                    <h2 class="collection-title">
                        <c:out value="${entry.key.nomeCollezione}" />
                    </h2>

                    <%-- Descrizione facoltativa della Collezione (mostrata solo se presente) --%>
                    <c:if test="${not empty entry.key.descrizione}">
                        <p class="collection-desc">
                            <c:out value="${entry.key.descrizione}" />
                        </p>
                    </c:if>

                    <%-- ── GRIGLIA PRODOTTI DELLA COLLEZIONE ───────────────────── --%>
                    <div class="grid">
                        <%-- Iterazione sulla lista dei prodotti appartenenti alla collezione corrente (entry.value) --%>
                        <c:forEach var="p" items="${entry.value}">
                            
                            <%-- Rendering condizionale: mostra solo i prodotti contrassegnati come attivi nel DB --%>
                            <c:if test="${p.attivo}">
                                <%-- Generazione dell'URL dinamico per la Servlet di Dettaglio Prodotto --%>
                                <c:set var="detailUrl" value="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${p.idProdotto}" />
                                
                                <%-- Normalizzazione del path dell'immagine senza operatore ternario --%>
                                <c:choose>
                                    <c:when test="${p.immagine.startsWith('http://') || p.immagine.startsWith('https://')}">
                                        <c:set var="imgSrc" value="${p.immagine}" />
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="imgSrc" value="${pageContext.request.contextPath}/${p.immagine}" />
                                    </c:otherwise>
                                </c:choose>

                                <div class="card product-card">
                                    <%-- Anteprima Immagine del prodotto con fallback automatico su errore di caricamento --%>
                                    <a href="${detailUrl}">
                                        <img class="product-thumb" 
                                             src="${imgSrc}" 
                                             alt="<c:out value='${p.nome}'/>" 
                                             onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/default.jpg';" />
                                    </a>
                                    
                                    <%-- Nome/Titolo del Prodotto cliccabile --%>
                                    <h3>
                                        <a href="${detailUrl}">
                                            <c:out value="${p.nome}" />
                                        </a>
                                    </h3>
                                    
                                    <%-- Prezzo del prodotto formattato come valuta Euro (€) --%>
                                    <p class="price">
                                        <fmt:formatNumber value="${p.costo}" type="currency" currencySymbol="€" />
                                    </p>

                                    <%-- Badge informativo sulla disponibilità a magazzino del prodotto --%>
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
                </section>
            </c:forEach>
        </c:when>
        
        <%-- Caso 2: Nessuna collezione disponibile o presente in memoria --%>
        <c:otherwise>
            <div class="empty-state">
                <p>Al momento non ci sono collezioni attive con prodotti disponibili.</p>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%-- Inclusione del frammento del Footer finale --%>
<%@ include file="/jsp/common/footer.jspf" %>