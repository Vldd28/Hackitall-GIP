package org.example.project.ui.components

@JsFun("""(lat, lng, zoom) => {
    window._wandrMapVisible = true;

    // Find Skiko's canvas
    var canvas = document.querySelector('canvas');
    if (canvas) {
        var r = canvas.getBoundingClientRect();
        console.log('[Wandr] canvas rect:', JSON.stringify(r));
        console.log('[Wandr] canvas attr size:', canvas.width, 'x', canvas.height);
        console.log('[Wandr] canvas offset size:', canvas.offsetWidth, 'x', canvas.offsetHeight);
        console.log('[Wandr] canvas transform:', window.getComputedStyle(canvas).transform);
    }

    // Create or reuse map overlay div
    var el = document.getElementById('wandr-map');
    if (!el) {
        el = document.createElement('div');
        el.id = 'wandr-map';
    }

    // Insert map div BEFORE the canvas inside my-app so they share stacking context
    var myApp = document.querySelector('.my-app') || document.querySelector('[class*="my-app"]') || document.body;
    if (canvas && canvas.parentNode) {
        canvas.parentNode.insertBefore(el, canvas);
    } else {
        myApp.appendChild(el);
    }

    el.style.position = 'absolute';
    el.style.top = '0px';
    el.style.left = '0px';
    el.style.width = window.innerWidth + 'px';
    el.style.height = window.innerHeight + 'px';
    el.style.zIndex = '0';
    el.style.display = 'block';

    // Clip the canvas — use its actual CSS height for the math
    if (canvas) {
        var navHeight = 100; // px to reveal at bottom for nav bar
        var cssH = canvas.offsetHeight || window.innerHeight;
        var clipTop = cssH - navHeight;
        console.log('[Wandr] clip-path inset top:', clipTop, 'of cssH:', cssH);
        canvas.style.setProperty('clip-path', 'inset(' + clipTop + 'px 0 0 0)', 'important');
        canvas.style.setProperty('z-index', '1', 'important');
        canvas.style.setProperty('position', 'relative', 'important');
    }

    requestAnimationFrame(function() {
        requestAnimationFrame(function() {
            if (window._wandrMap) {
                window._wandrMap.setView([lat, lng], zoom);
                window._wandrMap.invalidateSize();
            } else {
                window._wandrMap = L.map('wandr-map', { zoomControl: true }).setView([lat, lng], zoom);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    attribution: '&copy; OpenStreetMap contributors',
                    maxZoom: 19
                }).addTo(window._wandrMap);
                window._wandrMap.invalidateSize();
            }
        });
    });
}""")
external fun jsShowMap(lat: Double, lng: Double, zoom: Int)

@JsFun("""() => {
    window._wandrMapVisible = false;
    var el = document.getElementById('wandr-map');
    if (el) el.style.display = 'none';
    var canvas = document.querySelector('canvas');
    if (canvas) {
        canvas.style.removeProperty('clip-path');
        canvas.style.removeProperty('z-index');
        canvas.style.removeProperty('position');
    }
}""")
external fun jsHideMap()

@JsFun("""(lat, lng, title, location) => {
    if (!window._wandrMap) return;
    L.marker([lat, lng])
        .addTo(window._wandrMap)
        .bindPopup('<b>' + title + '</b><br>' + location);
}""")
external fun jsAddMarker(lat: Double, lng: Double, title: String, location: String)

@JsFun("""() => {
    if (!window._wandrMap) return;
    window._wandrMap.eachLayer(function(layer) {
        if (layer instanceof L.Marker) window._wandrMap.removeLayer(layer);
    });
}""")
external fun jsClearMarkers()
