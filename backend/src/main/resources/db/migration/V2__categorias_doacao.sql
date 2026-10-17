-- V2: categorias fixas de doação (dados de referência, valem em qualquer ambiente)
INSERT INTO categorias_doacao (slug, nome, ordem) VALUES
    ('alimentos',        'Alimentos',                     1),
    ('roupas',           'Roupas e agasalhos',            2),
    ('higiene',          'Produtos de higiene e limpeza', 3),
    ('material-escolar', 'Material escolar',              4),
    ('infantil',         'Fraldas e itens infantis',      5),
    ('moveis',           'Móveis e eletrodomésticos',     6),
    ('brinquedos',       'Brinquedos',                    7),
    ('outros',           'Outros',                       99);
