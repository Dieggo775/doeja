# Conteúdo do relatório final (modelo "MODELO - Relatório.pptx")

Textos prontos para colar em cada slide do modelo da UniFECAF. Onde aparece **[PREENCHER]** entra
informação que só existe depois da visita à instituição (fotos, assinaturas, depoimentos, números).
Esses campos não podem ser inventados. No fim, retire as instruções em amarelo/vermelho e exporte em PDF.

---

## Slide 1 – Capa

- Projeto de Extensão 2026
- Curso: **Análise e Desenvolvimento de Sistemas**
- Projeto: **DoeJá – conectando doadores a centros de doação em Taboão da Serra**
- Integrantes: 1. Diego Araujo – RA [PREENCHER]

## Slide 3 – 1.1 Justificativa sobre a temática escolhida

Escolheu-se a **Temática 1 – Digitalização e Automação de Processos Sociais** porque o problema observado é
operacional: a captação de doações das instituições sociais de Taboão da Serra depende de processos manuais
e informais (grupos de WhatsApp, posts em redes sociais, boca a boca e anotações em papel). Essa informação
desatualizada faz o doador não saber onde, quando e o que doar, e faz a instituição receber itens de que não
precisa enquanto falta o essencial. A temática inclui explicitamente "sistemas web, APIs" e "gestão de
cadastros e doações", que é exatamente o que um aluno de ADS pode entregar: um sistema web que centraliza o
cadastro das instituições, publica suas necessidades atualizadas, registra as doações do começo ao fim e gera
indicadores para a tomada de decisão.

## Slide 4 – 1.2 ODS identificados

Manter no slide: **ODS 1 – Erradicação da Pobreza** (principal). O DoeJá reduz a barreira entre quem quer
doar e as famílias atendidas pelas instituições, ampliando o acesso a alimentos, roupas e itens de higiene.

## Slide 5 – 2. A comunidade

- **Nome:** [PREENCHER: nome da instituição parceira]
- **Descrição:** doadores (moradores, comerciantes, igrejas) e instituições sociais de Taboão da Serra/SP que
  recebem e redistribuem doações para famílias em situação de vulnerabilidade. [PREENCHER: o perfil da
  instituição parceira: quem atende e há quanto tempo.]
- **Região:** Taboão da Serra, SP, com foco inicial nos bairros [PREENCHER: bairro da instituição e vizinhos].
- **Pessoas beneficiadas:** [PREENCHER: nº de famílias/pessoas atendidas pela instituição, segundo a entrevista]
  + doadores da região.

## Slides 6 a 8 – 2.1 Registros e evidências da comunidade

[PREENCHER com fotos reais da visita: fachada, estoque, reunião. Legenda em cada foto. Evitar o rosto de uma pessoa sozinha.]

## Slides 9 a 11 – 2.2 Carta de recomendação e termo de cessão

[PREENCHER: carta de recomendação assinada pela instituição e termo de cessão de imagem, escaneados.]

## Slide 12 – 3. As necessidades e os problemas

As instituições de Taboão da Serra dependem de doações, mas a comunicação com os doadores é informal e
fragmentada. Os pedidos ficam espalhados em grupos de WhatsApp e redes sociais e envelhecem rápido; o doador
não encontra com facilidade endereço, horário e contato, e não sabe o que está faltando naquele momento. A
instituição gasta tempo respondendo sempre as mesmas perguntas, recebe itens que não usa e não tem registro
organizado do que entrou, o que dificulta planejar e prestar contas. [PREENCHER: completar com frases e
números da entrevista.]

## Slide 13 – 3.1 Problema 1: informação de doação espalhada e desatualizada

Endereço, horário, contato e lista do que a instituição precisa não estão num lugar só, nem atualizados.
Consequência: o doador desiste ou leva o item errado. [PREENCHER: evidência, como print de grupo ou post
antigo, sem dados pessoais, e o relato da instituição.]

