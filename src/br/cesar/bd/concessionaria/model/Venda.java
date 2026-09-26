package br.cesar.bd.concessionaria.model;

import java.math.BigDecimal;
import java.sql.Date;

/** Tabela: venda (PK: id AUTO_INCREMENT | FK: vendedor_id, cliente_CPF) */
public class Venda {

    private int id;
    private Date data;
    private BigDecimal valor;
    private int vendedorId;
    private String clienteCpf;

    public Venda() {}

    public Venda(int id, Date data, BigDecimal valor, int vendedorId, String clienteCpf) {
        this.id = id;
        this.data = data;
        this.valor = valor;
        this.vendedorId = vendedorId;
        this.clienteCpf = clienteCpf;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public int getVendedorId() { return vendedorId; }
    public void setVendedorId(int vendedorId) { this.vendedorId = vendedorId; }

    public String getClienteCpf() { return clienteCpf; }
    public void setClienteCpf(String clienteCpf) { this.clienteCpf = clienteCpf; }

    @Override
    public String toString() {
        return id + " - R$ " + valor;
    }
}
