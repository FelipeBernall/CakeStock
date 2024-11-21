package com.example.cakestock.pedido;

public class Pedido {
    private String descricao;
    private String data;
    private String cliente;

<<<<<<< HEAD
    public Pedido() {
        // Construtor vazio necessário para o Firestore
    }

=======
>>>>>>> ed5652af40b377ea25bdfd91684cfd4e584e010e
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
