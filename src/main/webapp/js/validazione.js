/**
 * Gestione della validazione della registrazione lato client.
 * Combina controlli asincroni AJAX (in tempo reale) e validazione sincrona finale sul form.
 */
document.addEventListener("DOMContentLoaded", function () {

    // ==========================================
    // 1. CONTROLLO AJAX IN TEMPO REALE (SULL'EMAIL)
    // ==========================================
    const emailInput = document.getElementById("email");
    const registerForm = document.getElementById("registerForm");

    if (emailInput) {
        const emailErrorSpan = document.getElementById("emailError");
        // Regex robusta per la validazione della sintassi email
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

        let emailTimer = null;

        // Funzione per nascondere il messaggio di errore
        function clearEmailError() {
            if (emailErrorSpan) {
                emailErrorSpan.textContent = '';
                emailErrorSpan.style.display = 'none';
            }
            emailInput.style.borderColor = '';
            delete emailInput.dataset.exists;
        }

        // Funzione che verifica sintassi e, se valida, esistenza via AJAX
        function checkEmail(emailValore) {
            if (!emailErrorSpan) return;

            // Se il campo è vuoto, puliamo l'errore e usciamo
            if (!emailValore) {
                clearEmailError();
                return;
            }

            // 1. Validazione Sintattica
            if (!emailRegex.test(emailValore)) {
                emailErrorSpan.textContent = "Inserisci un indirizzo email valido.";
                emailErrorSpan.style.display = "block";
                emailInput.style.borderColor = "red";
                delete emailInput.dataset.exists;
                return;
            }

            // Se la sintassi è corretta, puliamo temporaneamente l'errore in attesa del server
            clearEmailError();
            emailInput.style.borderColor = "green"; // Segnala sintassi ok

            // Recupera il ContextPath dall'action del form se presente, altrimenti usa fallback
            let contextPath = "";
            if (registerForm && registerForm.action) {
                const actionUrl = new URL(registerForm.action, window.location.href);
                // Prende la prima parte del path (es: /NomeProgetto)
                contextPath = actionUrl.pathname.substring(0, actionUrl.pathname.indexOf('/', 1));
            }

            // 2. Controllo Esistenza via AJAX
            const servletUrl = `${contextPath}/VerificaEmailServlet?email=${encodeURIComponent(emailValore)}`;

            fetch(servletUrl)
                .then(resp => {
                    if (!resp.ok) throw new Error("Errore risposta server");
                    return resp.json();
                })
                .then(data => {
                    if (data && data.exists) {
                        emailErrorSpan.textContent = "Questa email è già registrata.";
                        emailErrorSpan.style.display = "block";
                        emailInput.style.borderColor = "red";
                        emailInput.dataset.exists = "true";
                    } else {
                        // Email valida e disponibile
                        clearEmailError();
                        emailInput.style.borderColor = 'green';
                        emailInput.dataset.exists = 'false';
                    }
                })
                .catch(err => {
                    console.error('Errore durante la verifica AJAX:', err);
                    // In caso di errore server/rete, non blocchiamo l'utente sul controllo AJAX
                    clearEmailError();
                });
        }

        // Evento Input (digitazione): azzera l'errore e avvia un debounce di 400ms
        emailInput.addEventListener('input', function() {
            const value = emailInput.value.trim();
            
            // Pulisce errori pendenti mentre l'utente sta scrivendo
            if (!value || emailRegex.test(value)) {
                clearEmailError();
            }

            if (emailTimer) clearTimeout(emailTimer);
            emailTimer = setTimeout(() => checkEmail(value), 400);
        });

        // Evento Blur (perdita di focus)
        emailInput.addEventListener('blur', function() {
            const value = emailInput.value.trim();
            if (emailTimer) clearTimeout(emailTimer);
            checkEmail(value);
        });
    }

    // ==========================================
    // 2. VALIDAZIONE FINALE AL MOMENTO DEL SUBMIT
    // ==========================================
    if (registerForm) {
        registerForm.addEventListener("submit", function(event) {
            let isValid = true;

            // 2.1 (Username rimosso) - validazione lato client gestita solo per campi rimasti

            // 2.2 Controllo duplicato Email (da dataset AJAX)
            if (emailInput && emailInput.dataset.exists === "true") {
                const errorSpan = document.getElementById("emailError");
                if (errorSpan) {
                    errorSpan.textContent = "Impossibile procedere: l'email è già associata a un altro account.";
                    errorSpan.style.display = "block";
                }
                if (isValid) emailInput.focus();
                isValid = false;
            }

            if (!isValid) {
                event.preventDefault();
            }
        });
    }
});