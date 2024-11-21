package com.example.cakestock.produto;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.example.cakestock.receita.Producao;
import com.example.cakestock.receita.HistoricoAdapter;
import com.example.cakestock.receita.ListaReceitas;
import com.example.cakestock.usuario.FormLogin;
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
    private ArrayList<Producao> listaHistorico;  // Usando o model 'Producao'
    private HistoricoAdapter adapterHistorico;  // Usando o adapter customizado 'HistoricoAdapter'
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

        // Inicializa a lista de histórico de produções com 'Producao'
        listaHistorico = new ArrayList<>();
        adapterHistorico = new HistoricoAdapter(this, listaHistorico);
        lvHistoricoProducoes.setAdapter(adapterHistorico);  // Usa o adapter customizado

        // Inicia o listener para carregar o histórico de produções em tempo real
        listarHistorico();

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
    private void listarHistorico() {
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
                            // Cria um objeto 'Producao' a partir do documento
                            Producao producao = new Producao(
                                    document.getString("nomeReceita"),
                                    document.getLong("quantidadeProduzida").intValue(),
                                    document.getString("dataProducao")
                            );
                            listaHistorico.add(producao); // Adiciona na lista (usando 'Producao')
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
