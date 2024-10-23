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

                                double quantidadeUsadaConvertida = calcularQuantidadeConvertida(usado, estoqueIngrediente);

                                if (estoqueIngrediente.getQuantidade() < quantidadeUsadaConvertida) {
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
                                double novaQuantidade = estoqueIngrediente.getQuantidade() - quantidadeUsadaConvertida;

                                ingredientesRef.document(doc.getId())
                                        .update("quantidade", novaQuantidade)
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
        double quantidadeUsada = usado.getQuantidade();
        double unidadeMedidaEstoque = estoqueIngrediente.getUnidadeMedida();

        switch (estoqueIngrediente.getTipoMedida().toLowerCase()) {
            case "Mililitros":
                return quantidadeUsada;  // Retorno para mililitros, sem necessidade de conversão
            case "Gramas":
                return quantidadeUsada;  // Também já está na mesma unidade
            case "Unidade":
                return quantidadeUsada * unidadeMedidaEstoque;  // Multiplicação para converter por unidade
            default:
                Log.e("ControleEstoque", "Tipo de medida desconhecida: " + estoqueIngrediente.getTipoMedida());
                return quantidadeUsada;  // Retorna a quantidade usada sem conversão
        }
    }
}
