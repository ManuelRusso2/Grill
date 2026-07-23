<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Gestione Prodotti</h1>

    <%-- 1. BLOCCO FEEDBACK UTENTE --%>
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success" style="color: #065f46; background-color: #d1fae5; border: 1px solid #a7f3d0; padding: 12px; border-radius: 4px; margin-bottom: 20px;">
            ✓ <c:out value="${successMessage}" />
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger" style="color: #991b1b; background-color: #fee2e2; border: 1px solid #fecaca; padding: 12px; border-radius: 4px; margin-bottom: 20px;">
            ✗ <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- 2. TABELLA PRODOTTI ESAURITI (Mostrata SOLO se ci sono prodotti con quantita <= 0) --%>
    <c:set var="hasEsauriti" value="false" />
    <c:forEach var="p" items="${prodottiAdmin}">
        <c:if test="${p.quantita <= 0}">
            <c:set var="hasEsauriti" value="true" />
        </c:if>
    </c:forEach>

    <c:if test="${hasEsauriti}">
        <div style="margin-bottom: 35px; background-color: #fef2f2; border: 1px solid #fecaca; padding: 16px; border-radius: 8px;">
            <h2 style="color: #991b1b; margin-top: 0; font-size: 1.3em; display: flex; align-items: center; gap: 8px;">
                ⚠️ Prodotti Esauriti
            </h2>
            <table style="width: 100%; border-collapse: collapse; margin-top: 10px; background-color: #ffffff;">
                <thead>
                    <tr style="background-color: #fee2e2; border-bottom: 2px solid #fca5a5; color: #991b1b;">
                        <th style="padding: 10px; text-align: left;">ID</th>
                        <th style="padding: 10px; text-align: left;">Nome</th>
                        <th style="padding: 10px; text-align: right;">Prezzo</th>
                        <th style="padding: 10px; text-align: center;">Quantità</th>
                        <th style="padding: 10px; text-align: center;">Stato</th>
                        <th style="padding: 10px; text-align: center;">Azioni</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="prodotto" items="${prodottiAdmin}">
                        <c:if test="${prodotto.quantita <= 0}">
                            <tr style="border-bottom: 1px solid #fee2e2;">
                                <td style="padding: 10px;"><c:out value="${prodotto.idProdotto}" /></td>
                                <td style="padding: 10px; font-weight: 600;"><c:out value="${prodotto.nome}" /></td>
                                <td style="padding: 10px; text-align: right;">
                                    <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" />
                                </td>
                                <td style="padding: 10px; text-align: center; color: #dc2626; font-weight: bold;">0</td>
                                <td style="padding: 10px; text-align: center;">
                                    <span style="padding: 4px 8px; border-radius: 12px; font-size: 0.85em; font-weight: 600; background-color: #fee2e2; color: #991b1b;">
                                        Esaurito
                                    </span>
                                </td>
                                <td style="padding: 10px; text-align: center;">
                                    <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=edit&id=${prodotto.idProdotto}" 
                                       class="btn-edit" style="padding: 5px 10px; text-decoration: none; border-radius: 3px; font-size: 0.9em;">
                                        ✏️ Rifornisci / Modifica
                                    </a>
                                </td>
                            </tr>
                        </c:if>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

    <%-- 3. PULSANTE AGGIUNGI NUOVO PRODOTTO --%>
    <div style="margin-bottom: 25px;">
        <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=new" class="btn-add" style="padding: 10px 20px; text-decoration: none; display: inline-block; border-radius: 4px;">
            ➕ Aggiungi Nuovo Prodotto
        </a>
    </div>

    <%-- 4. TABELLA TUTTI I PRODOTTI (CATALOGO COMPLETO) --%>
    <c:choose>
        <c:when test="${not empty prodottiAdmin}">
            <h2>Tutti i Prodotti</h2>
            <table style="width: 100%; border-collapse: collapse; margin-top: 15px;">
                <thead>
                    <tr style="background-color: #f3f4f6; border-bottom: 2px solid #d1d5db;">
                        <th style="padding: 12px; text-align: left; font-weight: bold;">ID</th>
                        <th style="padding: 12px; text-align: left; font-weight: bold;">Nome</th>
                        <th style="padding: 12px; text-align: left; font-weight: bold;">Descrizione</th>
                        <th style="padding: 12px; text-align: right; font-weight: bold;">Prezzo</th>
                        <th style="padding: 12px; text-align: center; font-weight: bold;">Quantità</th>
                        <th style="padding: 12px; text-align: center; font-weight: bold;">Tipo</th>
                        <th style="padding: 12px; text-align: center; font-weight: bold;">Stato</th>
                        <th style="padding: 12px; text-align: center; font-weight: bold;">Azioni</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="prodotto" items="${prodottiAdmin}" varStatus="status">
                        <tr style="border-bottom: 1px solid #e5e7eb; ${status.index % 2 == 0 ? 'background-color: #fafafa;' : ''}">
                            <td style="padding: 12px;">
                                <c:out value="${prodotto.idProdotto}" />
                            </td>
                            <td style="padding: 12px; font-weight: 500;">
                                <c:out value="${prodotto.nome}" />
                            </td>
                            <td style="padding: 12px; color: #6b7280; max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                                <c:out value="${prodotto.descrizione}" />
                            </td>
                            <td style="padding: 12px; text-align: right; font-weight: 600;">
                                <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€" />
                            </td>
                            <td style="padding: 12px; text-align: center;">
                                <c:out value="${prodotto.quantita}" />
                            </td>
                            <td style="padding: 12px; text-align: center;">
                                <c:out value="${empty prodotto.tipo ? '—' : prodotto.tipo}" />
                            </td>
                            <td style="padding: 12px; text-align: center;">
                                <span style="padding: 4px 8px; border-radius: 12px; font-size: 0.85em; font-weight: 600; ${prodotto.attivo ? 'background-color: #d1fae5; color: #065f46;' : 'background-color: #fee2e2; color: #991b1b;'}">
                                    ${prodotto.attivo ? 'Attivo' : 'Inattivo'}
                                </span>
                            </td>
                            <td style="padding: 12px; text-align: center; white-space: nowrap;">
                                <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=edit&id=${prodotto.idProdotto}" 
                                   class="btn-edit" style="padding: 6px 12px; text-decoration: none; display: inline-block; margin-right: 5px; border-radius: 3px;">
                                    ✏️ Modifica
                                </a>

                                <form method="post" action="${pageContext.request.contextPath}/AdminProdottoServlet" 
                                      style="display: inline;" 
                                      onsubmit="return confirm('Sei sicuro di voler eliminare il prodotto &quot;' + '${prodotto.nome}'.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/&quot;/g, '&quot;') + '&quot;?');">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="${prodotto.idProdotto}">
                                    <button type="submit" class="btn-delete" style="padding: 6px 12px; border-radius: 3px;">
                                        🗑️ Elimina
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>

        <c:otherwise>
            <div style="text-align: center; padding: 50px 20px; background-color: #f9fafb; border-radius: 6px; border: 1px solid #e5e7eb;">
                <p style="font-size: 1.1em; color: #64748b; margin-bottom: 20px;">
                    Il catalogo è attualmente vuoto. Inizia ad aggiungere prodotti!
                </p>
                <a href="${pageContext.request.contextPath}/AdminProdottoServlet?action=new" class="btn-add" style="padding: 10px 20px; text-decoration: none; display: inline-block; border-radius: 4px;">
                    ➕ Aggiungi il Primo Prodotto
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="/jsp/common/footer.jspf" %>