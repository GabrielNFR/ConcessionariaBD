package dao;

import model.Fornecedor;
import util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD da tabela fornecedor (chave primaria CHAR). */
public class FornecedorDAO {

    public void inserir(Fornecedor f) throws SQLException {
        String sql = "INSERT INTO fornecedor (CNPJ, nome, email) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getCnpj());
            ps.setString(2, f.getNome());
            ps.setString(3, f.getEmail());
            ps.executeUpdate();
        }
    }

    public void atualizar(Fornecedor f) throws SQLException {
        String sql = "UPDATE fornecedor SET nome = ?, email = ? WHERE CNPJ = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getNome());
            ps.setString(2, f.getEmail());
            ps.setString(3, f.getCnpj());
            ps.executeUpdate();
        }
    }

    public void excluir(String cnpj) throws SQLException {
        String sql = "DELETE FROM fornecedor WHERE CNPJ = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cnpj);
            ps.executeUpdate();
        }
    }

    public List<Fornecedor> listar() throws SQLException {
        List<Fornecedor> lista = new ArrayList<>();
        String sql = "SELECT CNPJ, nome, email FROM fornecedor ORDER BY nome";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Fornecedor(
                        rs.getString("CNPJ"),
                        rs.getString("nome"),
                        rs.getString("email")));
            }
        }
        return lista;
    }
}
