package model;

/** Tabela: modelo (PK: id AUTO_INCREMENT | FK: marca_id -> marca.id) */
public class Modelo {

    private int id;
    private String nome;
    private int marcaId;

    public Modelo() {}

    public Modelo(int id, String nome, int marcaId) {
        this.id = id;
        this.nome = nome;
        this.marcaId = marcaId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getMarcaId() { return marcaId; }
    public void setMarcaId(int marcaId) { this.marcaId = marcaId; }

    @Override
    public String toString() {
        return id + " - " + nome;
    }
}
