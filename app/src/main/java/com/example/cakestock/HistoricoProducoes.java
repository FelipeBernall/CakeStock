package com.example.cakestock;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HistoricoProducoes extends AppCompatActivity {

    private ListView lvHistoricoProducoes;
    private FloatingActionButton fabAdicionarProducao;
    private ArrayList<String> listaHistorico;
    private ArrayAdapter<String> adapterHistorico;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_producoes);

        // Inicializa componentes da tela
        lvHistoricoProducoes = findViewById(R.id.lv_historico_producoes);
        fabAdicionarProducao = findViewById(R.id.fab_adicionar_producao);

        // Inicializa Firestore e autenticação
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // Se o usuário não estiver logado, redireciona para a tela de login
            Intent intent = new Intent(HistoricoProducoes.this, FormLogin.class);
            startActivity(intent);
            finish();
            return;
        }

        // Inicializa a lista de histórico de produções
        listaHistorico = new ArrayList<>();
        adapterHistorico = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaHistorico);
        lvHistoricoProducoes.setAdapter(adapterHistorico);

        // Carrega o histórico de produções
        carregarHistoricoProducoes();

        // Configura o FAB para adicionar nova produção
        fabAdicionarProducao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("HistoricoProducoesActivity", "FAB Adicionar Produção clicado");
                // Abre a tela de ListaReceitas com a flag para controle de estoque
                Intent intent = new Intent(HistoricoProducoes.this, ListaReceitas.class);
                intent.putExtra("controle_estoque", true); // Passa a flag indicando que é para controle de estoque
                startActivity(intent);
            }
        });

    }

    private void carregarHistoricoProducoes() {
        db.collection("Usuarios").document(userId).collection("HistoricoProducoes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaHistorico.clear(); // Limpa a lista para evitar duplicação
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nomeReceita = document.getString("nomeReceita");
                            String dataProducao = document.getString("dataProducao");
                            if (nomeReceita != null && dataProducao != null) {
                                listaHistorico.add(nomeReceita + " - " + dataProducao);
                            }
                        }
                        adapterHistorico.notifyDataSetChanged(); // Atualiza o adapter
                    } else {
                        Log.d("HistoricoProducoesActivity", "Erro ao carregar histórico: " + task.getException());
                        Toast.makeText(HistoricoProducoes.this, "Erro ao carregar histórico de produções.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
