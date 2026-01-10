(function () {
    async function init(select) {
        const refName = select.dataset.jpRef || "";
        const paramName = select.dataset.jpParam || "";
        const url = select.dataset.jpUrl || "";

        const crumbField = select.dataset.jpCrumbField || "";
        const crumbValue = select.dataset.jpCrumbValue || "";

        const parameters = document.getElementsByName("parameter");
        let refValue = null;

        parameters.forEach(parameter => {
            const nameInput = parameter.querySelector('input[name="name"]');
            if (nameInput && nameInput.value === refName) {
                refValue = parameter.querySelector('[name="value"]');
            }
        });

        if (!refValue || !url) return;

        const refresh = async () => {
            const params = new URLSearchParams();
            params.set("paramName", paramName);
            params.set("refValue", refValue.value || "");

            const headers = { "Content-Type": "application/x-www-form-urlencoded" };
            if (crumbField && crumbValue) headers[crumbField] = crumbValue;

            const response = await fetch(url, {
                method: "POST",
                credentials: "same-origin",
                headers,
                body: params.toString()
            });

            const json = await response.json();
            const items = json.data;

            select.innerHTML = "";
            (items || []).forEach(item => {
                const value = item.value;
                if (value == null) return;
                const opt = document.createElement("option");
                opt.value = value;
                opt.textContent = item.name ?? String(value);
                if (item.selected) opt.selected = true;
                select.appendChild(opt);
            });

            if (!select.value && select.options.length) select.selectedIndex = 0;
            select.dispatchEvent(new Event("change", { bubbles: true }));
        };

        function debounce(fn, delay) {
            let timer = null;
            return function (...args) {
                clearTimeout(timer);
                timer = setTimeout(() => fn.apply(this, args), delay);
            };
        }

        await refresh();

        if (refValue.tagName === "SELECT") {
            refValue.addEventListener("change", refresh);
        } else if (refValue.tagName === "INPUT" || refValue.tagName === "TEXTAREA") {
            refValue.addEventListener("input", debounce(refresh, 300));
        }
    }

    function boot() {
        document
            .querySelectorAll('select[data-jp-url][data-jp-ref]')
            .forEach(sel => init(sel));
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", boot);
    } else {
        boot();
    }
})();
