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

// Activity principal (tela inicial) para o usuário instituição (Pessoa Jurídica).
public class homePJActivity extends AppCompatActivity {

    // Declaração das varáveis do Firebase e da interface.
    private FirebaseAuth mAuth; // Para gerenciar a autenticação.
    private DatabaseReference userRef; // Para acessar os dados do usuário no Realtime Database.
    private Button btnGerenciarObjetos, btnGerenciarDados, btnGerenciarFotos, btnGerenciarContatos; // Botões de navegação.

    // Metodo chamado na criação da Activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_pj); // Define o layout da tela.

        // Configura a Toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializa o Firebase Auth e obtém o usuário atual.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) { // Se não houver usuário logado, redireciona para a tela de login.
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Define a referência para o nó específico do usuário logado.
        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pj").child(currentUser.getUid());

        // Associa as variáveis aos componentes do layout.
        btnGerenciarObjetos = findViewById(R.id.btnGerenciarObjetos);
        btnGerenciarDados = findViewById(R.id.btnGerenciarDados);
        btnGerenciarFotos = findViewById(R.id.btnGerenciarFotos);
        btnGerenciarContatos = findViewById(R.id.btnGerenciarContatos);

        // Define os listeners de clique para os botões de gerenciamento.
        btnGerenciarObjetos.setOnClickListener(v ->
                startActivity(new Intent(homePJActivity.this, GerenciarObjetosActivity.class)));
        btnGerenciarDados.setOnClickListener(v ->
                startActivity(new Intent(homePJActivity.this, GerenciarDadosActivity.class)));
        btnGerenciarFotos.setOnClickListener(v ->
                startActivity(new Intent(homePJActivity.this, FotoPJActivity.class)));
        btnGerenciarContatos.setOnClickListener(v ->
                startActivity(new Intent(homePJActivity.this, ContatosPJActivity.class)));
    }

    // Infla o menu de opções na Toolbar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    // Lida com os cliques nos itens do menu da Toolbar.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Lidaa com as diferentes opções do menu.
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut(); // Desloga o usuário.
            startActivity(new Intent(this, LoginActivity.class)); // Volta para a tela de login.
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_delete_account) {
            showDeleteAccountDialog(); // Chama o dialog de confirmação.
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // Exibe um dialog de confirmação antes de excluir a conta.
    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Conta")
                .setMessage("Você tem certeza? Esta ação é irreversível. Todos os seus dados, incluindo objetos e contatos, serão apagados permanentemente.")
                .setIcon(android.R.drawable.ic_dialog_alert) // Ícone de alerta padrão.
                .setPositiveButton("Sim, Excluir", (dialog, whichButton) -> deleteUserAccount())
                .setNegativeButton("Não, Cancelar", null)
                .show();
    }

    // Executa a exclusão da conta do usuário.
    private void deleteUserAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) { // Verificação de segurança.
            Toast.makeText(this, "Erro: usuário não encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Exclui os dados do usuário do Realtime Database.
        userRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Se os dados foram excluídos, agora exclui a conta de autenticação.
                user.delete().addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        Toast.makeText(homePJActivity.this, "Conta excluída com sucesso.", Toast.LENGTH_LONG).show();
                        // Redireciona para a tela de Login e limpa o histórico de telas.
                        Intent intent = new Intent(homePJActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Trata o caso em que a autenticação é muito antiga e a exclusão falha.
                        Toast.makeText(homePJActivity.this, "Erro ao excluir conta. Por favor, faça login novamente e tente de novo.", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(homePJActivity.this, "Erro ao excluir os dados do banco.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
