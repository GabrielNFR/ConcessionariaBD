package br.cesar.bd.concessionaria.model;

import java.math.BigDecimal;

/**
 * Tabela: carro (PK: chassi)
 * FKs: cor_id, modelo_id, compra_id (obrigatorias) e venda_id (opcional).
 * venda_id == null significa que o carro ainda esta no estoque.
 */
public class Carro {

    private String chassi;
    private int ano;
    private BigDecimal preco;
    private int corId;
    private int modeloId;
    private int compraId;
    private Integer vendaId;     // Integer (e nao int) porque a coluna aceita NULL

    public Carro() {}

    public Carro(String chassi, int ano, BigDecimal preco,
                 int corId, int modeloId, int compraId, Integer vendaId) {
        this.chassi = chassi;
        this.ano = ano;
        this.preco = preco;
        this.corId = corId;
        this.modeloId = modeloId;
        this.compraId = compraId;
        this.vendaId = vendaId;
    }

    public String getChassi() { return chassi; }
    public void setChassi(String chassi) { this.chassi = chassi; }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public int getCorId() { return corId; }
    public void setCorId(int corId) { this.corId = corId; }

    public int getModeloId() { return modeloId; }
    public void setModeloId(int modeloId) { this.modeloId = modeloId; }

    public int getCompraId() { return compraId; }
    public void setCompraId(int compraId) { this.compraId = compraId; }

    public Integer getVendaId() { return vendaId; }
    public void setVendaId(Integer vendaId) { this.vendaId = vendaId; }

    /** true se o carro ainda nao foi vendido (esta no estoque). */
    public boolean isNoEstoque() { return vendaId == null; }

    @Override
    public String toString() {
        return chassi + " - ano " + ano + " - R$ " + preco;
    }
}
