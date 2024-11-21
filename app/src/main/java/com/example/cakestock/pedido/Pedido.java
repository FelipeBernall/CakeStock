package com.example.cakestock.pedido;

public class Pedido {
    private String descricao;
    private String data;
    private String cliente;

    public Pedido(String descricao, String data, String cliente) {
        this.descricao = descricao;
        this.data = data;
        this.cliente = cliente;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getData() {
        return data;
    }

    public String getCliente() {
        return cliente;
    }
}
