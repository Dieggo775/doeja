-- =====================================================================
-- DADOS DE DEMONSTRAÇÃO — INSTITUIÇÕES FICTÍCIAS
-- =====================================================================
-- Nomes de instituições, números de imóvel e telefones são INVENTADOS.
-- Ruas, bairros, CEPs e coordenadas são reais (OpenStreetMap), para o mapa
-- ficar realista. Quando houver instituições parceiras de verdade, troque
-- estes registros pelos dados reais (com autorização da instituição).
--
-- Telefones são fictícios (padrão (11) 90000-01xx); não ligue nem mande mensagem para eles.
-- Migration repetível (R__): roda de novo sempre que este arquivo mudar,
-- por isso cada comando é idempotente (não duplica nada).
-- Em produção, a pasta db/seed NÃO deve ser carregada (veja SPRING_FLYWAY_LOCATIONS).
-- =====================================================================

-- Remove os registros genéricos da versão anterior do seed ("Exemplo").
DELETE FROM centros_doacao
WHERE nome IN ('Centro Comunitário Esperança (exemplo)', 'Igreja Exemplo Boa Esperança',
               'Casa de Acolhimento Exemplo Luz', 'Ponto de Arrecadação Exemplo Solidário',
               'Associação Exemplo Vizinhos (inativa)')
  AND NOT EXISTS (SELECT 1 FROM doacoes d WHERE d.centro_id = centros_doacao.id);

INSERT INTO centros_doacao
    (nome, descricao, endereco, bairro, cidade, estado, cep,
     latitude, longitude, telefone, whatsapp, horario_funcionamento, ativo, status)
SELECT v.nome, v.descricao, v.endereco, v.bairro, 'Taboão da Serra', 'SP', v.cep,
       v.latitude, v.longitude, v.telefone, v.whatsapp, v.horario, v.ativo, v.status
FROM (VALUES
    ('Associação Comunitária Mãos Unidas',
     'Associação de moradores que monta cestas básicas para cerca de 120 famílias cadastradas do bairro.',
     'Avenida Vida Nova, 1180', 'Jardim Maria Rosa', '06763-015', -23.61085, -46.76793,
     '(11) 90000-0101', '5511900000101', 'Seg a Sex, 9h às 17h; Sáb, 9h às 12h', true, 'APROVADO'),

    ('Comunidade Cristã Semear',
     'Bazar solidário e campanha permanente de agasalhos e cobertores para pessoas em situação de rua.',
     'Rua Cesário Dau, 455', 'Jardim Bontempo', '06763-270', -23.61035, -46.76084,
     '(11) 90000-0102', '5511900000102', 'Ter a Sáb, 10h às 16h', true, 'APROVADO'),

    ('Casa de Acolhimento Ninho de Luz',
     'Acolhimento institucional de crianças de 0 a 12 anos. Recebe fraldas, leite e material escolar.',
     'Avenida Laurita Ortega Mari, 820', 'Parque Pinheiros', '06767-220', -23.61705, -46.77976,
     '(11) 90000-0103', '5511900000103', 'Seg a Sex, 8h às 18h', true, 'APROVADO'),

    ('Instituto Recomeçar Taboão',
     'Apoia famílias que perderam tudo em enchentes e despejos com móveis, utensílios e roupas.',
     'Avenida Aprígio Bezerra da Silva, 1530', 'Chácara Marapuí', '06763-015', -23.60888, -46.76673,
     '(11) 90000-0104', '5511900000104', 'Seg a Sex, 9h às 16h (retirada de móveis com agendamento)', true, 'APROVADO'),

    ('Banco de Alimentos Vizinho Solidário',
     'Recebe e redistribui alimentos para cozinhas comunitárias da região.',
     'Estrada São Francisco, 2240', 'Jardim Helena', '06754-000', -23.61817, -46.77543,
     '(11) 90000-0105', '5511900000105', 'Seg a Sáb, 8h às 14h', true, 'APROVADO'),

    ('Projeto Criança Sorrindo',
     'Contraturno escolar com reforço, esporte e lanche para 80 crianças de 6 a 14 anos.',
     'Avenida Ibirama, 310', 'Jardim São Judas Tadeu', '06785-290', -23.63550, -46.80983,
     '(11) 90000-0106', '5511900000106', 'Seg a Sex, 13h às 18h', true, 'APROVADO'),

    ('Lar Acolher - Residência para Idosos',
     'Instituição de longa permanência para idosos (ILPI) com 28 residentes.',
     'Rua José Milani, 140', 'Parque Marabá', '06767-170', -23.62357, -46.77960,
     '(11) 90000-0107', '5511900000107', 'Todos os dias, 9h às 17h', true, 'APROVADO'),

    -- Cadastro aguardando análise: usado para demonstrar a aprovação pelo administrador
    ('Coletivo Mulheres em Rede',
     'Rede de apoio a mães solo: kits de higiene, enxoval de bebê e cestas básicas.',
     'Avenida Armando de Andrade, 600', 'Parque Assunção', '06754-210', -23.60588, -46.75943,
     '(11) 90000-0108', '5511900000108', 'Qua a Sex, 14h às 19h', true, 'PENDENTE'),

    -- Aprovado, mas pausado: usado para demonstrar o filtro "Inativos"
    ('Ponto de Coleta Escola Viva',
     'Ponto de coleta em escola, pausado durante as férias escolares.',
     'Avenida Paulo Ayres, 95', 'Parque Pinheiros', '06767-230', -23.62042, -46.78533,
     '(11) 90000-0109', '5511900000109', 'Seg a Sex, 7h às 12h (durante o ano letivo)', false, 'APROVADO')
) AS v(nome, descricao, endereco, bairro, cep, latitude, longitude, telefone, whatsapp, horario, ativo, status)
WHERE NOT EXISTS (SELECT 1 FROM centros_doacao c WHERE c.nome = v.nome);

