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
import com.example.doafacil.models.UsuarioPJ;
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

public class ContatosPJActivity extends AppCompatActivity {

    private DatabaseReference userRef;
    private RecyclerView rvContatos;
    private TextView tvListaVazia;
    private ContatosAdapter adapter;
    private List<String> contatosList = new ArrayList<>();
    private Map<String, String> contatosMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contatos_pj);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pj").child(currentUser.getUid());

        rvContatos = findViewById(R.id.rvContatos);
        tvListaVazia = findViewById(R.id.tvListaVazia);

        setupRecyclerView();
        loadContatos();

        FloatingActionButton fab = findViewById(R.id.fabAdicionarContato);
        fab.setOnClickListener(v -> showAddContatoDialog());
    }

    private void setupRecyclerView() {
        adapter = new ContatosAdapter(contatosList, contato -> showDeleteContatoDialog(contato));
        rvContatos.setAdapter(adapter);
    }

    private void loadContatos() {
        userRef.child("contatos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contatosList.clear();
                if (snapshot.exists()) {
                    contatosMap = (Map<String, String>) snapshot.getValue();
                    if (contatosMap != null) {
                        contatosList.addAll(contatosMap.values());
                    }
                }
                adapter.notifyDataSetChanged();
                tvListaVazia.setVisibility(contatosList.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContatosPJActivity.this, "Erro ao carregar contatos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddContatoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Contato");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("email@exemplo.com ou (51) 99999-9999");
        builder.setView(input);

        builder.setPositiveButton("Adicionar", (dialog, which) -> {
            String contato = input.getText().toString().trim();
            if (!contato.isEmpty()) {
                String key = userRef.child("contatos").push().getKey();
                if (key != null) {
                    userRef.child("contatos").child(key).setValue(contato);
                }
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteContatoDialog(String contato) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Contato")
                .setMessage("Tem certeza que deseja excluir o contato '" + contato + "'?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    String keyToRemove = null;
                    if (contatosMap != null) {
                        for (Map.Entry<String, String> entry : contatosMap.entrySet()) {
                            if (Objects.equals(contato, entry.getValue())) {
                                keyToRemove = entry.getKey();
                                break;
                            }
                        }
                    }
                    if (keyToRemove != null) {
                        userRef.child("contatos").child(keyToRemove).removeValue()
                                .addOnSuccessListener(aVoid -> Toast.makeText(ContatosPJActivity.this, "Contato removido!", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
