package com.example.cakestock.usuario;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cakestock.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FormCadastro extends AppCompatActivity {

    // Declaração dos componente
    private EditText edit_nome, edit_email, edit_confirmar_email, edit_senha;
    private Button btn_cadastrar;
    String mensagem_erro = "Preencha todos os campos";
    String mensagem_sucesso = "Cadastro realizado com sucesso";

    String usuarioID; // armazena o ID do usuário

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializa o Firebase
        FirebaseApp.initializeApp(this);

        // Ajustes de layout (padrão do Android Studio)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_cadastro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        IniciarComponentes();

        // Adicionando TextWatcher para validar a senha
        edit_senha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 6) {
                    edit_senha.setError("A senha deve ter no mínimo 6 caracteres");
                } else {
                    edit_senha.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btn_cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // recuperar dados inseridos pelo usuário
                String nome = edit_nome.getText().toString();
                String email = edit_email.getText().toString();
                String confirmarEmail = edit_confirmar_email.getText().toString();
                String senha = edit_senha.getText().toString();

                // verificações
                if (nome.isEmpty() || email.isEmpty() || confirmarEmail.isEmpty() || senha.isEmpty()) {
                    Snackbar snackbar = Snackbar.make(v, mensagem_erro, Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                } else if (!email.equals(confirmarEmail)) {
                    Snackbar snackbar = Snackbar.make(v, "Os e-mails não coincidem", Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                } else {
                    CadastrarUsuario(v);
                }
            }
        });
    }

    private void CadastrarUsuario(View v) {

        // Pega os dados digitados pelo usuário
        String email = edit_email.getText().toString();
        String senha = edit_senha.getText().toString();

        // Tenta criar a conta no Firebase com e-mail e senha
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) { // Se o cadastro foi bem-sucedido
                    SalvarDadosUsuario(); // Salva os dados no Firestore

                    // Envia um e-mail de verificação para o usuário
                    FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) { // E-mail enviado (sucesso)
                                Snackbar snackbar = Snackbar.make(v, "Verifique seu e-mail para confirmar o cadastro.", Snackbar.LENGTH_LONG);
                                snackbar.setBackgroundTint(Color.WHITE);
                                snackbar.setTextColor(Color.BLACK);
                                snackbar.show();

                                // Redirecionar para a tela de login
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        FirebaseAuth.getInstance().signOut();

                                        Intent intent = new Intent(FormCadastro.this, FormLogin.class);
                                        startActivity(intent); // Vai para a tela de login
                                        finish();
                                    }
                                }, 2500);
                            } else {
                                Snackbar snackbar = Snackbar.make(v, "Falha ao enviar e-mail de verificação.", Snackbar.LENGTH_SHORT);
                                snackbar.setBackgroundTint(Color.WHITE);
                                snackbar.setTextColor(Color.BLACK);
                                snackbar.show();
                            }
                        }
                    });

                } else { // Se o cadastro falhar

                    // Tratamento de exceções (FirFirebase AuthenticationAuthentication)
                    String erro = "Erro ao cadastrar usuário";
                    if (task.getException() != null) {
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e) {
                            erro = "Digite uma senha com no mínimo 6 caracteres";  // senha fraca
                        } catch (FirebaseAuthUserCollisionException e) {
                            erro = "Esta conta já foi cadastrada";            // email já cadastrado
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            erro = "E-mail inválido";                       // email mal formatado
                        } catch (Exception e) {
                            erro = "Erro ao cadastrar usuário";
                        }
                    }
                    Snackbar snackbar = Snackbar.make(v, erro, Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }

            }
        });
    }

    private void SalvarDadosUsuario(){

        // Obtém o nome digitado pelo usuário
        String nome = edit_nome.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Cria um Map com os dados do usuário (chave-valor)
        Map<String,Object> usuarios = new HashMap<>();
        usuarios.put("nome", nome);

        // Obtém o ID único do usuário autenticado
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Cria uma referência ao documento do usuário no Firestore
        DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);

        // Salva os dados no Firestore
        documentReference.set(usuarios)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("db", "Sucesso ao salvar dados");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("db_error", "Erro ao salvar os dados" + e.toString());
                    }
                });
    }

    private void IniciarComponentes() {
        edit_nome = findViewById(R.id.edit_nome);
        edit_email = findViewById(R.id.edit_email);
        edit_confirmar_email = findViewById(R.id.edit_confirmar_email);
        edit_senha = findViewById(R.id.edit_senha);
        btn_cadastrar = findViewById(R.id.btn_cadastrar);
    }
}
