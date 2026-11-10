-- 04_indices.sql
-- ENTREGAVEL 1 DA ETAPA 04: os indices novos.
--
-- Rode depois do 02_insercao.sql. Criar indice com os dados ja carregados e mais
-- rapido do que manter o indice atualizado durante cada INSERT.
--
-- Estes indices nao se sobrepoem ao que o InnoDB ja cria sozinho:
--   - chaves primarias de todas as tabelas;
--   - um indice por chave estrangeira (compra.fornecedor_CNPJ, modelo.marca_id,
--     cliente.endereco_id, venda.vendedor_id, venda.cliente_CPF, carro.cor_id,
--     carro.modelo_id, carro.compra_id e carro.venda_id);
--   - os UNIQUE de fornecedor.email e cor.nome.
--
-- O script pode ser executado mais de uma vez. O MySQL nao tem CREATE INDEX
-- IF NOT EXISTS, entao cada indice e criado dentro de um bloco que consulta o
-- information_schema antes: se o indice ja existir, o comando vira um DO 0.

SET NAMES utf8mb4;

USE concessionaria;

-- ---------------------------------------------------------------------------
-- INDICE 1: venda (data)
--
-- Onde e usado:
--   - consulta 2 deste etapa (fluxo mensal, com faixa de datas);
--   - consulta 6 da Etapa 03 (faturamento mes a mes);
--   - o historico de vendas da interface, que ordena por data decrescente.
--
-- Por que e realmente necessario: toda analise temporal de venda filtra ou ordena
-- por data, e essa coluna nao tinha indice nenhum. Sem ele, um filtro por periodo
-- obriga o MySQL a percorrer a tabela inteira para depois descartar o que esta fora
-- da faixa. Com ele, o banco le apenas o intervalo pedido.
-- ---------------------------------------------------------------------------

SET @sql := IF((SELECT COUNT(*) FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'venda'
                   AND index_name = 'idx_venda_data') = 0,
               'CREATE INDEX idx_venda_data ON venda (data)',
               'DO 0');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- INDICE 2: carro (venda_id, modelo_id) - composto, nesta ordem
--
-- Onde e usado:
--   - consulta 1 (anti join de marcas sem estoque);
--   - consulta 3 (subconsulta de carros acima da media do estoque);
--   - view vw_estoque_por_marca_modelo, que filtra por venda_id e agrupa por modelo.
--
-- Por que e realmente necessario: "carro em estoque" e sempre venda_id IS NULL, e o
-- relatorio seguinte agrupa por modelo. Um indice apenas em venda_id ja existe por
-- causa da chave estrangeira, e ele resolveria o filtro, mas nao a ordenacao do
-- GROUP BY, que cairia numa tabela temporaria. O indice composto resolve as duas
-- coisas: filtra pelo NULL na primeira coluna e entrega modelo_id ja ordenado na
-- segunda.
-- ---------------------------------------------------------------------------

SET @sql := IF((SELECT COUNT(*) FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = 'carro'
                   AND index_name = 'idx_carro_estoque_modelo') = 0,
               'CREATE INDEX idx_carro_estoque_modelo ON carro (venda_id, modelo_id)',
               'DO 0');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

