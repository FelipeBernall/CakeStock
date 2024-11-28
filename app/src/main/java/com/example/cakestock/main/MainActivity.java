package com.example.cakestock.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.example.cakestock.cliente.ListaClientes;
import com.example.cakestock.financeiro.ListaTransacoes;
import com.example.cakestock.pedido.ListaPedidos;
import com.example.cakestock.ingrediente.EstoqueIngrediente;
import com.example.cakestock.produto.EstoqueProdutos;
import com.example.cakestock.receita.HistoricoProducoes;
import com.example.cakestock.receita.ListaReceitas;
import com.example.cakestock.usuario.PerfilUsuario;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializando LinearLayouts (botões)
        LinearLayout btnPerfilUsuario = findViewById(R.id.btnPerfilUsuario);
        LinearLayout btnControleEstoqueIngredientes = findViewById(R.id.btnControleEstoqueIngredientes);
        LinearLayout btnListaProdutos = findViewById(R.id.btnListaProdutos);
        LinearLayout btnListaReceitas = findViewById(R.id.btnListaReceitas);
        LinearLayout btnHistoricoProducao = findViewById(R.id.btnHistoricoProducao);
        LinearLayout btnListaClientes = findViewById(R.id.btnListaClientes);
        LinearLayout btnControleFinanceiro = findViewById(R.id.btnControleFinanceiro);
        LinearLayout btnPedidos = findViewById(R.id.btnPedidos);

        // Configurando ouvintes de clique
        btnPerfilUsuario.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PerfilUsuario.class)));
        btnControleEstoqueIngredientes.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EstoqueIngrediente.class)));
        btnListaProdutos.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EstoqueProdutos.class)));
        btnListaReceitas.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ListaReceitas.class)));
        btnHistoricoProducao.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HistoricoProducoes.class)));
        btnListaClientes.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ListaClientes.class)));
        btnControleFinanceiro.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ListaTransacoes.class)));
        btnPedidos.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ListaPedidos.class)));
    }
}
