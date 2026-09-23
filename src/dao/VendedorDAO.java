package dao;

import model.Vendedor;
import util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD da tabela vendedor. */
public class VendedorDAO {

    public int inserir(Vendedor v) throws SQLException {
        String sql = "INSERT INTO vendedor (nome, comissao) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getNome());
            ps.setBigDecimal(2, v.getComissao());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                return chaves.next() ? chaves.getInt(1) : -1;
            }
        }
    }

    public void atualizar(Vendedor v) throws SQLException {
        String sql = "UPDATE vendedor SET nome = ?, comissao = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getNome());
            ps.setBigDecimal(2, v.getComissao());
            ps.setInt(3, v.getId());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM vendedor WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Vendedor> listar() throws SQLException {
        List<Vendedor> lista = new ArrayList<>();
        String sql = "SELECT id, nome, comissao FROM vendedor ORDER BY nome";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Vendedor(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getBigDecimal("comissao")));
            }
        }
        return lista;
    }
}
