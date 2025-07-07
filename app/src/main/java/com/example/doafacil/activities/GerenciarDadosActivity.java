package com.example.doafacil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentContainerView;

import com.example.doafacil.R;
import com.example.doafacil.models.UsuarioPJ;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

// Activity para a instituição (PJ) gerenciar seus próprios dados cadastrais.
public class GerenciarDadosActivity extends AppCompatActivity {

    // Declaração dos componentes de UI.
    private EditText edtNome, edtCnpj, edtTelefone, edtDescricao;
    private EditText edtEstado, edtCidade, edtRua, edtNumero;
    private Button btnSalvarDados, btnAlterarLocal;

    // Referência do usuário no Firebase.
    private DatabaseReference userRef;

    // Componentes do Mapa.
    private GoogleMap mMapPreview; // Objeto do mapa para o preview.
    private FragmentContainerView mapPreviewContainer; // Container do fragmento do mapa.
    private LatLng localSelecionado; // Armazena a localização (latitude e longitude).

    // Launcher que aguarda o resultado da activity de seleção de local no mapa.
    private final ActivityResultLauncher<Intent> mapResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Verifica se a activity retornou um resultado positivo e com dados.
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Extrai as coordenadas retornadas.
                    double latitude = result.getData().getDoubleExtra("latitude", 0);
                    double longitude = result.getData().getDoubleExtra("longitude", 0);
                    // Atualiza a variável com a nova localização.
                    localSelecionado = new LatLng(latitude, longitude);
                    updateMapPreview(); // Atualiza a visualização do mapa.
                }
            }
    );

    // Metodo chamado na criação da Activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_dados);

        // Configuração da Toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Verifica se o usuário está logado.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não encontrado. Faça login novamente.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Define a referência para o nó do usuário logado no Firebase.
        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pj").child(currentUser.getUid());

        // Inicializa os componentes, o mapa e carrega os dados.
        initViews();
        setupMap();
        loadUserData();

        // Define o listener para o botão de alterar localização.
        btnAlterarLocal.setOnClickListener(v -> {
            Intent intent = new Intent(GerenciarDadosActivity.this, SelecionarLocalActivity.class);
            // Se já existe uma localização, a envia para a próxima tela para ser o ponto de partida.
            if (localSelecionado != null) {
                intent.putExtra("latitude_atual", localSelecionado.latitude);
                intent.putExtra("longitude_atual", localSelecionado.longitude);
            }
            mapResultLauncher.launch(intent); // Inicia a activity do mapa.
        });

        // Define o listener para o botão de salvar.
        btnSalvarDados.setOnClickListener(v -> saveUserData());
    }

    // Associa as variáveis de interface aos seus respectivos componentes no XML.
    private void initViews() {
        edtNome = findViewById(R.id.edtNomeInstituicao);
        edtCnpj = findViewById(R.id.edtCnpj);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtDescricao = findViewById(R.id.edtDescricao);
        edtEstado = findViewById(R.id.edtEstado);
        edtCidade = findViewById(R.id.edtCidade);
        edtRua = findViewById(R.id.edtRua);
        edtNumero = findViewById(R.id.edtNumero);
        btnSalvarDados = findViewById(R.id.btnSalvarDados);
        btnAlterarLocal = findViewById(R.id.btnAlterarLocal);
        mapPreviewContainer = findViewById(R.id.mapPreview);
    }

    // Configura o fragmento do mapa de preview.
    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapPreview);
        if (mapFragment != null) {
            // Prepara o mapa de forma assíncrona.
            mapFragment.getMapAsync(googleMap -> {
                mMapPreview = googleMap;
                mMapPreview.getUiSettings().setAllGesturesEnabled(false); // Desabilita interações com o mapa de preview.

                // Se a localização já foi carregada, atualiza o mapa.
                if (localSelecionado != null) {
                    updateMapPreview();
                }
            });
        }
    }

    // Carrega os dados da instituição do Firebase e preenche os campos.
    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UsuarioPJ usuario = snapshot.getValue(UsuarioPJ.class);
                if (usuario != null) {
                    // Preenche todos os campos de texto com os dados do usuário.
                    edtNome.setText(usuario.getNome());
                    edtCnpj.setText(usuario.getCnpj());
                    edtTelefone.setText(usuario.getTelefone());
                    edtDescricao.setText(usuario.getDescricao());
                    edtEstado.setText(usuario.getEstado());
                    edtCidade.setText(usuario.getCidade());
                    edtRua.setText(usuario.getRua());
                    edtNumero.setText(usuario.getNumero());

                    // Verifica se existem coordenadas de latitude e longitude.
                    if (usuario.getLatitude() != 0 && usuario.getLongitude() != 0) {
                        localSelecionado = new LatLng(usuario.getLatitude(), usuario.getLongitude());
                        // Se o mapa já estiver pronto, atualiza o preview.
                        if (mMapPreview != null) {
                            updateMapPreview();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GerenciarDadosActivity.this, "Falha ao carregar dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Atualiza a visualização do mapa com a localização selecionada.
    private void updateMapPreview() {
        if (mMapPreview != null && localSelecionado != null) {
            mapPreviewContainer.setVisibility(View.VISIBLE); // Torna o mapa visível.
            mMapPreview.clear(); // Limpa marcadores antigos.
            mMapPreview.addMarker(new MarkerOptions().position(localSelecionado).title("Localização Atual")); // Adiciona novo marcador.
            mMapPreview.moveCamera(CameraUpdateFactory.newLatLngZoom(localSelecionado, 16f)); // Move a câmera para o local.
        }
    }

    // Valida os dados e os salva no Firebase.
    private void saveUserData() {
        // Validação simples para o campo de nome.
        if (TextUtils.isEmpty(edtNome.getText().toString())) {
            edtNome.setError("O nome é obrigatório");
            return;
        }

        // Cria um mapa para armazenar apenas os dados que serão atualizados.
        Map<String, Object> updates = new HashMap<>();
        updates.put("nome", edtNome.getText().toString().trim());
        updates.put("telefone", edtTelefone.getText().toString().trim());
        updates.put("descricao", edtDescricao.getText().toString().trim());
        updates.put("estado", edtEstado.getText().toString().trim());
        updates.put("cidade", edtCidade.getText().toString().trim());
        updates.put("rua", edtRua.getText().toString().trim());
        updates.put("numero", edtNumero.getText().toString().trim());

        // Adiciona as coordenadas ao mapa de atualização, se existirem.
        if (localSelecionado != null) {
            updates.put("latitude", localSelecionado.latitude);
            updates.put("longitude", localSelecionado.longitude);
        }

        // Executa a operação de atualização no Firebase.
        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(GerenciarDadosActivity.this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();
                    finish(); // Fecha a activity em caso de sucesso.
                })
                .addOnFailureListener(e -> Toast.makeText(GerenciarDadosActivity.this, "Erro ao atualizar dados.", Toast.LENGTH_SHORT).show());
    }

    // Lida com o clique no botão "voltar" da Toolbar.
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
