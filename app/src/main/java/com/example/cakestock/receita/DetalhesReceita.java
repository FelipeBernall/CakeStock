package com.example.cakestock.receita;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.example.cakestock.usuario.FormLogin;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

// Exibir os detalhes de uma receita ou Editar
public class DetalhesReceita extends AppCompatActivity {

    // Componentes da interface
    private TextView tvNomeReceitaDetalhe;
    private TextView tvTempoPreparo;
    private TextView tvRendimento;
    private ListView lvIngredientesUtilizados;
    private FloatingActionButton fabEditarReceita;
    private FloatingActionButton fabModoPreparo;

    // Inicializa o Firestore
    private FirebaseFirestore db;

    private String userId;
    private String idReceita;
    private String nomeReceita;
    private String modoPreparo;


    // Lista e adaptador para os ingredientes utilizados
    private ArrayList<String> listaIngredientesUtilizados;
    private ArrayAdapter<String> adapterIngredientes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_receita);

        // Inicializa os componentes da tela
        tvNomeReceitaDetalhe = findViewById(R.id.tv_nome_receita_detalhe);
        tvTempoPreparo = findViewById(R.id.tv_tempo_preparo);
        tvRendimento = findViewById(R.id.tv_rendimento);
        lvIngredientesUtilizados = findViewById(R.id.lv_ingredientes_utilizados);
        fabEditarReceita = findViewById(R.id.fab_editar_receita);
        fabModoPreparo = findViewById(R.id.fab_modo_preparo);
        ImageButton btnVoltar = findViewById(R.id.btn_voltar);
        btnVoltar.setOnClickListener(v -> onBackPressed());

        // Inicializa o Firestore e autenticação
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            // Se o usuário não estiver logado, redireciona para a tela de login
            Intent intent = new Intent(DetalhesReceita.this, FormLogin.class);
            startActivity(intent);
            finish();
            return;
        }

        // Recebe os dados da intent
        Intent intent = getIntent();
        nomeReceita = intent.getStringExtra("nome_receita");
        idReceita = intent.getStringExtra("id_receita");

        if (nomeReceita == null || idReceita == null) {
            Toast.makeText(this, "Erro ao carregar detalhes da receita", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Exibe o nome da receita
        tvNomeReceitaDetalhe.setText(nomeReceita);

        // Inicializa a lista e o adapter customizado para os ingredientes
        listaIngredientesUtilizados = new ArrayList<>();
        adapterIngredientes = new ArrayAdapter<>(this, R.layout.item_detalhes_receita, R.id.tv_nome_ingrediente, listaIngredientesUtilizados);
        lvIngredientesUtilizados.setAdapter(adapterIngredientes);

        // Carrega os detalhes da receita
        carregarDetalhesReceita();

        // Configura o FAB para editar a receita
        fabEditarReceita.setOnClickListener(v -> {
            Intent editarIntent = new Intent(DetalhesReceita.this, t1_NomeReceita.class);
            editarIntent.putExtra("nome_receita", nomeReceita);
            editarIntent.putExtra("id_receita", idReceita);
            editarIntent.putExtra("tempo_preparo", tvTempoPreparo.getText().toString().replace("Tempo de Preparo: ", "").replace(" minutos", ""));
            editarIntent.putExtra("rendimento", tvRendimento.getText().toString().replace("Rendimento: ", "").replace(" porções", ""));
            editarIntent.putExtra("modo_preparo", modoPreparo);
            editarIntent.putStringArrayListExtra("ingredientes_lista", listaIngredientesUtilizados);
            startActivity(editarIntent); // Abre a tela de edição da receita
        });

        // Configura o FAB para mostrar o modo de preparo
        fabModoPreparo.setOnClickListener(v -> {
            new AlertDialog.Builder(DetalhesReceita.this)
                    .setTitle("Modo de Preparo")
                    .setMessage(modoPreparo)
                    .setPositiveButton("Fechar", null)
                    .show();
        });
    }

    private void carregarDetalhesReceita() {
        // Busca os detalhes da receita no Firestore
        db.collection("Usuarios").document(userId).collection("Receitas").document(idReceita)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Atualiza os campos com os dados obtidos
                        String tempoPreparo = documentSnapshot.getString("tempoPreparo");
                        String rendimento = documentSnapshot.getString("rendimento");
                        modoPreparo = documentSnapshot.getString("modoPreparo");

                        tvTempoPreparo.setText("Tempo de Preparo: " + tempoPreparo + " minutos");
                        tvRendimento.setText("Rendimento: " + rendimento);

                        // Carrega os ingredientes utilizados --> subcoleção IngredientesUtilizados
                        db.collection("Usuarios").document(userId).collection("Receitas").document(idReceita)
                                .collection("IngredientesUtilizados")
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    listaIngredientesUtilizados.clear();
                                    for (QueryDocumentSnapshot ingredienteDoc : queryDocumentSnapshots) {
                                        String nomeIngrediente = ingredienteDoc.getString("nomeIngrediente");
                                        Long quantidadeUsada = ingredienteDoc.getLong("quantidadeUsada");
                                        if (nomeIngrediente != null && quantidadeUsada != null) {
                                            String item = nomeIngrediente + ": " + quantidadeUsada;
                                            listaIngredientesUtilizados.add(item);
                                        }
                                    }
                                    adapterIngredientes.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(DetalhesReceita.this, "Erro ao carregar ingredientes", Toast.LENGTH_SHORT).show();
                                    Log.e("DetalhesReceita", "Erro ao carregar ingredientes: ", e);
                                });
                    } else {
                        // Caso a receita não seja encontrada
                        Toast.makeText(DetalhesReceita.this, "Receita não encontrada", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetalhesReceita.this, "Erro ao carregar detalhes da receita", Toast.LENGTH_SHORT).show();
                    Log.e("DetalhesReceita", "Erro ao carregar receita: ", e);
                });
    }
}
