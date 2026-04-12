class DoeJaApp {
    constructor() {
        this.API_BASE = '/api';
        this.currentPage = 0;
        this.pageSize = 10;
        this.currentFilters = {};
        this.map = null;
        this.markers = [];
        this.centrosAtuais = [];
        this.init();
    }

    async init() {
        await this.loadIcons();
        this.setupEventListeners();
        this.initThemeToggle();
        await this.loadCentros();
    }

    async loadIcons() {
        if (window.lucide) {
            lucide.createIcons();
        }
    }

    setupEventListeners() {
        document.getElementById('searchBtn')?.addEventListener('click', () => this.searchByLocation());
        document.getElementById('applyFilters')?.addEventListener('click', () => this.applyFilters());
        document.getElementById('clearFilters')?.addEventListener('click', () => this.clearFilters());

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                pos => {
                    if (this.map) {
                        this.centerMapOnUser(pos.coords);
                    }
                },
                () => console.log('Geolocalização não permitida')
            );
        }
    }

    normalizeCentro(centro) {
        return {
            id: centro.id ?? null,
            nome: centro.nome ?? 'Centro sem nome',
            endereco: centro.endereco ?? 'Endereço não informado',
            telefone: centro.telefone ?? 'Telefone não informado',
            horarioFuncionamento:
                centro.horarioFuncionamento ??
                centro.horario_funcionamento ??
                centro.horario ??
                'Horário não informado',
            cidade: centro.cidade ?? this.extractCidade(centro.endereco),
            bairro: centro.bairro ?? this.extractBairro(centro.endereco),
            ativo: centro.ativo ?? true,
            latitude: this.toNumber(centro.latitude),
            longitude: this.toNumber(centro.longitude)
        };
    }

    toNumber(value) {
        const n = Number(value);
        return Number.isFinite(n) ? n : null;
    }

    extractCidade(endereco) {
        if (!endereco) return 'Cidade não informada';
        const partes = endereco.split(' - ');
        return partes[partes.length - 1] || 'Cidade não informada';
    }

    extractBairro(endereco) {
        if (!endereco) return 'Bairro não informado';
        const partes = endereco.split(' - ');
        return partes.length >= 2 ? partes[1] : 'Bairro não informado';
    }

    async loadCentros(page = 0, filters = {}) {
        try {
            this.currentPage = page;

            const cleanFilters = Object.fromEntries(
                Object.entries(filters).filter(([_, value]) => value !== '' && value !== null && value !== undefined)
            );

            const params = new URLSearchParams({
                page: page.toString(),
                size: this.pageSize.toString(),
                ...cleanFilters
            });

            const response = await fetch(`${this.API_BASE}/centros?${params.toString()}`);

            if (!response.ok) {
                throw new Error(`Erro HTTP ${response.status}`);
            }

            const pageData = await response.json();
            const centros = (pageData.content || []).map(centro => this.normalizeCentro(centro));

            this.centrosAtuais = centros;

            this.renderCentros(centros);
            this.renderPagination(pageData);
            this.updateStats(pageData, centros);
            this.centerMapOnCentros(centros);
            this.populateDynamicFilters(centros);

        } catch (error) {
            console.error('Erro ao carregar centros:', error);
            this.renderErrorState();
        }
    }

    renderCentros(centros) {
        const grid = document.getElementById('centrosGrid');

        if (!grid) return;

        if (!centros.length) {
            grid.innerHTML = `
                <article class="centro-card">
                    <header class="centro-header">
                        <h4 class="centro-nome">Nenhum centro encontrado</h4>
                    </header>
                    <div class="centro-info">
                        <div class="centro-info-item">
                            <span>Tente alterar os filtros ou verificar se existem dados cadastrados.</span>
                        </div>
                    </div>
                </article>
            `;
            return;
        }

        grid.innerHTML = centros.map(centro => `
            <article class="centro-card" data-lat="${centro.latitude ?? ''}" data-lng="${centro.longitude ?? ''}">
                <header class="centro-header">
                    <div>
                        <h4 class="centro-nome">${centro.nome}</h4>
                        <span class="centro-status ${centro.ativo ? 'status-ativo' : ''}">
                            ${centro.ativo ? 'Ativo' : 'Inativo'}
                        </span>
                    </div>
                </header>

                <div class="centro-info">
                    <div class="centro-info-item">
                        <i data-lucide="map-pin"></i>
                        <span>${centro.endereco}</span>
                    </div>
                    <div class="centro-info-item">
                        <i data-lucide="building-2"></i>
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
                    <button class="pagination-btn" onclick="app.viewOnMap(this)">Ver no mapa</button>
                    <a href="tel:${String(centro.telefone).replace(/\D/g, '')}" class="pagination-btn">Ligar</a>
                </div>
            </article>
        `).join('');

        if (window.lucide) {
            lucide.createIcons();
        }
    }

    renderPagination(pageData) {
        const pagination = document.getElementById('pagination');
        if (!pagination) return;

        const totalPages = pageData.totalPages ?? 0;

        if (totalPages <= 1) {
            pagination.innerHTML = '';
            return;
        }

        let pagesHtml = '';

        for (let i = 0; i < totalPages; i++) {
            pagesHtml += `
                <button class="pagination-btn ${i === this.currentPage ? 'active' : ''}"
                        onclick="app.loadCentros(${i}, app.currentFilters)">
                    ${i + 1}
                </button>
            `;
        }

        pagination.innerHTML = pagesHtml;
    }

    updateStats(pageData, centros) {
        const stats = document.querySelectorAll('.stat-number');
        if (!stats.length) return;

        const ativos = centros.filter(c => c.ativo).length;
        const total = pageData.totalElements ?? centros.length;

        if (stats[0]) stats[0].dataset.target = ativos;
        if (stats[1]) stats[1].dataset.target = total;
        if (stats[2] && !stats[2].dataset.fixed) stats[2].dataset.target = total * 10;

        this.animateStats();
    }

    animateStats() {
        const animate = (el, target, duration = 1200) => {
            let start = 0;
            const increment = target / (duration / 16);

            const timer = setInterval(() => {
                start += increment;

                if (start >= target) {
                    el.textContent = Number(target).toLocaleString('pt-BR');
                    clearInterval(timer);
                } else {
                    el.textContent = Math.floor(start).toLocaleString('pt-BR');
                }
            }, 16);
        };

        document.querySelectorAll('.stat-number').forEach(stat => {
            const target = parseInt(stat.dataset.target || '0', 10);
            stat.textContent = '0';
            animate(stat, target);
        });
    }

    populateDynamicFilters(centros) {
        const cidadeFilter = document.getElementById('cidadeFilter');
        const bairroFilter = document.getElementById('bairroFilter');

        if (!cidadeFilter || !bairroFilter) return;

        const cidades = [...new Set(centros.map(c => c.cidade).filter(Boolean))].sort();
        const bairros = [...new Set(centros.map(c => c.bairro).filter(Boolean))].sort();

        const cidadeAtual = cidadeFilter.value;
        const bairroAtual = bairroFilter.value;

        cidadeFilter.innerHTML = `<option value="">Todas as cidades</option>` +
            cidades.map(cidade => `<option value="${cidade}">${cidade}</option>`).join('');

        bairroFilter.innerHTML = `<option value="">Todos os bairros</option>` +
            bairros.map(bairro => `<option value="${bairro}">${bairro}</option>`).join('');

        cidadeFilter.value = cidades.includes(cidadeAtual) ? cidadeAtual : '';
        bairroFilter.value = bairros.includes(bairroAtual) ? bairroAtual : '';
    }

    async applyFilters() {
        const cidade = document.getElementById('cidadeFilter')?.value || '';
        const bairro = document.getElementById('bairroFilter')?.value || '';
        const ativo = document.getElementById('ativoFilter')?.value || '';

        this.currentFilters = { cidade, bairro, ativo };
        await this.loadCentros(0, this.currentFilters);
    }

    clearFilters() {
        document.getElementById('cidadeFilter').value = '';
        document.getElementById('bairroFilter').value = '';
        document.getElementById('ativoFilter').value = '';
        this.currentFilters = {};
        this.loadCentros(0, {});
    }

    initThemeToggle() {
        const toggle = document.querySelector('[data-theme-toggle]');
        const html = document.documentElement;

        if (!toggle) return;

        toggle.addEventListener('click', () => {
            const current = html.getAttribute('data-theme');
            const next = current === 'dark' ? 'light' : 'dark';
            html.setAttribute('data-theme', next);
            toggle.setAttribute('aria-label', `Alternar para modo ${next === 'dark' ? 'claro' : 'escuro'}`);
        });
    }

    initMap() {
        if (!window.google || !google.maps) return;

        this.map = new google.maps.Map(document.getElementById('map'), {
            zoom: 13,
            center: { lat: -23.6261, lng: -46.7917 },
            styles: [
                { featureType: 'poi', elementType: 'labels', stylers: [{ visibility: 'off' }] }
            ]
        });

        if (this.centrosAtuais.length) {
            this.centerMapOnCentros(this.centrosAtuais);
        }
    }

    centerMapOnUser(coords) {
        if (!this.map) return;
        this.map.setCenter({ lat: coords.latitude, lng: coords.longitude });
        this.map.setZoom(14);
    }

    centerMapOnCentros(centros) {
        if (!this.map || !centros.length) return;

        this.markers.forEach(marker => marker.setMap(null));
        this.markers = [];

        const centrosComCoordenadas = centros.filter(
            centro => centro.latitude !== null && centro.longitude !== null
        );

        if (!centrosComCoordenadas.length) return;

        centrosComCoordenadas.forEach(centro => {
            const marker = new google.maps.Marker({
                position: { lat: centro.latitude, lng: centro.longitude },
                map: this.map,
                title: centro.nome,
                icon: {
                    url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
                        <svg width="32" height="40" viewBox="0 0 32 40" xmlns="http://www.w3.org/2000/svg">
                            <path d="M16 0C7.2 0 0 7.2 0 16c0 6.4 4 11.2 16 20 12-8.8 16-13.6 16-20C32 7.2 24.8 0 16 0z" fill="%232E7D32"/>
                            ircle cx="16" cy="16" r="8" fill="white"/>
                            ircle cx="16" cy="16" r="4" fill="%232E7D32"/>
                        </svg>
                    `),
                    scaledSize: new google.maps.Size(32, 40)
                }
            });

            this.markers.push(marker);

            const infoWindow = new google.maps.InfoWindow({
                content: `
                    <div style="padding: 8px; max-width: 220px;">
                        <h4 style="margin: 0 0 6px 0; font-size: 14px;">${centro.nome}</h4>
                        <p style="margin: 0 0 6px 0; font-size: 12px; color: #666;">${centro.endereco}</p>
                        <p style="margin: 0 0 6px 0; font-size: 12px;">${centro.horarioFuncionamento}</p>
                        <a href="tel:${String(centro.telefone).replace(/\D/g, '')}" style="color: #2E7D32; text-decoration: none; font-size: 12px;">
                            📞 ${centro.telefone}
                        </a>
                    </div>
                `
            });

            marker.addListener('click', () => infoWindow.open(this.map, marker));
        });

        if (centrosComCoordenadas.length === 1) {
            this.map.setCenter({
                lat: centrosComCoordenadas[0].latitude,
                lng: centrosComCoordenadas[0].longitude
            });
            this.map.setZoom(15);
            return;
        }

        const bounds = new google.maps.LatLngBounds();
        centrosComCoordenadas.forEach(centro => {
            bounds.extend({ lat: centro.latitude, lng: centro.longitude });
        });
        this.map.fitBounds(bounds);
    }

    viewOnMap(btn) {
        if (!this.map) return;

        const card = btn.closest('.centro-card');
        const lat = parseFloat(card.dataset.lat);
        const lng = parseFloat(card.dataset.lng);

        if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
            alert('Este centro não possui coordenadas disponíveis.');
            return;
        }

        this.map.setCenter({ lat, lng });
        this.map.setZoom(16);
        document.getElementById('map')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    async searchByLocation() {
        const query = document.getElementById('searchLocation')?.value?.trim();
        if (!query || !this.map || !window.google || !google.maps) return;

        try {
            const geocoder = new google.maps.Geocoder();

            const results = await new Promise((resolve, reject) => {
                geocoder.geocode({ address: `${query}, São Paulo, Brasil` }, (result, status) => {
                    if (status === 'OK') resolve(result);
                    else reject(new Error(`Geocode falhou: ${status}`));
                });
            });

            if (results[0]) {
                const location = results[0].geometry.location;
                this.map.setCenter(location);
                this.map.setZoom(14);
            }
        } catch (error) {
            console.error('Erro na busca:', error);
        }
    }

    renderErrorState() {
        const grid = document.getElementById('centrosGrid');
        const pagination = document.getElementById('pagination');

        if (grid) {
            grid.innerHTML = `
                <article class="centro-card">
                    <header class="centro-header">
                        <h4 class="centro-nome">Erro ao carregar centros</h4>
                    </header>
                    <div class="centro-info">
                        <div class="centro-info-item">
                            <span>Verifique se o backend está ativo e se o proxy /api está configurado corretamente.</span>
                        </div>
                    </div>
                </article>
            `;
        }

        if (pagination) {
            pagination.innerHTML = '';
        }
    }
}

const app = new DoeJaApp();

function initMap() {
    app.initMap();
}