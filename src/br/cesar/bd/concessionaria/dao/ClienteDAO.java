package br.cesar.bd.concessionaria.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import br.cesar.bd.concessionaria.model.Cliente;
import br.cesar.bd.concessionaria.util.ConnectionFactory;

/** CRUD da tabela cliente (PK CHAR + FK endereco_id). */
public class ClienteDAO {

    public void inserir(Cliente c) throws SQLException {
        String sql = "INSERT INTO cliente (CPF, nome, endereco_id) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCpf());
            ps.setString(2, c.getNome());
            ps.setInt(3, c.getEnderecoId());
            ps.executeUpdate();
        }
    }

    public void atualizar(Cliente c) throws SQLException {
        String sql = "UPDATE cliente SET nome = ?, endereco_id = ? WHERE CPF = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setInt(2, c.getEnderecoId());
            ps.setString(3, c.getCpf());
            ps.executeUpdate();
        }
    }

    public void excluir(String cpf) throws SQLException {
        String sql = "DELETE FROM cliente WHERE CPF = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpf);
            ps.executeUpdate();
        }
    }

    public List<Cliente> listar() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT CPF, nome, endereco_id FROM cliente ORDER BY nome";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Cliente(
                        rs.getString("CPF"),
                        rs.getString("nome"),
                        rs.getInt("endereco_id")));
            }
        }
        return lista;
    }

    public List<Object[]> listarParaTabela() throws SQLException {
        List<Object[]> linhas = new ArrayList<>();
        String sql = "SELECT cl.CPF, cl.nome, en.cidade, en.estado "
                + "FROM cliente cl INNER JOIN endereco en ON en.id = cl.endereco_id "
                + "ORDER BY cl.nome";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                linhas.add(new Object[]{
                        rs.getString("CPF"),
                        rs.getString("nome"),
                        rs.getString("cidade"),
                        rs.getString("estado")});
            }
        }
        return linhas;
    }
}
