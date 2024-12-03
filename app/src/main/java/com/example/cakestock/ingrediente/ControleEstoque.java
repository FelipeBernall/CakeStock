package com.example.cakestock.ingrediente;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Gerencia o controle de estoque de ingredientes
public class ControleEstoque {

    private FirebaseFirestore db; // Conexão com o Firestore
    private String userId;      // ID do usuário autenticado


    // Construtor que inicializa a conexão com o Firestore e verifica o usuário logado
    public ControleEstoque() {
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid(); // Obtém o ID do usuário logado
        } else {
            Log.e("ControleEstoque", "Erro: Usuário não autenticado.");
        }
    }

    // Interface usada para retornar o sucesso ou falha após atualizar o estoque
    public interface OnEstoqueUpdateListener {
        void onSuccess();
        void onFailure(String mensagem);
    }


    // Método principal para atualizar o estoque
    public void atualizarEstoque(final String idReceita, final int quantidadeProduzida, final OnEstoqueUpdateListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("ControleEstoque", "Erro: Usuário não autenticado.");
            listener.onFailure("Erro: Usuário não autenticado.");
            return;
        }

        // Referência aos ingredientes usados na receita
        CollectionReference ingredientesRef = db.collection("Usuarios")
                .document(userId)
                .collection("Receitas")
                .document(idReceita)
                .collection("IngredientesUtilizados");

        // Obtém os ingredientes utilizados na receita
        ingredientesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> ingredientes = task.getResult().getDocuments();
                if (!ingredientes.isEmpty()) {
                    List<Ingrediente> ingredientesUsados = new ArrayList<>();

                    // Calcula a quantidade de cada ingrediente necessário para a produção
                    for (DocumentSnapshot document : ingredientes) {
                        String nomeIngrediente = document.getString("nomeIngrediente");
                        double quantidadeUsadaPorReceita = document.getDouble("quantidadeUsada");
                        double quantidadeTotalUsada = quantidadeUsadaPorReceita * quantidadeProduzida;

                        ingredientesUsados.add(new Ingrediente(nomeIngrediente, quantidadeTotalUsada));
                    }
                    // Verifica o estoque para garantir que há ingredientes suficientes
                    verificarEstoque(ingredientesUsados, idReceita, quantidadeProduzida, listener);
                } else {
                    Log.e("ControleEstoque", "Nenhum ingrediente encontrado para a receita.");
                    listener.onFailure("Nenhum ingrediente encontrado para a receita.");
                }
            } else {
                Log.e("ControleEstoque", "Erro ao recuperar ingredientes: ", task.getException());
                listener.onFailure("Erro ao recuperar ingredientes.");
            }
        });
    }

    // Método que verifica se o estoque tem ingredientes suficientes
    private void verificarEstoque(List<Ingrediente> ingredientesUsados, String idReceita, int quantidadeProduzida, OnEstoqueUpdateListener listener) {
        List<String> ingredientesInsuficientes = new ArrayList<>();
        List<Ingrediente> ingredientesValidos = new ArrayList<>();

        // Contador para quando todos os ingredientes forem processados
        final int[] ingredientesProcessados = {0};

        // Verifica o estoque de cada ingrediente utilizado
        for (Ingrediente usado : ingredientesUsados) {
            CollectionReference ingredientesRef = db.collection("Usuarios")
                    .document(userId)
                    .collection("Ingredientes");

            ingredientesRef.whereEqualTo("nome", usado.getNome()).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                Ingrediente estoqueIngrediente = doc.toObject(Ingrediente.class);

                                // Converte as quantidades de estoque para um formato comum (mililitros ou gramas)
                                double quantidadeUsadaConvertida = calcularQuantidadeConvertida(usado, estoqueIngrediente);
                                double quantidadeEstoqueConvertida;

                                // Converte o estoque de acordo com o tipo de medida (mililitros, gramas, etc.)
                                if (estoqueIngrediente.getTipoMedida().equalsIgnoreCase("mililitros")) {
                                    quantidadeEstoqueConvertida = estoqueIngrediente.getQuantidade() * 1000;
                                } else if (estoqueIngrediente.getTipoMedida().equalsIgnoreCase("gramas")) {
                                    quantidadeEstoqueConvertida = estoqueIngrediente.getQuantidade() * 1000;
                                } else {
                                    quantidadeEstoqueConvertida = estoqueIngrediente.getQuantidade();
                                }

                                // Verifica se há estoque suficiente para o ingrediente
                                if (quantidadeEstoqueConvertida < quantidadeUsadaConvertida) {
                                    ingredientesInsuficientes.add(usado.getNome());
                                } else {
                                    ingredientesValidos.add(usado);
                                }
                            }
                        } else {
                            Log.e("ControleEstoque", "Ingrediente não encontrado no estoque: " + usado.getNome());
                        }

                        ingredientesProcessados[0]++;
                        if (ingredientesProcessados[0] == ingredientesUsados.size()) {

                            // Se todos os ingredientes foram processados, verifica se há estoque suficiente
                            if (ingredientesInsuficientes.isEmpty()) {
                                atualizarEstoque(ingredientesValidos); // Atualiza o estoque se tudo estiver certo
                                registrarProducaoNoHistorico(idReceita, quantidadeProduzida); // Registrar no histórico
                                listener.onSuccess(); // tudo certo = sucesso (retorno)
                            } else {
                                Log.e("ControleEstoque", "Estoque insuficiente para alguns ingredientes. Produção não registrada.");
                                listener.onFailure("Estoque insuficiente para: " + TextUtils.join(", ", ingredientesInsuficientes));
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ControleEstoque", "Erro ao acessar o estoque do ingrediente: " + usado.getNome(), e);
                        listener.onFailure("Erro ao acessar o estoque do ingrediente: " + usado.getNome());
                    });
        }
    }


    // Atualiza o estoque de ingredientes, subtraindo a quantidade usada
    private void atualizarEstoque(List<Ingrediente> ingredientesUsados) {
        for (Ingrediente ingredienteUsar : ingredientesUsados) {
            CollectionReference ingredientesRef = db.collection("Usuarios")
                    .document(userId)
                    .collection("Ingredientes");

            // Busca o ingrediente no estoque
            ingredientesRef.whereEqualTo("nome", ingredienteUsar.getNome()).get()
                    .addOnSuccessListener(queryDocumentSnapshotsUpdate -> {
                        if (!queryDocumentSnapshotsUpdate.isEmpty()) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshotsUpdate) {
                                Ingrediente estoqueIngrediente = doc.toObject(Ingrediente.class);

                                // Converte a quantidade usada para a unidade de medida correta
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

                                // Ajusta a quantidade com base na unidade de medida
                                if (!estoqueIngrediente.getTipoMedida().equalsIgnoreCase("unidades")) {
                                    novaQuantidade /= estoqueIngrediente.getUnidadeMedida();
                                }

                                // Atualiza o estoque no Firestore
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

    // Registra a produção da receita no histórico
    private void registrarProducaoNoHistorico(String idReceita, int quantidadeProduzida) {
        CollectionReference historicoRef = db.collection("Usuarios")
                .document(userId)
                .collection("HistoricoProducoes");

        // Busca o nome da receita para o histórico
        db.collection("Usuarios")
                .document(userId)
                .collection("Receitas")
                .document(idReceita)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nomeReceita = documentSnapshot.getString("nomeReceita"); // Obtém o nome da receita

                        // Dados para salvar no histórico
                        Map<String, Object> historicoData = new HashMap<>();
                        historicoData.put("nomeReceita", nomeReceita); // Usa o nome da receita
                        historicoData.put("quantidadeProduzida", quantidadeProduzida);
                        historicoData.put("dataProducao", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

                        // Adiciona o registro ao histórico
                        historicoRef.add(historicoData)
                                .addOnSuccessListener(documentReference -> Log.d("ControleEstoque", "Histórico de produção registrado com sucesso!"))
                                .addOnFailureListener(e -> Log.e("ControleEstoque", "Erro ao registrar histórico de produção", e));
                    } else {
                        Log.e("ControleEstoque", "Receita não encontrada para o ID: " + idReceita);
                    }
                })
                .addOnFailureListener(e -> Log.e("ControleEstoque", "Erro ao buscar nome da receita para o histórico", e));
    }


    // Converte a quantidade de ingrediente para a unidade de medida correta (mililitros, gramas, unidades)
    private double calcularQuantidadeConvertida(Ingrediente usado, Ingrediente estoqueIngrediente) {
        double quantidadeUsada = usado.getQuantidade();  // Quantidade usada por receita
        double estoqueAtual = estoqueIngrediente.getQuantidade();  // Quantidade atual no estoque

        String tipoMedidaEstoque = estoqueIngrediente.getTipoMedida().toLowerCase(Locale.ROOT);  // Tipo de medida do estoque

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
