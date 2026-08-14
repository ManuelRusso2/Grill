/**
 * Modulo per il completamento automatico (Autocompletion) della barra di ricerca.
 * Gestisce le chiamate AJAX asincrone con tecnica di debounce per recuperare i suggerimenti
 * dei prodotti in tempo reale e indirizzare l'utente alla pagina di dettaglio.
 */
document.addEventListener("DOMContentLoaded", function () {
    // Recupero dei riferimenti agli elementi DOM principali
    const input = document.getElementById("searchInput");
    const container = document.getElementById("searchSuggestions");

    // Interruzione preventiva se gli elementi necessari non sono presenti nella pagina
    if (!input || !container) return;

    // Recupera il path del contesto applicativo salvato nell'attributo HTML data-context-path
    const contextPath = input.dataset.contextPath || "";
    let timeoutId; // Timer per la gestione del debounce

    /**
     * Svuota e popola il contenitore dei suggerimenti con i dati ricevuti dal server.
     * 
     * @param {Array<Object>} items - Array di oggetti prodotto contenenti id, nome e prezzo.
     */
    function render(items) {
        container.innerHTML = ""; // Pulizia dei suggerimenti precedenti

        // Se non ci sono risultati, nasconde il menu a tendina
        if (!items || !items.length) {
            container.style.display = "none";
            return;
        }

        // Creazione dinamica degli elementi visuali per ciascun prodotto trovato
        items.forEach(item => {
            const div = document.createElement("div");
            div.className = "suggestion-item";
            
            // Formattazione del testo con nome e prezzo in formato valuta (€0.00)
            div.textContent = `${item.nome} — €${Number(item.prezzo).toFixed(2)}`;
            
            // Reindirizzamento alla Servlet di dettaglio del prodotto al click
            div.addEventListener("click", () => {
                window.location.href = `${contextPath}/DettaglioProdottoServlet?id=${item.id}`;
            });
            
            container.appendChild(div);
        });

        // Mostra il contenitore popolato
        container.style.display = "block";
    }

    /**
     * Gestore dell'evento di digitazione (input) nella barra di ricerca.
     * Utilizza un ritardo (debounce) di 250ms per limitare le richieste HTTP inviate al server.
     */
    input.addEventListener("input", function () {
        const query = input.value.trim();
        
        // Cancella il timer precedente se l'utente continua a digitare
        clearTimeout(timeoutId);

        // Se la ricerca contiene meno di 2 caratteri, nasconde i suggerimenti senza effettuare richieste
        if (query.length < 2) {
            render([]);
            return;
        }

        // Avvia il timer di debounce prima di effettuare la chiamata Fetch
        timeoutId = setTimeout(() => {
            const url = `${contextPath}/RicercaAjaxServlet?query=${encodeURIComponent(query)}`;

            // Esecuzione della chiamata AJAX alla servlet di ricerca
            fetch(url)
                .then(resp => {
                    if (!resp.ok) throw new Error("Errore durante la ricerca");
                    return resp.json();
                })
                .then(data => render(data))
                .catch(() => render([])); // In caso di errore o eccezione, pulisce il contenitore
        }, 250);
    });

    /**
     * Listener globale sui click per chiudere il menu dei suggerimenti 
     * quando l'utente clicca in un punto qualsiasi esterno al campo di testo o al contenitore.
     */
    document.addEventListener("click", function (event) {
        if (!container.contains(event.target) && event.target !== input) {
            container.style.display = "none";
        }
    });
});