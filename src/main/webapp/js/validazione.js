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

	// =========================================================================
	    // REGEX VALIDAZIONE EMAIL
	    // =========================================================================
	    // ^                  : Inizio della stringa
	    // [a-zA-Z0-9._%+-]+  : Nome utente (almeno un carattere tra lettere, numeri e . _ % + -)
	    // @                  : Simbolo chiocciola obbligatorio
	    // [a-zA-Z0-9.-]+     : Nome del dominio (almeno un carattere tra lettere, numeri, punti e trattini)
	    // \.                 : Punto letterale che separa dominio ed estensione (con \ per l'escape)
	    // [a-zA-Z]{2,}       : Estensione del dominio (es. .it, .com) di almeno 2 lettere
	    // $                  : Fine della stringa
	    var emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

	    // =========================================================================
	    // REGEX VALIDAZIONE TELEFONO
	    // =========================================================================
	    // ^                  : Inizio della stringa
	    // [0-9+\s\-]         : Caratteri ammessi (cifre da 0 a 9, simbolo +, spazi \s e trattini -)
	    // {7,20}             : Lunghezza consentita (minimo 7, massimo 20 caratteri totali)
	    // $                  : Fine della stringa
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

	        // Cerca prima uno span di errore già presente (sia lato server che creato da JS)
	        var span = input.parentElement.querySelector(".field-error-span");
	        if (!span) {
	            // Se non esiste, crea lo span con la classe corretta usata nei tuoi CSS
	            span = document.createElement("span");
	            span.className = "field-error-span field-error-js";
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

        var span = input.parentElement.querySelector(".field-error-span");
		if (span) {
			span.style.display = "none";
		    span.textContent = ""; // Pulisce il testo dell'errore
		}
    }

    // ── VALIDATORI SINGOLI CAMPI ─────────────────────────────────────────────

	/**
	 * Valida il campo Nome.
	 * Verifica che il campo sia presente, non sia vuoto e contenga almeno 2 caratteri.
	 * 
	 * @returns {boolean} true se il nome è valido, false se contiene errori
	 */
	function validateNome() {
	    // Se l'elemento HTML non esiste nella pagina, considera la validazione superata
	    if (!nomeInput) return true;

	    // Estrae il valore inserito dall'utente rimuovendo gli spazi bianchi a inizio e fine
	    var v = nomeInput.value.trim();

	    // Controllo 1: Verifica obbligatorietà (se la stringa è vuota)
	    if (!v) {
	        showError(nomeInput, "Il nome è obbligatorio.");
	        return false; // Blocca la validazione e restituisce errore
	    }

	    // Controllo 2: Verifica lunghezza minima (almeno 2 caratteri)
	    if (v.length < 2) {
	        showError(nomeInput, "Il nome deve contenere almeno 2 caratteri.");
	        return false; // Blocca la validazione e restituisce errore
	    }

	    // Se tutti i controlli sono superati, rimuove eventuali messaggi di errore precedenti
	    clearError(nomeInput);

	    // Restituisce true per confermare che il campo è valido
	    return true;
	}

	/**
	 * Valida il campo Cognome.
	 * Verifica che il campo sia presente, non sia vuoto e contenga almeno 2 caratteri.
	 * 
	 * @returns {boolean} true se il cognome è valido, false se contiene errori
	 */
	function validateCognome() {
	    // Se l'elemento HTML non esiste nella pagina, considera la validazione superata
	    if (!cognomeInput) return true;

	    // Estrae il valore inserito dall'utente rimuovendo gli spazi bianchi a inizio e fine
	    var v = cognomeInput.value.trim();

	    // Controllo 1: Verifica obbligatorietà (se la stringa è vuota)
	    if (!v) {
	        showError(cognomeInput, "Il cognome è obbligatorio.");
	        return false; // Blocca la validazione e restituisce errore
	    }

	    // Controllo 2: Verifica lunghezza minima (almeno 2 caratteri)
	    if (v.length < 2) {
	        showError(cognomeInput, "Il cognome deve contenere almeno 2 caratteri.");
	        return false; // Blocca la validazione e restituisce errore
	    }

	    // Se tutti i controlli sono superati, rimuove eventuali messaggi di errore precedenti
	    clearError(cognomeInput);

	    // Restituisce true per confermare che il campo è valido
	    return true;
	}

	/**
	 * Valida la Password.
	 * Verifica che il campo sia presente, non vuoto, abbia almeno 6 caratteri,
	 * contenga almeno una lettera maiuscola e almeno un numero.
	 * 
	 * @returns {boolean} true se la password rispetta i requisiti di sicurezza, false altrimenti
	 */
	function validatePassword() {
	    // Se l'elemento HTML non esiste nella pagina, considera la validazione superata
	    if (!passwordInput) return true;

	    // Estrae il valore inserito dall'utente (senza trim per preservare eventuali spazi intenzionali)
	    var v = passwordInput.value;

	    // Controllo 1: Verifica obbligatorietà (se la stringa è vuota)
	    if (!v) {
	        showError(passwordInput, "La password è obbligatoria.");
	        return false; // Blocca la validazione e segnala l'errore
	    }

	    // Controllo 2: Verifica lunghezza minima (almeno 6 caratteri)
	    if (v.length < 6) {
	        showError(passwordInput, "La password deve contenere almeno 6 caratteri.");
	        return false; // Blocca la validazione e segnala l'errore
	    }

	    // Controllo 3: Presenza di almeno una lettera maiuscola (regex [A-Z])
	    if (!/[A-Z]/.test(v)) {
	        showError(passwordInput, "La password deve contenere almeno una maiuscola.");
	        return false; // Blocca la validazione e segnala l'errore
	    }

	    // Controllo 4: Presenza di almeno un numero (regex [0-9])
	    if (!/[0-9]/.test(v)) {
	        showError(passwordInput, "La password deve contenere almeno un numero.");
	        return false; // Blocca la validazione e segnala l'errore
	    }

	    // Se tutti i controlli hanno esito positivo, rimuove eventuali messaggi d'errore visibili
	    clearError(passwordInput);

	    // Restituisce true per confermare la validità della password
	    return true;
	}

	/**
	 * Valida il Numero di Telefono.
	 * Trattandosi di un campo opzionale, se è vuoto viene considerato valido;
	 * se viene compilato, deve rispettare il formato definito dalla regex (telefonoRegex).
	 * 
	 * @returns {boolean} true se il campo è vuoto o ha una sintassi valida, false se il formato è errato
	 */
	function validateTelefono() {
	    // Se l'elemento HTML non esiste nella pagina, considera la validazione superata
	    if (!telefonoInput) return true;

	    // Estrae il valore inserito dall'utente rimuovendo gli spazi bianchi a inizio e fine
	    var v = telefonoInput.value.trim();

	    // Controllo formato: Se il campo non è vuoto (v), verifica che rispetti la Regex del telefono
	    if (v && !telefonoRegex.test(v)) {
	        showError(telefonoInput, "Inserisci un numero di telefono valido.");
	        return false; // Blocca la validazione e segnala l'errore
	    }

	    // Se il campo è vuoto oppure se rispetta la regex, rimuove eventuali messaggi d'errore visibili
	    clearError(telefonoInput);

	    // Restituisce true per confermare la validità del campo
	    return true;
	}

	/**
	 * Valida la sintassi dell'indirizzo Email.
	 * Verifica che il campo sia presente, non vuoto e rispetti il formato standard via Regex.
	 * 
	 * @returns {boolean} true se il formato dell'email è valido, false altrimenti
	 */
	function validateEmailSyntax() {
	    // Se l'elemento HTML non esiste nella pagina, considera la validazione superata
	    if (!emailInput) return true;

	    // Estrae il valore inserito dall'utente rimuovendo gli spazi bianchi a inizio e fine
	    var v = emailInput.value.trim();

	    // Controllo 1: Verifica obbligatorietà (se la stringa è vuota)
	    if (!v) {
	        showError(emailInput, "L'email è obbligatoria.");
	        return false; // Blocca la validazione e segnala l'errore
	    }

	    // Controllo 2: Verifica la sintassi dell'email tramite l'espressione regolare (emailRegex)
	    if (!emailRegex.test(v)) {
	        showError(emailInput, "Inserisci un indirizzo email valido.");
	        return false; // Blocca la validazione e segnala l'errore
	    }

	    // Restituisce true per confermare che la sintassi dell'email è corretta
	    return true;
	}

    // ── CONTROLLO AJAX PER EMAIL UNICA ───────────────────────────────────────
    var emailTimer = null; // Timer per il debounce della chiamata Fetch

	/**
	 * Verifica in modo asincrono (AJAX) se l'email inserita sia già presente nel Database.
	 * Invia una richiesta HTTP GET a 'VerificaEmailServlet' e aggiorna dinamicamente
	 * l'interfaccia utente oltre all'attributo HTML `data-exists` sull'input email.
	 *
	 * @param {string} value - L'indirizzo email da verificare a database
	 */
	function checkEmailAjax(value) {
	    // Controllo preventivo: se la sintassi dell'email non è valida, evita una chiamata di rete inutile
	    if (!validateEmailSyntax()) return;

	    // Recupera il contesto dinamico dell'applicazione dall'attributo 'data-contextpath' del form
	    var contextPath = form.getAttribute("data-contextpath") || "";
	        
	    // Esegue la chiamata asincrona via Fetch API alla Servlet di verifica email (con encoding dell'email)
	    fetch(contextPath + "/VerificaEmailServlet?email=" + encodeURIComponent(value))
	        .then(function (r) {
	            // Se la risposta HTTP non ha esito positivo (es. status != 200), lancia un'eccezione
	            if (!r.ok) throw new Error("Errore risposta server");
	                
	            // Converte il corpo della risposta da stringa JSON a oggetto JavaScript
	            return r.json();
	        })
	        .then(function (data) {
	            // Caso 1: L'email è già presente a database (data.exists === true)
	            if (data && data.exists) {
	                showError(emailInput, "Questa email è già registrata.");
	                emailInput.setAttribute("data-exists", "true"); // Traccia lo stato per il submit
	            } 
	            // Caso 2: L'email è libera e disponibile per la registrazione
	            else {
	                clearError(emailInput);
	                emailInput.setAttribute("data-exists", "false"); // Traccia lo stato per il submit
	            }
	        })
	        .catch(function () {
	            // Gestione del fallback in caso di offline, errore di rete o anomalie del server
	            clearError(emailInput);
	        });
	    }

		// ── REGISTRAZIONE DEGLI EVENT LISTENER (INPUT E BLUR) ───────────────────────

		// Assegna la validazione alla perdita del focus (evento 'blur') per i campi standard
		if (nomeInput) nomeInput.addEventListener("blur", validateNome);
		if (cognomeInput) cognomeInput.addEventListener("blur", validateCognome);
		if (passwordInput) passwordInput.addEventListener("blur", validatePassword);
		if (telefonoInput) telefonoInput.addEventListener("blur", validateTelefono);

		// Gestione speciale con debounce per il campo Email
		if (emailInput) {
		        
		    // Controllo in tempo reale durante la digitazione (evento 'input')
		    emailInput.addEventListener("input", function () {
		        // Annulla l'eventuale timer precedente in attesa (tecnica del Debounce)
		        clearTimeout(emailTimer);
		            
		        var v = emailInput.value.trim();
		            
		        // Se la sintassi dell'email è errata, blocca l'esecuzione senza fare la chiamata AJAX
		        if (!validateEmailSyntax()) return;

		        // Avvia un nuovo timer di 350ms: la chiamata AJAX parte solo se l'utente smette di digitare
		        emailTimer = setTimeout(function () {
		            checkEmailAjax(v);
		        }, 350);
		    });

		    // Esegue la verifica AJAX immediata non appena il campo perde il focus (evento 'blur')
		    emailInput.addEventListener("blur", function () {
		        // Cancella il timer del debounce per evitare chiamate duplicate
		        clearTimeout(emailTimer);
		            
		        // Forza il controllo immediato sul server dell'email inserita
		        checkEmailAjax(emailInput.value.trim());
		    });
		}

		// ── GESTIONE DELL'EVENTO SUBMIT DEL FORM ─────────────────────────────────

	/**
	 * Intercetta l'invio del modulo di registrazione (`#registerForm`).
	 * Esegue un controllo globale su tutti i campi, verifica che l'email non sia già in uso,
	 * blocca la sottomissione in caso di errori e sposta il cursore sul primo campo non valido.
	 */
	form.addEventListener("submit", function (e) {
		// 1. Esegue tutte le funzioni di validazione singole per mostrare gli errori visibili inline
		var isNomeOk = validateNome();
		var isCognomeOk = validateCognome();
		var isEmailOk = validateEmailSyntax();
		var isPasswordOk = validatePassword();
		var isTelefonoOk = validateTelefono();

		// 2. Calcola la validità complessiva del modulo (deve essere true per ciascun campo)
		var isFormValid = isNomeOk && isCognomeOk && isEmailOk && isPasswordOk && isTelefonoOk;
		        
		// 3. Verifica l'attributo HTML custom 'data-exists' impostato precedentemente dalla chiamata AJAX
		var isEmailTaken = emailInput && emailInput.getAttribute("data-exists") === "true";

		// Se l'email è già registrata nel DB, forza la visualizzazione dell'errore visivo
		if (isEmailTaken) {
		    showError(emailInput, "Questa email è già registrata.");
		}

		// 4. Se anche un solo campo è errato o l'email è già in uso, interrompe l'invio del form
		if (!isFormValid || isEmailTaken) {
		    // Annulla il comportamento predefinito del browser (impedisce l'invio della richiesta HTTP)
		    e.preventDefault(); 
		            
		    // 5. Ottimizzazione UX: posiziona automaticamente il cursore (focus) sul primo campo con errore
		    if (!isNomeOk && nomeInput) nomeInput.focus();
		    else if (!isCognomeOk && cognomeInput) cognomeInput.focus();
		    else if ((!isEmailOk || isEmailTaken) && emailInput) emailInput.focus();
		    else if (!isPasswordOk && passwordInput) passwordInput.focus();
		    else if (!isTelefonoOk && telefonoInput) telefonoInput.focus();
		}
	});
});