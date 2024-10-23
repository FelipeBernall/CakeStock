package com.example.cakestock;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.UUID;  // Importar para gerar um ID único

public class t1_NomeReceita extends AppCompatActivity {

    private EditText editNomeReceita;
    private Button btnProsseguir;

    private String receitaId; // Para edição
    private String nomeReceita; // Para edição
    private ArrayList<String> ingredientesLista; // Lista de ingredientes para edição

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t1_nome_receita);

        editNomeReceita = findViewById(R.id.editNomeReceita);
        btnProsseguir = findViewById(R.id.btnProsseguir);

        // Obter dados da Intent
        Intent intent = getIntent();
        receitaId = intent.getStringExtra("id_receita");
        nomeReceita = intent.getStringExtra("nome_receita");
        ingredientesLista = intent.getStringArrayListExtra("ingredientes_lista");

        // Se estiver editando, pré-preencher o campo
        if (receitaId != null && nomeReceita != null) {
            editNomeReceita.setText(nomeReceita);
        }

        btnProsseguir.setOnClickListener(v -> {
            String nome = editNomeReceita.getText().toString().trim();
            if (!nome.isEmpty()) {
                Intent nextIntent = new Intent(t1_NomeReceita.this, t2_AdicionarIngredientes.class);
                nextIntent.putExtra("nome_receita", nome);

                // Gerar um novo ID se for uma nova receita
                if (receitaId == null) {
                    receitaId = UUID.randomUUID().toString(); // Gerar ID único para receita nova
                }
                nextIntent.putExtra("id_receita", receitaId);

                if (ingredientesLista != null) {
                    nextIntent.putStringArrayListExtra("ingredientes_lista", ingredientesLista);
                }
                startActivity(nextIntent);
                finish(); // Opcional: encerra a atividade atual para evitar voltar atrás na edição
            } else {
                editNomeReceita.setError("Nome da receita é obrigatório.");
            }
        });
    }
}
