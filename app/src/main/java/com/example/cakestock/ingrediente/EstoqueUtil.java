package com.example.cakestock.ingrediente;

public class EstoqueUtil {

    // Calcula a quantidade total de ingrediente necessária
    public static long calcularQuantidadeTotal(Ingrediente ingrediente, int quantidadeProduzida) {
        return (long) (ingrediente.getQuantidade() * quantidadeProduzida);
    }

    // Valida se a quantidade necessária está disponível no estoque
    public static boolean validarQuantidadeEstoque(long estoqueAtual, long quantidadeNecessaria) {
        return estoqueAtual >= quantidadeNecessaria;
    }
}
