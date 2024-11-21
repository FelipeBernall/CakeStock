package com.example.cakestock.pedido;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cakestock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CadastroPedido extends AppCompatActivity {

    private EditText editDescricao, editData;
    private Spinner spinnerClientes;
    private Button btnCadastrarPedido, btnExcluirPedido;  // Adicionando o botão de exclusão
    private FirebaseFirestore db;
    private FirebaseUser user;
    private ImageButton btnVoltar;
    private String pedidoId;  // Atributo para o ID do pedido

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_pedido);

        editDescricao = findViewById(R.id.editDescricao);
        editData = findViewById(R.id.et_data);
        spinnerClientes = findViewById(R.id.spinnerClientes);
        btnCadastrarPedido = findViewById(R.id.btnCadastrarPedido);
        btnExcluirPedido = findViewById(R.id.btnExcluirPedido);  // Referência ao botão de excluir
        btnVoltar = findViewById(R.id.btn_voltar);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        btnVoltar.setOnClickListener(v -> finish());

        configurarSpinnerClientes();

        // Recupera dados da intent
        Intent intent = getIntent();
        pedidoId = intent.getStringExtra("pedidoId");  // Recebe o pedidoId
        String descricao = intent.getStringExtra("descricao");
        String data = intent.getStringExtra("data");
        String cliente = intent.getStringExtra("cliente");

        editDescricao.setText(descricao);
        editData.setText(data);

        if (cliente != null) {
            spinnerClientes.setSelection(getIndex(spinnerClientes, cliente));
        }

        editData.setOnClickListener(v -> showDatePickerDialog());

        btnCadastrarPedido.setOnClickListener(v -> {
            if (validarCampos()) {
                salvarPedido();
            }
        });

        // Verifica se o pedidoId não é nulo e torna o botão de excluir visível
        if (pedidoId != null) {
            btnExcluirPedido.setVisibility(View.VISIBLE);  // Tornar o botão visível
            btnExcluirPedido.setOnClickListener(v -> excluirPedido());  // Configura o clique do botão de excluir
        }
    }

    private int getIndex(Spinner spinner, String clienteNome) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(clienteNome)) {
                return i;
            }
        }
        return 0;
    }

    private void configurarSpinnerClientes() {
        db.collection("Usuarios")
                .document(user.getUid())
                .collection("Clientes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> clienteNomes = new ArrayList<>();
                        clienteNomes.add("Selecionar");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            clienteNomes.add(document.getString("nome"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clienteNomes);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerClientes.setAdapter(adapter);

                        if (!clienteNomes.isEmpty()) {
                            String clientePassado = getIntent().getStringExtra("cliente");
                            if (clientePassado != null) {
                                spinnerClientes.setSelection(clienteNomes.indexOf(clientePassado));
                            }
                        }
                    } else {
                        Toast.makeText(this, "Erro ao carregar clientes.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    editData.setText(formattedDate);
                },
                year, month, day);

        datePickerDialog.show();
    }

    private boolean validarCampos() {
        boolean valid = true;

        if (editDescricao.getText().toString().trim().isEmpty()) {
            editDescricao.setError("Descrição é obrigatória.");
            valid = false;
        }

        if (editData.getText().toString().trim().isEmpty()) {
            editData.setError("Data é obrigatória.");
            valid = false;
        }

        return valid;
    }

    private void salvarPedido() {
        String descricao = editDescricao.getText().toString().trim();
        String data = editData.getText().toString().trim();
        String cliente = spinnerClientes.getSelectedItem().toString();

        // Verifica se o cliente selecionado é "Selecionar"
        if (cliente.equals("Selecionar")) {
            Toast.makeText(CadastroPedido.this, "Por favor, selecione um cliente válido.", Toast.LENGTH_SHORT).show();
            return;  // Impede o salvamento e retorna
        }

        if (pedidoId == null) {
            // Se o pedidoId for nulo, significa que é um novo pedido
            Pedido pedido = new Pedido(null, descricao, data, cliente);
            db.collection("Usuarios")
                    .document(user.getUid())
                    .collection("Pedidos")
                    .add(pedido)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(CadastroPedido.this, "Pedido cadastrado com sucesso.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CadastroPedido.this, ListaPedidos.class);
                            intent.putExtra("atualizar", true);  // Adicionando o sinalizador
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  // Limpa a pilha de atividades e leva diretamente para a ListaPedidos
                            startActivity(intent);  // Redireciona para ListaPedidos
                            finish();  // Finaliza a tela de cadastro para garantir que não permaneça na pilha

                        } else {
                            Toast.makeText(CadastroPedido.this, "Erro ao cadastrar pedido.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Se já tiver pedidoId, atualiza o pedido existente
            Pedido pedido = new Pedido(pedidoId, descricao, data, cliente);
            db.collection("Usuarios")
                    .document(user.getUid())
                    .collection("Pedidos")
                    .document(pedidoId)
                    .set(pedido)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(CadastroPedido.this, "Pedido atualizado com sucesso.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CadastroPedido.this, ListaPedidos.class);
                            intent.putExtra("atualizar", true);  // Adicionando um sinalizador
                            startActivity(intent);  // Redirecionando para ListaPedidos
                            finish();
                        } else {
                            Toast.makeText(CadastroPedido.this, "Erro ao atualizar pedido.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void excluirPedido() {
        // Verifica se existe um pedidoId válido
        if (pedidoId != null) {
            // Criar um AlertDialog para confirmação
            new AlertDialog.Builder(CadastroPedido.this)
                    .setTitle("Confirmar Exclusão")
                    .setMessage("Você tem certeza de que deseja excluir este pedido?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        // Exclui o pedido
                        db.collection("Usuarios")
                                .document(user.getUid())
                                .collection("Pedidos")
                                .document(pedidoId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(CadastroPedido.this, "Pedido excluído com sucesso.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(CadastroPedido.this, ListaPedidos.class);
                                    intent.putExtra("atualizar", true);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  // Limpa a pilha de atividades
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(CadastroPedido.this, "Erro ao excluir pedido.", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Não", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();

        }
    }

}

