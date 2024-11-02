package com.example.cakestock.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.controller.EstoqueProdutos;
import com.example.cakestock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegistroVenda extends AppCompatActivity {
    private TextView textViewCliente, textViewProdutos, tvData;
    private EditText editTextDescricao, editTextValorTotal, editTextData;
    private Button btnRegistrarVenda;
    private FirebaseFirestore db;
    private String clienteId;
    private String produtosSelecionados = "";
    private double valorTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_venda);

        textViewCliente = findViewById(R.id.et_cliente);
        textViewProdutos = findViewById(R.id.et_produto);
        editTextDescricao = findViewById(R.id.et_descricao);
        editTextValorTotal = findViewById(R.id.et_valor_total);
        btnRegistrarVenda = findViewById(R.id.btn_salvar);
        editTextData = findViewById(R.id.et_data);
        tvData = findViewById(R.id.tv_data);

        editTextValorTotal.setFocusable(true);
        editTextValorTotal.setFocusableInTouchMode(true);

        db = FirebaseFirestore.getInstance();

        editTextData.setOnClickListener(v -> showDatePickerDialog());

        textViewCliente.setOnClickListener(v -> {
            Intent intent = new Intent(RegistroVenda.this, ListaClientes.class);
            intent.putExtra("fromRegistroVenda", true);
            startActivityForResult(intent, 1);
        });

        textViewProdutos.setOnClickListener(v -> startActivityForResult(new Intent(RegistroVenda.this, EstoqueProdutos.class), 2));

        btnRegistrarVenda.setOnClickListener(v -> registrarVenda());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RegistroVenda.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    editTextData.setText(formattedDate);
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void registrarVenda() {
        String descricao = editTextDescricao.getText().toString();
        String valor = editTextValorTotal.getText().toString();
        String data = editTextData.getText().toString();

        if (clienteId == null || produtosSelecionados.isEmpty() || descricao.isEmpty() || valor.isEmpty() || data.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> venda = new HashMap<>();
        venda.put("clienteId", clienteId);
        venda.put("produtos", produtosSelecionados);
        venda.put("descricao", descricao);
        venda.put("valorTotal", Double.parseDouble(valor));
        venda.put("data", data);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference vendasRef = db.collection("Usuarios").document(userId).collection("Vendas");

        vendasRef.add(venda).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RegistroVenda.this, "Venda registrada com sucesso.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(RegistroVenda.this, "Erro ao registrar venda.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) { // Cliente
                clienteId = data.getStringExtra("clienteId");
                String clienteNome = data.getStringExtra("clienteNome");
                textViewCliente.setText(clienteNome);
            } else if (requestCode == 2) { // Produtos
                String produto = data.getStringExtra("produtoNome");
                int quantidade = data.getIntExtra("quantidade", 0);
                produtosSelecionados += produto + " (x" + quantidade + "), ";
                textViewProdutos.setText(produtosSelecionados);
            }
        }
    }
}
