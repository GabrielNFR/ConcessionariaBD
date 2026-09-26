package br.cesar.bd.concessionaria.view;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import br.cesar.bd.concessionaria.controller.AppController;

public abstract class View extends JPanel{
	public abstract void aoAbrir();
	
    public void mostrarMensagem(String mensagem, boolean erro) {
        int tipo = erro ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
        JOptionPane.showMessageDialog(this, mensagem, erro ? "Erro" : "Sucesso", tipo);
    }
}
