package com.example.cakestock.receita;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.example.cakestock.usuario.FormLogin;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
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
}
