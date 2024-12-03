package com.example.cakestock.receita;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.example.cakestock.ingrediente.Ingrediente;
import com.example.cakestock.ingrediente.IngredienteUtil;

import java.util.ArrayList;
import java.util.List;

// Selecionar um ingrediente e quantidade (Spinner)
public class t3_SelecionarIngrediente extends AppCompatActivity {

    // Componentes da interface
    private Spinner spinnerIngredientes;
    private EditText editQuantidade;
    private Button btnAdicionarIngrediente;
    private List<Ingrediente> listaIngredientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t3_adicionar_ingrediente_receita);

        // Inicializar componentes
        spinnerIngredientes = findViewById(R.id.spinnerIngredientes);
        editQuantidade = findViewById(R.id.editQuantidade);
        btnAdicionarIngrediente = findViewById(R.id.btnAdicionarIngrediente);

        ImageButton btnVoltar = findViewById(R.id.btn_voltar);
        btnVoltar.setOnClickListener(v -> onBackPressed());

        // Carregar lista de ingredientes do estoque
        carregarIngredientes();


        btnAdicionarIngrediente.setOnClickListener(v -> {
            // Verificar se o ingrediente e a quantidade foram preenchidos
            if (spinnerIngredientes.getSelectedItem() != null && !editQuantidade.getText().toString().isEmpty()) {
                String nomeIngrediente = spinnerIngredientes.getSelectedItem().toString();
                String quantidade = editQuantidade.getText().toString();

                // Retornar os dados para a tela anterior
                Intent resultIntent = new Intent();
                resultIntent.putExtra("ingrediente", nomeIngrediente);
                resultIntent.putExtra("quantidade", quantidade);
                setResult(RESULT_OK, resultIntent);
                finish(); // Fecha a activity e retorna à anterior
            } else {
                // Exibe mensagem de erro caso os campos estejam vazios
                Toast.makeText(this, "Selecione um ingrediente e insira uma quantidade.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void carregarIngredientes() {
        // Obter a lista de ingredientes do estoque usando o utilitário
        IngredienteUtil.obterIngredientes(new IngredienteUtil.IngredientesCallback() {

            @Override
            public void onCallback(List<Ingrediente> ingredientes) {
                listaIngredientes = ingredientes;

                // Extrair os nomes dos ingredientes para exibir no Spinner
                List<String> nomesIngredientes = new ArrayList<>();
                for (Ingrediente ingrediente : ingredientes) {
                    nomesIngredientes.add(ingrediente.getNome());
                }

                // Configurar o adaptador para o Spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(t3_SelecionarIngrediente.this, android.R.layout.simple_spinner_item, nomesIngredientes);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerIngredientes.setAdapter(adapter);
            }
        });
    }
}
