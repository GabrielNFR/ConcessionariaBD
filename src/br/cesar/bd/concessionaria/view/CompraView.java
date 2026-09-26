package br.cesar.bd.concessionaria.view;

import br.cesar.bd.concessionaria.dao.*;
import br.cesar.bd.concessionaria.model.*;
import br.cesar.bd.concessionaria.util.ConnectionFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

public class CompraView extends View {
    
    private FornecedorDAO fornecedorDAO = new FornecedorDAO();
    private MarcaDAO marcaDAO = new MarcaDAO();
    private ModeloDAO modeloDAO = new ModeloDAO(); 
    private CorDAO corDAO = new CorDAO();
    private CompraDAO compraDAO = new CompraDAO();

    private JComboBox<Fornecedor> cbFornecedor;
    private JComboBox<Cor> cbCor; 
    private JComboBox<ModeloComboItem> cbModelo; 
    
    private JTextField txtAno;
    private JTextField txtPreco;
    private JTextField txtChassi;

    private JTable tabelaCarrinho;
    private DefaultTableModel modeloTabela;

    private static class ModeloComboItem {
        private int id;
        private String nomeModelo;
        private String nomeMarca;

        public ModeloComboItem(int id, String nomeModelo, String nomeMarca) {
            this.id = id;
            this.nomeModelo = nomeModelo;
            this.nomeMarca = nomeMarca;
        }

        public int getId() { return id; }

        @Override
        public String toString() {
            return nomeMarca + " " + nomeModelo; 
        }
    }

    public CompraView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel painelTopo = new JPanel(new BorderLayout(5, 5));
        
        JPanel painelFornecedor = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelFornecedor.setBorder(BorderFactory.createTitledBorder("1. Dados da Compra"));
        painelFornecedor.add(new JLabel("Fornecedor:"));
        cbFornecedor = new JComboBox<>();
        painelFornecedor.add(cbFornecedor);

        JPanel painelCarro = new JPanel(new GridLayout(2, 6, 5, 5));
        painelCarro.setBorder(BorderFactory.createTitledBorder("2. Adicionar Carros ao Lote"));
        
        painelCarro.add(new JLabel("Modelo:"));
        JPanel panelModeloAdd = new JPanel(new BorderLayout());
        cbModelo = new JComboBox<>();
        JButton btnAddModelo = new JButton("+");
        panelModeloAdd.add(cbModelo, BorderLayout.CENTER);
        panelModeloAdd.add(btnAddModelo, BorderLayout.EAST);
        painelCarro.add(panelModeloAdd);

        painelCarro.add(new JLabel("Cor:"));
        JPanel panelCorAdd = new JPanel(new BorderLayout());
        cbCor = new JComboBox<>();
        JButton btnAddCor = new JButton("+");
        panelCorAdd.add(cbCor, BorderLayout.CENTER);
        panelCorAdd.add(btnAddCor, BorderLayout.EAST);
        painelCarro.add(panelCorAdd);

        painelCarro.add(new JLabel("Ano:"));
        txtAno = new JTextField();
        painelCarro.add(txtAno);

        painelCarro.add(new JLabel("Preço (R$):"));
        txtPreco = new JTextField();
        painelCarro.add(txtPreco);

        painelCarro.add(new JLabel("Chassi:"));
        txtChassi = new JTextField();
        painelCarro.add(txtChassi);

        JButton btnAddCarro = new JButton("Adicionar à Lista");
        painelCarro.add(new JLabel("")); 
        painelCarro.add(btnAddCarro);

        painelTopo.add(painelFornecedor, BorderLayout.NORTH);
        painelTopo.add(painelCarro, BorderLayout.CENTER);
        add(painelTopo, BorderLayout.NORTH);

