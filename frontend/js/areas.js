/**
 * Áreas logadas e navegação do DoeJá:
 * rotas (#/...), login/cadastro, registrar doação, minhas doações,
 * painel do centro, administração e painel de impacto.
 */
class Areas {
    constructor(app) {
        this.app = app;
        this.esc = v => app.escapeHtml(v);
        this.destinoAposLogin = null;
        this.doacaoPendente = null;
        this.filtroAdmin = 'PENDENTE';

        this.ROTAS = {
            '/':        { view: 'view-inicio' },
            '/impacto': { view: 'view-impacto', carregar: () => this.carregarImpacto() },
            '/entrar':  { view: 'view-entrar' },
            '/doacoes': { view: 'view-doacoes', papel: 'USUARIO', carregar: () => this.carregarMinhasDoacoes() },
            '/centro':  { view: 'view-centro', papel: 'CENTRO', carregar: () => this.carregarPainelCentro() },
            '/admin':   { view: 'view-admin', papel: 'ADMIN', carregar: () => this.carregarAdmin() }
        };

        this.STATUS_DOACAO = {
            PROMETIDA:  { texto: 'Combinada', classe: 'st-prometida' },
            ENTREGUE:   { texto: 'Entregue (aguardando confirmação)', classe: 'st-entregue' },
            CONFIRMADA: { texto: 'Recebida pelo centro', classe: 'st-confirmada' },
            CANCELADA:  { texto: 'Cancelada', classe: 'st-cancelada' }
        };

        this.STATUS_CENTRO = {
            PENDENTE:  { texto: 'Em análise', classe: 'st-entregue' },
            APROVADO:  { texto: 'Aprovado', classe: 'st-confirmada' },
            REJEITADO: { texto: 'Recusado - revise os dados e salve de novo', classe: 'st-cancelada' }
        };

        this.init();
    }

    init() {
        window.addEventListener('hashchange', () => this.rotear());
        window.addEventListener('doeja:sessao', () => this.atualizarCabecalho());

        const sair = () => {
            Api.sair();
            this.toast('Você saiu da sua conta.');
            location.hash = '#/';
        };
        document.getElementById('logoutBtn')?.addEventListener('click', sair);
        document.getElementById('bottomSair')?.addEventListener('click', sair);

        this.prepararInstalacao();

        document.getElementById('loginForm')?.addEventListener('submit', e => this.entrar(e));
        document.getElementById('cadastroForm')?.addEventListener('submit', e => this.cadastrar(e));
        document.getElementById('doacaoForm')?.addEventListener('submit', e => this.enviarDoacao(e));
        document.getElementById('addItemBtn')?.addEventListener('click', () => this.adicionarItem());
        document.getElementById('centroForm')?.addEventListener('submit', e => this.salvarCentro(e));
        document.getElementById('localizarBtn')?.addEventListener('click', () => this.localizarEndereco());
        document.getElementById('necessidadesForm')?.addEventListener('submit', e => this.salvarNecessidades(e));
        document.getElementById('novoCentroBtn')?.addEventListener('click', () => this.abrirCentro(null));

        document.querySelectorAll('dialog [data-fechar]').forEach(btn =>
            btn.addEventListener('click', () => btn.closest('dialog').close()));

        // Tocar fora da janela (no fundo escuro) também fecha
        document.querySelectorAll('dialog.modal').forEach(dialog =>
            dialog.addEventListener('click', evento => {
                if (evento.target !== dialog) return;
                const r = dialog.getBoundingClientRect();
                const fora = evento.clientX < r.left || evento.clientX > r.right
                    || evento.clientY < r.top || evento.clientY > r.bottom;
                if (fora) dialog.close();
            }));

        document.querySelectorAll('[data-admin-filtro]').forEach(btn => btn.addEventListener('click', () => {
            this.filtroAdmin = btn.dataset.adminFiltro;
            document.querySelectorAll('[data-admin-filtro]').forEach(b => b.classList.toggle('active', b === btn));
            this.carregarAdmin();
        }));

        // Botões dentro das listas (um ouvinte por lista)
        document.getElementById('minhasDoacoes')?.addEventListener('click', e => this.acaoDoacao(e));
        document.getElementById('doacoesRecebidas')?.addEventListener('click', e => this.acaoDoacao(e));
        document.getElementById('meusCentros')?.addEventListener('click', e => this.acaoCentro(e));
        document.getElementById('adminCentros')?.addEventListener('click', e => this.acaoAdmin(e));

        this.verificarDemonstracao();
        this.atualizarCabecalho();
        this.rotear();
    }

