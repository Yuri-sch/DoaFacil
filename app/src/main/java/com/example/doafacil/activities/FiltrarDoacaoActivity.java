package com.example.doafacil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doafacil.R;
import com.example.doafacil.adapters.FiltroInstituicoesAdapter;
import com.example.doafacil.models.ObjetoDoacao;
import com.example.doafacil.models.UsuarioPJ;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FiltrarDoacaoActivity extends AppCompatActivity {

    private Spinner spinnerCategoria;
    private EditText edtCidade;
    private Button btnBuscar;
    private RecyclerView rvResultado;
    private ProgressBar progressBar;
    private TextView tvResultadoBusca;

    private List<String> categoriasList = new ArrayList<>();
    private List<UsuarioPJ> instituicoesEncontradas = new ArrayList<>();
    private FiltroInstituicoesAdapter adapter;
    private DatabaseReference pjRef, categoriasRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtrar_doacao);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        spinnerCategoria = findViewById(R.id.spinnerFiltroCategoria);
        edtCidade = findViewById(R.id.edtFiltroCidade);
        btnBuscar = findViewById(R.id.btnBuscar);
        rvResultado = findViewById(R.id.rvResultado);
        progressBar = findViewById(R.id.progressBarBusca);
        tvResultadoBusca = findViewById(R.id.tvResultadoBusca);

        pjRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pj");
        categoriasRef = FirebaseDatabase.getInstance().getReference("categorias");

        setupRecyclerView();
        loadCategorias();

        btnBuscar.setOnClickListener(v -> buscarInstituicoes());
    }

    private void setupRecyclerView() {
        adapter = new FiltroInstituicoesAdapter(instituicoesEncontradas, uid -> {
            Intent intent = new Intent(FiltrarDoacaoActivity.this, DetalheInstituicaoActivity.class);
            intent.putExtra("INSTITUICAO_UID", uid);
            startActivity(intent);
        });
        rvResultado.setAdapter(adapter);
    }

    private void loadCategorias() {
        categoriasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriasList.add("Selecione uma categoria..."); // Primeira opção
                for (DataSnapshot categoriaSnapshot : snapshot.getChildren()) {
                    categoriasList.add(categoriaSnapshot.getValue(String.class));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(FiltrarDoacaoActivity.this,
                        android.R.layout.simple_spinner_item, categoriasList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategoria.setAdapter(adapter);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void buscarInstituicoes() {
        String categoriaSelecionada = spinnerCategoria.getSelectedItem().toString();
        String cidadeFiltro = edtCidade.getText().toString().trim();

        boolean isCategoriaFiltro = spinnerCategoria.getSelectedItemPosition() > 0;

        if (!isCategoriaFiltro && cidadeFiltro.isEmpty()) {
            Toast.makeText(this, "Por favor, selecione uma categoria ou digite uma cidade.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvResultadoBusca.setVisibility(View.GONE);
        instituicoesEncontradas.clear();
        adapter.notifyDataSetChanged();

        pjRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UsuarioPJ instituicao = snapshot.getValue(UsuarioPJ.class);
                    if (instituicao == null) continue;

                    boolean cidadeMatch = cidadeFiltro.isEmpty() || instituicao.getCidade().equalsIgnoreCase(cidadeFiltro);

                    if (!isCategoriaFiltro) { // Filtro apenas por cidade
                        if (cidadeMatch) {
                            instituicoesEncontradas.add(instituicao);
                        }
                    } else { // Filtro por categoria (pode ter cidade ou não)
                        DataSnapshot objetosSnapshot = snapshot.child("objetos_doacao");
                        for (DataSnapshot itemSnapshot : objetosSnapshot.getChildren()) {
                            ObjetoDoacao objeto = itemSnapshot.getValue(ObjetoDoacao.class);
                            if (objeto != null && objeto.getCategoria().equals(categoriaSelecionada)) {
                                if (cidadeMatch) { // Se a cidade também bate (ou está vazia)
                                    instituicoesEncontradas.add(instituicao);
                                    break; // Instituição já adicionada, vai para a próxima
                                }
                            }
                        }
                    }
                }
                progressBar.setVisibility(View.GONE);
                tvResultadoBusca.setVisibility(View.VISIBLE);
                if (instituicoesEncontradas.isEmpty()) {
                    tvResultadoBusca.setText("Nenhuma instituição encontrada com esses critérios.");
                } else {
                    tvResultadoBusca.setText(instituicoesEncontradas.size() + " instituição(ões) encontrada(s):");
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FiltrarDoacaoActivity.this, "Erro ao buscar.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
