package com.example.cakestock.receita;

public class Receita {
    private String idReceita; // Alterado para IdReceita
    private String nome;
    private String modoPreparo; // Instruções de preparo
    private String rendimento; // Quantidade de porções ou volume final
    private double tempoPreparo; // Tempo de preparo em minutos

    // Construtor vazio necessário para o Firestore
    public Receita() {
    }

    // Construtor com todos os parâmetros
    public Receita(String idReceita, String nome, String modoPreparo, String rendimento, double tempoPreparo) {
        this.idReceita = idReceita; // Atualizado
        this.nome = nome;
        this.modoPreparo = modoPreparo;
        this.rendimento = rendimento;
        this.tempoPreparo = tempoPreparo;
    }

    // Getters e Setters
    public String getIdReceita() { return idReceita; } // Atualizado
    public void setIdReceita(String idReceita) { this.idReceita = idReceita; } // Atualizado

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getModoPreparo() { return modoPreparo; }
    public void setModoPreparo(String modoPreparo) { this.modoPreparo = modoPreparo; }

    public String getRendimento() { return rendimento; }
    public void setRendimento(String rendimento) { this.rendimento = rendimento; }

    public double getTempoPreparo() { return tempoPreparo; }
    public void setTempoPreparo(double tempoPreparo) { this.tempoPreparo = tempoPreparo; }
}
