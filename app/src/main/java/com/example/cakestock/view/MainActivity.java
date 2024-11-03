package com.example.cakestock.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.controller.CadastroCliente;
import com.example.cakestock.controller.CadastroIngrediente;
import com.example.cakestock.controller.CadastroProduto;
import com.example.cakestock.controller.EstoqueIngrediente;
import com.example.cakestock.controller.EstoqueProdutos;
import com.example.cakestock.R;
import com.example.cakestock.controller.activity.t1_NomeReceita;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializando botões
        Button btnPerfilUsuario = findViewById(R.id.btnPerfilUsuario);
        Button btnCadastroIngredientes = findViewById(R.id.btnCadastroIngredientes);
        Button btnControleEstoqueIngredientes = findViewById(R.id.btnControleEstoqueIngredientes);
        Button btnCadastroProduto = findViewById(R.id.btnCadastroProduto);
        Button btnListaProdutos = findViewById(R.id.btnListaProdutos);
        Button btnCadastroReceitas = findViewById(R.id.btnCadastroReceitas);
        Button btnListaReceitas = findViewById(R.id.btnListaReceitas);
        Button btnHistoricoProducao = findViewById(R.id.btnHistoricoProducao);
        Button btnCadastroCliente = findViewById(R.id.btnCadastroCliente);
        Button btnListaClientes = findViewById(R.id.btnListaClientes);
        Button btnControleFinanceiro = findViewById(R.id.btnControleFinanceiro);
        Button btnControlePedidos = findViewById(R.id.btnControlePedidos);

        // Configurando ouvintes de clique
        btnPerfilUsuario.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PerfilUsuario.class)));
        btnCadastroIngredientes.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CadastroIngrediente.class)));
        btnControleEstoqueIngredientes.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EstoqueIngrediente.class)));
        btnCadastroProduto.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CadastroProduto.class)));
        btnListaProdutos.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EstoqueProdutos.class)));
        btnCadastroReceitas.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, t1_NomeReceita.class)));
        btnListaReceitas.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ListaReceitas.class)));
        btnHistoricoProducao.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HistoricoProducoes.class)));
        btnCadastroCliente.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CadastroCliente.class)));
        btnListaClientes.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ListaClientes.class)));
        btnControleFinanceiro.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Transacoes.class)));

    }
}
