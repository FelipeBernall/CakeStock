package com.example.cakestock.receita;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.example.cakestock.usuario.FormLogin;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoricoProducoes extends AppCompatActivity {

    private TextView tvCurrentMonth;
    private ImageButton btnPreviousMonth, btnNextMonth;
    private ListView lvHistoricoProducoes;
    private FloatingActionButton fabAdicionarProducao;

    private FirebaseFirestore db;
    private String userId;

    private Calendar calendarAtual;
    private HistoricoAdapter historicoAdapter;
    private List<Producao> producoes;

    private static final String TAG = "HistoricoProducoes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_producoes);

        try {
            // Inicializa os componentes da UI
            tvCurrentMonth = findViewById(R.id.tv_current_month);
            btnPreviousMonth = findViewById(R.id.btn_previous_month);
            btnNextMonth = findViewById(R.id.btn_next_month);
            lvHistoricoProducoes = findViewById(R.id.lv_historico_producoes);
            fabAdicionarProducao = findViewById(R.id.fab_adicionar_producao);

            // Inicializa o Firestore e o Calendar
            db = FirebaseFirestore.getInstance();
            calendarAtual = Calendar.getInstance();

            // Obtém o ID do usuário
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            } else {
                Log.e(TAG, "Usuário não autenticado. Redirecionando para login.");
                startActivity(new Intent(HistoricoProducoes.this, FormLogin.class));
                finish();
                return;
            }

            // Configura o adapter e a lista de produções
            producoes = new ArrayList<>();
            historicoAdapter = new HistoricoAdapter(this, new ArrayList<>());
            lvHistoricoProducoes.setAdapter(historicoAdapter);

            // Atualiza o mês atual na interface
            atualizarMesAtual();

            // Configura os botões de navegação
            btnPreviousMonth.setOnClickListener(v -> alterarMes(-1));
            btnNextMonth.setOnClickListener(v -> alterarMes(1));

            // Configura o FAB para adicionar produções
            fabAdicionarProducao.setOnClickListener(v -> {
                Intent intent = new Intent(HistoricoProducoes.this, ListaReceitas.class);
                intent.putExtra("controle_estoque", true); // Flag indicando que é para adicionar produções
                startActivity(intent);
            });

            // Carrega os dados para o mês atual
            carregarProducoes();

        } catch (Exception e) {
            Log.e(TAG, "Erro inesperado na inicialização: ", e);
        }

        if (getIntent().hasExtra("mensagem")) {
            String mensagem = getIntent().getStringExtra("mensagem");
            Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
        }

        lvHistoricoProducoes.setOnItemClickListener((parent, view, position, id) -> {
            Producao producaoSelecionada = producoes.get(position);
            abrirDialogoEdicao(producaoSelecionada);
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarProducoes(); // Atualiza a lista ao voltar para a tela
    }


    private void atualizarMesAtual() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            String mesAtual = dateFormat.format(calendarAtual.getTime());
            tvCurrentMonth.setText(mesAtual);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar o mês atual: ", e);
        }
    }

    private void alterarMes(int incremento) {
        try {
            calendarAtual.add(Calendar.MONTH, incremento);
            atualizarMesAtual();
            carregarProducoes();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao alterar o mês: ", e);
        }
    }

    private void carregarProducoes() {
        try {
            int mesAtual = calendarAtual.get(Calendar.MONTH) + 1;
            int anoAtual = calendarAtual.get(Calendar.YEAR);

            db.collection("Usuarios")
                    .document(userId)
                    .collection("HistoricoProducoes")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            producoes.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    String nomeReceita = document.getString("nomeReceita");
                                    Long quantidadeLong = document.getLong("quantidadeProduzida");
                                    String dataProducao = document.getString("dataProducao");

                                    if (nomeReceita != null && quantidadeLong != null && dataProducao != null) {
                                        int quantidadeProduzida = quantidadeLong.intValue();
                                        if (pertenceAoMesEAno(dataProducao, mesAtual, anoAtual)) {
                                            producoes.add(new Producao(nomeReceita, quantidadeProduzida, dataProducao));
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Erro ao processar documento: " + document.getId(), e);
                                }
                            }

                            // Ordena a lista de produções em ordem decrescente de data
                            producoes.sort((p1, p2) -> p2.getDataProducao().compareTo(p1.getDataProducao()));

                            historicoAdapter.clear();
                            historicoAdapter.addAll(producoes);
                            historicoAdapter.notifyDataSetChanged();
                        } else {
                            Log.e(TAG, "Erro ao carregar produções: ", task.getException());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar produções: ", e);
        }
    }


    private boolean pertenceAoMesEAno(String data, int mesAtual, int anoAtual) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar dataCalendario = Calendar.getInstance();
            dataCalendario.setTime(dateFormat.parse(data));

            int mesProducao = dataCalendario.get(Calendar.MONTH) + 1;
            int anoProducao = dataCalendario.get(Calendar.YEAR);

            return mesProducao == mesAtual && anoProducao == anoAtual;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar data: " + data, e);
            return false;
        }
    }

    private void abrirDialogoEdicao(Producao producao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Produção de " + producao.getNomeReceita());

        final EditText input = new EditText(this);
        input.setHint("Nova quantidade produzida");
        input.setText(String.valueOf(producao.getQuantidadeProduzida())); // Preenche com o valor atual
        builder.setView(input);

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String novaQuantidadeStr = input.getText().toString();
            if (!novaQuantidadeStr.isEmpty()) {
                int novaQuantidade = Integer.parseInt(novaQuantidadeStr);
                int quantidadeAnterior = producao.getQuantidadeProduzida();
                int diferenca = novaQuantidade - quantidadeAnterior;

                ajustarEstoque(producao.getNomeReceita(), diferenca, () -> {
                    // Busca o documento no Firestore para obter o ID correto
                    db.collection("Usuarios")
                            .document(userId)
                            .collection("HistoricoProducoes")
                            .whereEqualTo("dataProducao", producao.getDataProducao())
                            .whereEqualTo("nomeReceita", producao.getNomeReceita())
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    String documentId = querySnapshot.getDocuments().get(0).getId();
                                    // Atualiza o documento com o ID correto
                                    db.collection("Usuarios")
                                            .document(userId)
                                            .collection("HistoricoProducoes")
                                            .document(documentId)
                                            .update("quantidadeProduzida", novaQuantidade)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Produção atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                                                carregarProducoes();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Erro ao atualizar produção.", Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    Toast.makeText(this, "Produção não encontrada.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erro ao buscar produção.", Toast.LENGTH_SHORT).show();
                            });
                });
            } else {
                Toast.makeText(this, "Por favor, insira uma quantidade válida.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    private void ajustarEstoque(String nomeReceita, int diferenca, Runnable callback) {
        if (diferenca == 0) {
            callback.run();
            return;
        }

        // Obtém os ingredientes usados pela receita
        db.collection("Usuarios")
                .document(userId)
                .collection("Receitas")
                .whereEqualTo("nomeReceita", nomeReceita)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String idReceita = queryDocumentSnapshots.getDocuments().get(0).getId();

                        db.collection("Usuarios")
                                .document(userId)
                                .collection("Receitas")
                                .document(idReceita)
                                .collection("IngredientesUtilizados")
                                .get()
                                .addOnSuccessListener(ingredientesSnapshot -> {
                                    for (QueryDocumentSnapshot ingredienteDoc : ingredientesSnapshot) {
                                        String nomeIngrediente = ingredienteDoc.getString("nomeIngrediente");
                                        Long quantidadePorReceita = ingredienteDoc.getLong("quantidadeUsada");

                                        if (nomeIngrediente != null && quantidadePorReceita != null) {
                                            int ajusteQuantidade = quantidadePorReceita.intValue() * Math.abs(diferenca);

                                            ajustarQuantidadeIngredienteFirestore(nomeIngrediente, ajusteQuantidade, diferenca > 0);
                                        }
                                    }

                                    callback.run();
                                })
                                .addOnFailureListener(e -> Log.e("Estoque", "Erro ao carregar ingredientes da receita.", e));
                    } else {
                        Log.e("Estoque", "Receita não encontrada.");
                    }
                })
                .addOnFailureListener(e -> Log.e("Estoque", "Erro ao buscar receita.", e));
    }

    private void ajustarQuantidadeIngredienteFirestore(String nomeIngrediente, int quantidade, boolean reduzir) {
        db.collection("Usuarios")
                .document(userId)
                .collection("Ingredientes")
                .whereEqualTo("nome", nomeIngrediente)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        double quantidadeAtual = doc.getDouble("quantidade");
                        double novaQuantidade = reduzir ? (quantidadeAtual - quantidade) : (quantidadeAtual + quantidade);

                        if (novaQuantidade < 0) {
                            Log.e("Estoque", "Quantidade insuficiente para o ingrediente: " + nomeIngrediente);
                            return;
                        }

                        doc.getReference().update("quantidade", novaQuantidade)
                                .addOnSuccessListener(aVoid -> Log.d("Estoque", "Estoque atualizado para " + nomeIngrediente + ": " + novaQuantidade))
                                .addOnFailureListener(e -> Log.e("Estoque", "Erro ao atualizar estoque do ingrediente: " + nomeIngrediente, e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Estoque", "Erro ao acessar o estoque do ingrediente: " + nomeIngrediente, e));
    }



}