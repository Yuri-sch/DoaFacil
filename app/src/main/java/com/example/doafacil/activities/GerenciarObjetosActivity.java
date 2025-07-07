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

// Activity para a instituição gerenciar os itens que necessita para doação.
public class GerenciarObjetosActivity extends AppCompatActivity {

    // Declaração das referências do Firebase e componentes de UI.
    private DatabaseReference objetosRef; // Referência para o nó de "objetos_doacao" do usuário.
    private DatabaseReference categoriasRef; // Referência para o nó de "categorias" geral.
    private RecyclerView rvObjetosDoacao; // Lista para exibir os objetos.
    private ObjetosDoacaoAdapter adapter; // Adapter da lista.
    private List<ObjetoDoacao> objetoList = new ArrayList<>(); // Lista de objetos de doação.
    private List<String> categoriasList = new ArrayList<>(); // Lista de categorias disponíveis.

    // Metodo chamado na criação da Activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_objetos);

        // Configuração da Toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Verifica a autenticação do usuário.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Define as referências para os nós do Firebase.
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        objetosRef = database.child("usuarios").child("pj").child(currentUser.getUid()).child("objetos_doacao");
        categoriasRef = database.child("categorias");

        // Configuração inicial dos componentes e dados.
        rvObjetosDoacao = findViewById(R.id.rvObjetosDoacao);
        setupRecyclerView(); // Configura o RecyclerView e o Adapter.
        loadCategoriasData(); // Carrega a lista de categorias do Firebase.
        attachDatabaseReadListener(); // Anexa o listener que atualiza a lista em tempo real.

        // Configura o botão flutuante para adicionar um novo objeto.
        FloatingActionButton fab = findViewById(R.id.fabAdicionarObjeto);
        fab.setOnClickListener(view -> showAddObjetoDialog(null)); // `null` indica que é um novo objeto.
    }

    // Configura o RecyclerView, seu LayoutManager e o Adapter.
    private void setupRecyclerView() {
        rvObjetosDoacao.setLayoutManager(new LinearLayoutManager(this));
        // Cria o adapter com a lista e o listener para os eventos de clique.
        adapter = new ObjetosDoacaoAdapter(objetoList, new ObjetosDoacaoAdapter.OnItemListener() {
            @Override
            public void onEditClick(ObjetoDoacao objeto) {
                showAddObjetoDialog(objeto); // Chama o dialog no modo de edição.
            }

            @Override
            public void onDeleteClick(ObjetoDoacao objeto) {
                // Exibe um dialog de confirmação antes de excluir.
                new AlertDialog.Builder(GerenciarObjetosActivity.this)
                        .setTitle("Excluir Objeto")
                        .setMessage("Tem certeza que deseja excluir o objeto '" + objeto.getNome() + "'?")
                        .setPositiveButton("Sim", (dialog, which) ->
                                objetosRef.child(objeto.getId()).removeValue() // Remove o objeto do Firebase.
                                        .addOnSuccessListener(aVoid -> Toast.makeText(GerenciarObjetosActivity.this, "Objeto excluído!", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Toast.makeText(GerenciarObjetosActivity.this, "Erro ao excluir.", Toast.LENGTH_SHORT).show()))
                        .setNegativeButton("Não", null)
                        .show();
            }
        });
        rvObjetosDoacao.setAdapter(adapter); // Associa o adapter ao RecyclerView.
    }

    // Anexa o ValueEventListener que escuta por mudanças nos dados dos objetos para atualização em tempo real.
    private void attachDatabaseReadListener() {
        objetosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                objetoList.clear(); // Limpa a lista antiga antes de popular com os novos dados.
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    ObjetoDoacao objeto = itemSnapshot.getValue(ObjetoDoacao.class);
                    if (objeto != null) {
                        objetoList.add(objeto); // Adiciona os itens novos à lista.
                    }
                }
                adapter.notifyDataSetChanged(); // Notifica o adapter que os dados mudaram para redesenhar a lista.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GerenciarObjetosActivity.this, "Erro ao carregar objetos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Carrega a lista de categorias do Firebase para usar no Spinner do dialog.
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

    // Exibe o diálogo para adicionar ou editar um objeto de doação.
    private void showAddObjetoDialog(final ObjetoDoacao objetoParaEditar) {
        // Verifica se as categorias já foram carregadas.
        if (categoriasList.isEmpty()) {
            Toast.makeText(this, "Carregando categorias, tente novamente em instantes.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_objeto, null); // Infla o layout do dialog.
        builder.setView(dialogView);

        // Associa os componentes de UI do dialog.
        final EditText edtNomeObjeto = dialogView.findViewById(R.id.edtNomeObjeto);
        final EditText edtDescricaoObjeto = dialogView.findViewById(R.id.edtDescricaoObjeto);
        final Spinner spinnerCategoria = dialogView.findViewById(R.id.spinnerCategoria);

        // Configura o adapter para o Spinner de categorias.
        ArrayAdapter<String> categoriaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriasList);
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(categoriaAdapter);

        // Se 'objetoParaEditar' não for nulo, preenche o dialog com os dados existentes.
        if (objetoParaEditar != null) {
            builder.setTitle("Editar Objeto");
            edtNomeObjeto.setText(objetoParaEditar.getNome());
            edtDescricaoObjeto.setText(objetoParaEditar.getDescricao());
            int spinnerPosition = categoriaAdapter.getPosition(objetoParaEditar.getCategoria());
            spinnerCategoria.setSelection(spinnerPosition);
        } else {
            builder.setTitle("Adicionar Novo Objeto");
        }

        // Define a ação do botão "Salvar".
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String nome = edtNomeObjeto.getText().toString().trim();
            String descricao = edtDescricaoObjeto.getText().toString().trim();
            String categoria = spinnerCategoria.getSelectedItem().toString();

            if (TextUtils.isEmpty(nome)) { // Validação do nome do objeto.
                Toast.makeText(this, "O nome do objeto é obrigatório.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Se for edição, usa o ID existente; se for adição, cria um novo ID.
            String objetoId = (objetoParaEditar != null) ? objetoParaEditar.getId() : objetosRef.push().getKey();
            if (objetoId == null) return;

            // Cria o objeto e o salva (ou atualiza) no Firebase.
            ObjetoDoacao novoObjeto = new ObjetoDoacao(objetoId, nome, descricao, categoria);
            objetosRef.child(objetoId).setValue(novoObjeto)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Objeto salvo!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar objeto.", Toast.LENGTH_SHORT).show());
        });

        // Define a ação do botão "Cancelar".
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.create().show(); // Cria e exibe o diálogo.
    }

    // Lida com o clique no botão "voltar" da Toolbar.
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
