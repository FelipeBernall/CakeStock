package com.example.cakestock.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class HistoricoProducoes extends AppCompatActivity {

    private ListView lvHistoricoProducoes;
    private FloatingActionButton fabAdicionarProducao;
    private ArrayList<String> listaHistorico;
    private ArrayAdapter<String> adapterHistorico;
    private FirebaseFirestore db;
    private String userId;
    private ListenerRegistration listenerRegistration; // Listener para atualizações em tempo real


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

        // Inicia o listener para carregar o histórico de produções em tempo real
        iniciarListenerHistorico();

        // Configura o FAB para adicionar nova produção
        fabAdicionarProducao.setOnClickListener(v -> {
            Log.d("HistoricoProducoesActivity", "FAB Adicionar Produção clicado");
            // Abre a tela de ListaReceitas com a flag para controle de estoque
            Intent intent = new Intent(HistoricoProducoes.this, ListaReceitas.class);
            intent.putExtra("controle_estoque", true); // Passa a flag indicando que é para controle de estoque
            startActivity(intent);
        });
    }

    // Método para iniciar o listener do Firestore em tempo real
    private void iniciarListenerHistorico() {
        listenerRegistration = db.collection("Usuarios").document(userId)
                .collection("HistoricoProducoes")
                .orderBy("dataProducao", com.google.firebase.firestore.Query.Direction.DESCENDING) // Ordena por data
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("HistoricoProducoes", "Erro ao escutar alterações:", e);
                        return;
                    }

                    listaHistorico.clear(); // Limpa a lista antes de atualizar
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot document : snapshots) {
                            // Recupera os campos do documento
                            String nomeReceita = document.getString("nomeReceita");
                            Long quantidadeProduzida = document.getLong("quantidadeProduzida");
                            String dataProducao = document.getString("dataProducao");

                            if (nomeReceita != null && quantidadeProduzida != null && dataProducao != null) {
                                // Cria o item de exibição
                                String item = nomeReceita + " - " + quantidadeProduzida + " unidades - " + dataProducao;
                                listaHistorico.add(item); // Adiciona na lista (agora em ordem correta)
                            }
                        }
                        adapterHistorico.notifyDataSetChanged(); // Atualiza o adapter
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove o listener para evitar vazamento de memória
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
