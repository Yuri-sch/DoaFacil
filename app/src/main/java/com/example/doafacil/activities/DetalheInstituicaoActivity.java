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

public class DetalheInstituicaoActivity extends AppCompatActivity {

    private ImageView imgFoto;
    private TextView tvNome, tvDescricao, tvEndereco;
    private LinearLayout layoutContatos, layoutItens;
    private DatabaseReference instituicaoRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_instituicao);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imgFoto = findViewById(R.id.imgFotoDetalhe);
        tvNome = findViewById(R.id.tvNomeDetalhe);
        tvDescricao = findViewById(R.id.tvDescricaoDetalhe);
        tvEndereco = findViewById(R.id.tvEnderecoDetalhe);
        layoutContatos = findViewById(R.id.layoutContatos);
        layoutItens = findViewById(R.id.layoutItens);

        String uid = getIntent().getStringExtra("INSTITUICAO_UID");
        if (uid == null) {
            Toast.makeText(this, "Erro: Instituição não encontrada.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        instituicaoRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pj").child(uid);
        loadInstituicaoData();
    }

    private void loadInstituicaoData() {
        instituicaoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UsuarioPJ instituicao = snapshot.getValue(UsuarioPJ.class);
                if (instituicao != null) {
                    getSupportActionBar().setTitle(instituicao.getNome());
                    tvNome.setText(instituicao.getNome());
                    tvDescricao.setText(instituicao.getDescricao());

                    String enderecoCompleto = String.format("Endereço: %s, %s - %s, %s",
                            instituicao.getRua(), instituicao.getNumero(), instituicao.getCidade(), instituicao.getEstado());
                    tvEndereco.setText(enderecoCompleto);

                    // Carregar foto
                    if (instituicao.getFotoUrl() != null && !instituicao.getFotoUrl().isEmpty()) {
                        Glide.with(DetalheInstituicaoActivity.this).load(instituicao.getFotoUrl()).into(imgFoto);
                    } else if (instituicao.getFotoBase64() != null && !instituicao.getFotoBase64().isEmpty()){
                        // Lógica para carregar de Base64 se existir
                        byte[] decodedString = android.util.Base64.decode(instituicao.getFotoBase64(), android.util.Base64.DEFAULT);
                        Glide.with(DetalheInstituicaoActivity.this).load(decodedString).into(imgFoto);
                    }

                    // Carregar contatos
                    if (instituicao.getContatos() != null) {
                        for (String contato : instituicao.getContatos().values()) {
                            addTextViewToList(layoutContatos, contato);
                        }
                    }

                    // Carregar itens para doação
                    if (snapshot.hasChild("objetos_doacao")) {
                        for (DataSnapshot itemSnapshot : snapshot.child("objetos_doacao").getChildren()) {
                            ObjetoDoacao objeto = itemSnapshot.getValue(ObjetoDoacao.class);
                            if (objeto != null) {
                                addTextViewToList(layoutItens, "• " + objeto.getNome() + " (" + objeto.getCategoria() + ")");
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetalheInstituicaoActivity.this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método auxiliar para adicionar TextViews dinamicamente
    private void addTextViewToList(LinearLayout layout, String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(16f);
        textView.setPadding(0, 4, 0, 4);
        layout.addView(textView);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}