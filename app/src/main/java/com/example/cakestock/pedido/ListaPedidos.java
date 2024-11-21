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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        listViewPedidos = findViewById(R.id.lv_lista_pedidos);
        btnVoltar = findViewById(R.id.btn_voltar);
        fabAdicionarPedido = findViewById(R.id.fab_adicionar_pedido);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        listaPedidos = new ArrayList<>();
        pedidoAdapter = new PedidoAdapter(this, listaPedidos);
        listViewPedidos.setAdapter(pedidoAdapter);

        // Verifica se o sinalizador 'atualizar' está presente
        if (getIntent().getBooleanExtra("atualizar", false)) {
            carregarPedidos();  // Recarrega a lista
        }

        carregarPedidos();

        btnVoltar.setOnClickListener(v -> finish());

        fabAdicionarPedido.setOnClickListener(v -> {
            Intent intent = new Intent(ListaPedidos.this, CadastroPedido.class);
            startActivity(intent);
        });

        listViewPedidos.setOnItemClickListener((parent, view, position, id) -> {
            Pedido pedidoSelecionado = listaPedidos.get(position);

            // Cria a intent para abrir a tela CadastroPedido
            Intent intent = new Intent(ListaPedidos.this, CadastroPedido.class);
            // Passa os dados do pedido e o pedidoId
            intent.putExtra("pedidoId", pedidoSelecionado.getPedidoId());
            intent.putExtra("descricao", pedidoSelecionado.getDescricao());
            intent.putExtra("data", pedidoSelecionado.getData());
            intent.putExtra("cliente", pedidoSelecionado.getCliente());

            startActivity(intent);
        });

        listViewPedidos.setOnItemLongClickListener((parent, view, position, id) -> {
            Pedido pedidoSelecionado = listaPedidos.get(position);

            // Cria o AlertDialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Concluir Pedido")
                    .setMessage("Deseja concluir este pedido? Isso resultará na exclusão permanente.")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        // Remove o pedido do Firestore
                        db.collection("Usuarios")
                                .document(user.getUid())
                                .collection("Pedidos")
                                .document(pedidoSelecionado.getPedidoId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Remove o pedido da lista local
                                    listaPedidos.remove(position);
                                    pedidoAdapter.notifyDataSetChanged();
                                    Toast.makeText(this, "Pedido concluído com sucesso.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Erro ao concluir o pedido.", Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("Não", null)  // Cancela a ação
                    .show();

            return true;  // Indica que o clique longo consumiu o evento
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
                            listaPedidos.clear();  // Limpa a lista antes de recarregar
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Pedido pedido = document.toObject(Pedido.class);
                                pedido.setPedidoId(document.getId());  // Definindo o pedidoId
                                listaPedidos.add(pedido);
                            }

                            // Ordena por data e descrição
                            Collections.sort(listaPedidos, (p1, p2) -> {
                                try {
                                    Date data1 = dateFormat.parse(p1.getData());
                                    Date data2 = dateFormat.parse(p2.getData());

                                    if (data1 != null && data2 != null) {
                                        int dateComparison = data1.compareTo(data2);
                                        if (dateComparison != 0) {
                                            return dateComparison;  // Se as datas forem diferentes, retorna a comparação
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                // Se as datas forem iguais, ordena pela descrição alfabeticamente
                                return p1.getDescricao().compareToIgnoreCase(p2.getDescricao());
                            });

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
