package com.example.cakestock.pedido;

public class Pedido {
    private String pedidoId;  // Novo atributo pedidoId
    private String descricao;
    private String data;
    private String cliente;

    public Pedido() {
        // Construtor vazio necessário para o Firestore
    }

    // Construtor cheio para criar um pedido
    public Pedido(String pedidoId, String descricao, String data, String cliente) {
        this.pedidoId = pedidoId;
        this.descricao = descricao;
        this.data = data;
        this.cliente = cliente;
    }

    // Getters e Setters
    public String getPedidoId() {
        return pedidoId;
    }
    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
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
