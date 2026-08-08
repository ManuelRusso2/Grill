<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ include file="/jsp/common/header.jspf" %>
<%@ include file="/jsp/common/menu.jspf" %>

<main>
    <!-- SEZIONE 1: CAROUSEL DI COLLEZIONI -->
    <section class="carousel-section">
        <div class="carousel-container">
            <button class="carousel-btn carousel-btn-prev" id="prevBtn">&#10094;</button>
            
            <div class="carousel-wrapper">
                <div class="carousel-track" id="carouselTrack">
                    <!-- Le immagini verranno caricate via JavaScript -->
                </div>
            </div>
            
            <button class="carousel-btn carousel-btn-next" id="nextBtn">&#10095;</button>
        </div>
        <div class="carousel-indicators" id="carouselIndicators"></div>
    </section>

    <!-- SEZIONE 2: PRODOTTI CASUALI SCORREVOLI -->
    <section class="products-scroll-section">
        <h2>Prodotti in Evidenza</h2>
        
        <div class="products-scroll-container">
            <button class="scroll-btn scroll-btn-prev" id="scrollPrevBtn">&#10094;</button>
            
            <div class="products-scroll-wrapper">
                <div class="products-scroll-track" id="productsTrack">
                    
                    <!-- I prodotti vengono stampati direttamente da JSP -->
                    <c:choose>
                        <c:when test="${not empty prodotti}">
                            <c:forEach var="prodotto" items="${prodotti}">
                                <div class="product-card-scroll">
                                    <div class="product-image-scroll">
                                        <img src="${pageContext.request.contextPath}/${prodotto.immagine}" alt="${prodotto.nome}">
                                    </div>
                                    <div class="product-info-scroll">
                                        <h3>${prodotto.nome}</h3>
                                        <p class="price">€<fmt:formatNumber value="${prodotto.costo}" pattern="#,##0.00" /></p>
                                        <a href="${pageContext.request.contextPath}/DettaglioProdottoServlet?id=${prodotto.idProdotto}" class="btn btn-small">
                                            Dettagli
                                        </a>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <p style="text-align: center; width: 100%; color: var(--text-gray);">Nessun prodotto in evidenza al momento.</p>
                        </c:otherwise>
                    </c:choose>

                </div>
            </div>
            
            <button class="scroll-btn scroll-btn-next" id="scrollNextBtn">&#10095;</button>
        </div>
    </section>
</main>

<%@ include file="/jsp/common/footer.jspf" %>

<script>
    // Dati delle collezioni
    const collections = [
        'Bloom.png',
        'Built Different.png',
        'Devil.png',
        'Elevate.png',
        'Flowless.png',
        'Freeflow.png',
        'Metal.png',
        'Peace Love (2).png',
        'Peace Love.png',
        'Peace X Flowless.png',
        'Reality (1).png',
        'Reality (2).png',
        'Reality (4).png',
        'Reality.png',
        'Speed.png'
    ];

    // Questo $ serve a JSP per stampare il contextPath in JS (nessun escape necessario)
    const contextPath = '${pageContext.request.contextPath}';
    let currentSlide = 0;

    // --- Logica Carousel ---
    function initCarousel() {
        const track = document.getElementById('carouselTrack');
        const indicators = document.getElementById('carouselIndicators');
        
        collections.forEach((img, index) => {
            const slide = document.createElement('div');
            slide.className = 'carousel-slide';
            // ATTENZIONE: i $ del template JS hanno il \ davanti per non farli rompere da Tomcat
            slide.innerHTML = `<img src="\${contextPath}/images/Collezioni/\${img}" alt="\${img}">`;
            track.appendChild(slide);
            
            const indicator = document.createElement('button');
            indicator.className = `indicator \${index == 0 ? 'active' : ''}`;
            indicator.dataset.index = index;
            indicator.addEventListener('click', () => goToSlide(index));
            indicators.appendChild(indicator);
        });

        document.getElementById('prevBtn').addEventListener('click', () => prevSlide());
        document.getElementById('nextBtn').addEventListener('click', () => nextSlide());
    }

    function updateCarousel() {
        const track = document.getElementById('carouselTrack');
        const offset = -currentSlide * 100;
        track.style.transform = `translateX(\${offset}%)`;
        
        document.querySelectorAll('.indicator').forEach((indicator, index) => {
            indicator.classList.toggle('active', index == currentSlide);
        });
    }

    function nextSlide() {
        currentSlide = (currentSlide + 1) % collections.length;
        updateCarousel();
    }

    function prevSlide() {
        currentSlide = (currentSlide - 1 + collections.length) % collections.length;
        updateCarousel();
    }

    function goToSlide(index) {
        currentSlide = index;
        updateCarousel();
    }

    // Auto-scroll del carousel ogni 5 secondi
    setInterval(nextSlide, 5000);

    // --- Logica Scroll Prodotti (adattata per HTML statico) ---
    let currentProductScroll = 0;
    const CARD_WIDTH = 25; // percentuale larghezza card nel CSS
    const GAP = 1.6; // percentuale margin/gap nel CSS
    const CARD_WITH_GAP = CARD_WIDTH + GAP;
    
    // Contiamo quanti prodotti ha stampato JSP
    const productsTrack = document.getElementById('productsTrack');
    const totalProducts = productsTrack ? document.querySelectorAll('.product-card-scroll').length : 0;
    
    document.getElementById('scrollPrevBtn').addEventListener('click', () => {
        if (currentProductScroll > 0) {
            currentProductScroll--;
            updateProductScroll();
        }
    });

    document.getElementById('scrollNextBtn').addEventListener('click', () => {
        // Assume di mostrare 4 prodotti per volta su schermo
        const maxScroll = Math.max(0, totalProducts - 4);
        if (currentProductScroll < maxScroll) {
            currentProductScroll++;
            updateProductScroll();
        }
    });

    function updateProductScroll() {
        if (!productsTrack) return;
        const offset = -currentProductScroll * CARD_WITH_GAP;
        productsTrack.style.transform = `translateX(\${offset}%)`;
        
        // Aggiorna stato disabilitato bottoni ai limiti
        const prevBtn = document.getElementById('scrollPrevBtn');
        const nextBtn = document.getElementById('scrollNextBtn');
        
        if(prevBtn) prevBtn.disabled = currentProductScroll == 0;
        if(nextBtn) nextBtn.disabled = currentProductScroll >= Math.max(0, totalProducts - 4);
    }

    // Inizializza la pagina al caricamento
    document.addEventListener('DOMContentLoaded', () => {
        initCarousel();
        // Le immagini dei prodotti sono già state caricate dal server (JSP),
        // aggiorno solo lo stato dei bottoni scroll
        if(totalProducts > 0) {
            updateProductScroll();
        }
    });
</script>