package com.example.cakestock.model;

public class Producao {
    private String nomeReceita;
    private int quantidadeProduzida;
    private String dataProducao;

    // Construtor
    public Producao(String nomeReceita, int quantidadeProduzida, String dataProducao) {
        this.nomeReceita = nomeReceita;
        this.quantidadeProduzida = quantidadeProduzida;
        this.dataProducao = dataProducao;
    }

    // Getters e setters
    public String getNomeReceita() {
        return nomeReceita;
    }

    public void setNomeReceita(String nomeReceita) {
        this.nomeReceita = nomeReceita;
    }

    public int getQuantidadeProduzida() {
        return quantidadeProduzida;
    }

    public void setQuantidadeProduzida(int quantidadeProduzida) {
        this.quantidadeProduzida = quantidadeProduzida;
    }

    public String getDataProducao() {
        return dataProducao;
    }

    public void setDataProducao(String dataProducao) {
        this.dataProducao = dataProducao;
    }
}