INSERT INTO necessidades_centro (centro_id, categoria_id, tipo, descricao, urgente)
SELECT c.id, k.id, v.tipo, v.descricao, v.urgente
FROM (VALUES
    ('Associação Comunitária Mãos Unidas', 'alimentos',        'PRECISA', 'Arroz, feijão, óleo e leite em pó para as cestas do mês', true),
    ('Associação Comunitária Mãos Unidas', 'higiene',          'ACEITA',  'Sabonete, pasta de dente e papel higiênico',             false),
    ('Comunidade Cristã Semear',           'roupas',           'PRECISA', 'Agasalhos e cobertores (adulto)',                        true),
    ('Comunidade Cristã Semear',           'outros',           'ACEITA',  'Calçados em bom estado',                                 false),
    ('Casa de Acolhimento Ninho de Luz',   'infantil',         'PRECISA', 'Fraldas tamanhos M, G e XG',                             true),
    ('Casa de Acolhimento Ninho de Luz',   'alimentos',        'ACEITA',  'Leite integral e achocolatado',                          false),
    ('Casa de Acolhimento Ninho de Luz',   'material-escolar', 'ACEITA',  'Cadernos, lápis de cor e mochilas',                      false),
    ('Instituto Recomeçar Taboão',         'moveis',           'PRECISA', 'Colchões, fogões e geladeiras funcionando',              true),
    ('Instituto Recomeçar Taboão',         'roupas',           'ACEITA',  'Roupas de cama e banho',                                 false),
    ('Banco de Alimentos Vizinho Solidário','alimentos',       'PRECISA', 'Alimentos não perecíveis dentro da validade',            false),
    ('Banco de Alimentos Vizinho Solidário','higiene',         'ACEITA',  'Produtos de limpeza',                                    false),
    ('Projeto Criança Sorrindo',           'material-escolar', 'PRECISA', 'Cadernos, estojos e livros infantis',                    true),
    ('Projeto Criança Sorrindo',           'brinquedos',       'ACEITA',  'Jogos educativos e bolas',                               false),
    ('Lar Acolher - Residência para Idosos','higiene',         'PRECISA', 'Fraldas geriátricas G e EG, lenços umedecidos',          true),
    ('Lar Acolher - Residência para Idosos','alimentos',       'ACEITA',  'Leite, biscoitos sem açúcar e frutas',                   false),
    ('Coletivo Mulheres em Rede',          'infantil',         'PRECISA', 'Enxoval de bebê e fraldas RN e P',                       true),
    ('Coletivo Mulheres em Rede',          'higiene',          'ACEITA',  'Absorventes e kits de higiene pessoal',                  false),
    ('Ponto de Coleta Escola Viva',        'material-escolar', 'ACEITA',  'Material escolar em geral',                              false)
) AS v(centro, categoria, tipo, descricao, urgente)
JOIN centros_doacao    c ON c.nome = v.centro
JOIN categorias_doacao k ON k.slug = v.categoria
ON CONFLICT (centro_id, categoria_id) DO NOTHING;
