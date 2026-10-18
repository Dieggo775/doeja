class DoeJaApp {
    constructor() {
        // Caminho relativo: o nginx encaminha /api para o back-end
        this.API_BASE = '/api';

        // Configurações da busca por localização
        this.CIDADE_PADRAO = 'Taboão da Serra, SP';
        this.MAP_CENTER = [-23.6261, -46.7917];
        this.RAIO_BUSCA_KM = 5;
        this.RAIO_MAXIMO_KM = 50;

        this.currentPage = 0;
        this.pageSize = 10;
        this.currentFilters = {};
        this.centrosAtuais = [];

        this.map = null;
        this.markersLayer = null;
        this.userMarker = null;

        this.init();
    }

    async init() {
        this.loadIcons();
        this.setupEventListeners();
        this.initThemeToggle();
        this.initMap();
        await this.loadCentros();
    }

    loadIcons() {
        if (window.lucide) {
            lucide.createIcons();
        }
    }

    // ---------- Utilidades ----------

    // Evita XSS: nunca coloque texto vindo da API no innerHTML sem passar por aqui
    escapeHtml(value) {
        return String(value ?? '').replace(/[&<>"']/g, ch => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        }[ch]));
    }

    toNumber(value) {
        if (value === null || value === undefined || value === '') return null;
        const n = Number(value);
        return Number.isFinite(n) ? n : null;
    }

    onlyDigits(value) {
        return String(value ?? '').replace(/\D/g, '');
    }

    formatDistancia(km) {
        return km < 1
            ? `${Math.round(km * 1000)} m`
            : `${km.toFixed(1).replace('.', ',')} km`;
    }

    setNotice(message) {
        const el = document.getElementById('searchNotice');
        if (!el) return;
        el.textContent = message || '';
        el.hidden = !message;
    }

    // ---------- Eventos ----------

    setupEventListeners() {
        document.getElementById('searchBtn')?.addEventListener('click', () => this.searchByLocation());
        document.getElementById('searchLocation')?.addEventListener('keydown', event => {
            if (event.key === 'Enter') this.searchByLocation();
        });
        document.getElementById('useLocationBtn')?.addEventListener('click', () => this.useMyLocation());
        document.getElementById('applyFilters')?.addEventListener('click', () => this.applyFilters());
        document.getElementById('clearFilters')?.addEventListener('click', () => this.clearFilters());
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

    // ---------- Dados ----------

    normalizeCentro(centro) {
        return {
            id: centro.id ?? null,
            nome: centro.nome ?? 'Centro sem nome',
            endereco: centro.endereco ?? 'Endereço não informado',
            telefone: centro.telefone ?? '',
            whatsapp: this.onlyDigits(centro.whatsapp),
            horarioFuncionamento: centro.horarioFuncionamento ?? 'Horário não informado',
            cidade: centro.cidade ?? 'Cidade não informada',
            bairro: centro.bairro ?? 'Bairro não informado',
            ativo: centro.ativo ?? true,
            latitude: this.toNumber(centro.latitude),
            longitude: this.toNumber(centro.longitude),
            distanciaKm: this.toNumber(centro.distanciaKm)
        };
    }

    async loadCentros(page = 0, filters = {}) {
        try {
            this.currentPage = page;

            const cleanFilters = Object.fromEntries(
                Object.entries(filters).filter(([, value]) => value !== '' && value !== null && value !== undefined)
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

            this.setNotice('');
            this.renderCentros(centros);
            this.renderPagination(pageData);
            this.renderMarkers(centros);

            // Só monta as opções dos filtros quando não há filtro ativo,
            // senão as listas encolhem para o valor já filtrado.
            if (Object.keys(cleanFilters).length === 0) {
                this.populateDynamicFilters(centros);
            }

        } catch (error) {
            console.error('Erro ao carregar centros:', error);
            this.renderErrorState();
        }
    }

    // ---------- Renderização ----------

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
                            <span>Tente alterar os filtros ou buscar outro bairro.</span>
                        </div>
                    </div>
                </article>
            `;
            return;
        }

        grid.innerHTML = centros.map(centro => {
            const tel = this.onlyDigits(centro.telefone);
            const temCoordenadas = Number.isFinite(centro.latitude) && Number.isFinite(centro.longitude);

            const distanciaHtml = Number.isFinite(centro.distanciaKm) ? `
                    <div class="centro-info-item">
                        <i data-lucide="navigation"></i>
                        <span class="centro-distancia">A ${this.escapeHtml(this.formatDistancia(centro.distanciaKm))} de você</span>
                    </div>` : '';

            const ligarHtml = tel.length >= 8
                ? `<a href="tel:${this.escapeHtml(tel)}" class="pagination-btn">Ligar</a>`
                : '';

            const mensagemZap = 'Olá! Vi o centro no DoeJá e gostaria de doar.';
            const whatsappHtml = centro.whatsapp.length >= 10 && centro.whatsapp.length <= 15
                ? `<a href="https://wa.me/${centro.whatsapp}?text=${encodeURIComponent(mensagemZap)}"
                       target="_blank" rel="noopener noreferrer" class="pagination-btn">WhatsApp</a>`
                : '';

            const rotaHtml = temCoordenadas
                ? `<a href="https://www.google.com/maps/search/?api=1&query=${centro.latitude},${centro.longitude}"
                       target="_blank" rel="noopener noreferrer" class="pagination-btn">Como chegar</a>`
                : '';

            return `
            <article class="centro-card" data-lat="${temCoordenadas ? centro.latitude : ''}" data-lng="${temCoordenadas ? centro.longitude : ''}">
                <header class="centro-header">
                    <div>
                        <h4 class="centro-nome">${this.escapeHtml(centro.nome)}</h4>
                        <span class="centro-status ${centro.ativo ? 'status-ativo' : ''}">
                            ${centro.ativo ? 'Ativo' : 'Inativo'}
                        </span>
                    </div>
                </header>

                <div class="centro-info">
                    <div class="centro-info-item">
                        <i data-lucide="map-pin"></i>
                        <span>${this.escapeHtml(centro.endereco)}</span>
                    </div>
                    <div class="centro-info-item">
                        <i data-lucide="building-2"></i>
                        <span>${this.escapeHtml(centro.bairro)} - ${this.escapeHtml(centro.cidade)}</span>
                    </div>
                    <div class="centro-info-item">
                        <i data-lucide="phone"></i>
                        <span>${this.escapeHtml(centro.telefone || 'Telefone não informado')}</span>
                    </div>
                    <div class="centro-info-item">
                        <i data-lucide="clock"></i>
                        <span>${this.escapeHtml(centro.horarioFuncionamento)}</span>
                    </div>${distanciaHtml}
                </div>

                <div class="centro-actions">
                    <button class="pagination-btn" type="button" onclick="app.viewOnMap(this)">Ver no mapa</button>
                    ${whatsappHtml}
                    ${rotaHtml}
                    ${ligarHtml}
                </div>
            </article>`;
        }).join('');

        this.loadIcons();
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
                <button class="pagination-btn ${i === this.currentPage ? 'active' : ''}" type="button"
                        onclick="app.loadCentros(${i}, app.currentFilters)">
                    ${i + 1}
                </button>
            `;
        }

        pagination.innerHTML = pagesHtml;
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
                            <span>Verifique se o back-end está ativo (docker compose ps) e se o proxy /api do nginx está correto.</span>
                        </div>
                    </div>
                </article>
            `;
        }

        if (pagination) {
            pagination.innerHTML = '';
        }
    }

    // ---------- Filtros ----------

    populateDynamicFilters(centros) {
        const cidadeFilter = document.getElementById('cidadeFilter');
        const bairroFilter = document.getElementById('bairroFilter');

        if (!cidadeFilter || !bairroFilter) return;

        const cidades = [...new Set(centros.map(c => c.cidade).filter(Boolean))].sort();
        const bairros = [...new Set(centros.map(c => c.bairro).filter(Boolean))].sort();

        const opcoes = (lista) => lista
            .map(item => `<option value="${this.escapeHtml(item)}">${this.escapeHtml(item)}</option>`)
            .join('');

        cidadeFilter.innerHTML = `<option value="">Todas as cidades</option>${opcoes(cidades)}`;
        bairroFilter.innerHTML = `<option value="">Todos os bairros</option>${opcoes(bairros)}`;
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
        document.getElementById('searchLocation').value = '';
        this.currentFilters = {};
        this.removeUserMarker();
        this.loadCentros(0, {});
    }

    // ---------- Mapa (Leaflet + OpenStreetMap) ----------

    initMap() {
        const mapElement = document.getElementById('map');

        if (!mapElement || !window.L) {
            console.warn('Leaflet não carregou: o mapa ficará desativado.');
            return;
        }

        this.map = L.map(mapElement).setView(this.MAP_CENTER, 13);

        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(this.map);

        this.markersLayer = L.layerGroup().addTo(this.map);
    }

    renderMarkers(centros, extraPoint = null) {
        if (!this.map) return;

        this.markersLayer.clearLayers();

        const pontos = centros.filter(c => Number.isFinite(c.latitude) && Number.isFinite(c.longitude));

        pontos.forEach(centro => {
            const tel = this.onlyDigits(centro.telefone);
            const ligar = tel.length >= 8
                ? `<br><a href="tel:${this.escapeHtml(tel)}">${this.escapeHtml(centro.telefone)}</a>`
                : '';

            L.marker([centro.latitude, centro.longitude])
                .addTo(this.markersLayer)
                .bindPopup(`
                    <strong>${this.escapeHtml(centro.nome)}</strong><br>
                    ${this.escapeHtml(centro.endereco)}<br>
                    ${this.escapeHtml(centro.horarioFuncionamento)}${ligar}
                `);
        });

        const coords = pontos.map(c => [c.latitude, c.longitude]);
        if (extraPoint) coords.push(extraPoint);

        if (coords.length === 1) {
            this.map.setView(coords[0], 15);
        } else if (coords.length > 1) {
            this.map.fitBounds(L.latLngBounds(coords), { padding: [40, 40] });
        }
    }

    setUserMarker(lat, lng, rotulo) {
        if (!this.map) return;
        this.removeUserMarker();

        this.userMarker = L.circleMarker([lat, lng], {
            radius: 9,
            weight: 3,
            fillOpacity: 0.8
        }).addTo(this.map).bindPopup(this.escapeHtml(rotulo || 'Você está aqui'));
    }

    removeUserMarker() {
        if (this.userMarker && this.map) {
            this.map.removeLayer(this.userMarker);
        }
        this.userMarker = null;
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

        this.map.setView([lat, lng], 16);
        document.getElementById('map')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    // ---------- Busca por localização ----------
    // A distância é calculada no servidor (/api/centros/proximos).

    useMyLocation() {
        if (!navigator.geolocation) {
            alert('Seu navegador não suporta geolocalização.');
            return;
        }

        if (!window.isSecureContext) {
            alert('O navegador só libera a localização em HTTPS ou em localhost. ' +
                  `Acesse por http://localhost:${location.port} usando o encaminhamento de portas do VS Code.`);
            return;
        }

        this.setNotice('Obtendo sua localização...');

        navigator.geolocation.getCurrentPosition(
            pos => this.showNearby(pos.coords.latitude, pos.coords.longitude, 'Você está aqui'),
            () => this.setNotice('Não foi possível obter sua localização. Verifique a permissão do navegador.'),
            { timeout: 10000 }
        );
    }

    async geocode(query) {
        const params = new URLSearchParams({
            format: 'jsonv2',
            limit: '1',
            countrycodes: 'br',
            q: `${query}, ${this.CIDADE_PADRAO}`
        });

        const response = await fetch(`https://nominatim.openstreetmap.org/search?${params.toString()}`);
        if (!response.ok) {
            throw new Error(`Geocodificação falhou: HTTP ${response.status}`);
        }

        const results = await response.json();
        if (!results.length) return null;

        return { lat: parseFloat(results[0].lat), lng: parseFloat(results[0].lon) };
    }

    async searchByLocation() {
        const query = document.getElementById('searchLocation')?.value?.trim();
        if (!query) return;

        this.setNotice('Buscando...');

        try {
            const ponto = await this.geocode(query);
            if (ponto) {
                this.showNearby(ponto.lat, ponto.lng, query);
                return;
            }
        } catch (error) {
            console.error('Erro na geocodificação:', error);
        }

        // Plano B: o endereço não foi achado no mapa; procura pelo nome do bairro no servidor
        this.currentFilters = { bairro: query };
        await this.loadCentros(0, this.currentFilters);
        this.setNotice(this.centrosAtuais.length
            ? `Resultados para o bairro "${query}"`
            : `Nenhum centro encontrado para "${query}".`);
    }

    async buscarProximos(lat, lng, raioKm) {
        const params = new URLSearchParams({
            lat: lat.toString(),
            lng: lng.toString(),
            raioKm: raioKm.toString(),
            limite: '20'
        });

        const response = await fetch(`${this.API_BASE}/centros/proximos?${params.toString()}`);
        if (!response.ok) {
            throw new Error(`Erro HTTP ${response.status}`);
        }

        const dados = await response.json();
        return dados.map(centro => this.normalizeCentro(centro));
    }

    async showNearby(lat, lng, rotulo) {
        this.setUserMarker(lat, lng, rotulo);

        try {
            let raio = this.RAIO_BUSCA_KM;
            let lista = await this.buscarProximos(lat, lng, raio);
            let ampliou = false;

            // Nada por perto: amplia a busca até o limite do servidor
            if (!lista.length && raio < this.RAIO_MAXIMO_KM) {
                raio = this.RAIO_MAXIMO_KM;
                lista = await this.buscarProximos(lat, lng, raio);
                ampliou = true;
            }

            this.renderCentros(lista);
            this.renderMarkers(lista, [lat, lng]);

            if (!lista.length) {
                this.setNotice(`Nenhum centro encontrado em até ${raio} km.`);
            } else if (ampliou) {
                this.setNotice(`Nenhum centro em até ${this.RAIO_BUSCA_KM} km. Mostrando os mais próximos (até ${raio} km).`);
            } else {
                this.setNotice(`${lista.length} centro(s) em até ${raio} km`);
            }
        } catch (error) {
            console.error('Erro ao buscar centros próximos:', error);
            this.setNotice('Não foi possível buscar os centros próximos. Tente novamente.');
        }
    }
}

window.app = new DoeJaApp();