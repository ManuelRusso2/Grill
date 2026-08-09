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

    const contextPath = '${pageContext.request.contextPath}';
    let currentSlide = 0;

    // --- Logica Carousel ---
    function initCarousel() {
        const track = document.getElementById('carouselTrack');
        const indicators = document.getElementById('carouselIndicators');
        
        if (!track || !indicators) return;

        collections.forEach((img, index) => {
            const slide = document.createElement('div');
            slide.className = 'carousel-slide';
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
        if (!track) return;
        const offset = -currentSlide * 100;
        track.style.transform = `translateX(\${offset}%)`;
        
        document.querySelectorAll('.indicator').forEach((indicator, index) => {
            indicator.classList.toggle('active', index == currentSlide);
        });
    }

    setInterval(nextSlide, 5000);

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

    // --- Logica Scroll Prodotti ---
    let currentProductScroll = 0;
    const CARD_WIDTH = 25; 
    const GAP = 1.6; 
    const CARD_WITH_GAP = CARD_WIDTH + GAP;
    
    const productsTrack = document.getElementById('productsTrack');
    const totalProducts = productsTrack ? document.querySelectorAll('.product-card-scroll').length : 0;
    
    if (productsTrack) {
        document.getElementById('scrollPrevBtn').addEventListener('click', () => {
            if (currentProductScroll > 0) {
                currentProductScroll--;
                updateProductScroll();
            }
        });

        document.getElementById('scrollNextBtn').addEventListener('click', () => {
            const maxScroll = Math.max(0, totalProducts - 4);
            if (currentProductScroll < maxScroll) {
                currentProductScroll++;
                updateProductScroll();
            }
        });
    }

    function updateProductScroll() {
        if (!productsTrack) return;
        const offset = -currentProductScroll * CARD_WITH_GAP;
        productsTrack.style.transform = `translateX(\${offset}%)`;
        
        const prevBtn = document.getElementById('scrollPrevBtn');
        const nextBtn = document.getElementById('scrollNextBtn');
        
        if(prevBtn) prevBtn.disabled = currentProductScroll == 0;
        if(nextBtn) nextBtn.disabled = currentProductScroll >= Math.max(0, totalProducts - 4);
    }

    document.addEventListener('DOMContentLoaded', () => {
        initCarousel();
        if(totalProducts > 0) {
            updateProductScroll();
        }
    });
</script>

<%-- Carica lo script globale delle altre pagine per mantenere sincronizzati click ed eventi futuri --%>
<c:if test="${not empty sessionScope.utente}">
    <script src="${pageContext.request.contextPath}/js/cart-badge.js"></script>
</c:if>

<%@ include file="/jsp/common/footer.jspf" %>