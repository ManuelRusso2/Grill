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
        // L'evento 'blur' si attiva quando l'utente sposta il cursore (focus) fuori dal campo email
        emailInput.addEventListener("blur", function() {
            const emailValore = emailInput.value.trim();
            const emailErrorSpan = document.getElementById("emailError");
            if (!emailErrorSpan) return;

            // Espressione regolare per la validazione sintattica dell'indirizzo email
            const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[a-zA-Z]{2,}$/;

            // 1.1 Validazione sintattica immediata (lato client)
            if (!emailRegex.test(emailValore)) {
                emailErrorSpan.textContent = "Inserisci un indirizzo email valido.";
                emailErrorSpan.style.display = "block";
                emailInput.style.borderColor = "red";
                return; // Interrompiamo l'esecuzione: inutile interrogare il DB se la sintassi è errata
            }

            // 1.2 Validazione asincrona (AJAX): Controlliamo sul DB se l'email è già in uso
            fetch(`../VerificaEmailServlet?email=${encodeURIComponent(emailValore)}`)
                .then(response => response.json()) // Convertiamo la risposta HTTP in formato JSON
                .then(data => {
                    if (data.exists) {
                        // Se l'email esiste già sul database, mostriamo l'errore inline
                        emailErrorSpan.textContent = "Questa email è già registrata.";
                        emailErrorSpan.style.display = "block";
                        emailInput.style.borderColor = "red";
                        // Utilizziamo un attributo 'dataset' personalizzato per salvare lo stato dell'errore
                        emailInput.dataset.exists = "true";
                    } else {
                        // Se l'email è libera, coloriamo il bordo di verde e nascondiamo l'errore
                        emailErrorSpan.textContent = "";
                        emailErrorSpan.style.display = "none";
                        emailInput.style.borderColor = "green";
                        emailInput.dataset.exists = "false";
                    }
                })
                .catch(error => console.error("Errore AJAX:", error));
        });
    }

    // ==========================================
    // 2. VALIDAZIONE FINALE AL MOMENTO DEL SUBMIT
    // ==========================================
    if (registerForm) {
        // L'evento 'submit' intercetta il click sul pulsante di invio del form
        registerForm.addEventListener("submit", function(event) {
            let isValid = true; // Flag di stato della validazione del form

            // Reset preventivo di tutti i messaggi di errore precedenti
            const errorSpans = document.querySelectorAll(".error-message");
            errorSpans.forEach(span => {
                span.textContent = "";
                span.style.display = "none";
            });

            // 2.1 Controllo di validità del campo Username
            const usernameInput = document.getElementById("username");
            // Regex: solo caratteri alfanumerici o underscore, lunghezza compresa tra 4 e 20 caratteri
            const usernameRegex = /^[a-zA-Z0-9_]{4,20}$/;
            
            if (usernameInput && !usernameRegex.test(usernameInput.value.trim())) {
                const errorSpan = document.getElementById("usernameError");
                if (errorSpan) {
                    errorSpan.textContent = "L'username deve contenere solo lettere, numeri o '_' (4-20 caratteri).";
                    errorSpan.style.display = "block";
                }
                // Se è il primo errore rilevato, impostiamo il focus automatico della tastiera sul campo
                if (isValid) usernameInput.focus();
                isValid = false;
            }

            // 2.2 Controllo sullo stato dell'email (proveniente dal controllo asincrono AJAX)
            // Impediamo l'invio del form se l'evento blur ha accertato che l'email è già registrata
            if (emailInput && emailInput.dataset.exists === "true") {
                const errorSpan = document.getElementById("emailError");
                if (errorSpan) {
                    errorSpan.textContent = "Impossibile procedere: l'email è già associata a un altro account.";
                    errorSpan.style.display = "block";
                }
                if (isValid) emailInput.focus();
                isValid = false;
            }

            // 2.3 Blocco della sottomissione
            if (!isValid) {
                // Invocando event.preventDefault(), impediamo al browser di ricaricare la pagina ed inviare i dati alla Servlet
                event.preventDefault();
            }
        });
    }
});