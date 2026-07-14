// ==========================================
// 1. CONTROLLO AJAX IN TEMPO REALE (SULL'EMAIL)
// ==========================================
document.getElementById("email").addEventListener("blur", function() {
    const emailInput = this;
    const emailValore = emailInput.value.trim();
    const emailErrorSpan = document.getElementById("emailError");

    // Validazione dell'email prima di disturbare il server con AJAX
    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (!emailRegex.test(emailValore)) {
        emailErrorSpan.textContent = "Inserisci un indirizzo email valido.";
        emailErrorSpan.style.display = "block";
        emailInput.style.borderColor = "red";
        return; 
    }

    // Chiamata AJAX con Fetch API
    fetch(`../VerificaEmailServlet?email=${encodeURIComponent(emailValore)}`)
        .then(response => response.json())
        .then(data => {
            if (data.exists) {
                emailErrorSpan.textContent = "Questa email è già registrata.";
                emailErrorSpan.style.display = "block";
                emailInput.style.borderColor = "red";
                emailInput.dataset.exists = "true"; // Ci salviamo temporaneamente questa info
            } else {
                emailErrorSpan.textContent = "";
                emailErrorSpan.style.display = "none";
                emailInput.style.borderColor = "green";
                emailInput.dataset.exists = "false";
            }
        })
        .catch(error => console.error("Errore AJAX:", error));
});


// ==========================================
// 2. VALIDAZIONE FINALE AL MOMENTO DEL SUBMIT
// ==========================================
document.getElementById("registerForm").addEventListener("submit", function(event) {
    let isValid = true;
    
    // Svuotiamo gli errori precedenti
    const errorSpans = document.querySelectorAll(".error-message");
    errorSpans.forEach(span => {
        span.textContent = "";
        span.style.display = "none";
    });

    // Validazione USERNAME
    const usernameInput = document.getElementById("username");
    const usernameRegex = /^[a-zA-Z0-9_]{4,20}$/;
    if (!usernameRegex.test(usernameInput.value.trim())) {
        const errorSpan = document.getElementById("usernameError");
        errorSpan.textContent = "L'username deve contenere solo lettere, numeri o '_' (4-20 caratteri).";
        errorSpan.style.display = "block";
        if (isValid) usernameInput.focus();
        isValid = false;
    }

    // Validazione EMAIL (blocco preventivo sul submit in caso di email già registrata tramite AJAX)
    const emailInput = document.getElementById("email");
    if (emailInput.dataset.exists === "true") {
        const errorSpan = document.getElementById("emailError");
        errorSpan.textContent = "Impossibile procedere: l'email è già associata a un altro account.";
        errorSpan.style.display = "block";
        if (isValid) emailInput.focus();
        isValid = false;
    }

    // Se c'è anche un solo errore, blocchiamo l'invio del form
    if (!isValid) {
        event.preventDefault(); 
    }
});