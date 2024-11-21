package com.example.cakestock.pedido;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cakestock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // Listener para o botão de salvar
        btnCadastrarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
                    salvarPedido();
                }
            }
        });
    }

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
                });
    }
}
