package com.example.cakestock.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
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

// Exibe e gerencia a lista de clientes -> Visualizar , ADD , Editar , Desativar
public class ListaClientes extends AppCompatActivity {

    // Elementos da interface
    private FirebaseFirestore db;
    private ListView listView;
    private ClienteAdapter adapter;
    private List<Cliente> clienteList;
    private List<Cliente> allClientes;
    private EditText searchField;

    // Indica se esta tela foi acessada para selecionar um cliente em um pedido
    private boolean fromRegistroVenda = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_clientes);

        // Inicializa os componentes de busca e adiciona o listener para filtrar clientes
        searchField = findViewById(R.id.searchField);

        searchField.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchClientes();  // Filtra os clientes ao digitar
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // FAB para adicionar um novo cliente
        FloatingActionButton fabAdicionarCliente = findViewById(R.id.fab_adicionar_cliente);
        fabAdicionarCliente.setOnClickListener(v -> startActivity(new Intent(ListaClientes.this, CadastroCliente.class)));

        // Inicializa o Firestore e as listas
        db = FirebaseFirestore.getInstance();
        listView = findViewById(R.id.lv_lista_clientes);
        clienteList = new ArrayList<>();
        allClientes = new ArrayList<>();
        adapter = new ClienteAdapter(this, clienteList);
        listView.setAdapter(adapter);


        // Verifica se a tela foi aberta para selecionar um cliente em vendas
        fromRegistroVenda = getIntent().getBooleanExtra("fromRegistroVenda", false);

        // Carrega a lista de clientes
        listarClientes();

        // Configura o clique nos itens da lista
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Cliente cliente = clienteList.get(position);
            if (fromRegistroVenda) {
                // Retorna o cliente selecionado para a tela anterior (venda)
                Intent returnIntent = new Intent();
                returnIntent.putExtra("clienteId", cliente.getId());
                returnIntent.putExtra("clienteNome", cliente.getNome());
                setResult(RESULT_OK, returnIntent);
                finish();
            } else {
                // Exibe o nome do cliente selecionado
                Toast.makeText(ListaClientes.this, "Cliente selecionado: " + cliente.getNome(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listarClientes(); // Recarrega os clientes ao retornar para a tela
    }

    // Busca os clientes no Firebase e organiza por ordem alfabética
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
                        allClientes.add(cliente); // Adiciona o cliente à lista geral
                    }
                    // Ordena os clientes por nome em ordem alfabética
                    Collections.sort(allClientes, new Comparator<Cliente>() {
                        @Override
                        public int compare(Cliente c1, Cliente c2) {
                            return c1.getNome().compareToIgnoreCase(c2.getNome());
                        }
                    });
                    searchClientes(); // Atualiza a lista exibida
                } else {
                    Toast.makeText(ListaClientes.this, "Erro ao carregar clientes.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Filtra os clientes com base no texto digitado no campo de busca.
    private void searchClientes() {
        String query = searchField.getText().toString().toLowerCase();
        List<Cliente> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            // Se o campo de busca estiver vazio, exibe todos os clientes
            filteredList.addAll(allClientes);
        } else {
            // Filtra os clientes pelo nome
            for (Cliente cliente : allClientes) {
                if (cliente.getNome().toLowerCase().contains(query)) {
                    filteredList.add(cliente);
                }
            }
        }

        // Atualiza a lista exibida
        clienteList.clear();
        clienteList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }
}
