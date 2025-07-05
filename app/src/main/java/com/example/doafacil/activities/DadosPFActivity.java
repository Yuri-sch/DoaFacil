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

public class DadosPFActivity extends AppCompatActivity {

    private EditText edtNome, edtCpf, edtIdade, edtTelefone;
    private Button btnSalvar;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dados_pf);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pf").child(currentUser.getUid());

        initViews();
        loadUserData();

        btnSalvar.setOnClickListener(v -> saveUserData());
    }

    private void initViews() {
        edtNome = findViewById(R.id.edtNomePF);
        edtCpf = findViewById(R.id.edtCpfPF);
        edtIdade = findViewById(R.id.edtIdadePF);
        edtTelefone = findViewById(R.id.edtTelefonePF);
        btnSalvar = findViewById(R.id.btnSalvarDadosPF);
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UsuarioPF usuario = snapshot.getValue(UsuarioPF.class);
                if (usuario != null) {
                    edtNome.setText(usuario.getNome());
                    edtCpf.setText(usuario.getCpf());
                    edtIdade.setText(String.valueOf(usuario.getIdade()));
                    edtTelefone.setText(usuario.getTelefone());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DadosPFActivity.this, "Falha ao carregar dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserData() {
        String nome = edtNome.getText().toString().trim();
        String idadeStr = edtIdade.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(idadeStr) || TextUtils.isEmpty(telefone)) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        int idade = Integer.parseInt(idadeStr);

        Map<String, Object> updates = new HashMap<>();
        updates.put("nome", nome);
        updates.put("idade", idade);
        updates.put("telefone", telefone);

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // O primeiro argumento é a view onde o Snackbar vai aparecer
                    Snackbar.make(findViewById(android.R.id.content), "Dados atualizados com sucesso!", Snackbar.LENGTH_LONG).show();
                    // Adicionamos um pequeno delay para o usuário conseguir ler o snackbar antes da tela fechar
                    new android.os.Handler().postDelayed(this::finish, 1500); // 1.5 segundos
                })
                .addOnFailureListener(e -> Snackbar.make(findViewById(android.R.id.content), "Erro ao atualizar dados.", Snackbar.LENGTH_LONG).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
