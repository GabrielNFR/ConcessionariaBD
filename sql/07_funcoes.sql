-- 07_funcoes.sql
-- ENTREGAVEL 1 DA ETAPA 05: duas funcoes com justificativa semantica.
--
-- Funcao no MySQL devolve um unico valor e pode ser usada dentro de SELECT, WHERE,
-- ORDER BY e em views. Diferente do procedimento, ela nao e chamada com CALL.
--
-- Rode depois do 02_insercao.sql, porque as duas leem dados das tabelas.
-- O DROP antes do CREATE deixa o script re-executavel.
--
-- O DELIMITER e necessario: sem ele o cliente mysql encerraria o comando no primeiro
-- ponto e virgula de dentro do BEGIN ... END.

SET NAMES utf8mb4;

USE concessionaria;

DROP FUNCTION IF EXISTS fn_perfil_cliente;
DROP FUNCTION IF EXISTS fn_valor_estoque_marca;

DELIMITER $$

-- ===========================================================================
-- FUNCAO 1: fn_perfil_cliente(cpf) -> VARCHAR  (USA ESTRUTURA CONDICIONAL)
--
-- Justificativa semantica: a concessionaria precisa segmentar a carteira para
-- campanhas. "Cliente que comprou uma vez" nao deve receber a mesma acao de
-- marketing de "cliente que ja comprou varias vezes e movimenta volume alto".
-- A funcao traduz o historico de compras em um rotulo unico de perfil.
--
-- Estrutura condicional: IF / ELSEIF / ELSE, que e o requisito da disciplina.
-- A leitura e declarada com READS SQL DATA: a funcao consulta, mas nao altera nada.
-- ===========================================================================
CREATE FUNCTION fn_perfil_cliente(p_cpf CHAR(11))
RETURNS VARCHAR(20)
READS SQL DATA
BEGIN
    DECLARE v_compras INT           DEFAULT 0;
    DECLARE v_total   DECIMAL(12,2) DEFAULT 0.00;

    SELECT COUNT(*), COALESCE(SUM(valor), 0)
      INTO v_compras, v_total
      FROM venda
     WHERE cliente_CPF = p_cpf;

    IF v_compras = 0 THEN
        RETURN 'SEM COMPRA';
    ELSEIF v_compras = 1 THEN
        RETURN 'NOVO';
    ELSEIF v_compras >= 4 OR v_total >= 150000 THEN
        RETURN 'VIP';
    ELSE
        RETURN 'RECORRENTE';
    END IF;
END$$

-- ===========================================================================
-- FUNCAO 2: fn_valor_estoque_marca(marca_id) -> DECIMAL
--
-- Justificativa semantica: o estoque e o capital imobilizado da concessionaria. A
-- pergunta "quanto dinheiro esta parado nesta marca?" aparece em toda decisao de
-- compra e nao e respondida por nenhuma coluna da tabela: e preciso somar o preco
-- dos carros daquela marca que ainda nao foram vendidos.
--
-- A funcao devolve um valor unico, pronto para ser usado em ORDER BY e em relatorio.
-- ===========================================================================
CREATE FUNCTION fn_valor_estoque_marca(p_marca_id INT)
RETURNS DECIMAL(12,2)
READS SQL DATA
BEGIN
    DECLARE v_total DECIMAL(12,2) DEFAULT 0.00;

    SELECT COALESCE(SUM(ca.preco), 0)
      INTO v_total
      FROM carro ca
      JOIN modelo mo ON mo.id = ca.modelo_id
     WHERE mo.marca_id = p_marca_id
       AND ca.venda_id IS NULL;

    RETURN v_total;
END$$

DELIMITER ;

-- ===========================================================================
-- Demonstracao 1: distribuicao da carteira por perfil
-- ===========================================================================
SELECT fn_perfil_cliente(CPF) AS perfil,
       COUNT(*)               AS clientes
FROM cliente
GROUP BY perfil
ORDER BY clientes DESC;

-- Caso em que o IF cai no primeiro ramo: CPF sem nenhuma compra
SELECT fn_perfil_cliente('99999999999') AS perfil_de_quem_nunca_comprou;

-- ===========================================================================
-- Demonstracao 2: as 5 marcas com mais capital parado em estoque
-- ===========================================================================
SELECT ma.nome                       AS marca,
       fn_valor_estoque_marca(ma.id) AS valor_em_estoque
FROM marca ma
ORDER BY valor_em_estoque DESC, ma.nome
LIMIT 5;
