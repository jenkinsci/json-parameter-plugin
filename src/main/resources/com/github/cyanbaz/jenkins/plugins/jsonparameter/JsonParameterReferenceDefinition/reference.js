(function () {
    const refName = '${it.ref}';
    const query   = '${it.query}';
    let refSelect = null;
    //var mySelect  = document.querySelector('select[name="value.${it.name}"]');
    console.log(refName)
    const refInput = document.querySelector('input[name="name"]');
    if (refInput && refInput.value === refName) {
        refSelect = document.querySelector('select[name="value"]');
    }

    if (!refSelect) return;

    function refresh() {
        const refValue = refSelect.value;
        console.log(refValue);
        console.log("${rootURL}/${it.descriptor.clazz.name}")

        const url = "${rootURL}/${it.descriptor.clazz.name}/loadOptions"
            + "?refName=" + encodeURIComponent(refName)
            + "&refValue=" + encodeURIComponent(refSelect.value || '')
            + "&query=" + encodeURIComponent(query || '');
        fetch(url, { credentials: 'same-origin' })
            .then(r => r.json())
            .then(list => {
                //mySelect.innerHTML = "";
                list.forEach(v => {
                    const opt = document.createElement("option");
                    opt.value = v;
                    opt.textContent = v;
                    console.log(opt);
                    console.log(v)
                    //mySelect.appendChild(opt);
                });
            });
    }

    refresh();
    refSelect.addEventListener('change', refresh);
})();