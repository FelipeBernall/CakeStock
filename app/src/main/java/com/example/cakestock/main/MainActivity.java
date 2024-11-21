package com.example.cakestock.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cakestock.R;
import com.example.cakestock.cliente.ListaClientes;
import com.example.cakestock.cliente.CadastroCliente;
import com.example.cakestock.financeiro.Transacoes;
import com.example.cakestock.ingrediente.CadastroIngrediente;
import com.example.cakestock.pedido.CadastroPedido;
<<<<<<< HEAD:app/src/main/java/com/example/cakestock/main/MainActivity.java
import com.example.cakestock.pedido.ListaPedidos;
=======
>>>>>>> ed5652af40b377ea25bdfd91684cfd4e584e010e:app/src/main/java/com/example/cakestock/view/MainActivity.java
import com.example.cakestock.produto.CadastroProduto;
import com.example.cakestock.estoque.EstoqueIngrediente;
import com.example.cakestock.estoque.EstoqueProdutos;
import com.example.cakestock.produto.HistoricoProducoes;
import com.example.cakestock.receita.ListaReceitas;
import com.example.cakestock.receita.t1_NomeReceita;
import com.example.cakestock.usuario.PerfilUsuario;

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
        Button btnCadastroPedido = findViewById(R.id.btnCadastroPedido);
<<<<<<< HEAD:app/src/main/java/com/example/cakestock/main/MainActivity.java
        Button btnPedidos = findViewById(R.id.btnPedidos);

=======
>>>>>>> ed5652af40b377ea25bdfd91684cfd4e584e010e:app/src/main/java/com/example/cakestock/view/MainActivity.java

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
        btnCadastroPedido.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CadastroPedido.class)));
<<<<<<< HEAD:app/src/main/java/com/example/cakestock/main/MainActivity.java
        btnPedidos.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ListaPedidos.class)));
=======
>>>>>>> ed5652af40b377ea25bdfd91684cfd4e584e010e:app/src/main/java/com/example/cakestock/view/MainActivity.java


    }
}