        String[] colunas = {"Chassi", "Modelo", "Cor", "Ano", "Preço"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaCarrinho = new JTable(modeloTabela);
        add(new JScrollPane(tabelaCarrinho), BorderLayout.CENTER);

        JPanel painelRodape = new JPanel(new BorderLayout());
        
        JButton btnHistorico = new JButton("Ver Recibos (Histórico)");
        painelRodape.add(btnHistorico, BorderLayout.WEST);         
        JButton btnFinalizar = new JButton("FINALIZAR COMPRA");
        btnFinalizar.setFont(new Font("Arial", Font.BOLD, 14));
        btnFinalizar.setBackground(new Color(34, 139, 34)); 
        btnFinalizar.setForeground(Color.WHITE);
        painelRodape.add(btnFinalizar, BorderLayout.EAST);         
        add(painelRodape, BorderLayout.SOUTH);

        btnAddCor.addActionListener(e -> {
            String novaCor = JOptionPane.showInputDialog(this, "Digite o nome da nova cor:");
            if (novaCor != null && !novaCor.trim().isEmpty()) {
                try {
                    mostrarMensagem("Cor adicionada!", false);
                } catch (Exception ex) {
                    mostrarMensagem("Erro ao salvar cor.", true);
                }
            }
        });

        btnAddModelo.addActionListener(e -> abrirDialogNovoModelo());

        btnHistorico.addActionListener(e -> abrirDialogHistorico());


        btnAddCarro.addActionListener(e -> {
            if (txtChassi.getText().isEmpty() || txtPreco.getText().isEmpty()) {
                mostrarMensagem("Preencha todos os campos do carro!", true);
                return;
            }
            modeloTabela.addRow(new Object[]{
                txtChassi.getText(), cbModelo.getSelectedItem(), cbCor.getSelectedItem(), txtAno.getText(), txtPreco.getText()
            });
            txtChassi.setText("");
            txtChassi.requestFocus(); 
        });

        btnFinalizar.addActionListener(e -> finalizarCompra());
    }

    @Override
    public void aoAbrir() {
        modeloTabela.setRowCount(0);
        txtChassi.setText("");
        txtPreco.setText("");
        
        carregarFornecedores();
        carregarModelos();
        carregarCores();
    }

    private void carregarFornecedores() {
        try {
            cbFornecedor.removeAllItems();
            for (Fornecedor f : fornecedorDAO.listar()) cbFornecedor.addItem(f);
        } catch (SQLException e) { mostrarMensagem("Erro", true); }
    }

    private void carregarModelos() {
        try {
            cbModelo.removeAllItems();
            for (Object[] linha : modeloDAO.listarParaTabela()) {
                cbModelo.addItem(new ModeloComboItem((int) linha[0], (String) linha[1], (String) linha[2]));
            }
        } catch (SQLException e) { mostrarMensagem("Erro", true); }
    }

    private void carregarCores() {
        try {
            cbCor.removeAllItems();
            for (Cor c : corDAO.listar()) cbCor.addItem(c);
        } catch (SQLException e) { mostrarMensagem("Erro", true); }
    }


    private void abrirDialogNovoModelo() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Novo Modelo", true);
        dialog.setSize(400, 200); 
        dialog.setLayout(new GridLayout(3, 1, 10, 10));
        dialog.setLocationRelativeTo(this);

        JPanel pnlNome = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlNome.add(new JLabel("Nome do Modelo:"));
        JTextField txtNome = new JTextField(15);
        pnlNome.add(txtNome);

        JPanel pnlMarca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlMarca.add(new JLabel("Marca:"));
        
        JComboBox<Marca> cbMarca = new JComboBox<>();
        
        Runnable atualizarComboMarcas = () -> {
            try {
                cbMarca.removeAllItems();
                for (Marca m : marcaDAO.listar()) { 
                    cbMarca.addItem(m);
                }
            } catch (SQLException ex) {
                mostrarMensagem("Erro ao carregar marcas: " + ex.getMessage(), true);
            }
        };
        
        atualizarComboMarcas.run();
        pnlMarca.add(cbMarca);

        JButton btnNovaMarca = new JButton("+");
        btnNovaMarca.addActionListener(e -> {
            String novaMarcaStr = JOptionPane.showInputDialog(dialog, "Digite o nome da nova Marca:");
            if (novaMarcaStr != null && !novaMarcaStr.trim().isEmpty()) {
                try {
                    atualizarComboMarcas.run();
                    mostrarMensagem("Marca cadastrada!", false);
                } catch (Exception ex) {
                    mostrarMensagem("Erro ao salvar marca.", true);
                }
            }
        });
        pnlMarca.add(btnNovaMarca);

