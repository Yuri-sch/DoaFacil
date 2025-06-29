package com.example.doafacil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.doafacil.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class homePJActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private TextView tvBoasVindas;
    private Button btnGerenciarObjetos, btnGerenciarDados, btnGerenciarFotos, btnGerenciarContatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_pj);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pj").child(currentUser.getUid());

        // Associação dos componentes
        tvBoasVindas = findViewById(R.id.tvBoasVindas);
        btnGerenciarObjetos = findViewById(R.id.btnGerenciarObjetos);
        btnGerenciarDados = findViewById(R.id.btnGerenciarDados);
        btnGerenciarFotos = findViewById(R.id.btnGerenciarFotos);
        btnGerenciarContatos = findViewById(R.id.btnGerenciarContatos);

        loadUserName();

        // Listeners dos botões
        btnGerenciarObjetos.setOnClickListener(v ->
                startActivity(new Intent(homePJActivity.this, GerenciarObjetosActivity.class)));
        btnGerenciarDados.setOnClickListener(v ->
                startActivity(new Intent(homePJActivity.this, GerenciarDadosActivity.class)));
        btnGerenciarFotos.setOnClickListener(v ->
                startActivity(new Intent(homePJActivity.this, FotoPJActivity.class)));
        btnGerenciarContatos.setOnClickListener(v ->
                startActivity(new Intent(homePJActivity.this, ContatosPJActivity.class)));
    }

    private void loadUserName() {
        // ... (código sem alteração)
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    // MÉTODO ATUALIZADO PARA LIDAR COM OS CLIQUES DO MENU
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Usando if/else if para lidar com múltiplas opções
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_delete_account) {
            showDeleteAccountDialog(); // Chama o diálogo de confirmação
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // NOVO: Método para mostrar o diálogo de confirmação
    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Conta")
                .setMessage("Você tem certeza? Esta ação é irreversível. Todos os seus dados, incluindo objetos e contatos, serão apagados permanentemente.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Sim, Excluir", (dialog, whichButton) -> deleteUserAccount())
                .setNegativeButton("Não, Cancelar", null)
                .show();
    }

    // NOVO: Método que executa a exclusão da conta
    private void deleteUserAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Erro: usuário não encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Excluir dados do Realtime Database
        userRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 2. Se os dados foram excluídos, agora exclui a conta de autenticação
                user.delete().addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        Toast.makeText(homePJActivity.this, "Conta excluída com sucesso.", Toast.LENGTH_LONG).show();
                        // 3. Redireciona para a tela de Login
                        Intent intent = new Intent(homePJActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Isso pode acontecer se o login for muito antigo.
                        // Uma boa prática seria pedir para o usuário logar novamente.
                        Toast.makeText(homePJActivity.this, "Erro ao excluir conta. Por favor, faça login novamente e tente de novo.", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(homePJActivity.this, "Erro ao excluir os dados do banco.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
