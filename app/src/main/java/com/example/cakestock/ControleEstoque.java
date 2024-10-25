package com.example.cakestock;

import android.util.Log;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ControleEstoque {

    private FirebaseFirestore db;
    private String userId;

    public ControleEstoque() {
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Log.e("ControleEstoque", "Erro: Usuário não autenticado.");
        }
    }

    // Método principal para atualizar o estoque
    public void atualizarEstoque(final String idReceita, final int quantidadeProduzida) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("ControleEstoque", "Erro: Usuário não autenticado.");
            return;
        }

        String userId = currentUser.getUid();
        CollectionReference ingredientesRef = db.collection("Usuarios")
                .document(userId)
                .collection("Receitas")
                .document(idReceita)
                .collection("IngredientesUtilizados");

        ingredientesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> ingredientes = task.getResult().getDocuments();
                if (!ingredientes.isEmpty()) {
                    List<Ingrediente> ingredientesUsados = new ArrayList<>();
                    for (DocumentSnapshot document : ingredientes) {
                        String nomeIngrediente = document.getString("nomeIngrediente");
                        double quantidadeUsadaPorReceita = document.getDouble("quantidadeUsada");
                        double quantidadeTotalUsada = quantidadeUsadaPorReceita * quantidadeProduzida;

                        ingredientesUsados.add(new Ingrediente(nomeIngrediente, quantidadeTotalUsada));
                    }
                    verificarEstoque(ingredientesUsados);
                } else {
                    Log.e("ControleEstoque", "Nenhum ingrediente encontrado para a receita.");
                }
            } else {
                Log.e("ControleEstoque", "Erro ao recuperar ingredientes: ", task.getException());
            }
        });
    }

    // Verifica se há estoque suficiente antes de prosseguir
    private void verificarEstoque(List<Ingrediente> ingredientesUsados) {
        List<String> ingredientesInsuficientes = new ArrayList<>();
        List<Ingrediente> ingredientesValidos = new ArrayList<>();

        for (Ingrediente usado : ingredientesUsados) {
            CollectionReference ingredientesRef = db.collection("Usuarios")
                    .document(userId)
                    .collection("Ingredientes");

            ingredientesRef.whereEqualTo("nome", usado.getNome()).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                Ingrediente estoqueIngrediente = doc.toObject(Ingrediente.class);

                                // Aqui fazemos a conversão correta
                                double quantidadeUsadaConvertida = calcularQuantidadeConvertida(usado, estoqueIngrediente);

                                double quantidadeEstoqueConvertida;
                                // Converte o estoque para a mesma unidade
                                if (estoqueIngrediente.getTipoMedida().equalsIgnoreCase("unidade")) {
                                    quantidadeEstoqueConvertida = estoqueIngrediente.getQuantidade() * estoqueIngrediente.getUnidadeMedida(); // Conversão correta para mililitros/gramas
                                } else if (estoqueIngrediente.getTipoMedida().equalsIgnoreCase("mililitros")) {
                                    quantidadeEstoqueConvertida = estoqueIngrediente.getQuantidade() * 1000;  // Converte litros para mililitros
                                } else if (estoqueIngrediente.getTipoMedida().equalsIgnoreCase("gramas")) {
                                    quantidadeEstoqueConvertida = estoqueIngrediente.getQuantidade() * 1000;  // Converte quilos para gramas
                                } else {
                                    quantidadeEstoqueConvertida = estoqueIngrediente.getQuantidade();  // Caso padrão, sem conversão
                                }

                                Log.d("ControleEstoque", "Quantidade de estoque após conversão: " + quantidadeEstoqueConvertida);
                                Log.d("ControleEstoque", "Quantidade usada convertida: " + quantidadeUsadaConvertida);

                                // Comparação correta agora que ambos estão na mesma unidade (mililitros, gramas, ou unidades)
                                if (quantidadeEstoqueConvertida < quantidadeUsadaConvertida) {
                                    ingredientesInsuficientes.add(usado.getNome());
                                    Log.e("ControleEstoque", "Estoque insuficiente para o ingrediente: " + usado.getNome());
                                } else {
                                    ingredientesValidos.add(usado);
                                }
                            }
                        } else {
                            Log.e("ControleEstoque", "Ingrediente não encontrado no estoque: " + usado.getNome());
                        }

                        // Validar após verificar todos os ingredientes
                        if (ingredientesInsuficientes.isEmpty()) {
                            atualizarEstoque(ingredientesValidos);
                        } else {
                            Log.e("ControleEstoque", "Estoque insuficiente para alguns ingredientes. Produção não registrada.");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("ControleEstoque", "Erro ao acessar o estoque do ingrediente: " + usado.getNome(), e));
        }
    }

    // Atualiza o estoque se todas as validações forem concluídas com sucesso
    private void atualizarEstoque(List<Ingrediente> ingredientesUsados) {
        for (Ingrediente ingredienteUsar : ingredientesUsados) {
            CollectionReference ingredientesRef = db.collection("Usuarios")
                    .document(userId)
                    .collection("Ingredientes");

            ingredientesRef.whereEqualTo("nome", ingredienteUsar.getNome()).get()
                    .addOnSuccessListener(queryDocumentSnapshotsUpdate -> {
                        if (!queryDocumentSnapshotsUpdate.isEmpty()) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshotsUpdate) {
                                Ingrediente estoqueIngrediente = doc.toObject(Ingrediente.class);

                                double quantidadeUsadaConvertida = calcularQuantidadeConvertida(ingredienteUsar, estoqueIngrediente);
                                double novaQuantidade = estoqueIngrediente.getQuantidade() * estoqueIngrediente.getUnidadeMedida() - quantidadeUsadaConvertida;

                                // Verificação de valor negativo
                                if (novaQuantidade < 0) {
                                    Log.e("ControleEstoque", "Erro: Tentativa de atualizar o estoque com valor negativo para o ingrediente: " + ingredienteUsar.getNome());
                                    return;  // Impede a atualização se a quantidade for negativa
                                }

                                // Atualiza o estoque se for maior ou igual a zero
                                ingredientesRef.document(doc.getId())
                                        .update("quantidade", novaQuantidade / estoqueIngrediente.getUnidadeMedida())  // Certifique-se de atualizar na unidade correta
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("ControleEstoque", "Estoque atualizado para o ingrediente: " + ingredienteUsar.getNome());
                                        })
                                        .addOnFailureListener(e -> Log.e("ControleEstoque", "Erro ao atualizar o estoque do ingrediente: " + ingredienteUsar.getNome(), e));
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("ControleEstoque", "Erro ao acessar o estoque do ingrediente: " + ingredienteUsar.getNome(), e));
        }
    }


    // Conversão da unidade de medida
    private double calcularQuantidadeConvertida(Ingrediente usado, Ingrediente estoqueIngrediente) {
        double quantidadeUsada = usado.getQuantidade();  // Quantidade usada por receita
        double unidadeMedidaEstoque = estoqueIngrediente.getUnidadeMedida();  // Ex: 1000 ml por unidade (no caso de unidade)
        double estoqueAtual = estoqueIngrediente.getQuantidade();  // Quantidade atual no estoque (em litros, unidades, etc.)

        String tipoMedidaEstoque = estoqueIngrediente.getTipoMedida().toLowerCase(Locale.ROOT);  // Tipo de medida (unidade, gramas, ml, etc.)

        Log.d("ControleEstoque", "Ingrediente: " + estoqueIngrediente.getNome());
        Log.d("ControleEstoque", "Quantidade usada: " + quantidadeUsada);
        Log.d("ControleEstoque", "Estoque atual: " + estoqueAtual);
        Log.d("ControleEstoque", "Unidade de medida do estoque: " + unidadeMedidaEstoque);

        switch (tipoMedidaEstoque) {
            case "mililitros":
                // O estoque está em litros, precisamos converter para mililitros
                double estoqueEmMl = estoqueAtual * 1000;  // Converte litros para mililitros
                Log.d("ControleEstoque", "Estoque convertido para mililitros: " + estoqueEmMl);
                return quantidadeUsada;  // Subtrai diretamente a quantidade usada em mililitros

            case "gramas":
                // O estoque está em quilos, precisamos converter para gramas
                double estoqueEmGramas = estoqueAtual * 1000;  // Converte quilos para gramas
                Log.d("ControleEstoque", "Estoque convertido para gramas: " + estoqueEmGramas);
                return quantidadeUsada;  // Subtrai diretamente a quantidade usada em gramas

            case "unidade":
                // O estoque está em unidades, então convertemos a quantidade usada para a unidade apropriada
                // Exemplo: 12 caixas * 1000 ml por unidade = 12000 ml no estoque
                double quantidadeEmMl = estoqueAtual * unidadeMedidaEstoque;  // Converte as unidades para mililitros ou gramas se necessário
                Log.d("ControleEstoque", "Estoque convertido para mililitros/gramas: " + quantidadeEmMl);
                return quantidadeUsada * unidadeMedidaEstoque;  // Certifique-se de multiplicar corretamente pela unidade medida

            default:
                Log.e("ControleEstoque", "Tipo de medida desconhecida: " + estoqueIngrediente.getTipoMedida());
                return quantidadeUsada;  // Retorna a quantidade usada sem conversão se o tipo de medida for desconhecido
        }
    }

}