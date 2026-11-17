package br.cesar.bd.concessionaria.dao;

import br.cesar.bd.concessionaria.util.ConnectionFactory;

import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ConsultaDAO {

    public DefaultTableModel consultaMarcasSemEstoque() throws SQLException {
        String sql = "SELECT ma.id AS ID_Marca, ma.nome AS Marca " +
                     "FROM marca ma " +
                     "LEFT JOIN modelo mo ON mo.marca_id = ma.id " +
                     "LEFT JOIN carro ca ON ca.modelo_id = mo.id AND ca.venda_id IS NULL " +
                     "WHERE ca.chassi IS NULL " +
                     "ORDER BY ma.nome";
        return executarParaModelo(sql);
    }

    public DefaultTableModel consultaFluxoCaixaPeriodo(Date dataInicio, Date dataFim) throws SQLException {
        String sql = "WITH compras_mes AS (" +
                     "    SELECT DATE_FORMAT(data, '%Y-%m') AS mes, " +
                     "           SUM(valor) AS total_compras, " +
                     "           COUNT(*) AS qtd_compras " +
                     "    FROM compra " +
                     "    WHERE data BETWEEN ? AND ? " +
                     "    GROUP BY DATE_FORMAT(data, '%Y-%m')" +
                     "), vendas_mes AS (" +
                     "    SELECT DATE_FORMAT(data, '%Y-%m') AS mes, " +
                     "           SUM(valor) AS total_vendas, " +
                     "           COUNT(*) AS qtd_vendas " +
                     "    FROM venda " +
                     "    WHERE data BETWEEN ? AND ? " +
                     "    GROUP BY DATE_FORMAT(data, '%Y-%m')" +
                     ") " +
                     "SELECT c.mes AS Mes, " +
                     "       c.total_compras AS Total_Compras, " +
                     "       c.qtd_compras AS Qtd_Compras, " +
                     "       COALESCE(v.total_vendas, 0) AS Total_Vendas, " +
                     "       COALESCE(v.qtd_vendas, 0) AS Qtd_Vendas, " +
                     "       COALESCE(v.total_vendas, 0) - c.total_compras AS Saldo " +
                     "FROM compras_mes c " +
                     "LEFT JOIN vendas_mes v ON v.mes = c.mes " +
                     "UNION " +
                     "SELECT v.mes, " +
                     "       COALESCE(c.total_compras, 0), " +
                     "       COALESCE(c.qtd_compras, 0), " +
                     "       v.total_vendas, " +
                     "       v.qtd_vendas, " +
                     "       v.total_vendas - COALESCE(c.total_compras, 0) " +
                     "FROM compras_mes c " +
                     "RIGHT JOIN vendas_mes v ON v.mes = c.mes " +
                     "ORDER BY Mes";

        return executarParaModelo(sql, dataInicio, dataFim, dataInicio, dataFim);
    }

    public DefaultTableModel consultaCarrosAcimaMedia(String marcaFiltro) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT ca.chassi AS Chassi, ma.nome AS Marca, mo.nome AS Modelo, ca.ano AS Ano, ca.preco AS Preco " +
                "FROM carro ca " +
                "JOIN modelo mo ON mo.id = ca.modelo_id " +
                "JOIN marca ma ON ma.id = mo.marca_id " +
                "WHERE ca.venda_id IS NULL " +
                "  AND ca.preco > (SELECT AVG(preco) FROM carro WHERE venda_id IS NULL) ");

        List<Object> params = new ArrayList<>();
        if (marcaFiltro != null && !marcaFiltro.equals("Todas")) {
            sql.append("AND ma.nome = ? ");
            params.add(marcaFiltro);
        }
        sql.append("ORDER BY ca.preco DESC");

        return executarParaModelo(sql.toString(), params.toArray());
    }

    public BigDecimal buscarPrecoMedioEstoque() throws SQLException {
        String sql = "SELECT COALESCE(AVG(preco), 0) FROM carro WHERE venda_id IS NULL";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }

    public DefaultTableModel consultaFornecedoresAcimaMedia() throws SQLException {
        String sql = "SELECT f.CNPJ AS CNPJ, f.nome AS Fornecedor, COUNT(c.id) AS Qtd_Compras, SUM(c.valor) AS Total_Comprado " +
                     "FROM fornecedor f " +
                     "JOIN compra c ON c.fornecedor_CNPJ = f.CNPJ " +
                     "GROUP BY f.CNPJ, f.nome " +
                     "HAVING SUM(c.valor) > (" +
                     "    SELECT AVG(t.total) FROM (SELECT SUM(valor) AS total FROM compra GROUP BY fornecedor_CNPJ) t" +
                     ") " +
                     "ORDER BY Total_Comprado DESC";
        return executarParaModelo(sql);
    }

    public DefaultTableModel consultarViewVendaDetalhada(String marca, String uf, String buscaTexto) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT venda_id AS Venda, data_venda AS Data, cliente AS Cliente, cliente_uf AS UF, " +
                "       vendedor AS Vendedor, marca AS Marca, modelo AS Modelo, cor AS Cor, " +
                "       ano AS Ano, preco_carro AS Preco_Carro, valor_venda AS Valor_Venda " +
                "FROM vw_venda_detalhada WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (marca != null && !marca.equals("Todas")) {
            sql.append("AND marca = ? ");
            params.add(marca);
        }
        if (uf != null && !uf.equals("Todas")) {
            sql.append("AND cliente_uf = ? ");
            params.add(uf);
        }
        if (buscaTexto != null && !buscaTexto.trim().isEmpty()) {
            sql.append("AND (cliente LIKE ? OR vendedor LIKE ?) ");
            String padrao = "%" + buscaTexto.trim() + "%";
            params.add(padrao);
            params.add(padrao);
        }
        sql.append("ORDER BY data_venda DESC, venda_id DESC");

        return executarParaModelo(sql.toString(), params.toArray());
    }

    public DefaultTableModel consultarViewEstoqueMarcaModelo(String marca) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT marca AS Marca, modelo AS Modelo, carros_em_estoque AS Qtd_Estoque, " +
                "       menor_preco AS Menor_Preco, maior_preco AS Maior_Preco, " +
                "       preco_medio AS Preco_Medio, valor_total_estoque AS Valor_Total " +
                "FROM vw_estoque_por_marca_modelo WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (marca != null && !marca.equals("Todas")) {
            sql.append("AND marca = ? ");
            params.add(marca);
        }
        sql.append("ORDER BY valor_total_estoque DESC");

        return executarParaModelo(sql.toString(), params.toArray());
    }

    public List<String> listarNomesMarcas() throws SQLException {
        List<String> marcas = new ArrayList<>();
        String sql = "SELECT nome FROM marca ORDER BY nome";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) marcas.add(rs.getString("nome"));
        }
        return marcas;
    }

    public List<String> listarUfsClientes() throws SQLException {
        List<String> ufs = new ArrayList<>();
        String sql = "SELECT DISTINCT estado FROM endereco ORDER BY estado";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ufs.add(rs.getString("estado"));
        }
        return ufs;
    }

    private DefaultTableModel executarParaModelo(String sql, Object... params) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int numColunas = metaData.getColumnCount();

                Vector<String> colunas = new Vector<>();
                for (int i = 1; i <= numColunas; i++) {
                    colunas.add(metaData.getColumnLabel(i));
                }

                Vector<Vector<Object>> linhas = new Vector<>();
                while (rs.next()) {
                    Vector<Object> linha = new Vector<>();
                    for (int i = 1; i <= numColunas; i++) {
                        linha.add(rs.getObject(i));
                    }
                    linhas.add(linha);
                }

                return new DefaultTableModel(linhas, colunas) {
                    @Override
                    public boolean isCellEditable(int row, int column) { return false; }
                };
            }
        }
    }
}