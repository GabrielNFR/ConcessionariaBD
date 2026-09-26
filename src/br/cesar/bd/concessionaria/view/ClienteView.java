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

        JPanel painelEndereco = new JPanel(new GridLayout(3, 4, 5, 5));
        painelEndereco.setBorder(BorderFactory.createTitledBorder("Endereço"));
        
        painelEndereco.add(new JLabel("CEP:"));
        txtCep = new JTextField();
        painelEndereco.add(txtCep);
        
        painelEndereco.add(new JLabel("Logradouro:"));
        txtLogradouro = new JTextField();
        painelEndereco.add(txtLogradouro);
        
        painelEndereco.add(new JLabel("Número:"));
        txtNumero = new JTextField();
        painelEndereco.add(txtNumero);
        
        painelEndereco.add(new JLabel("Complemento:"));
        txtComplemento = new JTextField();
        painelEndereco.add(txtComplemento);
        
        painelEndereco.add(new JLabel("Bairro:"));
        txtBairro = new JTextField();
        painelEndereco.add(txtBairro);
        
        painelEndereco.add(new JLabel("Cidade:"));
        txtCidade = new JTextField();
        painelEndereco.add(txtCidade);
        
        painelEndereco.add(new JLabel("Estado (UF):"));
        txtEstado = new JTextField();
        painelEndereco.add(txtEstado);

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
            try {
                Endereco end = extrairEnderecoDoFormulario();
                int idEndGerado = enderecoDAO.inserir(end);
                
                Cliente cli = new Cliente(txtCpf.getText(), txtNome.getText(), idEndGerado);
                clienteDAO.inserir(cli);
                
                mostrarMensagem("Cliente salvo com sucesso!", false);
                limparCampos();
                atualizarTabela();
            } catch (SQLException ex) {
                mostrarMensagem("Erro ao salvar: " + ex.getMessage(), true);
            }
        });

        btnAtualizar.addActionListener(e -> {
            if (enderecoIdAtual == -1) {
                mostrarMensagem("Selecione um cliente na tabela para atualizar!", true);
                return;
            }
            try {
                Endereco end = extrairEnderecoDoFormulario();
                end.setId(enderecoIdAtual); 
                enderecoDAO.atualizar(end);
                
                Cliente cli = new Cliente(txtCpf.getText(), txtNome.getText(), enderecoIdAtual);
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
                    clienteDAO.excluir(txtCpf.getText());
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

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

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

    private Endereco extrairEnderecoDoFormulario() {
        return new Endereco(
            0, 
            txtLogradouro.getText(),
            txtNumero.getText(),
            txtComplemento.getText(),
            txtBairro.getText(),
            txtCidade.getText(),
            txtEstado.getText(),
            txtCep.getText()
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