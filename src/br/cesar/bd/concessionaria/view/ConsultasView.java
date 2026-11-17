package br.cesar.bd.concessionaria.view;

import br.cesar.bd.concessionaria.dao.ConsultaDAO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ConsultasView extends View {

    private ConsultaDAO consultaDAO = new ConsultaDAO();

    private JComboBox<String> cbConsulta;
    private JLabel lblDescricao;

    // Painel dinâmico de filtros (CardLayout)
    private JPanel painelFiltrosCards;
    private CardLayout cardLayoutFiltros;

    // Filtros Consulta 2 (Período)
    private JTextField txtDataInicio;
    private JTextField txtDataFim;

    // Filtro Marca (Consulta 3 e View 2)
    private JComboBox<String> cbMarca;

    // Filtros View 1 (vw_venda_detalhada)
    private JComboBox<String> cbMarcaVenda;
    private JComboBox<String> cbUfVenda;
    private JTextField txtBuscaVenda;

    // Indicadores (KPIs)
    private JLabel lblKpi1Titulo, lblKpi1Valor;
    private JLabel lblKpi2Titulo, lblKpi2Valor;
    private JLabel lblKpi3Titulo, lblKpi3Valor;

    // Tabela e Gráfico
    private JTable tabelaResultados;
    private ChartPanel chartPanel;

    private final NumberFormat moedaBR = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter formatoDataBR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ConsultasView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // === TOPO: Seletor + Descrição + Filtros Dinâmicos ===
        JPanel painelTopo = new JPanel(new BorderLayout(8, 8));

        JPanel painelSelecao = new JPanel(new BorderLayout(10, 5));
        painelSelecao.setBorder(BorderFactory.createTitledBorder("Consultas Avançadas e Views (Etapas 03 e 04)"));

        cbConsulta = new JComboBox<>(new String[]{
            "1. [Anti Join] Marcas sem Unidades em Estoque",
            "2. [Full Outer Join] Fluxo de Caixa Mensal (Compras x Vendas)",
            "3. [Subconsulta WHERE] Carros em Estoque Acima da Média",
            "4. [Subconsulta HAVING] Fornecedores com Volume Acima da Média",
            "5. [VIEW] Vendas Detalhadas (vw_venda_detalhada)",
            "6. [VIEW] Estoque por Marca e Modelo (vw_estoque_por_marca_modelo)"
        });
        cbConsulta.setFont(new Font("Arial", Font.BOLD, 13));

        JButton btnExecutar = new JButton("Executar / Filtrar");
        btnExecutar.setFont(new Font("Arial", Font.BOLD, 12));
        btnExecutar.setBackground(new Color(30, 100, 180));
        btnExecutar.setForeground(Color.WHITE);

        JPanel linhaCombo = new JPanel(new BorderLayout(8, 0));
        linhaCombo.add(cbConsulta, BorderLayout.CENTER);
        linhaCombo.add(btnExecutar, BorderLayout.EAST);

        lblDescricao = new JLabel("Selecione uma consulta ou view para visualizar.");
        lblDescricao.setFont(new Font("Arial", Font.ITALIC, 12));
        lblDescricao.setForeground(new Color(70, 70, 70));

        painelSelecao.add(linhaCombo, BorderLayout.NORTH);
        painelSelecao.add(lblDescricao, BorderLayout.SOUTH);

        // Cards de Filtros
        cardLayoutFiltros = new CardLayout();
        painelFiltrosCards = new JPanel(cardLayoutFiltros);
        painelFiltrosCards.setBorder(BorderFactory.createTitledBorder("Parâmetros e Filtros"));

        // Card 0: Sem filtros extras
        JPanel cardSemFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cardSemFiltro.add(new JLabel("Esta consulta não exige parâmetros manuais. Clique em 'Executar / Filtrar' para atualizar."));

        // Card 1: Filtro de Período (Consulta 2)
        JPanel cardPeriodo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        cardPeriodo.add(new JLabel("Data Início (dd/MM/yyyy):"));
        txtDataInicio = new JTextField("01/01/2023", 10);
        cardPeriodo.add(txtDataInicio);
        cardPeriodo.add(new JLabel("Data Fim (dd/MM/yyyy):"));
        txtDataFim = new JTextField("31/12/2026", 10);
        cardPeriodo.add(txtDataFim);

        // Card 2: Filtro por Marca (Consulta 3 e View 2)
        JPanel cardMarca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        cardMarca.add(new JLabel("Filtrar por Marca:"));
        cbMarca = new JComboBox<>();
        cbMarca.setPreferredSize(new Dimension(200, 25));
        cardMarca.add(cbMarca);

        // Card 3: Filtros da View 1 (vw_venda_detalhada)
        JPanel cardVendaDetalhada = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        cardVendaDetalhada.add(new JLabel("Marca:"));
        cbMarcaVenda = new JComboBox<>();
        cardVendaDetalhada.add(cbMarcaVenda);

        cardVendaDetalhada.add(new JLabel("UF Cliente:"));
        cbUfVenda = new JComboBox<>();
        cardVendaDetalhada.add(cbUfVenda);

        cardVendaDetalhada.add(new JLabel("Cliente ou Vendedor:"));
        txtBuscaVenda = new JTextField(15);
        cardVendaDetalhada.add(txtBuscaVenda);

        painelFiltrosCards.add(cardSemFiltro, "SEM_FILTRO");
        painelFiltrosCards.add(cardPeriodo, "PERIODO");
        painelFiltrosCards.add(cardMarca, "MARCA");
        painelFiltrosCards.add(cardVendaDetalhada, "VENDA_DETALHADA");

        painelTopo.add(painelSelecao, BorderLayout.NORTH);
        painelTopo.add(painelFiltrosCards, BorderLayout.CENTER);

        // === INDICADORES (KPIs) ===
        JPanel painelKpis = new JPanel(new GridLayout(1, 3, 10, 0));
        lblKpi1Titulo = new JLabel("Registros", SwingConstants.CENTER);
        lblKpi1Valor = new JLabel("-", SwingConstants.CENTER);
        lblKpi2Titulo = new JLabel("Indicador 2", SwingConstants.CENTER);
        lblKpi2Valor = new JLabel("-", SwingConstants.CENTER);
        lblKpi3Titulo = new JLabel("Indicador 3", SwingConstants.CENTER);
        lblKpi3Valor = new JLabel("-", SwingConstants.CENTER);

        painelKpis.add(criarCardKpi(lblKpi1Titulo, lblKpi1Valor));
        painelKpis.add(criarCardKpi(lblKpi2Titulo, lblKpi2Valor));
        painelKpis.add(criarCardKpi(lblKpi3Titulo, lblKpi3Valor));

        painelTopo.add(painelKpis, BorderLayout.SOUTH);
        add(painelTopo, BorderLayout.NORTH);

        // === CENTRO: Tabela + Gráfico ===
        tabelaResultados = new JTable();
        JScrollPane scrollTabela = new JScrollPane(tabelaResultados);

        chartPanel = new ChartPanel(ChartFactory.createBarChart("", "", "", new DefaultCategoryDataset()));
        chartPanel.setPreferredSize(new Dimension(400, 260));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTabela, chartPanel);
        splitPane.setResizeWeight(0.52);
        add(splitPane, BorderLayout.CENTER);

        // Eventos
        cbConsulta.addActionListener(e -> {
            atualizarPainelFiltros();
            executarConsultaSelecionada();
        });

        btnExecutar.addActionListener(e -> executarConsultaSelecionada());
    }

    private JPanel criarCardKpi(JLabel lblTitulo, JLabel lblValor) {
        JPanel card = new JPanel(new BorderLayout(2, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 205, 215)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        card.setBackground(new Color(245, 247, 250));

        lblTitulo.setFont(new Font("Arial", Font.PLAIN, 11));
        lblTitulo.setForeground(new Color(90, 95, 105));

        lblValor.setFont(new Font("Arial", Font.BOLD, 15));
        lblValor.setForeground(new Color(25, 45, 75));

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
    }

    @Override
    public void aoAbrir() {
        carregarCombosFiltro();
        cbConsulta.setSelectedIndex(0);
        atualizarPainelFiltros();
        executarConsultaSelecionada();
    }

    private void carregarCombosFiltro() {
        try {
            cbMarca.removeAllItems();
            cbMarcaVenda.removeAllItems();
            cbMarca.addItem("Todas");
            cbMarcaVenda.addItem("Todas");
            for (String m : consultaDAO.listarNomesMarcas()) {
                cbMarca.addItem(m);
                cbMarcaVenda.addItem(m);
            }

            cbUfVenda.removeAllItems();
            cbUfVenda.addItem("Todas");
            for (String uf : consultaDAO.listarUfsClientes()) {
                cbUfVenda.addItem(uf);
            }
        } catch (SQLException e) {
            mostrarMensagem("Erro ao carregar filtros: " + e.getMessage(), true);
        }
    }

    private void atualizarPainelFiltros() {
        int idx = cbConsulta.getSelectedIndex();
        switch (idx) {
            case 0:
                lblDescricao.setText("Anti Join (LEFT JOIN + IS NULL): identifica marcas zeradas no pátio que precisam de reposição.");
                cardLayoutFiltros.show(painelFiltrosCards, "SEM_FILTRO");
                break;
            case 1:
                lblDescricao.setText("Full Outer Join (LEFT + UNION + RIGHT): compara compras e vendas mês a mês no período informado.");
                cardLayoutFiltros.show(painelFiltrosCards, "PERIODO");
                break;
            case 2:
                lblDescricao.setText("Subconsulta Escalar no WHERE: lista veículos em estoque com preço superior à média geral do pátio.");
                cardLayoutFiltros.show(painelFiltrosCards, "MARCA");
                break;
            case 3:
                lblDescricao.setText("Subconsulta no HAVING: filtra fornecedores cujo volume total comprado supera a média entre fornecedores.");
                cardLayoutFiltros.show(painelFiltrosCards, "SEM_FILTRO");
                break;
            case 4:
                lblDescricao.setText("Consulta direta na VIEW vw_venda_detalhada (junção de 7 tabelas: venda, cliente, endereço, vendedor, carro, marca, modelo e cor).");
                cardLayoutFiltros.show(painelFiltrosCards, "VENDA_DETALHADA");
                break;
            case 5:
                lblDescricao.setText("Consulta direta na VIEW vw_estoque_por_marca_modelo: consolida quantidade, faixa de preço e capital imobilizado.");
                cardLayoutFiltros.show(painelFiltrosCards, "MARCA");
                break;
        }
    }

    private void executarConsultaSelecionada() {
        int idx = cbConsulta.getSelectedIndex();
        try {
            DefaultTableModel modelo;

            switch (idx) {
                case 0: { // Anti Join
                    modelo = consultaDAO.consultaMarcasSemEstoque();
                    tabelaResultados.setModel(modelo);

                    lblKpi1Titulo.setText("Marcas Sem Estoque");
                    lblKpi1Valor.setText(String.valueOf(modelo.getRowCount()));
                    lblKpi2Titulo.setText("Status de Reposição");
                    lblKpi2Valor.setText(modelo.getRowCount() == 0 ? "Estoque Abastecido" : "Reposição Necessária");
                    lblKpi3Titulo.setText("Técnica SQL");
                    lblKpi3Valor.setText("LEFT JOIN + IS NULL");

                    DefaultPieDataset<String> pie = new DefaultPieDataset<>();
                    int semEstoque = modelo.getRowCount();
                    int totalMarcas = Math.max(semEstoque, cbMarca.getItemCount() - 1);
                    pie.setValue("Com Estoque (" + (totalMarcas - semEstoque) + ")", totalMarcas - semEstoque);
                    pie.setValue("Sem Estoque (" + semEstoque + ")", semEstoque);
                    chartPanel.setChart(ChartFactory.createPieChart("Cobertura de Marcas no Pátio", pie, true, true, false));
                    break;
                }

                case 1: { // Full Outer Join
                    Date dtIni = lerData(txtDataInicio.getText());
                    Date dtFim = lerData(txtDataFim.getText());
                    if (dtIni == null || dtFim == null) {
                        mostrarMensagem("Informe datas válidas no formato dd/MM/yyyy.", true);
                        return;
                    }

                    modelo = consultaDAO.consultaFluxoCaixaPeriodo(dtIni, dtFim);
                    tabelaResultados.setModel(modelo);

                    double somaCompras = 0, somaVendas = 0;
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                    for (int i = 0; i < modelo.getRowCount(); i++) {
                        String mes = String.valueOf(modelo.getValueAt(i, 0));
                        double compras = toDouble(modelo.getValueAt(i, 1));
                        double vendas = toDouble(modelo.getValueAt(i, 3));
                        somaCompras += compras;
                        somaVendas += vendas;
                        dataset.addValue(compras, "Compras (Saída)", mes);
                        dataset.addValue(vendas, "Vendas (Entrada)", mes);
                    }

                    lblKpi1Titulo.setText("Total Compras no Período");
                    lblKpi1Valor.setText(moedaBR.format(somaCompras));
                    lblKpi2Titulo.setText("Total Vendas no Período");
                    lblKpi2Valor.setText(moedaBR.format(somaVendas));
                    lblKpi3Titulo.setText("Saldo Líquido");
                    lblKpi3Valor.setText(moedaBR.format(somaVendas - somaCompras));

                    chartPanel.setChart(ChartFactory.createBarChart(
                            "Compras x Vendas por Mês", "Mês", "Valor (R$)",
                            dataset, PlotOrientation.VERTICAL, true, true, false));
                    break;
                }

                case 2: { // Subconsulta WHERE
                    String marca = (String) cbMarca.getSelectedItem();
                    modelo = consultaDAO.consultaCarrosAcimaMedia(marca);
                    BigDecimal mediaEstoque = consultaDAO.buscarPrecoMedioEstoque();
                    tabelaResultados.setModel(modelo);

                    double somaCapital = 0;
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                    for (int i = 0; i < modelo.getRowCount(); i++) {
                        String rotulo = modelo.getValueAt(i, 1) + " " + modelo.getValueAt(i, 2) + " (" + modelo.getValueAt(i, 0) + ")";
                        double preco = toDouble(modelo.getValueAt(i, 4));
                        somaCapital += preco;
                        dataset.addValue(preco, "Preço (R$)", rotulo);
                    }

                    lblKpi1Titulo.setText("Carros Acima da Média");
                    lblKpi1Valor.setText(String.valueOf(modelo.getRowCount()));
                    lblKpi2Titulo.setText("Ticket Médio do Estoque");
                    lblKpi2Valor.setText(moedaBR.format(mediaEstoque));
                    lblKpi3Titulo.setText("Capital Imobilizado (Alvo Promoção)");
                    lblKpi3Valor.setText(moedaBR.format(somaCapital));

                    chartPanel.setChart(ChartFactory.createBarChart(
                            "Veículos Acima da Média de Preço do Estoque", "Veículo", "Preço (R$)",
                            dataset, PlotOrientation.HORIZONTAL, true, true, false));
                    break;
                }

                case 3: { // Subconsulta HAVING
                    modelo = consultaDAO.consultaFornecedoresAcimaMedia();
                    tabelaResultados.setModel(modelo);

                    double totalFornecedores = 0;
                    int totalCompras = 0;
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                    for (int i = 0; i < modelo.getRowCount(); i++) {
                        String forn = String.valueOf(modelo.getValueAt(i, 1));
                        int qtd = (int) toDouble(modelo.getValueAt(i, 2));
                        double val = toDouble(modelo.getValueAt(i, 3));
                        totalCompras += qtd;
                        totalFornecedores += val;
                        dataset.addValue(val, "Total Comprado (R$)", forn);
                    }

                    lblKpi1Titulo.setText("Fornecedores Destaque");
                    lblKpi1Valor.setText(String.valueOf(modelo.getRowCount()));
                    lblKpi2Titulo.setText("Compras Realizadas");
                    lblKpi2Valor.setText(String.valueOf(totalCompras));
                    lblKpi3Titulo.setText("Volume Total Negociado");
                    lblKpi3Valor.setText(moedaBR.format(totalFornecedores));

                    chartPanel.setChart(ChartFactory.createBarChart(
                            "Fornecedores com Volume Acima da Média", "Fornecedor", "Total Comprado (R$)",
                            dataset, PlotOrientation.VERTICAL, true, true, false));
                    break;
                }

                case 4: { // View 1: vw_venda_detalhada
                    String marca = (String) cbMarcaVenda.getSelectedItem();
                    String uf = (String) cbUfVenda.getSelectedItem();
                    String busca = txtBuscaVenda.getText();

                    modelo = consultaDAO.consultarViewVendaDetalhada(marca, uf, busca);
                    tabelaResultados.setModel(modelo);

                    double somaCarros = 0;
                    Map<String, Double> faturamentoPorMarca = new LinkedHashMap<>();
                    for (int i = 0; i < modelo.getRowCount(); i++) {
                        String marcaLinha = String.valueOf(modelo.getValueAt(i, 5));
                        double precoCarro = toDouble(modelo.getValueAt(i, 9));
                        somaCarros += precoCarro;
                        faturamentoPorMarca.put(marcaLinha, faturamentoPorMarca.getOrDefault(marcaLinha, 0.0) + precoCarro);
                    }

                    lblKpi1Titulo.setText("Carros Vendidos (Filtrado)");
                    lblKpi1Valor.setText(String.valueOf(modelo.getRowCount()));
                    lblKpi2Titulo.setText("Valor Total dos Veículos");
                    lblKpi2Valor.setText(moedaBR.format(somaCarros));
                    lblKpi3Titulo.setText("Ticket Médio por Veículo");
                    lblKpi3Valor.setText(modelo.getRowCount() > 0 ? moedaBR.format(somaCarros / modelo.getRowCount()) : "R$ 0,00");

                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                    for (Map.Entry<String, Double> entry : faturamentoPorMarca.entrySet()) {
                        dataset.addValue(entry.getValue(), "Faturamento (R$)", entry.getKey());
                    }
                    chartPanel.setChart(ChartFactory.createBarChart(
                            "Faturamento por Marca (vw_venda_detalhada)", "Marca", "Valor (R$)",
                            dataset, PlotOrientation.VERTICAL, true, true, false));
                    break;
                }

                case 5: { // View 2: vw_estoque_por_marca_modelo
                    String marca = (String) cbMarca.getSelectedItem();
                    modelo = consultaDAO.consultarViewEstoqueMarcaModelo(marca);
                    tabelaResultados.setModel(modelo);

                    int totalUnidades = 0;
                    double capitalTotal = 0;
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                    for (int i = 0; i < modelo.getRowCount(); i++) {
                        String rotulo = modelo.getValueAt(i, 0) + " " + modelo.getValueAt(i, 1);
                        int qtd = (int) toDouble(modelo.getValueAt(i, 2));
                        double valTotal = toDouble(modelo.getValueAt(i, 6));
                        totalUnidades += qtd;
                        capitalTotal += valTotal;
                        dataset.addValue(valTotal, "Valor em Estoque (R$)", rotulo);
                    }

                    lblKpi1Titulo.setText("Modelos em Estoque");
                    lblKpi1Valor.setText(String.valueOf(modelo.getRowCount()));
                    lblKpi2Titulo.setText("Total de Veículos");
                    lblKpi2Valor.setText(totalUnidades + " unid.");
                    lblKpi3Titulo.setText("Capital Total Imobilizado");
                    lblKpi3Valor.setText(moedaBR.format(capitalTotal));

                    chartPanel.setChart(ChartFactory.createBarChart(
                            "Valor em Estoque por Marca/Modelo (vw_estoque_por_marca_modelo)", "Modelo", "Valor Total (R$)",
                            dataset, PlotOrientation.HORIZONTAL, true, true, false));
                    break;
                }
            }
        } catch (SQLException ex) {
            mostrarMensagem("Erro ao executar consulta: " + ex.getMessage(), true);
        }
    }

    private Date lerData(String texto) {
        if (texto == null || texto.trim().isEmpty()) return null;
        try {
            return Date.valueOf(LocalDate.parse(texto.trim(), formatoDataBR));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private double toDouble(Object valor) {
        if (valor instanceof Number) {
            return ((Number) valor).doubleValue();
        }
        return 0.0;
    }
}