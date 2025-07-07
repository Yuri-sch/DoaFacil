package com.example.doafacil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doafacil.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Activity responsável pela tela de login e redirecionamento inicial.
public class LoginActivity extends AppCompatActivity {

    // Declaração dos componentes de UI e Firebase.
    private TextInputEditText edtEmail, edtSenha; // Campos para inserção de email e senha.
    private Button btnLogin, btnCadastroPF, btnCadastroPJ; // Botões de ação.
    private FirebaseAuth mAuth; // Instância para gerenciar a autenticação.
    private DatabaseReference mDatabase; // Referência para a raiz do banco de dados Firebase.

    // Metodo chamado na criação da Activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Exibe uma mensagem se o usuário acabou de se cadastrar com sucesso.
        if (getIntent().getBooleanExtra("cadastro_sucesso", false)) {
            Snackbar.make(findViewById(android.R.id.content), "Cadastro realizado com sucesso!", Snackbar.LENGTH_LONG).show();
        }

        // Associa as variáveis aos componentes do layout.
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btnLogin = findViewById(R.id.btnLogin);
        btnCadastroPF = findViewById(R.id.btnCadastroPF);
        btnCadastroPJ = findViewById(R.id.btnCadastroPJ);

        // Inicializa as instâncias do Firebase.
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Define o listener do botão de login.
        btnLogin.setOnClickListener(v -> loginUsuario());

        // Define o listener do botão para se cadastrar como Pessoa Física.
        btnCadastroPF.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, CadastroPFActivity.class));
        });

        // Define o listener do botão para se cadastrar como Pessoa Jurídica.
        btnCadastroPJ.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, CadastroPJActivity.class));
        });
    }

    // Tenta autenticar o usuário com o e-mail e senha fornecidos.
    private void loginUsuario() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        // Verifica se os campos de e-mail e senha não estão vazios.
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Snackbar.make(findViewById(android.R.id.content), "Preencha todos os campos!", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Tenta fazer o login com o Firebase Auth.
        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) { // Se o login for bem-sucedido...
                        checkUserType(task.getResult().getUser().getUid()); // ...verifica o tipo de usuário para o redirecionamento correto.
                    } else { // Se o login falhar...
                        Snackbar.make(findViewById(android.R.id.content), "E-mail e senha incorretos", Snackbar.LENGTH_SHORT).show(); // ...mostra uma mensagem de erro.
                    }
                });
    }

    // Verifica no banco de dados se o usuário é do tipo PF ou PJ.
    private void checkUserType(String uid) {
        // Cria uma referência para o nó de usuários PJ com o UID do usuário logado.
        DatabaseReference pjRef = mDatabase.child("usuarios").child("pj").child(uid);

        // Executa uma leitura única no banco de dados.
        pjRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Intent intent;
                if (snapshot.exists()) { // Se o UID existe no nó "pj"...
                    intent = new Intent(LoginActivity.this, homePJActivity.class); // ...o usuário é uma instituição.
                } else { // Caso contrário...
                    intent = new Intent(LoginActivity.this, HomePFActivity.class); // ...o usuário é um doador (PF).
                }
                startActivity(intent); // Inicia a activity correspondente.
                finish(); // Fecha a tela de login.
            }

            // Tratamento para erros no login
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Erro ao verificar tipo de usuário. Tente novamente.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
