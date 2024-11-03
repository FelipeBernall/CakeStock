package com.example.cakestock.controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.example.cakestock.view.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class t4_CadastrarReceita extends AppCompatActivity {

    private EditText edtTempoPreparo, edtRendimento, edtModoPreparo;
    private Button btnSalvar;
    private String receitaId;  // Variável para armazenar o receitaId
    private String nomeReceita; // Variável para armazenar o nome da receita
    private List<String> listaIngredientes; // Lista de ingredientes a ser recebida

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t4_cadastrar_receita);

        // Inicializar as views
        edtTempoPreparo = findViewById(R.id.editTempoPreparo);
        edtRendimento = findViewById(R.id.editRendimento);
        edtModoPreparo = findViewById(R.id.editModoPreparo);
        btnSalvar = findViewById(R.id.btnCadastrarReceita);

        // Obter o receitaId, nomeReceita e listaIngredientes da Intent
        Intent intent = getIntent();
        receitaId = intent.getStringExtra("receita_id");
        nomeReceita = intent.getStringExtra("nome_receita");
        listaIngredientes = intent.getStringArrayListExtra("ingredientes_lista");

        // Inicializar o Firestore e o Auth
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Verificar se o receitaId foi passado corretamente
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
        // Obter os dados atuais da receita para pré-preenchimento
        db.collection("Usuarios").document(userId).collection("Receitas").document(receitaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
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
        // Obter os dados dos campos de entrada
        String tempoPreparo = edtTempoPreparo.getText().toString().trim();
        String rendimento = edtRendimento.getText().toString().trim();
        String modoPreparo = edtModoPreparo.getText().toString().trim();

        if (tempoPreparo.isEmpty() || rendimento.isEmpty() || modoPreparo.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Preparar os dados para salvar no Firestore
        Map<String, Object> dadosReceita = new HashMap<>();
        dadosReceita.put("nomeReceita", nomeReceita);
        dadosReceita.put("tempoPreparo", tempoPreparo);
        dadosReceita.put("rendimento", rendimento);
        dadosReceita.put("modoPreparo", modoPreparo);

        // Referência à receita no Firestore
        CollectionReference receitasRef = db.collection("Usuarios").document(userId).collection("Receitas");

        receitasRef.document(receitaId).set(dadosReceita)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(t4_CadastrarReceita.this, "Receita salva com sucesso!", Toast.LENGTH_SHORT).show();
                    salvarIngredientesUtilizados();  // Salvar os ingredientes utilizados após salvar a receita

                    // Redirecionar para a MainActivity após salvar a receita
                    Intent intent = new Intent(t4_CadastrarReceita.this, MainActivity.class);
                    startActivity(intent);
                    finish();  // Encerrar a atividade atual
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(t4_CadastrarReceita.this, "Erro ao salvar a receita", Toast.LENGTH_SHORT).show();
                });
    }

    private void salvarIngredientesUtilizados() {
        // Subcoleção de IngredientesUtilizados dentro da receita
        CollectionReference ingredientesUtilizadosRef = db.collection("Usuarios").document(userId)
                .collection("Receitas").document(receitaId).collection("IngredientesUtilizados");

        // Limpar ingredientes existentes antes de adicionar os novos (para edição)
        ingredientesUtilizadosRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documento : queryDocumentSnapshots) {
                ingredientesUtilizadosRef.document(documento.getId()).delete();
            }

            // Adicionar os novos ingredientes
            for (String ingrediente : listaIngredientes) {
                // Extrair nome e quantidade do formato "nome: quantidade"
                String[] partes = ingrediente.split(": ");
                if (partes.length == 2) {
                    String nomeIngrediente = partes[0];
                    String quantidade = partes[1];

                    // Converter a quantidade para número (Long)
                    long quantidadeNumerica;
                    try {
                        quantidadeNumerica = Long.parseLong(quantidade);
                    } catch (NumberFormatException e) {
                        Toast.makeText(t4_CadastrarReceita.this, "Quantidade inválida para o ingrediente: " + nomeIngrediente, Toast.LENGTH_SHORT).show();
                        continue; // Pula para o próximo ingrediente
                    }

                    // Criar o mapa para o ingrediente utilizado
                    Map<String, Object> ingredienteUsado = new HashMap<>();
                    ingredienteUsado.put("nomeIngrediente", nomeIngrediente);
                    ingredienteUsado.put("quantidadeUsada", quantidadeNumerica); // Armazena como número

                    ingredientesUtilizadosRef.add(ingredienteUsado)
                            .addOnSuccessListener(documentReference -> {
                                // Ingrediente salvo com sucesso
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(t4_CadastrarReceita.this, "Erro ao salvar ingredientes utilizados", Toast.LENGTH_SHORT).show();
                            });
                }
            }

        }).addOnFailureListener(e -> {
            Toast.makeText(t4_CadastrarReceita.this, "Erro ao limpar ingredientes antigos", Toast.LENGTH_SHORT).show();
        });
    }
}
