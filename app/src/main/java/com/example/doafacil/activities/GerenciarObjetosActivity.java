package com.example.doafacil.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doafacil.R;
import com.example.doafacil.adapters.ObjetosDoacaoAdapter;
import com.example.doafacil.models.ObjetoDoacao;
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

public class GerenciarObjetosActivity extends AppCompatActivity {

    private DatabaseReference objetosRef;
    private DatabaseReference categoriasRef;
    private RecyclerView rvObjetosDoacao;
    private ObjetosDoacaoAdapter adapter;
    private List<ObjetoDoacao> objetoList = new ArrayList<>();
    private List<String> categoriasList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_objetos);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Referências do Firebase
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        objetosRef = database.child("usuarios").child("pj").child(currentUser.getUid()).child("objetos_doacao");
        categoriasRef = database.child("categorias");

        // Configuração inicial
        rvObjetosDoacao = findViewById(R.id.rvObjetosDoacao);
        setupRecyclerView(); // Configura o RecyclerView e o Adapter
        loadCategoriasData(); // Carrega a lista de categorias do Firebase

        // Anexa o "ouvinte" que atualiza a lista em tempo real
        attachDatabaseReadListener();

        FloatingActionButton fab = findViewById(R.id.fabAdicionarObjeto);
        fab.setOnClickListener(view -> showAddObjetoDialog(null));
    }

    private void setupRecyclerView() {
        rvObjetosDoacao.setLayoutManager(new LinearLayoutManager(this));
        // Cria o adapter com a lista (inicialmente vazia)
        adapter = new ObjetosDoacaoAdapter(objetoList, new ObjetosDoacaoAdapter.OnItemListener() {
            @Override
            public void onEditClick(ObjetoDoacao objeto) {
                showAddObjetoDialog(objeto);
            }

            @Override
            public void onDeleteClick(ObjetoDoacao objeto) {
                // Diálogo de confirmação para exclusão
                new AlertDialog.Builder(GerenciarObjetosActivity.this)
                        .setTitle("Excluir Objeto")
                        .setMessage("Tem certeza que deseja excluir o objeto '" + objeto.getNome() + "'?")
                        .setPositiveButton("Sim", (dialog, which) ->
                                objetosRef.child(objeto.getId()).removeValue()
                                        .addOnSuccessListener(aVoid -> Toast.makeText(GerenciarObjetosActivity.this, "Objeto excluído!", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Toast.makeText(GerenciarObjetosActivity.this, "Erro ao excluir.", Toast.LENGTH_SHORT).show()))
                        .setNegativeButton("Não", null)
                        .show();
            }
        });
        // Liga o adapter ao RecyclerView
        rvObjetosDoacao.setAdapter(adapter);
    }

    /**
     * Anexa o ValueEventListener que escuta por mudanças nos dados dos objetos.
     * Este método é a chave para a atualização em tempo real.
     */
    private void attachDatabaseReadListener() {
        objetosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                objetoList.clear(); // Limpa a lista antiga
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    ObjetoDoacao objeto = itemSnapshot.getValue(ObjetoDoacao.class);
                    if (objeto != null) {
                        objetoList.add(objeto); // Adiciona os itens novos
                    }
                }
                // Notifica o adapter que os dados mudaram, para que ele redesenhe a lista
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GerenciarObjetosActivity.this, "Erro ao carregar objetos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategoriasData() {
        categoriasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriasList.clear();
                for (DataSnapshot categoriaSnapshot : snapshot.getChildren()) {
                    String categoria = categoriaSnapshot.getValue(String.class);
                    if (categoria != null) {
                        categoriasList.add(categoria);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GerenciarObjetosActivity.this, "Erro ao carregar categorias.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddObjetoDialog(final ObjetoDoacao objetoParaEditar) {
        if (categoriasList.isEmpty()) {
            Toast.makeText(this, "Carregando categorias, tente novamente em instantes.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_objeto, null);
        builder.setView(dialogView);

        final EditText edtNomeObjeto = dialogView.findViewById(R.id.edtNomeObjeto);
        final EditText edtDescricaoObjeto = dialogView.findViewById(R.id.edtDescricaoObjeto);
        final Spinner spinnerCategoria = dialogView.findViewById(R.id.spinnerCategoria);

        ArrayAdapter<String> categoriaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriasList);
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(categoriaAdapter);

        if (objetoParaEditar != null) {
            builder.setTitle("Editar Objeto");
            edtNomeObjeto.setText(objetoParaEditar.getNome());
            edtDescricaoObjeto.setText(objetoParaEditar.getDescricao());
            int spinnerPosition = categoriaAdapter.getPosition(objetoParaEditar.getCategoria());
            spinnerCategoria.setSelection(spinnerPosition);
        } else {
            builder.setTitle("Adicionar Novo Objeto");
        }

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String nome = edtNomeObjeto.getText().toString().trim();
            String descricao = edtDescricaoObjeto.getText().toString().trim();
            String categoria = spinnerCategoria.getSelectedItem().toString();

            if (TextUtils.isEmpty(nome)) {
                Toast.makeText(this, "O nome do objeto é obrigatório.", Toast.LENGTH_SHORT).show();
                return;
            }

            String objetoId = (objetoParaEditar != null) ? objetoParaEditar.getId() : objetosRef.push().getKey();
            if (objetoId == null) return;

            ObjetoDoacao novoObjeto = new ObjetoDoacao(objetoId, nome, descricao, categoria);
            objetosRef.child(objetoId).setValue(novoObjeto)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Objeto salvo!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar objeto.", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
