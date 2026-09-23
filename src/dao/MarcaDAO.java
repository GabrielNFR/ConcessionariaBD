package dao;

import model.Marca;
import util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD da tabela marca. */
public class MarcaDAO {

    public void inserir(Marca m) throws SQLException {
        String sql = "INSERT INTO marca (nome) VALUES (?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getNome());
            ps.executeUpdate();
        }
    }

    public void atualizar(Marca m) throws SQLException {
        String sql = "UPDATE marca SET nome = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getNome());
            ps.setInt(2, m.getId());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM marca WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Marca> listar() throws SQLException {
        List<Marca> lista = new ArrayList<>();
        String sql = "SELECT id, nome FROM marca ORDER BY nome";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Marca(rs.getInt("id"), rs.getString("nome")));
            }
        }
        return lista;
    }
}
