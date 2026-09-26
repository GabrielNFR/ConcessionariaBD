package br.cesar.bd.concessionaria.model;

import java.math.BigDecimal;

/** Tabela: vendedor (PK: id AUTO_INCREMENT) */
public class Vendedor {

    private int id;
    private String nome;
    private BigDecimal comissao;

    public Vendedor() {}

    public Vendedor(int id, String nome, BigDecimal comissao) {
        this.id = id;
        this.nome = nome;
        this.comissao = comissao;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public BigDecimal getComissao() { return comissao; }
    public void setComissao(BigDecimal comissao) { this.comissao = comissao; }

    @Override
    public String toString() {
        return id + " - " + nome;
    }
}
