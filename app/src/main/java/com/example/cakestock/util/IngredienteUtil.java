package com.example.cakestock.util;

import android.util.Log;

import com.example.cakestock.model.Ingrediente;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class IngredienteUtil {

    public interface IngredientesCallback {
        void onCallback(List<Ingrediente> ingredientes);
    }

    public static void obterIngredientes(final IngredientesCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            Log.d("IngredienteUtil", "Recuperando ingredientes para o usuário: " + user.getUid());
            CollectionReference ingredientesRef = db.collection("Usuarios").document(user.getUid()).collection("Ingredientes");
            ingredientesRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<Ingrediente> listaIngredientes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Ingrediente ingrediente = document.toObject(Ingrediente.class);
                        listaIngredientes.add(ingrediente);
                        Log.d("IngredienteUtil", "Ingrediente recuperado: " + ingrediente.getNome() + ", quantidade: " + ingrediente.getQuantidade());
                    }
                    callback.onCallback(listaIngredientes);
                } else {
                    Log.d("IngredienteUtil", "Erro ao recuperar ingredientes: " + task.getException());
                    callback.onCallback(new ArrayList<>());  // Retorna lista vazia em caso de falha
                }
            });
        } else {
            Log.d("IngredienteUtil", "Usuário não logado. Retornando lista vazia.");
            callback.onCallback(new ArrayList<>());  // Retorna lista vazia se o usuário não estiver logado
        }
    }

}
