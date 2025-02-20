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

// Tela para registrar ou atualizar vendas, incluindo clientes, produtos e descontos
public class RegistroVenda extends AppCompatActivity {

    // Componentes da interface
    private Spinner spinnerCliente, spinnerProduto;
    private TextView tvData, tvValorTotal;
    private EditText editTextDescricao, editTextData, editTextQuantidade, editTextDesconto;
    private Button btnRegistrarVenda;
    private ImageButton btnAdicionarProduto;
    private ListView listViewProdutos;
    private FirebaseFirestore db;

    // Dados auxiliares
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

        // Inicialização de componentes visuais
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
        ImageButton btnVoltar = findViewById(R.id.btn_voltar);

        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(RegistroVenda.this, ListaTransacoes.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Finaliza a atividade atual
        });

        // Inicializa Firestore e listas
        db = FirebaseFirestore.getInstance();
        clientesList = new ArrayList<>();
        produtosList = new ArrayList<>();
        produtosAdicionados = new ArrayList<>();
        valorTotal = 0.0;

        carregarClientes();  // Carrega clientes ativos no spinner.
        carregarProdutos(); // Carrega produtos disponíveis no spinner

        // Configura ações dos botões
        editTextData.setOnClickListener(v -> showDatePickerDialog());
        btnAdicionarProduto.setOnClickListener(v -> adicionarProduto());
        btnRegistrarVenda.setOnClickListener(v -> {
            Transacao transacao = (Transacao) getIntent().getSerializableExtra("transacao");
            registrarVenda(transacao);
        });

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


        // Adiciona o listener para clique longo
        listViewProdutos.setOnItemLongClickListener((parent, view, position, id) -> {
            // Chama o método para confirmar a remoção
            removerProdutoComConfirmacao(position);
            return true; // Indica que o evento foi tratado
        });

        // Preenche campos caso seja uma edição
        Intent intent = getIntent();
        Transacao transacao = (Transacao) intent.getSerializableExtra("transacao");

        if (transacao != null) {
            editTextDescricao.setText(transacao.getDescricao());
            editTextData.setText(transacao.getData());
            valorTotal = transacao.getValorTotal(); // Valor total
            produtosAdicionados.addAll(transacao.getProdutos()); // Produtos associados

            atualizarValorTotal(); // Atualiza os valores na tela
            // Atualiza a listView de produtos
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, produtosAdicionados);
            listViewProdutos.setAdapter(adapter);
        }
    }

    // Mostra um seletor de data para o campo de data.
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

    // Carrega clientes ativos do banco de dados e os exibe no spinner
    private void carregarClientes() {
        db.collection("Usuarios")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Clientes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> clienteNomes = new ArrayList<>();
                        clienteNomes.add("Selecionar"); // Adiciona a opção de seleção inicial
                        clientesList.clear(); // Limpa a lista antes de adicionar novos clientes

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Cliente cliente = document.toObject(Cliente.class);
                            // Adiciona apenas clientes ativos
                            if (cliente.isAtivo()) {
                                clientesList.add(cliente); // Adiciona o cliente à lista de clientes
                                clienteNomes.add(cliente.getNome()); // Adiciona o nome ao spinner
                            }
                        }

                        // Cria o adaptador e configura o spinner com os nomes dos clientes ativos
                        ArrayAdapter<String> clienteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clienteNomes);
                        clienteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCliente.setAdapter(clienteAdapter);
                    } else {
                        Toast.makeText(this, "Erro ao carregar clientes.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Carrega produtos disponíveis do banco de dados e os exibe no spinner
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
        // Obtém os dados informada pelo usuário.
        String produtoSelecionado = spinnerProduto.getSelectedItem().toString();
        String quantidadeText = editTextQuantidade.getText().toString();

        // Verifica se um produto foi selecionado e se a quantidade foi preenchida
        if (produtoSelecionado.equals("Selecionar") || quantidadeText.isEmpty()) {
            Toast.makeText(this, "Selecione um produto e insira a quantidade.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Converte a quantidade para inteiro
        int quantidade = Integer.parseInt(quantidadeText);

        // Obtém Produto correspondente ao produto selecionado no Spinner
        Produto produto = produtosList.get(spinnerProduto.getSelectedItemPosition() - 1); // Ajusta o índice

        if (quantidade > produto.getQuantidade()) {
            Toast.makeText(this, "Quantidade disponível insuficiente! Estoque: " + produto.getQuantidade(), Toast.LENGTH_SHORT).show();
            return; // Não adiciona o produto se a quantidade for maior que a disponível
        }

        // Verifica se o produto já foi adicionado anteriormente
        if (produtosMap.containsKey(produto.getNome())) {
            Toast.makeText(this, "Este produto já foi adicionado.", Toast.LENGTH_SHORT).show();
            return; // Produto já foi adicionado
        }

        // Adiciona o produto ao mapa e lista
        produtosMap.put(produto.getNome(), quantidade);
        produtosAdicionados.add(produto.getNome() + " (x" + quantidade + ") : R$ " + String.format(Locale.getDefault(), "%.2f", produto.getValor() * quantidade));

        // Atualiza o valor total da venda
        valorTotal += produto.getValor() * quantidade;
        atualizarValorTotal();


        // Atualiza a Lista com os produtos adicionados
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, produtosAdicionados);
        listViewProdutos.setAdapter(adapter);

        // Ajusta a altura da Lista com base no número de itens
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

        // Verifica se há um desconto informado pelo usuário
        if (!editTextDesconto.getText().toString().isEmpty()) {
            desconto = Double.parseDouble(editTextDesconto.getText().toString());
        }

        // Calcula o valor total com desconto, garantindo que não seja negativo
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

    private void registrarVenda(Transacao transacaoExistente) {
        // Obtém os valores dos campos de entrada
        String descricao = editTextDescricao.getText().toString().trim();
        String data = editTextData.getText().toString().trim();
        int clienteIndex = spinnerCliente.getSelectedItemPosition();

        // Verifica se os campos obrigatórios foram preenchidos
        if (descricao.isEmpty() || data.isEmpty() || clienteIndex <= 0 || produtosAdicionados.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtém o ID do cliente selecionado e cria o mapa com os dados da venda
        String clienteId = clientesList.get(clienteIndex - 1).getId();
        Map<String, Object> dadosVenda = new HashMap<>();
        dadosVenda.put("descricao", descricao);
        dadosVenda.put("data", data);
        dadosVenda.put("clienteId", clienteId);
        dadosVenda.put("produtos", produtosAdicionados);
        dadosVenda.put("valorTotal", valorTotal);

        // Obtém o ID do usuário para salvar os dados na coleção correta
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (transacaoExistente == null) {
            // Caso seja uma nova venda, adiciona os dados no Firestore
            db.collection("Usuarios")
                    .document(userId)
                    .collection("Vendas")
                    .add(dadosVenda)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            atualizarEstoque(() -> {
                                // Após atualizar o estoque, redireciona
                                Toast.makeText(this, "Venda registrada com sucesso!", Toast.LENGTH_SHORT).show();
                                navegarParaListaTransacoes();
                            });
                        } else {
                            Toast.makeText(this, "Erro ao registrar venda.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Atualização de venda existente
            db.collection("Usuarios")
                    .document(userId)
                    .collection("Vendas")
                    .whereEqualTo("descricao", transacaoExistente.getDescricao())
                    .whereEqualTo("data", transacaoExistente.getData())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {

                            // Obtém o ID do documento da venda existente.
                            String docId = task.getResult().getDocuments().get(0).getId();
                            db.collection("Usuarios")
                                    .document(userId)
                                    .collection("Vendas")
                                    .document(docId)
                                    .update(dadosVenda)
                                    .addOnSuccessListener(aVoid -> {
                                        atualizarEstoque(() -> {
                                            // Após atualizar o estoque, redireciona para a lista de transações
                                            Toast.makeText(this, "Venda atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                                            navegarParaListaTransacoes();
                                        });
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Erro ao atualizar venda: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "Erro ao localizar venda para atualizar.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Navegação para a tela de transações
    private void navegarParaListaTransacoes() {
        Intent intent = new Intent(RegistroVenda.this, ListaTransacoes.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Finaliza a atividade atual para evitar que ela permaneça na pilha
    }


    // Atualização de estoque com callback
    private void atualizarEstoque(Runnable callback) {
        for (Map.Entry<String, Integer> entry : produtosMap.entrySet()) {
            String nomeProduto = entry.getKey();
            int quantidadeVendida = entry.getValue();

            db.collection("Usuarios")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("Produtos")
                    .whereEqualTo("nome", nomeProduto)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String produtoId = document.getId();
                                int quantidadeAtual = document.getLong("quantidade").intValue();
                                int novaQuantidade = quantidadeAtual - quantidadeVendida;

                                db.collection("Usuarios")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("Produtos")
                                        .document(produtoId)
                                        .update("quantidade", novaQuantidade)
                                        .addOnCompleteListener(updateTask -> {
                                            if (callback != null && updateTask.isSuccessful()) {
                                                callback.run();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Erro ao localizar o produto no estoque.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Remover produto da lista
    private void removerProduto(int position) {
        // Verifica se a posição é válida
        if (position >= 0 && position < produtosAdicionados.size()) {
            // Obtém o produto a ser removido
            String produtoRemovido = produtosAdicionados.get(position);

            // Extrai o nome do produto (antes do " (x")
            String nomeProduto = produtoRemovido.split(" \\(x")[0];

            // Remove o produto do mapa
            if (produtosMap.containsKey(nomeProduto)) {
                produtosMap.remove(nomeProduto);
            }

            // Extrai o valor do produto (assumindo que o valor está no formato "R$ X,XX" no final)
            String[] partes = produtoRemovido.split(": R\\$ ");
            if (partes.length > 1) {
                try {
                    double valorProduto = Double.parseDouble(partes[1].replace(",", "."));
                    valorTotal -= valorProduto; // Atualiza o valor total
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Erro ao calcular o valor do produto removido.", Toast.LENGTH_SHORT).show();
                }
            }

            // Remove o produto da lista
            produtosAdicionados.remove(position);

            // Atualiza a ListView com a nova lista
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) listViewProdutos.getAdapter();
            adapter.notifyDataSetChanged(); // Notifica o adapter para atualizar a exibição

            // Atualiza o valor total no TextView
            atualizarValorTotal();

            // Exibe uma mensagem de sucesso
            Toast.makeText(this, "Produto removido.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Erro ao remover produto.", Toast.LENGTH_SHORT).show();
        }
    }

    // Cria o AlertDialog para confirmação de exclusão
    private void removerProdutoComConfirmacao(int position) {
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
