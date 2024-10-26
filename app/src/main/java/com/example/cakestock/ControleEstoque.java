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

        // Usar contador para verificar quando todos os ingredientes foram processados
        final int[] ingredientesProcessados = {0};

        for (Ingrediente usado : ingredientesUsados) {
            CollectionReference ingredientesRef = db.collection("Usuarios")
                    .document(userId)
                    .collection("Ingredientes");

            ingredientesRef.whereEqualTo("nome", usado.getNome()).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                Ingrediente estoqueIngrediente = doc.toObject(Ingrediente.class);

                                double quantidadeUsadaConvertida = calcularQuantidadeConvertida(usado, estoqueIngrediente);
                                double quantidadeEstoqueConvertida;

                                if (estoqueIngrediente.getTipoMedida().equalsIgnoreCase("mililitros")) {
                                    quantidadeEstoqueConvertida = estoqueIngrediente.getQuantidade() * 1000;
                                } else if (estoqueIngrediente.getTipoMedida().equalsIgnoreCase("gramas")) {
                                    quantidadeEstoqueConvertida = estoqueIngrediente.getQuantidade() * 1000;
                                } else {
                                    quantidadeEstoqueConvertida = estoqueIngrediente.getQuantidade();
                                }

                                if (quantidadeEstoqueConvertida < quantidadeUsadaConvertida) {
                                    ingredientesInsuficientes.add(usado.getNome());
                                } else {
                                    ingredientesValidos.add(usado);
                                }
                            }
                        } else {
                            Log.e("ControleEstoque", "Ingrediente não encontrado no estoque: " + usado.getNome());
                        }

                        // Verifica se todos os ingredientes já foram processados
                        ingredientesProcessados[0]++;
                        if (ingredientesProcessados[0] == ingredientesUsados.size()) {
                            if (ingredientesInsuficientes.isEmpty()) {
                                atualizarEstoque(ingredientesValidos);
                            } else {
                                Log.e("ControleEstoque", "Estoque insuficiente para alguns ingredientes. Produção não registrada.");
                            }
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

                                double novaQuantidade;
                                if (estoqueIngrediente.getTipoMedida().equalsIgnoreCase("unidades")) {
                                    novaQuantidade = estoqueIngrediente.getQuantidade() - quantidadeUsadaConvertida;
                                } else {
                                    novaQuantidade = estoqueIngrediente.getQuantidade() * estoqueIngrediente.getUnidadeMedida() - quantidadeUsadaConvertida;
                                }

                                if (novaQuantidade < 0) {
                                    Log.e("ControleEstoque", "Erro: Tentativa de atualizar o estoque com valor negativo para o ingrediente: " + ingredienteUsar.getNome());
                                    return;
                                }

                                if (!estoqueIngrediente.getTipoMedida().equalsIgnoreCase("unidades")) {
                                    novaQuantidade /= estoqueIngrediente.getUnidadeMedida();
                                }

                                ingredientesRef.document(doc.getId())
                                        .update("quantidade", novaQuantidade)
                                        .addOnSuccessListener(aVoid -> Log.d("ControleEstoque", "Estoque atualizado para o ingrediente: " + ingredienteUsar.getNome()))
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
        double estoqueAtual = estoqueIngrediente.getQuantidade();  // Quantidade atual no estoque

        String tipoMedidaEstoque = estoqueIngrediente.getTipoMedida().toLowerCase(Locale.ROOT);  // Tipo de medida

        Log.d("ControleEstoque", "Ingrediente: " + estoqueIngrediente.getNome());
        Log.d("ControleEstoque", "Quantidade usada: " + quantidadeUsada);
        Log.d("ControleEstoque", "Estoque atual: " + estoqueAtual);
        Log.d("ControleEstoque", "Tipo de medida do estoque: " + tipoMedidaEstoque);

        switch (tipoMedidaEstoque) {
            case "mililitros":
                // Estoque em litros, converte para mililitros
                double estoqueEmMl = estoqueAtual * 1000;
                Log.d("ControleEstoque", "Estoque convertido para mililitros: " + estoqueEmMl);
                return quantidadeUsada;

            case "gramas":
                // Estoque em quilos, converte para gramas
                double estoqueEmGramas = estoqueAtual * 1000;
                Log.d("ControleEstoque", "Estoque convertido para gramas: " + estoqueEmGramas);
                return quantidadeUsada;

            case "unidades":
                // Unidades são subtraídas diretamente
                Log.d("ControleEstoque", "Estoque em unidades. Nenhuma conversão necessária.");
                return quantidadeUsada;

            default:
                Log.e("ControleEstoque", "Tipo de medida desconhecida: " + estoqueIngrediente.getTipoMedida());
                return quantidadeUsada;
        }
    }

}