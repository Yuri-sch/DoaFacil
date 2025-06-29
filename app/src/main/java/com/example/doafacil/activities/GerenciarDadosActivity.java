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

public class GerenciarDadosActivity extends AppCompatActivity {

    // Campos de texto
    private EditText edtNome, edtCnpj, edtTelefone, edtDescricao;
    private EditText edtEstado, edtCidade, edtRua, edtNumero;
    private Button btnSalvarDados, btnAlterarLocal;

    // Referências do Firebase
    private DatabaseReference userRef;

    // Componentes do Mapa
    private GoogleMap mMapPreview;
    private FragmentContainerView mapPreviewContainer;
    private LatLng localSelecionado;

    // Launcher para a activity de seleção de local
    private final ActivityResultLauncher<Intent> mapResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    double latitude = result.getData().getDoubleExtra("latitude", 0);
                    double longitude = result.getData().getDoubleExtra("longitude", 0);
                    localSelecionado = new LatLng(latitude, longitude);
                    updateMapPreview();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_dados);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não encontrado. Faça login novamente.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pj").child(currentUser.getUid());

        initViews();
        setupMap();
        loadUserData();

        btnAlterarLocal.setOnClickListener(v -> {
            Intent intent = new Intent(GerenciarDadosActivity.this, SelecionarLocalActivity.class);
            // Envia a localização atual para a próxima tela, se ela existir
            if (localSelecionado != null) {
                intent.putExtra("latitude_atual", localSelecionado.latitude);
                intent.putExtra("longitude_atual", localSelecionado.longitude);
            }
            mapResultLauncher.launch(intent);
        });

        btnSalvarDados.setOnClickListener(v -> saveUserData());
    }

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

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapPreview);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMapPreview = googleMap;
                mMapPreview.getUiSettings().setAllGesturesEnabled(false); // Apenas visualização

                // Se os dados do usuário (e a localização) já foram carregados antes do mapa,
                // atualiza o preview agora.
                if (localSelecionado != null) {
                    updateMapPreview();
                }
            });
        }
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UsuarioPJ usuario = snapshot.getValue(UsuarioPJ.class);
                if (usuario != null) {
                    // ... (preenchimento dos outros campos de texto continua igual)
                    edtNome.setText(usuario.getNome());
                    edtCnpj.setText(usuario.getCnpj());
                    edtTelefone.setText(usuario.getTelefone());
                    edtDescricao.setText(usuario.getDescricao());
                    edtEstado.setText(usuario.getEstado());
                    edtCidade.setText(usuario.getCidade());
                    edtRua.setText(usuario.getRua());
                    edtNumero.setText(usuario.getNumero());

                    if (usuario.getLatitude() != 0 && usuario.getLongitude() != 0) {
                        localSelecionado = new LatLng(usuario.getLatitude(), usuario.getLongitude());

                        // Se o mapa já estiver pronto, atualiza o preview.
                        // Se não, o `setupMap` vai chamar a atualização quando ficar pronto.
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

    private void updateMapPreview() {
        if (mMapPreview != null && localSelecionado != null) {
            mapPreviewContainer.setVisibility(View.VISIBLE); // Mostra o mapa
            mMapPreview.clear();
            mMapPreview.addMarker(new MarkerOptions().position(localSelecionado).title("Localização Atual"));
            mMapPreview.moveCamera(CameraUpdateFactory.newLatLngZoom(localSelecionado, 16f));
        }
    }

    private void saveUserData() {
        if (TextUtils.isEmpty(edtNome.getText().toString())) {
            edtNome.setError("O nome é obrigatório");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("nome", edtNome.getText().toString().trim());
        updates.put("telefone", edtTelefone.getText().toString().trim());
        updates.put("descricao", edtDescricao.getText().toString().trim());
        updates.put("estado", edtEstado.getText().toString().trim());
        updates.put("cidade", edtCidade.getText().toString().trim());
        updates.put("rua", edtRua.getText().toString().trim());
        updates.put("numero", edtNumero.getText().toString().trim());

        // Adiciona a latitude e longitude ao Map para salvar
        if (localSelecionado != null) {
            updates.put("latitude", localSelecionado.latitude);
            updates.put("longitude", localSelecionado.longitude);
        }

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(GerenciarDadosActivity.this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(GerenciarDadosActivity.this, "Erro ao atualizar dados.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
