document.addEventListener('DOMContentLoaded', function () {
    function setBadge(count) {
        const cartCountSpan = document.getElementById('cart-count');
        if (!cartCountSpan) return;
        const text = (count && Number(count) > 0) ? `(${Number(count)})` : '';
        cartCountSpan.textContent = text;
    }

    // Listen for custom events dispatched by pages which modify the cart
    window.addEventListener('cartUpdated', function (e) {
        try {
            const count = e && e.detail && (e.detail.count || e.detail.cartCount);
            setBadge(count);
        } catch (err) {
            console.error('cartUpdated event handling error', err);
        }
    });

    // Optional: expose a global helper
    window.updateCartBadge = function (count) {
        setBadge(count);
    };

    // If server-side rendered value exists as a JS variable on window, use it
    if (window.__initialCartCount !== undefined) {
        setBadge(window.__initialCartCount);
    }
});
