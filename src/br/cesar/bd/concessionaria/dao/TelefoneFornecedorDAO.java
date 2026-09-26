package br.cesar.bd.concessionaria.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import br.cesar.bd.concessionaria.model.TelefoneFornecedor;
import br.cesar.bd.concessionaria.util.ConnectionFactory;

/** CRUD da tabela telefone_fornecedor (chave primaria COMPOSTA). */
public class TelefoneFornecedorDAO {

    public void inserir(TelefoneFornecedor t) throws SQLException {
        String sql = "INSERT INTO telefone_fornecedor (fornecedor_CNPJ, telefone) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getFornecedorCnpj());
            ps.setString(2, t.getTelefone());
            ps.executeUpdate();
        }
    }

    public void atualizar(String cnpj, String telefoneAntigo, String telefoneNovo) throws SQLException {
        String sql = "UPDATE telefone_fornecedor SET telefone = ? "
                + "WHERE fornecedor_CNPJ = ? AND telefone = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, telefoneNovo);
            ps.setString(2, cnpj);
            ps.setString(3, telefoneAntigo);
            ps.executeUpdate();
        }
    }

    public void excluir(String cnpj, String telefone) throws SQLException {
        String sql = "DELETE FROM telefone_fornecedor WHERE fornecedor_CNPJ = ? AND telefone = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cnpj);
            ps.setString(2, telefone);
            ps.executeUpdate();
        }
    }

    public List<TelefoneFornecedor> listar() throws SQLException {
        List<TelefoneFornecedor> lista = new ArrayList<>();
        String sql = "SELECT fornecedor_CNPJ, telefone FROM telefone_fornecedor "
                + "ORDER BY fornecedor_CNPJ, telefone";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new TelefoneFornecedor(
                        rs.getString("fornecedor_CNPJ"),
                        rs.getString("telefone")));
            }
        }
        return lista;
    }

    public List<TelefoneFornecedor> listarPorFornecedor(String cnpj) throws SQLException {
        List<TelefoneFornecedor> lista = new ArrayList<>();
        String sql = "SELECT fornecedor_CNPJ, telefone FROM telefone_fornecedor "
                + "WHERE fornecedor_CNPJ = ? ORDER BY telefone";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cnpj);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new TelefoneFornecedor(
                            rs.getString("fornecedor_CNPJ"),
                            rs.getString("telefone")));
                }
            }
        }
        return lista;
    }
}