        JButton btnSalvar = new JButton("Salvar Novo Modelo");
        btnSalvar.addActionListener(ev -> {
            if (txtNome.getText().trim().isEmpty() || cbMarca.getSelectedItem() == null) {
                mostrarMensagem("Preencha o nome e selecione uma marca!", true);
                return;
            }
            
            try {
                Marca marcaSelecionada = (Marca) cbMarca.getSelectedItem();
                int marcaId = marcaSelecionada.getId();
                
                
                dialog.dispose();
                carregarModelos();
                mostrarMensagem("Modelo salvo!", false);
            } catch (Exception ex) {
                mostrarMensagem("Erro: " + ex.getMessage(), true);
            }
        });

        dialog.add(pnlNome);
        dialog.add(pnlMarca);
        dialog.add(btnSalvar);
        dialog.setVisible(true);
    }

    private void abrirDialogHistorico() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Histórico de Recibos", true);
        dialog.setSize(600, 400);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        String[] colunas = {"ID Compra", "Data", "Valor Total (R$)", "Fornecedor"};
        DefaultTableModel modelHistorico = new DefaultTableModel(colunas, 0);
        JTable tabelaHistorico = new JTable(modelHistorico);

        try {
            List<Object[]> recibos = compraDAO.listarParaTabela();
            for (Object[] row : recibos) {
                modelHistorico.addRow(row);
            }
        } catch (SQLException e) {
            mostrarMensagem("Erro ao carregar histórico: " + e.getMessage(), true);
        }

        dialog.add(new JScrollPane(tabelaHistorico), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void finalizarCompra() {
        if (modeloTabela.getRowCount() == 0) {
            mostrarMensagem("Adicione ao menos um carro na lista!", true);
            return;
        }

        Fornecedor fornecedor = (Fornecedor) cbFornecedor.getSelectedItem();
        
        BigDecimal valorTotalCompra = BigDecimal.ZERO;
        for (int i = 0; i < modeloTabela.getRowCount(); i++) {
            BigDecimal precoCarro = new BigDecimal(modeloTabela.getValueAt(i, 4).toString());
            valorTotalCompra = valorTotalCompra.add(precoCarro);
        }
        
        String sqlCompra = "INSERT INTO compra (data, valor, fornecedor_cnpj) VALUES (CURRENT_DATE, ?, ?)";
        String sqlCarro = "INSERT INTO carro (chassi, ano, preco, cor_id, modelo_id, compra_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false); 
            
            try {
                int idCompraGerado = 0;
                
                try (PreparedStatement psCompra = conn.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS)) {
                    psCompra.setBigDecimal(1, valorTotalCompra);
                    psCompra.setString(2, fornecedor.getCnpj());
                    psCompra.executeUpdate();
                    
                    ResultSet rs = psCompra.getGeneratedKeys();
                    if (rs.next()) {
                        idCompraGerado = rs.getInt(1);
                    }
                }

                try (PreparedStatement psCarro = conn.prepareStatement(sqlCarro)) {
                    for (int i = 0; i < modeloTabela.getRowCount(); i++) {
                        String chassi = modeloTabela.getValueAt(i, 0).toString();
                        ModeloComboItem modeloSelecionado = (ModeloComboItem) modeloTabela.getValueAt(i, 1);
                        Cor corSelecionada = (Cor) modeloTabela.getValueAt(i, 2);
                        int ano = Integer.parseInt(modeloTabela.getValueAt(i, 3).toString());
                        BigDecimal preco = new BigDecimal(modeloTabela.getValueAt(i, 4).toString());

                        psCarro.setString(1, chassi);
                        psCarro.setInt(2, ano);
                        psCarro.setBigDecimal(3, preco);
                        psCarro.setInt(4, corSelecionada.getId()); 
                        psCarro.setInt(5, modeloSelecionado.getId()); 
                        psCarro.setInt(6, idCompraGerado); 
                        
                        psCarro.executeUpdate();
                    }
                }
                
                conn.commit(); 
                mostrarMensagem("Compra de R$ " + valorTotalCompra + " finalizada com sucesso!", false);
                aoAbrir();

            } catch (SQLException erroSql) {
                conn.rollback(); 
                throw erroSql;
            }

        } catch (SQLException e) {
            mostrarMensagem("Erro ao finalizar compra: " + e.getMessage(), true);
        }
    }
}