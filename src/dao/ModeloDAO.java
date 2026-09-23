package dao;

import model.Modelo;
import util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD da tabela modelo (FK marca_id -> marca.id). */
public class ModeloDAO {

    public int inserir(Modelo m) throws SQLException {
        String sql = "INSERT INTO modelo (nome, marca_id) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getNome());
            ps.setInt(2, m.getMarcaId());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                return chaves.next() ? chaves.getInt(1) : -1;
            }
        }
    }

    public void atualizar(Modelo m) throws SQLException {
        String sql = "UPDATE modelo SET nome = ?, marca_id = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getNome());
            ps.setInt(2, m.getMarcaId());
            ps.setInt(3, m.getId());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM modelo WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Modelo> listar() throws SQLException {
        List<Modelo> lista = new ArrayList<>();
        String sql = "SELECT id, nome, marca_id FROM modelo ORDER BY nome";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Modelo(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getInt("marca_id")));
            }
        }
        return lista;
    }

    public List<Object[]> listarParaTabela() throws SQLException {
        List<Object[]> linhas = new ArrayList<>();
        String sql = "SELECT mo.id, mo.nome AS modelo, m.nome AS marca "
                + "FROM modelo mo INNER JOIN marca m ON m.id = mo.marca_id "
                + "ORDER BY m.nome, mo.nome";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                linhas.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("modelo"),
                        rs.getString("marca")});
            }
        }
        return linhas;
    }
}
