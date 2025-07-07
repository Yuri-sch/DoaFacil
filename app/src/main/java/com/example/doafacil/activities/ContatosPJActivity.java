package com.example.doafacil.activities;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doafacil.R;
import com.example.doafacil.adapters.ContatosAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Activity para gerenciar os contatos de uma instituição (PJ).
public class ContatosPJActivity extends AppCompatActivity {

    // Declaração de variáveis para os componentes de UI e Firebase.
    private DatabaseReference userRef; // Referência para o usuário no Firebase Database.
    private RecyclerView rvContatos; // RecyclerView para exibir a lista de contatos.
    private TextView tvListaVazia; // TextView para mostrar quando a lista de contatos está vazia.
    private ContatosAdapter adapter; // Adapter para o RecyclerView.
    private List<String> contatosList = new ArrayList<>(); // Lista para armazenar as strings de contato.
    private Map<String, String> contatosMap; // Mapa para associar a chave do Firebase ao valor do contato.

    // Metodo chamado na criação da Activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contatos_pj);

        // Configura a Toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita o botão de voltar.

        // Verifica se há um usuário autenticado.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            finish(); // Fecha a activity se não houver usuário.
            return;
        }

        // Define a referência para o usuário atual.
        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pj").child(currentUser.getUid());

        // Associa as variáveis aos componentes do layout.
        rvContatos = findViewById(R.id.rvContatos);
        tvListaVazia = findViewById(R.id.tvListaVazia);

        // Configura o RecyclerView e carrega os contatos.
        setupRecyclerView();
        loadContatos();

        // Configura o botão flutuante para adicionar novos contatos.
        FloatingActionButton fab = findViewById(R.id.fabAdicionarContato);
        fab.setOnClickListener(v -> showAddContatoDialog());
    }

    // Configura o RecyclerView com o adapter.
    private void setupRecyclerView() {
        // Inicializa o adapter, passando a lista e o listener para exclusão.
        adapter = new ContatosAdapter(contatosList, contato -> showDeleteContatoDialog(contato));
        rvContatos.setAdapter(adapter);
    }

    // Carrega os contatos do Firebase e atualiza a lista em tempo real.
    private void loadContatos() {
        // Adiciona um listener
        userRef.child("contatos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contatosList.clear(); // Limpa a lista antes de adicionar os novos dados.
                if (snapshot.exists()) {
                    // Obtém os contatos como um mapa de Chave-Valor.
                    contatosMap = (Map<String, String>) snapshot.getValue();
                    if (contatosMap != null) {
                        contatosList.addAll(contatosMap.values()); // Adiciona todos os valores (contatos) à lista.
                    }
                }
                adapter.notifyDataSetChanged(); // Notifica o adapter que os dados mudaram.
                // Controla a visibilidade da mensagem de lista vazia.
                tvListaVazia.setVisibility(contatosList.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Exibe uma mensagem de erro em caso de falha.
                Toast.makeText(ContatosPJActivity.this, "Erro ao carregar contatos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Exibe um diálogo para adicionar um novo contato.
    private void showAddContatoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Contato");

        // Cria um campo de texto para o diálogo.
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("email@exemplo.com ou (51) 99999-9999");
        builder.setView(input);

        // Configura o botão "Adicionar".
        builder.setPositiveButton("Adicionar", (dialog, which) -> {
            String contato = input.getText().toString().trim();
            if (!contato.isEmpty()) { // Verifica se o campo não está vazio.
                String key = userRef.child("contatos").push().getKey(); // Gera uma chave única no Firebase.
                if (key != null) {
                    userRef.child("contatos").child(key).setValue(contato); // Salva o contato no Firebase.
                }
            }
        });
        // Configura o botão "Cancelar".
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show(); // Exibe o diálogo.
    }

    // Exibe um diálogo de confirmação para excluir um contato.
    private void showDeleteContatoDialog(String contato) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Contato")
                .setMessage("Tem certeza que deseja excluir o contato '" + contato + "'?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    String keyToRemove = null;
                    if (contatosMap != null) {
                        // Procura a chave correspondente ao valor do contato a ser removido.
                        for (Map.Entry<String, String> entry : contatosMap.entrySet()) {
                            if (Objects.equals(contato, entry.getValue())) {
                                keyToRemove = entry.getKey();
                                break; // Encontrou a chave, pode parar o loop.
                            }
                        }
                    }
                    if (keyToRemove != null) {
                        // Remove o contato do Firebase usando a chave encontrada.
                        userRef.child("contatos").child(keyToRemove).removeValue()
                                .addOnSuccessListener(aVoid -> Toast.makeText(ContatosPJActivity.this, "Contato removido!", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Não", null)
                .show();
    }

    // Lida com o clique no botão "voltar" da Toolbar.
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Igual o botão de voltar do dispositivo.
        return true;
    }
}
