document.addEventListener("DOMContentLoaded", function () {
    const input = document.getElementById("searchInput");
    const container = document.getElementById("searchSuggestions");

    if (!input || !container) {
        console.warn("[search.js] input o container non trovati");
        return;
    }

    const contextPath = input.dataset.contextPath || "";
    console.log("[search.js] contextPath:", contextPath);

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
            div.textContent = `${item.nome} — €${Number(item.prezzo).toFixed(2)}`;
            div.addEventListener("click", () => {
                window.location.href = `${contextPath}/DettaglioProdottoServlet?id=${item.id}`;
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
            const url = `${contextPath}/RicercaAjaxServlet?query=${encodeURIComponent(query)}`;
            console.log("[search.js] fetch:", url);
            fetch(url)
                .then(resp => {
                    console.log("[search.js] status:", resp.status);
                    return resp.json();
                })
                .then(data => {
                    console.log("[search.js] risultati:", data);
                    render(data);
                })
                .catch(err => {
                    console.error("[search.js] errore:", err);
                    render([]);
                });
        }, 250);
    });

    document.addEventListener("click", function (event) {
        if (!container.contains(event.target) && event.target !== input) {
            container.style.display = "none";
        }
    });
});
