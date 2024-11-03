package com.example.cakestock.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.example.cakestock.controller.EstoqueProdutos;
import com.example.cakestock.model.Cliente;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegistroVenda extends AppCompatActivity {
    private AutoCompleteTextView autoCompleteCliente;
    private TextView textViewProdutos, tvData;
    private EditText editTextDescricao, editTextValorTotal, editTextData;
    private Button btnRegistrarVenda;
    private FirebaseFirestore db;
    private List<Cliente> clientesList;
    private ArrayAdapter<String> clienteAdapter;
    private String clienteId;
    private String produtosSelecionados = "";
    private double valorTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_venda);

        autoCompleteCliente = findViewById(R.id.autoCompleteCliente); // Substituído por AutoCompleteTextView
        textViewProdutos = findViewById(R.id.et_produto);
        editTextDescricao = findViewById(R.id.et_descricao);
        editTextValorTotal = findViewById(R.id.et_valor_total);
        btnRegistrarVenda = findViewById(R.id.btn_salvar);
        editTextData = findViewById(R.id.et_data);
        tvData = findViewById(R.id.tv_data);

        db = FirebaseFirestore.getInstance();
        clientesList = new ArrayList<>();

        editTextValorTotal.setFocusable(true);
        editTextValorTotal.setFocusableInTouchMode(true);

        // Método para carregar os clientes do Firestore
        carregarClientes();

        // Configurar comportamento para o AutoCompleteTextView
        autoCompleteCliente.setOnItemClickListener((parent, view, position, id) -> {
            // Obter o cliente selecionado
            String clienteNome = clienteAdapter.getItem(position);
            autoCompleteCliente.setText(clienteNome); // Atualiza o campo com o nome do cliente
            clienteId = clientesList.get(position).getId(); // Salva o ID do cliente selecionado
        });

        editTextData.setOnClickListener(v -> showDatePickerDialog());

        textViewProdutos.setOnClickListener(v -> {
            Intent intent = new Intent(RegistroVenda.this, EstoqueProdutos.class);
            startActivityForResult(intent, 2);
        });

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

    // Método para carregar os clientes do Firestore
    private void carregarClientes() {
        db.collection("Usuarios")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Clientes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> clienteNomes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Cliente cliente = document.toObject(Cliente.class);
                            clientesList.add(cliente); // Adiciona à lista de clientes
                            clienteNomes.add(cliente.getNome()); // Adiciona o nome à lista de nomes para o adapter
                        }

                        // Configura o adapter para o AutoCompleteTextView
                        clienteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, clienteNomes);
                        autoCompleteCliente.setAdapter(clienteAdapter);
                    } else {
                        Toast.makeText(this, "Erro ao carregar clientes.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 2) { // Produtos
                String produto = data.getStringExtra("produtoNome");
                int quantidade = data.getIntExtra("quantidade", 0);
                double precoProduto = data.getDoubleExtra("precoProduto", 0.0);

                // Adicione o log aqui para verificar os dados recebidos
                Log.d("RegistroVenda", "Produto: " + produto + ", Quantidade: " + quantidade + ", Preço: " + precoProduto);

                produtosSelecionados += produto + " (x" + quantidade + "), ";
                textViewProdutos.setText(produtosSelecionados);

                // Atualizar o valor total com base no preço do produto
                valorTotal += precoProduto * quantidade;
                editTextValorTotal.setText(String.valueOf(valorTotal));
            }
        }
    }


}
