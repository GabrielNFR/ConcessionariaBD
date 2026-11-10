-- 09_triggers.sql
-- ENTREGAVEL 3 DA ETAPA 05: dois triggers com justificativa semantica.
--
--   1) trg_carro_preco_alterado: registra toda alteracao de preco em log_auditoria
--   2) trg_venda_data_futura: impede venda com data futura
--
-- Rode depois do 02_insercao.sql (e de preferencia depois dos procedimentos, porque o
-- reajuste de preco do procedimento alimenta o log de auditoria).
-- O script e re-executavel: a tabela de log usa IF NOT EXISTS e os triggers sao
-- apagados antes de serem recriados.

SET NAMES utf8mb4;

USE concessionaria;

-- ---------------------------------------------------------------------------
-- Tabela de log.
-- E um historico que so cresce: nunca recebe UPDATE nem DELETE pela aplicacao.
-- Guarda o essencial para auditoria: o que mudou, em qual registro, de qual valor
-- para qual valor, quem fez e quando.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS log_auditoria (
    id             INT PRIMARY KEY AUTO_INCREMENT,
    tabela         VARCHAR(50)  NOT NULL,
    acao           VARCHAR(20)  NOT NULL,
    chave_registro VARCHAR(50)  NOT NULL,
    detalhe        VARCHAR(255),
    usuario        VARCHAR(100),
    data_hora      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

DROP TRIGGER IF EXISTS trg_carro_preco_alterado;
DROP TRIGGER IF EXISTS trg_venda_data_futura;

DELIMITER $$

-- ===========================================================================
-- TRIGGER 1: trg_carro_preco_alterado  (ATUALIZA A TABELA DE LOGS)
--
-- Justificativa semantica: o preco de um carro e informacao com impacto financeiro e
-- historico. Se alguem altera o preco de um carro no patio, a concessionaria precisa
-- saber quem alterou, quando e qual era o valor anterior. Sem esse registro, uma
-- divergencia de preco na negociacao vira discussao sem prova.
--
-- O trigger dispara em qualquer UPDATE da tabela carro, mas so grava quando o preco
-- realmente mudou: alterar ano, cor ou modelo nao deve poluir a auditoria.
-- CURRENT_USER() guarda o usuario do banco que executou o comando.
-- ===========================================================================
CREATE TRIGGER trg_carro_preco_alterado
AFTER UPDATE ON carro
FOR EACH ROW
BEGIN
    IF OLD.preco <> NEW.preco THEN
        INSERT INTO log_auditoria (tabela, acao, chave_registro, detalhe, usuario)
        VALUES ('carro',
                'UPDATE',
                NEW.chassi,
                CONCAT('preco alterado de ', OLD.preco, ' para ', NEW.preco),
                CURRENT_USER());
    END IF;
END$$

-- ===========================================================================
-- TRIGGER 2: trg_venda_data_futura
--
-- Justificativa semantica: uma venda com data futura e erro de negocio. Nao existe
-- "vender hoje algo que so sera entregue no futuro" no fluxo da concessionaria, e
-- data futura distorce qualquer relatorio por periodo (o faturamento do mes
-- seguinte apareceria antes do tempo).
--
-- Por que trigger e nao CHECK: o MySQL nao aceita funcao nao deterministica
-- (CURRENT_DATE) dentro de uma constraint CHECK. O trigger BEFORE INSERT e o unico
-- lugar onde a regra pode ser aplicada com a data do momento.
--
-- Datas nulas passam: quando a coluna vem nula, o DEFAULT (CURRENT_DATE) da tabela
-- preenche com a data de hoje, que e valida.
-- ===========================================================================
CREATE TRIGGER trg_venda_data_futura
BEFORE INSERT ON venda
FOR EACH ROW
BEGIN
    IF NEW.data IS NOT NULL AND NEW.data > CURRENT_DATE THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Venda nao pode ter data futura.';
    END IF;
END$$

DELIMITER ;

-- ===========================================================================
-- Demonstracoes (rodar manualmente, uma por vez).
--
-- 1) O reajuste de preco alimenta o log de auditoria:
--    CALL sp_reajustar_preco_modelo(1, 10.00, @afetados);
--    SELECT @afetados;
--    SELECT * FROM log_auditoria ORDER BY id DESC;
--
-- 2) Alteracao que nao mexe no preco NAO gera log:
--    UPDATE carro SET ano = ano WHERE chassi = '9BWZZZ00000000031';
--    SELECT COUNT(*) FROM log_auditoria;
--
-- 3) Venda com data futura e recusada:
--    INSERT INTO venda (data, valor, vendedor_id, cliente_CPF)
--    VALUES ('2030-01-01', 1000.00, 1, '11111111101');
--    -- ERROR 1644 (45000): Venda nao pode ter data futura.
-- ===========================================================================
