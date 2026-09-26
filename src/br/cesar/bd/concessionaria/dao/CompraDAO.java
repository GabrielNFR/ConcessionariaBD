package br.cesar.bd.concessionaria.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import br.cesar.bd.concessionaria.model.Compra;
import br.cesar.bd.concessionaria.util.ConnectionFactory;

/** CRUD da tabela compra (PK AUTO_INCREMENT + FK para fornecedor). */
public class CompraDAO {

    public int inserir(Compra c) throws SQLException {
        String sql = "INSERT INTO compra (data, valor, fornecedor_CNPJ) "
                + "VALUES (COALESCE(?, CURRENT_DATE), ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, c.getData());
            ps.setBigDecimal(2, c.getValor());
            ps.setString(3, c.getFornecedorCnpj());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                return chaves.next() ? chaves.getInt(1) : -1;
            }
        }
    }

    public void atualizar(Compra c) throws SQLException {
        String sql = "UPDATE compra SET data = ?, valor = ?, fornecedor_CNPJ = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, c.getData());
            ps.setBigDecimal(2, c.getValor());
            ps.setString(3, c.getFornecedorCnpj());
            ps.setInt(4, c.getId());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM compra WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Compra> listar() throws SQLException {
        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT id, data, valor, fornecedor_CNPJ FROM compra ORDER BY id DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Compra(
                        rs.getInt("id"),
                        rs.getDate("data"),
                        rs.getBigDecimal("valor"),
                        rs.getString("fornecedor_CNPJ")));
            }
        }
        return lista;
    }

    public List<Object[]> listarParaTabela() throws SQLException {
        List<Object[]> linhas = new ArrayList<>();
        String sql = "SELECT c.id, c.data, c.valor, f.nome AS fornecedor "
                + "FROM compra c INNER JOIN fornecedor f ON f.CNPJ = c.fornecedor_CNPJ "
                + "ORDER BY c.data DESC, c.id DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                linhas.add(new Object[]{
                        rs.getInt("id"),
                        rs.getDate("data"),
                        rs.getBigDecimal("valor"),
                        rs.getString("fornecedor")});
            }
        }
        return linhas;
    }
}
