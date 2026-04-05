package org.example.project.ui.components

// ── Map lifecycle ─────────────────────────────────────────────────────────────

@JsFun("""(lat, lng, zoom) => {
    var NAV_H = 110;

    // ── 1. Map overlay div ───────────────────────────────────────────────────
    var el = document.getElementById('wandr-map');
    if (!el) {
        el = document.createElement('div');
        el.id = 'wandr-map';
        document.documentElement.appendChild(el);
    }
    el.style.cssText = [
        'position:fixed','top:0','left:0',
        'width:100vw','height:100vh',
        'z-index:9000',          /* always visible; canvas clips above it */
        'display:block'
    ].join(';');

    // ── 2. Compose canvas — clipped to nav-bar strip only ────────────────────
    var canvas = document.querySelector('canvas');
    if (canvas) {
        canvas.style.setProperty('will-change', 'auto', 'important');
        canvas.style.setProperty('z-index', '1', 'important');
        canvas.style.setProperty('position', 'relative', 'important');
        // Show only the bottom NAV_H pixels (nav bar)
        canvas.style.setProperty('clip-path',
            'inset(' + (window.innerHeight - NAV_H) + 'px 0 0 0)', 'important');
    }

    // ── 3. Init Leaflet ───────────────────────────────────────────────────────
    requestAnimationFrame(function() {
        requestAnimationFrame(function() {
            if (window._wandrMap) {
                window._wandrMap.setView([lat, lng], zoom);
                window._wandrMap.invalidateSize();
            } else {
                window._wandrMap = L.map('wandr-map', {
                    zoomControl: true, attributionControl: true
                }).setView([lat, lng], zoom);

                // CartoDB Dark Matter base
                L.tileLayer(
                    'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png',
                    { attribution: '\u00a9 OpenStreetMap contributors \u00a9 CARTO',
                      subdomains: 'abcd', maxZoom: 20 }
                ).addTo(window._wandrMap);

                window._wandrMap.invalidateSize();

                // Apply blue tint to tiles only (not markers/labels)
                // sepia gives colour base, hue-rotate shifts to blue family
                setTimeout(function() {
                    var tp = el.querySelector('.leaflet-tile-pane');
                    if (tp) tp.style.filter =
                        'sepia(1) hue-rotate(190deg) saturate(2.2) brightness(0.6)';
                    var topLeft = el.querySelector('.leaflet-top.leaflet-left');
                    if (topLeft) {
                        topLeft.style.left = '50%';
                        topLeft.style.transform = 'translateX(-50%)';
                        topLeft.style.right = 'auto';
                    }
                }, 600);
            }
        });
    });
}""")
external fun jsShowMap(lat: Double, lng: Double, zoom: Int)

@JsFun("""() => {
    var el = document.getElementById('wandr-map');
    if (el) el.style.display = 'none';
    var canvas = document.querySelector('canvas');
    if (canvas) {
        canvas.style.removeProperty('will-change');
        canvas.style.removeProperty('z-index');
        canvas.style.removeProperty('position');
        canvas.style.removeProperty('clip-path');
    }
}""")
external fun jsHideMap()

// ── Bottom-sheet visibility ───────────────────────────────────────────────────
// When a sheet opens: remove the canvas clip so the full canvas is usable.
// The map stays at z-index 9000 and shows through the transparent Compose areas
// (Skiko uses clearRect so unpainted regions are truly transparent).
// The sheet itself has an opaque background so it covers the map in its area.

@JsFun("""() => {
    var canvas = document.querySelector('canvas');
    if (canvas) {
        canvas.style.removeProperty('clip-path');          // full canvas
        canvas.style.setProperty('z-index', '9999', 'important'); // above map
    }
    // Map stays at z-index 9000 — visible through transparent canvas areas
}""")
external fun jsShowBottomSheet()

@JsFun("""() => {
    var NAV_H = 110;
    var canvas = document.querySelector('canvas');
    if (canvas) {
        canvas.style.setProperty('clip-path',
            'inset(' + (window.innerHeight - NAV_H) + 'px 0 0 0)', 'important');
        canvas.style.setProperty('z-index', '1', 'important');
    }
    // Map stays unchanged at z-index 9000
}""")
external fun jsRestoreMapLayout()

// ── Zoom / centre queries ─────────────────────────────────────────────────────

@JsFun("() => window._wandrMap ? window._wandrMap.getZoom() : 12.0")
external fun jsGetZoom(): Double

@JsFun("() => window._wandrMap ? window._wandrMap.getCenter().lat : 44.4268")
external fun jsGetCenterLat(): Double

@JsFun("() => window._wandrMap ? window._wandrMap.getCenter().lng : 26.1025")
external fun jsGetCenterLng(): Double

// ── Event markers ─────────────────────────────────────────────────────────────

