package model;

/** Tabela: telefone_cliente (PK composta: cliente_CPF + telefone) */
public class TelefoneCliente {

    private String clienteCpf;
    private String telefone;

    public TelefoneCliente() {}

    public TelefoneCliente(String clienteCpf, String telefone) {
        this.clienteCpf = clienteCpf;
        this.telefone = telefone;
    }

    public String getClienteCpf() { return clienteCpf; }
    public void setClienteCpf(String clienteCpf) { this.clienteCpf = clienteCpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    @Override
    public String toString() {
        return clienteCpf + " - " + telefone;
    }
}
