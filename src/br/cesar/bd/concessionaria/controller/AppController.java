package br.cesar.bd.concessionaria.controller;

import java.util.EnumMap;
import java.util.Map;

import br.cesar.bd.concessionaria.view.JanelaPrincipal;
import br.cesar.bd.concessionaria.view.Rota;
import br.cesar.bd.concessionaria.view.View;


public class AppController {
	private JanelaPrincipal janela;
	private Map<Rota, View> rotas;
	
	public void iniciar() {
		janela = new JanelaPrincipal(this);
		rotas = new EnumMap<>(Rota.class);
		
		for(Rota rota : Rota.values()) {
			rotas.put(rota, rota.instanciarView());
		}
		
		navegar(Rota.INICIO);
		janela.setVisible(true);
	}
	
	public void navegar(Rota destino){		
		View view = rotas.get(destino);
		
		view.aoAbrir();
		janela.updateMenu(destino);
		janela.setBody(view);
	}
	
}
