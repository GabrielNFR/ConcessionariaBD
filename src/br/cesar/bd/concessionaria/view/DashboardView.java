package br.cesar.bd.concessionaria.view;

import br.cesar.bd.concessionaria.dao.DashboardDAO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardView extends View {

    private DashboardDAO dashboardDAO = new DashboardDAO();

    private JTextField txtDataIni;
    private JTextField txtDataFim;
    private JComboBox<String> cbMarca;
    private JComboBox<String> cbVendedor;

    private JLabel lblKpiFaturamento, lblKpiFaturamentoSub;
    private JLabel lblKpiMediaMediana, lblKpiMediaMedianaSub;
    private JLabel lblKpiDispersao, lblKpiDispersaoSub;
    private JLabel lblKpiModa, lblKpiModaSub;
    private JLabel lblKpiEstoqueClientes, lblKpiEstoqueClientesSub;
    private JLabel lblKpiTendencia, lblKpiTendenciaSub;

    private ChartPanel cpLinhaTemporal;
    private ChartPanel cpBarrasEstatistica;
    private ChartPanel cpHistogramaFrequencia;
    private ChartPanel cpDispersaoCorrelacao;
    private ChartPanel cpPizzaMarcas;
    private ChartPanel cpRadarVendedores;

    private final NumberFormat moedaBR = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter formatoDataBR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DashboardView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel painelTopo = new JPanel(new BorderLayout(8, 8));

        JPanel painelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        painelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros Interativos do Dashboard (Dados em Tempo Real)"));

        painelFiltros.add(new JLabel("De:"));
        txtDataIni = new JTextField("01/01/2020", 9);
        painelFiltros.add(txtDataIni);

        painelFiltros.add(new JLabel("Até:"));
        txtDataFim = new JTextField(LocalDate.now().format(formatoDataBR), 9);
        painelFiltros.add(txtDataFim);

        painelFiltros.add(new JLabel("Marca:"));
        cbMarca = new JComboBox<>();
        cbMarca.setPreferredSize(new Dimension(140, 25));
        painelFiltros.add(cbMarca);

        painelFiltros.add(new JLabel("Vendedor:"));
        cbVendedor = new JComboBox<>();
        cbVendedor.setPreferredSize(new Dimension(150, 25));
        painelFiltros.add(cbVendedor);

        JButton btnAplicar = new JButton("Atualizar Dashboard");
        btnAplicar.setBackground(new Color(30, 100, 180));
        btnAplicar.setForeground(Color.WHITE);
        btnAplicar.setFont(new Font("Arial", Font.BOLD, 12));
        painelFiltros.add(btnAplicar);

        JButton btnReset = new JButton("Limpar Filtros");
        painelFiltros.add(btnReset);

        JPanel painelKpis = new JPanel(new GridLayout(2, 3, 8, 8));

        lblKpiFaturamento = new JLabel("R$ 0,00", SwingConstants.CENTER);
        lblKpiFaturamentoSub = new JLabel("0 vendas no período", SwingConstants.CENTER);
        painelKpis.add(criarCardKpi("Faturamento Total (Período)", lblKpiFaturamento, lblKpiFaturamentoSub, new Color(25, 95, 55)));

        lblKpiMediaMediana = new JLabel("R$ 0,00", SwingConstants.CENTER);
        lblKpiMediaMedianaSub = new JLabel("Mediana: R$ 0,00", SwingConstants.CENTER);
        painelKpis.add(criarCardKpi("Média (Ticket) & Mediana", lblKpiMediaMediana, lblKpiMediaMedianaSub, new Color(25, 65, 120)));

        lblKpiDispersao = new JLabel("s = R$ 0,00", SwingConstants.CENTER);
        lblKpiDispersaoSub = new JLabel("Variância (s²): 0", SwingConstants.CENTER);
        painelKpis.add(criarCardKpi("Dispersão (Desvio Padrão & Variância)", lblKpiDispersao, lblKpiDispersaoSub, new Color(110, 55, 130)));

        lblKpiModa = new JLabel("-", SwingConstants.CENTER);
        lblKpiModaSub = new JLabel("Faixa modal: -", SwingConstants.CENTER);
        painelKpis.add(criarCardKpi("Moda (Marca & Faixa Mais Frequente)", lblKpiModa, lblKpiModaSub, new Color(160, 85, 20)));

        lblKpiEstoqueClientes = new JLabel("0 em estoque", SwingConstants.CENTER);
        lblKpiEstoqueClientesSub = new JLabel("0 clientes | Giro: 0%", SwingConstants.CENTER);
        painelKpis.add(criarCardKpi("Estoque, Giro & Base de Clientes", lblKpiEstoqueClientes, lblKpiEstoqueClientesSub, new Color(35, 90, 110)));

        lblKpiTendencia = new JLabel("0,0%", SwingConstants.CENTER);
        lblKpiTendenciaSub = new JLabel("Comparativo temporal", SwingConstants.CENTER);
        painelKpis.add(criarCardKpi("Tendência Temporal (Crescimento)", lblKpiTendencia, lblKpiTendenciaSub, new Color(30, 115, 80)));

        painelTopo.add(painelFiltros, BorderLayout.NORTH);
        painelTopo.add(painelKpis, BorderLayout.CENTER);
        add(painelTopo, BorderLayout.NORTH);

        JPanel gridGraficos = new JPanel(new GridLayout(3, 2, 12, 12));
        gridGraficos.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

        cpLinhaTemporal = criarChartPanelVazio();
        cpBarrasEstatistica = criarChartPanelVazio();
        cpHistogramaFrequencia = criarChartPanelVazio();
        cpDispersaoCorrelacao = criarChartPanelVazio();
        cpPizzaMarcas = criarChartPanelVazio();
        cpRadarVendedores = criarChartPanelVazio();

        gridGraficos.add(cpLinhaTemporal);
        gridGraficos.add(cpBarrasEstatistica);
        gridGraficos.add(cpHistogramaFrequencia);
        gridGraficos.add(cpDispersaoCorrelacao);
        gridGraficos.add(cpPizzaMarcas);
        gridGraficos.add(cpRadarVendedores);

        JScrollPane scrollGraficos = new JScrollPane(gridGraficos);
        scrollGraficos.getVerticalScrollBar().setUnitIncrement(18);
        add(scrollGraficos, BorderLayout.CENTER);

        // Eventos dos Filtros
        btnAplicar.addActionListener(e -> atualizarDashboard());
        cbMarca.addActionListener(e -> {
            if (cbMarca.getSelectedItem() != null) atualizarDashboard();
        });
        cbVendedor.addActionListener(e -> {
            if (cbVendedor.getSelectedItem() != null) atualizarDashboard();
        });
        btnReset.addActionListener(e -> {
            txtDataIni.setText("01/01/2020");
            txtDataFim.setText(LocalDate.now().format(formatoDataBR));
            if (cbMarca.getItemCount() > 0) cbMarca.setSelectedIndex(0);
            if (cbVendedor.getItemCount() > 0) cbVendedor.setSelectedIndex(0);
            atualizarDashboard();
        });
    }

    private JPanel criarCardKpi(String titulo, JLabel lblPrincipal, JLabel lblSub, Color corDestaque) {
        JPanel card = new JPanel(new BorderLayout(2, 2));
        card.setBackground(new Color(246, 248, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 212, 222)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JLabel lblTit = new JLabel(titulo, SwingConstants.CENTER);
        lblTit.setFont(new Font("Arial", Font.PLAIN, 11));
        lblTit.setForeground(new Color(85, 90, 100));

        lblPrincipal.setFont(new Font("Arial", Font.BOLD, 15));
        lblPrincipal.setForeground(corDestaque);

        lblSub.setFont(new Font("Arial", Font.PLAIN, 11));
        lblSub.setForeground(new Color(65, 70, 80));

        card.add(lblTit, BorderLayout.NORTH);
        card.add(lblPrincipal, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);
        return card;
    }

    private ChartPanel criarChartPanelVazio() {
        JFreeChart chart = ChartFactory.createBarChart("", "", "", new DefaultCategoryDataset());
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(480, 290));
        panel.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 225)));
        return panel;
    }

    @Override
    public void aoAbrir() {
        carregarCombos();
        txtDataIni.setText("01/01/2020");
        txtDataFim.setText(LocalDate.now().format(formatoDataBR));
        atualizarDashboard();
    }

    private void carregarCombos() {
        try {
            cbMarca.removeAllItems();
            cbMarca.addItem("Todas");
            for (String m : dashboardDAO.listarMarcas()) cbMarca.addItem(m);

            cbVendedor.removeAllItems();
            cbVendedor.addItem("Todos");
            for (String v : dashboardDAO.listarVendedores()) cbVendedor.addItem(v);
        } catch (SQLException e) {
            mostrarMensagem("Erro ao carregar filtros do dashboard: " + e.getMessage(), true);
        }
    }

    private void atualizarDashboard() {
        Date dtIni = lerData(txtDataIni.getText());
        Date dtFim = lerData(txtDataFim.getText());
        if (dtIni == null || dtFim == null) {
            mostrarMensagem("Informe datas válidas no formato dd/MM/yyyy.", true);
            return;
        }

        String marca = cbMarca.getSelectedItem() != null ? cbMarca.getSelectedItem().toString() : "Todas";
        String vendedor = cbVendedor.getSelectedItem() != null ? cbVendedor.getSelectedItem().toString() : "Todos";

        try {
            List<Double> valores = dashboardDAO.listarValoresVendas(dtIni, dtFim, marca, vendedor);
            double soma = 0;
            for (double v : valores) soma += v;

            int n = valores.size();
            double media = n > 0 ? soma / n : 0.0;
            double mediana = calcularMediana(valores);
            double variancia = calcularVarianciaAmostral(valores, media);
            double desvioPadrao = Math.sqrt(variancia);
            double modaNumerica = calcularModaOuPontoMedioModal(valores);

            int[] gerais = dashboardDAO.indicadoresGerais(marca);
            int totalClientes = gerais[0];
            int emEstoque = gerais[1];
            int vendidos = gerais[2];
            int totalCarros = emEstoque + vendidos;
            double taxaGiro = totalCarros > 0 ? (vendidos * 100.0 / totalCarros) : 0.0;
            String marcaModa = dashboardDAO.buscarMarcaMaisVendida(dtIni, dtFim);

            Map<String, Double> serieMensal = dashboardDAO.faturamentoMensal(dtIni, dtFim, marca, vendedor);
            double crescimentoPct = calcularTendenciaCrescimento(serieMensal);

            lblKpiFaturamento.setText(moedaBR.format(soma));
            lblKpiFaturamentoSub.setText(n + " registros de venda filtrados");

            lblKpiMediaMediana.setText("Média: " + moedaBR.format(media));
            lblKpiMediaMedianaSub.setText("Mediana: " + moedaBR.format(mediana));

            lblKpiDispersao.setText("s = " + moedaBR.format(desvioPadrao));
            lblKpiDispersaoSub.setText(String.format(new Locale("pt", "BR"), "Variância (s²): %,.0f", variancia));

            lblKpiModa.setText(marcaModa);
            lblKpiModaSub.setText("Valor modal: " + moedaBR.format(modaNumerica));

            lblKpiEstoqueClientes.setText(emEstoque + " em estoque | " + vendidos + " vendidos");
            lblKpiEstoqueClientesSub.setText(String.format(new Locale("pt", "BR"), "%d clientes | Giro: %.1f%%", totalClientes, taxaGiro));

            String sinal = crescimentoPct >= 0 ? "+" : "";
            lblKpiTendencia.setText(String.format(new Locale("pt", "BR"), "%s%.1f%%", sinal, crescimentoPct));
            lblKpiTendenciaSub.setText(crescimentoPct >= 0 ? "Tendência de alta no período" : "Tendência de retração no período");

            atualizarGrafico1LinhaTemporal(serieMensal);
            atualizarGrafico2BarrasEstatistica(media, mediana, modaNumerica, desvioPadrao);
            atualizarGrafico3HistogramaFrequencia(valores);
            atualizarGrafico4DispersaoCorrelacao(dashboardDAO.paresAnoPrecoCarros(marca));
            atualizarGrafico5PizzaMarcas(dashboardDAO.vendasPorMarca(dtIni, dtFim, vendedor));
            atualizarGrafico6RadarVendedores(dashboardDAO.desempenhoVendedores(dtIni, dtFim));

        } catch (SQLException e) {
            mostrarMensagem("Erro ao carregar dados do dashboard: " + e.getMessage(), true);
        }
    }

    private void atualizarGrafico1LinhaTemporal(Map<String, Double> serieMensal) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : serieMensal.entrySet()) {
            dataset.addValue(entry.getValue(), "Faturamento (R$)", entry.getKey());
        }
        JFreeChart chart = ChartFactory.createLineChart(
                "1. Tendência Temporal: Faturamento Mensal",
                "Mês", "Valor (R$)", dataset,
                PlotOrientation.VERTICAL, false, true, false
        );
        cpLinhaTemporal.setChart(chart);
    }

    private void atualizarGrafico2BarrasEstatistica(double media, double mediana, double moda, double desvioPadrao) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(media, "Valor (R$)", "Média");
        dataset.addValue(mediana, "Valor (R$)", "Mediana");
        dataset.addValue(moda, "Valor (R$)", "Moda");
        dataset.addValue(desvioPadrao, "Valor (R$)", "Desvio Padrão (s)");

        JFreeChart chart = ChartFactory.createBarChart(
                "2. Estatística Descritiva das Vendas",
                "Medida Estatística", "Valor (R$)", dataset,
                PlotOrientation.VERTICAL, false, true, false
        );
        cpBarrasEstatistica.setChart(chart);
    }

    private void atualizarGrafico3HistogramaFrequencia(List<Double> valores) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int f1 = 0, f2 = 0, f3 = 0, f4 = 0;

        for (double v : valores) {
            if (v < 50000) f1++;
            else if (v < 90000) f2++;
            else if (v < 140000) f3++;
            else f4++;
        }

        dataset.addValue(f1, "Frequência (fi)", "< R$ 50 mil");
        dataset.addValue(f2, "Frequência (fi)", "R$ 50k - 90k");
        dataset.addValue(f3, "Frequência (fi)", "R$ 90k - 140k");
        dataset.addValue(f4, "Frequência (fi)", ">= R$ 140 mil");

        JFreeChart chart = ChartFactory.createBarChart(
                "3. Distribuição de Frequência por Faixa de Valor",
                "Classe de Valor", "Frequência Absoluta (Qtd)", dataset,
                PlotOrientation.VERTICAL, false, true, false
        );
        cpHistogramaFrequencia.setChart(chart);
    }

    private void atualizarGrafico4DispersaoCorrelacao(List<double[]> pontos) {
        XYSeries serie = new XYSeries("Veículos");
        for (double[] p : pontos) {
            serie.add(p[0], p[1]);
        }
        XYSeriesCollection dataset = new XYSeriesCollection(serie);
        double r = calcularCorrelacaoPearson(pontos);

        String titulo = String.format(new Locale("pt", "BR"), "4. Correlação Ano x Preço (r = %.2f)", r);
        JFreeChart chart = ChartFactory.createScatterPlot(
                titulo, "Ano de Fabricação", "Preço (R$)", dataset,
                PlotOrientation.VERTICAL, false, true, false
        );

        XYPlot plot = chart.getXYPlot();
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);
        domainAxis.setNumberFormatOverride(new DecimalFormat("0000"));

        cpDispersaoCorrelacao.setChart(chart);
    }

    private void atualizarGrafico5PizzaMarcas(Map<String, Double> vendasMarca) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        double total = 0;
        for (double val : vendasMarca.values()) total += val;

        for (Map.Entry<String, Double> entry : vendasMarca.entrySet()) {
            double pct = total > 0 ? (entry.getValue() * 100.0 / total) : 0;
            String label = String.format(new Locale("pt", "BR"), "%s (%.1f%%)", entry.getKey(), pct);
            dataset.setValue(label, entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "5. Participação de Vendas por Marca (%)",
                dataset, true, true, false
        );
        cpPizzaMarcas.setChart(chart);
    }

    private void atualizarGrafico6RadarVendedores(List<Object[]> vendedores) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        double maxQtd = 1, maxFat = 1, maxTicket = 1;
        for (Object[] row : vendedores) {
            maxQtd = Math.max(maxQtd, ((Number) row[1]).doubleValue());
            maxFat = Math.max(maxFat, ((Number) row[2]).doubleValue());
            maxTicket = Math.max(maxTicket, ((Number) row[3]).doubleValue());
        }

        for (Object[] row : vendedores) {
            String nome = String.valueOf(row[0]);
            double qtdPct = (((Number) row[1]).doubleValue() / maxQtd) * 100.0;
            double fatPct = (((Number) row[2]).doubleValue() / maxFat) * 100.0;
            double ticketPct = (((Number) row[3]).doubleValue() / maxTicket) * 100.0;

            dataset.addValue(qtdPct, nome, "Volume de Vendas");
            dataset.addValue(fatPct, nome, "Faturamento Total");
            dataset.addValue(ticketPct, nome, "Ticket Médio");
        }

        SpiderWebPlot plot = new SpiderWebPlot(dataset);
        JFreeChart chart = new JFreeChart(
                "6. Radar de Desempenho por Vendedor (Escala Relativa)",
                JFreeChart.DEFAULT_TITLE_FONT, plot, true
        );
        cpRadarVendedores.setChart(chart);
    }

    // === MÉTODOS ESTATÍSTICOS ===

    private double calcularMediana(List<Double> ordenados) {
        int n = ordenados.size();
        if (n == 0) return 0.0;
        if (n % 2 == 1) {
            return ordenados.get(n / 2);
        }
        return (ordenados.get(n / 2 - 1) + ordenados.get(n / 2)) / 2.0;
    }

    private double calcularVarianciaAmostral(List<Double> valores, double media) {
        int n = valores.size();
        if (n <= 1) return 0.0;
        double somaQuadrados = 0.0;
        for (double v : valores) {
            double diff = v - media;
            somaQuadrados += diff * diff;
        }
        return somaQuadrados / (n - 1);
    }

    private double calcularModaOuPontoMedioModal(List<Double> valores) {
        if (valores.isEmpty()) return 0.0;

        // Verifica se há repetição exata de valor
        Map<Double, Integer> freqExata = new HashMap<>();
        double moda = valores.get(0);
        int maxFreq = 0;

        for (double v : valores) {
            int f = freqExata.getOrDefault(v, 0) + 1;
            freqExata.put(v, f);
            if (f > maxFreq) {
                maxFreq = f;
                moda = v;
            }
        }
        if (maxFreq > 1) return moda;

        Map<Integer, Integer> freqClasse = new HashMap<>();
        int classeModal = 0;
        int maxClasse = 0;
        for (double v : valores) {
            int classe = (int) (v / 20000);
            int f = freqClasse.getOrDefault(classe, 0) + 1;
            freqClasse.put(classe, f);
            if (f > maxClasse) {
                maxClasse = f;
                classeModal = classe;
            }
        }
        return (classeModal * 20000.0) + 10000.0;
    }

    private double calcularCorrelacaoPearson(List<double[]> pontos) {
        int n = pontos.size();
        if (n <= 1) return 0.0;

        double somaX = 0, somaY = 0;
        for (double[] p : pontos) {
            somaX += p[0];
            somaY += p[1];
        }
        double mediaX = somaX / n;
        double mediaY = somaY / n;

        double num = 0, denX = 0, denY = 0;
        for (double[] p : pontos) {
            double dx = p[0] - mediaX;
            double dy = p[1] - mediaY;
            num += dx * dy;
            denX += dx * dx;
            denY += dy * dy;
        }
        double denom = Math.sqrt(denX * denY);
        return denom > 0 ? (num / denom) : 0.0;
    }

    private double calcularTendenciaCrescimento(Map<String, Double> serieMensal) {
        if (serieMensal.size() < 2) return 0.0;
        Double[] vals = serieMensal.values().toArray(new Double[0]);
        int meio = vals.length / 2;

        double primeiraMetade = 0, segundaMetade = 0;
        for (int i = 0; i < meio; i++) primeiraMetade += vals[i];
        for (int i = meio; i < vals.length; i++) segundaMetade += vals[i];

        if (primeiraMetade == 0) return 0.0;
        return ((segundaMetade - primeiraMetade) / primeiraMetade) * 100.0;
    }

    private Date lerData(String texto) {
        if (texto == null || texto.trim().isEmpty()) return null;
        try {
            return Date.valueOf(LocalDate.parse(texto.trim(), formatoDataBR));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}