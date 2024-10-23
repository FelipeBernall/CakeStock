package com.example.cakestock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListaClientes extends AppCompatActivity {
    private FirebaseFirestore db;
    private ListView listView;
    private ClienteAdapter adapter;
    private List<Cliente> clienteList;
    private List<Cliente> allClientes;
    private EditText searchField;
    private boolean fromRegistroVenda = false; // Novo campo para controlar a origem

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_clientes);

        searchField = findViewById(R.id.searchField);
        ImageButton searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> searchClientes());

        searchField.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchClientes();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        FloatingActionButton fabAdicionarCliente = findViewById(R.id.fab_adicionar_cliente);
        fabAdicionarCliente.setOnClickListener(v -> startActivity(new Intent(ListaClientes.this, CadastroCliente.class)));

        db = FirebaseFirestore.getInstance();
        listView = findViewById(R.id.lv_lista_clientes);
        clienteList = new ArrayList<>();
        allClientes = new ArrayList<>();
        adapter = new ClienteAdapter(this, clienteList);
        listView.setAdapter(adapter);

        // Recebe a flag indicando se foi aberto pelo RegistroVenda
        fromRegistroVenda = getIntent().getBooleanExtra("fromRegistroVenda", false);

        listarClientes();

        // Ajuste para seleção de cliente
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Cliente cliente = clienteList.get(position);

            if (fromRegistroVenda) {
                // Retorna o cliente selecionado para RegistroVenda
                Intent returnIntent = new Intent();
                returnIntent.putExtra("clienteId", cliente.getId());
                returnIntent.putExtra("clienteNome", cliente.getNome());
                setResult(RESULT_OK, returnIntent);
                finish();
            } else {
                // Comportamento original
                Toast.makeText(ListaClientes.this, "Cliente selecionado: " + cliente.getNome(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listarClientes();
    }

    private void listarClientes() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference clientesRef = db.collection("Usuarios").document(userId).collection("Clientes");

        clientesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    allClientes.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Cliente cliente = document.toObject(Cliente.class);
                        cliente.setId(document.getId());
                        allClientes.add(cliente);
                    }
                    Collections.sort(allClientes, new Comparator<Cliente>() {
                        @Override
                        public int compare(Cliente c1, Cliente c2) {
                            return c1.getNome().compareToIgnoreCase(c2.getNome());
                        }
                    });
                    searchClientes();
                } else {
                    Toast.makeText(ListaClientes.this, "Erro ao carregar clientes.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void searchClientes() {
        String query = searchField.getText().toString().toLowerCase();
        List<Cliente> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(allClientes);
        } else {
            for (Cliente cliente : allClientes) {
                if (cliente.getNome().toLowerCase().contains(query)) {
                    filteredList.add(cliente);
                }
            }
        }

        clienteList.clear();
        clienteList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }
}
