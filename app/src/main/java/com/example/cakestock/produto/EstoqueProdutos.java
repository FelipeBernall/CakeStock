package com.example.cakestock.produto;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Exibe e gerencia o estoque de produtos -> Visualizar , ADD , Editar , Excluir
public class EstoqueProdutos extends AppCompatActivity {
    private FirebaseFirestore db;
    private ListView listView;
    private ProdutoAdapter adapter;
    private List<Produto> produtoList;
    private List<Produto> allProdutos;
    private EditText searchField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estoque_produtos);

        // Inicializa os componentes de busca
        searchField = findViewById(R.id.searchField);

        // Configura a busca por produtos ao clicar no botão ou digitar no campo
        searchField.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchProdutos(); // Atualiza a lista enquanto o usuário digita (TextChanged)
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // FAB para adicionar um novo produto
        FloatingActionButton fabAdicionarProduto = findViewById(R.id.fab_adicionar_produto);
        fabAdicionarProduto.setOnClickListener(v ->
                startActivity(new Intent(EstoqueProdutos.this, CadastroProduto.class)));


        // Inicializa o Firebase e as listas de produtos
        db = FirebaseFirestore.getInstance();
        listView = findViewById(R.id.lv_lista_produtos);
        produtoList = new ArrayList<>();
        allProdutos = new ArrayList<>();
        adapter = new ProdutoAdapter(this, produtoList);
        listView.setAdapter(adapter);

        // Carrega os produtos ao iniciar a tela
        listarProdutos();

        // Listener para ADD produtos ( + )
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Produto produto = produtoList.get(position);
            showQuantityDialog(produto);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listarProdutos(); // Atualiza a lista quando a tela volta a ser exibida
    }


    // Método para listar todos os produtos do Firebase
    private void listarProdutos() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference produtosRef = db.collection("Usuarios").document(userId).collection("Produtos");

        produtosRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    allProdutos.clear(); // Limpa a lista para evitar duplicações
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Produto produto = document.toObject(Produto.class);
                        produto.setId(document.getId());  // Atribui o ID do documento ao produto
                        allProdutos.add(produto); // Adiciona à lista completa
                    }

                    // Ordena a lista por nome
                    Collections.sort(allProdutos, new Comparator<Produto>() {
                        @Override
                        public int compare(Produto p1, Produto p2) {
                            return p1.getNome().compareToIgnoreCase(p2.getNome());
                        }
                    });
                    searchProdutos();
                } else {
                    Toast.makeText(EstoqueProdutos.this, "Erro ao carregar produtos.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Método para filtrar os produtos com base na busca
    private void searchProdutos() {
        String query = searchField.getText().toString().toLowerCase();
        List<Produto> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            // Se a busca estiver vazia, mostra todos os produtos
            filteredList.addAll(allProdutos);
        } else {
            // Filtra os produtos cujo nome contém o texto da busca
            for (Produto produto : allProdutos) {
                if (produto.getNome().toLowerCase().contains(query)) {
                    filteredList.add(produto);
                }
            }
        }

        // Atualiza a lista visível
        produtoList.clear();
        produtoList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }

    // Mostra um diálogo para selecionar uma quantidade
    private void showQuantityDialog(Produto produto) {
        // Cria um AlertDialog para inserir a quantidade
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione a quantidade");

        // Campo de entrada para o usuário digitar a quantidade
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); // Aceita apenas números inteiros
        builder.setView(input);

        // Define as ações do AlertDialog
        builder.setPositiveButton("OK", (dialog, which) -> {
            String quantidadeStr = input.getText().toString();
            int quantidade;
            try {
                quantidade = Integer.parseInt(quantidadeStr); // Valida a entrada
            } catch (NumberFormatException e) {
                Toast.makeText(EstoqueProdutos.this, "Quantidade inválida.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Retorna os dados para a tela anterior
            Intent returnIntent = new Intent();
            returnIntent.putExtra("produtoNome", produto.getNome());
            returnIntent.putExtra("quantidade", quantidade);
            returnIntent.putExtra("precoProduto", produto.getValor()); // Passa o valor do produto
            setResult(RESULT_OK, returnIntent);
            finish();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
