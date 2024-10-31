package com.example.cakestock;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

        searchField = findViewById(R.id.searchField);
        ImageButton searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> searchProdutos());

        searchField.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchProdutos();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        FloatingActionButton fabAdicionarProduto = findViewById(R.id.fab_adicionar_produto);
        fabAdicionarProduto.setOnClickListener(v -> startActivity(new Intent(EstoqueProdutos.this, CadastroProduto.class)));

        db = FirebaseFirestore.getInstance();
        listView = findViewById(R.id.lv_lista_produtos);
        produtoList = new ArrayList<>();
        allProdutos = new ArrayList<>();
        adapter = new ProdutoAdapter(this, produtoList);
        listView.setAdapter(adapter);

        listarProdutos();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Produto produto = produtoList.get(position);
            Intent returnIntent = new Intent();
            returnIntent.putExtra("produtoNome", produto.getNome());
            returnIntent.putExtra("quantidade", 1); // Aqui você pode permitir que o usuário escolha a quantidade
            setResult(RESULT_OK, returnIntent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listarProdutos();
    }

    private void listarProdutos() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference produtosRef = db.collection("Usuarios").document(userId).collection("Produtos");

        produtosRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    allProdutos.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Produto produto = document.toObject(Produto.class);
                        produto.setId(document.getId());
                        allProdutos.add(produto);
                    }
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

    private void searchProdutos() {
        String query = searchField.getText().toString().toLowerCase();
        List<Produto> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(allProdutos);
        } else {
            for (Produto produto : allProdutos) {
                if (produto.getNome().toLowerCase().contains(query)) {
                    filteredList.add(produto);
                }
            }
        }

        produtoList.clear();
        produtoList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }
}
