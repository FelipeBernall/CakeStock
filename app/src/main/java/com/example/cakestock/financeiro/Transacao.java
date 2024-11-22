package com.example.cakestock.financeiro;

import java.util.List;

public class Transacao {
    private String descricao;
    private String data;
    private String clienteId;
    private List<String> produtos;
    private double valorTotal;

    public Transacao(String descricao, String data, String clienteId, List<String> produtos, double valorTotal) {
        this.descricao = descricao;
        this.data = data;
        this.clienteId = clienteId;
        this.produtos = produtos;
        this.valorTotal = valorTotal;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public List<String> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<String> produtos) {
        this.produtos = produtos;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }
}
