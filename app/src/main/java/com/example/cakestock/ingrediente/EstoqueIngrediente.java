package com.example.cakestock.ingrediente;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cakestock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// Exibe e gerencia o estoque de ingredientes
public class EstoqueIngrediente extends AppCompatActivity {

    // Campos de entrada de dados
    private RecyclerView recyclerViewIngredientes;
    private IngredienteAdapter adapter;
    private List<Ingrediente> ingredientes;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private ImageButton fabAdicionarIngrediente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estoque_ingrediente);

        // Inicializa os componentes da tela
        recyclerViewIngredientes = findViewById(R.id.recyclerViewIngredientes);
        fabAdicionarIngrediente = findViewById(R.id.fabAdicionarIngrediente);
        ImageButton btnVoltar = findViewById(R.id.btn_voltar);

        // Ação para o botão de voltar
        btnVoltar.setOnClickListener(v -> onBackPressed());

        // Conexão com Firestore e obtenção do usuário logado
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Inicializa a lista de ingredientes e o adaptador para exibição
        ingredientes = new ArrayList<>();
        adapter = new IngredienteAdapter(this, ingredientes, new IngredienteAdapter.OnIngredienteClickListener() {
            @Override
            public void onEditClick(Ingrediente ingrediente) {
                Intent intent = new Intent(EstoqueIngrediente.this, CadastroIngrediente.class);
                intent.putExtra("ingrediente_id", ingrediente.getId());
                startActivityForResult(intent, 1);
            }

            @Override
            public void onDeleteClick(Ingrediente ingrediente) {
                // Exibe uma confirmação antes de excluir o ingrediente
                new AlertDialog.Builder(EstoqueIngrediente.this)
                        .setTitle("Excluir Ingrediente")
                        .setMessage("Tem certeza de que deseja excluir o ingrediente \"" + ingrediente.getNome() + "\"?")
                        .setPositiveButton("Sim", (dialog, which) -> excluirIngrediente(ingrediente))
                        .setNegativeButton("Não", null)
                        .show();
            }

            @Override
            public void onAddQuantityClick(Ingrediente ingrediente) {
                // Ação ao adicionar quantidade ao ingrediente
                abrirDialogAdicionarQuantidade(ingrediente);
            }
        });

        // Configura a Lista para exibir os ingredientes
        recyclerViewIngredientes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewIngredientes.setAdapter(adapter);

        // Ação para o FAB -> ir para o form Cadastro de ingrediente
        fabAdicionarIngrediente.setOnClickListener(v -> {
            Intent intent = new Intent(EstoqueIngrediente.this, CadastroIngrediente.class);
            startActivityForResult(intent, 1);
        });

        // Carrega os ingredientes do banco de dados
        carregarIngredientes();
    }

    // Método que é chamado quando a atividade retorna da tela de cadastro/edição de ingredientes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Toast.makeText(this, "Ingrediente adicionado com sucesso!", Toast.LENGTH_SHORT).show();
            carregarIngredientes(); // Recarrega a lista de ingredientes
        }
    }

    // Carrega os ingredientes do Firestore e os exibe na tela
    private void carregarIngredientes() {
        CollectionReference ingredientesRef = db.collection("Usuarios").document(user.getUid()).collection("Ingredientes");

        ingredientesRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ingredientes.clear();

                        // Preenche a lista com os ingredientes recuperados do Firestore
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Ingrediente ingrediente = document.toObject(Ingrediente.class);
                            ingrediente.setId(document.getId());
                            ingredientes.add(ingrediente);
                        }
                        adapter.notifyDataSetChanged();  // Atualiza a interface com os novos dados
                    } else {
                        Toast.makeText(EstoqueIngrediente.this, "Erro ao carregar ingredientes.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Exclui um ingrediente do banco de dados -> SE NÃO ESTIVER EM USO
    private void excluirIngrediente(Ingrediente ingrediente) {
        if (ingrediente.isEmUso()) {
            // Não pode excluir se o ingrediente estiver em uso
            Toast.makeText(this, "Ingrediente não pode ser excluído: está em uso", Toast.LENGTH_SHORT).show();
        } else {
            // Exclui o ingrediente do Firestore
            db.collection("Usuarios").document(user.getUid()).collection("Ingredientes").document(ingrediente.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Ingrediente excluído com sucesso.", Toast.LENGTH_SHORT).show();
                        carregarIngredientes(); // Recarrega a lista após exclusão
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Erro ao excluir ingrediente.", Toast.LENGTH_SHORT).show());
        }
    }

    // Abre um DIALOG para adicionar quantidade e valor unitário a um ingrediente JÁ CADASTRADO
    private void abrirDialogAdicionarQuantidade(Ingrediente ingrediente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Quantidade e Valor Unitário");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_quantidade, null);
        builder.setView(dialogView);

        // Campos para inserir a quantidade adicional e o valor unitário
        EditText editQuantidade = dialogView.findViewById(R.id.editQuantidade);
        EditText editUnidadeMedida = dialogView.findViewById(R.id.editUnidadeMedida); // Para GRAMAS ou ML
        EditText editValorUnitario = dialogView.findViewById(R.id.editValorUnitario);

        // Exibe ou oculta o campo de unidade de medida conforme o tipo de medida
        if (ingrediente.getTipoMedida().equals("Gramas") || ingrediente.getTipoMedida().equals("Mililitros")) {
            editUnidadeMedida.setVisibility(View.VISIBLE);
        } else {
            editUnidadeMedida.setVisibility(View.GONE);
        }

        // Ação ao clicar no botão "Adicionar
        builder.setPositiveButton("Adicionar", (dialog, which) -> {
            try {
                // Lê os valores inseridos pelo usuário
                double quantidadeAdicional = Double.parseDouble(editQuantidade.getText().toString().trim());
                double valorUnitarioAdicional = Double.parseDouble(editValorUnitario.getText().toString().trim()); // Captura do valor unitário

                double novaQuantidade;
                double novoValorUnitario;

                // Cálculo do valor total atual e o novo valor unitário
                double valorTotalAtual = ingrediente.getQuantidade() * ingrediente.getValorUnitario();
                double valorTotalAdicional = quantidadeAdicional * valorUnitarioAdicional;

                // Lógica para calcular a nova quantidade dependendo do tipo de medida
                if (ingrediente.getTipoMedida().equals("Gramas")) {
                    double unidadeMedida = ingrediente.getUnidadeMedida();
                    if (editUnidadeMedida.getVisibility() == View.VISIBLE) {
                        unidadeMedida = Double.parseDouble(editUnidadeMedida.getText().toString().trim());
                    }
                    novaQuantidade = ingrediente.getQuantidade() + (quantidadeAdicional * unidadeMedida) / 500; // Cálculo para gramas
                } else if (ingrediente.getTipoMedida().equals("Mililitros")) {
                    double unidadeMedida = ingrediente.getUnidadeMedida();
                    if (editUnidadeMedida.getVisibility() == View.VISIBLE) {
                        unidadeMedida = Double.parseDouble(editUnidadeMedida.getText().toString().trim());
                    }
                    novaQuantidade = ingrediente.getQuantidade() + (quantidadeAdicional * unidadeMedida) / 1000; // Cálculo para mililitros
                } else if (ingrediente.getTipoMedida().equals("Unidades")) {
                    novaQuantidade = ingrediente.getQuantidade() + quantidadeAdicional; // Simples soma para unidades
                } else {
                    Toast.makeText(this, "Tipo de medida desconhecido.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Cálculo do novo valor unitário
                double valorTotalNovo = valorTotalAtual + valorTotalAdicional;
                novoValorUnitario = valorTotalNovo / novaQuantidade;

                // Formata os valores para 2 casas decimais
                novaQuantidade = Math.round(novaQuantidade * 100.0) / 100.0;
                novoValorUnitario = Math.round(novoValorUnitario * 100.0) / 100.0;

                // Atualiza no banco de dados
                db.collection("Usuarios").document(user.getUid())
                        .collection("Ingredientes").document(ingrediente.getId())
                        .update("quantidade", novaQuantidade, "valorUnitario", novoValorUnitario) // Atualiza quantidade e valor unitário
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Quantidade e valor unitário atualizados com sucesso!", Toast.LENGTH_SHORT).show();
                            carregarIngredientes();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Erro ao atualizar quantidade e valor unitário.", Toast.LENGTH_SHORT).show();
                        });

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Por favor, insira valores válidos.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }


    // Subtrai a quantidade dos ingredientes usados do estoque
    public void subtrairIngredientes(ArrayList<Ingrediente> ingredientesUsados) {
        for (Ingrediente usado : ingredientesUsados) {
            db.collection("Usuarios").document(user.getUid()).collection("Ingredientes")
                    .whereEqualTo("nome", usado.getNome())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Ingrediente estoqueIngrediente = doc.toObject(Ingrediente.class);
                            double novaQuantidade = estoqueIngrediente.getQuantidade() - usado.getQuantidade();

                            if (novaQuantidade < 0) {
                                Toast.makeText(this, "Estoque insuficiente para o ingrediente: " + usado.getNome(), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            db.collection("Usuarios").document(user.getUid()).collection("Ingredientes")
                                    .document(doc.getId())
                                    .update("quantidade", novaQuantidade)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Estoque atualizado para o ingrediente: " + usado.getNome(), Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Erro ao atualizar o estoque do ingrediente: " + usado.getNome(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao acessar o estoque do ingrediente: " + usado.getNome(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
