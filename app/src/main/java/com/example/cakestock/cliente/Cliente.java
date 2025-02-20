package com.example.cakestock.cliente;

public class Cliente {
    private String id;
    private String nome;
    private String telefone;
    private boolean ativo;

    // Construtor vazio necessário para o Firebase
    public Cliente() {
    }

    public Cliente(String nome, String telefone) {
        this.nome = nome;
        this.telefone = telefone;
        this.ativo = true; // Por padrão, o cliente está ativo ao ser criado
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

    public String getTelefone() {
        return telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public boolean isAtivo() {
        return ativo;
    }
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

}
