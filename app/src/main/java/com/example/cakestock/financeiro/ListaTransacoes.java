package com.example.cakestock.financeiro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListaTransacoes extends AppCompatActivity {

    private ListView lvTransacoes;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_transacoes);

        // Inicializa os componentes de UI
        lvTransacoes = findViewById(R.id.lv_transacoes);
        ImageButton btnVendas = findViewById(R.id.btn_vendas);
        ImageButton btnDespesas = findViewById(R.id.btn_despesas);

        // Redireciona para a tela de registro de venda
        btnVendas.setOnClickListener(v -> {
            Intent intent = new Intent(ListaTransacoes.this, RegistroVenda.class);
            startActivity(intent);
        });

        // Redireciona para a tela de registro de despesa
        btnDespesas.setOnClickListener(v -> {
            Intent intent = new Intent(ListaTransacoes.this, RegistroDespesa.class);
            startActivity(intent);
        });

        // Inicializa o Firestore
        db = FirebaseFirestore.getInstance();

        // Carrega as transações (vendas)
        carregarVendas();
    }

    private void carregarVendas() {
        db.collection("Usuarios")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Vendas")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(ListaTransacoes.this, "Erro ao carregar vendas.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> vendasList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String descricao = document.getString("descricao");
                        Double valorTotal = document.getDouble("valorTotal");
                        vendasList.add(descricao + " : " + String.format("R$ %.2f", valorTotal));
                    }

                    // Atualiza o ListView com as vendas
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ListaTransacoes.this, android.R.layout.simple_list_item_1, vendasList);
                    lvTransacoes.setAdapter(adapter);
                });
    }
}
