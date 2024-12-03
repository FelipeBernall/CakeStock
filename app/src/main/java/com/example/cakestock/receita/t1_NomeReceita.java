package com.example.cakestock.receita;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;

import java.util.ArrayList;
import java.util.UUID;  // Importar para gerar um ID único

// Inserir ou editar nome de receita
public class t1_NomeReceita extends AppCompatActivity {

    // Campos de entrada para o nome da receita
    private EditText editNomeReceita;
    private Button btnProsseguir;

    // Dados da receita em edição ou criação
    private String receitaId;
    private String nomeReceita;
    private ArrayList<String> ingredientesLista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t1_nome_receita);

        // Inicializa componentes da interface
        editNomeReceita = findViewById(R.id.editNomeReceita);
        btnProsseguir = findViewById(R.id.btnProsseguir);
        ImageButton btnVoltar = findViewById(R.id.btn_voltar);
        btnVoltar.setOnClickListener(v -> onBackPressed());

        // Recupera dados passados pela Intent, se for edição
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

                // Cria uma Intent para a próxima tela
                Intent nextIntent = new Intent(t1_NomeReceita.this, t2_AdicionarIngredientes.class);
                nextIntent.putExtra("nome_receita", nome);

                // Gerar um novo ID se for uma nova receita
                if (receitaId == null) {
                    receitaId = UUID.randomUUID().toString(); // Gerar ID único para receita nova
                }
                nextIntent.putExtra("id_receita", receitaId);

                // Passa a lista de ingredientes, se houver
                if (ingredientesLista != null) {
                    nextIntent.putStringArrayListExtra("ingredientes_lista", ingredientesLista);
                }
                startActivity(nextIntent);
                finish(); // Encerra a atividade atual
            } else {
                // erro se o campo estiver vazio
                editNomeReceita.setError("Nome da receita é obrigatório.");
            }
        });
    }
}
