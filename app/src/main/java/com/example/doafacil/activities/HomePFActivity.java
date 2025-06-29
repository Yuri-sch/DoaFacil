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

import com.example.doafacil.MainActivity;
import com.example.doafacil.R;
import com.example.doafacil.models.UsuarioPF;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomePFActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private TextView tvBoasVindas;
    private Button btnQueroDoar, btnInstituicoesProximas, btnDadosPessoais;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_pf);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pf").child(currentUser.getUid());

        tvBoasVindas = findViewById(R.id.tvBoasVindasPF);
        btnQueroDoar = findViewById(R.id.btnQueroDoar);
        btnInstituicoesProximas = findViewById(R.id.btnInstituicoesProximas);
        btnDadosPessoais = findViewById(R.id.btnDadosPessoais);

        loadUserName();

        btnQueroDoar.setOnClickListener(v ->
                startActivity(new Intent(HomePFActivity.this, FiltrarDoacaoActivity.class)));

        btnInstituicoesProximas.setOnClickListener(v ->
                startActivity(new Intent(HomePFActivity.this, ListaInstituicoesActivity.class)));

        btnDadosPessoais.setOnClickListener(v ->
                startActivity(new Intent(HomePFActivity.this, DadosPFActivity.class)));
    }

    private void loadUserName() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UsuarioPF usuario = snapshot.getValue(UsuarioPF.class);
                if (usuario != null && usuario.getNome() != null && !usuario.getNome().isEmpty()) {
                    String primeiroNome = usuario.getNome().split(" ")[0];
                    tvBoasVindas.setText("Olá, " + primeiroNome + "!");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    // MÉTODO ATUALIZADO PARA INCLUIR A LÓGICA DE EXCLUSÃO
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        } else if (itemId == R.id.action_delete_account) {
            showDeleteAccountDialog(); // Chama o diálogo de confirmação
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // NOVO: Método para mostrar o diálogo de confirmação de exclusão
    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Conta")
                .setMessage("Você tem certeza? Esta ação é irreversível e todos os seus dados serão apagados permanentemente.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Sim, Excluir", (dialog, whichButton) -> deleteUserAccount())
                .setNegativeButton("Não, Cancelar", null)
                .show();
    }

    // NOVO: Método que executa a exclusão da conta PF
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
                        Toast.makeText(HomePFActivity.this, "Conta excluída com sucesso.", Toast.LENGTH_LONG).show();
                        // 3. Redireciona para a tela de Login
                        Intent intent = new Intent(HomePFActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(HomePFActivity.this, "Erro ao excluir conta. Por favor, faça login novamente e tente de novo.", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(HomePFActivity.this, "Erro ao excluir os dados do banco.", Toast.LENGTH_LONG).show();
            }
        });
    }
}