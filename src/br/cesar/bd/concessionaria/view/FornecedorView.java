package br.cesar.bd.concessionaria.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import br.cesar.bd.concessionaria.controller.AppController;
import br.cesar.bd.concessionaria.dao.FornecedorDAO;
import br.cesar.bd.concessionaria.model.Fornecedor;

public class FornecedorView extends View{
	private JTextField txtCnpj;
	private JTextField txtNome;
	private JTextField txtEmail;
	
	private JTable tabela;
	private DefaultTableModel modeloTabela;
	
	private FornecedorDAO fornecedorDAO = new FornecedorDAO();
	
	public FornecedorView() {
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JPanel painelTopo = new JPanel(new BorderLayout());
		
		JPanel painelForm = new JPanel(new GridLayout(3, 2, 5, 5));
        painelForm.add(new JLabel("CNPJ:"));
        txtCnpj = new JTextField();
        painelForm.add(txtCnpj);
		
        painelForm.add(new JLabel("Nome:"));
        txtNome = new JTextField();
        painelForm.add(txtNome);
        
        painelForm.add(new JLabel("E-mail:"));
        txtEmail = new JTextField();
        painelForm.add(txtEmail);   
     
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
      
        String[] colunas = {"CNPJ", "Nome", "E-mail"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabela = new JTable(modeloTabela);
        JScrollPane scrollPane = new JScrollPane(tabela);
        add(scrollPane, BorderLayout.CENTER);

        btnSalvar.addActionListener(e -> {
        	try {
				fornecedorDAO.inserir(new Fornecedor(txtCnpj.getText(), txtNome.getText(), txtEmail.getText()));

				mostrarMensagem("Fornecedor salvo com sucesso!", false);
	            limparCampos();
	            atualizarTabela();
			} catch (SQLException e1) {
				
			}        	
        });
                
        btnAtualizar.addActionListener(e -> {
            try {
                Fornecedor f = new Fornecedor(txtCnpj.getText(), txtNome.getText(), txtEmail.getText());
                fornecedorDAO.atualizar(f);
                
                mostrarMensagem("Fornecedor atualizado!", false);
                limparCampos();
	            atualizarTabela();
            } catch (SQLException exception) {
            }
        });
                
        btnExcluir.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Deseja excluir?", "Aviso", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    fornecedorDAO.excluir(txtCnpj.getText());
                    mostrarMensagem("Fornecedor excluído!", false);
                    limparCampos();
    	            atualizarTabela();
                } catch (SQLException exception) {
                }
            }
        });

        btnLimpar.addActionListener(e -> limparCampos());

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabela.getSelectedRow() != -1) {
                int linha = tabela.getSelectedRow();
                txtCnpj.setText(modeloTabela.getValueAt(linha, 0).toString());
                txtNome.setText(modeloTabela.getValueAt(linha, 1).toString());
                txtEmail.setText(modeloTabela.getValueAt(linha, 2).toString());
                txtCnpj.setEnabled(false);
            }
        }); 
	}
	
    public void aoAbrir() {
        limparCampos();
        try {
			atualizarTabela();
		} catch (RuntimeException e) {
			mostrarMensagem(e.getMessage(), true);
		}
    }
		
	public void limparCampos() {
        txtCnpj.setText("");
        txtNome.setText("");
        txtEmail.setText("");
        txtCnpj.setEnabled(true);
        tabela.clearSelection();
        txtCnpj.requestFocus();
    }

    public void atualizarTabela() throws RuntimeException{
    	List<Fornecedor> fornecedores;
		try {
			fornecedores = fornecedorDAO.listar();
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao buscar dados: " + e.getMessage());
		}
        modeloTabela.setRowCount(0);
        for (Fornecedor f : fornecedores) {
            modeloTabela.addRow(new Object[]{f.getCnpj(), f.getNome(), f.getEmail()});
        }
    }

}
