package br.cesar.bd.concessionaria.view;

import br.cesar.bd.concessionaria.dao.RelatorioDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class RelatoriosView extends View {
    
    private RelatorioDAO relatorioDAO = new RelatorioDAO();
    private JTable tabelaResultados;
    private JLabel lblTituloRelatorio;

    public RelatoriosView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel painelTopo = new JPanel(new BorderLayout());
        
        lblTituloRelatorio = new JLabel("Selecione um relatório para visualizar os dados", SwingConstants.CENTER);
        lblTituloRelatorio.setFont(new Font("Arial", Font.BOLD, 16));
        lblTituloRelatorio.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        painelTopo.add(lblTituloRelatorio, BorderLayout.NORTH);

        JPanel painelBotoes = new JPanel(new GridLayout(2, 2, 10, 10));
        
        JButton btnRel1 = new JButton("1. Total Comprado por Fornecedor");
        JButton btnRel2 = new JButton("2. Resumo de Carros no Estoque");
        JButton btnRel3 = new JButton("3. Distribuição de Clientes por Cidade");
        JButton btnRel4 = new JButton("4. Top 10 Carros Mais Caros (Disponíveis)");

        painelBotoes.add(btnRel1);
        painelBotoes.add(btnRel2);
        painelBotoes.add(btnRel3);
        painelBotoes.add(btnRel4);
        
        painelTopo.add(painelBotoes, BorderLayout.CENTER);
        add(painelTopo, BorderLayout.NORTH);

        tabelaResultados = new JTable();
        tabelaResultados.setModel(new DefaultTableModel(new Object[]{"Nenhum relatório selecionado"}, 0)); 
        
        JScrollPane scrollPane = new JScrollPane(tabelaResultados);
        add(scrollPane, BorderLayout.CENTER);

        btnRel1.addActionListener(e -> carregarRelatorio(1, "Total Gasto em Compras por Fornecedor"));
        btnRel2.addActionListener(e -> carregarRelatorio(2, "Visão Geral: Valor e Quantidade do Estoque por Modelo"));
        btnRel3.addActionListener(e -> carregarRelatorio(3, "Distribuição Geográfica dos Clientes Cadastrados"));
        btnRel4.addActionListener(e -> carregarRelatorio(4, "Top 10 Carros de Maior Valor Disponíveis no Estoque"));
    }

    @Override
    public void aoAbrir() {
        tabelaResultados.setModel(new DefaultTableModel(new Object[]{"Nenhum relatório selecionado"}, 0));
        lblTituloRelatorio.setText("Selecione um relatório para visualizar os dados");
    }

    private void carregarRelatorio(int tipo, String titulo) {
        try {
            DefaultTableModel modeloDinamico = relatorioDAO.gerarRelatorio(tipo);
            
            tabelaResultados.setModel(modeloDinamico);
            
            lblTituloRelatorio.setText(titulo);
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar relatório: " + ex.getMessage(), 
                                          "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}