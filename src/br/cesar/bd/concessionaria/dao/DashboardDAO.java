package br.cesar.bd.concessionaria.dao;

import br.cesar.bd.concessionaria.util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardDAO {

    /** Retorna os valores individuais das vendas filtradas (ordenados) para cálculo de média, mediana, moda, variância e desvio padrão */
    public List<Double> listarValoresVendas(Date dataIni, Date dataFim, String marca, String vendedor) throws SQLException {
        List<Double> valores = new ArrayList<>();
        boolean filtrarMarca = marca != null && !marca.equals("Todas");

        StringBuilder sql = new StringBuilder();
        if (filtrarMarca) {
            sql.append("SELECT ca.preco AS valor ")
               .append("FROM venda v ")
               .append("INNER JOIN vendedor vd ON vd.id = v.vendedor_id ")
               .append("INNER JOIN carro ca ON ca.venda_id = v.id ")
               .append("INNER JOIN modelo mo ON mo.id = ca.modelo_id ")
               .append("INNER JOIN marca ma ON ma.id = mo.marca_id ")
               .append("WHERE v.data BETWEEN ? AND ? AND ma.nome = ? ");
        } else {
            sql.append("SELECT v.valor AS valor ")
               .append("FROM venda v ")
               .append("INNER JOIN vendedor vd ON vd.id = v.vendedor_id ")
               .append("WHERE v.data BETWEEN ? AND ? ");
        }

        List<Object> params = new ArrayList<>();
        params.add(dataIni);
        params.add(dataFim);
        if (filtrarMarca) params.add(marca);

        if (vendedor != null && !vendedor.equals("Todos")) {
            sql.append("AND vd.nome = ? ");
            params.add(vendedor);
        }
        sql.append("ORDER BY valor ASC");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    valores.add(rs.getDouble("valor"));
                }
            }
        }
        return valores;
    }

    /** Gráfico 1 (Linha): Evolução temporal do faturamento mensal */
    public Map<String, Double> faturamentoMensal(Date dataIni, Date dataFim, String marca, String vendedor) throws SQLException {
        Map<String, Double> serie = new LinkedHashMap<>();
        boolean filtrarMarca = marca != null && !marca.equals("Todas");

        StringBuilder sql = new StringBuilder();
        if (filtrarMarca) {
            sql.append("SELECT DATE_FORMAT(v.data, '%Y-%m') AS mes, SUM(ca.preco) AS total ")
               .append("FROM venda v ")
               .append("INNER JOIN vendedor vd ON vd.id = v.vendedor_id ")
               .append("INNER JOIN carro ca ON ca.venda_id = v.id ")
               .append("INNER JOIN modelo mo ON mo.id = ca.modelo_id ")
               .append("INNER JOIN marca ma ON ma.id = mo.marca_id ")
               .append("WHERE v.data BETWEEN ? AND ? AND ma.nome = ? ");
        } else {
            sql.append("SELECT DATE_FORMAT(v.data, '%Y-%m') AS mes, SUM(v.valor) AS total ")
               .append("FROM venda v ")
               .append("INNER JOIN vendedor vd ON vd.id = v.vendedor_id ")
               .append("WHERE v.data BETWEEN ? AND ? ");
        }

        List<Object> params = new ArrayList<>();
        params.add(dataIni);
        params.add(dataFim);
        if (filtrarMarca) params.add(marca);

        if (vendedor != null && !vendedor.equals("Todos")) {
            sql.append("AND vd.nome = ? ");
            params.add(vendedor);
        }
        sql.append("GROUP BY DATE_FORMAT(v.data, '%Y-%m') ORDER BY mes ASC");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    serie.put(rs.getString("mes"), rs.getDouble("total"));
                }
            }
        }
        return serie;
    }

    /** Gráfico 4 (Dispersão / Correlação): Pares [Ano, Preço] dos veículos no banco */
    public List<double[]> paresAnoPrecoCarros(String marca) throws SQLException {
        List<double[]> pontos = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT ca.ano, ca.preco FROM carro ca " +
                "INNER JOIN modelo mo ON mo.id = ca.modelo_id " +
                "INNER JOIN marca ma ON ma.id = mo.marca_id ");

        boolean filtrarMarca = marca != null && !marca.equals("Todas");
        if (filtrarMarca) {
            sql.append("WHERE ma.nome = ? ");
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (filtrarMarca) ps.setString(1, marca);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pontos.add(new double[]{rs.getInt("ano"), rs.getDouble("preco")});
                }
            }
        }
        return pontos;
    }

    /** Gráfico 5 (Pizza) + Moda Categórica: Quantidade e faturamento de carros vendidos por marca */
    public Map<String, Double> vendasPorMarca(Date dataIni, Date dataFim, String vendedor) throws SQLException {
        Map<String, Double> mapa = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder(
                "SELECT ma.nome AS marca, SUM(ca.preco) AS total " +
                "FROM venda v " +
                "INNER JOIN vendedor vd ON vd.id = v.vendedor_id " +
                "INNER JOIN carro ca ON ca.venda_id = v.id " +
                "INNER JOIN modelo mo ON mo.id = ca.modelo_id " +
                "INNER JOIN marca ma ON ma.id = mo.marca_id " +
                "WHERE v.data BETWEEN ? AND ? ");

        List<Object> params = new ArrayList<>();
        params.add(dataIni);
        params.add(dataFim);

        if (vendedor != null && !vendedor.equals("Todos")) {
            sql.append("AND vd.nome = ? ");
            params.add(vendedor);
        }
        sql.append("GROUP BY ma.nome ORDER BY total DESC");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mapa.put(rs.getString("marca"), rs.getDouble("total"));
                }
            }
        }
        return mapa;
    }

    /** Marca com maior frequência de vendas (Moda categórica) */
    public String buscarMarcaMaisVendida(Date dataIni, Date dataFim) throws SQLException {
        String sql = "SELECT ma.nome, COUNT(ca.chassi) AS qtd " +
                     "FROM venda v " +
                     "INNER JOIN carro ca ON ca.venda_id = v.id " +
                     "INNER JOIN modelo mo ON mo.id = ca.modelo_id " +
                     "INNER JOIN marca ma ON ma.id = mo.marca_id " +
                     "WHERE v.data BETWEEN ? AND ? " +
                     "GROUP BY ma.nome ORDER BY qtd DESC LIMIT 1";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, dataIni);
            ps.setDate(2, dataFim);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nome") + " (" + rs.getInt("qtd") + " un.)";
                }
            }
        }
        return "N/A";
    }

    /** Gráfico 6 (Radar / SpiderWeb): Métricas comparativas por vendedor [Qtd Vendas, Faturamento, Ticket Médio] */
    public List<Object[]> desempenhoVendedores(Date dataIni, Date dataFim) throws SQLException {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT vd.nome AS vendedor, " +
                     "       COUNT(v.id) AS qtd, " +
                     "       COALESCE(SUM(v.valor), 0) AS faturamento, " +
                     "       COALESCE(AVG(v.valor), 0) AS ticket_medio " +
                     "FROM vendedor vd " +
                     "LEFT JOIN venda v ON v.vendedor_id = vd.id AND v.data BETWEEN ? AND ? " +
                     "GROUP BY vd.id, vd.nome " +
                     "ORDER BY faturamento DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, dataIni);
            ps.setDate(2, dataFim);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getString("vendedor"),
                        rs.getInt("qtd"),
                        rs.getDouble("faturamento"),
                        rs.getDouble("ticket_medio")
                    });
                }
            }
        }
        return lista;
    }

    /** Indicadores gerais do banco: [totalClientes, carrosEmEstoque, carrosVendidos] */
    public int[] indicadoresGerais(String marca) throws SQLException {
        int totalClientes = 0;
        int emEstoque = 0;
        int vendidos = 0;

        try (Connection conn = ConnectionFactory.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM cliente");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) totalClientes = rs.getInt(1);
            }

            boolean filtrarMarca = marca != null && !marca.equals("Todas");
            String sqlCarros = "SELECT SUM(CASE WHEN ca.venda_id IS NULL THEN 1 ELSE 0 END) AS estoque, " +
                               "       SUM(CASE WHEN ca.venda_id IS NOT NULL THEN 1 ELSE 0 END) AS vendidos " +
                               "FROM carro ca " +
                               "INNER JOIN modelo mo ON mo.id = ca.modelo_id " +
                               "INNER JOIN marca ma ON ma.id = mo.marca_id " +
                               (filtrarMarca ? "WHERE ma.nome = ?" : "");

            try (PreparedStatement ps = conn.prepareStatement(sqlCarros)) {
                if (filtrarMarca) ps.setString(1, marca);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        emEstoque = rs.getInt("estoque");
                        vendidos = rs.getInt("vendidos");
                    }
                }
            }
        }
        return new int[]{totalClientes, emEstoque, vendidos};
    }

    public List<String> listarMarcas() throws SQLException {
        List<String> lista = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT nome FROM marca ORDER BY nome");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(rs.getString("nome"));
        }
        return lista;
    }

    public List<String> listarVendedores() throws SQLException {
        List<String> lista = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT nome FROM vendedor ORDER BY nome");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(rs.getString("nome"));
        }
        return lista;
    }
}