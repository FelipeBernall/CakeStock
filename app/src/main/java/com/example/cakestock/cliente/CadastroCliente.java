package com.example.cakestock.cliente;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

// Responsável por Cadastrar , Editar ou ADD Clientes
public class CadastroCliente extends AppCompatActivity {

    // Campos de entrada de dados
    private EditText editTextNome, editTextTelefone;
    private Button btnCadastrar;
    private FirebaseFirestore db;
    private String clienteId; // ID do cliente para edição
    private static final String TAG = "CadastroCliente";
    private static final Pattern TELEFONE_PATTERN = Pattern.compile("\\(\\d{2}\\) \\d{5}-\\d{4}");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_cliente);

        // Inicialização dos campos de entrada
        editTextNome = findViewById(R.id.editNome);
        editTextTelefone = findViewById(R.id.editTelefone);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        db = FirebaseFirestore.getInstance();
        ImageButton btnVoltar = findViewById(R.id.btn_voltar);
        btnVoltar.setOnClickListener(v -> onBackPressed());


        // Verifica se está editando um cliente
        if (getIntent().hasExtra("clienteId")) {
            clienteId = getIntent().getStringExtra("clienteId");
            carregarDadosCliente(clienteId); // Carrega os dados para edição
            btnCadastrar.setText("Editar");
        } else {
            btnCadastrar.setText("Cadastrar");
        }

        // Aplica a formatação para o campo de telefone
        aplicarMascaraTelefone();

        // Configura o botão para cadastrar ou editar o cliente
        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // id diferente de null -> tem cadastro -> editar
                if (clienteId != null) {
                    editarCliente(clienteId); // Edita um cliente existente
                } else {
                    cadastrarCliente(); // Cria um novo cliente
                }
            }
        });
    }

    // Carrega os dados de um cliente EXISTENTE para edição
    private void carregarDadosCliente(String clienteId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Usuarios").document(userId).collection("Clientes").document(clienteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Preenche os campos com os dados do cliente
                        editTextNome.setText(documentSnapshot.getString("nome"));
                        editTextTelefone.setText(documentSnapshot.getString("telefone"));
                    } else {
                        Toast.makeText(CadastroCliente.this, "Cliente não encontrado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(CadastroCliente.this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show());
    }

    // Cadastra um novo cliente no Firebase
    private void cadastrarCliente() {
        String nome = editTextNome.getText().toString().trim();
        String telefone = editTextTelefone.getText().toString().trim();

        if (validarCampos(nome, telefone)) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Verifica se o nome já existe no Firestore
            db.collection("Usuarios").document(userId).collection("Clientes")
                    .whereEqualTo("nome", nome)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(CadastroCliente.this, "Já existe um cliente com este nome.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Cria um novo cliente e o salva no Firestore
                            Cliente cliente = new Cliente(nome, telefone);
                            db.collection("Usuarios").document(userId).collection("Clientes")
                                    .add(cliente)
                                    .addOnSuccessListener(documentReference -> {
                                        String clienteId = documentReference.getId();
                                        cliente.setId(clienteId);
                                        documentReference.set(cliente)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(CadastroCliente.this, "Cliente cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(CadastroCliente.this, "Erro ao salvar o ID do cliente.", Toast.LENGTH_SHORT).show());
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(CadastroCliente.this, "Erro ao cadastrar cliente.", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(CadastroCliente.this, "Erro ao verificar duplicidade.", Toast.LENGTH_SHORT).show());
        }
    }


    // Edita os dados de um cliente existente no Firebase
    private void editarCliente(String clienteId) {
        String nome = editTextNome.getText().toString().trim();
        String telefone = editTextTelefone.getText().toString().trim();

        if (validarCampos(nome, telefone)) {
            Cliente cliente = new Cliente(nome, telefone);
            cliente.setId(clienteId); // Certifique-se de definir o ID do cliente

            // Obtendo o ID do usuário logado para montar o caminho correto
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            db.collection("Usuarios").document(userId).collection("Clientes")
                    .document(clienteId)
                    .set(cliente) // Agora o cliente já tem o ID definido
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CadastroCliente.this, "Cliente editado com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CadastroCliente.this, "Erro ao editar cliente.", Toast.LENGTH_SHORT).show();
                    });
        }
    }



    private boolean validarCampos(String nome, String telefone) {
        if (nome.isEmpty()) {
            Toast.makeText(this, "O nome não pode estar vazio.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!TELEFONE_PATTERN.matcher(telefone).matches()) {
            Toast.makeText(this, "O telefone deve seguir o formato (00) 00000-0000.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void aplicarMascaraTelefone() {
        editTextTelefone.addTextChangedListener(new TextWatcher() {
            boolean isUpdating;
            String oldText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString().replaceAll("[^\\d]", "");
                if (str.length() > 11) { // Limite para 11 caracteres (sem formatação)
                    str = str.substring(0, 11); // Trunca a string se exceder
                }
                if (isUpdating) {
                    oldText = str;
                    isUpdating = false;
                    return;
                }

                isUpdating = true;
                StringBuilder formatted = new StringBuilder();

                if (str.length() > 2) {
                    formatted.append("(").append(str.substring(0, 2)).append(") ");
                    if (str.length() > 7) {
                        formatted.append(str.substring(2, 7)).append("-").append(str.substring(7));
                    } else if (str.length() > 2) {
                        formatted.append(str.substring(2));
                    }
                } else {
                    formatted.append(str);
                }

                editTextTelefone.setText(formatted.toString());
                editTextTelefone.setSelection(formatted.length());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

}
