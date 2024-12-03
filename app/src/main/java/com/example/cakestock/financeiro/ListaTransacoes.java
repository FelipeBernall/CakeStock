package com.example.cakestock.financeiro;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class ListaTransacoes extends AppCompatActivity {
    // Componentes da interface
    private ListView lvTransacoes;
    private TextView tvVendas, tvDespesas, tvSaldo;
    private FirebaseFirestore db;
    private TextView tvCurrentMonth;
    private Calendar calendarAtual;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_transacoes);

        // Inicializa os componentes de interface
        lvTransacoes = findViewById(R.id.lv_transacoes);
        tvVendas = findViewById(R.id.tv_vendas);
        tvDespesas = findViewById(R.id.tv_despesas);
        tvSaldo = findViewById(R.id.tv_saldo);
        tvCurrentMonth = findViewById(R.id.tv_current_month);

        // Botões para adicionar vendas/despesas e navegar entre meses
        ImageButton btnVendas = findViewById(R.id.btn_vendas);
        ImageButton btnDespesas = findViewById(R.id.btn_despesas);
        ImageButton btnPreviousMonth = findViewById(R.id.btn_previous_month);
        ImageButton btnNextMonth = findViewById(R.id.btn_next_month);

        // Redireciona para a tela de registro de venda
        btnVendas.setOnClickListener(v -> {
            Intent intent = new Intent(ListaTransacoes.this, RegistroVenda.class);
            startActivity(intent);
            finish();
        });

        // Redireciona para a tela de registro de despesa
        btnDespesas.setOnClickListener(v -> {
            Intent intent = new Intent(ListaTransacoes.this, RegistroDespesa.class);
            startActivity(intent);
            finish();
        });

        // Inicializa o Calendar e exibe o mês atual
        calendarAtual = Calendar.getInstance();
        atualizarMesAtual();

        // Configurações dos botões de navegação
        btnPreviousMonth.setOnClickListener(v -> alterarMes(-1));
        btnNextMonth.setOnClickListener(v -> alterarMes(1));

        // Inicializa o Firestore
        db = FirebaseFirestore.getInstance();

        // Carrega todas as transações (vendas e despesas)
        carregarTransacoes();

        // Configurações de interação com a lista de transações
        lvTransacoes.setOnItemLongClickListener((parent, view, position, id) -> {
            Transacao transacao = (Transacao) parent.getItemAtPosition(position);

            // Confirmação para excluir uma transação
            new AlertDialog.Builder(ListaTransacoes.this)
                    .setTitle("Confirmar Exclusão")
                    .setMessage("Você tem certeza que deseja excluir esta transação?")
                    .setPositiveButton("Sim", (dialog, which) -> excluirTransacao(transacao))
                    .setNegativeButton("Não", null)
                    .show();
            return true;
        });

        lvTransacoes.setOnItemClickListener((parent, view, position, id) -> {
            // Redireciona para a tela de edição de uma transação
            Transacao transacao = (Transacao) parent.getItemAtPosition(position);
            Intent intent;

            if (transacao.getValorTotal() >= 0) {
                intent = new Intent(ListaTransacoes.this, RegistroVenda.class);
            } else {
                intent = new Intent(ListaTransacoes.this, RegistroDespesa.class);
            }

            intent.putExtra("transacao", transacao);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarTransacoes();
    }

    // Carrega vendas e despesas do Firestore e atualiza a interface
    private void carregarTransacoes() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        int mesAtual = calendarAtual.get(Calendar.MONTH) + 1;
        int anoAtual = calendarAtual.get(Calendar.YEAR);

        List<Transacao> transacoes = new ArrayList<>();
        final double[] totalVendas = {0.0};
        final double[] totalDespesas = {0.0};

        // Carrega todas as vendas
        db.collection("Usuarios")
                .document(userId)
                .collection("Vendas")
                .get()
                .addOnCompleteListener(taskVendas -> {
                    if (taskVendas.isSuccessful()) {
                        for (QueryDocumentSnapshot document : taskVendas.getResult()) {
                            String descricao = document.getString("descricao");
                            Double valorTotal = document.getDouble("valorTotal");
                            String data = document.getString("data");

                            if (data != null && pertenceAoMesEAno(data, mesAtual, anoAtual)) {
                                totalVendas[0] += valorTotal;
                                transacoes.add(new Transacao(descricao, data, null, null, valorTotal));
                            }
                        }

                        // Carrega despesas após as vendas
                        db.collection("Usuarios")
                                .document(userId)
                                .collection("Despesas")
                                .get()
                                .addOnCompleteListener(taskDespesas -> {
                                    if (taskDespesas.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : taskDespesas.getResult()) {
                                            String descricao = document.getString("descricao");
                                            Double valor = document.getDouble("valor");
                                            String data = document.getString("data");

                                            if (data != null && pertenceAoMesEAno(data, mesAtual, anoAtual)) {
                                                totalDespesas[0] += valor;
                                                transacoes.add(new Transacao(descricao, data, null, null, -valor));
                                            }
                                        }

                                        // Atualiza lista de transações
                                        atualizarResumo(totalVendas[0], totalDespesas[0]);
                                        atualizarLista(transacoes); // Lista unificada
                                    }
                                });
                    }
                });
    }



    // Verifica se uma data pertence ao mês e ano atuais
    private boolean pertenceAoMesEAno(String data, int mesAtual, int anoAtual) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar dataCalendario = Calendar.getInstance();
            dataCalendario.setTime(dateFormat.parse(data));

            int mesTransacao = dataCalendario.get(Calendar.MONTH) + 1;
            int anoTransacao = dataCalendario.get(Calendar.YEAR);

            return mesTransacao == mesAtual && anoTransacao == anoAtual;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Atualiza a lista de transações na interface
    private void atualizarLista(List<Transacao> transacoes) {
        // Ordena as transações pela data em ordem decrescente
        Collections.sort(transacoes, (t1, t2) -> {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                // Compare as datas para garantir que as mais recentes fiquem no topo
                return dateFormat.parse(t2.getData()).compareTo(dateFormat.parse(t1.getData()));
            } catch (Exception e) {
                e.printStackTrace();
                return 0; // Se houver erro na comparação, mantém a posição original
            }
        });

        // Configura o Adapter atualizado
        TransacaoAdapter adapter = new TransacaoAdapter(this, transacoes);
        lvTransacoes.setAdapter(adapter);
    }

    // Atualiza os valores das TextViews de resumo
    private void atualizarResumo(double totalVendas, double totalDespesas) {
        tvVendas.setText(String.format("R$ %.2f", totalVendas));
        tvDespesas.setText(String.format("R$ %.2f", totalDespesas));

        // Calcula o saldo e atualiza a TextView de saldo
        double saldo = totalVendas - totalDespesas;
        tvSaldo.setText(String.format("R$ %.2f", saldo));

        // Altera a cor do saldo com base no valor
        if (saldo >= 0) {
            tvSaldo.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvSaldo.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    // Formata e exibe o mês atual no TextView
    private void atualizarMesAtual() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String mesAtual = dateFormat.format(calendarAtual.getTime());
        tvCurrentMonth.setText(mesAtual);
    }

    // Altera o mês no Calendar
    private void alterarMes(int incremento) {
        calendarAtual.add(Calendar.MONTH, incremento);
        atualizarMesAtual();

        // Recarrega as transações para o mês atualizado
        carregarTransacoes();
    }

    // Exclui uma transação do Firestore
    private void excluirTransacao(Transacao transacao) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String collection = transacao.getValorTotal() >= 0 ? "Vendas" : "Despesas";

        db.collection("Usuarios")
                .document(userId)
                .collection(collection)
                .whereEqualTo("descricao", transacao.getDescricao())
                .whereEqualTo("data", transacao.getData())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String docId = task.getResult().getDocuments().get(0).getId();
                        db.collection("Usuarios")
                                .document(userId)
                                .collection(collection)
                                .document(docId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Transação excluída com sucesso!", Toast.LENGTH_SHORT).show();
                                    carregarTransacoes();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao excluir transação: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Transação não encontrada.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
