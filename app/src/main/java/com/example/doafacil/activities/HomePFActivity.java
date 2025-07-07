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
import com.example.doafacil.models.UsuarioPF;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Activity de tela principal para o usuário doador (Pessoa Física).
public class HomePFActivity extends AppCompatActivity {

    // Declaração das varíaveis da interface e do Firebase
    private FirebaseAuth mAuth; // Para gerenciar a autenticação do usuário.
    private DatabaseReference userRef; // Para acessar os dados do usuário no Realtime Database.
    private TextView tvBoasVindas; // Exibe a mensagem de boas-vindas.
    private Button btnQueroDoar, btnInstituicoesProximas, btnDadosPessoais; // Botões de navegação.

    // Metodo executado na criação da Activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_pf);

        // Configura a Toolbar como a barra de ações da activity.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializa o Firebase Auth e obtém o usuário atual.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) { // Se não houver usuário logado, redireciona para a tela de login.
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Fecha a activity atual.
            return;
        }

        // Define a referência para o nó específico do usuário logado.
        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pf").child(currentUser.getUid());

        // Associa as variáveis aos componentes do layout.
        tvBoasVindas = findViewById(R.id.tvBoasVindasPF);
        btnQueroDoar = findViewById(R.id.btnQueroDoar);
        btnInstituicoesProximas = findViewById(R.id.btnInstituicoesProximas);
        btnDadosPessoais = findViewById(R.id.btnDadosPessoais);

        // Carrega o nome do usuário para a mensagem de boas-vindas.
        loadUserName();

        // Define as ações de clique para os botões de navegação.
        btnQueroDoar.setOnClickListener(v ->
                startActivity(new Intent(HomePFActivity.this, FiltrarDoacaoActivity.class)));

        btnInstituicoesProximas.setOnClickListener(v ->
                startActivity(new Intent(HomePFActivity.this, ListaInstituicoesActivity.class)));

        btnDadosPessoais.setOnClickListener(v ->
                startActivity(new Intent(HomePFActivity.this, DadosPFActivity.class)));
    }

    // Carrega o nome do usuário do Firebase para personalizar a saudação.
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
            public void onCancelled(@NonNull DatabaseError error) {} // Metodo chamado em caso de erro na leitura.
        });
    }

    // Infla o menu de opções (logout, excluir conta) na Toolbar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    // Lida com os cliques nos itens do menu da Toolbar.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_logout) { // Se o item de logout for clicado.
            mAuth.signOut(); // Desloga o usuário do Firebase.
            startActivity(new Intent(this, LoginActivity.class)); // Volta para a tela de login.
            finish();
            return true;
        } else if (itemId == R.id.action_delete_account) { // Se o item de excluir conta for clicado.
            showDeleteAccountDialog(); // Chama o diálogo de confirmação.
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Exibe um diálogo de confirmação antes de excluir a conta do usuário.
    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Conta")
                .setMessage("Você tem certeza? Esta ação é irreversível e todos os seus dados serão apagados permanentemente.")
                .setIcon(android.R.drawable.ic_dialog_alert) // Ícone de alerta padrão do Android.
                .setPositiveButton("Sim, Excluir", (dialog, whichButton) -> deleteUserAccount())
                .setNegativeButton("Não, Cancelar", null)
                .show();
    }

    // Executa a lógica para excluir a conta do usuário.
    private void deleteUserAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) { // Verifica se o usuário ainda está logado.
            Toast.makeText(this, "Erro: usuário não encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Primeiro, exclui os dados do Realtime Database.
        userRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Se os dados foram excluídos, agora exclui a conta de autenticação.
                user.delete().addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        Toast.makeText(HomePFActivity.this, "Conta excluída com sucesso.", Toast.LENGTH_LONG).show();
                        // Redireciona para a tela de Login, limpando o histórico de activities.
                        Intent intent = new Intent(HomePFActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Informa ao usuário que pode ser necessário relogar para excluir a conta.
                        Toast.makeText(HomePFActivity.this, "Erro ao excluir conta. Por favor, faça login novamente e tente de novo.", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(HomePFActivity.this, "Erro ao excluir os dados do banco.", Toast.LENGTH_LONG).show();
            }
        });
    }
}