package com.example.doafacil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doafacil.MainActivity;
import com.example.doafacil.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtSenha;
    private Button btnLogin, btnCadastroPF, btnCadastroPJ;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase; // Referência para o banco de dados

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getIntent().getBooleanExtra("cadastro_sucesso", false)) {
            Snackbar.make(findViewById(android.R.id.content), "Cadastro realizado com sucesso!", Snackbar.LENGTH_LONG).show();
        }

        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btnLogin = findViewById(R.id.btnLogin);
        btnCadastroPF = findViewById(R.id.btnCadastroPF);
        btnCadastroPJ = findViewById(R.id.btnCadastroPJ);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // Referência raiz do banco

        btnLogin.setOnClickListener(v -> loginUsuario());

        btnCadastroPF.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, CadastroPFActivity.class));
        });

        btnCadastroPJ.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, CadastroPJActivity.class));
        });
    }

    private void loginUsuario() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Snackbar.make(findViewById(android.R.id.content), "Preencha todos os campos!", Snackbar.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkUserType(task.getResult().getUser().getUid());
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "E-mail e senha incorretos", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * NOVO MÉTODO: Verifica se o usuário é PJ ou PF no banco de dados
     * @param uid O ID do usuário que acabou de logar.
     */
    private void checkUserType(String uid) {
        // Procura o UID do usuário dentro do nó "usuarios/pj"
        DatabaseReference pjRef = mDatabase.child("usuarios").child("pj").child(uid);

        pjRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Intent intent;
                if (snapshot.exists()) {
                    // Se o usuário é uma PJ, vai para a home de PJ
                    intent = new Intent(LoginActivity.this, homePJActivity.class);
                } else {
                    // Se não, é um PF e vai para a home de PF
                    // LINHA ALTERADA ABAIXO
                    intent = new Intent(LoginActivity.this, HomePFActivity.class);
                }
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Em caso de erro, apenas vai para a tela principal como padrão
                Toast.makeText(LoginActivity.this, "Erro ao verificar tipo de usuário.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
