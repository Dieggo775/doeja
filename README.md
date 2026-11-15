# DoeJá

Sistema web que conecta **doadores** a **centros de doação** (ONGs, igrejas, casas de acolhimento,
pontos de arrecadação) de Taboão da Serra/SP. O doador vê no mapa **quem está precisando de quê agora**,
registra a doação e o centro confirma o recebimento. Os números de uso viram indicadores de impacto.

- **Projeto de Extensão Curricularizada – UniFECAF (ADS)**
- **Temática 1:** Digitalização e Automação de Processos Sociais (gestão de doações)
- **ODS 1:** Erradicação da Pobreza

## O problema

Famílias em vulnerabilidade dependem de doações, e há pessoas dispostas a doar, mas a informação é
espalhada (grupos de WhatsApp, posts antigos, boca a boca). O doador não sabe **onde** doar, **quando**
o centro está aberto e **o que** ele realmente precisa; o centro recebe o que não precisa e falta o essencial,
e não tem registro do que entrou.

## O que o sistema faz

| Quem | O que pode fazer |
|---|---|
| **Qualquer pessoa** | Buscar centros por bairro, CEP ou localização; filtrar por "o que quero doar"; ver no mapa; ver o que cada centro precisa (com destaque para urgente); ligar, chamar no WhatsApp ou traçar rota; ver o painel de impacto |
| **Doador** (conta USUARIO) | Registrar uma doação (itens e quantidades) para um centro, acompanhar o status e avisar quando entregou |
| **Responsável por centro** (conta CENTRO) | Cadastrar o centro (entra "em análise"), editar dados, informar o que precisa/aceita e marcar urgências, pausar o recebimento, ver as doações combinadas e confirmar o recebimento |
| **Administrador** (ADMIN) | Analisar cadastros de centros e aprovar ou recusar. Só centros aprovados aparecem ao público |

Ciclo de uma doação: **Combinada → Entregue (doador avisa) → Recebida (centro confirma)**, ou **Cancelada**.

Painel de impacto (`#/impacto`): centros ativos, bairros atendidos, pedidos urgentes, doações registradas,
itens confirmados por categoria, taxa de doações que chegaram ao centro e tempo médio até a confirmação.

## Arquitetura

```
Navegador ──> nginx (frontend estático + proxy /api) ──> Spring Boot (API REST) ──> PostgreSQL
              HTML/CSS/JS + Leaflet/OpenStreetMap         Java 17, JWT, Flyway        16
```

- **Frontend:** HTML, CSS e JavaScript puro; mapa com Leaflet + OpenStreetMap; geocodificação com Nominatim.
- **Backend:** Spring Boot 3.4 (Web, Data JPA, Validation, Security + OAuth2 Resource Server para JWT, Actuator).
- **Banco:** PostgreSQL 16, versionado com Flyway (`backend/src/main/resources/db/migration`).
- **Infra:** Docker Compose com healthchecks; banco e API só escutam em `127.0.0.1` no servidor.

Modelo de dados: `usuarios`, `centros_doacao`, `categorias_doacao`, `necessidades_centro`, `doacoes`, `itens_doacao`
(ver `V1__schema_inicial.sql` e `Diagrama_DoeJa.drawio`).

## Como rodar

Pré-requisito: Docker com Docker Compose.

```bash
cp .env.example .env      # preencha as senhas e o JWT_SECRET
docker compose up -d --build
```

Acesse `http://localhost:3100` (ou a porta definida em `FRONTEND_PORT`).

Variáveis do `.env`:

