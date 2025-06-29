package com.example.doafacil.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.example.doafacil.adapters.ObjetosDoacaoAdapter; // MUDANÇA AQUI
import com.example.doafacil.models.ObjetoDoacao; // MUDANÇA AQUI
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

public class homePJActivity extends AppCompatActivity { // MUDANÇA AQUI

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private DatabaseReference objetosRef; // MUDANÇA AQUI

    private TextView tvNomeInstituicao, tvDescricaoInstituicao;
    private RecyclerView rvObjetosDoacao; // MUDANÇA AQUI
    private ObjetosDoacaoAdapter adapter; // MUDANÇA AQUI
    private List<ObjetoDoacao> objetoList = new ArrayList<>(); // MUDANÇA AQUI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_pj); // MUDANÇA AQUI

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String uid = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pj").child(uid);
        objetosRef = userRef.child("objetos_doacao"); // MUDANÇA AQUI (nome do "nó" no Firebase)

        tvNomeInstituicao = findViewById(R.id.tvNomeInstituicao);
        tvDescricaoInstituicao = findViewById(R.id.tvDescricaoInstituicao);
        rvObjetosDoacao = findViewById(R.id.rvObjetosDoacao); // MUDANÇA AQUI

        setupRecyclerView();
        loadUserData();
        loadObjetosData(); // MUDANÇA AQUI

        FloatingActionButton fab = findViewById(R.id.fabAdicionarObjeto); // MUDANÇA AQUI
        fab.setOnClickListener(view -> showAddObjetoDialog(null)); // MUDANÇA AQUI
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        adapter = new ObjetosDoacaoAdapter(objetoList, new ObjetosDoacaoAdapter.OnItemListener() {
            @Override
            public void onEditClick(ObjetoDoacao objeto) { // MUDANÇA AQUI
                showAddObjetoDialog(objeto); // MUDANÇA AQUI
            }

            @Override
            public void onDeleteClick(ObjetoDoacao objeto) { // MUDANÇA AQUI
                new AlertDialog.Builder(homePJActivity.this)
                        .setTitle("Excluir Objeto")
                        .setMessage("Tem certeza que deseja excluir o objeto '" + objeto.getNome() + "'?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            objetosRef.child(objeto.getId()).removeValue() // MUDANÇA AQUI
                                    .addOnSuccessListener(aVoid -> Toast.makeText(homePJActivity.this, "Objeto excluído!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(homePJActivity.this, "Erro ao excluir.", Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Não", null)
                        .show();
            }
        });
        rvObjetosDoacao.setAdapter(adapter);
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UsuarioPJ usuario = snapshot.getValue(UsuarioPJ.class);
                if (usuario != null) {
                    tvNomeInstituicao.setText(usuario.getNome());
                    tvDescricaoInstituicao.setText(usuario.getDescricao());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(homePJActivity.this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadObjetosData() { // MUDANÇA AQUI
        objetosRef.addValueEventListener(new ValueEventListener() { // MUDANÇA AQUI
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                objetoList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    ObjetoDoacao objeto = itemSnapshot.getValue(ObjetoDoacao.class); // MUDANÇA AQUI
                    if (objeto != null) {
                        objetoList.add(objeto);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(homePJActivity.this, "Erro ao carregar objetos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddObjetoDialog(final ObjetoDoacao objetoParaEditar) { // MUDANÇA AQUI
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        // MUDANÇA AQUI para usar o novo layout de diálogo
        View dialogView = inflater.inflate(R.layout.dialog_add_objeto, null);
        builder.setView(dialogView);

        final EditText edtNomeObjeto = dialogView.findViewById(R.id.edtNomeObjeto);
        final EditText edtDescricaoObjeto = dialogView.findViewById(R.id.edtDescricaoObjeto);

        if (objetoParaEditar != null) {
            builder.setTitle("Editar Objeto");
            edtNomeObjeto.setText(objetoParaEditar.getNome());
            edtDescricaoObjeto.setText(objetoParaEditar.getDescricao());
        } else {
            builder.setTitle("Adicionar Novo Objeto");
        }

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String nome = edtNomeObjeto.getText().toString().trim();
            String descricao = edtDescricaoObjeto.getText().toString().trim();

            if (TextUtils.isEmpty(nome)) {
                Toast.makeText(this, "O nome do objeto é obrigatório.", Toast.LENGTH_SHORT).show();
                return;
            }

            String objetoId;
            if (objetoParaEditar != null) {
                objetoId = objetoParaEditar.getId();
            } else {
                objetoId = objetosRef.push().getKey(); // MUDANÇA AQUI
            }

            if (objetoId == null) {
                Toast.makeText(this, "Erro ao gerar ID para o objeto.", Toast.LENGTH_SHORT).show();
                return;
            }

            ObjetoDoacao novoObjeto = new ObjetoDoacao(objetoId, nome, descricao);
            objetosRef.child(objetoId).setValue(novoObjeto) // MUDANÇA AQUI
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Objeto salvo!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar objeto.", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
