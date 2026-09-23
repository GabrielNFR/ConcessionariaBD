package dao;

import model.Endereco;
import util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD da tabela endereco. */
public class EnderecoDAO {

    public int inserir(Endereco e) throws SQLException {
        String sql = "INSERT INTO endereco "
                + "(logradouro, numero, complemento, bairro, cidade, estado, CEP) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getLogradouro());
            ps.setString(2, e.getNumero());
            ps.setString(3, e.getComplemento());
            ps.setString(4, e.getBairro());
            ps.setString(5, e.getCidade());
            ps.setString(6, e.getEstado());
            ps.setString(7, e.getCep());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                return chaves.next() ? chaves.getInt(1) : -1;
            }
        }
    }

    public void atualizar(Endereco e) throws SQLException {
        String sql = "UPDATE endereco SET logradouro = ?, numero = ?, complemento = ?, "
                + "bairro = ?, cidade = ?, estado = ?, CEP = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getLogradouro());
            ps.setString(2, e.getNumero());
            ps.setString(3, e.getComplemento());
            ps.setString(4, e.getBairro());
            ps.setString(5, e.getCidade());
            ps.setString(6, e.getEstado());
            ps.setString(7, e.getCep());
            ps.setInt(8, e.getId());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM endereco WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void excluirSeNaoUsado(int id) throws SQLException {
        String sql = "DELETE FROM endereco "
                + "WHERE id = ? AND NOT EXISTS (SELECT 1 FROM cliente WHERE endereco_id = ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public Endereco buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, logradouro, numero, complemento, bairro, cidade, estado, CEP "
                + "FROM endereco WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return montar(rs);
            }
        }
        return null;
    }

    public List<Endereco> listar() throws SQLException {
        List<Endereco> lista = new ArrayList<>();
        String sql = "SELECT id, logradouro, numero, complemento, bairro, cidade, estado, CEP "
                + "FROM endereco ORDER BY cidade, logradouro";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(montar(rs));
        }
        return lista;
    }

    private Endereco montar(ResultSet rs) throws SQLException {
        return new Endereco(
                rs.getInt("id"),
                rs.getString("logradouro"),
                rs.getString("numero"),
                rs.getString("complemento"),
                rs.getString("bairro"),
                rs.getString("cidade"),
                rs.getString("estado"),
                rs.getString("CEP"));
    }
}
