-- 05_consultas_avancadas.sql
-- ENTREGAVEL 2 DA ETAPA 04: quatro consultas novas.
--
--   1) anti join a esquerda
--   2) full outer join (emulado, porque o MySQL nao implementa FULL OUTER JOIN)
--   3) subconsulta escalar no WHERE
--   4) subconsulta no HAVING
--
-- Rode depois do 02_insercao.sql. Nenhuma delas altera dados.

SET NAMES utf8mb4;

USE concessionaria;

-- ===========================================================================
-- CONSULTA 1: ANTI JOIN A ESQUERDA
--
-- Pergunta de negocio: quais marcas nao tem nenhuma unidade em estoque, ou seja,
-- tiveram tudo vendido? Sao as candidatas naturais a um novo pedido de compra.
--
-- Tecnica: LEFT JOIN em que a tabela da direita nao deve ter correspondencia. O
-- teste "ca.chassi IS NULL" no WHERE e o que caracteriza o anti join.
--
-- O detalhe que quase todo mundo erra: a condicao "ca.venda_id IS NULL" fica no ON,
-- nao no WHERE. Se fosse para o WHERE, ela descartaria justamente as linhas em que
-- o carro virou NULL por nao existir, e a consulta voltaria a ser um INNER JOIN.
-- ===========================================================================
SELECT
    ma.id   AS marca_id,
    ma.nome AS marca
FROM marca ma
LEFT JOIN modelo mo
       ON mo.marca_id = ma.id
LEFT JOIN carro ca
       ON ca.modelo_id = mo.id
      AND ca.venda_id IS NULL
WHERE ca.chassi IS NULL
ORDER BY ma.nome;


-- ===========================================================================
-- CONSULTA 2: FULL OUTER JOIN
--
-- Pergunta de negocio: como o caixa se comporta mes a mes, comparando o que saiu em
-- compras de estoque com o que entrou em vendas?
--
-- Limitacao do MySQL: nao existe FULL OUTER JOIN. O resultado e montado somando as
-- duas metades com UNION (sem ALL, para nao duplicar os meses que aparecem nos dois
-- lados):
--   - LEFT JOIN  traz os meses que existem em compras, mesmo sem venda;
--   - RIGHT JOIN traz os meses que existem em vendas, mesmo sem compra.
--
-- O ganho e visivel no resultado: um INNER JOIN devolveria apenas os 4 meses em que
-- houve compra e venda; o full outer devolve os 7 meses do periodo, incluindo
-- janeiro (so compra) e fevereiro e marco (so venda).
--
-- O filtro de datas usa o indice idx_venda_data.
-- ===========================================================================
WITH compras_mes AS (
    SELECT DATE_FORMAT(data, '%Y-%m') AS mes,
           SUM(valor)                 AS total_compras,
           COUNT(*)                   AS qtd_compras
    FROM compra
    WHERE data BETWEEN '2023-01-01' AND '2023-07-31'
    GROUP BY DATE_FORMAT(data, '%Y-%m')
),
vendas_mes AS (
    SELECT DATE_FORMAT(data, '%Y-%m') AS mes,
           SUM(valor)                 AS total_vendas,
           COUNT(*)                   AS qtd_vendas
    FROM venda
    WHERE data BETWEEN '2023-01-01' AND '2023-07-31'
    GROUP BY DATE_FORMAT(data, '%Y-%m')
)
SELECT
    c.mes                                         AS mes,
    c.total_compras                               AS total_compras,
    c.qtd_compras                                 AS qtd_compras,
    COALESCE(v.total_vendas, 0)                   AS total_vendas,
    COALESCE(v.qtd_vendas, 0)                     AS qtd_vendas,
    COALESCE(v.total_vendas, 0) - c.total_compras AS saldo
FROM compras_mes c
LEFT JOIN vendas_mes v ON v.mes = c.mes
UNION
SELECT
    v.mes,
    COALESCE(c.total_compras, 0),
    COALESCE(c.qtd_compras, 0),
    v.total_vendas,
    v.qtd_vendas,
    v.total_vendas - COALESCE(c.total_compras, 0)
FROM compras_mes c
RIGHT JOIN vendas_mes v ON v.mes = c.mes
ORDER BY mes;


-- ===========================================================================
-- CONSULTA 3: SUBCONSULTA ESCALAR NO WHERE
--
-- Pergunta de negocio: quais carros parados no patio estao com preco acima da media
-- do proprio estoque? Sao o primeiro alvo de uma promocao, porque sao os que mais
-- capital imobilizado representam.
--
-- Tecnica: a subconsulta devolve um unico valor (a media do estoque) e e comparada
-- linha a linha pelo WHERE. Ela tambem filtra por venda_id IS NULL, entao usa o
-- indice idx_carro_estoque_modelo.
-- ===========================================================================
SELECT
    ca.chassi AS chassi,
    ma.nome   AS marca,
    mo.nome   AS modelo,
    ca.ano    AS ano,
    ca.preco  AS preco
FROM carro ca
JOIN modelo mo ON mo.id = ca.modelo_id
JOIN marca  ma ON ma.id = mo.marca_id
WHERE ca.venda_id IS NULL
  AND ca.preco > (SELECT AVG(preco) FROM carro WHERE venda_id IS NULL)
ORDER BY ca.preco DESC;


-- ===========================================================================
-- CONSULTA 4: SUBCONSULTA NO HAVING
--
-- Pergunta de negocio: quais fornecedores tem volume comprado acima da media dos
-- fornecedores? Serve para decidir onde negociar condicao especial.
--
-- Tecnica: o HAVING filtra o resultado da agregacao, e a subconsulta calcula a media
-- dos totais por fornecedor. A tabela derivada "t" e obrigatoria: nao se pode
-- aplicar AVG diretamente sobre o SUM do mesmo agrupamento na mesma consulta.
-- Esta e a unica consulta do projeto que usa HAVING.
-- ===========================================================================
SELECT
    f.CNPJ          AS cnpj,
    f.nome          AS fornecedor,
    COUNT(c.id)     AS compras,
    SUM(c.valor)    AS total_comprado
FROM fornecedor f
JOIN compra c
  ON c.fornecedor_CNPJ = f.CNPJ
GROUP BY f.CNPJ, f.nome
HAVING SUM(c.valor) > (
    SELECT AVG(t.total)
    FROM (
        SELECT SUM(valor) AS total
        FROM compra
        GROUP BY fornecedor_CNPJ
    ) t
)
ORDER BY total_comprado DESC;
