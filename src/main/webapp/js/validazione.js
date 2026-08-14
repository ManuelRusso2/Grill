/**
 * Script di validazione lato client per il modulo di registrazione utenti (`#registerForm`).
 * Gestisce la validazione dei campi in tempo reale, feedback visivo inline degli errori,
 * un controllo asincrono (AJAX) di unicità dell'email e la gestione dello scroll/focus al submit.
 */
document.addEventListener("DOMContentLoaded", function () {
    // Recupero del form principale
    var form = document.getElementById("registerForm");
    if (!form) return; // Interrompe l'esecuzione se il form non è presente nel DOM

    // Riferimenti ai campi di input
    var nomeInput     = document.getElementById("nome");
    var cognomeInput  = document.getElementById("cognome");
    var emailInput    = document.getElementById("email");
    var passwordInput = document.getElementById("password");
    var telefonoInput = document.getElementById("telefono");

    // Espressioni regolari per il controllo dei formati
    var emailRegex    = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    var telefonoRegex = /^[0-9+\s\-]{7,20}$/;

    // ── HELPERS GESTIONE ERRORE INLINE (SENZA ALERT) ─────────────────────────

    /**
     * Mostra un messaggio di errore associato a uno specifico campo di input.
     * Aggiunge le classi CSS per lo stile di errore e crea/aggiorna l'elemento SPAN dedicato.
     * 
     * @param {HTMLElement} input - L'elemento input del form
     * @param {string} message - Il messaggio di errore da visualizzare
     */
    function showError(input, message) {
        if (!input) return;
        input.classList.add("input-error");
        input.classList.remove("input-ok");

        // Cerca lo span di errore esistente nel parent container
        var span = input.parentElement.querySelector(".field-error-js");
        if (!span) {
            // Se lo span non esiste ancora, lo crea e lo inserisce subito dopo l'input
            span = document.createElement("span");
            span.className = "field-error field-error-js";
            input.insertAdjacentElement("afterend", span);
        }
        span.textContent = message;
        span.style.display = "block";
    }

    /**
     * Rimuove il messaggio di errore e applica lo stato di validazione corretta al campo.
     * 
     * @param {HTMLElement} input - L'elemento input da resettare
     */
    function clearError(input) {
        if (!input) return;
        input.classList.remove("input-error");
        input.classList.add("input-ok");

        var span = input.parentElement.querySelector(".field-error-js");
        if (span) span.style.display = "none";
    }

    // ── VALIDATORI SINGOLI CAMPI ─────────────────────────────────────────────

    /**
     * Valida il campo Nome (Obbligatorio, min 2 caratteri).
     * @returns {boolean} true se valido, false altrimenti
     */
    function validateNome() {
        if (!nomeInput) return true;
        var v = nomeInput.value.trim();
        if (!v) {
            showError(nomeInput, "Il nome è obbligatorio.");
            return false;
        }
        if (v.length < 2) {
            showError(nomeInput, "Il nome deve contenere almeno 2 caratteri.");
            return false;
        }
        clearError(nomeInput);
        return true;
    }

    /**
     * Valida il campo Cognome (Obbligatorio, min 2 caratteri).
     * @returns {boolean} true se valido, false altrimenti
     */
    function validateCognome() {
        if (!cognomeInput) return true;
        var v = cognomeInput.value.trim();
        if (!v) {
            showError(cognomeInput, "Il cognome è obbligatorio.");
            return false;
        }
        if (v.length < 2) {
            showError(cognomeInput, "Il cognome deve contenere almeno 2 caratteri.");
            return false;
        }
        clearError(cognomeInput);
        return true;
    }

    /**
     * Valida la Password (Obbligatoria, min 6 caratteri, almeno 1 maiuscola e 1 numero).
     * @returns {boolean} true se valida, false altrimenti
     */
    function validatePassword() {
        if (!passwordInput) return true;
        var v = passwordInput.value;
        if (!v) {
            showError(passwordInput, "La password è obbligatoria.");
            return false;
        }
        if (v.length < 6) {
            showError(passwordInput, "La password deve contenere almeno 6 caratteri.");
            return false;
        }
        if (!/[A-Z]/.test(v)) {
            showError(passwordInput, "La password deve contenere almeno una maiuscola.");
            return false;
        }
        if (!/[0-9]/.test(v)) {
            showError(passwordInput, "La password deve contenere almeno un numero.");
            return false;
        }
        clearError(passwordInput);
        return true;
    }

    /**
     * Valida il Numero di Telefono (Opzionale, ma se compilato deve rispettare la regex).
     * @returns {boolean} true se valido o vuoto, false altrimenti
     */
    function validateTelefono() {
        if (!telefonoInput) return true;
        var v = telefonoInput.value.trim();
        if (v && !telefonoRegex.test(v)) {
            showError(telefonoInput, "Inserisci un numero di telefono valido.");
            return false;
        }
        clearError(telefonoInput);
        return true;
    }

    /**
     * Valida la sintassi dell'indirizzo Email (Obbligatoria, formato standard).
     * @returns {boolean} true se il formato è corretto, false altrimenti
     */
    function validateEmailSyntax() {
        if (!emailInput) return true;
        var v = emailInput.value.trim();
        if (!v) {
            showError(emailInput, "L'email è obbligatoria.");
            return false;
        }
        if (!emailRegex.test(v)) {
            showError(emailInput, "Inserisci un indirizzo email valido.");
            return false;
        }
        return true;
    }

    // ── CONTROLLO AJAX PER EMAIL UNICA ───────────────────────────────────────
    var emailTimer = null; // Timer per il debounce della chiamata Fetch

    /**
     * Verifica in modo asincrono (AJAX) se l'email inserita sia già presente nel DB.
     * Imposta l'attributo HTML `data-exists` sull'input email per tracciarne lo stato.
     * 
     * @param {string} value - L'indirizzo email da verificare
     */
    function checkEmailAjax(value) {
        // Se la sintassi dell'email non è valida, evita la chiamata di rete
        if (!validateEmailSyntax()) return;

        var contextPath = form.getAttribute("data-contextpath") || "";
        
        fetch(contextPath + "/VerificaEmailServlet?email=" + encodeURIComponent(value))
            .then(function (r) {
                if (!r.ok) throw new Error("Errore risposta server");
                return r.json();
            })
            .then(function (data) {
                if (data && data.exists) {
                    showError(emailInput, "Questa email è già registrata.");
                    emailInput.setAttribute("data-exists", "true");
                } else {
                    clearError(emailInput);
                    emailInput.setAttribute("data-exists", "false");
                }
            })
            .catch(function () {
                // In caso di errore server/connessione, resetta provvisoriamente lo stato di errore
                clearError(emailInput);
            });
    }

    // ── LISTENER DI INPUT E BLUR ─────────────────────────────────────────────

    // Esegue le validazioni singole quando il campo perde il focus (evento 'blur')
    if (nomeInput) nomeInput.addEventListener("blur", validateNome);
    if (cognomeInput) cognomeInput.addEventListener("blur", validateCognome);
    if (passwordInput) passwordInput.addEventListener("blur", validatePassword);
    if (telefonoInput) telefonoInput.addEventListener("blur", validateTelefono);

    if (emailInput) {
        // Controllo in tempo reale sulla digitazione con debounce di 350ms
        emailInput.addEventListener("input", function () {
            clearTimeout(emailTimer);
            var v = emailInput.value.trim();
            if (!validateEmailSyntax()) return;

            emailTimer = setTimeout(function () {
                checkEmailAjax(v);
            }, 350);
        });

        // Esegue subito la verifica AJAX alla perdita del focus
        emailInput.addEventListener("blur", function () {
            clearTimeout(emailTimer);
            checkEmailAjax(emailInput.value.trim());
        });
    }

    // ── VALIDAZIONE AL SUBMIT ────────────────────────────────────────────────

    /**
     * Intercetta l'invio del form per eseguire un controllo globale su tutti i campi.
     * Blocca l'invio in caso di errori o email duplicata e porta il focus sul primo campo non valido.
     */
    form.addEventListener("submit", function (e) {
        // Esegue tutte le funzioni di validazione
        var isNomeOk = validateNome();
        var isCognomeOk = validateCognome();
        var isEmailOk = validateEmailSyntax();
        var isPasswordOk = validatePassword();
        var isTelefonoOk = validateTelefono();

        // Verifica la validità complessiva del form
        var isFormValid = isNomeOk && isCognomeOk && isEmailOk && isPasswordOk && isTelefonoOk;
        var isEmailTaken = emailInput && emailInput.getAttribute("data-exists") === "true";

        if (isEmailTaken) {
            showError(emailInput, "Questa email è già registrata.");
        }

        // Se un qualsiasi campo non è valido o l'email è già in uso, blocca il submit
        if (!isFormValid || isEmailTaken) {
            e.preventDefault(); // Interrompe la sottomissione della richiesta HTTP
            
            // Focus automatico sul primo campo non valido per migliorare l'usabilità (UX)
            if (!isNomeOk && nomeInput) nomeInput.focus();
            else if (!isCognomeOk && cognomeInput) cognomeInput.focus();
            else if ((!isEmailOk || isEmailTaken) && emailInput) emailInput.focus();
            else if (!isPasswordOk && passwordInput) passwordInput.focus();
            else if (!isTelefonoOk && telefonoInput) telefonoInput.focus();
        }
    });
});