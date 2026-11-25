package br.cesar.bd.concessionaria.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import br.cesar.bd.concessionaria.dao.RelatorioDAO;

public class RelatoriosView extends View {

    private RelatorioDAO relatorioDAO = new RelatorioDAO();
    private JTable tabelaResultados;
    private JLabel lblTituloRelatorio;
    private ChartPanel chartPanel;

    public RelatoriosView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel painelTopo = new JPanel(new BorderLayout());

        lblTituloRelatorio = new JLabel("Selecione um relatório para visualizar os dados", SwingConstants.CENTER);
        lblTituloRelatorio.setFont(new Font("Arial", Font.BOLD, 16));
        lblTituloRelatorio.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        painelTopo.add(lblTituloRelatorio, BorderLayout.NORTH);

        JPanel painelBotoes = new JPanel(new GridLayout(0, 2, 10, 10));

        JButton btnRel1 = new JButton("1. Total Comprado por Fornecedor");
        JButton btnRel2 = new JButton("2. Resumo de Carros no Estoque");
        JButton btnRel3 = new JButton("3. Distribuição de Clientes por Cidade");
        JButton btnRel4 = new JButton("4. Vendas Acima da Média");
        JButton btnRel5 = new JButton("5. Classificação de Perfil dos Clientes (Função)");
        JButton btnRel6 = new JButton("6. Fechamento de Caixa Mensal (Procedure)");
        

        painelBotoes.add(btnRel1);
        painelBotoes.add(btnRel2);
        painelBotoes.add(btnRel3);
        painelBotoes.add(btnRel4);
        painelBotoes.add(btnRel5);
        painelBotoes.add(btnRel6);
        
        painelTopo.add(painelBotoes, BorderLayout.CENTER);
        add(painelTopo, BorderLayout.NORTH);

        tabelaResultados = new JTable();
        tabelaResultados.setModel(new DefaultTableModel(new Object[]{"Nenhum relatório selecionado"}, 0));

        JScrollPane scrollPane = new JScrollPane(tabelaResultados);

        JFreeChart chartVazio = ChartFactory.createBarChart("", "", "", new DefaultCategoryDataset());
        chartPanel = new ChartPanel(chartVazio);
        chartPanel.setPreferredSize(new Dimension(400, 300));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, chartPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        btnRel1.addActionListener(e -> carregarRelatorio(1, "Total Gasto em Compras por Fornecedor"));
        btnRel2.addActionListener(e -> carregarRelatorio(2, "Visão Geral: Valor e Quantidade do Estoque por Modelo"));
        btnRel3.addActionListener(e -> carregarRelatorio(3, "Distribuição Geográfica dos Clientes Cadastrados"));
        btnRel4.addActionListener(e -> carregarRelatorio(4, "Vendas Acima da Média Geral"));
        btnRel5.addActionListener(e -> carregarRelatorio(5, "Classificação de Perfil dos Clientes (fn_perfil_cliente)"));
        btnRel6.addActionListener(e -> carregarRelatorio(6, "Fechamento de Caixa Mensal (sp_fechamento_caixa)"));
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

            atualizarGrafico(tipo, modeloDinamico, titulo);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar relatório: " + ex.getMessage(),
                                          "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void atualizarGrafico(int tipo, DefaultTableModel modelo, String titulo) {

        JFreeChart chart;

        switch (tipo) {

            case 1: {
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    String categoria = String.valueOf(modelo.getValueAt(i, 0));
                    double valor = ((Number) modelo.getValueAt(i, 2)).doubleValue();
                    dataset.addValue(valor, "Valor Total", categoria);
                }
                chart = ChartFactory.createBarChart(titulo, "Fornecedor", "Valor (R$)",
                        dataset, PlotOrientation.VERTICAL, true, true, false);
                break;
            }

            case 2: {
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    String categoria = modelo.getValueAt(i, 0) + " - " + modelo.getValueAt(i, 1);
                    double valor = ((Number) modelo.getValueAt(i, 3)).doubleValue();
                    dataset.addValue(valor, "Valor em Estoque", categoria);
                }
                chart = ChartFactory.createBarChart(titulo, "Modelo", "Valor (R$)",
                        dataset, PlotOrientation.HORIZONTAL, true, true, false);
                break;
            }

            case 3: {
                DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    String categoria = modelo.getValueAt(i, 1) + "/" + modelo.getValueAt(i, 0);
                    double valor = ((Number) modelo.getValueAt(i, 2)).doubleValue();
                    dataset.setValue(categoria, valor);
                }
                chart = ChartFactory.createPieChart(titulo, dataset, true, true, false);
                break;
            }

            case 4: {
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    String categoria = "#" + modelo.getValueAt(i, 0) + " " + modelo.getValueAt(i, 2);
                    double valor = ((Number) modelo.getValueAt(i, 4)).doubleValue();
                    dataset.addValue(valor, "Valor da venda", categoria);
                }
                chart = ChartFactory.createBarChart(titulo, "Venda / cliente", "Valor (R$)",
                        dataset, PlotOrientation.HORIZONTAL, true, true, false);
                break;
            }

            case 5: {
                Map<String, Integer> contagemPerfis = new LinkedHashMap<>();
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    String perfil = String.valueOf(modelo.getValueAt(i, 2));
                    contagemPerfis.put(perfil, contagemPerfis.getOrDefault(perfil, 0) + 1);
                }

                DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
                for (Map.Entry<String, Integer> entry : contagemPerfis.entrySet()) {
                    dataset.setValue(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
                }
                chart = ChartFactory.createPieChart(titulo, dataset, true, true, false);
                break;
            }

            case 6: {
                // Colunas do fechamento: Mes, Compras, Vendas, Saldo_Mes, Saldo_Acumulado, Situacao.
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    String mes = String.valueOf(modelo.getValueAt(i, 0));
                    double saldoMes = ((Number) modelo.getValueAt(i, 3)).doubleValue();
                    double saldoAcumulado = ((Number) modelo.getValueAt(i, 4)).doubleValue();
                    dataset.addValue(saldoMes, "Saldo do mês", mes);
                    dataset.addValue(saldoAcumulado, "Saldo acumulado", mes);
                }
                chart = ChartFactory.createBarChart(titulo, "Mês", "Saldo (R$)",
                        dataset, PlotOrientation.VERTICAL, true, true, false);
                break;
            }

            default: {
                chart = ChartFactory.createBarChart("", "", "", new DefaultCategoryDataset());
            }
        }

        chartPanel.setChart(chart);
    }
}