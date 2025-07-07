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

// Activity que permite aos usuários filtrar instituições para doação por categoria de item ou por cidade.
public class FiltrarDoacaoActivity extends AppCompatActivity {

    // Declaração dos componentes de UI.
    private Spinner spinnerCategoria; // Dropdown para selecionar a categoria do item.
    private EditText edtCidade;
    private Button btnBuscar; // Botão para iniciar a busca.
    private RecyclerView rvResultado; // Lista para exibir os resultados da busca.
    private ProgressBar progressBar; // Indicador de progresso exibido durante a busca.
    private TextView tvResultadoBusca;

    // Listas e referências de dados.
    private List<String> categoriasList = new ArrayList<>(); // Lista para armazenar as categorias de doação.
    private List<UsuarioPJ> instituicoesEncontradas = new ArrayList<>(); // Lista para armazenar as instituições encontradas no filtro.
    private FiltroInstituicoesAdapter adapter; // Adapter para o RecyclerView.
    private DatabaseReference pjRef, categoriasRef; // Referência para o Firebase.

    // Metodo chamado na criação da Activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtrar_doacao);

        // Configuração da Toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita o botão de voltar.

        // Associação dos componentes de interface com as variáveis.
        spinnerCategoria = findViewById(R.id.spinnerFiltroCategoria);
        edtCidade = findViewById(R.id.edtFiltroCidade);
        btnBuscar = findViewById(R.id.btnBuscar);
        rvResultado = findViewById(R.id.rvResultado);
        progressBar = findViewById(R.id.progressBarBusca);
        tvResultadoBusca = findViewById(R.id.tvResultadoBusca);

        // Define as referências para os nós "pj" e "categorias" no Firebase.
        pjRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pj");
        categoriasRef = FirebaseDatabase.getInstance().getReference("categorias");

        // Configura o RecyclerView e carrega as categorias do Firebase.
        setupRecyclerView();
        loadCategorias();

        // Define o listener do botão de busca.
        btnBuscar.setOnClickListener(v -> buscarInstituicoes());
    }

    // Configura o RecyclerView, seu adapter e o listener de clique.
    private void setupRecyclerView() {
        // Inicializa o adapter, que abrirá a tela de detalhes ao clicar em uma instituição.
        adapter = new FiltroInstituicoesAdapter(instituicoesEncontradas, uid -> {
            Intent intent = new Intent(FiltrarDoacaoActivity.this, DetalheInstituicaoActivity.class);
            intent.putExtra("INSTITUICAO_UID", uid); // Passa o UID da instituição para a próxima tela.
            startActivity(intent);
        });
        rvResultado.setAdapter(adapter);
    }

    // Carrega as categorias de doação do Firebase para preencher o Spinner.
    private void loadCategorias() {
        categoriasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriasList.add("Selecione uma categoria..."); // Opção padrão.
                for (DataSnapshot categoriaSnapshot : snapshot.getChildren()) {
                    categoriasList.add(categoriaSnapshot.getValue(String.class)); // Adiciona cada categoria da base.
                }
                // Cria e define o adapter para o Spinner.
                ArrayAdapter<String> adapter = new ArrayAdapter<>(FiltrarDoacaoActivity.this,
                        android.R.layout.simple_spinner_item, categoriasList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategoria.setAdapter(adapter);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {} // Metodo chamado em caso de erro.
        });
    }

    // Executa a lógica de busca de instituições com base nos filtros selecionados.
    private void buscarInstituicoes() {
        String categoriaSelecionada = spinnerCategoria.getSelectedItem().toString();
        String cidadeFiltro = edtCidade.getText().toString().trim();

        // Verifica se um filtro de categoria foi selecionado (posição > 0).
        boolean isCategoriaFiltro = spinnerCategoria.getSelectedItemPosition() > 0;

        // Valida se pelo menos um filtro foi preenchido.
        if (!isCategoriaFiltro && cidadeFiltro.isEmpty()) {
            Toast.makeText(this, "Por favor, selecione uma categoria ou digite uma cidade.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepara a UI para a busca.
        progressBar.setVisibility(View.VISIBLE);
        tvResultadoBusca.setVisibility(View.GONE);
        instituicoesEncontradas.clear();
        adapter.notifyDataSetChanged();

        // Executa a busca no nó de instituições (PJ).
        pjRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UsuarioPJ instituicao = snapshot.getValue(UsuarioPJ.class);
                    if (instituicao == null) continue; // Pula para o próximo se a instituição for nula.

                    // Verifica se a cidade da instituição corresponde ao filtro ou se o filtro de cidade está vazio.
                    boolean cidadeMatch = cidadeFiltro.isEmpty() || instituicao.getCidade().equalsIgnoreCase(cidadeFiltro);

                    if (!isCategoriaFiltro) { // Se o filtro é apenas por cidade.
                        if (cidadeMatch) {
                            instituicoesEncontradas.add(instituicao);
                        }
                    } else { // Se o filtro é por categoria (e opcionalmente por cidade).
                        DataSnapshot objetosSnapshot = snapshot.child("objetos_doacao");
                        for (DataSnapshot itemSnapshot : objetosSnapshot.getChildren()) {
                            ObjetoDoacao objeto = itemSnapshot.getValue(ObjetoDoacao.class);
                            // Verifica se a instituição precisa de um item da categoria selecionada.
                            if (objeto != null && objeto.getCategoria().equals(categoriaSelecionada)) {
                                if (cidadeMatch) {
                                    // Adiciona a instituição apenas uma vez e vai para a próxima.
                                    instituicoesEncontradas.add(instituicao);
                                    break;
                                }
                            }
                        }
                    }
                }
                // Atualiza a UI após a busca.
                progressBar.setVisibility(View.GONE);
                tvResultadoBusca.setVisibility(View.VISIBLE);
                if (instituicoesEncontradas.isEmpty()) {
                    tvResultadoBusca.setText("Nenhuma instituição encontrada com esses critérios.");
                } else {
                    tvResultadoBusca.setText(instituicoesEncontradas.size() + " instituição(ões) encontrada(s):");
                }
                adapter.notifyDataSetChanged(); // Atualiza a lista de resultados.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Lida com erros durante a busca.
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FiltrarDoacaoActivity.this, "Erro ao buscar.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Lida com o clique no botão de voltar da Toolbar.
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Igual ao botão de voltar do dispositivo.
        return true;
    }
}
