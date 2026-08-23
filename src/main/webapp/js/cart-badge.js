/**
 * Modulo per l'aggiornamento dinamico del badge del carrello nell'interfaccia utente.
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

    // Mostra il numero puro se valido e > 0, altrimenti nasconde il testo per non mostrare lo 0
	if (!isNaN(num) && num > 0) {
	    cartCountSpan.textContent = num;
	} else {
	    cartCountSpan.textContent = '';
	}
}

/**
 * Esposizione della funzione nell'oggetto globale 'window'
 * per consentirne l'invocazione da qualsiasi altro script (es. risposte AJAX).
 */
window.updateCartBadge = setBadge;