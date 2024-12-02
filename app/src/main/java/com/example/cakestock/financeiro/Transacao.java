package com.example.cakestock.financeiro;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Transacao implements Serializable {
    private String descricao;
    private String data;
    private String clienteId;
    private List<String> produtos;
    private double valorTotal;
    private String tipo;


    public Transacao(String descricao, String data, String clienteId, List<String> produtos, double valorTotal) {
        this.descricao = descricao != null ? descricao : "";
        this.data = data != null ? data : "";
        this.clienteId = clienteId;
        this.produtos = produtos != null ? produtos : new ArrayList<>();
        this.valorTotal = valorTotal;
    }


    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

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
