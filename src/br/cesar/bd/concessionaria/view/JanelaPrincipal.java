package br.cesar.bd.concessionaria.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import br.cesar.bd.concessionaria.controller.AppController;

public class JanelaPrincipal extends JFrame{
	private JPanel header;
	private JPanel body;
	
	public JanelaPrincipal(AppController controller) {
		setTitle("Gestão de Concessionária"); 
	    setSize(800, 600);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setLocationRelativeTo(null);

		header = new JPanel();
		
		for(Rota rota : Rota.values()) {
			JButton btn = new JButton(rota.getText());
			btn.setActionCommand(rota.name());
			btn.addActionListener(e -> controller.navegar(rota));
			header.add(btn);
		}
		
		add(header, BorderLayout.NORTH);
		
		body = new JPanel();
		body.setLayout(new BorderLayout());
		add(body, BorderLayout.CENTER);
	}
	
	public void updateMenu(Rota rotaAtiva) {
		for(Component comp : header.getComponents()) {
			if(comp instanceof JButton) {
				JButton btn = (JButton)comp;
				if(btn.getActionCommand().equals(rotaAtiva.name())) {
					btn.setBackground(Color.YELLOW);
					btn.setEnabled(false);
				}else {
					btn.setBackground(UIManager.getColor("Button.background"));
					btn.setEnabled(true);
				}
			}
		}
	}

	public void setBody(JPanel view) {
		body.removeAll();
		body.add(view, BorderLayout.CENTER);
		body.revalidate();
		body.repaint();
	}
	
}
