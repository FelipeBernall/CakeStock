package com.example.cakestock.produto;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

// Responsável por Cadastrar , Editar ou ADD Produtos
public class CadastroProduto extends AppCompatActivity {

    // Campos de entrada de dados
    private EditText editNomeProduto;
    private EditText editQuantidadeProduto;
    private EditText editValorProduto; // Novo campo para o valor do produto
    private String produtoId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_produto);

        // Inicialização dos campos de entrada
        editNomeProduto = findViewById(R.id.editNomeProduto);
        editQuantidadeProduto = findViewById(R.id.editQuantidadeProduto);
        editValorProduto = findViewById(R.id.editValorProduto); // Inicializa o campo de valor

        Button btnSalvar = findViewById(R.id.btnSalvar);
        ImageButton btnVoltar = findViewById(R.id.btn_voltar); // Inicializa o botão de voltar

        // Inicializa o Firebase
        db = FirebaseFirestore.getInstance();

        // Verifica se está editando um produto
        produtoId = getIntent().getStringExtra("produtoId");
        if (produtoId != null) {
            // Carrega os dados do produto
            carregarDadosProduto(produtoId);
        }

        // Ação do botão Salvar
        btnSalvar.setOnClickListener(v -> salvarProduto());

        // Ação do botão Voltar
        btnVoltar.setOnClickListener(v -> {
            finish(); // Volta para a tela anterior
        });
    }

    // Método para carregar os dados de um produto a partir do Firebase
    private void carregarDadosProduto(String id) {
        db.collection("Usuarios").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Produtos").document(id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Converte o documento recuperado em um objeto Produto
                            Produto produto = task.getResult().toObject(Produto.class);

                            // Preenche os campos da tela com os dados do produto
                            editNomeProduto.setText(produto.getNome());
                            editQuantidadeProduto.setText(String.valueOf(produto.getQuantidade()));
                            editValorProduto.setText(String.valueOf(produto.getValor())); // Exibe o valor do produto
                        } else {
                            Toast.makeText(CadastroProduto.this, "Erro ao carregar produto.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Método para salvar ou atualizar o produto no Firebase
    private void salvarProduto() {

        // Captura os valores digitados pelo usuário
        String nome = editNomeProduto.getText().toString().trim();
        int quantidade;
        double valor;

        // Valida e converte o valor
        try {
            quantidade = Integer.parseInt(editQuantidadeProduto.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantidade inválida.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            valor = Double.parseDouble(editValorProduto.getText().toString().trim()); // Captura o valor do produto
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtém o ID do usuário logado
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (produtoId == null) {
            // Caso seja um novo produto, cria um objeto Produto
            Produto novoProduto = new Produto(nome, quantidade, valor);

            // Adiciona o novo produto no Firebase
            db.collection("Usuarios").document(userId).collection("Produtos")
                    .add(novoProduto)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Obter o ID do documento gerado
                            String id = task.getResult().getId();
                            // Atribuir o ID ao produto
                            novoProduto.setId(id);

                            // Atualizar o documento com o ID do produto
                            db.collection("Usuarios").document(userId).collection("Produtos").document(id)
                                    .set(novoProduto) // Salva o produto com o ID
                                    .addOnCompleteListener(innerTask -> {
                                        if (innerTask.isSuccessful()) {
                                            Toast.makeText(this, "Produto cadastrado com sucesso.", Toast.LENGTH_SHORT).show();
                                            finish(); // Volta para a tela anterior
                                        } else {
                                            Toast.makeText(this, "Erro ao cadastrar produto.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Erro ao cadastrar produto.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Caso seja uma edição, atualiza os dados do produto
            db.collection("Usuarios").document(userId).collection("Produtos").document(produtoId)
                    .update("nome", nome, "quantidade", quantidade, "valor", valor) // Atualiza o valor do produto
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Produto atualizado com sucesso.", Toast.LENGTH_SHORT).show();
                            finish(); // Volta para a tela anterior
                        } else {
                            Toast.makeText(this, "Erro ao atualizar produto.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
