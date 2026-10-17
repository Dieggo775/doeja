-- DADOS FICTÍCIOS APENAS PARA DESENVOLVIMENTO/HOMOLOGAÇÃO.
-- Migration repetível (R__): roda de novo sempre que este arquivo mudar.
-- Por isso cada INSERT é idempotente (não duplica se já existir).
-- Telefones e endereços não existem; coordenadas são aproximadas.
-- Em produção, a pasta db/seed NÃO deve ser carregada (veja SPRING_FLYWAY_LOCATIONS).

INSERT INTO centros_doacao
    (nome, descricao, endereco, bairro, cidade, estado, cep,
     latitude, longitude, telefone, whatsapp, horario_funcionamento, ativo, status)
SELECT v.nome, v.descricao, v.endereco, v.bairro, v.cidade, v.estado, v.cep,
       v.latitude, v.longitude, v.telefone, v.whatsapp, v.horario, v.ativo, v.status
FROM (VALUES
    ('Centro Comunitário Esperança (exemplo)', 'Recebe alimentos não perecíveis e itens de higiene.',
     'Rua Exemplo, 100', 'Centro', 'Taboão da Serra', 'SP', '06750-000',
     -23.6256, -46.7918, '(11) 90000-0001', '5511900000001', 'Seg a Sex, 9h às 17h', true, 'APROVADO'),
    ('Igreja Exemplo Boa Esperança', 'Campanha permanente de roupas e agasalhos.',
     'Avenida Exemplo, 250', 'Jardim Maria Rosa', 'Taboão da Serra', 'SP', '06750-010',
     -23.6122, -46.7805, '(11) 90000-0002', '5511900000002', 'Ter a Sáb, 10h às 16h', true, 'APROVADO'),
    ('Casa de Acolhimento Exemplo Luz', 'Aceita alimentos, fraldas e material escolar.',
     'Rua Exemplo, 45', 'Santa Tereza', 'Taboão da Serra', 'SP', '06750-020',
     -23.6330, -46.7760, '(11) 90000-0003', '5511900000003', 'Seg a Sex, 8h às 18h', true, 'APROVADO'),
    ('Ponto de Arrecadação Exemplo Solidário', 'Ponto de entrega de cestas básicas.',
     'Praça Exemplo, 10', 'Centro', 'Taboão da Serra', 'SP', '06750-030',
     -23.6228, -46.7890, '(11) 90000-0004', '5511900000004', 'Seg a Sáb, 9h às 15h', true, 'APROVADO'),
    ('Associação Exemplo Vizinhos (inativa)', 'Cadastro inativo, usado para testar o filtro de status.',
     'Rua Exemplo, 77', 'Santa Tereza', 'Taboão da Serra', 'SP', '06750-040',
     -23.6301, -46.7802, '(11) 90000-0005', '5511900000005', 'Seg a Sex, 8h às 12h', false, 'APROVADO')
) AS v(nome, descricao, endereco, bairro, cidade, estado, cep,
       latitude, longitude, telefone, whatsapp, horario, ativo, status)
WHERE NOT EXISTS (SELECT 1 FROM centros_doacao c WHERE c.nome = v.nome);

INSERT INTO necessidades_centro (centro_id, categoria_id, tipo, descricao, urgente)
SELECT c.id, k.id, v.tipo, v.descricao, v.urgente
FROM (VALUES
    ('Centro Comunitário Esperança (exemplo)', 'alimentos', 'PRECISA', 'Arroz, feijão e óleo',          true),
    ('Centro Comunitário Esperança (exemplo)', 'higiene',   'ACEITA',  'Sabonete e pasta de dente',     false),
    ('Igreja Exemplo Boa Esperança',           'roupas',    'PRECISA', 'Agasalhos para o inverno',      true),
    ('Casa de Acolhimento Exemplo Luz',        'alimentos', 'ACEITA',  'Alimentos não perecíveis',      false),
    ('Casa de Acolhimento Exemplo Luz',        'infantil',  'PRECISA', 'Fraldas tamanhos M e G',        true),
    ('Casa de Acolhimento Exemplo Luz',        'material-escolar', 'ACEITA', 'Cadernos e lápis',        false),
    ('Ponto de Arrecadação Exemplo Solidário', 'alimentos', 'PRECISA', 'Itens para cestas básicas',     false)
) AS v(centro, categoria, tipo, descricao, urgente)
JOIN centros_doacao   c ON c.nome = v.centro
JOIN categorias_doacao k ON k.slug = v.categoria
ON CONFLICT (centro_id, categoria_id) DO NOTHING;
