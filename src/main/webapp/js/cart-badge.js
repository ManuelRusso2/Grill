/**
 * Modulo per la gestione e l'aggiornamento dinamico del badge del carrello nell'interfaccia utente.
 * Supporta chiamate sincrone/inline, eventi personalizzati e inizializzazione lato server.
 */

/**
 * Aggiorna il testo del badge del carrello nel DOM con il conteggio corrente degli articoli.
 * 
 * @param {number|string} count - Il numero di articoli presenti nel carrello.
 */
function setBadge(count) {
    // Recupera l'elemento SPAN preposto a mostrare il contatore nel DOM
    const cartCountSpan = document.getElementById('cart-count');
    
    // Se l'elemento non è presente nella pagina corrente, interrompe l'esecuzione
    if (!cartCountSpan) return;

    // Converte il parametro in un valore numerico intero in base 10
    const num = parseInt(count, 10);

    // Mostra il numero tra parentesi se è un numero valido e maggiore di zero, altrimenti svuota il testo
    cartCountSpan.textContent = (!isNaN(num) && num > 0) ? '(' + num + ')' : '';
}

/**
 * Esposizione immediata della funzione nell'oggetto globale 'window'.
 * Permette l'invocazione diretta (es. script inline all'interno di pagine JSP) 
 * prima o indipendentemente dal caricamento completo del DOM.
 */
window.updateCartBadge = setBadge;

/**
 * Inizializzazione dei listener e dello stato iniziale al completamento del DOM.
 */
document.addEventListener('DOMContentLoaded', function () {
    
    // Ascolta l'evento personalizzato 'cartUpdated' lanciato da altri script (es. chiamate AJAX per aggiunta al carrello)
    window.addEventListener('cartUpdated', function (e) {
        if (e && e.detail) {
            // Estrae il conteggio gestendo sia la proprietà 'count' che 'cartCount' per retrocompatibilità
            const count = (e.detail.count !== undefined) ? e.detail.count : e.detail.cartCount;
            setBadge(count);
        }
    });

    // Inizializza il badge al caricamento della pagina se il server (JSP) ha definito una variabile globale iniziale
    if (typeof window.__initialCartCount !== 'undefined') {
        setBadge(window.__initialCartCount);
    }
});