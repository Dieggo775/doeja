# Publicação na internet: Vercel + Render + Neon

```
Navegador ──> Vercel (site: pasta frontend/) ──/api/*──> Render (API Java: pasta backend/) ──> Neon (PostgreSQL)
```

Tudo nos planos gratuitos. A Vercel repassa `/api/*` para o Render (`frontend/vercel.json`), então o
navegador fala com um endereço só e não há configuração de CORS.

> No plano grátis do Render a API "dorme" após 15 min sem uso. A primeira visita depois disso leva
> cerca de 1 minuto: o site mostra "Conectando ao servidor..." e tenta de novo sozinho.
> Antes de apresentar, abra o site uma vez para acordar a API.

## 1. Banco de dados no Neon

1. Entre em https://neon.tech (pode usar a conta do GitHub) e crie um projeto:
   - Nome: `doeja`, Postgres 16, região **AWS US East 1 (N. Virginia)** (perto do Render).
2. Em **Connection string**, copie o endereço que começa com `postgresql://...` (com `sslmode=require`).
   Ele contém a senha do banco: não publique nem coloque no Git.

## 2. API no Render

1. Entre em https://render.com com a conta do GitHub.
2. **New > Blueprint** e escolha o repositório `doeja`. O Render lê o `render.yaml` e cria o serviço `doeja-api`.
3. Preencha os campos pedidos:
   - `DATABASE_URL`: o endereço copiado do Neon.
   - `ADMIN_EMAIL` e `ADMIN_SENHA`: a conta de administrador (senha com 8+ caracteres).
   - `DEMO_SENHA`: senha das contas de demonstração (8+ caracteres).
   - `JWT_SECRET` é gerado sozinho.
4. Aguarde o deploy (o primeiro leva de 5 a 10 min). Teste em `https://doeja-api.onrender.com/actuator/health`,
   que deve responder `{"status":"UP"}`.
5. Se o Render der outro endereço (por exemplo, `doeja-api-xyz.onrender.com`), troque-o em `frontend/vercel.json`.

## 3. Site na Vercel

1. Entre em https://vercel.com, **Add New > Project** e importe o repositório `doeja`.
2. Em **Root Directory** escolha `frontend`. Framework: **Other**. Sem comando de build.
3. **Deploy**. O site fica em `https://<nome-do-projeto>.vercel.app`.

Pelo terminal, a alternativa é:

```bash
cd frontend
npx vercel@latest login
npx vercel@latest --prod
```

## 4. Conferência

- A página inicial mostra os centros no mapa e a faixa "Ambiente de demonstração".
- Entrar com `ADMIN_EMAIL` abre a Administração.
- Entrar com `maos.unidas@doeja.example` (senha `DEMO_SENHA`) abre "Meu centro".

## Atualizações

Cada `git push` na `main` atualiza o Render e a Vercel automaticamente.

## Quando houver instituições reais

No Render, mude `DEMO_ENABLED` para `false` e `SPRING_FLYWAY_LOCATIONS` para `classpath:db/migration`.
Num banco novo, os centros fictícios não são criados. Num banco que já os tem, eles precisam ser
removidos pela Administração (recusar) ou direto no banco.
