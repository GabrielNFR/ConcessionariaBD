package br.cesar.bd.concessionaria.view;

import br.cesar.bd.concessionaria.dao.CarroDAO;
import br.cesar.bd.concessionaria.dao.ClienteDAO;
import br.cesar.bd.concessionaria.dao.VendaDAO;
import br.cesar.bd.concessionaria.dao.VendedorDAO;
import br.cesar.bd.concessionaria.model.Cliente;
import br.cesar.bd.concessionaria.model.Vendedor;
import br.cesar.bd.concessionaria.util.ConnectionFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VendaView extends View {

    private VendedorDAO vendedorDAO = new VendedorDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();
    private CarroDAO carroDAO = new CarroDAO();
    private VendaDAO vendaDAO = new VendaDAO();

    private JComboBox<Vendedor> cbVendedor;
    private JComboBox<Cliente> cbCliente;
    private JTextField txtData;
    private JComboBox<CarroEstoqueItem> cbCarro;
    private JTextField txtPreco;
    private JLabel lblTotal;

    private JTable tabelaCarrinho;
    private DefaultTableModel modeloTabela;

    private Map<String, CarroEstoqueItem> itensNoCarrinho = new HashMap<>();
    private final NumberFormat moedaBR = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter formatoDataBR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static class CarroEstoqueItem {
        private String chassi;
        private String modeloMarca;
        private String cor;
        private int ano;
        private BigDecimal preco;

        public CarroEstoqueItem(String chassi, String modeloMarca, String cor, int ano, BigDecimal preco) {
            this.chassi = chassi;
            this.modeloMarca = modeloMarca;
            this.cor = cor;
            this.ano = ano;
            this.preco = preco;
        }

        public String getChassi() { return chassi; }
        public String getModeloMarca() { return modeloMarca; }
        public String getCor() { return cor; }
        public int getAno() { return ano; }
        public BigDecimal getPreco() { return preco; }

        @Override
        public String toString() {
            return modeloMarca + " (" + ano + ") - " + cor + " | Chassi: " + chassi;
        }
    }

    public VendaView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel painelTopo = new JPanel(new BorderLayout(5, 5));

        // 1. Dados da Venda (Vendedor, Cliente e Data)
        JPanel painelDadosVenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        painelDadosVenda.setBorder(BorderFactory.createTitledBorder("1. Dados da Venda"));

        painelDadosVenda.add(new JLabel("Vendedor:"));
        cbVendedor = new JComboBox<>();
        painelDadosVenda.add(cbVendedor);

        painelDadosVenda.add(new JLabel("Cliente:"));
        cbCliente = new JComboBox<>();
        painelDadosVenda.add(cbCliente);

        painelDadosVenda.add(new JLabel("Data:"));
        txtData = new JTextField(9);
        painelDadosVenda.add(txtData);

        // 2. Seleção de Carros em Estoque
        JPanel painelCarro = new JPanel(new GridLayout(2, 1, 5, 5));
        painelCarro.setBorder(BorderFactory.createTitledBorder("2. Adicionar Carros do Estoque"));

        JPanel linhaCarro = new JPanel(new BorderLayout(8, 0));
        linhaCarro.add(new JLabel("Carro em Estoque:"), BorderLayout.WEST);
        cbCarro = new JComboBox<>();
        linhaCarro.add(cbCarro, BorderLayout.CENTER);

        JPanel linhaAcoesCarro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linhaAcoesCarro.add(new JLabel("Valor de Venda (R$):"));
        txtPreco = new JTextField(12);
        linhaAcoesCarro.add(txtPreco);

        JButton btnAddCarro = new JButton("Adicionar à Lista");
        JButton btnRemoverCarro = new JButton("Remover Selecionado");
        linhaAcoesCarro.add(btnAddCarro);
        linhaAcoesCarro.add(btnRemoverCarro);

        painelCarro.add(linhaCarro);
        painelCarro.add(linhaAcoesCarro);

        painelTopo.add(painelDadosVenda, BorderLayout.NORTH);
        painelTopo.add(painelCarro, BorderLayout.CENTER);
        add(painelTopo, BorderLayout.NORTH);

        // Tabela Central
        String[] colunas = {"Chassi", "Modelo", "Cor", "Ano", "Preço"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaCarrinho = new JTable(modeloTabela);
        add(new JScrollPane(tabelaCarrinho), BorderLayout.CENTER);

        // Rodapé
        JPanel painelRodape = new JPanel(new BorderLayout(10, 0));

        JButton btnHistorico = new JButton("Ver Recibos (Histórico)");
        painelRodape.add(btnHistorico, BorderLayout.WEST);

        lblTotal = new JLabel("Total: R$ 0,00", SwingConstants.CENTER);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
        painelRodape.add(lblTotal, BorderLayout.CENTER);

        JButton btnFinalizar = new JButton("FINALIZAR VENDA");
        btnFinalizar.setFont(new Font("Arial", Font.BOLD, 14));
        btnFinalizar.setBackground(new Color(34, 139, 34));
        btnFinalizar.setForeground(Color.WHITE);
        painelRodape.add(btnFinalizar, BorderLayout.EAST);

        add(painelRodape, BorderLayout.SOUTH);

        // Eventos
        cbCarro.addActionListener(e -> {
            CarroEstoqueItem selecionado = (CarroEstoqueItem) cbCarro.getSelectedItem();
            if (selecionado != null && selecionado.getPreco() != null) {
                txtPreco.setText(selecionado.getPreco().toPlainString());
            } else {
                txtPreco.setText("");
            }
        });

        btnHistorico.addActionListener(e -> abrirDialogHistorico());

        btnAddCarro.addActionListener(e -> {
            if (cbVendedor.getSelectedItem() == null || cbCliente.getSelectedItem() == null) {
                mostrarMensagem("Cadastre/selecione um vendedor e um cliente antes de montar a venda.", true);
                return;
            }

            CarroEstoqueItem carroSelecionado = (CarroEstoqueItem) cbCarro.getSelectedItem();
            if (carroSelecionado == null) {
                mostrarMensagem("Não há carros disponíveis em estoque para adicionar.", true);
                return;
            }

            BigDecimal precoVenda = lerPreco(txtPreco.getText());
            if (precoVenda == null) {
                mostrarMensagem("Preço inválido. Use apenas números, por exemplo 65000,00.", true);
                return;
            }

            modeloTabela.addRow(new Object[]{
                carroSelecionado.getChassi(),
                carroSelecionado.getModeloMarca(),
                carroSelecionado.getCor(),
                carroSelecionado.getAno(),
                precoVenda
            });

            itensNoCarrinho.put(carroSelecionado.getChassi(), carroSelecionado);
            cbCarro.removeItem(carroSelecionado);
            atualizarTotal();
        });

        btnRemoverCarro.addActionListener(e -> {
            int linha = tabelaCarrinho.getSelectedRow();
            if (linha == -1) {
                mostrarMensagem("Selecione um carro na tabela para remover.", true);
                return;
            }

            String chassi = modeloTabela.getValueAt(linha, 0).toString();
            modeloTabela.removeRow(linha);

            CarroEstoqueItem devolvido = itensNoCarrinho.remove(chassi);
            if (devolvido != null) {
                cbCarro.addItem(devolvido);
            }
            atualizarTotal();
        });

        btnFinalizar.addActionListener(e -> finalizarVenda());
    }

    @Override
    public void aoAbrir() {
        modeloTabela.setRowCount(0);
        itensNoCarrinho.clear();
        txtData.setText(LocalDate.now().format(formatoDataBR));
        txtPreco.setText("");
        atualizarTotal();

        carregarVendedores();
        carregarClientes();
        carregarCarrosDisponiveis();
    }

    private void carregarVendedores() {
        try {
            cbVendedor.removeAllItems();
            for (Vendedor v : vendedorDAO.listar()) {
                cbVendedor.addItem(v);
            }
        } catch (SQLException e) {
            mostrarMensagem("Erro ao carregar vendedores: " + e.getMessage(), true);
        }
    }

    private void carregarClientes() {
        try {
            cbCliente.removeAllItems();
            for (Cliente c : clienteDAO.listar()) {
                cbCliente.addItem(c);
            }
        } catch (SQLException e) {
            mostrarMensagem("Erro ao carregar clientes: " + e.getMessage(), true);
        }
    }

    private void carregarCarrosDisponiveis() {
        try {
            cbCarro.removeAllItems();
            for (Object[] row : carroDAO.listarParaTabela()) {
                String status = row[7].toString();
                if ("Em estoque".equals(status)) {
                    String chassi = row[0].toString();
                    String modeloMarca = row[1] + " " + row[2];
                    String cor = row[3].toString();
                    int ano = (int) row[4];
                    BigDecimal preco = (BigDecimal) row[5];

                    cbCarro.addItem(new CarroEstoqueItem(chassi, modeloMarca, cor, ano, preco));
                }
            }
        } catch (SQLException e) {
            mostrarMensagem("Erro ao carregar carros em estoque: " + e.getMessage(), true);
        }
    }

    private void atualizarTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < modeloTabela.getRowCount(); i++) {
            BigDecimal preco = (BigDecimal) modeloTabela.getValueAt(i, 4);
            total = total.add(preco);
        }
        lblTotal.setText("Total: " + moedaBR.format(total));
    }

    private void abrirDialogHistorico() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Histórico de Vendas", true);
        dialog.setSize(650, 400);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        String[] colunas = {"ID Venda", "Data", "Valor Total (R$)", "Vendedor", "Cliente"};
        DefaultTableModel modelHistorico = new DefaultTableModel(colunas, 0);
        JTable tabelaHistorico = new JTable(modelHistorico);

        try {
            List<Object[]> recibos = vendaDAO.listarParaTabela();
            for (Object[] row : recibos) {
                modelHistorico.addRow(row);
            }
        } catch (SQLException e) {
            mostrarMensagem("Erro ao carregar histórico: " + e.getMessage(), true);
        }

        dialog.add(new JScrollPane(tabelaHistorico), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private BigDecimal lerPreco(String texto) {
        if (texto == null) return null;
        String limpo = texto.trim().replace("R$", "").replace(" ", "");
        if (limpo.isEmpty()) return null;
        if (limpo.contains(",")) {
            limpo = limpo.replace(".", "").replace(",", ".");
        }
        try {
            BigDecimal valor = new BigDecimal(limpo);
            return valor.compareTo(BigDecimal.ZERO) > 0 ? valor : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void finalizarVenda() {
        if (modeloTabela.getRowCount() == 0) {
            mostrarMensagem("Adicione ao menos um carro na lista!", true);
            return;
        }

        Vendedor vendedor = (Vendedor) cbVendedor.getSelectedItem();
        Cliente cliente = (Cliente) cbCliente.getSelectedItem();
        if (vendedor == null || cliente == null) {
            mostrarMensagem("Selecione o vendedor e o cliente da venda!", true);
            return;
        }

        // Lê a data digitada (sem bloquear data futura no Java, para o Trigger do MySQL validar!)
        Date dataVenda = null;
        String textoData = txtData.getText().trim();
        if (!textoData.isEmpty()) {
            try {
                LocalDate localDate = LocalDate.parse(textoData, formatoDataBR);
                dataVenda = Date.valueOf(localDate);
            } catch (DateTimeParseException e) {
                mostrarMensagem("Data inválida. Use o formato dd/MM/yyyy (ex: 17/11/2026).", true);
                return;
            }
        }

        BigDecimal valorTotalVenda = BigDecimal.ZERO;
        for (int i = 0; i < modeloTabela.getRowCount(); i++) {
            BigDecimal precoCarro = (BigDecimal) modeloTabela.getValueAt(i, 4);
            valorTotalVenda = valorTotalVenda.add(precoCarro);
        }

        String sqlVenda = "INSERT INTO venda (data, valor, vendedor_id, cliente_CPF) "
                + "VALUES (COALESCE(?, CURRENT_DATE), ?, ?, ?)";
        String sqlCarro = "UPDATE carro SET venda_id = ? WHERE chassi = ? AND venda_id IS NULL";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int idVendaGerado = 0;

                try (PreparedStatement psVenda = conn.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                    psVenda.setDate(1, dataVenda);
                    psVenda.setBigDecimal(2, valorTotalVenda);
                    psVenda.setInt(3, vendedor.getId());
                    psVenda.setString(4, cliente.getCpf());
                    psVenda.executeUpdate();

                    ResultSet rs = psVenda.getGeneratedKeys();
                    if (rs.next()) {
                        idVendaGerado = rs.getInt(1);
                    }
                }

                try (PreparedStatement psCarro = conn.prepareStatement(sqlCarro)) {
                    for (int i = 0; i < modeloTabela.getRowCount(); i++) {
                        String chassi = modeloTabela.getValueAt(i, 0).toString();

                        psCarro.setInt(1, idVendaGerado);
                        psCarro.setString(2, chassi);

                        int atualizados = psCarro.executeUpdate();
                        if (atualizados == 0) {
                            throw new SQLException("O carro de chassi " + chassi + " não está mais disponível.");
                        }
                    }
                }

                conn.commit();
                mostrarMensagem("Venda de " + moedaBR.format(valorTotalVenda) + " finalizada com sucesso!", false);
                aoAbrir();

            } catch (SQLException erroSql) {
                conn.rollback();
                throw erroSql;
            }

        } catch (SQLException e) {
            mostrarMensagem("Erro ao finalizar venda: " + e.getMessage(), true);
        }
    }
}