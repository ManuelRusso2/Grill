document.addEventListener("DOMContentLoaded", function () {
    const input = document.getElementById("searchInput");
    const container = document.getElementById("searchSuggestions");

    if (!input || !container) return;

    let timeoutId;

    function render(items) {
        container.innerHTML = "";
        if (!items.length) {
            container.style.display = "none";
            return;
        }

        items.forEach(item => {
            const div = document.createElement("div");
            div.className = "suggestion-item";
            div.textContent = `${item.nome} - €${Number(item.prezzo).toFixed(2)}`;
            div.addEventListener("click", () => {
                window.location.href = `DettaglioProdottoServlet?id=${item.id}`;
            });
            container.appendChild(div);
        });
        container.style.display = "block";
    }

    input.addEventListener("input", function () {
        const query = input.value.trim();
        clearTimeout(timeoutId);

        if (query.length < 2) {
            render([]);
            return;
        }

        timeoutId = setTimeout(() => {
            fetch(`RicercaAjaxServlet?query=${encodeURIComponent(query)}`)
                .then(resp => resp.json())
                .then(render)
                .catch(() => render([]));
        }, 250);
    });

    document.addEventListener("click", function (event) {
        if (!container.contains(event.target) && event.target !== input) {
            container.style.display = "none";
        }
    });
});