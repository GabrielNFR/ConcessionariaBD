-- 03_consultas.sql
-- As 7 consultas analiticas do projeto. Rode depois do 02_insercao.sql.
--
-- Cobre, de proposito: JOIN, varios JOINs encadeados, GROUP BY com COUNT/SUM/AVG,
-- MIN/MAX, subconsulta (consulta 4), LEFT JOIN (consulta 7) e DATE_FORMAT (consulta 6).

SET NAMES utf8mb4;

USE concessionaria;

-- CONSULTA 1

-- Mostra quem comprou, quem vendeu, qual carro foi vendido, a marca, a cor, a data e o valor da venda.
SELECT
    v.id AS venda_id,
    v.data AS data_venda,
    c.CPF,
    c.nome AS cliente,
    vd.nome AS vendedor,
    ca.chassi,
    mo.nome AS modelo,
    ma.nome AS marca,
    co.nome AS cor,
    v.valor AS valor_venda
FROM venda v
JOIN cliente c
    ON c.CPF = v.cliente_CPF
JOIN vendedor vd
    ON vd.id = v.vendedor_id
JOIN carro ca
    ON ca.venda_id = v.id
JOIN modelo mo
    ON mo.id = ca.modelo_id
JOIN marca ma
    ON ma.id = mo.marca_id
JOIN cor co
    ON co.id = ca.cor_id
ORDER BY v.data, v.id;


-- CONSULTA 2

-- Mostra quantidade de vendas, faturamento e ticket medio de cada vendedor.
SELECT
    vd.id AS vendedor_id,
    vd.nome AS vendedor,
    COUNT(v.id) AS quantidade_vendas,
    SUM(v.valor) AS faturamento_total,
    AVG(v.valor) AS ticket_medio
FROM vendedor vd
JOIN venda v
    ON v.vendedor_id = vd.id
GROUP BY vd.id, vd.nome
ORDER BY faturamento_total DESC;


-- CONSULTA 3

-- Mostra quanto foi comprado de cada fornecedor e o valor medio das compras.
SELECT
    f.CNPJ,
    f.nome AS fornecedor,
    COUNT(co.id) AS quantidade_compras,
    SUM(co.valor) AS valor_total_compras,
    AVG(co.valor) AS valor_medio_compra
FROM fornecedor f
JOIN compra co
    ON co.fornecedor_CNPJ = f.CNPJ
GROUP BY f.CNPJ, f.nome
ORDER BY valor_total_compras DESC;


-- CONSULTA 4

-- Lista as vendas cujo valor esta acima da media de todas as vendas cadastradas.
SELECT
    v.id AS venda_id,
    v.data AS data_venda,
    c.nome AS cliente,
    vd.nome AS vendedor,
    v.valor
FROM venda v
JOIN cliente c
    ON c.CPF = v.cliente_CPF
JOIN vendedor vd
    ON vd.id = v.vendedor_id
WHERE v.valor > (
    SELECT AVG(valor)
    FROM venda
)
ORDER BY v.valor DESC;


-- CONSULTA 5

-- Pode ser usada em um grafico para comparar a quantidade de carros e os precos por marca.
SELECT
    ma.id AS marca_id,
    ma.nome AS marca,
    COUNT(ca.chassi) AS quantidade_carros,
    AVG(ca.preco) AS preco_medio,
    MIN(ca.preco) AS menor_preco,
    MAX(ca.preco) AS maior_preco
FROM marca ma
JOIN modelo mo
    ON mo.marca_id = ma.id
JOIN carro ca
    ON ca.modelo_id = mo.id
GROUP BY ma.id, ma.nome
ORDER BY quantidade_carros DESC, preco_medio DESC;


-- CONSULTA 6

-- Pode ser usada diretamente como fonte de dados para um grafico de faturamento mensal.
SELECT
    DATE_FORMAT(data, '%Y-%m') AS mes,
    COUNT(id) AS quantidade_vendas,
    SUM(valor) AS faturamento
FROM venda
GROUP BY DATE_FORMAT(data, '%Y-%m')
ORDER BY mes;


-- CONSULTA 7

-- O LEFT JOIN faz com que clientes sem telefone tambem aparecam no resultado.
SELECT
    c.CPF,
    c.nome AS cliente,
    COUNT(tc.telefone) AS quantidade_telefones
FROM cliente c
LEFT JOIN telefone_cliente tc
    ON tc.cliente_CPF = c.CPF
GROUP BY c.CPF, c.nome
ORDER BY quantidade_telefones DESC, c.nome;
