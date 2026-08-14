<%-- 
    Pagina di gestione/amministrazione delle categorie.
    Consente agli utenti autorizzati (Admin) di visualizzare l'elenco delle categorie presenti,
    accedere al form per crearne una nuova, modificarne una esistente o procedere con l'eliminazione.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Inclusione della Tag Library JSTL Core per le strutture di controllo e output --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Inclusione del frammento di intestazione (Header HTML, meta-tag, stylesheet, JS globali) --%>
<%@ include file="/jsp/common/header.jspf" %>

<%-- Inclusione del menu di navigazione principale --%>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Gestione Categorie</h1>

    <%-- ── MESSAGGI DI NOTIFICA ED ESITO OPERAZIONI ────────────────────────────── --%>
    
    <%-- Banner di successo (mostrato ad esempio dopo la creazione/modifica/eliminazione) --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">
            <c:out value="${successMessage}" />
        </div>
    </c:if>

    <%-- Banner di errore (mostrato in caso di eccezioni o vincoli di integrità referenziale) --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- ── BARRA DEGLI STRUMENTI AMMINISTRATIVA ────────────────────────────── --%>
    <div class="admin-toolbar">
        <h2>Elenco Categorie</h2>
        <%-- Pulsante per il reindirizzamento al form di inserimento di una nuova categoria --%>
        <a href="${pageContext.request.contextPath}/AdminCategoriaServlet?action=new" class="btn btn-small">➕ Nuova Categoria</a>
    </div>

    <%-- ── TABELLA ELENCO CATEGORIE ────────────────────────────────────────── --%>
    <div class="admin-table-wrapper">
        <table class="admin-table">
            <thead>
                <tr>
                    <th class="col-id">ID</th>
                    <th class="col-nome">Nome</th>
                    <th>Descrizione</th>
                    <th class="text-right col-actions">Azioni</th>
                </tr>
            </thead>
            <tbody>
                <%-- Controllo se la lista 'categorie' recuperata dal controller contiene elementi --%>
                <c:choose>
                    <c:when test="${not empty categorie}">
                        <%-- Ciclo di iterazione sulla collezione di categorie (List<CategoriaBean>) --%>
                        <c:forEach var="cat" items="${categorie}">
                            <tr>
                                <%-- ID Categoria con sanificazione dei caratteri speciali tramite c:out --%>
                                <td><strong>#<c:out value="${cat.idCategoria}"/></strong></td>
                                
                                <%-- Nome della categoria --%>
                                <td><c:out value="${cat.nome}"/></td>
                                
                                <%-- Descrizione della categoria --%>
                                <td><c:out value="${cat.descrizione}"/></td>
                                
                                <%-- Cella azioni (Modifica / Elimina) --%>
                                <td class="text-right">
                                    <div class="action-cell">
                                        <%-- Link per la modifica della categoria mediante la Servlet --%>
                                        <a href="${pageContext.request.contextPath}/AdminCategoriaServlet?action=edit&id=${cat.idCategoria}" class="btn-edit">Modifica</a>
                                        
                                        <%-- Form POST per l'eliminazione sicura con conferma tramite popup JavaScript inline --%>
                                        <form method="post" action="${pageContext.request.contextPath}/AdminCategoriaServlet" class="action-form" onsubmit="return confirm('Sei sicuro di voler eliminare la categoria ${cat.nome}?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${cat.idCategoria}">
                                            <button type="submit" class="btn-delete">Elimina</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    
                    <%-- Blocco alternativo visualizzato in caso di lista vuota o null --%>
                    <c:otherwise>
                        <tr>
                            <td colspan="4" class="empty-table-msg">Nessuna categoria presente nel database.</td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</main>

<%-- Inclusione del piè di pagina (Footer e script di chiusura) --%>
<%@ include file="/jsp/common/footer.jspf" %>