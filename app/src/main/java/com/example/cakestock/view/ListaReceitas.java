package com.example.cakestock.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.controller.ControleEstoque;
import com.example.cakestock.controller.activity.FormLogin;
import com.example.cakestock.controller.activity.t1_NomeReceita;
import com.example.cakestock.model.Receita;
import com.example.cakestock.R;
import com.example.cakestock.adapter.ReceitaAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ListaReceitas extends AppCompatActivity {

    private ListView lvListaReceitas;
    private FloatingActionButton fabAdicionarReceita;
    private ArrayList<Receita> listaReceitas;
    private ArrayList<String> listaReceitasIds;
    private ReceitaAdapter adapterReceitas;
    private FirebaseFirestore db;
    private String userId;
    private boolean controleEstoque = false; // Variável para controle de estoque
    private ControleEstoque controleEstoqueClass; // Instância de ControleEstoque

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_receitas);

        // Inicializa componentes da tela
        lvListaReceitas = findViewById(R.id.lv_lista_receitas);
        fabAdicionarReceita = findViewById(R.id.fab_adicionar_receita);

        // Inicializa o Firestore e autenticação
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Intent intent = new Intent(ListaReceitas.this, FormLogin.class);
            startActivity(intent);
            finish();
            return;
        }

        // Verifica se veio do controle de estoque
        if (getIntent() != null) {
            controleEstoque = getIntent().getBooleanExtra("controle_estoque", false);
        }

        listaReceitas = new ArrayList<>();
        listaReceitasIds = new ArrayList<>();

        // Configura o adapter personalizado
        adapterReceitas = new ReceitaAdapter(this, R.layout.item_receita, listaReceitas);
        lvListaReceitas.setAdapter(adapterReceitas);

        // Inicializa a classe de controle de estoque
        controleEstoqueClass = new ControleEstoque();

        // Carrega as receitas do Firestore
        carregarReceitas();

        // Configura o clique em cada item da lista
        lvListaReceitas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Receita receitaSelecionada = listaReceitas.get(position);
                String idReceitaSelecionada = listaReceitasIds.get(position);

                if (controleEstoque) {
                    // Se for controle de estoque, abrir o AlertDialog
                    abrirDialogoProducao(receitaSelecionada.getNome(), idReceitaSelecionada);
                } else {
                    // Se não for controle de estoque, abrir a tela de detalhes
                    Intent intent = new Intent(ListaReceitas.this, DetalhesReceita.class);
                    intent.putExtra("nome_receita", receitaSelecionada.getNome());
                    intent.putExtra("id_receita", idReceitaSelecionada);
                    startActivity(intent);
                }
            }
        });

        // Configura o FAB para adicionar nova receita
        fabAdicionarReceita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListaReceitas.this, t1_NomeReceita.class);
                startActivity(intent);
            }
        });
    }

    // Método para carregar as Receitas do Firestore
    private void carregarReceitas() {
        db.collection("Usuarios").document(userId).collection("Receitas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaReceitas.clear();
                        listaReceitasIds.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nomeReceita = document.getString("nomeReceita");
                            if (nomeReceita != null) {
                                // Adiciona uma nova instância de Receita com os dados do documento
                                Receita receita = new Receita(document.getId(), nomeReceita, "", "", 0);
                                listaReceitas.add(receita);
                                listaReceitasIds.add(document.getId());
                            }
                        }
                        adapterReceitas.notifyDataSetChanged();
                    } else {
                        Log.d("Firestore", "Erro ao buscar receitas: ", task.getException());
                        Toast.makeText(ListaReceitas.this, "Erro ao carregar receitas.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void abrirDialogoProducao(String receitaSelecionada, String idReceitaSelecionada) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Produção de " + receitaSelecionada);

        final EditText input = new EditText(this);
        input.setHint("Quantidade produzida");
        builder.setView(input);

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String quantidadeStr = input.getText().toString();
            if (!quantidadeStr.isEmpty()) {
                int quantidadeProduzida = Integer.parseInt(quantidadeStr);

                // Chama o método para atualizar o estoque e salvar o histórico
                controleEstoqueClass.atualizarEstoque(idReceitaSelecionada, quantidadeProduzida, new ControleEstoque.OnEstoqueUpdateListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ListaReceitas.this, "Produção registrada e estoque atualizado.", Toast.LENGTH_SHORT).show();
                        // Redireciona para a tela de histórico após confirmar a produção
                        Intent intent = new Intent(ListaReceitas.this, HistoricoProducoes.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(String mensagem) {
                        Toast.makeText(ListaReceitas.this, mensagem, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ListaReceitas.this, "Por favor, insira uma quantidade válida.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarReceitas();
    }
}
