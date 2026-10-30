-- 06_visoes.sql
-- ENTREGAVEL 3 DA ETAPA 04: duas visoes.
--
-- Uma view no MySQL nao guarda dados: e uma consulta salva. O banco executa o SELECT
-- no momento em que ela e consultada, por isso o resultado acompanha o estado atual
-- das tabelas e nunca fica desatualizado.
--
-- Rode depois do 02_insercao.sql. O DROP VIEW IF EXISTS no inicio torna o script
-- re-executavel.

SET NAMES utf8mb4;

USE concessionaria;

DROP VIEW IF EXISTS vw_venda_detalhada;
DROP VIEW IF EXISTS vw_estoque_por_marca_modelo;

-- ===========================================================================
-- VIEW 1: vw_venda_detalhada  (7 joins)
--
-- Justificativa semantica: no dominio da concessionaria, uma venda isolada nao
-- responde nada. Ela so faz sentido junto com quem comprou (e de onde essa pessoa
-- e), quem vendeu, qual carro foi entregue, de qual marca, modelo e cor. Essa juncao
-- de 7 tabelas ja existe hoje escrita a mao na consulta 1 da Etapa 03, e teria de ser
-- repetida em cada relatorio novo de vendas.
--
-- A view transforma essa juncao em uma "tabela" unica, com uma linha por carro
-- vendido, e passa a ser a fonte natural para relatorios por vendedor, por cliente,
-- por cidade, por marca ou por mes.
--
-- Observacao de modelagem: como a granularidade e "carro vendido", uma venda com dois
-- carros aparece em duas linhas. Por isso, ao contar vendas a partir desta view, o
-- correto e COUNT(DISTINCT venda_id).
-- ===========================================================================
CREATE OR REPLACE VIEW vw_venda_detalhada AS
SELECT
    v.id                         AS venda_id,
    v.data                       AS data_venda,
    DATE_FORMAT(v.data, '%Y-%m') AS mes,
    v.valor                      AS valor_venda,
    c.CPF                        AS cliente_cpf,
    c.nome                       AS cliente,
    en.cidade                    AS cliente_cidade,
    en.estado                    AS cliente_uf,
    vd.nome                      AS vendedor,
    ca.chassi                    AS chassi,
    ma.nome                      AS marca,
    mo.nome                      AS modelo,
    co.nome                      AS cor,
    ca.ano                       AS ano,
    ca.preco                     AS preco_carro
FROM venda v
JOIN cliente  c  ON c.CPF      = v.cliente_CPF
JOIN endereco en ON en.id      = c.endereco_id
JOIN vendedor vd ON vd.id      = v.vendedor_id
JOIN carro    ca ON ca.venda_id = v.id
JOIN modelo   mo ON mo.id      = ca.modelo_id
JOIN marca    ma ON ma.id      = mo.marca_id
JOIN cor      co ON co.id      = ca.cor_id;

-- ===========================================================================
-- VIEW 2: vw_estoque_por_marca_modelo  (3 joins + agregacao)
--
-- Justificativa semantica: o estoque e o capital parado da concessionaria. A gestao
-- precisa saber, por marca e modelo, quantas unidades estao disponiveis, qual a
-- faixa de preco praticada e quanto dinheiro isso representa.
--
-- Sao 3 joins (carro, modelo e marca) e uma agregacao por marca e modelo. E a versao
-- persistente do relatorio 2 da interface, disponivel para qualquer consulta e para
-- o DBeaver sem precisar reescrever o SQL.
--
-- O filtro "venda_id IS NULL" usa o indice idx_carro_estoque_modelo.
-- ===========================================================================
CREATE OR REPLACE VIEW vw_estoque_por_marca_modelo AS
SELECT
    ma.id                   AS marca_id,
    ma.nome                 AS marca,
    mo.id                   AS modelo_id,
    mo.nome                 AS modelo,
    COUNT(ca.chassi)        AS carros_em_estoque,
    MIN(ca.preco)           AS menor_preco,
    MAX(ca.preco)           AS maior_preco,
    ROUND(AVG(ca.preco), 2) AS preco_medio,
    SUM(ca.preco)           AS valor_total_estoque
FROM carro ca
JOIN modelo mo ON mo.id = ca.modelo_id
JOIN marca  ma ON ma.id = mo.marca_id
WHERE ca.venda_id IS NULL
GROUP BY ma.id, ma.nome, mo.id, mo.nome;

-- ===========================================================================
-- Exemplos de uso, para mostrar na apresentacao que a view se comporta como tabela:
--
--   -- O estoque mais valioso, direto da view
--   SELECT marca, modelo, carros_em_estoque, preco_medio, valor_total_estoque
--   FROM vw_estoque_por_marca_modelo
--   ORDER BY valor_total_estoque DESC;
--
--   -- As 5 vendas de maior valor com todos os dados da venda
--   SELECT venda_id, data_venda, cliente, cliente_uf, vendedor, marca, modelo, cor, valor_venda
--   FROM vw_venda_detalhada
--   ORDER BY valor_venda DESC
--   LIMIT 5;
--
--   -- Faturamento por UF do cliente, sem repetir nenhum join
--   SELECT cliente_uf, COUNT(DISTINCT venda_id) AS vendas, SUM(valor_venda) AS faturamento
--   FROM vw_venda_detalhada
--   GROUP BY cliente_uf
--   ORDER BY faturamento DESC;
-- ===========================================================================
