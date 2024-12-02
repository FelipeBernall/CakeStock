package com.example.cakestock.usuario;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cakestock.R;
import com.example.cakestock.main.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FormLogin extends AppCompatActivity {

    // Declaração dos componentes
    private TextView text_tela_cadastro;
    private TextView edit_email, edit_senha;
    private Button btn_entrar;
    private TextView esqueceu_senha;
    private ProgressBar progressBar;
    // String[] mensagens = {"Preencha todos os campos"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ajustes de layout (padrão do Android Studio)
        EdgeToEdge.enable(this); // visualização de borda a borda
        setContentView(R.layout.activity_form_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar componentes
        IniciarComponentes();

        // Configurar OnClickListener para o TextView "text_tela_cadastro"
        text_tela_cadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar a atividade FormCadastro
                Intent intent = new Intent(FormLogin.this, FormCadastro.class);
                startActivity(intent);
            }
        });

        // Evento de Clique no botão ENTRAR
        btn_entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edit_email.getText().toString();
                String senha = edit_senha.getText().toString();

                if(email.isEmpty() || senha.isEmpty()){
                    Snackbar snackbar = Snackbar.make(v, "Preencha todos os campos", Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                } else {
                    AutenticarUsuario(v);
                }
            }
        });

        // Evento de Clique no TextView "Esqueceu sua senha?"
        esqueceu_senha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edit_email.getText().toString();

                if (email.isEmpty()) {
                    Snackbar snackbar = Snackbar.make(v, "Por favor, insira seu e-mail para redefinir a senha", Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                } else {
                    resetPassword(v, email);
                }
            }
        });
    }

    // Redefinir senha
    private void resetPassword(View v, String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Snackbar snackbar = Snackbar.make(v, "E-mail de redefinição de senha enviado", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar.make(v, "Falha ao enviar e-mail de redefinição de senha", Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }
            }
        });
    }

    private void AutenticarUsuario(View view){
        // Obtém o e-mail e a senha digitados pelo usuário
        String email = edit_email.getText().toString();
        String senha = edit_senha.getText().toString();

        // Tenta autenticar o usuário no Firebase
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){ // Verifica se o login foi bem-sucedido -> se for bem suceddido
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    // Verificar se o e-mail foi verificado
                    if (user != null && user.isEmailVerified()) {

                        // Se email verificado/dados válidos  -> barra de progresso e redireciona para a tela principal
                        progressBar.setVisibility(View.VISIBLE);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                irParaTelaPrincipal();
                            }
                        }, 3000);
                    } else {
                        // E-mail não verificado -> exibe mensagem para verificar o e-mail
                        FirebaseAuth.getInstance().signOut();
                        Snackbar snackbar = Snackbar.make(view, "Por favor, verifique seu e-mail antes de fazer login.", Snackbar.LENGTH_LONG);
                        snackbar.setBackgroundTint(Color.WHITE);
                        snackbar.setTextColor(Color.BLACK);
                        snackbar.show();
                    }
                } else {
                    // Mensagem de erro se falhar
                    String erro;

                    try {
                        throw task.getException();
                    } catch (Exception e){
                        erro = "Erro ao logar usuário";
                    }
                    Snackbar snackbar = Snackbar.make(view, erro, Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }
            }
        });
    }

    // Chamado qnd a tela é iniciada
    @Override
    protected void onStart() {
        super.onStart();

        // Obtém o usuário atual logado no Firebase
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();

        // Se o usuário estiver logado E o e-mail estiver confirmado
        if(usuarioAtual != null && usuarioAtual.isEmailVerified()){
            // Redireciona para a tela principal
            irParaTelaPrincipal();
        }
    }

    private void irParaTelaPrincipal(){
        Intent intent = new Intent(FormLogin.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void IniciarComponentes() {
        text_tela_cadastro = findViewById(R.id.text_tela_cadastro);
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        btn_entrar = findViewById(R.id.btn_entrar);
        esqueceu_senha = findViewById(R.id.esqueceu_senha);
        progressBar = findViewById(R.id.progressbar);
    }
}
