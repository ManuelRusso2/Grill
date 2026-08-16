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

    <%-- ── MESSAGGI DI ERRORE / FEEDBACK ──────────────────────────────────── --%>
    <%-- Mostra un messaggio di alert nel caso in cui si verifichi un errore durante l'elaborazione dell'ordine --%>
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
            
            <%-- Tabella contenente l'elenco dei prodotti presenti nel carrello pronti per l'acquisto --%>
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
                    <%-- Iterazione sugli elementi della mappa dei prodotti nel carrello --%>
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
                                <strong style="color: var(--primary-purple);">
                                    <c:out value="${empty prodotto.tagliaSelezionata ? 'Unica' : prodotto.tagliaSelezionata}"/>
                                </strong>
                            </td>
                            <%-- Costo unitario del prodotto formattato in Euro (€) --%>
                            <td>
                                <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€"/>
                            </td>
                            <%-- Quantità ordinata del singolo prodotto --%>
                            <td><c:out value="${quantita}"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <%-- Riquadro con l'importo totale dell'ordine --%>
            <div class="checkout-total">
                <p>Totale Ordine: <span><fmt:formatNumber value="${not empty totaleCarrello ? totaleCarrello : 0.0}" type="currency" currencySymbol="€"/></span></p>
            </div>
        </div>

        <%-- ── SEZIONE 2: FORM DI SPEDIZIONE E PAGAMENTO ─────────────────────── --%>
        <div class="checkout-form-card">
            <h2>Dati Spedizione e Pagamento</h2>

            <%-- Form per l'invio delle informazioni alla CheckoutServlet via POST --%>
            <form method="post" action="${pageContext.request.contextPath}/CheckoutServlet" id="checkout-form">
                
                <%-- Inserimento dell'indirizzo di spedizione --%>
                <div class="form-group">
                    <label for="indirizzoConsegna">Indirizzo di consegna:</label>
                    <input type="text" id="indirizzoConsegna" name="indirizzoConsegna" required placeholder="Via Roma 123, Milano">
                </div>

                <%-- Selettore interattivo del metodo di pagamento --%>
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
                    
                    <%-- Campo nascosto trasmesso al Servlet con la modalità scelta --%>
                    <input type="hidden" name="metodoPagamento" id="metodoPagamentoInput" value="Carta">
                </div>

                <%-- Campi specifici per pagamento con Carta di Credito/Debito --%>
                <div id="cardDetails" class="payment-method-section">
                    <h3>Dettagli Carta</h3>

                    <%-- Numero di Carta --%>
                    <div class="form-group">
                        <label for="cartaNumero">Numero carta:</label>
                        <input type="text" id="cartaNumero" name="cartaNumero" inputmode="numeric" maxlength="19" placeholder="4242 4242 4242 4242">
                        <span id="cartaNumeroError" class="field-error-span"></span>
                    </div>

                    <%-- Nome e Cognome dell'intestatario della carta --%>
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

                    <%-- Scadenza e Codice di Sicurezza CVV --%>
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

                    <%-- Nome e Cognome dell'intestatario del conto --%>
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

                    <%-- Codice IBAN del conto --%>
                    <div class="form-group">
                        <label for="contoIBAN">IBAN:</label>
                        <input type="text" id="contoIBAN" name="contoIBAN" maxlength="34" placeholder="IT60 X054 2811 1010 0000 0123 456">
                        <span id="contoIBANError" class="field-error-span"></span>
                    </div>
                </div>

                <%-- Pulsante finale di sottomissione dell'ordine --%>
                <button type="submit" class="btn btn-checkout-submit">Conferma Ordine</button>
            </form>
        </div>
    </div>

    <%-- ── SCRIPT JS: GESTIONE DINAMICA E VALIDAZIONE CLIENT-SIDE ─────────────────── --%>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // Selezione degli elementi DOM rilevanti
            const options = document.querySelectorAll('.payment-option');
            const hiddenInput = document.getElementById('metodoPagamentoInput');
            const cardDetails = document.getElementById('cardDetails');
            const bankDetails = document.getElementById('bankDetails');
            const form = document.getElementById('checkout-form');

            /**
             * Gestisce la commutazione visiva e funzionale tra i metodi di pagamento.
             * Disabilita gli input del pannello non visibile per evitare di inviare dati non pertinenti.
             */
            function togglePaymentMethod(method) {
                hiddenInput.value = method;
                const isCard = method === 'Carta';

                // Mostra/Nasconde i relativi sezioni
                cardDetails.classList.toggle('is-hidden', !isCard);
                bankDetails.classList.toggle('is-hidden', isCard);

                // Disabilita i campi non selezionati per ignorarli durante il submit del form
                cardDetails.querySelectorAll('input').forEach(i => i.disabled = !isCard);
                bankDetails.querySelectorAll('input').forEach(i => i.disabled = isCard);
            }

            // Registrazione dell'evento di click per la selezione del metodo di pagamento
            options.forEach(option => {
                option.addEventListener('click', function() {
                    options.forEach(opt => opt.classList.remove('active'));
                    this.classList.add('active');
                    togglePaymentMethod(this.dataset.method);
                });
            });

            // Imposta lo stato iniziale sulla carta di credito
            togglePaymentMethod('Carta');

            /**
             * Algoritmo di Luhn per la validazione formale del numero della carta di credito.
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
             * Validazione mediante Regex della sintassi standard di un codice IBAN.
             */
            function validateIBAN(iban) {
                return /^[A-Z]{2}[0-9A-Z]{13,32}$/.test(iban.replace(/\s+/g, '').toUpperCase());
            }

            /**
             * Utility per impostare e visualizzare o nascondere i messaggi di errore sui singoli campi.
             */
            function setError(id, text) {
                const el = document.getElementById(id);
                if (el) {
                    el.textContent = text;
                    el.style.display = text ? 'block' : 'none';
                }
            }

            // Validazione client-side prima del submit del modulo
            form.addEventListener('submit', function(e) {
                // Reset di tutti gli errori precedenti
                form.querySelectorAll('.field-error-span').forEach(s => { s.textContent = ''; s.style.display = 'none'; });

                const metodo = hiddenInput.value;
                let ok = true;

                // Controlli specifici per Carta di Credito
                if (metodo === 'Carta') {
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
                // Controlli specifici per Conto Bancario
                else {
                    const nome = document.getElementById('contoNome').value.trim();
                    const cognome = document.getElementById('contoCognome').value.trim();
                    const iban = document.getElementById('contoIBAN').value.trim();

                    if (!nome) { setError('contoNomeError', 'Nome richiesto'); ok = false; }
                    if (!cognome) { setError('contoCognomeError', 'Cognome richiesto'); ok = false; }
                    if (!validateIBAN(iban)) { setError('contoIBANError', 'IBAN non valido'); ok = false; }
                }

                // In caso di errori, blocca l'invio e riposiziona la schermata al form
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