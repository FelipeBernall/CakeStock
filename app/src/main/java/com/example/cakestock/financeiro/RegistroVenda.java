package com.example.cakestock.financeiro;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.example.cakestock.cliente.Cliente;
import com.example.cakestock.produto.Produto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.view.View;

public class RegistroVenda extends AppCompatActivity {
    private Spinner spinnerCliente, spinnerProduto;
    private TextView tvData, tvValorTotal;
    private EditText editTextDescricao, editTextData, editTextQuantidade, editTextDesconto;
    private Button btnRegistrarVenda;
    private ImageButton btnAdicionarProduto;
    private ListView listViewProdutos;
    private FirebaseFirestore db;

    private List<Cliente> clientesList;
    private List<Produto> produtosList;
    private List<String> produtosAdicionados;
    private double valorTotal;
    private TextView tvValorComDesconto;
    private LinearLayout layoutValorComDesconto;
    private ImageButton btnAdicionarDesconto;
    private TextView tvDesconto, tvDescontoLabel;
    private LinearLayout layoutDesconto;
    private Map<String, Integer> produtosMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_venda);

        spinnerCliente = findViewById(R.id.spinner_cliente);
        spinnerProduto = findViewById(R.id.spinner_produto);
        editTextDescricao = findViewById(R.id.et_descricao);
        editTextData = findViewById(R.id.et_data);
        editTextQuantidade = findViewById(R.id.et_quantidade);
        editTextDesconto = findViewById(R.id.et_desconto);
        tvData = findViewById(R.id.tv_data);
        tvValorTotal = findViewById(R.id.tv_valor_total);
        btnAdicionarProduto = findViewById(R.id.btn_adicionar_produto);
        listViewProdutos = findViewById(R.id.lv_produtos_disponiveis);
        btnRegistrarVenda = findViewById(R.id.btn_salvar);

        tvValorComDesconto = findViewById(R.id.tv_valor_com_desconto);
        layoutValorComDesconto = findViewById(R.id.layout_valor_com_desconto);
        ImageButton btnAdicionarDesconto = findViewById(R.id.btn_adicionar_desconto);
        ImageButton btnRemoverDesconto = findViewById(R.id.btn_remover_desconto);
        tvDesconto = findViewById(R.id.tv_desconto);
        tvDescontoLabel = findViewById(R.id.tv_desconto_label);
        layoutDesconto = findViewById(R.id.layout_desconto);



        db = FirebaseFirestore.getInstance();
        clientesList = new ArrayList<>();
        produtosList = new ArrayList<>();
        produtosAdicionados = new ArrayList<>();
        valorTotal = 0.0;

        carregarClientes();
        carregarProdutos();

        editTextData.setOnClickListener(v -> showDatePickerDialog());
        btnAdicionarProduto.setOnClickListener(v -> adicionarProduto());
        btnRegistrarVenda.setOnClickListener(v -> registrarVenda());

        // Configura o botão de adicionar desconto
        btnAdicionarDesconto.setOnClickListener(v -> {
            String descontoTexto = editTextDesconto.getText().toString();
            if (!descontoTexto.isEmpty()) {
                double desconto = Double.parseDouble(descontoTexto);
                if (desconto <= valorTotal) {
                    atualizarValorTotal(); // Atualiza valores
                    editTextDesconto.setText(""); // Limpa o campo
                    Toast.makeText(this, "Desconto aplicado.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "O desconto não pode ser maior que o valor total.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Insira um valor de desconto.", Toast.LENGTH_SHORT).show();
            }
        });


        // Lógica para remover o desconto
        btnRemoverDesconto.setOnClickListener(v -> {
            editTextDesconto.setText(""); // Limpa o campo de desconto
            layoutDesconto.setVisibility(View.GONE); // Oculta o layout de desconto
            layoutValorComDesconto.setVisibility(View.GONE); // Oculta o layout de valor com desconto
            atualizarValorTotal(); // Atualiza os valores
            Toast.makeText(this, "Desconto removido.", Toast.LENGTH_SHORT).show();
        });

        // Inicializa a ListView
        listViewProdutos = findViewById(R.id.lv_produtos_disponiveis);

        // Configura o Adapter para a ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, produtosAdicionados);
        listViewProdutos.setAdapter(adapter);

        // Adiciona o listener para clique longo
        listViewProdutos.setOnItemLongClickListener((parent, view, position, id) -> {
            // Chama o método para confirmar a remoção
            removerProdutoComConfirmacao(position);
            return true; // Indica que o evento foi tratado
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RegistroVenda.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    editTextData.setText(formattedDate);
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void carregarClientes() {
        db.collection("Usuarios")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Clientes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> clienteNomes = new ArrayList<>();
                        clienteNomes.add("Selecionar");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Cliente cliente = document.toObject(Cliente.class);
                            clientesList.add(cliente);
                            clienteNomes.add(cliente.getNome());
                        }
                        ArrayAdapter<String> clienteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clienteNomes);
                        clienteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCliente.setAdapter(clienteAdapter);
                    } else {
                        Toast.makeText(this, "Erro ao carregar clientes.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void carregarProdutos() {
        db.collection("Usuarios")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Produtos")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> produtoNomes = new ArrayList<>();
                        produtoNomes.add("Selecionar");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Produto produto = document.toObject(Produto.class);
                            produtosList.add(produto);
                            produtoNomes.add(produto.getNome());
                        }
                        ArrayAdapter<String> produtoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, produtoNomes);
                        produtoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerProduto.setAdapter(produtoAdapter);
                    } else {
                        Toast.makeText(this, "Erro ao carregar produtos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void adicionarProduto() {
        String produtoSelecionado = spinnerProduto.getSelectedItem().toString();
        String quantidadeText = editTextQuantidade.getText().toString();

        if (produtoSelecionado.equals("Selecionar") || quantidadeText.isEmpty()) {
            Toast.makeText(this, "Selecione um produto e insira a quantidade.", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantidade = Integer.parseInt(quantidadeText);
        Produto produto = produtosList.get(spinnerProduto.getSelectedItemPosition() - 1); // Ajusta o índice

        if (quantidade > produto.getQuantidade()) {
            Toast.makeText(this, "Quantidade disponível insuficiente! Estoque: " + produto.getQuantidade(), Toast.LENGTH_SHORT).show();
            return; // Não adiciona o produto se a quantidade for maior que a disponível
        }

        // Verifica se o produto já foi adicionado
        if (produtosMap.containsKey(produto.getNome())) {
            Toast.makeText(this, "Este produto já foi adicionado.", Toast.LENGTH_SHORT).show();
            return; // Produto já foi adicionado
        }

        // Adiciona o produto ao mapa e lista
        produtosMap.put(produto.getNome(), quantidade);
        produtosAdicionados.add(produto.getNome() + " (x" + quantidade + ") : R$ " + String.format(Locale.getDefault(), "%.2f", produto.getValor() * quantidade));
        valorTotal += produto.getValor() * quantidade;

        atualizarValorTotal();

        // Atualiza a ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, produtosAdicionados);
        listViewProdutos.setAdapter(adapter);

        // Ajusta a altura da ListView com base no número de itens
        ajustarAlturaListView();

        // Limpa os campos de input
        spinnerProduto.setSelection(0);
        editTextQuantidade.setText("");
    }


    private void ajustarAlturaListView() {
        int numItems = produtosAdicionados.size();
        int maxItems = 3;

        // Calcula a altura dependendo do número de itens
        int height = (numItems <= maxItems) ? numItems * getItemHeight() : maxItems * getItemHeight();
        listViewProdutos.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height
        ));
        listViewProdutos.requestLayout();
    }

    private int getItemHeight() {
        // Criamos uma nova instância do item de lista para medir sua altura
        View itemView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        itemView.measure(0, 0); // Mede o item
        return itemView.getMeasuredHeight();
    }



    private void atualizarValorTotal() {
        double desconto = 0.0;
        if (!editTextDesconto.getText().toString().isEmpty()) {
            desconto = Double.parseDouble(editTextDesconto.getText().toString());
        }

        double valorComDesconto = valorTotal - desconto;
        if (valorComDesconto < 0) valorComDesconto = 0;

        // Atualiza o valor total
        tvValorTotal.setText(String.format(Locale.getDefault(), "R$ %.2f", valorTotal));

        // Atualiza o campo de desconto
        tvDesconto.setText(String.format(Locale.getDefault(), "- R$ %.2f", desconto));

        // Atualiza o campo de valor com desconto
        tvValorComDesconto.setText(String.format(Locale.getDefault(), "R$ %.2f", valorComDesconto));

        // Controla a visibilidade dos layouts
        if (desconto > 0) {
            layoutDesconto.setVisibility(View.VISIBLE); // Torna visível o desconto
            layoutValorComDesconto.setVisibility(View.VISIBLE); // Torna visível o valor com desconto
        } else {
            layoutDesconto.setVisibility(View.GONE); // Oculta o desconto
            layoutValorComDesconto.setVisibility(View.GONE); // Oculta o valor com desconto
        }
    }

    private void registrarVenda() {
        String descricao = editTextDescricao.getText().toString();
        String data = editTextData.getText().toString();
        String clienteId = clientesList.get(spinnerCliente.getSelectedItemPosition() - 1).getId();

        if (descricao.isEmpty() || data.isEmpty() || produtosAdicionados.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cria a Transacao
        Transacao transacao = new Transacao(descricao, data, clienteId, produtosAdicionados, valorTotal);

        // Enviar dados para o Firestore
        db.collection("Usuarios")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Vendas")
                .add(transacao)  // Adiciona a transação
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Se a transação for registrada com sucesso
                        Toast.makeText(this, "Venda registrada com sucesso!", Toast.LENGTH_SHORT).show();

                        // Redireciona para a tela de transações
                        Intent intent = new Intent(RegistroVenda.this, ListaTransacoes.class);
                        startActivity(intent);

                        // Finaliza a atividade atual
                        finish();
                    } else {
                        // Se ocorrer um erro durante a gravação
                        Toast.makeText(this, "Erro ao registrar venda.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Caso ocorra uma falha ao salvar no Firestore
                    Toast.makeText(this, "Erro ao salvar venda: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void removerProduto(int position) {
        // Verifica se a posição é válida
        if (position >= 0 && position < produtosAdicionados.size()) {
            // Remove o produto da lista
            produtosAdicionados.remove(position);

            // Atualiza a ListView com a nova lista
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) listViewProdutos.getAdapter();
            adapter.notifyDataSetChanged(); // Notifica o adapter para atualizar a exibição

            // Recalcula o valor total
            atualizarValorTotal();

            // Exibe uma mensagem de sucesso
            Toast.makeText(this, "Produto removido.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Erro ao remover produto.", Toast.LENGTH_SHORT).show();
        }
    }


    private void removerProdutoComConfirmacao(int position) {
        // Cria o AlertDialog para confirmação
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Você tem certeza que deseja remover este produto?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    // Se o usuário confirmar, remove o produto
                    removerProduto(position);
                })
                .setNegativeButton("Não", null) // Se o usuário cancelar, não faz nada
                .show(); // Exibe o AlertDialog
    }




    private void atualizarTransacoes() {
        // Atualize sua `ListView` na tela de transações conforme necessário
        // Aqui você pode enviar um broadcast ou usar outro método para atualizar a lista
    }

    private void aplicarDesconto() {
        String descontoText = editTextDesconto.getText().toString();

        if (descontoText.isEmpty()) {
            Toast.makeText(this, "Insira um valor de desconto.", Toast.LENGTH_SHORT).show();
            return;
        }

        double desconto = Double.parseDouble(descontoText);
        if (desconto > valorTotal) {
            Toast.makeText(this, "O desconto não pode ser maior que o valor total.", Toast.LENGTH_SHORT).show();
            return;
        }

        double valorComDesconto = valorTotal - desconto;
        tvValorComDesconto.setText(String.format(Locale.getDefault(), "R$ %.2f", valorComDesconto));

        // Exibe o layout do valor com desconto
        layoutValorComDesconto.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Desconto aplicado.", Toast.LENGTH_SHORT).show();
    }

}
