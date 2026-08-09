<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Checkout</h1>

    <div class="checkout-grid">
        <%-- RIEPILOGO ORDINE --%>
        <div class="checkout-summary-card">
            <h2>Riepilogo Ordine</h2>
            <table class="cart-table checkout-table">
                <thead>
                    <tr>
                        <th>Prodotto</th>
                        <th>Prezzo</th>
                        <th>Quantità</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="entry" items="${prodottiCarrello}">
                        <c:set var="prodotto" value="${entry.key}" />
                        <c:set var="quantita" value="${entry.value}" />
                        <tr>
                            <td>
                                <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${prodotto.idProdotto}" class="cart-product-title">
                                    <c:out value="${prodotto.nome}"/>
                                </a>
                            </td>
                            <td>
                                <fmt:formatNumber value="${prodotto.costo}" type="currency" currencySymbol="€"/>
                            </td>
                            <td><c:out value="${quantita}"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <div class="checkout-total">
                <p>Totale Ordine: <span><fmt:formatNumber value="${not empty totaleCarrello ? totaleCarrello : 0.0}" type="currency" currencySymbol="€"/></span></p>
            </div>
        </div>

        <%-- FORM DI PAGAMENTO E SPEDIZIONE --%>
        <div class="checkout-form-card">
            <h2>Dati Spedizione e Pagamento</h2>

            <form method="post" action="${pageContext.request.contextPath}/CheckoutServlet" id="checkout-form">
                
                <div class="form-group">
                    <label for="indirizzoConsegna">Indirizzo di consegna:</label>
                    <input type="text" id="indirizzoConsegna" name="indirizzoConsegna" required placeholder="Via Roma 123, Milano">
                </div>

                <div class="form-group">
                    <label for="metodoPagamento">Metodo di pagamento:</label>
                    <select name="metodoPagamento" id="metodoPagamento">
                        <option value="Carta">Carta di Credito / Debito</option>
                        <option value="Conto_bancario">Conto Bancario (IBAN)</option>
                    </select>
                </div>

                <!-- Campi per pagamento con Carta -->
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

                <!-- Campi per pagamento con Conto Bancario -->
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

                <button type="submit" class="btn btn-checkout-submit">Conferma Ordine</button>
            </form>
        </div>
    </div>

    <script>
        (function(){
            const metodoSelect = document.getElementById('metodoPagamento');
            const cardDetails = document.getElementById('cardDetails');
            const bankDetails = document.getElementById('bankDetails');
            const form = document.getElementById('checkout-form');

            function showHide() {
                const val = metodoSelect.value;
                if (val === 'Carta') {
                    cardDetails.classList.remove('is-hidden');
                    bankDetails.classList.add('is-hidden');
                } else {
                    cardDetails.classList.add('is-hidden');
                    bankDetails.classList.remove('is-hidden');
                }
            }

            metodoSelect.addEventListener('change', showHide);
            // Inizializza stato
            showHide();

            // Funzioni validazione...
            function luhnCheck(cardNumber) {
                const s = cardNumber.replace(/\D/g,'');
                let sum = 0, odd = false;
                for (let i = s.length - 1; i >= 0; i--) {
                    let d = parseInt(s.charAt(i), 10);
                    if (odd) d *= 2;
                    if (d > 9) d -= 9;
                    sum += d;
                    odd = !odd;
                }
                return (sum % 10) === 0;
            }

            function validateIBAN(iban){
                if(!iban) return false;
                const value = iban.replace(/\s+/g,'').toUpperCase();
                const re = /^[A-Z]{2}[0-9A-Z]{13,32}$/;
                return re.test(value);
            }

            function setError(elementId, text) {
                const el = document.getElementById(elementId);
                if (el) {
                    el.textContent = text;
                    el.style.display = text ? 'block' : 'none'; // Assicura visibilità
                }
            }

            form.addEventListener('submit', function(e){
                // Reset errori
                const errors = form.querySelectorAll('.field-error-span');
                errors.forEach(s => { s.textContent = ''; s.style.display = 'none'; });

                const metodo = metodoSelect.value;
                let ok = true;

                if (metodo === 'Carta') {
                    const num = document.getElementById('cartaNumero').value.trim();
                    const nome = document.getElementById('cartaNome').value.trim();
                    const cognome = document.getElementById('cartaCognome').value.trim();
                    const scad = document.getElementById('cartaScadenza').value.trim();
                    const cvv = document.getElementById('cartaCVV').value.trim();

                    if (!num || !/^[0-9\s]{13,19}$/.test(num) || !luhnCheck(num)) {
                        setError('cartaNumeroError', 'Numero carta non valido'); ok = false;
                    }
                    if (!nome) { setError('cartaNomeError', 'Nome richiesto'); ok = false; }
                    if (!cognome) { setError('cartaCognomeError', 'Cognome richiesto'); ok = false; }
                    if (!/^(0[1-9]|1[0-2])\/(\d{2})$/.test(scad)) { setError('cartaScadenzaError', 'Formato MM/AA non valido'); ok = false; }
                    if (!/^[0-9]{3,4}$/.test(cvv)) { setError('cartaCVVError', 'CVV non valido'); ok = false; }
                } else {
                    const nome = document.getElementById('contoNome').value.trim();
                    const cognome = document.getElementById('contoCognome').value.trim();
                    const iban = document.getElementById('contoIBAN').value.trim();

                    if (!nome) { setError('contoNomeError', 'Nome richiesto'); ok = false; }
                    if (!cognome) { setError('contoCognomeError', 'Cognome richiesto'); ok = false; }
                    if (!validateIBAN(iban)) { setError('contoIBANError', 'IBAN non valido'); ok = false; }
                }

                if (!ok) {
                    e.preventDefault();
                    window.scrollTo({ top: form.offsetTop - 100, behavior: 'smooth' });
                }
            });
        })();
    </script>
</main>

<%@ include file="/jsp/common/footer.jspf" %>