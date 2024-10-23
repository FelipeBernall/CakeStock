package com.example.cakestock;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class t2_AdicionarIngredientes extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private TextView txtNomeReceita;
    private ListView listViewIngredientes;
    private Button btnAdicionarIngrediente, btnSalvarIngredientes;
    private ArrayList<String> listaIngredientes;
    private ArrayAdapter<String> adapter;
    private String nomeReceita;
    private String receitaId; // Declarar o receitaId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t2_adicionar_ingredientes);

        // Inicializar as views
        txtNomeReceita = findViewById(R.id.txtNomeReceita);
        listViewIngredientes = findViewById(R.id.listViewIngredientes);
        btnAdicionarIngrediente = findViewById(R.id.btnAdicionarIngrediente);
        btnSalvarIngredientes = findViewById(R.id.btnSalvarIngredientes);

        // Obter o nome da receita e receitaId da Intent
        Intent intent = getIntent();
        nomeReceita = intent.getStringExtra("nome_receita");
        receitaId = intent.getStringExtra("id_receita");

        txtNomeReceita.setText(nomeReceita);

        // Inicializar a lista de ingredientes e o adaptador
        listaIngredientes = new ArrayList<>();
        if (intent.hasExtra("ingredientes_lista")) {
            ArrayList<String> ingredientesPassados = intent.getStringArrayListExtra("ingredientes_lista");
            if (ingredientesPassados != null) {
                listaIngredientes.addAll(ingredientesPassados);
            }
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaIngredientes);
        listViewIngredientes.setAdapter(adapter);

        // Botão para adicionar ingrediente
        btnAdicionarIngrediente.setOnClickListener(v -> {
            Intent addIngredienteIntent = new Intent(t2_AdicionarIngredientes.this, t3_SelecionarIngrediente.class);
            addIngredienteIntent.putExtra("nome_receita", nomeReceita);
            startActivityForResult(addIngredienteIntent, REQUEST_CODE);
        });

        // Botão para salvar os ingredientes e ir para a próxima Activity
        btnSalvarIngredientes.setOnClickListener(v -> {
            Intent salvarIntent = new Intent(t2_AdicionarIngredientes.this, t4_CadastrarReceita.class);
            salvarIntent.putExtra("nome_receita", nomeReceita);
            salvarIntent.putExtra("receita_id", receitaId); // Passar o receitaId para a próxima Activity
            salvarIntent.putStringArrayListExtra("ingredientes_lista", listaIngredientes);
            startActivity(salvarIntent);
            finish(); // Encerrar a atividade atual
        });
    }

    // Método para receber o resultado de AdicionarIngredienteReceitaActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String ingrediente = data.getStringExtra("ingrediente");
            String quantidade = data.getStringExtra("quantidade");

            if (ingrediente != null && quantidade != null) {
                String item = ingrediente + ": " + quantidade;
                listaIngredientes.add(item);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
