/**
 * Cliente da API + sessão do usuário.
 * O token JWT fica no sessionStorage (some ao fechar a aba) e só é enviado
 * nas chamadas que precisam de login.
 */
const Api = {
    BASE: '/api',
    CHAVE: 'doeja.sessao',

    sessao() {
        try {
            const s = JSON.parse(sessionStorage.getItem(this.CHAVE) || 'null');
            if (s && s.expiraEm && new Date(s.expiraEm) <= new Date()) {
                this.sair();
                return null;
            }
            return s;
        } catch {
            return null;
        }
    },

    usuario() {
        return this.sessao()?.usuario ?? null;
    },

    salvarSessao(resposta) {
        sessionStorage.setItem(this.CHAVE, JSON.stringify({
            token: resposta.token,
            expiraEm: resposta.expiraEm,
            usuario: resposta.usuario
        }));
        window.dispatchEvent(new Event('doeja:sessao'));
    },

    sair() {
        sessionStorage.removeItem(this.CHAVE);
        window.dispatchEvent(new Event('doeja:sessao'));
    },

    /**
     * Faz a requisição e devolve o JSON. Em erro, lança Error com a mensagem do servidor
     * (e .campos com os erros por campo, quando houver).
     */
    async req(metodo, caminho, corpo = undefined, { autenticar = true } = {}) {
        const headers = { 'Accept': 'application/json' };
        if (corpo !== undefined) headers['Content-Type'] = 'application/json';

        const sessao = this.sessao();
        if (autenticar && sessao?.token) headers['Authorization'] = `Bearer ${sessao.token}`;

        let resposta;
        try {
            resposta = await fetch(`${this.BASE}${caminho}`, {
                method: metodo,
                headers,
                body: corpo === undefined ? undefined : JSON.stringify(corpo)
            });
        } catch {
            throw new Error('Não foi possível falar com o servidor. Verifique sua conexão.');
        }

        const texto = await resposta.text();
        let dados = null;
        try { dados = texto ? JSON.parse(texto) : null; } catch { dados = null; }

        if (!resposta.ok) {
            if (resposta.status === 401 && autenticar && sessao) {
                this.sair();
            }
            const erro = new Error(dados?.message || `Erro ${resposta.status}`);
            erro.status = resposta.status;
            erro.campos = dados?.campos || null;
            throw erro;
        }
        return dados;
    },

    get(caminho, opcoes) { return this.req('GET', caminho, undefined, opcoes); },
    post(caminho, corpo, opcoes) { return this.req('POST', caminho, corpo, opcoes); },
    put(caminho, corpo) { return this.req('PUT', caminho, corpo); },
    patch(caminho, corpo) { return this.req('PATCH', caminho, corpo); }
};
