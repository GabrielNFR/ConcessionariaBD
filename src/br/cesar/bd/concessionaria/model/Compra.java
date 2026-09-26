package br.cesar.bd.concessionaria.model;

import java.math.BigDecimal;
import java.sql.Date;

/** Tabela: compra (PK: id AUTO_INCREMENT) */
public class Compra {

    private int id;
    private Date data;               // DATE (aceita NULL: o banco usa CURRENT_DATE)
    private BigDecimal valor;
    private String fornecedorCnpj;

    public Compra() {}

    public Compra(int id, Date data, BigDecimal valor, String fornecedorCnpj) {
        this.id = id;
        this.data = data;
        this.valor = valor;
        this.fornecedorCnpj = fornecedorCnpj;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getFornecedorCnpj() { return fornecedorCnpj; }
    public void setFornecedorCnpj(String fornecedorCnpj) { this.fornecedorCnpj = fornecedorCnpj; }

    @Override
    public String toString() {
        return id + " - " + (data == null ? "" : data + " ") + "R$ " + valor;
    }
}
