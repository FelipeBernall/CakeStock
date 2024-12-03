package com.example.cakestock.receita;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.example.cakestock.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Finalizar o cadastro da receita -> registrar no Firebase
public class t4_CadastrarReceita extends AppCompatActivity {

    // Componentes da interface
    private EditText edtTempoPreparo, edtRendimento, edtModoPreparo;
    private Button btnSalvar;
    private String receitaId;  // Variável para armazenar o receitaId
    private String nomeReceita; // Variável para armazenar o nome da receita
    private List<String> listaIngredientes; // Lista de ingredientes a ser recebida


    // Inicializa o Firestore
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t4_cadastrar_receita);

        // Inicializar componentes
        edtTempoPreparo = findViewById(R.id.editTempoPreparo);
        edtRendimento = findViewById(R.id.editRendimento);
        edtModoPreparo = findViewById(R.id.editModoPreparo);
        btnSalvar = findViewById(R.id.btnCadastrarReceita);
        ImageButton btnVoltar = findViewById(R.id.btn_voltar);
        btnVoltar.setOnClickListener(v -> onBackPressed());

        // Obter dados da Intent
        Intent intent = getIntent();
        receitaId = intent.getStringExtra("receita_id");
        nomeReceita = intent.getStringExtra("nome_receita");
        listaIngredientes = intent.getStringArrayListExtra("ingredientes_lista");

        // Inicializar o Firestore e o Auth
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Validar se os dados foram passados corretamente
        if (receitaId == null || nomeReceita == null) {
            Toast.makeText(this, "Erro ao carregar dados da receita", Toast.LENGTH_SHORT).show();
            finish(); // Encerrar a atividade se o receitaId ou nomeReceita não for recebido
            return;
        }

        // Preencher os campos se estiver editando
        prePreencherCampos();

        btnSalvar.setOnClickListener(v -> salvarDados());
    }

    private void prePreencherCampos() {
        // Recuperar dados da receita para edição
        db.collection("Usuarios").document(userId).collection("Receitas").document(receitaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        // Preencher os campos com os valores do Firestore
                        String tempoPreparo = documentSnapshot.getString("tempoPreparo");
                        String rendimento = documentSnapshot.getString("rendimento");
                        String modoPreparo = documentSnapshot.getString("modoPreparo");

                        edtTempoPreparo.setText(tempoPreparo);
                        edtRendimento.setText(rendimento);
                        edtModoPreparo.setText(modoPreparo);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(t4_CadastrarReceita.this, "Erro ao carregar dados da receita", Toast.LENGTH_SHORT).show();
                });
    }

    private void salvarDados() {

        // Obter dados preenchidos pelo usuário
        String tempoPreparo = edtTempoPreparo.getText().toString().trim();
        String rendimento = edtRendimento.getText().toString().trim();
        String modoPreparo = edtModoPreparo.getText().toString().trim();

        // Validar campos obrigatórios
        if (tempoPreparo.isEmpty() || rendimento.isEmpty() || modoPreparo.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Preparar dados para salvar no Firestore
        Map<String, Object> dadosReceita = new HashMap<>();
        dadosReceita.put("nomeReceita", nomeReceita);
        dadosReceita.put("tempoPreparo", tempoPreparo);
        dadosReceita.put("rendimento", rendimento);
        dadosReceita.put("modoPreparo", modoPreparo);
        dadosReceita.put("emUso", false); // Define como não em uso inicialmente

        // Salvar receita no Firestore
        db.collection("Usuarios").document(userId).collection("Receitas").document(receitaId)
                .set(dadosReceita)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(t4_CadastrarReceita.this, "Receita salva com sucesso!", Toast.LENGTH_SHORT).show();
                    salvarIngredientesUtilizados();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(t4_CadastrarReceita.this, "Erro ao salvar a receita", Toast.LENGTH_SHORT).show();
                });
    }


    private void salvarIngredientesUtilizados() {
        // Referência para os ingredientes da receita no Firestore
        CollectionReference ingredientesUtilizadosRef = db.collection("Usuarios").document(userId)
                .collection("Receitas").document(receitaId).collection("IngredientesUtilizados");

        // Apagar ingredientes antigos para evitar duplicidades
        ingredientesUtilizadosRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documento : queryDocumentSnapshots) {
                ingredientesUtilizadosRef.document(documento.getId()).delete();
            }

            // Adicionar os ingredientes da lista atual
            for (String ingrediente : listaIngredientes) {
                String[] partes = ingrediente.split(": ");
                if (partes.length == 2) {
                    String nomeIngrediente = partes[0];
                    String quantidade = partes[1];
                    long quantidadeNumerica;

                    try {
                        quantidadeNumerica = Long.parseLong(quantidade);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Quantidade inválida para o ingrediente: " + nomeIngrediente, Toast.LENGTH_SHORT).show();
                        continue;
                    }

                    Map<String, Object> ingredienteUsado = new HashMap<>();
                    ingredienteUsado.put("nomeIngrediente", nomeIngrediente);
                    ingredienteUsado.put("quantidadeUsada", quantidadeNumerica);

                    ingredientesUtilizadosRef.add(ingredienteUsado)
                            .addOnSuccessListener(documentReference -> {
                                // Atualiza o campo emUso do ingrediente correspondente
                                db.collection("Usuarios").document(userId).collection("Ingredientes")
                                        .whereEqualTo("nome", nomeIngrediente)
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            for (QueryDocumentSnapshot doc : querySnapshot) {
                                                doc.getReference().update("emUso", true);
                                            }
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erro ao salvar ingredientes utilizados", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Erro ao limpar ingredientes antigos", Toast.LENGTH_SHORT).show();
        });
    }

}