## Slide 14 – 3.2 Problema 2: falta de registro e de indicadores das doações

A instituição não sabe com precisão quanto recebeu, de quê, nem quanto do que foi prometido chegou.
Sem dados, não consegue planejar nem mostrar resultados a apoiadores. [PREENCHER: como registram hoje,
por exemplo caderno ou nada, com foto se autorizado.]

## Slide 15 – 4. Planejamento do projeto (escopo)

**Dentro do escopo:** sistema web responsivo com (1) mapa e busca de centros por bairro, CEP ou localização;
(2) cadastro de centros com análise pelo administrador; (3) lista do que cada centro precisa ou aceita, com
urgência; (4) registro e acompanhamento de doações (combinada → entregue → recebida); (5) painel de
indicadores de impacto; (6) segurança e LGPD (login, perfis, senhas criptografadas, consentimento).

**Fora do escopo:** logística de coleta e transporte, doações em dinheiro ou pagamentos, aplicativo nativo,
integração com sistemas da prefeitura.

## Slide 16 – 4.1 Proposta de melhoria e solução

O DoeJá é uma plataforma web gratuita, acessível pelo celular, que reúne num só mapa as instituições de doação
da cidade. Cada instituição mantém seu cadastro e informa, em poucos cliques, o que está precisando e o que é
urgente. O doador busca pelo bairro ou filtra pelo que quer doar ("roupas", "alimentos"...), vê horário,
WhatsApp e rota, e registra a doação. Quando entrega, a instituição confirma o recebimento, e esse registro
alimenta indicadores de impacto (itens por categoria, taxa de doações que chegam, tempo até a confirmação).
Para a comunidade, a mudança percebida é: o doador acerta o que levar e quando ir, e a instituição recebe o
que precisa e tem histórico do que entrou.

Tecnologias: Java 17 + Spring Boot (API REST, segurança com JWT), PostgreSQL com Flyway, HTML/CSS/JavaScript
com Leaflet e OpenStreetMap, Docker Compose; 28 testes automatizados.

## Slide 17 – 4.2 Tabela de planejamento

Colar a tabela de `docs/Tabela_de_Planejamento_DoeJa.xlsx`, atualizando a coluna "Data da realização".

## Slides 18 e 19 – 5. Execução

**Ações realizadas:**
1. Levantamento do problema e definição do MVP (abr/2026).
2. Desenvolvimento da 1ª versão: mapa, busca e cards de centros (abr/2026).
3. Infraestrutura em servidor com Docker, banco versionado com Flyway e segurança básica (out/2026).
4. Versão final (nov/2026): contas e perfis, cadastro e aprovação de centros, necessidades, registro de doações,
   painel de impacto e testes automatizados.
5. [PREENCHER: visita e validação com a instituição, com data, quem participou e o que foi feito.]
6. [PREENCHER: apresentação ou treinamento para a instituição.]

**Parcerias construídas:** [PREENCHER]
**Evidências:** prints do sistema (já disponíveis) + [PREENCHER: fotos e depoimentos].

## Slide 20 e 21 – 6. Plano de divulgação

Meios mais viáveis para a comunidade de Taboão da Serra:
- **QR Code impresso** no balcão e na porta das instituições parceiras, levando direto ao mapa.
- **Grupos de WhatsApp** das instituições e de voluntários (mensagem curta com o link).
- **Instagram/Facebook** das instituições parceiras, com post "Veja o que estamos precisando agora".
- **Igrejas, associações de moradores e comércios locais** (cartaz A4 com QR Code).
[PREENCHER: evidência, como foto do cartaz afixado ou print do post.]

## Slides 22 e 23 – 7. Resultados e impacto

Usar os números de `docs/02-METRICAS.md`. Estrutura sugerida:

