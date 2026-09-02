<%-- 
    Pagina di Checkout per la finalizzazione dell'ordine.
    Gestisce il riepilogo articoli, l'inserimento dell'indirizzo di spedizione,
    la selezione dinamica del metodo di pagamento (Carta di Credito / IBAN)
    e la validazione dei dati client-side prima dell'invio al server.
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

<%-- Contenitore principale di layout della pagina di checkout --%>
<main class="container">
    <h1>Checkout</h1>

    <%-- ── MESSAGGI DI ERRORE / FEEDBACK LATO SERVER ────────────────────────── --%>
    <%-- Visualizza l'avviso d'errore inviato dalla Servlet (es. dati di pagamento non validi o stock esaurito) --%>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">
            ✗ <c:out value="${errorMessage}" />
        </div>
    </c:if>

    <%-- Layout a griglia responsive: 2 colonne per separare il riepilogo prodotti dal modulo di pagamento --%>
    <div class="checkout-grid">
        
        <%-- ── SEZIONE 1: SCHEDA RIEPILOGO DELL'ORDINE ─────────────────────────────── --%>
        <div class="checkout-summary-card">
            <h2>Riepilogo Ordine</h2>
            
            <%-- Contenitore wrapper per garantire la responsività della tabella su schermi stretti --%>
            <div class="cart-table-wrapper">
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
                        <%-- Iterazione sulla mappa dei prodotti recuperata dalla Servlet (<ProdottoBean, Integer (Quantità)>) --%>
                        <c:forEach var="entry" items="${prodottiCarrello}">
                            <c:set var="prodotto" value="${entry.key}" />
                            <c:set var="quantita" value="${entry.value}" />
                            <tr>
                                <%-- Titolo del prodotto con link diretto alla scheda di dettaglio --%>
                                <td>
                                    <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${prodotto.idProdotto}" class="cart-product-title">
                                        <c:out value="${prodotto.nome}"/>
                                    </a>
                                </td>
                                
                                <%-- Badge stilizzato per la visualizzazione della taglia selezionata senza ternario --%>
                                <td>
                                    <span class="order-size-badge">
                                        <c:choose>
                                            <c:when test="${not empty prodotto.tagliaSelezionata}">
                                                <c:out value="${prodotto.tagliaSelezionata}"/>
                                            </c:when>
                                            <c:otherwise>
                                                Unica
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                </td>

                                <%-- Prezzo unitario formattato in Euro (€) con localizzazione --%>
                                <td>
                                    <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€"/>
                                </td>

                                <%-- Quantità ordinata dall'utente per la singola riga --%>
                                <td><c:out value="${quantita}"/></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <%-- Riquadro di evidenziazione dell'importo totale complessivo dell'ordine --%>
            <div class="checkout-total">
                <p>Totale Ordine: <span>
                    <c:choose>
                        <c:when test="${not empty totaleCarrello}">
                            <fmt:formatNumber value="${totaleCarrello}" type="currency" currencySymbol="€"/>
                        </c:when>
                        <c:otherwise>
                            <fmt:formatNumber value="0.0" type="currency" currencySymbol="€"/>
                        </c:otherwise>
                    </c:choose>
                </span></p>
            </div>
        </div>

        <%-- ── SEZIONE 2: SCHEDA FORM DI SPEDIZIONE E PAGAMENTO ─────────────────────── --%>
        <div class="checkout-form-card">
            <h2>Dati Spedizione e Pagamento</h2>

            <%-- Modulo d'invio ordine: l'action viene aggiornata dinamicamente via JS al variare del metodo di pagamento --%>
            <form method="post" action="${pageContext.request.contextPath}/CheckoutServlet" id="checkout-form">
                
                <%-- Campo per l'inserimento dell'indirizzo di spedizione --%>
                <div class="form-group">
                    <label for="indirizzoConsegna">Indirizzo di consegna:</label>
                    <input type="text" id="indirizzoConsegna" name="indirizzoConsegna" class="form-control" required placeholder="Via Roma 123, Milano">
                </div>

                <%-- Selettore a schede/pulsanti visivi per la scelta del metodo di pagamento --%>
                <div class="form-group">
                    <label>Scegli il Metodo di pagamento:</label>
                    
                    <div class="payment-method-selector">
                        <%-- Opzione Carta di Credito / Debito (selezionata di default) --%>
                        <div class="payment-option active" data-method="Carta">
                            <span class="icon">💳</span>
                            <span>Carta di Credito / Debito</span>
                        </div>
                        <%-- Opzione Conto Bancario tramite codice IBAN --%>
                        <div class="payment-option" data-method="Conto_bancario">
                            <span class="icon">🏦</span>
                            <span>Conto Bancario (IBAN)</span>
                        </div>
                    </div>
                </div>

                <%-- Blocco campi specifici per Carta di Credito (visibile di default) --%>
                <div id="cardDetails" class="payment-method-section">
                    <h3>Dettagli Carta</h3>

                    <%-- Campo Numero Carta di Credito --%>
                    <div class="form-group">
                        <label for="cartaNumero">Numero carta:</label>
                        <input type="text" id="cartaNumero" name="cartaNumero" class="form-control" inputmode="numeric" maxlength="19" placeholder="4242 4242 4242 4242">
                        <span id="cartaNumeroError" class="field-error-span"></span>
                    </div>

                    <%-- Riga affiancata per Nome e Cognome Intestatario Carta --%>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="cartaNome">Nome intestatario:</label>
                            <input type="text" id="cartaNome" name="cartaNome" class="form-control" placeholder="Mario">
                            <span id="cartaNomeError" class="field-error-span"></span>
                        </div>

                        <div class="form-group">
                            <label for="cartaCognome">Cognome intestatario:</label>
                            <input type="text" id="cartaCognome" name="cartaCognome" class="form-control" placeholder="Rossi">
                            <span id="cartaCognomeError" class="field-error-span"></span>
                        </div>
                    </div>

                    <%-- Riga affiancata per Scadenza (MM/AA) e Codice di Sicurezza CVV --%>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="cartaScadenza">Scadenza (MM/AA):</label>
                            <input type="text" id="cartaScadenza" name="cartaScadenza" class="form-control" maxlength="5" placeholder="MM/AA">
                            <span id="cartaScadenzaError" class="field-error-span"></span>
                        </div>

                        <div class="form-group">
                            <label for="cartaCVV">CVV:</label>
                            <input type="text" id="cartaCVV" name="cartaCVV" class="form-control" inputmode="numeric" maxlength="4" placeholder="123">
                            <span id="cartaCVVError" class="field-error-span"></span>
                        </div>
                    </div>
                </div>

                <%-- Blocco campi specifici per Conto Bancario (nascosto di default via CSS) --%>
                <div id="bankDetails" class="payment-method-section is-hidden">
                    <h3>Dettagli Conto Bancario</h3>

                    <%-- Riga affiancata per Nome e Cognome Intestatario Conto --%>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="contoNome">Nome intestatario:</label>
                            <input type="text" id="contoNome" name="contoNome" class="form-control" placeholder="Mario">
                            <span id="contoNomeError" class="field-error-span"></span>
                        </div>

                        <div class="form-group">
                            <label for="contoCognome">Cognome intestatario:</label>
                            <input type="text" id="contoCognome" name="contoCognome" class="form-control" placeholder="Rossi">
                            <span id="contoCognomeError" class="field-error-span"></span>
                        </div>
                    </div>

                    <%-- Campo Codice IBAN --%>
                    <div class="form-group">
                        <label for="contoIBAN">IBAN:</label>
                        <input type="text" id="contoIBAN" name="contoIBAN" class="form-control" maxlength="34" placeholder="IT60 X054 2811 1010 0000 0123 456">
                        <span id="contoIBANError" class="field-error-span"></span>
                    </div>
                </div>

                <%-- Pulsante principale per la sottomissione definitiva dell'ordine --%>
                <button type="submit" class="btn btn-md btn-primary btn-full btn-checkout-submit">Conferma Ordine</button>
            </form>
        </div>
    </div>

    <%-- ── SCRIPT JS: COMMUTAZIONE DINAMICA E VALIDAZIONE CLIENT-SIDE ──────────────── --%>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // Selezione dei principali elementi del DOM
            const options = document.querySelectorAll('.payment-option');
            const cardDetails = document.getElementById('cardDetails');
            const bankDetails = document.getElementById('bankDetails');
            const form = document.getElementById('checkout-form');
            const baseUrl = '${pageContext.request.contextPath}/CheckoutServlet';
            
            // Variabile di stato per tracciare il metodo di pagamento correntemente selezionato
            let selectedMethod = 'Carta';

            /**
             * Cambia il metodo di pagamento attivo:
             * 1. Aggiorna l'URL di destinazione della form aggiungendo il parametro 'metodoPagamento'.
             * 2. Mostra e nasconde le rispettive sezioni di input nel DOM.
             * 3. Abilita/Disabilita gli input per evitare l'invio di dati inutili al server.
             * 
             * @param {string} method - Il nome del metodo selezionato ('Carta' o 'Conto_bancario')
             */
            function togglePaymentMethod(method) {
                selectedMethod = method;
                form.action = baseUrl + '?metodoPagamento=' + method;
                const isCard = method === 'Carta';

                if (isCard) {
                    // Mostra la sezione Carta e nasconde la sezione IBAN
                    cardDetails.classList.remove('is-hidden');
                    bankDetails.classList.add('is-hidden');

                    // Abilita i campi della Carta e disabilita quelli dell'IBAN
                    cardDetails.querySelectorAll('input').forEach(function(input) { input.disabled = false; });
                    bankDetails.querySelectorAll('input').forEach(function(input) { input.disabled = true; });
                } else {
                    // Nasconde la sezione Carta e mostra la sezione IBAN
                    cardDetails.classList.add('is-hidden');
                    bankDetails.classList.remove('is-hidden');

                    // Disabilita i campi della Carta e abilita quelli dell'IBAN
                    cardDetails.querySelectorAll('input').forEach(function(input) { input.disabled = true; });
                    bankDetails.querySelectorAll('input').forEach(function(input) { input.disabled = false; });
                }
            }

            // Assegna l'evento click alla scheda di selezione del metodo di pagamento
            document.querySelector('.payment-method-selector').addEventListener('click', function(e) {
                const option = e.target.closest('.payment-option');
                if (!option) return;
            
                // Rimuove lo stato attivo dalla precedente opzione e lo assegna a quella cliccata
                const activeOpt = document.querySelector('.payment-option.active');
                if (activeOpt) {
                    activeOpt.classList.remove('active');
                }
                option.classList.add('active');
                togglePaymentMethod(option.dataset.method);
            });

            // Inizializza la pagina impostando di default il metodo "Carta"
            togglePaymentMethod('Carta');

            /**
             * Algoritmo di Luhn per la validazione formale delle cifre del numero di carta di credito.
             * 
             * @param {string} num - Stringa contenente le cifre della carta
             * @returns {boolean} true se il numero è valido, false altrimenti
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
             * Validazione del formato della stringa IBAN tramite Espressione Regolare (Regex).
             * 
             * @param {string} iban - Stringa del codice IBAN da verificare
             * @returns {boolean} true se l'IBAN rispetta il formato standard
             */
            function validateIBAN(iban) {
                return /^[A-Z]{2}[0-9A-Z]{13,32}$/.test(iban.replace(/\s+/g, '').toUpperCase());
            }

            /**
             * Helper per stampare il testo d'errore all'interno dello span di riferimento del campo.
             * 
             * @param {string} id - ID dell'elemento span di errore
             * @param {string} text - Testo del messaggio d'errore da mostrare
             */
            function setError(id, text) {
                const el = document.getElementById(id);
                if (el) {
                    el.textContent = text;
                }
            }

            // Validazione client-side dei campi prima dell'invio del modulo
            form.addEventListener('submit', function(e) {
                // Azzera tutti i messaggi d'errore visibili in precedenza
                form.querySelectorAll('.field-error-span').forEach(s => { s.textContent = ''; });

                let ok = true;

                // Controlli specifici in caso di pagamento con Carta di Credito
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
                // Controlli specifici in caso di pagamento con Conto Bancario / IBAN
                else {
                    const nome = document.getElementById('contoNome').value.trim();
                    const cognome = document.getElementById('contoCognome').value.trim();
                    const iban = document.getElementById('contoIBAN').value.trim();

                    if (!nome) { setError('contoNomeError', 'Nome richiesto'); ok = false; }
                    if (!cognome) { setError('contoCognomeError', 'Cognome richiesto'); ok = false; }
                    if (!validateIBAN(iban)) { setError('contoIBANError', 'IBAN non valido'); ok = false; }
                }

                // Se sono stati riscontrati errori, blocca la sottomissione ed effettua lo scroll al form
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