package com.example.cakestock.financeiro;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Tela para registrar ou atualizar despesas
public class RegistroDespesa extends AppCompatActivity {

    // Componentes da interface
    private EditText editTextDescricao, editTextValor, editTextData;
    private Button btnSalvar;
    private ImageButton btnVoltar;
    private FirebaseFirestore db;

    // Objeto para manipulação de datas.
    private Calendar calendario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_despesa);

        // Inicializar os componentes
        editTextDescricao = findViewById(R.id.et_descricao);
        editTextValor = findViewById(R.id.et_valor);
        editTextData = findViewById(R.id.et_data);
        btnSalvar = findViewById(R.id.btn_salvar);
        btnVoltar = findViewById(R.id.btn_voltar);

        // Inicializa o Firestore.
        db = FirebaseFirestore.getInstance();
        calendario = Calendar.getInstance();

        // Configurar botão de voltar
        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(RegistroDespesa.this, ListaTransacoes.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Finaliza a atividade atual
        });

        // Configurar o DatePicker para o campo de data
        editTextData.setOnClickListener(v -> abrirDatePicker());

        // Configurar o botão de salvar
        btnSalvar.setOnClickListener(v -> registrarDespesa());

        // Verifica se a tela recebeu uma transação existente para edição
        Intent intent = getIntent();
        Transacao transacao = (Transacao) intent.getSerializableExtra("transacao");

        // Preenche os campos com os dados da transação recebida
        if (transacao != null) {
            editTextDescricao.setText(transacao.getDescricao());
            editTextValor.setText(String.valueOf(Math.abs(transacao.getValorTotal())));
            editTextData.setText(transacao.getData());

            // Configura o botão para atualizar a despesa em vez de criar uma nova
            btnSalvar.setOnClickListener(v -> atualizarDespesa(transacao));
        }


    }

    // Abre o seletor de data e preenche o campo de data com a data selecionada
    private void abrirDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendario.set(year, month, dayOfMonth);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            editTextData.setText(format.format(calendario.getTime()));
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    // Registra uma nova despesa no banco de dados
    private void registrarDespesa() {
        String descricao = editTextDescricao.getText().toString();
        String valor = editTextValor.getText().toString();
        String data = editTextData.getText().toString();

        // Verifica se todos os campos obrigatórios estão preenchidos
        if (descricao.isEmpty() || valor.isEmpty() || data.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Map para armazenar os dados da despesa
        Map<String, Object> despesa = new HashMap<>();
        despesa.put("descricao", descricao);
        despesa.put("valor", Double.parseDouble(valor));
        despesa.put("data", data);

        // Adiciona a despesa ao Firestore
        db.collection("Usuarios")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Despesas")
                .add(despesa)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Despesa registrada com sucesso!", Toast.LENGTH_SHORT).show();

                        // Redireciona para a tela de lista de transações
                        Intent intent = new Intent(RegistroDespesa.this, ListaTransacoes.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Erro ao registrar despesa.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar despesa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Atualiza uma despesa existente no banco de dados
    private void atualizarDespesa(Transacao transacao) {
        String descricao = editTextDescricao.getText().toString();
        double valor = Double.parseDouble(editTextValor.getText().toString());
        String data = editTextData.getText().toString();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Localiza a despesa no banco de dados e atualiza os dados.
        db.collection("Usuarios")
                .document(userId)
                .collection("Despesas")
                .whereEqualTo("descricao", transacao.getDescricao())
                .whereEqualTo("data", transacao.getData())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String docId = task.getResult().getDocuments().get(0).getId();
                        Map<String, Object> updatedData = new HashMap<>();
                        updatedData.put("descricao", descricao);
                        updatedData.put("valor", valor); // Deve ser positivo para despesa
                        updatedData.put("data", data);

                        db.collection("Usuarios")
                                .document(userId)
                                .collection("Despesas")
                                .document(docId)
                                .update(updatedData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Despesa atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao atualizar despesa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Erro ao localizar despesa.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