| Variável | Para que serve |
|---|---|
| `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Banco de dados |
| `JWT_SECRET` | Chave dos tokens de login, 32+ caracteres (`openssl rand -base64 48`) |
| `ADMIN_EMAIL`, `ADMIN_SENHA` | Conta de administrador criada na primeira subida |
| `DEMO_ENABLED` | `true` mostra o aviso de demonstração e cria as contas de teste |
| `DEMO_SENHA` | Senha das contas de demonstração (8+ caracteres) |
| `DB_PORT`, `FRONTEND_PORT` | Portas no servidor (padrão 5434 e 3100) |

### Publicar na internet

No ar em **https://doeja.vercel.app**. Site na **Vercel**, API no **Render** e banco no **Neon**, todos gratuitos. Passo a passo em
[`docs/05-DEPLOY.md`](docs/05-DEPLOY.md) (arquivos `frontend/vercel.json` e `render.yaml`).

### Dados de demonstração

Com `DEMO_ENABLED=true`, o banco recebe **9 instituições fictícias** em ruas e bairros reais de Taboão da Serra
(coordenadas do OpenStreetMap), com nomes e telefones inventados, e o site mostra uma faixa avisando que é
demonstração. São criadas também estas contas (senha = `DEMO_SENHA`):

- Doador: `doador@doeja.example`
- Responsáveis: `maos.unidas@doeja.example`, `semear@doeja.example`, `ninho.de.luz@doeja.example`,
  `recomecar@doeja.example`, `vizinho.solidario@doeja.example`, `crianca.sorrindo@doeja.example`,
  `lar.acolher@doeja.example`, `mulheres.em.rede@doeja.example` (este último com o centro "em análise")

Para usar com instituições reais: `DEMO_ENABLED=false` e remova `classpath:db/seed` de `SPRING_FLYWAY_LOCATIONS`.

## Testes

```bash
docker compose --profile test run --rm backend-tests
docker compose --profile test down -v
```

Sobe um PostgreSQL descartável e roda **34 testes**:

- **Unitários:** distância geográfica (Haversine), leitura do `DATABASE_URL`, bloqueio de login por tentativas, regras de transição de status
  da doação, cálculo dos indicadores, normalização de CEP/UF.
- **Integração (API + banco real):** listagem pública só de aprovados, filtro por categoria, busca por proximidade,
  cadastro com validação e LGPD, e-mail duplicado, bloqueio após 5 senhas erradas, 401/403 por perfil, e o fluxo
  completo: centro cadastra → admin aprova → centro informa necessidades → doador doa → doador avisa entrega →
  centro confirma → indicador atualizado.

## Segurança e LGPD

- Senhas guardadas só como hash **BCrypt**; login com **JWT** (expira em 8 h), sem sessão no servidor.
- Perfis com permissões separadas; cada centro só enxerga e altera os próprios dados (acesso indevido responde 404).
- Login bloqueado por 15 min após 5 erros seguidos; mensagem de erro não revela se o e-mail existe.
- Validação de todos os dados de entrada; erros internos não expõem detalhes ao usuário.
- Proteção contra XSS no front (todo texto da API é escapado) e contra curingas em buscas.
- **LGPD:** consentimento registrado no cadastro (`lgpd_consentimento_em`); coleta mínima (nome, e-mail);
  o centro vê só o **nome** do doador, nunca o e-mail; estatísticas públicas são só números agregados.
- Banco e API não ficam expostos na rede (só `127.0.0.1`); segredos ficam no `.env`, fora do Git.

## API (resumo)

| Método e rota | Acesso | Descrição |
|---|---|---|
| `GET /api/centros?bairro=&cidade=&cep=&ativo=&categoria=&page=&size=` | público | Centros aprovados, com necessidades |
| `GET /api/centros/proximos?lat=&lng=&raioKm=&limite=` | público | Centros mais próximos, com distância |
| `GET /api/centros/{id}` | público | Detalhe de um centro aprovado |
| `GET /api/categorias` · `GET /api/estatisticas` · `GET /api/info` | público | Categorias, indicadores, modo demonstração |
| `POST /api/auth/cadastro` · `POST /api/auth/login` · `GET /api/auth/eu` | público / logado | Contas e login |
| `GET/POST /api/meus-centros` · `PUT /api/meus-centros/{id}` | CENTRO | Listar, cadastrar e editar os próprios centros |
| `PUT /api/meus-centros/{id}/necessidades` · `PATCH /api/meus-centros/{id}/ativo` | CENTRO | O que precisa; pausar/retomar |
| `POST /api/doacoes` · `GET /api/doacoes/minhas` | USUARIO | Registrar e acompanhar doações |
| `GET /api/doacoes/recebidas` | CENTRO | Doações para os próprios centros |
| `PATCH /api/doacoes/{id}/status` | doador ou centro | Entregue, confirmada ou cancelada |
| `GET /api/admin/centros?status=` · `PATCH /api/admin/centros/{id}/status` | ADMIN | Moderação de cadastros |

## Documentação do projeto de extensão

A pasta [`docs/`](docs/) tem o material de apoio para a entrega: conteúdo do relatório, roteiro de validação
com a instituição, protocolo de métricas antes/depois, roteiro do vídeo pitch e a tabela de planejamento.
