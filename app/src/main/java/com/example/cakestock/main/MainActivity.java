package com.example.cakestock.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.example.cakestock.cliente.ListaClientes;
import com.example.cakestock.financeiro.ListaTransacoes;
import com.example.cakestock.pedido.ListaPedidos;
import com.example.cakestock.ingrediente.EstoqueIngrediente;
import com.example.cakestock.estoque.EstoqueProdutos;
import com.example.cakestock.produto.HistoricoProducoes;
import com.example.cakestock.receita.ListaReceitas;
import com.example.cakestock.usuario.PerfilUsuario;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializando botões
        Button btnPerfilUsuario = findViewById(R.id.btnPerfilUsuario);
        Button btnControleEstoqueIngredientes = findViewById(R.id.btnControleEstoqueIngredientes);
        Button btnListaProdutos = findViewById(R.id.btnListaProdutos);
        Button btnListaReceitas = findViewById(R.id.btnListaReceitas);
        Button btnHistoricoProducao = findViewById(R.id.btnHistoricoProducao);
        Button btnListaClientes = findViewById(R.id.btnListaClientes);
        Button btnControleFinanceiro = findViewById(R.id.btnControleFinanceiro);
        Button btnPedidos = findViewById(R.id.btnPedidos);


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
