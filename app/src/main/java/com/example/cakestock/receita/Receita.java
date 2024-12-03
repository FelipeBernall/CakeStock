package com.example.cakestock.receita;

public class Receita {
    private String idReceita;
    private String nome;
    private String modoPreparo;
    private String rendimento;
    private double tempoPreparo;
    private boolean emUso;

    // Construtor vazio necessário para o Firestore
    public Receita() {
        this.emUso = false; // Valor padrão
    }

    // Construtor com todos os parâmetros
    public Receita(String idReceita, String nome, String modoPreparo, String rendimento, double tempoPreparo, boolean emUso) {
        this.idReceita = idReceita;
        this.nome = nome;
        this.modoPreparo = modoPreparo;
        this.rendimento = rendimento;
        this.tempoPreparo = tempoPreparo;
        this.emUso = emUso;
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

    public boolean isEmUso() { return emUso; }
    public void setEmUso(boolean emUso) { this.emUso = emUso; }

}
