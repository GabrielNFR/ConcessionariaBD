package model;

/** Tabela: fornecedor (PK: CNPJ) */
public class Fornecedor {

    private String cnpj;
    private String nome;
    private String email;

    public Fornecedor() {}

    public Fornecedor(String cnpj, String nome, String email) {
        this.cnpj = cnpj;
        this.nome = nome;
        this.email = email;
    }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return cnpj + " - " + nome;
    }
}
