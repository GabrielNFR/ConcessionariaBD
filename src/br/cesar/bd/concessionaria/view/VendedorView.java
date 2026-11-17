package br.cesar.bd.concessionaria.view;

import br.cesar.bd.concessionaria.dao.VendedorDAO;
import br.cesar.bd.concessionaria.model.Vendedor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;

public class VendedorView extends View {

    private VendedorDAO vendedorDAO = new VendedorDAO();

    private JTextField txtNome;
    private JTextField txtComissao;

    private JTable tabela;
    private DefaultTableModel modeloTabela;

    private int vendedorIdAtual = -1;

    public VendedorView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel painelTopo = new JPanel(new BorderLayout(0, 10));

        JPanel painelForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        painelForm.setBorder(BorderFactory.createTitledBorder("Dados do Vendedor"));

        painelForm.add(new JLabel("Nome Completo:"));
        txtNome = new JTextField(25);
        painelForm.add(txtNome);

        painelForm.add(new JLabel("Comissão (%):"));
        txtComissao = new JTextField(10);
        painelForm.add(txtComissao);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnSalvar = new JButton("Salvar Novo");
        JButton btnAtualizar = new JButton("Atualizar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnLimpar = new JButton("Limpar Form");

        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnLimpar);

        painelTopo.add(painelForm, BorderLayout.CENTER);
        painelTopo.add(painelBotoes, BorderLayout.SOUTH);
        add(painelTopo, BorderLayout.NORTH);

        String[] colunas = {"ID", "Nome", "Comissão"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabela = new JTable(modeloTabela);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        btnSalvar.addActionListener(e -> salvarVendedor());
        btnAtualizar.addActionListener(e -> atualizarVendedor());
        btnExcluir.addActionListener(e -> excluirVendedor());
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
            for (Vendedor v : vendedorDAO.listar()) {
                modeloTabela.addRow(new Object[]{
                    v.getId(),
                    v.getNome(),
                    v.getComissao()
                });
            }
        } catch (SQLException e) {
            mostrarMensagem("Erro ao atualizar tabela: " + e.getMessage(), true);
        }
    }

    private void salvarVendedor() {
        Vendedor vendedor = validarEConstruirVendedor(0);
        if (vendedor == null) return;

        try {
            vendedorDAO.inserir(vendedor);
            mostrarMensagem("Vendedor salvo com sucesso!", false);
            limparCampos();
            atualizarTabela();
        } catch (SQLException ex) {
            mostrarMensagem("Erro ao salvar vendedor: " + ex.getMessage(), true);
        }
    }

    private void atualizarVendedor() {
        if (vendedorIdAtual == -1) {
            mostrarMensagem("Selecione um vendedor na tabela para atualizar!", true);
            return;
        }

        Vendedor vendedor = validarEConstruirVendedor(vendedorIdAtual);
        if (vendedor == null) return;

        try {
            vendedorDAO.atualizar(vendedor);
            mostrarMensagem("Vendedor atualizado com sucesso!", false);
            limparCampos();
            atualizarTabela();
        } catch (SQLException ex) {
            mostrarMensagem("Erro ao atualizar vendedor: " + ex.getMessage(), true);
        }
    }

    private void excluirVendedor() {
        if (vendedorIdAtual == -1) {
            mostrarMensagem("Selecione um vendedor na tabela para excluir!", true);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente excluir este vendedor?",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                vendedorDAO.excluir(vendedorIdAtual);
                mostrarMensagem("Vendedor excluído!", false);
                limparCampos();
                atualizarTabela();
            } catch (SQLException ex) {
                mostrarMensagem("Erro ao excluir vendedor: " + ex.getMessage(), true);
            }
        }
    }

    private void preencherFormularioPelaTabela() {
        int linha = tabela.getSelectedRow();
        vendedorIdAtual = (int) tabela.getValueAt(linha, 0);
        txtNome.setText(tabela.getValueAt(linha, 1).toString());

        Object comissaoObj = tabela.getValueAt(linha, 2);
        if (comissaoObj instanceof BigDecimal) {
            txtComissao.setText(((BigDecimal) comissaoObj).toPlainString());
        } else if (comissaoObj != null) {
            txtComissao.setText(comissaoObj.toString());
        } else {
            txtComissao.setText("");
        }
    }

    private Vendedor validarEConstruirVendedor(int id) {
        String nome = txtNome.getText().trim();
        if (nome.isEmpty()) {
            mostrarMensagem("Informe o nome do vendedor!", true);
            txtNome.requestFocus();
            return null;
        }

        BigDecimal comissao = lerComissao(txtComissao.getText());
        if (comissao == null) {
            mostrarMensagem("Comissão inválida. Informe um valor numérico maior ou igual a zero (ex: 5,00).", true);
            txtComissao.requestFocus();
            return null;
        }

        return new Vendedor(id, nome, comissao);
    }

    private BigDecimal lerComissao(String texto) {
        if (texto == null) return null;
        String limpo = texto.trim().replace("%", "").replace("R$", "").replace(" ", "");
        if (limpo.isEmpty()) return null;
        if (limpo.contains(",")) {
            limpo = limpo.replace(".", "").replace(",", ".");
        }
        try {
            BigDecimal valor = new BigDecimal(limpo);
            return valor.compareTo(BigDecimal.ZERO) >= 0 ? valor : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void limparCampos() {
        txtNome.setText("");
        txtComissao.setText("");

        vendedorIdAtual = -1;
        tabela.clearSelection();
        txtNome.requestFocus();
    }
}