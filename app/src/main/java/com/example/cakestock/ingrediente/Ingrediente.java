package com.example.cakestock.ingrediente;

public class Ingrediente {
    private String id;
    private String nome;
    private double quantidade;
    private String tipoMedida;
    private double unidadeMedida;
    private double valorUnitario;
    private double valorTotal;
    private boolean emUso;

    // Construtor vazio necessário para o Firestore
    public Ingrediente() {
        this.emUso = false; // Valor padrão
    }

    // Construtor com todos os parâmetros (construtor cheio)
    public Ingrediente(String id, String nome, double quantidade, String tipoMedida, double unidadeMedida, double valorUnitario, double valorTotal, boolean emUso) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.tipoMedida = tipoMedida;
        this.unidadeMedida = unidadeMedida;
        this.valorUnitario = valorUnitario;
        this.valorTotal = valorTotal;
        this.emUso = emUso;
    }

    // Construtor com nome e quantidade
    public Ingrediente(String nome, double quantidade) {
        this.nome = nome;
        this.quantidade = quantidade;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getQuantidade() { return quantidade; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }

    public String getTipoMedida() { return tipoMedida; }
    public void setTipoMedida(String tipoMedida) { this.tipoMedida = tipoMedida; }

    public double getUnidadeMedida() { return unidadeMedida; }
    public void setUnidadeMedida(double unidadeMedida) { this.unidadeMedida = unidadeMedida; }

    public double getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(double valorUnitario) { this.valorUnitario = valorUnitario; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public boolean isEmUso() { return emUso; }
    public void setEmUso(boolean emUso) { this.emUso = emUso; }

}
