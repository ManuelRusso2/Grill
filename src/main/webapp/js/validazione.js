document.addEventListener("DOMContentLoaded", function () {

    const form        = document.getElementById("registerForm");
    const nomeInput   = document.getElementById("nome");
    const cognomeInput= document.getElementById("cognome");
    const emailInput  = document.getElementById("email");
    const passwordInput = document.getElementById("password");
    const telefonoInput = document.getElementById("telefono");

    if (!form) return;

    const emailRegex    = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    const telefonoRegex = /^[0-9+\s\-]{7,20}$/;

    // ── Helpers ──────────────────────────────────────────────────────────────

    function showError(input, message) {
        input.classList.add("input-error");
        input.classList.remove("input-ok");
        let span = input.parentElement.querySelector(".field-error-js");
        if (!span) {
            span = document.createElement("span");
            span.className = "field-error field-error-js";
            input.insertAdjacentElement("afterend", span);
        }
        span.textContent = message;
        span.style.display = "block";
    }

    function clearError(input) {
        input.classList.remove("input-error");
        input.classList.add("input-ok");
        const span = input.parentElement.querySelector(".field-error-js");
        if (span) span.style.display = "none";
    }

    // ── Validatori per campo ─────────────────────────────────────────────────

    function validateNome() {
        const v = nomeInput.value.trim();
        if (!v) { showError(nomeInput, "Il nome è obbligatorio."); return false; }
        if (v.length < 2) { showError(nomeInput, "Il nome deve contenere almeno 2 caratteri."); return false; }
        clearError(nomeInput); return true;
    }

    function validateCognome() {
        const v = cognomeInput.value.trim();
        if (!v) { showError(cognomeInput, "Il cognome è obbligatorio."); return false; }
        if (v.length < 2) { showError(cognomeInput, "Il cognome deve contenere almeno 2 caratteri."); return false; }
        clearError(cognomeInput); return true;
    }

    function validatePassword() {
        const v = passwordInput.value;
        if (!v) { showError(passwordInput, "La password è obbligatoria."); return false; }
        if (v.length < 6) { showError(passwordInput, "La password deve contenere almeno 6 caratteri."); return false; }
        if (!/[A-Z]/.test(v)) { showError(passwordInput, "La password deve contenere almeno una lettera maiuscola."); return false; }
        if (!/[0-9]/.test(v)) { showError(passwordInput, "La password deve contenere almeno un numero."); return false; }
        clearError(passwordInput); return true;
    }

    function validateTelefono() {
        const v = telefonoInput.value.trim();
        if (v && !telefonoRegex.test(v)) {
            showError(telefonoInput, "Inserisci un numero di telefono valido (es. 333 1234567).");
            return false;
        }
        clearError(telefonoInput); return true;
    }

    // ── Validazione email con debounce + AJAX ────────────────────────────────

    let emailTimer = null;

    function validateEmailSyntax() {
        const v = emailInput.value.trim();
        if (!v) { showError(emailInput, "L'email è obbligatoria."); return false; }
        if (!emailRegex.test(v)) { showError(emailInput, "Inserisci un indirizzo email valido (es. nome@dominio.it)."); return false; }
        return true;
    }

    function checkEmailAjax(value) {
        if (!validateEmailSyntax()) return;
        const contextPath = form.getAttribute("data-contextpath") || "";
        fetch(`${contextPath}/VerificaEmailServlet?email=${encodeURIComponent(value)}`)
            .then(r => r.json())
            .then(data => {
                if (data && data.exists) {
                    showError(emailInput, "Questa email è già registrata.");
                    emailInput.dataset.exists = "true";
                } else {
                    clearError(emailInput);
                    emailInput.dataset.exists = "false";
                }
            })
            .catch(() => clearError(emailInput));
    }

    emailInput.addEventListener("input", function () {
        clearTimeout(emailTimer);
        const v = emailInput.value.trim();
        if (!v || !emailRegex.test(v)) {
            validateEmailSyntax();
            return;
        }
        clearError(emailInput);
        emailTimer = setTimeout(() => checkEmailAjax(v), 400);
    });

    emailInput.addEventListener("blur", function () {
        clearTimeout(emailTimer);
        checkEmailAjax(emailInput.value.trim());
    });

    // ── Listener blur per gli altri campi ────────────────────────────────────

    nomeInput.addEventListener("blur", validateNome);
    cognomeInput.addEventListener("blur", validateCognome);
    passwordInput.addEventListener("blur", validatePassword);
    passwordInput.addEventListener("input", validatePassword);
    telefonoInput.addEventListener("blur", validateTelefono);

    // ── Validazione finale al submit ─────────────────────────────────────────

    form.addEventListener("submit", function (e) {
        const ok = [
            validateNome(),
            validateCognome(),
            validateEmailSyntax(),
            validatePassword(),
            validateTelefono()
        ].every(Boolean);

        if (emailInput.dataset.exists === "true") {
            showError(emailInput, "Questa email è già registrata.");
            e.preventDefault();
            return;
        }

        if (!ok) e.preventDefault();
    });
});