@JsFun("""(lat, lng, title, location, eventId) => {
    if (!window._wandrMap) return;
    window._wandrEventMarkers = window._wandrEventMarkers || [];
    var color = '#76ABAE';
    var html = '<div style="width:36px;height:48px;display:flex;flex-direction:column;align-items:center">'
             + '<div style="width:36px;height:36px;border-radius:50%;background:' + color
             + ';display:flex;align-items:center;justify-content:center;font-size:18px'
             + ';box-shadow:0 2px 6px rgba(0,0,0,0.7)">📍</div>'
             + '<div style="width:0;height:0;border-left:9px solid transparent'
             + ';border-right:9px solid transparent;border-top:12px solid ' + color + '"></div>'
             + '</div>';
    var icon = L.divIcon({ html: html, className: '',
        iconSize: [36,48], iconAnchor: [18,48], popupAnchor: [0,-48] });
    var marker = L.marker([lat, lng], { icon: icon })
        .addTo(window._wandrMap)
        .bindPopup('<b>' + title + '</b><br><span style="color:#aaa;font-size:12px">' + location + '</span>');
    marker.on('click', function(e) {
        L.DomEvent.stopPropagation(e);
        window._wandrPendingEventId = eventId;
    });
    window._wandrEventMarkers.push(marker);
}""")
external fun jsAddEventMarker(lat: Double, lng: Double, title: String, location: String, eventId: String)

@JsFun("""() => {
    if (!window._wandrMap) return;
    (window._wandrEventMarkers || []).forEach(function(m) { window._wandrMap.removeLayer(m); });
    window._wandrEventMarkers = [];
}""")
external fun jsClearEventMarkers()

// ── Place markers ─────────────────────────────────────────────────────────────

@JsFun("""(lat, lng, emoji, color, name, address, placeId, eventCount) => {
    if (!window._wandrMap) return;
    window._wandrPlaceMarkers = window._wandrPlaceMarkers || [];
    var badge = '';
    if (eventCount === 1) {
        badge = '<div style="position:absolute;top:-6px;right:-8px;width:22px;height:22px;border-radius:50%;'
              + 'background:#3D7A7D;box-shadow:0 2px 5px rgba(0,0,0,.5);border:2px solid #1a1a1a;'
              + 'display:flex;align-items:center;justify-content:center">'
              + '<span style="font-size:12px">📌</span></div>';
    } else if (eventCount > 1) {
        badge = '<div style="position:absolute;top:-6px;right:-14px;min-width:28px;height:22px;border-radius:11px;'
              + 'background:#3D7A7D;box-shadow:0 2px 5px rgba(0,0,0,.5);border:2px solid #1a1a1a;'
              + 'display:flex;align-items:center;justify-content:center;padding:0 3px">'
              + '<span style="font-size:12px">📌</span>'
              + '<span style="font-size:12px;color:#fff;font-weight:bold">+</span></div>';
    }
    var html = '<div style="position:relative;width:36px;height:48px;overflow:visible">'
             + '<div style="display:flex;flex-direction:column;align-items:center">'
             + '<div style="width:36px;height:36px;border-radius:50%;background:' + color
             + ';display:flex;align-items:center;justify-content:center;font-size:16px'
             + ';box-shadow:0 2px 6px rgba(0,0,0,0.7)">' + emoji + '</div>'
             + '<div style="width:0;height:0;border-left:9px solid transparent'
             + ';border-right:9px solid transparent;border-top:12px solid ' + color + '"></div>'
             + '</div>'
             + badge
             + '</div>';
    var icon = L.divIcon({ html: html, className: '',
        iconSize: [36,48], iconAnchor: [18,48], popupAnchor: [0,-48] });
    var marker = L.marker([lat, lng], { icon: icon })
        .addTo(window._wandrMap)
        .bindPopup('<b>' + name + '</b><br><span style="color:#aaa;font-size:12px">' + address + '</span>');
    marker.on('click', function(e) {
        L.DomEvent.stopPropagation(e);
        window._wandrPendingPlaceId = placeId;
    });
    window._wandrPlaceMarkers.push(marker);
}""")
external fun jsAddPlaceMarker(lat: Double, lng: Double, emoji: String, color: String, name: String, address: String, placeId: String, eventCount: Int)

@JsFun("""() => {
    if (!window._wandrMap) return;
    (window._wandrPlaceMarkers || []).forEach(function(m) { window._wandrMap.removeLayer(m); });
    window._wandrPlaceMarkers = [];
}""")
external fun jsClearPlaceMarkers()

// ── Pan / fly to location ────────────────────────────────────────────────────

@JsFun("""(lat, lng, zoom) => {
    function tryFly() {
        if (window._wandrMap) {
            window._wandrMap.flyTo([lat, lng], zoom, {duration: 0.8});
        } else {
            setTimeout(tryFly, 100);
        }
    }
    tryFly();
}""")
external fun jsPanTo(lat: Double, lng: Double, zoom: Int)

// ── Click polling ─────────────────────────────────────────────────────────────

@JsFun("""() => {
    var id = window._wandrPendingEventId; window._wandrPendingEventId = null; return id || null;
}""")
external fun jsPollEventClickId(): JsString?

@JsFun("""() => {
    var id = window._wandrPendingPlaceId; window._wandrPendingPlaceId = null; return id || null;
}""")
external fun jsPollPlaceClickId(): JsString?
