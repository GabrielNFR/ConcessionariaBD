package br.cesar.bd.concessionaria.view;

public enum Rota {
	INICIO("Inicio",InicioView.class),
	FORNECEDOR("Fornecedores",FornecedorView.class),
	COMPRA("Recibos de Compras",CompraView.class),
	CLIENTE("Clientes",ClienteView.class);
	
	private final String text;
	private final Class<? extends View> implementation;

	Rota(String text,Class<? extends View> implementation){
		this.text  = text;
		this.implementation = implementation;
	}
	
	public String getText() {
		return text;
	}

	public View instanciarView() throws RuntimeException{
		try {
			return implementation.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Erro ao instanciar a view " + this.name() + ". Verifique se ela possui um construtor que recebe o Controller.", e);
		}
	}
}
