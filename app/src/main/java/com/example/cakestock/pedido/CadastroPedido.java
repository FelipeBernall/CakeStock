package com.example.cakestock.pedido;

<<<<<<< HEAD
import android.app.DatePickerDialog;
=======
>>>>>>> ed5652af40b377ea25bdfd91684cfd4e584e010e
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
<<<<<<< HEAD
=======
import android.widget.TextView;
>>>>>>> ed5652af40b377ea25bdfd91684cfd4e584e010e
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cakestock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
<<<<<<< HEAD
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
=======
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
>>>>>>> ed5652af40b377ea25bdfd91684cfd4e584e010e

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

<<<<<<< HEAD
        // Configura o DatePicker para o campo de data
        editData.setOnClickListener(v -> showDatePickerDialog());

        // Listener para o botão de salvar
        btnCadastrarPedido.setOnClickListener(v -> {
            if (validarCampos()) {
                salvarPedido();
=======
        // Listener para o botão de salvar
        btnCadastrarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
                    salvarPedido();
                }
>>>>>>> ed5652af40b377ea25bdfd91684cfd4e584e010e
            }
        });
    }

<<<<<<< HEAD
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
=======
    private void configurarSpinnerClientes() {
        // Aqui você deve configurar o Adapter para o Spinner com os dados dos clientes.
        // Exemplo:
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getClientes());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClientes.setAdapter(adapter);
    }

    private String[] getClientes() {
        // Aqui você deve obter os clientes da sua base de dados ou algum recurso.
        // Exemplo está retornando um array de clientes fictícios.
        return new String[]{"Cliente 1", "Cliente 2", "Cliente 3"};
>>>>>>> ed5652af40b377ea25bdfd91684cfd4e584e010e
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

<<<<<<< HEAD
        if (spinnerClientes.getSelectedItem() != null && spinnerClientes.getSelectedItem().toString().equals("Selecionar Cliente")) {
            Toast.makeText(this, "Selecione um cliente.", Toast.LENGTH_SHORT).show();
            valid = false;
        }

=======
>>>>>>> ed5652af40b377ea25bdfd91684cfd4e584e010e
        return valid;
    }

    private void salvarPedido() {
        String descricao = editDescricao.getText().toString().trim();
        String data = editData.getText().toString().trim();
        String cliente = spinnerClientes.getSelectedItem().toString();

<<<<<<< HEAD
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
=======
        // Cria um mapa de dados para o pedido
        Pedido pedido = new Pedido(descricao, data, cliente);

        // Salva no Firebase Firestore
        CollectionReference pedidosRef = db.collection("Usuarios")
                .document(user.getUid())
                .collection("Pedidos");

        pedidosRef.add(pedido)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CadastroPedido.this, "Pedido cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish(); // Volta para a tela anterior
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CadastroPedido.this, "Erro ao cadastrar pedido.", Toast.LENGTH_SHORT).show();
>>>>>>> ed5652af40b377ea25bdfd91684cfd4e584e010e
                });
    }
}
