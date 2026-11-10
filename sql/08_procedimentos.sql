-- 08_procedimentos.sql
-- ENTREGAVEL 2 DA ETAPA 05: dois procedimentos.
--
--   1) sp_reajustar_preco_modelo: atualizacao de dados
--   2) sp_fechamento_caixa: usa CURSOR porque o calculo e sequencial
--
-- Rode depois do 02_insercao.sql. O script e re-executavel: apaga os procedimentos
-- antes de recriar e a tabela de apoio usa IF NOT EXISTS.

SET NAMES utf8mb4;

USE concessionaria;

DROP PROCEDURE IF EXISTS sp_reajustar_preco_modelo;
DROP PROCEDURE IF EXISTS sp_fechamento_caixa;

-- Tabela de apoio do procedimento com cursor: uma linha por mes.
CREATE TABLE IF NOT EXISTS fechamento_caixa (
    mes             CHAR(7)       NOT NULL PRIMARY KEY,
    total_compras   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_vendas    DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    saldo_mes       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    saldo_acumulado DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    situacao        VARCHAR(10)   NOT NULL
);

DELIMITER $$

-- ===========================================================================
-- PROCEDIMENTO 1: sp_reajustar_preco_modelo  (ATUALIZACAO DE DADOS)
--
-- Justificativa semantica: reajustar a tabela de precos de um modelo e uma operacao
-- de rotina na concessionaria, para promoção ou para repassar aumento de custo.
--
-- Duas decisoes de negocio embutidas:
--   - o reajuste atinge apenas carros EM ESTOQUE (venda_id IS NULL). Carro vendido
--     tem preco historico: mudar o valor dele falsearia a venda que ja aconteceu;
--   - percentual invalido gera erro de negocio com SIGNAL, em vez de gravar preco
--     negativo. O CHECK (preco > 0) da tabela pegaria o caso, mas com mensagem
--     tecnica, sem explicar ao usuario o que fazer.
--
-- O parametro OUT devolve quantas linhas foram alteradas, para a tela informar.
-- ===========================================================================
CREATE PROCEDURE sp_reajustar_preco_modelo(
    IN  p_modelo_id  INT,
    IN  p_percentual DECIMAL(5,2),
    OUT p_afetados   INT
)
BEGIN
    IF p_percentual IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Percentual nao informado.';
    END IF;

    IF p_percentual <= -100 OR p_percentual > 100 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Percentual invalido: informe um valor maior que -100 e menor ou igual a 100.';
    END IF;

    UPDATE carro
       SET preco = ROUND(preco * (1 + p_percentual / 100), 2)
     WHERE modelo_id = p_modelo_id
       AND venda_id IS NULL;

    SET p_afetados = ROW_COUNT();
END$$

-- ===========================================================================
-- PROCEDIMENTO 2: sp_fechamento_caixa  (CURSOR)
--
-- Justificativa semantica: o fechamento de caixa mostra, mes a mes, quanto entrou em
-- vendas, quanto saiu em compras e qual o saldo. O campo que exige processamento
-- linha a linha e o SALDO ACUMULADO: o acumulado de um mes depende do acumulado do
-- mes anterior.
--
-- Por que nao da para fazer com um UPDATE: um UPDATE aplica uma expressao a cada
-- linha de forma independente, sem memoria entre elas. Aqui existe estado que
-- atravessa as linhas (o acumulado), e o cursor e o que mantem esse estado na
-- variavel v_saldo_acumulado enquanto percorre os meses em ordem. Alem disso, o
-- rotulo de situacao e decidido a cada iteracao, depois do acumulado atualizado.
--
-- Fluxo: TRUNCATE na tabela de apoio, abre o cursor dos meses ordenados, percorre
-- com LOOP/FETCH, calcula saldo do mes e acumulado, classifica e grava uma linha.
-- ===========================================================================
CREATE PROCEDURE sp_fechamento_caixa()
BEGIN
    DECLARE v_mes             CHAR(7);
    DECLARE v_compras         DECIMAL(12,2);
    DECLARE v_vendas          DECIMAL(12,2);
    DECLARE v_saldo_mes       DECIMAL(12,2);
    DECLARE v_saldo_acumulado DECIMAL(12,2) DEFAULT 0.00;
    DECLARE v_situacao        VARCHAR(10);
    DECLARE v_fim             BOOLEAN      DEFAULT FALSE;

    DECLARE cur_meses CURSOR FOR
        SELECT t.mes,
               SUM(t.total_compras) AS total_compras,
               SUM(t.total_vendas)  AS total_vendas
        FROM (
            SELECT DATE_FORMAT(data, '%Y-%m') AS mes,
                   SUM(valor)                 AS total_compras,
                   0                          AS total_vendas
            FROM compra
            GROUP BY DATE_FORMAT(data, '%Y-%m')
            UNION ALL
            SELECT DATE_FORMAT(data, '%Y-%m') AS mes,
                   0                          AS total_compras,
                   SUM(valor)                 AS total_vendas
            FROM venda
            GROUP BY DATE_FORMAT(data, '%Y-%m')
        ) t
        GROUP BY t.mes
        ORDER BY t.mes;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_fim = TRUE;

    TRUNCATE TABLE fechamento_caixa;

    OPEN cur_meses;

    loop_meses: LOOP
        FETCH cur_meses INTO v_mes, v_compras, v_vendas;

        IF v_fim THEN
            LEAVE loop_meses;
        END IF;

        SET v_saldo_mes       = v_vendas - v_compras;
        SET v_saldo_acumulado = v_saldo_acumulado + v_saldo_mes;

        IF v_saldo_acumulado >= 0 THEN
            SET v_situacao = 'POSITIVO';
        ELSE
            SET v_situacao = 'NEGATIVO';
        END IF;

        INSERT INTO fechamento_caixa
            (mes, total_compras, total_vendas, saldo_mes, saldo_acumulado, situacao)
        VALUES
            (v_mes, v_compras, v_vendas, v_saldo_mes, v_saldo_acumulado, v_situacao);
    END LOOP loop_meses;

    CLOSE cur_meses;
END$$

DELIMITER ;

-- ===========================================================================
-- Demonstracao 1: roda o fechamento e mostra o resultado.
-- Pode rodar quantas vezes quiser: a tabela e limpa no inicio.
-- ===========================================================================
CALL sp_fechamento_caixa();

SELECT mes, total_compras, total_vendas, saldo_mes, saldo_acumulado, situacao
FROM fechamento_caixa
ORDER BY mes;

-- ===========================================================================
-- Demonstracao 2: reajuste de 10% no modelo 1 (Corolla).
-- Fica comentado de proposito: rodar o script nao deve alterar os precos do banco.
-- Ao rodar manualmente, os carros em estoque desse modelo sao reajustados e o
-- trigger de auditoria registra cada alteracao em log_auditoria.
--
--   CALL sp_reajustar_preco_modelo(1, 10.00, @afetados);
--   SELECT @afetados AS carros_reajustados;
--
--   -- percentual invalido: deve devolver ERROR 1644 (45000)
--   CALL sp_reajustar_preco_modelo(1, -150.00, @afetados2);
-- ===========================================================================
