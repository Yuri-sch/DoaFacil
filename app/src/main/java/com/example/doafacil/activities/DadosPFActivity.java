package com.example.doafacil.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

// Activity para que o usuário (Pessoa Física) possa visualizar e editar seus dados cadastrais.
public class DadosPFActivity extends AppCompatActivity {

    // Declaração das varáiveis dos componentes de UI e da referência do Firebase.
    private EditText edtNome, edtCpf, edtIdade, edtTelefone;
    private Button btnSalvar;
    private DatabaseReference userRef;

    // Metodo chamado quando a Activity é criada.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dados_pf);

        // Configura a Toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita o botão de voltar na toolbar.

        // Obtém o usuário atualmente logado.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Se não houver usuário, exibe um aviso e fecha a activity.
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Define a referência específica para os dados do usuário logado.
        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pf").child(currentUser.getUid());

        // Inicializa os componentes de UI e carrega os dados do usuário.
        initViews();
        loadUserData();

        // Define a ação do botão de salvar.
        btnSalvar.setOnClickListener(v -> saveUserData());
    }

    // Associa as variáveis aos componentes do XML.
    private void initViews() {
        edtNome = findViewById(R.id.edtNomePF);
        edtCpf = findViewById(R.id.edtCpfPF);
        edtIdade = findViewById(R.id.edtIdadePF);
        edtTelefone = findViewById(R.id.edtTelefonePF);
        btnSalvar = findViewById(R.id.btnSalvarDadosPF);
    }

    // Carrega os dados do usuário do Firebase e os exibe nos campos de texto.
    private void loadUserData() {
        // Adiciona um listener para ler os dados do Firebase uma única vez.
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Converte o snapshot do Firebase em um objeto UsuarioPF.
                UsuarioPF usuario = snapshot.getValue(UsuarioPF.class);
                if (usuario != null) { // Se o usuário existir, preenche os campos.
                    edtNome.setText(usuario.getNome());
                    edtCpf.setText(usuario.getCpf()); // O CPF não é editável, mas vai aparecer.
                    edtIdade.setText(String.valueOf(usuario.getIdade()));
                    edtTelefone.setText(usuario.getTelefone());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Exibe uma mensagem em caso de erro na leitura dos dados.
                Toast.makeText(DadosPFActivity.this, "Falha ao carregar dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Salva os dados modificados pelo usuário no Firebase.
    private void saveUserData() {
        // Obtém os dados dos campos de texto.
        String nome = edtNome.getText().toString().trim();
        String idadeStr = edtIdade.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();

        // Valida se os campos obrigatórios foram preenchidos.
        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(idadeStr) || TextUtils.isEmpty(telefone)) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Converte a idade para o tipo inteiro.
        int idade = Integer.parseInt(idadeStr);

        // Cria um mapa para enviar apenas os campos atualizados para o Firebase.
        Map<String, Object> updates = new HashMap<>();
        updates.put("nome", nome);
        updates.put("idade", idade);
        updates.put("telefone", telefone);

        // Executa a atualização no Firebase.
        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // Exibe uma mensagem de sucesso com um Snackbar.
                    Snackbar.make(findViewById(android.R.id.content), "Dados atualizados com sucesso!", Snackbar.LENGTH_LONG).show();
                    // Fecha a activity após um pequeno delay para que o usuário veja a mensagem.
                    new android.os.Handler().postDelayed(this::finish, 1500);
                })
                .addOnFailureListener(e -> Snackbar.make(findViewById(android.R.id.content), "Erro ao atualizar dados.", Snackbar.LENGTH_LONG).show());
    }

    // Lida com o clique no botão "voltar" da Toolbar.
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Igual ao botão de voltar do dispositivo.
        return true;
    }
}
