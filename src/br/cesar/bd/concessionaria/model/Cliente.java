package br.cesar.bd.concessionaria.model;

/** Tabela: cliente (PK: CPF | FK: endereco_id -> endereco.id) */
public class Cliente {

    private String cpf;
    private String nome;
    private int enderecoId;

    public Cliente() {}

    public Cliente(String cpf, String nome, int enderecoId) {
        this.cpf = cpf;
        this.nome = nome;
        this.enderecoId = enderecoId;
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getEnderecoId() { return enderecoId; }
    public void setEnderecoId(int enderecoId) { this.enderecoId = enderecoId; }

    @Override
    public String toString() {
        return cpf + " - " + nome;
    }
}
