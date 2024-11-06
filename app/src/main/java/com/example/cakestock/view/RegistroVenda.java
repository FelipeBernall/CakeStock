package com.example.cakestock.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.example.cakestock.model.Cliente;
import com.example.cakestock.model.Produto;
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
    private TextView tvData;
    private EditText editTextDescricao, editTextValorTotal, editTextData;
    private Button btnRegistrarVenda;
    private FirebaseFirestore db;
    private List<Cliente> clientesList;
    private ArrayAdapter<String> clienteAdapter;
    private String clienteId;
    private String produtosSelecionados = "";
    private double valorTotal;
    private Spinner spinnerProduto;
    private EditText editTextQuantidade;
    private ImageButton btnAdicionarProduto;
    private ListView listViewProdutos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_venda);

        autoCompleteCliente = findViewById(R.id.autoCompleteCliente);
        spinnerProduto = findViewById(R.id.spinner_produto);
        editTextDescricao = findViewById(R.id.et_descricao);
        editTextValorTotal = findViewById(R.id.et_valor_total);
        btnRegistrarVenda = findViewById(R.id.btn_salvar);
        editTextData = findViewById(R.id.et_data);
        tvData = findViewById(R.id.tv_data);
        editTextQuantidade = findViewById(R.id.et_quantidade);
        btnAdicionarProduto = findViewById(R.id.btn_adicionar_produto);
        listViewProdutos = findViewById(R.id.lv_produtos_disponiveis);

        db = FirebaseFirestore.getInstance();
        clientesList = new ArrayList<>();

        // Carregar dados dos clientes e produtos
        carregarClientes();
        carregarProdutos(); // Configurar o spinner com hint "Selecionar"

        // Configurar comportamento para o AutoCompleteTextView
        autoCompleteCliente.setOnItemClickListener((parent, view, position, id) -> {
            String clienteNome = clienteAdapter.getItem(position);
            autoCompleteCliente.setText(clienteNome);
            clienteId = clientesList.get(position).getId();
        });

        // Configurar o botão "Adicionar" para adicionar o produto na lista
        btnAdicionarProduto.setOnClickListener(v -> {
            String produtoSelecionado = spinnerProduto.getSelectedItem().toString();
            String quantidadeText = editTextQuantidade.getText().toString();

            if (produtoSelecionado.equals("Selecionar") || quantidadeText.isEmpty()) {
                Toast.makeText(this, "Selecione um produto e insira a quantidade.", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantidade = Integer.parseInt(quantidadeText);

            // Adicionar produto à lista
            produtosSelecionados += produtoSelecionado + " (x" + quantidade + "), ";
            listViewProdutos.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, produtosSelecionados.split(", ")));

            // Limpar os campos de seleção e quantidade
            spinnerProduto.setSelection(0);
            editTextQuantidade.setText("");
        });

        editTextData.setOnClickListener(v -> showDatePickerDialog());
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
                            clientesList.add(cliente);
                            clienteNomes.add(cliente.getNome());
                        }

                        clienteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, clienteNomes);
                        autoCompleteCliente.setAdapter(clienteAdapter);
                    } else {
                        Toast.makeText(this, "Erro ao carregar clientes.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Novo método para carregar produtos do Firestore e configurar o spinner
    private void carregarProdutos() {
        db.collection("Usuarios")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Produtos")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> produtoNomes = new ArrayList<>();
                        produtoNomes.add("Selecionar"); // Adiciona a opção de hint
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Produto produto = document.toObject(Produto.class);
                            produtoNomes.add(produto.getNome());
                        }

                        ArrayAdapter<String> produtoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, produtoNomes);
                        produtoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerProduto.setAdapter(produtoAdapter);
                    } else {
                        Toast.makeText(this, "Erro ao carregar produtos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
