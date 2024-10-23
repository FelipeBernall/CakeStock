package com.example.cakestock;

public class UtilitariosEstoque {

    // Calcula a quantidade total de ingrediente necessária
    public static long calcularQuantidadeTotal(Ingrediente ingrediente, int quantidadeProduzida) {
        // Como getQuantidade() pode ser double, multiplicamos e convertemos o resultado para long
        return (long) (ingrediente.getQuantidade() * quantidadeProduzida);
    }

    // Valida se a quantidade necessária está disponível no estoque
    public static boolean validarQuantidadeEstoque(long estoqueAtual, long quantidadeNecessaria) {
        return estoqueAtual >= quantidadeNecessaria;
    }
}
