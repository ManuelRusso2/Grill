/**
 * Gestione asincrona dei suggerimenti di ricerca tramite AJAX e Fetch API.
 * Attende il caricamento completo del DOM per evitare di accedere a elementi non ancora pronti.
 */
document.addEventListener("DOMContentLoaded", function () {
    // 1. Recuperiamo gli elementi HTML della barra di ricerca e del contenitore dei suggerimenti
    const input = document.getElementById("searchInput");
    const container = document.getElementById("searchSuggestions");

    // Se la barra di ricerca non è presente nella pagina corrente, interrompe l'esecuzione
    if (!input || !container) return;

    let timeoutId; // Variabile di appoggio per il meccanismo di ritardo della chiamata

    /**
     * Sottometodo helper per renderizzare graficamente i suggerimenti ricevuti in formato JSON.
     * @param {Array} items - Array di oggetti prodotto restituiti dalla servlet
     */
    function render(items) {
        container.innerHTML = ""; // Svuotiamo i suggerimenti della ricerca precedente
        
        // Se non ci sono risultati, nascondiamo il contenitore grafico e usciamo
        if (!items.length) {
            container.style.display = "none";
            return;
        }

        // Cicliamo ogni singolo prodotto ricevuto dal server per generare il markup dinamico
        items.forEach(item => {
            const div = document.createElement("div");
            div.className = "suggestion-item"; // Classe CSS per la formattazione grafica
            
            // Usiamo 'textContent' per prevenire attacchi di tipo XSS (Cross-Site Scripting)
            div.textContent = `${item.nome} - €${Number(item.prezzo).toFixed(2)}`;
            
            // Evento click sul suggerimento: reindirizza l'utente alla servlet di dettaglio del prodotto
            div.addEventListener("click", () => {
                window.location.href = `DettaglioProdottoServlet?id=${item.id}`;
            });
            
            container.appendChild(div); // Aggiungiamo il singolo elemento al pannello dei suggerimenti
        });
        
        container.style.display = "block"; // Rendiamo visibile il box dei suggerimenti
    }

    /**
     * Ascoltatore dell'evento di digitazione (input) nella barra di ricerca.
     */
    input.addEventListener("input", function () {
        const query = input.value.trim(); // Recuperiamo il testo inserito rimuovendo gli spazi vuoti esterni
        clearTimeout(timeoutId); // Cancella la chiamata AJAX precedente se l'utente sta continuando a digitare velocemente

        // Se l'utente ha inserito meno di 2 caratteri, non inviamo richieste e svuotiamo la lista
        if (query.length < 2) {
            render([]);
            return;
        }

        // Avviamo il timer. Se l'utente non digita nient'altro per 250ms, parte la chiamata AJAX
        timeoutId = setTimeout(() => {
            // Effettuiamo la chiamata asincrona alla Servlet di ricerca passando la query codificata nell'URL
            fetch(`RicercaAjaxServlet?query=${encodeURIComponent(query)}`)
                .then(resp => resp.json()) // Convertiamo la risposta HTTP della Servlet da stringa JSON a oggetto JavaScript
                .then(render)             // Passiamo l'array convertito alla nostra funzione di renderizzazione grafica
                .catch(() => render([])); // In caso di errore di rete o del server, resettiamo e nascondiamo i suggerimenti
        }, 250);
    });

    /**
     * Ascoltatore di eventi globale sui click della pagina.
     * Serve a nascondere il pannello dei suggerimenti quando l'utente clicca all'esterno della ricerca.
     */
    document.addEventListener("click", function (event) {
        // Se il click avviene fuori dal contenitore dei suggerimenti E fuori dall'input di ricerca...
        if (!container.contains(event.target) && event.target !== input) {
            container.style.display = "none"; // ...nascondiamo i suggerimenti
        }
    });
});