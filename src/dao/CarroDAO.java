package dao;

import model.Carro;
import util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD da tabela carro (PK CHAR + 4 chaves estrangeiras). */
public class CarroDAO {

    public void inserir(Carro c) throws SQLException {
        String sql = "INSERT INTO carro (chassi, ano, preco, cor_id, modelo_id, compra_id, venda_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getChassi());
            ps.setInt(2, c.getAno());
            ps.setBigDecimal(3, c.getPreco());
            ps.setInt(4, c.getCorId());
            ps.setInt(5, c.getModeloId());
            ps.setInt(6, c.getCompraId());
            ps.setObject(7, c.getVendaId(), Types.INTEGER);
            ps.executeUpdate();
        }
    }

    public void atualizar(Carro c) throws SQLException {
        String sql = "UPDATE carro SET ano = ?, preco = ?, cor_id = ?, modelo_id = ?, "
                + "compra_id = ?, venda_id = ? WHERE chassi = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getAno());
            ps.setBigDecimal(2, c.getPreco());
            ps.setInt(3, c.getCorId());
            ps.setInt(4, c.getModeloId());
            ps.setInt(5, c.getCompraId());
            ps.setObject(6, c.getVendaId(), Types.INTEGER);
            ps.setString(7, c.getChassi());
            ps.executeUpdate();
        }
    }

    public void excluir(String chassi) throws SQLException {
        String sql = "DELETE FROM carro WHERE chassi = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chassi);
            ps.executeUpdate();
        }
    }

    public void definirVenda(String chassi, Integer vendaId) throws SQLException {
        String sql = "UPDATE carro SET venda_id = ? WHERE chassi = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, vendaId, Types.INTEGER);
            ps.setString(2, chassi);
            ps.executeUpdate();
        }
    }

    public List<Carro> listar() throws SQLException {
        List<Carro> lista = new ArrayList<>();
        String sql = "SELECT chassi, ano, preco, cor_id, modelo_id, compra_id, venda_id "
                + "FROM carro ORDER BY chassi";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(montar(rs));
        }
        return lista;
    }

    public List<Object[]> listarParaTabela() throws SQLException {
        List<Object[]> linhas = new ArrayList<>();
        String sql = "SELECT c.chassi, m.nome AS marca, mo.nome AS modelo, co.nome AS cor, "
                + "c.ano, c.preco, c.compra_id, c.venda_id "
                + "FROM carro c "
                + "INNER JOIN modelo mo ON mo.id = c.modelo_id "
                + "INNER JOIN marca  m  ON m.id  = mo.marca_id "
                + "INNER JOIN cor    co ON co.id = c.cor_id "
                + "ORDER BY m.nome, mo.nome, c.chassi";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int vendaId = rs.getInt("venda_id");
                boolean emEstoque = rs.wasNull();

                linhas.add(new Object[]{
                        rs.getString("chassi"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getString("cor"),
                        rs.getInt("ano"),
                        rs.getBigDecimal("preco"),
                        rs.getInt("compra_id"),
                        emEstoque ? "Em estoque" : "Vendido (venda " + vendaId + ")"});
            }
        }
        return linhas;
    }

    public List<Carro> listarEmEstoque() throws SQLException {
        List<Carro> lista = new ArrayList<>();
        String sql = "SELECT chassi, ano, preco, cor_id, modelo_id, compra_id, venda_id "
                + "FROM carro WHERE venda_id IS NULL ORDER BY chassi";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(montar(rs));
        }
        return lista;
    }

    private Carro montar(ResultSet rs) throws SQLException {
        int vendaId = rs.getInt("venda_id");
        Integer venda = rs.wasNull() ? null : vendaId;

        return new Carro(
                rs.getString("chassi"),
                rs.getInt("ano"),
                rs.getBigDecimal("preco"),
                rs.getInt("cor_id"),
                rs.getInt("modelo_id"),
                rs.getInt("compra_id"),
                venda);
    }
}
