package com.example.doafacil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doafacil.MainActivity;
import com.example.doafacil.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // Declaração dos elementos da interface
    private EditText edtEmail, edtSenha;
    private Button btnLogin, btnCadastroPF, btnCadastroPJ;

    // Instância da autenticação do Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Define o layout da Activity

        if (getIntent().getBooleanExtra("cadastro_sucesso", false)) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Cadastro realizado com sucesso!", Snackbar.LENGTH_INDEFINITE);
            snackbar.setDuration(4000); // 4 segundos
            snackbar.show();
        }

        // Associação dos elementos da interface com os IDs definidos no XML
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btnLogin = findViewById(R.id.btnLogin);
        btnCadastroPF = findViewById(R.id.btnCadastroPF);
        btnCadastroPJ = findViewById(R.id.btnCadastroPJ);

        // Inicializa a instância do Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Quando o botão de login é clicado, chama a função de login
        btnLogin.setOnClickListener(v -> loginUsuario());

        // Botão para cadastro de Pessoa Física
        btnCadastroPF.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, CadastroPFActivity.class);
            startActivity(i); // Abre a tela de cadastro PF
        });

        // Botão para cadastro de Pessoa Jurídica
        btnCadastroPJ.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, CadastroPJActivity.class);
            startActivity(i); // Abre a tela de cadastro PJ
        });
    }

    // Função responsável por realizar o login no Firebase
    private void loginUsuario() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        // Verifica se os campos estão vazios
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Autentica o usuário com Firebase
        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Login bem-sucedido
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, homePJActivity.class); // MUDANÇA AQUI
                        startActivity(intent);
                        finish();
                    } else {
                        // Erro no login
                        Toast.makeText(this, "Erro ao fazer login: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
