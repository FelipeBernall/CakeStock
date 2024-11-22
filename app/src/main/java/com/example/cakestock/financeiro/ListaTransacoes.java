package com.example.cakestock.financeiro;

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
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class ListaTransacoes extends AppCompatActivity {

    private ListView lvTransacoes;
    private TextView tvVendas, tvDespesas, tvSaldo;
    private FirebaseFirestore db;
    private TextView tvCurrentMonth;
    private Calendar calendarAtual;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_transacoes);

        // Inicializa os componentes de UI
        lvTransacoes = findViewById(R.id.lv_transacoes);
        tvVendas = findViewById(R.id.tv_vendas);
        tvDespesas = findViewById(R.id.tv_despesas);
        tvSaldo = findViewById(R.id.tv_saldo);
        tvCurrentMonth = findViewById(R.id.tv_current_month);

        ImageButton btnVendas = findViewById(R.id.btn_vendas);
        ImageButton btnDespesas = findViewById(R.id.btn_despesas);
        ImageButton btnPreviousMonth = findViewById(R.id.btn_previous_month);
        ImageButton btnNextMonth = findViewById(R.id.btn_next_month);

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
    }


    private void carregarTransacoes() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Lista para armazenar as transações
        List<String> transacoesList = new ArrayList<>();

        // Variáveis para totalizar vendas e despesas
        final double[] totalVendas = {0.0};
        final double[] totalDespesas = {0.0};

        // Carregar Vendas
        db.collection("Usuarios")
                .document(userId)
                .collection("Vendas")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(ListaTransacoes.this, "Erro ao carregar vendas.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String descricao = document.getString("descricao");
                        Double valorTotal = document.getDouble("valorTotal");
                        if (valorTotal != null) {
                            totalVendas[0] += valorTotal;
                        }
                        String venda = descricao + ": " + String.format("+ R$ %.2f", valorTotal);
                        transacoesList.add(venda);
                    }

                    atualizarLista(transacoesList);
                    atualizarResumo(totalVendas[0], totalDespesas[0]);
                });

        // Carregar Despesas
        db.collection("Usuarios")
                .document(userId)
                .collection("Despesas")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(ListaTransacoes.this, "Erro ao carregar despesas.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String descricao = document.getString("descricao");
                        Double valor = document.getDouble("valor");
                        if (valor != null) {
                            totalDespesas[0] += valor;
                        }
                        String despesa = descricao + ": " + String.format("- R$ %.2f", valor);
                        transacoesList.add(despesa);
                    }

                    atualizarLista(transacoesList);
                    atualizarResumo(totalVendas[0], totalDespesas[0]);
                });
    }

    private void atualizarLista(List<String> transacoesList) {
        // Atualiza o ListView com as transações
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ListaTransacoes.this, android.R.layout.simple_list_item_1, transacoesList);
        lvTransacoes.setAdapter(adapter);
    }

    private void atualizarResumo(double totalVendas, double totalDespesas) {
        // Atualiza os valores das TextViews de resumo
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

    private void atualizarMesAtual() {
        // Formata e exibe o mês atual no TextView
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String mesAtual = dateFormat.format(calendarAtual.getTime());
        tvCurrentMonth.setText(mesAtual);
    }

    private void alterarMes(int incremento) {
        // Altera o mês no Calendar
        calendarAtual.add(Calendar.MONTH, incremento);
        atualizarMesAtual();

        // Recarrega as transações para o mês atualizado
        carregarTransacoes();
    }

}
