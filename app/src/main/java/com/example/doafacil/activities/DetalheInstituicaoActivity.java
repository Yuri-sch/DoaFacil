package com.example.doafacil.activities;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.doafacil.R;
import com.example.doafacil.models.ObjetoDoacao;
import com.example.doafacil.models.UsuarioPJ;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

// Activity que exibe os detalhes completos de uma instituição específica.
public class DetalheInstituicaoActivity extends AppCompatActivity {

    // Declaração dos componentes de interface do usuário (UI).
    private ImageView imgFoto;
    private TextView tvNome, tvDescricao, tvEndereco;
    private LinearLayout layoutContatos, layoutItens; // Layouts para adicionar contatos e itens dinamicamente.
    private DatabaseReference instituicaoRef; // Referência para o nó da instituição no Firebase.

    // Metodo executado quando a activity é criada.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_instituicao);

        // Configuração a Toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita o botão "voltar".

        // Associação das variáveis aos componentes do layout XML.
        imgFoto = findViewById(R.id.imgFotoDetalhe);
        tvNome = findViewById(R.id.tvNomeDetalhe);
        tvDescricao = findViewById(R.id.tvDescricaoDetalhe);
        tvEndereco = findViewById(R.id.tvEnderecoDetalhe);
        layoutContatos = findViewById(R.id.layoutContatos);
        layoutItens = findViewById(R.id.layoutItens);

        // Obtém o UID da instituição passado pela activity anterior.
        String uid = getIntent().getStringExtra("INSTITUICAO_UID");
        if (uid == null) { // Se o UID for nulo, exibe um erro e fecha a activity.
            Toast.makeText(this, "Erro: Instituição não encontrada.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Define a referência do Firebase para a instituição específica.
        instituicaoRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pj").child(uid);
        loadInstituicaoData(); // Chama o metodo para carregar os dados.
    }

    // Carrega os dados da instituição do Firebase e preenche a interface.
    private void loadInstituicaoData() {
        // Adiciona um listener para ler os dados do Firebase uma única vez.
        instituicaoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Converte os dados do Firebase para um objeto UsuarioPJ.
                UsuarioPJ instituicao = snapshot.getValue(UsuarioPJ.class);
                if (instituicao != null) {
                    getSupportActionBar().setTitle(instituicao.getNome()); // Define o título da Toolbar.
                    tvNome.setText(instituicao.getNome()); // Preenche o nome, descrição e endereço.
                    tvDescricao.setText(instituicao.getDescricao());
                    String enderecoCompleto = String.format("Endereço: %s, %s - %s, %s",
                            instituicao.getRua(), instituicao.getNumero(), instituicao.getCidade(), instituicao.getEstado());
                    tvEndereco.setText(enderecoCompleto);

                    // Tenta carregar a foto a partir de uma URL ou de uma string Base64.
                    if (instituicao.getFotoUrl() != null && !instituicao.getFotoUrl().isEmpty()) {
                        Glide.with(DetalheInstituicaoActivity.this).load(instituicao.getFotoUrl()).into(imgFoto);
                    } else if (instituicao.getFotoBase64() != null && !instituicao.getFotoBase64().isEmpty()){
                        byte[] decodedString = android.util.Base64.decode(instituicao.getFotoBase64(), android.util.Base64.DEFAULT);
                        Glide.with(DetalheInstituicaoActivity.this).load(decodedString).into(imgFoto);
                    }

                    // Carrega e exibe os contatos da instituição.
                    if (instituicao.getContatos() != null) {
                        for (String contato : instituicao.getContatos().values()) {
                            addTextViewToList(layoutContatos, contato); // Adiciona cada contato à lista na UI.
                        }
                    }

                    // Carrega e exibe os itens que a instituição precisa para doação.
                    if (snapshot.hasChild("objetos_doacao")) {
                        for (DataSnapshot itemSnapshot : snapshot.child("objetos_doacao").getChildren()) {
                            ObjetoDoacao objeto = itemSnapshot.getValue(ObjetoDoacao.class);
                            if (objeto != null) {
                                // Adiciona cada item e sua categoria à lista na UI.
                                addTextViewToList(layoutItens, "• " + objeto.getNome() + " (" + objeto.getCategoria() + ")");
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Exibe uma mensagem de erro se o carregamento falhar.
                Toast.makeText(DetalheInstituicaoActivity.this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Metodo auxiliar para criar e adicionar TextViews dinamicamente a um layout.
    private void addTextViewToList(LinearLayout layout, String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(16f); // Define o tamanho da fonte.
        textView.setPadding(0, 4, 0, 4); // Define o espaçamento interno.
        layout.addView(textView);
    }

    // Lida com o clique no botão "voltar" da Toolbar.
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Igual ao botão "voltar" do dispositivo.
        return true;
    }
}