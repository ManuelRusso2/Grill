<%-- Impostazione del tipo di contenuto della pagina e della codifica dei caratteri (UTF-8) --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Inclusione delle librerie di tag JSTL per la logica di controllo e la formattazione --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Inclusione dei frammenti di codice statici per l'intestazione (header) e la barra di navigazione (menu) --%>
<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<%-- Contenitore principale della pagina Home --%>
<main class="container">

    <%-- ── SEZIONE 1: CAROUSEL DI COLLEZIONI ────────────────────────────────── --%>
    <section class="carousel-section">
        <h2>LE NOSTRE COLLEZIONI</h2>
        
        <%-- Struttura del carosello immagini --%>
        <div class="carousel-container">
            <%-- Pulsante di scorrimento verso sinistra --%>
            <button class="carousel-btn carousel-btn-prev" id="prevBtn" aria-label="Collezione precedente">&#10094;</button>
            
            <%-- Wrapper e traccia dinamica in cui verranno inserite le immagini via JS --%>
            <div class="carousel-wrapper">
                <div class="carousel-track" id="carouselTrack">
                    <%-- Elementi caricati dinamicamente da JavaScript --%>
                </div>
            </div>
            
            <%-- Pulsante di scorrimento verso destra --%>
            <button class="carousel-btn carousel-btn-next" id="nextBtn" aria-label="Collezione successiva">&#10095;</button>
        </div>

        <%-- Indicatori a pallino per identificare la slide corrente --%>
        <div class="carousel-indicators" id="carouselIndicators"></div>
    </section>

    <%-- ── SEZIONE 2: PRODOTTI CASUALI SCORREVOLI ──────────────────────────── --%>
    <section class="products-scroll-section">
        <h2>Prodotti in Evidenza</h2>
        
        <%-- Struttura dello slider orizzontale dei prodotti --%>
        <div class="products-scroll-container">
            <%-- Pulsante per scorrere i prodotti a sinistra --%>
            <button class="scroll-btn scroll-btn-prev" id="scrollPrevBtn" aria-label="Prodotti precedenti">&#10094;</button>
            
            <%-- Traccia dinamica popolata via AJAX con i prodotti in evidenza --%>
            <div class="products-scroll-wrapper">
                <div class="products-scroll-track" id="productsTrack">
                    <%-- Card prodotti generate via Fetch API da JavaScript --%>
                </div>
            </div>
            
            <%-- Pulsante per scorrere i prodotti a destra --%>
            <button class="scroll-btn scroll-btn-next" id="scrollNextBtn" aria-label="Prodotti successivi">&#10095;</button>
        </div>
    </section>

</main>

