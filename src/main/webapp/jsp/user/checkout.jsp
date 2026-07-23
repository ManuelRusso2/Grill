<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Importiamo JSTL --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Nota: Se usi Tomcat 10 cambia gli URI in "jakarta.tags.core" e "jakarta.tags.fmt" --%>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main class="container">
    <h1>Checkout</h1>

    <table>
        <thead>
            <tr><th>Prodotto</th><th>Prezzo</th><th>Quantità</th></tr>
        </thead>
        <tbody>
            <%-- Iteriamo sulla mappa passata dalla servlet --%>
            <c:forEach var="entry" items="${prodottiCarrello}">
                <c:set var="prodotto" value="${entry.key}" />
                <c:set var="quantita" value="${entry.value}" />
                <tr>
                    <td>
                        <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${prodotto.idProdotto}">
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

    <%-- Mostriamo il totale formattato in euro. Se è vuoto o null, mostra €0,00 --%>
    <p>
        Totale: 
        <fmt:formatNumber value="${not empty totaleCarrello ? totaleCarrello : 0.0}" type="currency" currencySymbol="€"/>
    </p>

    <form method="post" action="${pageContext.request.contextPath}/CheckoutServlet">
        <label>Metodo pagamento:
            <select name="metodoPagamento">
                <option value="Carta">Carta</option>
                <option value="Conto_bancario">Conto bancario</option>
            </select>
        </label>
        <br>
        <label>Indirizzo di consegna:
            <input type="text" name="indirizzoConsegna" required>
        </label>
        <br>

        <!-- Campi per pagamento con Carta -->
        <div id="cardDetails" style="margin-top:12px; display:block;">
            <h3>Dettagli Carta</h3>
            <label>Numero carta:
                <input type="text" id="cartaNumero" name="cartaNumero" inputmode="numeric" maxlength="19" placeholder="4242 4242 4242 4242">
            </label>
            <span id="cartaNumeroError" class="error-message" style="display:none;color:red;margin-left:8px;"></span>
            <br>
            <label>Nome intestatario:
                <input type="text" id="cartaNome" name="cartaNome" placeholder="Mario">
            </label>
            <span id="cartaNomeError" class="error-message" style="display:none;color:red;margin-left:8px;"></span>
            <br>
            <label>Cognome intestatario:
                <input type="text" id="cartaCognome" name="cartaCognome" placeholder="Rossi">
            </label>
            <span id="cartaCognomeError" class="error-message" style="display:none;color:red;margin-left:8px;"></span>
            <br>
            <label>Data scadenza (MM/AA):
                <input type="text" id="cartaScadenza" name="cartaScadenza" maxlength="5" placeholder="MM/AA">
            </label>
            <span id="cartaScadenzaError" class="error-message" style="display:none;color:red;margin-left:8px;"></span>
            <br>
            <label>CVV:
                <input type="text" id="cartaCVV" name="cartaCVV" inputmode="numeric" maxlength="4" placeholder="123">
            </label>
            <span id="cartaCVVError" class="error-message" style="display:none;color:red;margin-left:8px;"></span>
        </div>

        <!-- Campi per pagamento con Conto Bancario -->
        <div id="bankDetails" style="margin-top:12px; display:none;">
            <h3>Dettagli Conto Bancario</h3>
            <label>Nome intestatario:
                <input type="text" id="contoNome" name="contoNome" placeholder="Mario">
            </label>
            <span id="contoNomeError" class="error-message" style="display:none;color:red;margin-left:8px;"></span>
            <br>
            <label>Cognome intestatario:
                <input type="text" id="contoCognome" name="contoCognome" placeholder="Rossi">
            </label>
            <span id="contoCognomeError" class="error-message" style="display:none;color:red;margin-left:8px;"></span>
            <br>
            <label>IBAN:
                <input type="text" id="contoIBAN" name="contoIBAN" maxlength="34" placeholder="IT60 X054 2811 1010 0000 0123 456">
            </label>
            <span id="contoIBANError" class="error-message" style="display:none;color:red;margin-left:8px;"></span>
        </div>
        <br>
        <button type="submit">Conferma ordine</button>
    </form>
    <script>
        (function(){
            const metodoSelect = document.querySelector('select[name="metodoPagamento"]');
            const cardDetails = document.getElementById('cardDetails');
            const bankDetails = document.getElementById('bankDetails');
            const form = document.querySelector('form[action$="/CheckoutServlet"]');

            function showHide() {
                const val = metodoSelect.value;
                if (val === 'Carta') {
                    cardDetails.style.display = 'block';
                    bankDetails.style.display = 'none';
                } else {
                    cardDetails.style.display = 'none';
                    bankDetails.style.display = 'block';
                }
            }

            metodoSelect.addEventListener('change', showHide);
            // inizializza
            showHide();

            // semplice validazione lato client
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

            form.addEventListener('submit', function(e){
                // reset error spans
                const errors = form.querySelectorAll('.error-message');
                errors.forEach(s=>{ s.textContent=''; s.style.display='none'; });

                const metodo = metodoSelect.value;
                let ok = true;

                if (metodo === 'Carta') {
                    const num = document.getElementById('cartaNumero').value.trim();
                    const nome = document.getElementById('cartaNome').value.trim();
                    const cognome = document.getElementById('cartaCognome').value.trim();
                    const scad = document.getElementById('cartaScadenza').value.trim();
                    const cvv = document.getElementById('cartaCVV').value.trim();

                    if (!num || !/^[0-9\s]{13,19}$/.test(num) || !luhnCheck(num)) {
                        const s = document.getElementById('cartaNumeroError'); s.textContent='Numero carta non valido'; s.style.display='inline'; ok=false;
                    }
                    if (!nome) { const s = document.getElementById('cartaNomeError'); s.textContent='Nome intestatario richiesto'; s.style.display='inline'; ok=false; }
                    if (!cognome) { const s = document.getElementById('cartaCognomeError'); s.textContent='Cognome intestatario richiesto'; s.style.display='inline'; ok=false; }
                    // scadenza MM/AA
                    if (!/^(0[1-9]|1[0-2])\/(\d{2})$/.test(scad)) { const s = document.getElementById('cartaScadenzaError'); s.textContent='Formato scadenza MM/AA'; s.style.display='inline'; ok=false; }
                    // cvv 3 o 4 cifre
                    if (!/^[0-9]{3,4}$/.test(cvv)) { const s = document.getElementById('cartaCVVError'); s.textContent='CVV non valido'; s.style.display='inline'; ok=false; }
                } else {
                    const nome = document.getElementById('contoNome').value.trim();
                    const cognome = document.getElementById('contoCognome').value.trim();
                    const iban = document.getElementById('contoIBAN').value.trim();
                    if (!nome) { const s = document.getElementById('contoNomeError'); s.textContent='Nome intestatario richiesto'; s.style.display='inline'; ok=false; }
                    if (!cognome) { const s = document.getElementById('contoCognomeError'); s.textContent='Cognome intestatario richiesto'; s.style.display='inline'; ok=false; }
                    if (!validateIBAN(iban)) { const s = document.getElementById('contoIBANError'); s.textContent='IBAN non valido'; s.style.display='inline'; ok=false; }
                }

                if (!ok) {
                    e.preventDefault();
                    window.scrollTo({ top: form.offsetTop - 40, behavior: 'smooth' });
                }
            });
        })();
    </script>
</main>

<%@ include file="/jsp/common/footer.jspf" %>