package br.cesar.bd.concessionaria.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import br.cesar.bd.concessionaria.model.Venda;
import br.cesar.bd.concessionaria.util.ConnectionFactory;

/** CRUD da tabela venda (FK vendedor_id + cliente_CPF). */
public class VendaDAO {

    public int inserir(Venda v) throws SQLException {
        String sql = "INSERT INTO venda (data, valor, vendedor_id, cliente_CPF) "
                + "VALUES (COALESCE(?, CURRENT_DATE), ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, v.getData());
            ps.setBigDecimal(2, v.getValor());
            ps.setInt(3, v.getVendedorId());
            ps.setString(4, v.getClienteCpf());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                return chaves.next() ? chaves.getInt(1) : -1;
            }
        }
    }

    public void atualizar(Venda v) throws SQLException {
        String sql = "UPDATE venda SET data = ?, valor = ?, vendedor_id = ?, cliente_CPF = ? "
                + "WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, v.getData());
            ps.setBigDecimal(2, v.getValor());
            ps.setInt(3, v.getVendedorId());
            ps.setString(4, v.getClienteCpf());
            ps.setInt(5, v.getId());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM venda WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Venda> listar() throws SQLException {
        List<Venda> lista = new ArrayList<>();
        String sql = "SELECT id, data, valor, vendedor_id, cliente_CPF FROM venda ORDER BY id DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Venda(
                        rs.getInt("id"),
                        rs.getDate("data"),
                        rs.getBigDecimal("valor"),
                        rs.getInt("vendedor_id"),
                        rs.getString("cliente_CPF")));
            }
        }
        return lista;
    }

    public List<Object[]> listarParaTabela() throws SQLException {
        List<Object[]> linhas = new ArrayList<>();
        String sql = "SELECT ve.id, ve.data, ve.valor, vd.nome AS vendedor, cl.nome AS cliente "
                + "FROM venda ve "
                + "INNER JOIN vendedor vd ON vd.id = ve.vendedor_id "
                + "INNER JOIN cliente  cl ON cl.CPF = ve.cliente_CPF "
                + "ORDER BY ve.data DESC, ve.id DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                linhas.add(new Object[]{
                        rs.getInt("id"),
                        rs.getDate("data"),
                        rs.getBigDecimal("valor"),
                        rs.getString("vendedor"),
                        rs.getString("cliente")});
            }
        }
        return linhas;
    }
}
