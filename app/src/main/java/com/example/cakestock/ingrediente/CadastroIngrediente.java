package com.example.cakestock.ingrediente;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CadastroIngrediente extends AppCompatActivity {

    private EditText editNomeIngrediente, editQuantidadeIngrediente, editUnidadeMedida, editValorUnitario;
    private Spinner spinnerTipoMedida;
    private TextView textValorTotal;
    private Button btnSalvarIngrediente, btnCancelarCadastro;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String ingredienteId; // ID do ingrediente para edição

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_ingrediente);

        // Inicializa os componentes do layout
        editNomeIngrediente = findViewById(R.id.editNomeIngrediente);
        editQuantidadeIngrediente = findViewById(R.id.editQuantidadeIngrediente);
        editUnidadeMedida = findViewById(R.id.editUnidadeMedida);
        editValorUnitario = findViewById(R.id.editValorUnitario);
        textValorTotal = findViewById(R.id.textValorTotal);
        btnSalvarIngrediente = findViewById(R.id.btnSalvarIngrediente);
        btnCancelarCadastro = findViewById(R.id.btnCancelarCadastro);
        spinnerTipoMedida = findViewById(R.id.spinnerTipoMedida);

        // Configura o adapter para o Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tipo_medida_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoMedida.setAdapter(adapter);

        // Inicializa o Firebase Firestore e o usuário atual
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Obtém o ID do ingrediente da Intent, se disponível
        ingredienteId = getIntent().getStringExtra("ingrediente_id");

        // Se um ID de ingrediente estiver disponível, carrega os dados
        if (ingredienteId != null) {
            carregarIngrediente(ingredienteId);
        }

        // Listener para o Spinner
        spinnerTipoMedida.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equals("Gramas") || selected.equals("Mililitros")) {
                    editUnidadeMedida.setVisibility(View.VISIBLE);
                } else {
                    editUnidadeMedida.setVisibility(View.GONE);
                }
                calcularValorTotal();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editUnidadeMedida.setVisibility(View.GONE);
            }
        });

        // Listeners para os campos de texto
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularValorTotal();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        editValorUnitario.addTextChangedListener(textWatcher);
        editQuantidadeIngrediente.addTextChangedListener(textWatcher);

        // Listener para o botão de salvar
        btnSalvarIngrediente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    salvarIngrediente();
                }
            }
        });

        // Listener para o botão de cancelar
        btnCancelarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Volta para a página anterior
            }
        });
    }

    private void carregarIngrediente(String id) {
        DocumentReference ingredienteRef = db.collection("Usuarios").document(user.getUid()).collection("Ingredientes").document(id);

        ingredienteRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Ingrediente ingrediente = task.getResult().toObject(Ingrediente.class);
                        if (ingrediente != null) {
                            // Preenche os campos do formulário com os dados do ingrediente
                            editNomeIngrediente.setText(ingrediente.getNome());
                            editQuantidadeIngrediente.setText(String.valueOf(ingrediente.getQuantidade()));
                            spinnerTipoMedida.setSelection(((ArrayAdapter) spinnerTipoMedida.getAdapter()).getPosition(ingrediente.getTipoMedida()));
                            if (ingrediente.getTipoMedida().equals("Gramas") || ingrediente.getTipoMedida().equals("Mililitros")) {
                                editUnidadeMedida.setVisibility(View.VISIBLE);
                                editUnidadeMedida.setText(String.valueOf(ingrediente.getUnidadeMedida()));
                            } else {
                                editUnidadeMedida.setVisibility(View.GONE);
                            }
                            editValorUnitario.setText(String.valueOf(ingrediente.getValorUnitario()));
                            calcularValorTotal();
                        }
                    } else {
                        Toast.makeText(CadastroIngrediente.this, "Erro ao carregar dados do ingrediente.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void calcularValorTotal() {
        String strQuantidade = editQuantidadeIngrediente.getText().toString().replace(',', '.');
        String strValorUnitario = editValorUnitario.getText().toString().replace(',', '.');

        if (!strQuantidade.isEmpty() && !strValorUnitario.isEmpty()) {
            double quantidade = Double.parseDouble(strQuantidade);
            double valorUnitario = Double.parseDouble(strValorUnitario);
            double valorTotal = quantidade * valorUnitario;
            textValorTotal.setText(String.format("Valor Total: R$ %.2f", valorTotal));
        } else {
            textValorTotal.setText("Valor Total: R$ 0,00");
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        if (editNomeIngrediente.getText().toString().trim().isEmpty()) {
            editNomeIngrediente.setError("Nome do ingrediente é obrigatório.");
            valid = false;
        }

        if (editQuantidadeIngrediente.getText().toString().trim().isEmpty()) {
            editQuantidadeIngrediente.setError("Quantidade é obrigatória.");
            valid = false;
        }

        if (spinnerTipoMedida.getSelectedItem().toString().equals("Gramas") || spinnerTipoMedida.getSelectedItem().toString().equals("Mililitros")) {
            if (editUnidadeMedida.getText().toString().trim().isEmpty()) {
                editUnidadeMedida.setError("Unidade de medida é obrigatória.");
                valid = false;
            }
        }

        if (editValorUnitario.getText().toString().trim().isEmpty()) {
            editValorUnitario.setError("Valor unitário é obrigatório.");
            valid = false;
        }

        return valid;
    }

    private void salvarIngrediente() {
        String nome = editNomeIngrediente.getText().toString().trim();
        double quantidade = Double.parseDouble(editQuantidadeIngrediente.getText().toString().trim().replace(',', '.'));
        String tipoMedida = spinnerTipoMedida.getSelectedItem().toString();
        double unidadeMedida = 0;
        if (editUnidadeMedida.getVisibility() == View.VISIBLE) {
            unidadeMedida = Double.parseDouble(editUnidadeMedida.getText().toString().trim().replace(',', '.'));
        }
        double valorUnitario = Double.parseDouble(editValorUnitario.getText().toString().trim().replace(',', '.'));
        double valorTotal = quantidade * valorUnitario;

        Ingrediente ingrediente = new Ingrediente(ingredienteId, nome, quantidade, tipoMedida, unidadeMedida, valorUnitario, valorTotal, false);

        CollectionReference ingredientesRef = db.collection("Usuarios").document(user.getUid()).collection("Ingredientes");

        if (ingredienteId != null) {
            // Atualiza o ingrediente existente
            ingredientesRef.document(ingredienteId).set(ingrediente)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CadastroIngrediente.this, "Ingrediente atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("ingrediente_id", ingredienteId);
                        setResult(RESULT_OK, resultIntent);
                        finish(); // Volta para a página anterior
                    })
                    .addOnFailureListener(e -> Toast.makeText(CadastroIngrediente.this, "Erro ao atualizar ingrediente: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Adiciona um novo ingrediente
            ingredientesRef.add(ingrediente)
                    .addOnSuccessListener(documentReference -> {
                        // Atualiza o ingrediente com o ID correto
                        ingrediente.setId(documentReference.getId());
                        documentReference.set(ingrediente)
                                .addOnSuccessListener(aVoid -> {
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("ingrediente_id", ingrediente.getId());
                                    setResult(RESULT_OK, resultIntent);
                                    finish(); // Retorna à tela anterior
                                })
                                .addOnFailureListener(e -> Toast.makeText(CadastroIngrediente.this, "Erro ao salvar ingrediente: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(CadastroIngrediente.this, "Erro ao salvar ingrediente: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }


}