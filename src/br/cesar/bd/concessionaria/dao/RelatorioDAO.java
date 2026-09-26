package br.cesar.bd.concessionaria.dao;

import br.cesar.bd.concessionaria.util.ConnectionFactory;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Vector;

public class RelatorioDAO {

    public DefaultTableModel gerarRelatorio(int tipoConsulta) throws SQLException {
        String sql = "";

        switch (tipoConsulta) {
            case 1:
                sql = "SELECT f.nome AS Fornecedor, COUNT(c.id) AS Qtd_Compras, SUM(c.valor) AS Valor_Total " +
                      "FROM compra c " +
                      "INNER JOIN fornecedor f ON c.fornecedor_CNPJ = f.CNPJ " +
                      "GROUP BY f.nome " +
                      "ORDER BY Valor_Total DESC";
                break;
                
            case 2:
                sql = "SELECT ma.nome AS Marca, mo.nome AS Modelo, COUNT(ca.chassi) AS Qtd_Estoque, SUM(ca.preco) AS Valor_Estoque " +
                      "FROM carro ca " +
                      "INNER JOIN modelo mo ON ca.modelo_id = mo.id " +
                      "INNER JOIN marca ma ON mo.marca_id = ma.id " +
                      "WHERE ca.venda_id IS NULL " +
                      "GROUP BY ma.nome, mo.nome " +
                      "ORDER BY ma.nome, mo.nome";
                break;
                
            case 3:
                sql = "SELECT en.estado AS UF, en.cidade AS Cidade, COUNT(cl.CPF) AS Qtd_Clientes " +
                      "FROM cliente cl " +
                      "INNER JOIN endereco en ON cl.endereco_id = en.id " +
                      "GROUP BY en.estado, en.cidade " +
                      "ORDER BY Qtd_Clientes DESC, en.estado";
                break;
                
            case 4:
                sql = "SELECT ca.chassi AS Chassi, ma.nome AS Marca, mo.nome AS Modelo, co.nome AS Cor, ca.ano AS Ano, ca.preco AS Preco " +
                      "FROM carro ca " +
                      "INNER JOIN modelo mo ON ca.modelo_id = mo.id " +
                      "INNER JOIN marca ma ON mo.marca_id = ma.id " +
                      "INNER JOIN cor co ON ca.cor_id = co.id " +
                      "WHERE ca.venda_id IS NULL " +
                      "ORDER BY ca.preco DESC " +
                      "LIMIT 10";
                break;
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int numColunas = metaData.getColumnCount();

            Vector<String> colunas = new Vector<>();
            for (int i = 1; i <= numColunas; i++) {
                colunas.add(metaData.getColumnLabel(i));
            }

            Vector<Vector<Object>> linhas = new Vector<>();
            while (rs.next()) {
                Vector<Object> linha = new Vector<>();
                for (int i = 1; i <= numColunas; i++) {
                    linha.add(rs.getObject(i));
                }
                linhas.add(linha);
            }

            return new DefaultTableModel(linhas, colunas) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
        }
    }
}