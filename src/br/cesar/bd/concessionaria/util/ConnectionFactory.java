package br.cesar.bd.concessionaria.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {

    private static final String BANCO = "concessionaria";

    private static final String URL = "jdbc:mysql://localhost:3306/" + BANCO
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Sao_Paulo";

    private static final String USER = "root";
    private static final String PASS = "root";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Nome do banco configurado (usado nas mensagens de erro da interface). */
    public static String getNomeBanco() {
        return BANCO;
    }

    /** Abre e fecha uma conexao apenas para conferir se o banco responde. */
    public static void testar() throws SQLException {
        try (Connection conn = getConnection()) {
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Nao foi possivel abrir a conexao.");
            }
        }
    }
   
    public static void inicializarTriggers() {
        String dropSql = "DROP TRIGGER IF EXISTS trg_venda_data_futura";
        String createSql = "CREATE TRIGGER trg_venda_data_futura "
                + "BEFORE INSERT ON venda "
                + "FOR EACH ROW "
                + "BEGIN "
                + "    IF NEW.data IS NOT NULL AND NEW.data > CURRENT_DATE THEN "
                + "        SIGNAL SQLSTATE '45000' "
                + "            SET MESSAGE_TEXT = 'Venda nao pode ter data futura.'; "
                + "    END IF; "
                + "END";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(dropSql);
            stmt.execute(createSql);
        } catch (SQLException e) {
            System.err.println("Aviso ao criar trigger: " + e.getMessage());
        }
    }
    
    public static void inicializarFuncoes() {
        String dropSql = "DROP FUNCTION IF EXISTS fn_perfil_cliente";
        String createSql = "CREATE FUNCTION fn_perfil_cliente(p_cpf CHAR(11))\n"
                + "RETURNS VARCHAR(20)\n"
                + "READS SQL DATA\n"
                + "BEGIN\n"
                + "    DECLARE v_compras INT           DEFAULT 0;\n"
                + "    DECLARE v_total   DECIMAL(12,2) DEFAULT 0.00;\n"
                + "\n"
                + "    SELECT COUNT(*), COALESCE(SUM(valor), 0)\n"
                + "      INTO v_compras, v_total\n"
                + "      FROM venda\n"
                + "     WHERE cliente_CPF = p_cpf;\n"
                + "\n"
                + "    IF v_compras = 0 THEN\n"
                + "        RETURN 'SEM COMPRA';\n"
                + "    ELSEIF v_compras = 1 THEN\n"
                + "        RETURN 'NOVO';\n"
                + "    ELSEIF v_compras >= 4 OR v_total >= 150000 THEN\n"
                + "        RETURN 'VIP';\n"
                + "    ELSE\n"
                + "        RETURN 'RECORRENTE';\n"
                + "    END IF;\n"
                + "END";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(dropSql);
            stmt.execute(createSql);
        } catch (SQLException e) {
            System.err.println("Aviso ao criar function: " + e.getMessage());
        }
    }
    
    public static void inicializarProcedures() {
        String dropSql = "DROP PROCEDURE IF EXISTS sp_fechamento_caixa";
        String createSql = "CREATE PROCEDURE sp_fechamento_caixa()\n"
                + "BEGIN\n"
                + "    DECLARE v_mes             CHAR(7);\n"
                + "    DECLARE v_compras         DECIMAL(12,2);\n"
                + "    DECLARE v_vendas          DECIMAL(12,2);\n"
                + "    DECLARE v_saldo_mes       DECIMAL(12,2);\n"
                + "    DECLARE v_saldo_acumulado DECIMAL(12,2) DEFAULT 0.00;\n"
                + "    DECLARE v_situacao        VARCHAR(10);\n"
                + "    DECLARE v_fim             BOOLEAN      DEFAULT FALSE;\n"
                + "\n"
                + "    DECLARE cur_meses CURSOR FOR\n"
                + "        SELECT t.mes,\n"
                + "               SUM(t.total_compras) AS total_compras,\n"
                + "               SUM(t.total_vendas)  AS total_vendas\n"
                + "        FROM (\n"
                + "            SELECT DATE_FORMAT(data, '%Y-%m') AS mes,\n"
                + "                   SUM(valor)                 AS total_compras,\n"
                + "                   0                          AS total_vendas\n"
                + "            FROM compra\n"
                + "            GROUP BY DATE_FORMAT(data, '%Y-%m')\n"
                + "            UNION ALL\n"
                + "            SELECT DATE_FORMAT(data, '%Y-%m') AS mes,\n"
                + "                   0                          AS total_compras,\n"
                + "                   SUM(valor)                 AS total_vendas\n"
                + "            FROM venda\n"
                + "            GROUP BY DATE_FORMAT(data, '%Y-%m')\n"
                + "        ) t\n"
                + "        GROUP BY t.mes\n"
                + "        ORDER BY t.mes;\n"
                + "\n"
                + "    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_fim = TRUE;\n"
                + "\n"
                + "    TRUNCATE TABLE fechamento_caixa;\n"
                + "\n"
                + "    OPEN cur_meses;\n"
                + "\n"
                + "    loop_meses: LOOP\n"
                + "        FETCH cur_meses INTO v_mes, v_compras, v_vendas;\n"
                + "\n"
                + "        IF v_fim THEN\n"
                + "            LEAVE loop_meses;\n"
                + "        END IF;\n"
                + "\n"
                + "        SET v_saldo_mes       = v_vendas - v_compras;\n"
                + "        SET v_saldo_acumulado = v_saldo_acumulado + v_saldo_mes;\n"
                + "\n"
                + "        IF v_saldo_acumulado >= 0 THEN\n"
                + "            SET v_situacao = 'POSITIVO';\n"
                + "        ELSE\n"
                + "            SET v_situacao = 'NEGATIVO';\n"
                + "        END IF;\n"
                + "\n"
                + "        INSERT INTO fechamento_caixa\n"
                + "            (mes, total_compras, total_vendas, saldo_mes, saldo_acumulado, situacao)\n"
                + "        VALUES\n"
                + "            (v_mes, v_compras, v_vendas, v_saldo_mes, v_saldo_acumulado, v_situacao);\n"
                + "    END LOOP loop_meses;\n"
                + "\n"
                + "    CLOSE cur_meses;\n"
                + "END";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(dropSql);
            stmt.execute(createSql);
        } catch (SQLException e) {
            System.err.println("Aviso ao criar procedure: " + e.getMessage());
        }
    }
    public static void inicializarViews() {
        String viewVendaDetalhada = "CREATE OR REPLACE VIEW vw_venda_detalhada AS\n"
                + "SELECT\n"
                + "    v.id                         AS venda_id,\n"
                + "    v.data                       AS data_venda,\n"
                + "    DATE_FORMAT(v.data, '%Y-%m') AS mes,\n"
                + "    v.valor                      AS valor_venda,\n"
                + "    c.CPF                        AS cliente_cpf,\n"
                + "    c.nome                       AS cliente,\n"
                + "    en.cidade                    AS cliente_cidade,\n"
                + "    en.estado                    AS cliente_uf,\n"
                + "    vd.nome                      AS vendedor,\n"
                + "    ca.chassi                    AS chassi,\n"
                + "    ma.nome                      AS marca,\n"
                + "    mo.nome                      AS modelo,\n"
                + "    co.nome                      AS cor,\n"
                + "    ca.ano                       AS ano,\n"
                + "    ca.preco                     AS preco_carro\n"
                + "FROM venda v\n"
                + "JOIN cliente  c  ON c.CPF       = v.cliente_CPF\n"
                + "JOIN endereco en ON en.id       = c.endereco_id\n"
                + "JOIN vendedor vd ON vd.id       = v.vendedor_id\n"
                + "JOIN carro    ca ON ca.venda_id = v.id\n"
                + "JOIN modelo   mo ON mo.id       = ca.modelo_id\n"
                + "JOIN marca    ma ON ma.id       = mo.marca_id\n"
                + "JOIN cor      co ON co.id       = ca.cor_id";

        String viewEstoqueMarcaModelo = "CREATE OR REPLACE VIEW vw_estoque_por_marca_modelo AS\n"
                + "SELECT\n"
                + "    ma.id                   AS marca_id,\n"
                + "    ma.nome                 AS marca,\n"
                + "    mo.id                   AS modelo_id,\n"
                + "    mo.nome                 AS modelo,\n"
                + "    COUNT(ca.chassi)        AS carros_em_estoque,\n"
                + "    MIN(ca.preco)           AS menor_preco,\n"
                + "    MAX(ca.preco)           AS maior_preco,\n"
                + "    ROUND(AVG(ca.preco), 2) AS preco_medio,\n"
                + "    SUM(ca.preco)           AS valor_total_estoque\n"
                + "FROM carro ca\n"
                + "JOIN modelo mo ON mo.id = ca.modelo_id\n"
                + "JOIN marca  ma ON ma.id = mo.marca_id\n"
                + "WHERE ca.venda_id IS NULL\n"
                + "GROUP BY ma.id, ma.nome, mo.id, mo.nome";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(viewVendaDetalhada);
            stmt.execute(viewEstoqueMarcaModelo);
        } catch (SQLException e) {
            System.err.println("Aviso ao criar views: " + e.getMessage());
        }
    }
    
}