    // ------------------------------------------------------------------ utilidades

    toast(mensagem, tipo = 'ok') {
        const el = document.getElementById('toast');
        if (!el) return;
        el.textContent = mensagem;
        el.className = `toast toast-${tipo}`;
        el.hidden = false;
        clearTimeout(this._toastTimer);
        this._toastTimer = setTimeout(() => { el.hidden = true; }, 6000);
        el.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    data(iso) {
        if (!iso) return '';
        return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
    }

    mostrarErro(form, erro) {
        const caixa = form.querySelector('[data-erro]');
        form.querySelectorAll('[aria-invalid]').forEach(el => el.removeAttribute('aria-invalid'));
        if (erro?.campos) {
            Object.keys(erro.campos).forEach(nome => form.querySelector(`[name="${nome}"]`)?.setAttribute('aria-invalid', 'true'));
        }
        if (caixa) {
            caixa.textContent = erro ? erro.message : '';
            caixa.hidden = !erro;
        }
    }

    ocupado(form, sim) {
        form.querySelectorAll('button[type="submit"]').forEach(b => { b.disabled = sim; });
    }

    vazio(mensagem) {
        return `<div class="vazio">${this.esc(mensagem)}</div>`;
    }

    async verificarDemonstracao() {
        try {
            const info = await Api.get('/info', { autenticar: false });
            document.getElementById('demoBanner').hidden = !info?.demonstracao;
        } catch {
            /* sem aviso se a API não responder */
        }
    }

    // ------------------------------------------------------------------ instalar como app

    /** Chrome/Android/Edge oferecem a instalação; guardamos o pedido e mostramos o botão. */
    prepararInstalacao() {
        const botao = document.getElementById('instalarBtn');
        if (!botao) return;

        window.addEventListener('beforeinstallprompt', evento => {
            evento.preventDefault();
            this.pedidoInstalacao = evento;
            botao.hidden = false;
        });
        botao.addEventListener('click', async () => {
            if (!this.pedidoInstalacao) return;
            this.pedidoInstalacao.prompt();
            await this.pedidoInstalacao.userChoice;
            this.pedidoInstalacao = null;
            botao.hidden = true;
        });
        window.addEventListener('appinstalled', () => {
            botao.hidden = true;
            this.toast('DoeJá instalado! Ele já está na sua tela inicial.');
        });
    }

    // ------------------------------------------------------------------ navegação

    rotear() {
        const caminho = (location.hash.replace(/^#/, '') || '/').split('?')[0];
        const rota = this.ROTAS[caminho] || this.ROTAS['/'];
        const usuario = Api.usuario();

        if (rota.papel) {
            if (!usuario) {
                this.destinoAposLogin = caminho;
                this.toast('Entre na sua conta para continuar.', 'info');
                location.hash = '#/entrar';
                return;
            }
            if (usuario.papel !== rota.papel) {
                this.toast('Esta área não está disponível para o seu tipo de conta.', 'erro');
                location.hash = '#/';
                return;
            }
        }

        document.querySelectorAll('.view').forEach(v => { v.hidden = v.id !== rota.view; });
        document.querySelectorAll('#mainNav a, #bottomNav a').forEach(a =>
            a.classList.toggle('ativo', a.dataset.rota === caminho));

        if (rota.view === 'view-inicio' && this.app.map) {
            setTimeout(() => this.app.map.invalidateSize(), 50);
        }
        rota.carregar?.();
        window.scrollTo({ top: 0 });
    }

    atualizarCabecalho() {
        const usuario = Api.usuario();
        document.getElementById('loginLink').hidden = !!usuario;
        document.getElementById('logoutBtn').hidden = !usuario;

        const chip = document.getElementById('userChip');
        chip.hidden = !usuario;
        chip.textContent = usuario ? `Olá, ${usuario.nome.split(' ')[0]}` : '';

        document.querySelectorAll('#mainNav [data-papel], #bottomNav [data-papel]').forEach(a => {
            a.hidden = !usuario || usuario.papel !== a.dataset.papel;
        });
        document.getElementById('bottomEntrar').hidden = !!usuario;
        document.getElementById('bottomSair').hidden = !usuario;
    }

    paginaInicialDo(papel) {
        return { CENTRO: '/centro', ADMIN: '/admin' }[papel] || '/';
    }

    // ------------------------------------------------------------------ login e cadastro

    async entrar(evento) {
        evento.preventDefault();
        const form = evento.target;
        const dados = Object.fromEntries(new FormData(form));
        this.mostrarErro(form, null);
        this.ocupado(form, true);
        try {
            const resposta = await Api.post('/auth/login', { email: dados.email, senha: dados.senha }, { autenticar: false });
            this.aposEntrar(form, resposta);
        } catch (erro) {
            this.mostrarErro(form, erro);
        } finally {
            this.ocupado(form, false);
        }
    }

    async cadastrar(evento) {
        evento.preventDefault();
        const form = evento.target;
        const dados = Object.fromEntries(new FormData(form));
        this.mostrarErro(form, null);
        this.ocupado(form, true);
        try {
            const resposta = await Api.post('/auth/cadastro', {
                nome: dados.nome,
                email: dados.email,
                senha: dados.senha,
                papel: dados.papel,
                aceiteLgpd: dados.aceiteLgpd === 'on'
            }, { autenticar: false });
            this.aposEntrar(form, resposta);
            this.toast(resposta.usuario.papel === 'CENTRO'
                ? 'Conta criada! Agora cadastre o seu centro de doação.'
                : 'Conta criada! Escolha um centro no mapa e clique em "Quero doar".');
        } catch (erro) {
            this.mostrarErro(form, erro);
        } finally {
            this.ocupado(form, false);
        }
    }

    aposEntrar(form, resposta) {
        Api.salvarSessao(resposta);
        form.reset();
        const papel = resposta.usuario.papel;
        const destino = this.destinoAposLogin && this.ROTAS[this.destinoAposLogin]?.papel === papel
            ? this.destinoAposLogin
            : this.paginaInicialDo(papel);
        this.destinoAposLogin = null;
        location.hash = `#${destino}`;

        if (this.doacaoPendente && papel === 'USUARIO') {
            const id = this.doacaoPendente;
            this.doacaoPendente = null;
            setTimeout(() => this.abrirDoacao(id), 300);
        }
    }

    // ------------------------------------------------------------------ registrar doação

    abrirDoacao(centroId) {
        const usuario = Api.usuario();
        if (!usuario) {
            this.doacaoPendente = centroId;
            this.toast('Entre ou crie uma conta de doador para registrar a doação.', 'info');
            location.hash = '#/entrar';
            return;
        }
        if (usuario.papel !== 'USUARIO') {
            this.toast('Só contas de doador podem registrar doações.', 'erro');
            return;
        }

        const centro = this.app.centrosPorId.get(centroId);
        if (!centro) return;

        const dialog = document.getElementById('doacaoDialog');
        const form = document.getElementById('doacaoForm');
        form.reset();
        form.dataset.centroId = String(centroId);
        this.mostrarErro(form, null);

        document.getElementById('doacaoCentroNome').textContent = `Para: ${centro.nome} - ${centro.endereco}`;
        const precisa = centro.necessidades.filter(n => n.tipo === 'PRECISA');
        const aviso = document.getElementById('doacaoPrecisa');
        aviso.hidden = !precisa.length;
        aviso.textContent = precisa.length
            ? `Este centro está precisando de: ${precisa.map(n => n.categoriaNome + (n.descricao ? ` (${n.descricao})` : '')).join('; ')}.`
            : '';

        document.getElementById('itensDoacao').innerHTML = '';
        this.adicionarItem(precisa[0]?.categoria || centro.necessidades[0]?.categoria || '');
        dialog.showModal();
        this.app.loadIcons();
    }

    adicionarItem(categoria = '') {
        const lista = document.getElementById('itensDoacao');
        if (lista.children.length >= 20) return;
        const opcoes = this.app.categorias.map(c =>
            `<option value="${this.esc(c.slug)}" ${c.slug === categoria ? 'selected' : ''}>${this.esc(c.nome)}</option>`).join('');
        const linha = document.createElement('div');
        linha.className = 'item-linha';
        linha.innerHTML = `
            <select name="categoria" class="filter-select" aria-label="Categoria" required>${opcoes}</select>
            <input name="descricao" class="search-input" placeholder="O que é? Ex.: arroz 5 kg" maxlength="200" aria-label="Descrição" required>
            <input name="quantidade" class="search-input" type="number" min="1" max="1000" value="1" aria-label="Quantidade" required>
            <button type="button" class="clear-btn" aria-label="Remover item">✕</button>`;
        linha.querySelector('button').addEventListener('click', () => {
            if (lista.children.length > 1) linha.remove();
        });
        lista.appendChild(linha);
    }

    async enviarDoacao(evento) {
        evento.preventDefault();
        const form = evento.target;
        const itens = [...document.querySelectorAll('#itensDoacao .item-linha')].map(l => ({
            categoria: l.querySelector('[name="categoria"]').value,
            descricao: l.querySelector('[name="descricao"]').value.trim(),
            quantidade: Number(l.querySelector('[name="quantidade"]').value)
        }));

        if (itens.some(i => !i.descricao)) {
            this.mostrarErro(form, new Error('Descreva cada item (ex.: "arroz 5 kg").'));
            return;
        }

        this.ocupado(form, true);
        try {
            await Api.post('/doacoes', {
                centroId: Number(form.dataset.centroId),
                observacao: form.observacao.value,
                itens
            });
            document.getElementById('doacaoDialog').close();
            this.toast('Doação registrada! Combine a entrega pelo telefone ou WhatsApp do centro e acompanhe aqui.');
            location.hash = '#/doacoes';
        } catch (erro) {
            this.mostrarErro(form, erro);
        } finally {
            this.ocupado(form, false);
        }
    }

    // ------------------------------------------------------------------ listas de doações

    cartaoDoacao(d, visaoCentro) {
        const st = this.STATUS_DOACAO[d.status] || { texto: d.status, classe: '' };
        const itens = d.itens.map(i =>
            `<li>${this.esc(i.quantidade)}× ${this.esc(i.descricao)} <span class="muted">(${this.esc(i.categoriaNome)})</span></li>`).join('');

        let botoes = '';
        if (visaoCentro) {
            if (d.status === 'PROMETIDA' || d.status === 'ENTREGUE') {
                botoes += `<button class="filter-btn" data-doacao="${d.id}" data-status="CONFIRMADA">Confirmar recebimento</button>`;
            }
            if (d.status === 'PROMETIDA') {
                botoes += `<button class="clear-btn" data-doacao="${d.id}" data-status="CANCELADA">Cancelar</button>`;
            }
        } else if (d.status === 'PROMETIDA') {
            botoes = `<button class="filter-btn" data-doacao="${d.id}" data-status="ENTREGUE">Já entreguei</button>
                      <button class="clear-btn" data-doacao="${d.id}" data-status="CANCELADA">Desistir</button>`;
        }

        return `
            <article class="centro-card">
                <header class="centro-header">
                    <div>
                        <h4 class="centro-nome">${this.esc(visaoCentro ? `De: ${d.doadorNome}` : d.centroNome)}</h4>
                        <span class="muted">${this.esc(visaoCentro ? d.centroNome : d.centroEndereco)} · ${this.esc(this.data(d.criadoEm))}</span>
                    </div>
                    <span class="badge ${st.classe}">${this.esc(st.texto)}</span>
                </header>
                <ul class="itens">${itens}</ul>
                ${d.observacao ? `<p class="muted">"${this.esc(d.observacao)}"</p>` : ''}
                ${botoes ? `<div class="centro-actions">${botoes}</div>` : ''}
            </article>`;
    }

    async carregarMinhasDoacoes() {
        const alvo = document.getElementById('minhasDoacoes');
        alvo.innerHTML = '<p>Carregando...</p>';
        try {
            const lista = await Api.get('/doacoes/minhas');
            alvo.innerHTML = lista.length
                ? `<div class="centros-grid">${lista.map(d => this.cartaoDoacao(d, false)).join('')}</div>`
                : this.vazio('Você ainda não registrou doações. Encontre um centro no mapa e clique em "Quero doar".');
        } catch (erro) {
            alvo.innerHTML = this.vazio(erro.message);
        }
    }

    async acaoDoacao(evento) {
        const btn = evento.target.closest('[data-doacao]');
        if (!btn) return;
        const novo = btn.dataset.status;
        if (novo === 'CANCELADA' && !confirm('Cancelar esta doação?')) return;
        btn.disabled = true;
        try {
            await Api.patch(`/doacoes/${btn.dataset.doacao}/status`, { status: novo });
            this.toast({ ENTREGUE: 'Obrigado! O centro vai confirmar o recebimento.',
                         CONFIRMADA: 'Recebimento confirmado.',
                         CANCELADA: 'Doação cancelada.' }[novo] || 'Atualizado.');
            Api.usuario()?.papel === 'CENTRO' ? this.carregarDoacoesRecebidas() : this.carregarMinhasDoacoes();
        } catch (erro) {
            this.toast(erro.message, 'erro');
            btn.disabled = false;
        }
    }

    // ------------------------------------------------------------------ painel do centro

    async carregarPainelCentro() {
        await Promise.all([this.carregarMeusCentros(), this.carregarDoacoesRecebidas()]);
    }

    async carregarMeusCentros() {
        const alvo = document.getElementById('meusCentros');
        alvo.innerHTML = '<p>Carregando...</p>';
        try {
            const centros = await Api.get('/meus-centros');
            this.meusCentros = new Map(centros.map(c => [c.id, c]));
            alvo.innerHTML = centros.length
                ? `<div class="centros-grid">${centros.map(c => this.cartaoMeuCentro(c)).join('')}</div>`
                : this.vazio('Nenhum centro cadastrado ainda. Clique em "Cadastrar centro" para começar.');
            this.app.loadIcons();
        } catch (erro) {
            alvo.innerHTML = this.vazio(erro.message);
        }
    }

    cartaoMeuCentro(c) {
        const st = this.STATUS_CENTRO[c.status] || { texto: c.status, classe: '' };
        const tags = (c.necessidades || []).map(n =>
            `<li class="tag ${n.tipo === 'PRECISA' ? 'tag-precisa' : 'tag-aceita'} ${n.urgente ? 'tag-urgente' : ''}">
                ${n.urgente ? '<strong>Urgente:</strong> ' : ''}${this.esc(n.categoriaNome)}</li>`).join('');
        return `
            <article class="centro-card">
                <header class="centro-header">
                    <div>
                        <h4 class="centro-nome">${this.esc(c.nome)}</h4>
                        <span class="muted">${this.esc(c.endereco)} - ${this.esc(c.bairro)}</span>
                    </div>
                    <span class="badge ${st.classe}">${this.esc(st.texto)}</span>
                </header>
                ${tags ? `<ul class="necessidades">${tags}</ul>` : '<p class="muted">Nenhuma necessidade informada.</p>'}
                <p class="muted">${c.ativo ? 'Recebendo doações' : 'Pausado: não aparece como disponível para doar'}</p>
                <div class="centro-actions">
                    <button class="filter-btn" data-centro="${c.id}" data-acao="necessidades">O que precisamos</button>
                    <button class="pagination-btn" data-centro="${c.id}" data-acao="editar">Editar dados</button>
                    <button class="pagination-btn" data-centro="${c.id}" data-acao="ativo">${c.ativo ? 'Pausar' : 'Retomar'}</button>
                </div>
            </article>`;
    }

    async carregarDoacoesRecebidas() {
        const alvo = document.getElementById('doacoesRecebidas');
        alvo.innerHTML = '<p>Carregando...</p>';
        try {
            const lista = await Api.get('/doacoes/recebidas');
            alvo.innerHTML = lista.length
                ? `<div class="centros-grid">${lista.map(d => this.cartaoDoacao(d, true)).join('')}</div>`
                : this.vazio('Nenhuma doação registrada para os seus centros ainda.');
        } catch (erro) {
            alvo.innerHTML = this.vazio(erro.message);
        }
    }

    async acaoCentro(evento) {
        const btn = evento.target.closest('[data-centro]');
        if (!btn) return;
        const centro = this.meusCentros?.get(Number(btn.dataset.centro));
        if (!centro) return;

        if (btn.dataset.acao === 'editar') this.abrirCentro(centro);
        if (btn.dataset.acao === 'necessidades') this.abrirNecessidades(centro);
        if (btn.dataset.acao === 'ativo') {
            btn.disabled = true;
            try {
                await Api.patch(`/meus-centros/${centro.id}/ativo`, { ativo: !centro.ativo });
                this.toast(centro.ativo ? 'Centro pausado.' : 'Centro voltou a receber doações.');
                this.carregarMeusCentros();
            } catch (erro) {
                this.toast(erro.message, 'erro');
                btn.disabled = false;
            }
        }
    }

    abrirCentro(centro) {
        const form = document.getElementById('centroForm');
        form.reset();
        this.mostrarErro(form, null);
        document.getElementById('centroFormTitulo').textContent = centro ? 'Editar centro' : 'Cadastrar centro';

        // Obs.: o campo oculto se chama "idCentro" porque form.id é o atributo id do próprio <form>
        const campos = ['nome', 'descricao', 'endereco', 'bairro', 'cep', 'cidade', 'estado',
            'telefone', 'whatsapp', 'horarioFuncionamento', 'latitude', 'longitude'];
        campos.forEach(c => {
            if (centro) form.elements[c].value = centro[c] ?? '';
        });
        form.elements.idCentro.value = centro ? centro.id : '';
        if (!centro) {
            form.elements.latitude.value = '';
            form.elements.longitude.value = '';
        }
        this.mostrarCoordenadas();
        document.getElementById('centroDialog').showModal();
        this.app.loadIcons();
    }

    mostrarCoordenadas() {
        const form = document.getElementById('centroForm');
        const info = document.getElementById('coordsInfo');
        const lat = Number(form.latitude.value);
        const lng = Number(form.longitude.value);
        info.innerHTML = form.latitude.value && Number.isFinite(lat) && Number.isFinite(lng)
            ? `Localizado em ${lat.toFixed(5)}, ${lng.toFixed(5)} - <a href="https://www.openstreetmap.org/?mlat=${lat}&mlon=${lng}#map=17/${lat}/${lng}" target="_blank" rel="noopener noreferrer">conferir no mapa</a>`
            : 'Localização ainda não definida.';
    }

    async localizarEndereco() {
        const form = document.getElementById('centroForm');
        const partes = [form.endereco.value, form.bairro.value, form.cidade.value, form.estado.value]
            .map(v => v.trim()).filter(Boolean);
        if (partes.length < 2) {
            this.mostrarErro(form, new Error('Preencha endereço e bairro para localizar.'));
            return false;
        }
        document.getElementById('coordsInfo').textContent = 'Localizando...';
        try {
            const params = new URLSearchParams({ format: 'jsonv2', limit: '1', countrycodes: 'br', q: partes.join(', ') });
            const r = await fetch(`https://nominatim.openstreetmap.org/search?${params}`);
            const lista = r.ok ? await r.json() : [];
            if (!lista.length) {
                this.mostrarCoordenadas();
                this.mostrarErro(form, new Error('Endereço não encontrado no mapa. Confira rua, bairro e cidade.'));
                return false;
            }
            form.latitude.value = Number(lista[0].lat).toFixed(6);
            form.longitude.value = Number(lista[0].lon).toFixed(6);
            this.mostrarErro(form, null);
            this.mostrarCoordenadas();
            return true;
        } catch {
            this.mostrarCoordenadas();
            this.mostrarErro(form, new Error('Não foi possível consultar o mapa agora. Tente de novo.'));
            return false;
        }
    }

    async salvarCentro(evento) {
        evento.preventDefault();
        const form = evento.target;
        if (!form.latitude.value || !form.longitude.value) {
            const ok = await this.localizarEndereco();
            if (!ok) return;
        }
        const dados = Object.fromEntries(new FormData(form));
        const corpo = {
            nome: dados.nome, descricao: dados.descricao, endereco: dados.endereco, bairro: dados.bairro,
            cidade: dados.cidade, estado: dados.estado, cep: dados.cep, telefone: dados.telefone,
            whatsapp: (dados.whatsapp || '').replace(/\D/g, ''),
            horarioFuncionamento: dados.horarioFuncionamento,
            latitude: Number(dados.latitude), longitude: Number(dados.longitude)
        };

        this.ocupado(form, true);
        try {
            if (dados.idCentro) {
                await Api.put(`/meus-centros/${dados.idCentro}`, corpo);
                this.toast('Dados do centro atualizados.');
            } else {
                await Api.post('/meus-centros', corpo);
                this.toast('Centro enviado para análise. Assim que for aprovado, aparece no mapa.');
            }
            document.getElementById('centroDialog').close();
            this.carregarMeusCentros();
        } catch (erro) {
            this.mostrarErro(form, erro);
        } finally {
            this.ocupado(form, false);
        }
    }

    abrirNecessidades(centro) {
        const form = document.getElementById('necessidadesForm');
        form.centroId.value = centro.id;
        this.mostrarErro(form, null);
        const atuais = new Map((centro.necessidades || []).map(n => [n.categoria, n]));

        document.getElementById('necessidadesLista').innerHTML = this.app.categorias.map(c => {
            const n = atuais.get(c.slug);
            return `
                <div class="necessidade-linha" data-categoria="${this.esc(c.slug)}">
                    <label class="check"><input type="checkbox" data-campo="ativa" ${n ? 'checked' : ''}> <strong>${this.esc(c.nome)}</strong></label>
                    <select class="filter-select" data-campo="tipo" aria-label="Tipo">
                        <option value="PRECISA" ${n?.tipo !== 'ACEITA' ? 'selected' : ''}>Precisa</option>
                        <option value="ACEITA" ${n?.tipo === 'ACEITA' ? 'selected' : ''}>Aceita</option>
                    </select>
                    <input class="search-input" data-campo="descricao" maxlength="300" placeholder="Detalhe (opcional)" value="${this.esc(n?.descricao || '')}">
                    <label class="check"><input type="checkbox" data-campo="urgente" ${n?.urgente ? 'checked' : ''}> Urgente</label>
                </div>`;
        }).join('');
        document.getElementById('necessidadesDialog').showModal();
    }

    async salvarNecessidades(evento) {
        evento.preventDefault();
        const form = evento.target;
        const lista = [...document.querySelectorAll('#necessidadesLista .necessidade-linha')]
            .filter(l => l.querySelector('[data-campo="ativa"]').checked)
            .map(l => ({
                categoria: l.dataset.categoria,
                tipo: l.querySelector('[data-campo="tipo"]').value,
                descricao: l.querySelector('[data-campo="descricao"]').value.trim(),
                urgente: l.querySelector('[data-campo="urgente"]').checked
            }));

        this.ocupado(form, true);
        try {
            await Api.put(`/meus-centros/${form.centroId.value}/necessidades`, lista);
            document.getElementById('necessidadesDialog').close();
            this.toast('Necessidades atualizadas. Os doadores já veem a mudança.');
            this.carregarMeusCentros();
        } catch (erro) {
            this.mostrarErro(form, erro);
        } finally {
            this.ocupado(form, false);
        }
    }

    // ------------------------------------------------------------------ administração

    async carregarAdmin() {
        const alvo = document.getElementById('adminCentros');
        alvo.innerHTML = '<p>Carregando...</p>';
        try {
            const centros = await Api.get(`/admin/centros?status=${encodeURIComponent(this.filtroAdmin)}`);
            alvo.innerHTML = centros.length
                ? `<div class="centros-grid">${centros.map(c => this.cartaoAdmin(c)).join('')}</div>`
                : this.vazio('Nada por aqui.');
        } catch (erro) {
            alvo.innerHTML = this.vazio(erro.message);
        }
    }

    cartaoAdmin(c) {
        const st = this.STATUS_CENTRO[c.status] || { texto: c.status, classe: '' };
        const botoes = [];
        if (c.status !== 'APROVADO') botoes.push(`<button class="filter-btn" data-admin="${c.id}" data-status="APROVADO">Aprovar</button>`);
        if (c.status !== 'REJEITADO') botoes.push(`<button class="clear-btn" data-admin="${c.id}" data-status="REJEITADO">Recusar</button>`);
        if (c.status !== 'PENDENTE') botoes.push(`<button class="pagination-btn" data-admin="${c.id}" data-status="PENDENTE">Voltar para análise</button>`);
        const temCoord = Number.isFinite(c.latitude) && Number.isFinite(c.longitude);
        return `
            <article class="centro-card">
                <header class="centro-header">
                    <h4 class="centro-nome">${this.esc(c.nome)}</h4>
                    <span class="badge ${st.classe}">${this.esc(st.texto)}</span>
                </header>
                ${c.descricao ? `<p class="centro-descricao">${this.esc(c.descricao)}</p>` : ''}
                <dl class="detalhes">
                    <dt>Endereço</dt><dd>${this.esc(c.endereco)} - ${this.esc(c.bairro)}, ${this.esc(c.cidade)}/${this.esc(c.estado)} ${this.esc(c.cep || '')}</dd>
                    <dt>Telefone</dt><dd>${this.esc(c.telefone)}${c.whatsapp ? ` · WhatsApp ${this.esc(c.whatsapp)}` : ''}</dd>
                    <dt>Horário</dt><dd>${this.esc(c.horarioFuncionamento)}</dd>
                    <dt>Mapa</dt><dd>${temCoord ? `<a href="https://www.openstreetmap.org/?mlat=${c.latitude}&mlon=${c.longitude}#map=17/${c.latitude}/${c.longitude}" target="_blank" rel="noopener noreferrer">conferir localização</a>` : 'sem coordenadas'}</dd>
                </dl>
                <div class="centro-actions">${botoes.join('')}</div>
            </article>`;
    }

    async acaoAdmin(evento) {
        const btn = evento.target.closest('[data-admin]');
        if (!btn) return;
        btn.disabled = true;
        try {
            await Api.patch(`/admin/centros/${btn.dataset.admin}/status`, { status: btn.dataset.status });
            this.toast({ APROVADO: 'Centro aprovado: já aparece no mapa.',
                         REJEITADO: 'Cadastro recusado.',
                         PENDENTE: 'Cadastro voltou para análise.' }[btn.dataset.status]);
            this.carregarAdmin();
            this.app.loadCentros(0, this.app.currentFilters);
        } catch (erro) {
            this.toast(erro.message, 'erro');
            btn.disabled = false;
        }
    }

    // ------------------------------------------------------------------ painel de impacto

    formatarTempo(minutos) {
        if (minutos == null) return '-';
        if (minutos < 60) return `${minutos} min`;
        const horas = minutos / 60;
        return horas < 48
            ? `${horas.toFixed(1).replace('.', ',')} h`
            : `${(horas / 24).toFixed(1).replace('.', ',')} dias`;
    }

    async carregarImpacto() {
        const alvo = document.getElementById('impactoConteudo');
        alvo.innerHTML = '<p>Carregando...</p>';
        try {
            const e = await Api.get('/estatisticas', { autenticar: false });
            const num = n => Number(n ?? 0).toLocaleString('pt-BR');
            const tile = (valor, rotulo) => `
                <div class="stat-item"><div class="stat-number">${valor}</div><div class="stat-label">${this.esc(rotulo)}</div></div>`;
            const barras = (lista, vazio) => {
                if (!lista.length) return `<p class="muted">${this.esc(vazio)}</p>`;
                const max = Math.max(...lista.map(i => i.total), 1);
                return `<ul class="barras">${lista.map(i => `
                    <li><span class="barra-rotulo">${this.esc(i.nome)}</span>
                        <span class="barra"><span style="width:${Math.round(i.total / max * 100)}%"></span></span>
                        <span class="barra-valor">${num(i.total)}</span></li>`).join('')}</ul>`;
            };
            const porStatus = Object.entries(e.doacoesPorStatus || {}).map(([s, total]) =>
                ({ nome: this.STATUS_DOACAO[s]?.texto || s, total }));

            alvo.innerHTML = `
                <div class="stats-grid">
                    ${tile(num(e.centrosAtivos), 'centros recebendo doações')}
                    ${tile(num(e.bairrosAtendidos), 'bairros com centro ativo')}
                    ${tile(num(e.necessidadesUrgentes), 'pedidos urgentes abertos')}
                    ${tile(num(e.doacoesRegistradas), 'doações registradas')}
                    ${tile(num(e.itensConfirmados), 'itens entregues e confirmados')}
                    ${tile(`${String(e.taxaConfirmacaoPercentual).replace('.', ',')}%`, 'das doações chegaram ao centro')}
                    ${tile(this.formatarTempo(e.tempoMedioConfirmacaoMinutos), 'tempo médio até a confirmação')}
                    ${tile(num(e.doadoresCadastrados), 'doadores cadastrados')}
                </div>
                <div class="graficos">
                    <section><h3 class="section-title">Itens confirmados por categoria</h3>${barras(e.itensPorCategoria, 'Ainda não há doações confirmadas.')}</section>
                    <section><h3 class="section-title">Doações por situação</h3>${barras(porStatus, 'Ainda não há doações.')}</section>
                    <section><h3 class="section-title">Centros ativos por bairro</h3>${barras(e.centrosPorBairro, 'Nenhum centro ativo.')}</section>
                </div>`;
        } catch (erro) {
            alvo.innerHTML = this.vazio(erro.message);
        }
    }
}

window.areas = new Areas(window.app);
