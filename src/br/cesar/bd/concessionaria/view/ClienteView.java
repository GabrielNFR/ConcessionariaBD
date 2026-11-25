package br.cesar.bd.concessionaria.view;

import br.cesar.bd.concessionaria.dao.ClienteDAO;
import br.cesar.bd.concessionaria.dao.EnderecoDAO;
import br.cesar.bd.concessionaria.model.Cliente;
import br.cesar.bd.concessionaria.model.Endereco;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class ClienteView extends View {
    
    private ClienteDAO clienteDAO = new ClienteDAO();
    private EnderecoDAO enderecoDAO = new EnderecoDAO();

    private JTextField txtCpf;
    private JTextField txtNome;

    private JTextField txtCep;
    private JTextField txtLogradouro;
    private JTextField txtNumero;
    private JTextField txtComplemento;
    private JTextField txtBairro;
    private JTextField txtCidade;
    private JTextField txtEstado;

    private JTable tabela;
    private DefaultTableModel modeloTabela;
    
    private int enderecoIdAtual = -1; 

    public ClienteView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        JPanel painelTopo = new JPanel(new BorderLayout(0, 10));

        JPanel painelCliente = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelCliente.setBorder(BorderFactory.createTitledBorder("Dados Pessoais"));
        painelCliente.add(new JLabel("CPF:"));
        txtCpf = new JTextField(12);
        painelCliente.add(txtCpf);
        
        painelCliente.add(new JLabel("Nome Completo:"));
        txtNome = new JTextField(25);
        painelCliente.add(txtNome);

        // Dois pares rótulo+campo por linha. O GridLayout antigo (3x4) recebia 14
        // componentes, então o layout recalculava para 3x5 e desalinhava tudo: o "Número:"
        // ficava ao lado do campo do Logradouro. Cada par agora é um painel próprio, então
        // o rótulo nunca se separa do seu campo.
        JPanel painelEndereco = new JPanel(new GridLayout(0, 2, 12, 6));
        painelEndereco.setBorder(BorderFactory.createTitledBorder("Endereço"));

        txtCep = new JTextField();
        txtLogradouro = new JTextField();
        txtNumero = new JTextField();
        txtComplemento = new JTextField();
        txtBairro = new JTextField();
        txtCidade = new JTextField();
        txtEstado = new JTextField();

        painelEndereco.add(campoComRotulo("CEP:", txtCep));
        painelEndereco.add(campoComRotulo("Logradouro:", txtLogradouro));
        painelEndereco.add(campoComRotulo("Número:", txtNumero));
        painelEndereco.add(campoComRotulo("Complemento:", txtComplemento));
        painelEndereco.add(campoComRotulo("Bairro:", txtBairro));
        painelEndereco.add(campoComRotulo("Cidade:", txtCidade));
        painelEndereco.add(campoComRotulo("Estado (UF):", txtEstado));

        JPanel painelFormularios = new JPanel(new BorderLayout());
        painelFormularios.add(painelCliente, BorderLayout.NORTH);
        painelFormularios.add(painelEndereco, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnSalvar = new JButton("Salvar Novo");
        JButton btnAtualizar = new JButton("Atualizar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnLimpar = new JButton("Limpar Form");

        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnLimpar);

        painelTopo.add(painelFormularios, BorderLayout.CENTER);
        painelTopo.add(painelBotoes, BorderLayout.SOUTH);
        add(painelTopo, BorderLayout.NORTH);

        String[] colunas = {"CPF", "Nome", "Cidade", "Estado"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabela = new JTable(modeloTabela);
        add(new JScrollPane(tabela), BorderLayout.CENTER);


        
        btnSalvar.addActionListener(e -> {
            if (!validarFormulario()) {
                return;
            }
            int idEndGerado = -1;
            try {
                Endereco end = extrairEnderecoDoFormulario();
                idEndGerado = enderecoDAO.inserir(end);
                
                Cliente cli = new Cliente(txtCpf.getText().trim(), txtNome.getText().trim(), idEndGerado);
                clienteDAO.inserir(cli);
                
                mostrarMensagem("Cliente salvo com sucesso!", false);
                limparCampos();
                atualizarTabela();
            } catch (SQLException ex) {
                if (idEndGerado != -1) {
                    try { enderecoDAO.excluir(idEndGerado); } catch (SQLException ignorada) { }
                }
                mostrarMensagem("Erro ao salvar: " + ex.getMessage(), true);
            }
        });

        btnAtualizar.addActionListener(e -> {
            if (enderecoIdAtual == -1) {
                mostrarMensagem("Selecione um cliente na tabela para atualizar!", true);
                return;
            }
            if (!validarFormulario()) {
                return;
            }
            try {
                Endereco end = extrairEnderecoDoFormulario();
                end.setId(enderecoIdAtual); 
                enderecoDAO.atualizar(end);
                
                Cliente cli = new Cliente(txtCpf.getText().trim(), txtNome.getText().trim(), enderecoIdAtual);
                clienteDAO.atualizar(cli);
                
                mostrarMensagem("Cliente atualizado!", false);
                limparCampos();
                atualizarTabela();
            } catch (SQLException ex) {
                mostrarMensagem("Erro ao atualizar: " + ex.getMessage(), true);
            }
        });

        btnExcluir.addActionListener(e -> {
            if (enderecoIdAtual == -1) return;
            
            int confirm = JOptionPane.showConfirmDialog(this, "Deseja realmente excluir este cliente?", "Aviso", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    clienteDAO.excluir(txtCpf.getText().trim());
                    enderecoDAO.excluirSeNaoUsado(enderecoIdAtual);
                    
                    mostrarMensagem("Cliente excluído!", false);
                    limparCampos();
                    atualizarTabela();
                } catch (SQLException ex) {
                    mostrarMensagem("Erro ao excluir: " + ex.getMessage(), true);
                }
            }
        });

        btnLimpar.addActionListener(e -> limparCampos());

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabela.getSelectedRow() != -1) {
                preencherFormularioPelaTabela();
            }
        });
    }

    @Override
    public void aoAbrir() {
        limparCampos();
        atualizarTabela();
    }

    private void atualizarTabela() {
        try {
            modeloTabela.setRowCount(0);
            for (Object[] linha : clienteDAO.listarParaTabela()) {
                modeloTabela.addRow(linha);
            }
        } catch (SQLException e) {
            mostrarMensagem("Erro ao atualizar tabela: " + e.getMessage(), true);
        }
    }

    private void preencherFormularioPelaTabela() {
        try {
            String cpfSelecionado = tabela.getValueAt(tabela.getSelectedRow(), 0).toString();
            
            Cliente clienteSelecionado = null;
            for (Cliente c : clienteDAO.listar()) {
                if (c.getCpf().equals(cpfSelecionado)) {
                    clienteSelecionado = c;
                    break;
                }
            }

            if (clienteSelecionado != null) {
                txtCpf.setText(clienteSelecionado.getCpf());
                txtNome.setText(clienteSelecionado.getNome());
                txtCpf.setEnabled(false);                 
                enderecoIdAtual = clienteSelecionado.getEnderecoId();
                
                Endereco end = enderecoDAO.buscarPorId(enderecoIdAtual);
                if (end != null) {
                    txtCep.setText(end.getCep());
                    txtLogradouro.setText(end.getLogradouro());
                    txtNumero.setText(end.getNumero());
                    txtComplemento.setText(end.getComplemento());
                    txtBairro.setText(end.getBairro());
                    txtCidade.setText(end.getCidade());
                    txtEstado.setText(end.getEstado());
                }
            }
        } catch (SQLException e) {
            mostrarMensagem("Erro ao buscar detalhes: " + e.getMessage(), true);
        }
    }

    /** Um par rótulo + campo, com o rótulo com largura fixa para os campos ficarem alinhados. */
    private static JPanel campoComRotulo(String rotulo, JTextField campo) {
        JPanel par = new JPanel(new BorderLayout(6, 0));
        JLabel label = new JLabel(rotulo);
        label.setPreferredSize(new Dimension(104, label.getPreferredSize().height));
        par.add(label, BorderLayout.WEST);
        par.add(campo, BorderLayout.CENTER);
        return par;
    }

    /**
     * Confere os campos obrigatórios antes de gravar. Sem isso, um clique em "Salvar Novo"
     * com o formulário em branco gravava endereço e cliente com strings vazias — e o CPF
     * vazio ocupava a chave primária, impedindo o cadastro seguinte.
     */
    private boolean validarFormulario() {
        String cpf = txtCpf.getText().trim();
        if (!cpf.matches("\\d{11}")) {
            mostrarMensagem("CPF inválido. Informe os 11 dígitos, só números.", true);
            txtCpf.requestFocus();
            return false;
        }
        if (txtNome.getText().trim().isEmpty()) {
            mostrarMensagem("Informe o nome do cliente.", true);
            txtNome.requestFocus();
            return false;
        }
        if (txtLogradouro.getText().trim().isEmpty()) {
            mostrarMensagem("Informe o logradouro do endereço.", true);
            txtLogradouro.requestFocus();
            return false;
        }
        if (txtCidade.getText().trim().isEmpty()) {
            mostrarMensagem("Informe a cidade do endereço.", true);
            txtCidade.requestFocus();
            return false;
        }
        if (!txtEstado.getText().trim().matches("[A-Za-z]{2}")) {
            mostrarMensagem("UF inválida. Informe a sigla com 2 letras (ex: PE).", true);
            txtEstado.requestFocus();
            return false;
        }
        String cep = txtCep.getText().trim();
        if (!cep.isEmpty() && !cep.matches("\\d{8}")) {
            mostrarMensagem("CEP inválido. Informe os 8 dígitos ou deixe em branco.", true);
            txtCep.requestFocus();
            return false;
        }
        return true;
    }

    private Endereco extrairEnderecoDoFormulario() {
        return new Endereco(
            0, 
            txtLogradouro.getText().trim(),
            txtNumero.getText().trim(),
            txtComplemento.getText().trim(),
            txtBairro.getText().trim(),
            txtCidade.getText().trim(),
            txtEstado.getText().trim(),
            txtCep.getText().trim()
        );
    }

    public void limparCampos() {
        txtCpf.setText("");
        txtNome.setText("");
        txtCep.setText("");
        txtLogradouro.setText("");
        txtNumero.setText("");
        txtComplemento.setText("");
        txtBairro.setText("");
        txtCidade.setText("");
        txtEstado.setText("");
        
        txtCpf.setEnabled(true);
        enderecoIdAtual = -1;
        tabela.clearSelection();
        txtCpf.requestFocus();
    }
}