<%-- ── SCRIPT JS PER LOGICA CAROUSEL E CARICAMENTO PRODOTTI AJAX ────────────── --%>
<script>
document.addEventListener('DOMContentLoaded', () => {
    // Recupero dinamico del percorso di contesto dell'applicazione Web (Context Path)
    const contextPath = '${pageContext.request.contextPath}';

    <%-- ── FUNZIONI DI UTILITÀ PER SICUREZZA E FORMATTAZIONE ────────────────── --%>
    
    /**
     * Esegue il sanificamento delle stringhe per prevenire attacchi Cross-Site Scripting (XSS).
     * @param {string} str - La stringa da convertire in entità HTML sicure.
     */
    const escapeHtml = (str) => {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    };

    /**
     * Risolve il percorso corretto delle immagini gestendo URL assoluti, relativi o fallback predefiniti.
     * @param {string} path - Il percorso relativo o l'URL dell'immagine.
     */
    const getImgSrc = (path) => {
        if (!path) return contextPath + '/images/default.jpg';
        if (path.startsWith('http://') || path.startsWith('https://')) return path;
        return contextPath + '/' + path.replace(/^\/+/, '');
    };

    /**
     * Formatta un valore numerico in valuta Euro (€) usando la localizzazione italiana.
     * @param {number} val - Importo numerico da formattare.
     */
    const formatCurrency = (val) => {
        return new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR' }).format(val || 0);
    };

    <%-- ── LOGICA CAROUSEL COLLEZIONI ────────────────────────────────────────── --%>
    
    // Lista di oggetti contenenti i nomi delle immagini e gli ID collezione associati
    const collections = [
        { img: 'Bloom.png', id: 8 },
        { img: 'Built Different.png', id: 7 },
        { img: 'Devil.png', id: 10 },
        { img: 'Elevate.png', id: 11 },
        { img: 'Flowless.png', id: 2 },
        { img: 'Freeflow.png', id: 3 },
        { img: 'Metal.png', id: 9 },
        { img: 'Peace Love (2).png', id: 1 },
        { img: 'Peace Love.png', id: 1 },
        { img: 'Peace X Flowless.png', id: 5 },
        { img: 'Reality (1).png', id: 4 },
        { img: 'Reality (2).png', id: 4 },
        { img: 'Reality (4).png', id: 4 },
        { img: 'Reality.png', id: 4 },
        { img: 'Speed.png', id: 6 }
    ];

    let currentSlide = 0; // Indice della slide attualmente attiva

    /**
     * Inizializza la struttura del carosello inserendo le immagini e gli indicatori nel DOM.
     */
    function initCarousel() {
        const track = document.getElementById('carouselTrack');
        const indicators = document.getElementById('carouselIndicators');
        const prevBtn = document.getElementById('prevBtn');
        const nextBtn = document.getElementById('nextBtn');
        
        if (!track || !indicators) return;

        // Generazione dinamica delle slide e dei pallini indicatori
        collections.forEach((item, index) => {
            const slide = document.createElement('div');
            slide.className = 'carousel-slide';
            
            const itemImgPath = contextPath + '/images/Collezioni/' + item.img;
            slide.innerHTML = 
                '<a href="' + contextPath + '/CollezioniServlet#' + item.id + '">' +
                    '<img src="' + itemImgPath + '" alt="Collezione ' + escapeHtml(item.img) + '">' +
                '</a>';
                
            track.appendChild(slide);
            
            // Creazione del singolo indicatore di stato
            const indicator = document.createElement('button');
            indicator.className = 'indicator ' + (index === 0 ? 'active' : '');
            indicator.dataset.index = index;
            indicator.addEventListener('click', () => goToSlide(index));
            indicators.appendChild(indicator);
        });

        // Event listener per la navigazione manuale tramite pulsanti
        if (prevBtn) prevBtn.addEventListener('click', prevSlide);
        if (nextBtn) nextBtn.addEventListener('click', nextSlide);

        // Avvio dello scorrimento automatico ogni 5 secondi
        setInterval(nextSlide, 5000);
    }

    /**
     * Aggiorna la posizione della traccia del carosello e lo stato degli indicatori.
     */
    function updateCarousel() {
        const track = document.getElementById('carouselTrack');
        if (!track) return;
        
        // Calcola lo spostamento orizzontale in percentuale
        const offset = -currentSlide * 100;
        track.style.transform = 'translateX(' + offset + '%)';
        
        // Aggiorna la classe CSS attiva sui pallini indicatori
        document.querySelectorAll('.indicator').forEach((indicator, index) => {
            indicator.classList.toggle('active', index === currentSlide);
        });
    }

    /** Passa alla slide successiva (con riavvolgimento ad anello) */
    function nextSlide() {
        currentSlide = (currentSlide + 1) % collections.length;
        updateCarousel();
    }

    /** Passa alla slide precedente (con riavvolgimento ad anello) */
    function prevSlide() {
        currentSlide = (currentSlide - 1 + collections.length) % collections.length;
        updateCarousel();
    }

    /** Naviga direttamente a uno specifico indice di slide */
    function goToSlide(index) {
        currentSlide = index;
        updateCarousel();
    }

    <%-- ── LOGICA SCROLL PRODOTTI IN EVIDENZA (AJAX) ────────────────────────── --%>
    
    let currentProductScroll = 0;
    const CARD_WIDTH = 25;       // Larghezza percentuale della singola card
    const GAP = 1.6;             // Spaziatura percentuale tra le card
    const CARD_WITH_GAP = CARD_WIDTH + GAP;
    let totalProducts = 0;       // Conteggio totale dei prodotti caricati

    /**
     * Richiede i prodotti in evidenza tramite chiamata REST API / Fetch asincrona.
     */
    function loadFeaturedProducts() {
        fetch(contextPath + '/api/prodotti')
            .then(response => response.json())
            .then(prodotti => {
                const productsTrack = document.getElementById('productsTrack');
                if (!productsTrack) return;

                // Gestione caso in cui non ci siano prodotti restituiti dall'API
                if (!prodotti || prodotti.length === 0) {
                    productsTrack.innerHTML = '<p class="no-featured-msg">Nessun prodotto in evidenza al momento.</p>';
                    return;
                }

                totalProducts = prodotti.length;
                
                // Generazione del markup HTML per ogni card prodotto recuperata
                productsTrack.innerHTML = prodotti.map(p => {
                    const detailUrl = contextPath + '/DettaglioProdottoServlet?id=' + p.idProdotto;
                    const imgSrc = getImgSrc(p.immagine);
                    const defaultImg = contextPath + '/images/default.jpg';

                    return '<div class="product-card-scroll">' +
                        '<div class="product-image-scroll">' +
                            '<a href="' + detailUrl + '">' +
                                '<img src="' + imgSrc + '" alt="' + escapeHtml(p.nome) + '" onerror="this.onerror=null; this.src=\'' + defaultImg + '\';">' +
                            '</a>' +
                        '</div>' +
                        '<div class="product-info-scroll">' +
                            '<h3>' + escapeHtml(p.nome) + '</h3>' +
                            '<p class="price">' + formatCurrency(p.costo) + '</p>' +
                            '<a href="' + detailUrl + '" class="btn btn-small">' +
                                'Dettagli' +
                            '</a>' +
                        '</div>' +
                    '</div>';
                }).join('');

                // Inizializza i controlli di scorrimento dopo aver popolato il DOM
                initProductScrollControls();
            })
            .catch(err => {
                console.error('Errore nel recupero prodotti in evidenza:', err);
            });
    }

    /**
     * Configura gli eventi click sui pulsanti di scorrimento laterale dei prodotti.
     */
    function initProductScrollControls() {
        const prevBtn = document.getElementById('scrollPrevBtn');
        const nextBtn = document.getElementById('scrollNextBtn');

        if (prevBtn && nextBtn) {
            // Scorrimento indietro
            prevBtn.addEventListener('click', () => {
                if (currentProductScroll > 0) {
                    currentProductScroll--;
                    updateProductScroll();
                }
            });

            // Scorrimento avanti
            nextBtn.addEventListener('click', () => {
                const maxScroll = Math.max(0, totalProducts - 4);
                if (currentProductScroll < maxScroll) {
                    currentProductScroll++;
                    updateProductScroll();
                }
            });
        }
        updateProductScroll();
    }

    /**
     * Applica la trasformazione CSS per scorrere le card e abilita/disabilita i pulsanti.
     */
    function updateProductScroll() {
        const productsTrack = document.getElementById('productsTrack');
        if (!productsTrack) return;
        
        // Sposta la traccia in base all'indice corrente
        const offset = -currentProductScroll * CARD_WITH_GAP;
        productsTrack.style.transform = 'translateX(' + offset + '%)';
        
        const prevBtn = document.getElementById('scrollPrevBtn');
        const nextBtn = document.getElementById('scrollNextBtn');
        
        // Disabilita i pulsanti ai limiti dello scorrimento
        if (prevBtn) prevBtn.disabled = (currentProductScroll === 0);
        if (nextBtn) nextBtn.disabled = (currentProductScroll >= Math.max(0, totalProducts - 4));
    }

    // Inizializzazione di entrambi i moduli al caricamento completo della pagina
    initCarousel();
    loadFeaturedProducts();
});
</script>

<%-- Inclusione dello script per l'aggiornamento del badge carrello se l'utente è autenticato in sessione --%>
<c:if test="${not empty sessionScope.utente}">
    <script src="${pageContext.request.contextPath}/js/cart-badge.js"></script>
</c:if>

<%-- Inclusione del piè di pagina (footer) statico --%>
<%@ include file="/jsp/common/footer.jspf" %>