- **Antes:** [PREENCHER: tempo médio para achar onde doar; horas/semana respondendo dúvidas; % de doações adequadas].
- **Depois:** [PREENCHER: os mesmos indicadores com o DoeJá + indicadores da tela Impacto].
- **Impacto social:** mais doações certas chegando a quem precisa (ODS 1).
- **Impacto econômico:** horas de voluntários economizadas e menos desperdício de itens não utilizados.
- **Depoimentos:** [PREENCHER].

## Slide 24 – 7.2 Como as ações podem continuar

O sistema fica no ar e é mantido pelas próprias instituições: cada uma atualiza suas necessidades sem depender
do aluno. Novas instituições se cadastram sozinhas e passam pela aprovação do administrador. O código é aberto
(GitHub) e documentado, o que permite que outros alunos ou voluntários continuem o projeto (próximos passos:
notificação de doações por WhatsApp/e-mail, relatórios mensais em PDF para a instituição e expansão para outros
bairros e cidades vizinhas).

## Slides 25 a 28 – 8. Avaliações

[PREENCHER com os resultados dos formulários de avaliação da comunidade e da autoavaliação (modelos UniFECAF).]

## Slide 30 – 9.1 Principais desafios

- Sair de uma lista simples para um fluxo completo com perfis diferentes (doador, centro, administrador)
  sem perder a simplicidade para quem não tem familiaridade com tecnologia.
- Segurança e privacidade: proteger senhas, impedir que um centro altere dados de outro e expor ao centro
  só o necessário do doador (LGPD).
- Mapa e localização sem custo: troca do Google Maps (pago, com chave exposta) por OpenStreetMap.
- Infraestrutura: subir banco, API e site com Docker num servidor e versionar o banco com migrations.
- [PREENCHER: desafios da parte com a comunidade, como agendar visitas e conseguir dados.]

## Slide 31 – 9.2 Habilidades desenvolvidas

**Técnicas:** Java/Spring Boot, API REST, Spring Security com JWT, PostgreSQL e Flyway, JavaScript, Leaflet,
Docker Compose, testes automatizados (JUnit e MockMvc), Git/GitHub, LGPD aplicada a sistemas.
**Comportamentais:** escuta ativa e levantamento de requisitos com a comunidade, comunicação com público não
técnico, organização e cumprimento de prazos, empatia com a realidade das instituições.

## Slide 32 – 9.3 Conclusão

O objetivo era facilitar o encontro entre quem quer doar e as instituições que precisam, em Taboão da Serra.
O DoeJá entrega isso: centraliza as instituições num mapa, mostra o que cada uma precisa agora, registra as
doações até a confirmação do recebimento e transforma esse registro em indicadores. [PREENCHER: retomar os
números da validação para evidenciar o resultado e encerrar com a sua consideração pessoal.]

## Slide 33 – 9.4 Retorno à comunidade

- Apresentação do sistema e treinamento rápido para os responsáveis da instituição.
- Entrega de um **guia de uso de uma página** e do cartaz com QR Code para divulgação.
- Compartilhamento dos indicadores do painel de impacto com a instituição, para uso em prestação de contas.
- [PREENCHER: data e evidência da devolutiva.]

## Slide 34 – Referências

- ONU Brasil. *Objetivos de Desenvolvimento Sustentável – ODS 1: Erradicação da pobreza.* Disponível em: https://brasil.un.org/pt-br/sdgs/1
- BRASIL. *Lei nº 13.709, de 14 de agosto de 2018 – Lei Geral de Proteção de Dados Pessoais (LGPD).*
- UniFECAF. *Ementa e Roteiro – Extensão Curricularizada Tech – Projeto Prático.* 2026.
- Spring. *Spring Boot Reference Documentation.* Disponível em: https://docs.spring.io/spring-boot/
- OpenStreetMap Foundation. *OpenStreetMap* e *Nominatim.* Disponível em: https://www.openstreetmap.org
- Leaflet. *Leaflet – an open-source JavaScript library for interactive maps.* Disponível em: https://leafletjs.com
