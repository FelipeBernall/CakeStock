package com.example.cakestock.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class DetalhesReceita extends AppCompatActivity {

    private TextView tvNomeReceitaDetalhe;
    private TextView tvTempoPreparo;
    private TextView tvRendimento;
    private TextView tvModoPreparo;
    private ListView lvIngredientesUtilizados;
    private FloatingActionButton fabEditarReceita;

    private FirebaseFirestore db;
    private String userId;
    private String idReceita;
    private String nomeReceita;

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
        tvModoPreparo = findViewById(R.id.tv_modo_preparo);
        lvIngredientesUtilizados = findViewById(R.id.lv_ingredientes_utilizados);
        fabEditarReceita = findViewById(R.id.fab_editar_receita);

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
        adapterIngredientes = new ArrayAdapter<>(this, R.layout.item_ingrediente, R.id.textNome, listaIngredientesUtilizados);
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
            editarIntent.putExtra("modo_preparo", tvModoPreparo.getText().toString());
            editarIntent.putStringArrayListExtra("ingredientes_lista", listaIngredientesUtilizados);
            startActivity(editarIntent);
        });
    }

    private void carregarDetalhesReceita() {
        db.collection("Usuarios").document(userId).collection("Receitas").document(idReceita)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Obter os campos principais
                        String tempoPreparo = documentSnapshot.getString("tempoPreparo");
                        String rendimento = documentSnapshot.getString("rendimento");
                        String modoPreparo = documentSnapshot.getString("modoPreparo");

                        tvTempoPreparo.setText("Tempo de Preparo: " + tempoPreparo + " minutos");
                        tvRendimento.setText("Rendimento: " + rendimento + " porções");
                        tvModoPreparo.setText(modoPreparo);

                        // Obter a subcoleção IngredientesUtilizados
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
