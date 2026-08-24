<%-- Impostazione del tipo di contenuto della pagina e della codifica dei caratteri (UTF-8) --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Inclusione delle librerie di tag JSTL per la logica di controllo e la formattazione di numeri/valute --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Inclusione dei frammenti di codice statici per l'intestazione (header) e la barra di navigazione (menu) --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale della pagina di checkout --%>
<main class="container">
    <h1>Checkout</h1>

    <%-- ── MESSAGGI DI ERRORE / FEEDBACK LATO SERVER ────────────────────────── --%>
    <%-- Mostra un messaggio d'avviso se la Servlet ha riscontrato un errore durante l'elaborazione --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- Layout a griglia per affiancare il riepilogo dell'ordine e il modulo di pagamento --%>
    <div class="checkout-grid">
        
        <%-- ── SEZIONE 1: RIEPILOGO DELL'ORDINE ─────────────────────────────── --%>
        <div class="checkout-summary-card">
            <h2>Riepilogo Ordine</h2>
            
            <%-- Tabella contenente l'elenco dei prodotti presenti a carrello --%>
            <table class="cart-table checkout-table">
                <thead>
                    <tr>
                        <th>Prodotto</th>
                        <th>Taglia</th>
                        <th>Prezzo</th>
                        <th>Q.tà</th>
                    </tr>
                </thead>
                <tbody>
                    <%-- Iterazione sulla mappa dei prodotti recuperata dalla Servlet --%>
                    <c:forEach var="entry" items="${prodottiCarrello}">
                        <c:set var="prodotto" value="${entry.key}" />
                        <c:set var="quantita" value="${entry.value}" />
                        <tr>
                            <%-- Titolo del prodotto con link alla pagina di dettaglio --%>
                            <td>
                                <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${prodotto.idProdotto}" class="cart-product-title">
                                    <c:out value="${prodotto.nome}"/>
                                </a>
                            </td>
                            <%-- Indicazione della taglia selezionata o valore 'Unica' di default --%>
                            <td>
                                <strong class="cart-product-size">
                                    <c:out value="${empty prodotto.tagliaSelezionata ? 'Unica' : prodotto.tagliaSelezionata}"/>
                                </strong>
                            </td>
                            <%-- Prezzo unitario formattato in Euro (€) --%>
                            <td>
                                <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€"/>
                            </td>
                            <%-- Quantità ordinata --%>
                            <td><c:out value="${quantita}"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <%-- Riquadro con l'importo totale calcolato dell'ordine --%>
            <div class="checkout-total">
                <p>Totale Ordine: <span><fmt:formatNumber value="${not empty totaleCarrello ? totaleCarrello : 0.0}" type="currency" currencySymbol="€"/></span></p>
            </div>
        </div>

        <%-- ── SEZIONE 2: FORM DI SPEDIZIONE E PAGAMENTO ─────────────────────── --%>
        <div class="checkout-form-card">
            <h2>Dati Spedizione e Pagamento</h2>

            <%-- Form di invio dati ordine: l'azione/URL viene aggiornata dinamicamente da JS per includere il metodo di pagamento --%>
            <form method="post" action="${pageContext.request.contextPath}/CheckoutServlet" id="checkout-form">
                
                <%-- Campo per l'indirizzo di consegna --%>
                <div class="form-group">
                    <label for="indirizzoConsegna">Indirizzo di consegna:</label>
                    <input type="text" id="indirizzoConsegna" name="indirizzoConsegna" required placeholder="Via Roma 123, Milano">
                </div>

                <%-- Selettore visivo del metodo di pagamento --%>
                <div class="form-group">
                    <label>Scegli il Metodo di pagamento:</label>
                    
                    <div class="payment-method-selector">
                        <%-- Opzione Carta di Credito/Debito --%>
                        <div class="payment-option active" data-method="Carta">
                            <span class="icon">💳</span>
                            <span>Carta di Credito / Debito</span>
                        </div>
                        <%-- Opzione Conto Bancario --%>
                        <div class="payment-option" data-method="Conto_bancario">
                            <span class="icon">🏦</span>
                            <span>Conto Bancario (IBAN)</span>
                        </div>
                    </div>
                </div>

                <%-- Campi specifici per pagamento con Carta di Credito --%>
                <div id="cardDetails" class="payment-method-section">
                    <h3>Dettagli Carta</h3>

                    <div class="form-group">
                        <label for="cartaNumero">Numero carta:</label>
                        <input type="text" id="cartaNumero" name="cartaNumero" inputmode="numeric" maxlength="19" placeholder="4242 4242 4242 4242">
                        <span id="cartaNumeroError" class="field-error-span"></span>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="cartaNome">Nome intestatario:</label>
                            <input type="text" id="cartaNome" name="cartaNome" placeholder="Mario">
                            <span id="cartaNomeError" class="field-error-span"></span>
                        </div>

                        <div class="form-group">
                            <label for="cartaCognome">Cognome intestatario:</label>
                            <input type="text" id="cartaCognome" name="cartaCognome" placeholder="Rossi">
                            <span id="cartaCognomeError" class="field-error-span"></span>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="cartaScadenza">Scadenza (MM/AA):</label>
                            <input type="text" id="cartaScadenza" name="cartaScadenza" maxlength="5" placeholder="MM/AA">
                            <span id="cartaScadenzaError" class="field-error-span"></span>
                        </div>

                        <div class="form-group">
                            <label for="cartaCVV">CVV:</label>
                            <input type="text" id="cartaCVV" name="cartaCVV" inputmode="numeric" maxlength="4" placeholder="123">
                            <span id="cartaCVVError" class="field-error-span"></span>
                        </div>
                    </div>
                </div>

                <%-- Campi specifici per pagamento con Conto Bancario --%>
                <div id="bankDetails" class="payment-method-section is-hidden">
                    <h3>Dettagli Conto Bancario</h3>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="contoNome">Nome intestatario:</label>
                            <input type="text" id="contoNome" name="contoNome" placeholder="Mario">
                            <span id="contoNomeError" class="field-error-span"></span>
                        </div>

                        <div class="form-group">
                            <label for="contoCognome">Cognome intestatario:</label>
                            <input type="text" id="contoCognome" name="contoCognome" placeholder="Rossi">
                            <span id="contoCognomeError" class="field-error-span"></span>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="contoIBAN">IBAN:</label>
                        <input type="text" id="contoIBAN" name="contoIBAN" maxlength="34" placeholder="IT60 X054 2811 1010 0000 0123 456">
                        <span id="contoIBANError" class="field-error-span"></span>
                    </div>
                </div>

                <%-- Pulsante di sottomissione dell'ordine --%>
                <button type="submit" class="btn btn-checkout-submit">Conferma Ordine</button>
            </form>
        </div>
    </div>

    <%-- ── SCRIPT JS: COMMUTAZIONE DINAMICA E VALIDAZIONE CLIENT-SIDE ──────────────── --%>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // Selezione degli elementi del DOM principali
            const options = document.querySelectorAll('.payment-option');
            const cardDetails = document.getElementById('cardDetails');
            const bankDetails = document.getElementById('bankDetails');
            const form = document.getElementById('checkout-form');
            const baseUrl = '${pageContext.request.contextPath}/CheckoutServlet';
            
            // Variabile di stato per memorizzare il metodo selezionato
            let selectedMethod = 'Carta';

            /**
             * Cambia il metodo di pagamento attivo:
             * 1. Aggiorna l'URL dell'action della form aggiungendo 'metodoPagamento' come parametro GET.
             * 2. Mostra/nasconde le sezioni relative.
             * 3. Disabilita gli input della sezione nascosta per evitare di inviare parametri vuoti/inutili.
             */
            function togglePaymentMethod(method) {
                selectedMethod = method;
                form.action = baseUrl + '?metodoPagamento=' + method;
                const isCard = method === 'Carta';

                if (isCard) {
                    // Mostra Carta e nasconde IBAN
                    cardDetails.classList.remove('is-hidden');
                    bankDetails.classList.add('is-hidden');

                    // Abilita i campi Carta, disabilita i campi IBAN
                    cardDetails.querySelectorAll('input').forEach(function(input) { input.disabled = false; });
                    bankDetails.querySelectorAll('input').forEach(function(input) { input.disabled = true; });
                } else {
                    // Nasconde Carta e mostra IBAN
                    cardDetails.classList.add('is-hidden');
                    bankDetails.classList.remove('is-hidden');

                    // Disabilita i campi Carta, abilita i campi IBAN
                    cardDetails.querySelectorAll('input').forEach(function(input) { input.disabled = true; });
                    bankDetails.querySelectorAll('input').forEach(function(input) { input.disabled = false; });
                }
            }

            // Registra l'evento di click su ciascuna opzione di pagamento visiva
			document.querySelector('.payment-method-selector').addEventListener('click', function(e) {
			    const option = e.target.closest('.payment-option');
			    if (!option) return;
			
			    document.querySelector('.payment-option.active')?.classList.remove('active');
			    option.classList.add('active');
			    togglePaymentMethod(option.dataset.method);
			});

            // Imposta lo stato iniziale del metodo di pagamento su "Carta"
            togglePaymentMethod('Carta');

            /**
             * Algoritmo di Luhn per verificare la validità formale della carta di credito.
             */
            function luhnCheck(num) {
                const s = num.replace(/\D/g, '');
                let sum = 0, odd = false;
                for (let i = s.length - 1; i >= 0; i--) {
                    let d = parseInt(s.charAt(i), 10);
                    if (odd) d *= 2;
                    if (d > 9) d -= 9;
                    sum += d;
                    odd = !odd;
                }
                return s.length >= 13 && (sum % 10) === 0;
            }

            /**
             * Validazione tramite Regex per la struttura standard dell'IBAN.
             */
            function validateIBAN(iban) {
                return /^[A-Z]{2}[0-9A-Z]{13,32}$/.test(iban.replace(/\s+/g, '').toUpperCase());
            }

            /**
             * Helper per impostare il testo d'errore negli span dedicati.
             */
            function setError(id, text) {
                const el = document.getElementById(id);
                if (el) {
                    el.textContent = text;
                }
            }

            // Validazione client-side all'invio del modulo
            form.addEventListener('submit', function(e) {
                // Reset dei messaggi di errore precedenti
                form.querySelectorAll('.field-error-span').forEach(s => { s.textContent = ''; });

                let ok = true;

                // Controlli per metodo "Carta di Credito"
                if (selectedMethod === 'Carta') {
                    const num = document.getElementById('cartaNumero').value.trim();
                    const nome = document.getElementById('cartaNome').value.trim();
                    const cognome = document.getElementById('cartaCognome').value.trim();
                    const scad = document.getElementById('cartaScadenza').value.trim();
                    const cvv = document.getElementById('cartaCVV').value.trim();

                    if (!luhnCheck(num)) { setError('cartaNumeroError', 'Numero carta non valido'); ok = false; }
                    if (!nome) { setError('cartaNomeError', 'Nome richiesto'); ok = false; }
                    if (!cognome) { setError('cartaCognomeError', 'Cognome richiesto'); ok = false; }
                    if (!/^(0[1-9]|1[0-2])\/(\d{2})$/.test(scad)) { setError('cartaScadenzaError', 'Formato MM/AA non valido'); ok = false; }
                    if (!/^[0-9]{3,4}$/.test(cvv)) { setError('cartaCVVError', 'CVV non valido'); ok = false; }
                } 
                // Controlli per metodo "Conto Bancario"
                else {
                    const nome = document.getElementById('contoNome').value.trim();
                    const cognome = document.getElementById('contoCognome').value.trim();
                    const iban = document.getElementById('contoIBAN').value.trim();

                    if (!nome) { setError('contoNomeError', 'Nome richiesto'); ok = false; }
                    if (!cognome) { setError('contoCognomeError', 'Cognome richiesto'); ok = false; }
                    if (!validateIBAN(iban)) { setError('contoIBANError', 'IBAN non valido'); ok = false; }
                }

                // Se ci sono errori, impedisce l'invio e riporta lo scroll al form
                if (!ok) {
                    e.preventDefault();
                    window.scrollTo({ top: form.offsetTop - 100, behavior: 'smooth' });
                }
            });
        });
    </script>
</main>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>