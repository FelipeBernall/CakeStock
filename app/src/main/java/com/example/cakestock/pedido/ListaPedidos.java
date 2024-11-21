package com.example.cakestock.pedido;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cakestock.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ListaPedidos extends AppCompatActivity {

    private ListView listViewPedidos;
    private ImageButton btnVoltar;
    private FloatingActionButton fabAdicionarPedido;
    private PedidoAdapter pedidoAdapter;
    private List<Pedido> listaPedidos;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_pedidos);

        // Inicializa os componentes do layout
        listViewPedidos = findViewById(R.id.lv_lista_pedidos);
        btnVoltar = findViewById(R.id.btn_voltar);
        fabAdicionarPedido = findViewById(R.id.fab_adicionar_pedido);

        // Inicializa Firebase Firestore e o usuário atual
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Lista para armazenar os pedidos
        listaPedidos = new ArrayList<>();

        // Configura o adapter
        pedidoAdapter = new PedidoAdapter(this, listaPedidos);
        listViewPedidos.setAdapter(pedidoAdapter);

        // Carrega os pedidos do Firebase
        carregarPedidos();

        // Listener para o botão voltar
        btnVoltar.setOnClickListener(v -> finish());

        // Listener para o botão adicionar pedido
        fabAdicionarPedido.setOnClickListener(v -> {
            // Intent para abrir a tela CadastroPedido
            Intent intent = new Intent(ListaPedidos.this, CadastroPedido.class);
            startActivity(intent);
        });
    }

    private void carregarPedidos() {
        if (user != null) {
            db.collection("Usuarios")
                    .document(user.getUid())
                    .collection("Pedidos")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listaPedidos.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Pedido pedido = document.toObject(Pedido.class);
                                listaPedidos.add(pedido);
                            }
                            pedidoAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "Erro ao carregar pedidos.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
        }
    }
}
