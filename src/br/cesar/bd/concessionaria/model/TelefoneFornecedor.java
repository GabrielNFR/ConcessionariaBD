package br.cesar.bd.concessionaria.model;

/** Tabela: telefone_fornecedor (PK composta: fornecedor_CNPJ + telefone) */
public class TelefoneFornecedor {

    private String fornecedorCnpj;
    private String telefone;

    public TelefoneFornecedor() {}

    public TelefoneFornecedor(String fornecedorCnpj, String telefone) {
        this.fornecedorCnpj = fornecedorCnpj;
        this.telefone = telefone;
    }

    public String getFornecedorCnpj() { return fornecedorCnpj; }
    public void setFornecedorCnpj(String fornecedorCnpj) { this.fornecedorCnpj = fornecedorCnpj; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    @Override
    public String toString() {
        return fornecedorCnpj + " - " + telefone;
    }
}
