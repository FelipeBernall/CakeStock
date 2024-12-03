package com.example.cakestock.ingrediente;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// Util para recuperar os ingredientes do Firestore
public class IngredienteUtil {

    // Interface para retorno dos ingredientes após a consulta
    public interface IngredientesCallback {
        void onCallback(List<Ingrediente> ingredientes);  // Método chamado quando os ingredientes são recuperados
    }

    // Método estático para obter todos os ingredientes do Firestore para o usuário logado
    public static void obterIngredientes(final IngredientesCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Verifica se o usuário está logado
        if (user != null) {
            Log.d("IngredienteUtil", "Recuperando ingredientes para o usuário: " + user.getUid());

            // Referência à coleção de ingredientes do usuário no Firestore
            CollectionReference ingredientesRef = db.collection("Usuarios").document(user.getUid()).collection("Ingredientes");

            // Recupera os documentos (ingredientes) da coleção
            ingredientesRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Ingrediente> listaIngredientes = new ArrayList<>();

                    // Itera sobre os documentos retornados e converte em objetos Ingrediente
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Ingrediente ingrediente = document.toObject(Ingrediente.class);
                        listaIngredientes.add(ingrediente);
                        Log.d("IngredienteUtil", "Ingrediente recuperado: " + ingrediente.getNome() + ", quantidade: " + ingrediente.getQuantidade());
                    }

                    // Chama o callback passando a lista de ingredientes
                    callback.onCallback(listaIngredientes);

                } else {
                    // Em caso de erro na recuperação dos ingredientes, retorna uma lista vazia
                    Log.d("IngredienteUtil", "Erro ao recuperar ingredientes: " + task.getException());
                    callback.onCallback(new ArrayList<>());  // Retorna lista vazia em caso de falha
                }
            });
        } else {
            // Se o usuário não estiver logado, retorna uma lista vazia
            Log.d("IngredienteUtil", "Usuário não logado. Retornando lista vazia.");
            callback.onCallback(new ArrayList<>());  // Retorna lista vazia se o usuário não estiver logado
        }
    }

}
