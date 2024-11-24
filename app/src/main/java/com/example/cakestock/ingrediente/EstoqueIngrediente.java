package com.example.cakestock.ingrediente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;  // Alterado de Button para ImageButton
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cakestock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EstoqueIngrediente extends AppCompatActivity {

    // Declaração das variáveis para RecyclerView, Adapter, lista de ingredientes, FirebaseFirestore, FirebaseUser e botão
    private RecyclerView recyclerViewIngredientes;
    private IngredienteAdapter adapter;
    private List<Ingrediente> ingredientes;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private ImageButton fabAdicionarIngrediente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estoque_ingrediente);

        recyclerViewIngredientes = findViewById(R.id.recyclerViewIngredientes);
        fabAdicionarIngrediente = findViewById(R.id.fabAdicionarIngrediente);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        ingredientes = new ArrayList<>();
        adapter = new IngredienteAdapter(this, ingredientes, new IngredienteAdapter.OnIngredienteClickListener() {
            @Override
            public void onEditClick(Ingrediente ingrediente) {
                Intent intent = new Intent(EstoqueIngrediente.this, CadastroIngrediente.class);
                intent.putExtra("ingrediente_id", ingrediente.getId());
                startActivityForResult(intent, 1);
            }

            @Override
            public void onDeleteClick(Ingrediente ingrediente) {
                excluirIngrediente(ingrediente);
            }
        });

        recyclerViewIngredientes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewIngredientes.setAdapter(adapter);

        fabAdicionarIngrediente.setOnClickListener(v -> {
            Intent intent = new Intent(EstoqueIngrediente.this, CadastroIngrediente.class);
            startActivityForResult(intent, 1);
        });

        carregarIngredientes();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Toast.makeText(this, "Ingrediente adicionado com sucesso!", Toast.LENGTH_SHORT).show();
            carregarIngredientes(); // Atualiza a lista de ingredientes
        }
    }

    // Método para carregar os ingredientes do Firestore
    private void carregarIngredientes() {
        // Obtém a referência para a coleção "Ingredientes" do usuário atual
        CollectionReference ingredientesRef = db.collection("Usuarios").document(user.getUid()).collection("Ingredientes");

        // Realiza a consulta para obter todos os ingredientes
        ingredientesRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Limpa a lista de ingredientes antes de adicionar novos
                        ingredientes.clear();
                        // Itera sobre os documentos retornados
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Converte cada documento em um objeto Ingrediente
                            Ingrediente ingrediente = document.toObject(Ingrediente.class);
                            // Define o ID do ingrediente

                            Log.d("DebugEstoque", "Ingrediente: " + ingrediente.getNome() + ", Quantidade: " + ingrediente.getQuantidade() + ", TipoMedida: " + ingrediente.getTipoMedida());

                            ingrediente.setId(document.getId());
                            // Adiciona o ingrediente à lista
                            ingredientes.add(ingrediente);
                        }
                        // Notifica o Adapter que os dados mudaram
                        adapter.notifyDataSetChanged();
                    } else {
                        // Mostra uma mensagem de erro caso a consulta falhe
                        Toast.makeText(EstoqueIngrediente.this, "Erro ao carregar ingredientes.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método para excluir um ingrediente do Firestore
    private void excluirIngrediente(Ingrediente ingrediente) {
        // Obtém a referência para o documento do ingrediente a ser excluído
        db.collection("Usuarios").document(user.getUid()).collection("Ingredientes").document(ingrediente.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Mostra uma mensagem de sucesso após a exclusão
                    Toast.makeText(EstoqueIngrediente.this, "Ingrediente excluído com sucesso.", Toast.LENGTH_SHORT).show();
                    // Atualiza a lista de ingredientes após a exclusão
                    carregarIngredientes();
                })
                .addOnFailureListener(e -> {
                    // Mostra uma mensagem de erro caso a exclusão falhe
                    Toast.makeText(EstoqueIngrediente.this, "Erro ao excluir ingrediente.", Toast.LENGTH_SHORT).show();
                });
    }

    // Método para subtrair ingredientes do estoque
    public void subtrairIngredientes(ArrayList<Ingrediente> ingredientesUsados) {
        for (Ingrediente usado : ingredientesUsados) {
            db.collection("Usuarios").document(user.getUid()).collection("Ingredientes")
                    .whereEqualTo("nome", usado.getNome())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Ingrediente estoqueIngrediente = doc.toObject(Ingrediente.class);
                            double novaQuantidade = estoqueIngrediente.getQuantidade() - usado.getQuantidade();

                            Log.d("ControleEstoque", "Ingrediente: " + estoqueIngrediente.getNome());
                            Log.d("ControleEstoque", "Quantidade usada: " + usado.getQuantidade());
                            Log.d("ControleEstoque", "Quantidade atual: " + estoqueIngrediente.getQuantidade());
                            Log.d("ControleEstoque", "Nova quantidade: " + novaQuantidade);

                            // Verifica se a nova quantidade é negativa
                            if (novaQuantidade < 0) {
                                Toast.makeText(this, "Estoque insuficiente para o ingrediente: " + usado.getNome(), Toast.LENGTH_SHORT).show();
                                return; // Para evitar atualizar o estoque
                            }

                            // Atualiza o estoque somente se a nova quantidade for válida
                            db.collection("Usuarios").document(user.getUid()).collection("Ingredientes")
                                    .document(doc.getId())
                                    .update("quantidade", novaQuantidade)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Estoque atualizado para o ingrediente: " + usado.getNome(), Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Erro ao atualizar o estoque do ingrediente: " + usado.getNome(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao acessar o estoque do ingrediente: " + usado.getNome(), Toast.LENGTH_SHORT).show();
                    });
        }
    }


}
