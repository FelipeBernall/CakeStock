package com.example.cakestock.model;

public class Produto {
    private String id;
    private String nome;
    private int quantidade;
    private double valor;

    public Produto() {
    }

    public Produto(String nome, int quantidade, double valor) {
        this.nome = nome;
        this.quantidade = quantidade;
        this.valor = valor;
    }

    // Getters e setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getQuantidade() {
        return quantidade; // Retorna como int
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade; // Define como int
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
}
