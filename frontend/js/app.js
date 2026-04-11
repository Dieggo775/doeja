class DoeJaApp {
    constructor() {
        this.API_BASE = '/api';
        this.currentPage = 0;
        this.pageSize = 10;
        this.currentFilters = {};
        this.map = null;
        this.markers = [];
        this.init();
    }

    async init() {
        await this.loadIcons();
        this.setupEventListeners();
        this.initThemeToggle();
        await this.loadCentros();
        this.animateStats();
    }

    async loadIcons() {
        lucide.createIcons();
    }

    setupEventListeners() {
        document.getElementById('searchBtn').addEventListener('click', () => this.searchByLocation());
        document.getElementById('applyFilters').addEventListener('click', () => this.applyFilters());
        document.getElementById('clearFilters').addEventListener('click', () => this.clearFilters());
        
        // Detect geolocation
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                pos => this.centerMapOnUser(pos.coords),
                () => console.log('Geolocalização não permitida')
            );
        }
    }

    async loadCentros(page = 0, filters = {}) {
        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: this.pageSize.toString(),
                ...filters
            });
            
            const response = await fetch(`${this.API_BASE}/centros?${params}`);
            const pageData = await response.json();
            
            this.renderCentros(pageData.content);
            this.renderPagination(pageData);
            this.updateStats(pageData);
            this.centerMapOnCentros(pageData.content);
            
        } catch (error) {
            console.error('Erro ao carregar centros:', error);
        }
    }

    renderCentros(centros) {
        const grid = document.getElementById('centrosGrid');
        grid.innerHTML = centros.map(centro => `
            <article class="centro-card" data-lat="${centro.latitude}" data-lng="${centro.longitude}">
                <header class="centro-header">
                    <h4 class="centro-nome">${centro.nome}</h4>
                    <span class="centro-status status-ativo">${centro.ativo ? 'Ativo' : 'Inativo'}</span>
                </header>
                <div class="centro-info">
                    <div class="centro-info-item">
                        <i data-lucide="map-pin"></i>
                        <span>${centro.bairro} - ${centro.cidade}</span>
                    </div>
                    <div class="centro-info-item">
                        <i data-lucide="phone"></i>
                        <span>${centro.telefone}</span>
                    </div>
                    <div class="centro-info-item">
                        <i data-lucide="clock"></i>
                        <span>${centro.horarioFuncionamento}</span>
                    </div>
                </div>
                <div class="centro-actions">
                    <button class="btn-secondary" onclick="app.viewOnMap(this)">Ver no mapa</button>
                    <a href="tel:${centro.telefone.replace(/\D/g,'')}" class="btn-primary">Ligar</a>
                </div>
            </article>
        `).join('');
        
        lucide.createIcons();
    }

    renderPagination(pageData) {
        const pagination = document.getElementById('pagination');
        const totalPages = pageData.totalPages;
        let pagesHtml = '';
        
        for (let i = 0; i < totalPages; i++) {
            pagesHtml += `
                <button class="pagination-btn ${i === this.currentPage ? 'active' : ''}"
                        onclick="app.loadCentros(${i})">
                    ${i + 1}
                </button>
            `;
        }
        pagination.innerHTML = pagesHtml;
    }

    async applyFilters() {
        const cidade = document.getElementById('cidadeFilter').value;
        const bairro = document.getElementById('bairroFilter').value;
        const ativo = document.getElementById('ativoFilter').value;
        
        this.currentFilters = { cidade, bairro, ativo: ativo || null };
        await this.loadCentros(0, this.currentFilters);
    }

    clearFilters() {
        document.getElementById('cidadeFilter').value = '';
        document.getElementById('bairroFilter').value = '';
        document.getElementById('ativoFilter').value = '';
        this.currentFilters = {};
        this.loadCentros(0);
    }

    initThemeToggle() {
        const toggle = document.querySelector('[data-theme-toggle]');
        const html = document.documentElement;
        
        toggle.addEventListener('click', () => {
            const current = html.getAttribute('data-theme');
            const next = current === 'dark' ? 'light' : 'dark';
            html.setAttribute('data-theme', next);
            toggle.setAttribute('aria-label', `Alternar para ${next === 'dark' ? 'claro' : 'escuro'} modo`);
        });
    }

    animateStats() {
        const animate = (el, target, duration = 2000) => {
            let start = 0;
            const increment = target / (duration / 16);
            const timer = setInterval(() => {
                start += increment;
                if (start >= target) {
                    el.textContent = target.toLocaleString();
                    clearInterval(timer);
                } else {
                    el.textContent = Math.floor(start).toLocaleString();
                }
            }, 16);
        };

        document.querySelectorAll('.stat-number').forEach(stat => {
            const target = parseInt(stat.dataset.target);
            animate(stat, target);
        });
    }

    // Map functions
    initMap() {
        this.map = new google.maps.Map(document.getElementById('map'), {
            zoom: 13,
            center: { lat: -23.6261, lng: -46.7917 }, // Taboão da Serra
            styles: [
                { featureType: "poi", elementType: "labels", stylers: [{ visibility: "off" }] }
            ]
        });
    }

    centerMapOnUser(coords) {
        this.map.setCenter({ lat: coords.latitude, lng: coords.longitude });
        this.map.setZoom(14);
    }

    centerMapOnCentros(centros) {
        if (centros.length === 0) return;
        
        // Clear existing markers
        this.markers.forEach(marker => marker.setMap(null));
        this.markers = [];
        
        // Add markers
        centros.forEach(centro => {
            const marker = new google.maps.Marker({
                position: { lat: centro.latitude, lng: centro.longitude },
                map: this.map,
                title: centro.nome,
                icon: {
                    url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
                        <svg width="32" height="40" viewBox="0 0 32 40" xmlns="http://www.w3.org/2000/svg">
                            <path d="M16 0C7.2 0 0 7.2 0 16c0 6.4 4 11.2 16 20 12-8.8 16-13.6 16-20C32 7.2 24.8 0 16 0z" fill="%232E7D32"/>
                            <circle cx="16" cy="16" r="8" fill="white"/>
                            <circle cx="16" cy="16" r="4" fill="%232E7D32"/>
                        </svg>
                    `),
                    scaledSize: new google.maps.Size(32, 40)
                }
            });
            
            this.markers.push(marker);
            
            // Info window
            const infoWindow = new google.maps.InfoWindow({
                content: `
                    <div style="padding: 8px; max-width: 200px;">
                        <h4 style="margin: 0 0 4px 0; font-size: 14px;">${centro.nome}</h4>
                        <p style="margin: 0 0 4px 0; font-size: 12px; color: #666;">${centro.bairro}</p>
                        <a href="tel:${centro.telefone}" style="color: #2E7D32; text-decoration: none; font-size: 12px;">📞 ${centro.telefone}</a>
                    </div>
                `
            });
            
            marker.addListener('click', () => infoWindow.open(this.map, marker));
        });
        
        // Fit bounds
        if (centros.length > 1) {
            const bounds = new google.maps.LatLngBounds();
            centros.forEach(centro => {
                bounds.extend({ lat: centro.latitude, lng: centro.longitude });
            });
            this.map.fitBounds(bounds);
        }
    }

    viewOnMap(btn) {
        const card = btn.closest('.centro-card');
        const lat = parseFloat(card.dataset.lat);
        const lng = parseFloat(card.dataset.lng);
        
        this.map.setCenter({ lat, lng });
        this.map.setZoom(16);
    }

    async searchByLocation() {
        const query = document.getElementById('searchLocation').value;
        if (!query) return;
        
        try {
            // Geocode via Google Maps
            const geocoder = new google.maps.Geocoder();
            const results = await new Promise(resolve => {
                geocoder.geocode({ address: query + ', Taboão da Serra, SP' }, resolve);
            });
            
            if (results[0]) {
                const location = results[0].geometry.location;
                this.map.setCenter(location);
                this.map.setZoom(14);
                
                // Load centros próximos
                await this.loadCentros(0, { ...this.currentFilters });
            }
        } catch (error) {
            console.error('Erro na busca:', error);
        }
    }
}

// Inicializar app
const app = new DoeJaApp();

// Google Maps callback
function initMap() {
    app.initMap();
}