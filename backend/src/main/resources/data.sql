-- DADOS FICTÍCIOS PARA DESENVOLVIMENTO.
-- Telefones e endereços não existem; coordenadas são aproximadas.
-- Substituir pelos dados do centro-piloto real quando ele for definido.
INSERT INTO centros_doacao
    (nome, descricao, endereco, bairro, cidade, estado, cep, latitude, longitude, telefone, horario_funcionamento, ativo)
VALUES
('Centro Comunitário Esperança (exemplo)', 'Recebe alimentos não perecíveis e itens de higiene.', 'Rua Exemplo, 100', 'Centro', 'Taboão da Serra', 'SP', '06750-000', -23.6256, -46.7918, '(11) 90000-0001', 'Seg a Sex, 9h às 17h', true),
('Igreja Exemplo Boa Esperança', 'Campanha permanente de roupas e agasalhos.', 'Avenida Exemplo, 250', 'Jardim Maria Rosa', 'Taboão da Serra', 'SP', '06750-010', -23.6122, -46.7805, '(11) 90000-0002', 'Ter a Sáb, 10h às 16h', true),
('Casa de Acolhimento Exemplo Luz', 'Aceita alimentos, fraldas e material escolar.', 'Rua Exemplo, 45', 'Santa Tereza', 'Taboão da Serra', 'SP', '06750-020', -23.6330, -46.7760, '(11) 90000-0003', 'Seg a Sex, 8h às 18h', true),
('Ponto de Arrecadação Exemplo Solidário', 'Ponto de entrega de cestas básicas.', 'Praça Exemplo, 10', 'Centro', 'Taboão da Serra', 'SP', '06750-030', -23.6228, -46.7890, '(11) 90000-0004', 'Seg a Sáb, 9h às 15h', true),
('Associação Exemplo Vizinhos (inativa)', 'Cadastro inativo, usado para testar o filtro de status.', 'Rua Exemplo, 77', 'Santa Tereza', 'Taboão da Serra', 'SP', '06750-040', -23.6301, -46.7802, '(11) 90000-0005', 'Seg a Sex, 8h às 12h', false);
