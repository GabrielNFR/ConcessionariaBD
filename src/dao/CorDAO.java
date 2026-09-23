package dao;

import model.Cor;
import util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD da tabela cor. */
public class CorDAO {

    public void inserir(Cor c) throws SQLException {
        String sql = "INSERT INTO cor (nome) VALUES (?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.executeUpdate();
        }
    }

    public void atualizar(Cor c) throws SQLException {
        String sql = "UPDATE cor SET nome = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setInt(2, c.getId());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM cor WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Cor> listar() throws SQLException {
        List<Cor> lista = new ArrayList<>();
        String sql = "SELECT id, nome FROM cor ORDER BY nome";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Cor(rs.getInt("id"), rs.getString("nome")));
            }
        }
        return lista;
    }
}
