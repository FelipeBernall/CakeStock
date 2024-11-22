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

public class RegistroDespesa extends AppCompatActivity {

    private EditText editTextDescricao, editTextValor, editTextData;
    private Button btnSalvar;
    private ImageButton btnVoltar;

    private FirebaseFirestore db;
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

        db = FirebaseFirestore.getInstance();
        calendario = Calendar.getInstance();

        // Configurar botão de voltar
        btnVoltar.setOnClickListener(v -> onBackPressed());

        // Configurar o DatePicker para o campo de data
        editTextData.setOnClickListener(v -> abrirDatePicker());

        // Configurar o botão de salvar
        btnSalvar.setOnClickListener(v -> registrarDespesa());
    }

    private void abrirDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendario.set(year, month, dayOfMonth);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            editTextData.setText(format.format(calendario.getTime()));
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void registrarDespesa() {
        String descricao = editTextDescricao.getText().toString();
        String valor = editTextValor.getText().toString();
        String data = editTextData.getText().toString();

        if (descricao.isEmpty() || valor.isEmpty() || data.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Map para armazenar os dados da despesa
        Map<String, Object> despesa = new HashMap<>();
        despesa.put("descricao", descricao);
        despesa.put("valor", Double.parseDouble(valor));
        despesa.put("data", data);

        // Enviar dados para o Firestore
        db.collection("Usuarios")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Despesas")
                .add(despesa)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Despesa registrada com sucesso!", Toast.LENGTH_SHORT).show();

                        // Redireciona para a tela de transações
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
}
