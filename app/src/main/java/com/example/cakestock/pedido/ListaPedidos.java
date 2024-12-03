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

    // Declaração das variáveis de interface e banco de dados
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

        // Inicialização dos componentes da interface
        listViewPedidos = findViewById(R.id.lv_lista_pedidos);
        btnVoltar = findViewById(R.id.btn_voltar);
        fabAdicionarPedido = findViewById(R.id.fab_adicionar_pedido);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Inicializa lista e adapter
        listaPedidos = new ArrayList<>();
        pedidoAdapter = new PedidoAdapter(this, listaPedidos);
        listViewPedidos.setAdapter(pedidoAdapter);

        // Verifica se o sinalizador 'atualizar' está presente
        if (getIntent().getBooleanExtra("atualizar", false)) {
            carregarPedidos();  // Recarrega a lista
        }

        // Carrega os pedidos ao abrir a tela
        carregarPedidos();

        // Configura o botão de voltar
        btnVoltar.setOnClickListener(v -> finish());

        // Configura o botão de adicionar novo pedido
        fabAdicionarPedido.setOnClickListener(v -> {
            Intent intent = new Intent(ListaPedidos.this, CadastroPedido.class);
            startActivity(intent);
        });

        // Listener para editar um pedido (clique simples)
        listViewPedidos.setOnItemClickListener((parent, view, position, id) -> {
            Pedido pedidoSelecionado = listaPedidos.get(position);

            // Redireciona para a tela de cadastro com os dados do pedido
            Intent intent = new Intent(ListaPedidos.this, CadastroPedido.class);

            // Passa os dados do pedido e o pedidoId
            intent.putExtra("pedidoId", pedidoSelecionado.getPedidoId());
            intent.putExtra("descricao", pedidoSelecionado.getDescricao());
            intent.putExtra("data", pedidoSelecionado.getData());
            intent.putExtra("cliente", pedidoSelecionado.getCliente());

            startActivity(intent);
        });

        // Clique longo para concluir/remover um pedido
        listViewPedidos.setOnItemLongClickListener((parent, view, position, id) -> {
            Pedido pedidoSelecionado = listaPedidos.get(position);

            // Mostra um alerta para confirmação de exclusão
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

    // Carrega pedidos do Firestore
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

                            // Adiciona cada pedido retornado do banco
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Pedido pedido = document.toObject(Pedido.class);
                                pedido.setPedidoId(document.getId());  // Definindo o pedidoId
                                listaPedidos.add(pedido);
                            }

                            // Ordena a liste por data (prioridade)
                            Collections.sort(listaPedidos, (p1, p2) -> {
                                try {
                                    Date data1 = dateFormat.parse(p1.getData());
                                    Date data2 = dateFormat.parse(p2.getData());

                                    if (data1 != null && data2 != null) {
                                        int dateComparison = data1.compareTo(data2);
                                        if (dateComparison != 0) {
                                            return dateComparison;  // Ordena por data
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                // Se as datas forem iguais, ordena pela descrição alfabeticamente
                                return p1.getDescricao().compareToIgnoreCase(p2.getDescricao());
                            });

                            pedidoAdapter.notifyDataSetChanged(); // Atualiza a lista na interface
                        } else {
                            Toast.makeText(this, "Erro ao carregar pedidos.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
        }
    }

}
