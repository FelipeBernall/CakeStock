package com.example.cakestock.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cakestock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class PerfilUsuario extends AppCompatActivity {
    
    
    private TextView nomeUsuario, emailUsuario;
    private Button btn_deslogar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String usuarioID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ajustes de layout (padrão do Android Studio)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_usuario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        IniciarComponentes();

        // Listener ao clicar no botão para deslogar
        btn_deslogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut(); // DESLOGAR USUÁRIO
                Intent intent = new Intent(PerfilUsuario.this, FormLogin.class); // nova intent para voltar à tela de login
                startActivity(intent);
                finish();
            }
        });
    }

    // RECUPERAR OS DADOS DE PERFIL DO USUÁRIO
    @Override
    protected void onStart() {
        super.onStart();

        // Obtém o e-mail e ID do usuário atualmente logado
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Cria uma referência no documento do usuário na coleção "Usuarios"
        DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);

        // ouvinte para mudanças no documento do usuário em tempo real
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if(documentSnapshot != null){

                    // Mostra o nome e o email do usuário na tla
                    nomeUsuario.setText(documentSnapshot.getString("nome"));
                    emailUsuario.setText(email);

                }
            }
        });
    }

    private void IniciarComponentes(){
        nomeUsuario = findViewById(R.id.text_nomeUsuario);
        emailUsuario = findViewById(R.id.text_emailUsuario);
        btn_deslogar = findViewById(R.id.btn_deslogar);

    }
}