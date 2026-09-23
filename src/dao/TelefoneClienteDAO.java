package dao;

import model.TelefoneCliente;
import util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD da tabela telefone_cliente (chave primaria COMPOSTA). */
public class TelefoneClienteDAO {

    public void inserir(TelefoneCliente t) throws SQLException {
        String sql = "INSERT INTO telefone_cliente (cliente_CPF, telefone) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getClienteCpf());
            ps.setString(2, t.getTelefone());
            ps.executeUpdate();
        }
    }

    public void atualizar(String cpf, String telefoneAntigo, String telefoneNovo) throws SQLException {
        String sql = "UPDATE telefone_cliente SET telefone = ? WHERE cliente_CPF = ? AND telefone = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, telefoneNovo);
            ps.setString(2, cpf);
            ps.setString(3, telefoneAntigo);
            ps.executeUpdate();
        }
    }

    public void excluir(String cpf, String telefone) throws SQLException {
        String sql = "DELETE FROM telefone_cliente WHERE cliente_CPF = ? AND telefone = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpf);
            ps.setString(2, telefone);
            ps.executeUpdate();
        }
    }

    public List<TelefoneCliente> listar() throws SQLException {
        List<TelefoneCliente> lista = new ArrayList<>();
        String sql = "SELECT cliente_CPF, telefone FROM telefone_cliente "
                + "ORDER BY cliente_CPF, telefone";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new TelefoneCliente(
                        rs.getString("cliente_CPF"),
                        rs.getString("telefone")));
            }
        }
        return lista;
    }

    public List<TelefoneCliente> listarPorCliente(String cpf) throws SQLException {
        List<TelefoneCliente> lista = new ArrayList<>();
        String sql = "SELECT cliente_CPF, telefone FROM telefone_cliente "
                + "WHERE cliente_CPF = ? ORDER BY telefone";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpf);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new TelefoneCliente(
                            rs.getString("cliente_CPF"),
                            rs.getString("telefone")));
                }
            }
        }
        return lista;
    }
}
