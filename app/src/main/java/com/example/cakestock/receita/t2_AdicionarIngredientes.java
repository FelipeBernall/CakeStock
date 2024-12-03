package com.example.cakestock.receita;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;

import java.util.ArrayList;

// Adicionar e visualizar ingredientes usados na receita
public class t2_AdicionarIngredientes extends AppCompatActivity {

    private static final int REQUEST_CODE = 1; // identificar a Activity de seleção de ingredientes
    private TextView txtNomeReceita;
    private ListView listViewIngredientes;
    private Button btnAdicionarIngrediente, btnSalvarIngredientes;
    private ArrayList<String> listaIngredientes;
    private ArrayAdapter<String> adapter;
    private String nomeReceita;
    private String receitaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t2_adicionar_ingredientes);

        // Inicializa componentes da interface
        txtNomeReceita = findViewById(R.id.txtNomeReceita);
        listViewIngredientes = findViewById(R.id.listViewIngredientes);
        btnAdicionarIngrediente = findViewById(R.id.btnAdicionarIngrediente);
        btnSalvarIngredientes = findViewById(R.id.btnSalvarIngredientes);
        ImageButton btnVoltar = findViewById(R.id.btn_voltar);
        btnVoltar.setOnClickListener(v -> onBackPressed());

        // Recupera dados da Intent
        Intent intent = getIntent();
        nomeReceita = intent.getStringExtra("nome_receita");
        receitaId = intent.getStringExtra("id_receita");

        txtNomeReceita.setText(nomeReceita);

        // Inicializa a lista e o adaptador de ingredientes
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
            if (listaIngredientes.isEmpty()) {
                // Exibir uma mensagem de erro se a lista estiver vazia
                Toast.makeText(t2_AdicionarIngredientes.this, "Adicione pelo menos um ingrediente antes de salvar!", Toast.LENGTH_SHORT).show();
            } else {
                // Prosseguir apenas se houver itens na lista
                Intent salvarIntent = new Intent(t2_AdicionarIngredientes.this, t4_CadastrarReceita.class);
                salvarIntent.putExtra("nome_receita", nomeReceita);
                salvarIntent.putExtra("receita_id", receitaId); // Passar o receitaId para a próxima Activity
                salvarIntent.putStringArrayListExtra("ingredientes_lista", listaIngredientes);
                startActivity(salvarIntent);
                finish(); // Encerrar a atividade atual
            }
        });

    }

    // Método para receber o resultado do ingrediente adicionado (t3)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String ingrediente = data.getStringExtra("ingrediente");
            String quantidade = data.getStringExtra("quantidade");

            if (ingrediente != null && quantidade != null) {
                // Verificar se o ingrediente já existe na lista (independente da quantidade)
                if (isIngredienteDuplicado(ingrediente)) {
                    Toast.makeText(this, "Este ingrediente já foi adicionado à lista.", Toast.LENGTH_SHORT).show();
                } else {
                    // Adicionar o ingrediente à lista se não for duplicado
                    String item = ingrediente + ": " + quantidade;
                    listaIngredientes.add(item);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    // Verifica se o ingrediente já está na lista
    private boolean isIngredienteDuplicado(String ingrediente) {
        for (String item : listaIngredientes) {
            // Separar o nome do ingrediente do formato "ingrediente: quantidade"
            String nomeExistente = item.split(":")[0].trim();
            if (nomeExistente.equalsIgnoreCase(ingrediente.trim())) {
                return true; // Ingrediente já existe na lista
            }
        }
        return false; // Ingrediente não encontrado
    }


}
