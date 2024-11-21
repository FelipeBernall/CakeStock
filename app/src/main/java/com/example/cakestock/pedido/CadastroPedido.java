package com.example.cakestock.pedido;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
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
    private Button btnCadastrarPedido;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_pedido);

        // Inicializa os componentes do layout
        editDescricao = findViewById(R.id.editDescricao);
        editData = findViewById(R.id.et_data);
        spinnerClientes = findViewById(R.id.spinnerClientes);
        btnCadastrarPedido = findViewById(R.id.btnCadastrarPedido);

        // Inicializa o Firebase Firestore e o usuário atual
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Configura o Spinner de clientes
        configurarSpinnerClientes();

        // Configura o DatePicker para o campo de data
        editData.setOnClickListener(v -> showDatePickerDialog());

        // Listener para o botão de salvar
        btnCadastrarPedido.setOnClickListener(v -> {
            if (validarCampos()) {
                salvarPedido();
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

    private void configurarSpinnerClientes() {
        db.collection("Usuarios")
                .document(user.getUid())
                .collection("Clientes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> clienteNomes = new ArrayList<>();
                        clienteNomes.add("Selecionar"); // Hint para o Spinner
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            clienteNomes.add(document.getString("nome")); // Assume que o campo 'nome' está no documento do cliente
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clienteNomes);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerClientes.setAdapter(adapter);

                        spinnerClientes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                // Você pode capturar o cliente selecionado aqui, se necessário
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // Nada a fazer
                            }
                        });
                    } else {
                        Toast.makeText(this, "Erro ao carregar clientes.", Toast.LENGTH_SHORT).show();
                    }
                });
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

        if (spinnerClientes.getSelectedItem() != null && spinnerClientes.getSelectedItem().toString().equals("Selecionar Cliente")) {
            Toast.makeText(this, "Selecione um cliente.", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    private void salvarPedido() {
        String descricao = editDescricao.getText().toString().trim();
        String data = editData.getText().toString().trim();
        String cliente = spinnerClientes.getSelectedItem().toString();

        Pedido pedido = new Pedido(descricao, data, cliente);

        db.collection("Usuarios")
                .document(user.getUid())
                .collection("Pedidos")
                .add(pedido)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Pedido cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish(); // Volta para a tela anterior
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao cadastrar pedido.", Toast.LENGTH_SHORT).show();
                });
    }
}
