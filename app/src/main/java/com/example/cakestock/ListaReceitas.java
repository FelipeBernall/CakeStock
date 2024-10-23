package com.example.cakestock;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ListaReceitas extends AppCompatActivity {

    private ListView lvListaReceitas;
    private FloatingActionButton fabAdicionarReceita;
    private ArrayList<String> listaReceitas;
    private ArrayList<String> listaReceitasIds;
    private ArrayAdapter<String> adapterReceitas;
    private FirebaseFirestore db;
    private String userId;
    private boolean controleEstoque = false; // Variável para controlar se é controle de estoque
    private ControleEstoque controleEstoqueClass; // Instância da classe ControleEstoque

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
        adapterReceitas = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaReceitas);
        lvListaReceitas.setAdapter(adapterReceitas);

        // Inicializa a classe de controle de estoque
        controleEstoqueClass = new ControleEstoque();

        // Carrega as receitas do Firestore
        carregarReceitas();

        // Configura o clique em cada item da lista
        lvListaReceitas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String receitaSelecionada = listaReceitas.get(position);
                String idReceitaSelecionada = listaReceitasIds.get(position);

                if (controleEstoque) {
                    // Se for controle de estoque, abrir o AlertDialog
                    abrirDialogoProducao(receitaSelecionada, idReceitaSelecionada);
                } else {
                    // Se não for controle de estoque, abrir a tela de detalhes
                    Intent intent = new Intent(ListaReceitas.this, DetalhesReceita.class);
                    intent.putExtra("nome_receita", receitaSelecionada);
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
                                listaReceitas.add(nomeReceita);
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
        // Criar AlertDialog para inserção da quantidade produzida
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Produção de " + receitaSelecionada);

        final EditText input = new EditText(this);
        input.setHint("Quantidade produzida");
        builder.setView(input);

        // Botão de confirmação
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String quantidadeStr = input.getText().toString();
                if (!quantidadeStr.isEmpty()) {
                    int quantidadeProduzida = Integer.parseInt(quantidadeStr);

                    // Atualizar estoque e registrar produção
                    controleEstoqueClass.atualizarEstoque(idReceitaSelecionada, quantidadeProduzida);

                    Toast.makeText(ListaReceitas.this, "Produção registrada e estoque atualizado.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ListaReceitas.this, "Por favor, insira uma quantidade válida.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Botão de cancelar
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarReceitas();
    }
}
