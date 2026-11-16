/*
 * Service worker do DoeJá (app instalável).
 * - Guarda o "esqueleto" do site (HTML, CSS, JS, ícones) para abrir rápido, até sem internet.
 * - A API (/api) NUNCA é guardada: dados de doações e login sempre vêm do servidor.
 * - Ao publicar uma versão nova, troque VERSAO para descartar o cache antigo.
 */
const VERSAO = 'doeja-v1';

const ESQUELETO = [
    './',
    'index.html',
    'css/style.css',
    'css/extras.css',
    'css/mobile.css',
    'js/api.js',
    'js/app.js',
    'js/areas.js',
    'manifest.webmanifest',
    'assets/icon-192.png',
    'assets/icon-512.png'
];

/** Bibliotecas de CDN com versão fixa: podem ficar guardadas. */
const CDNS = ['unpkg.com', 'api.fontshare.com', 'cdn.fontshare.com'];

self.addEventListener('install', evento => {
    evento.waitUntil(
        caches.open(VERSAO).then(cache => cache.addAll(ESQUELETO)).then(() => self.skipWaiting())
    );
});

self.addEventListener('activate', evento => {
    evento.waitUntil(
        caches.keys()
            .then(nomes => Promise.all(nomes.filter(n => n !== VERSAO).map(n => caches.delete(n))))
            .then(() => self.clients.claim())
    );
});

self.addEventListener('fetch', evento => {
    const pedido = evento.request;
    if (pedido.method !== 'GET') return;

    const url = new URL(pedido.url);
    const mesmoSite = url.origin === self.location.origin;

    // API e mapas (tiles do OpenStreetMap, busca de endereço): sempre pela rede
    if (mesmoSite && url.pathname.startsWith('/api/')) return;
    if (!mesmoSite && !CDNS.includes(url.hostname)) return;

    // Abrir o app: tenta a versão mais nova; sem internet, usa a guardada
    if (pedido.mode === 'navigate') {
        evento.respondWith(
            fetch(pedido)
                .then(resposta => {
                    const copia = resposta.clone();
                    caches.open(VERSAO).then(cache => cache.put('index.html', copia));
                    return resposta;
                })
                .catch(() => caches.match('index.html'))
        );
        return;
    }

    // Bibliotecas de CDN (versão fixa no endereço): cache primeiro
    if (!mesmoSite) {
        evento.respondWith(
            caches.match(pedido).then(guardado => guardado || fetch(pedido).then(resposta => {
                if (resposta.ok || resposta.type === 'opaque') {
                    const copia = resposta.clone();
                    caches.open(VERSAO).then(cache => cache.put(pedido, copia));
                }
                return resposta;
            }))
        );
        return;
    }

    // Arquivos do site: rede primeiro (atualização nunca mistura versões), cache se estiver sem internet
    evento.respondWith(
        fetch(pedido)
            .then(resposta => {
                if (resposta.ok) {
                    const copia = resposta.clone();
                    caches.open(VERSAO).then(cache => cache.put(pedido, copia));
                }
                return resposta;
            })
            .catch(() => caches.match(pedido))
    );
});
