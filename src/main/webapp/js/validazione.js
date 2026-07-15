document.addEventListener("DOMContentLoaded", function() {
    // ==========================================
    // 1. CONTROLLO AJAX IN TEMPO REALE (SULL'EMAIL)
    // ==========================================
    const emailInput = document.getElementById("email");
    const registerForm = document.getElementById("registerForm");

    if (emailInput) {
        emailInput.addEventListener("blur", function() {
            const emailValore = emailInput.value.trim();
            const emailErrorSpan = document.getElementById("emailError");
            if (!emailErrorSpan) return;

            const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[a-zA-Z]{2,}$/;

            if (!emailRegex.test(emailValore)) {
                emailErrorSpan.textContent = "Inserisci un indirizzo email valido.";
                emailErrorSpan.style.display = "block";
                emailInput.style.borderColor = "red";
                return;
            }

            fetch(`../VerificaEmailServlet?email=${encodeURIComponent(emailValore)}`)
                .then(response => response.json())
                .then(data => {
                    if (data.exists) {
                        emailErrorSpan.textContent = "Questa email è già registrata.";
                        emailErrorSpan.style.display = "block";
                        emailInput.style.borderColor = "red";
                        emailInput.dataset.exists = "true";
                    } else {
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
        registerForm.addEventListener("submit", function(event) {
            let isValid = true;

            const errorSpans = document.querySelectorAll(".error-message");
            errorSpans.forEach(span => {
                span.textContent = "";
                span.style.display = "none";
            });

            const usernameInput = document.getElementById("username");
            const usernameRegex = /^[a-zA-Z0-9_]{4,20}$/;
            if (usernameInput && !usernameRegex.test(usernameInput.value.trim())) {
                const errorSpan = document.getElementById("usernameError");
                if (errorSpan) {
                    errorSpan.textContent = "L'username deve contenere solo lettere, numeri o '_' (4-20 caratteri).";
                    errorSpan.style.display = "block";
                }
                if (isValid) usernameInput.focus();
                isValid = false;
            }

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