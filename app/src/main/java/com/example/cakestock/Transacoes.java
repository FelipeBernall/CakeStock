package com.example.cakestock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class Transacoes extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transacao);

        ImageButton btnVendas = findViewById(R.id.btn_vendas);
        ImageButton btnDespesas = findViewById(R.id.btn_despesas);

        btnVendas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Transacoes.this, RegistroVenda.class);
                startActivity(intent);
            }
        });

        btnDespesas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Transacoes.this, RegistroDespesa.class);
                startActivity(intent);
            }
        });
    }
}
