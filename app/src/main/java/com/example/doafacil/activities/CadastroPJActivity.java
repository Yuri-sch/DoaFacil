package com.example.doafacil.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.doafacil.R;
import com.example.doafacil.models.UsuarioPJ;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class CadastroPJActivity extends AppCompatActivity {

    private LinearLayout step1, step2, step3, step4;
    private EditText edtNome, edtTelefone, edtCnpj;
    private EditText edtDescricao;
    private EditText edtEstado, edtCidade, edtRua, edtNumero;
    private EditText edtEmail, edtSenha;

    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private LatLng localSelecionado;
    private FusedLocationProviderClient locationClient;

    private String nome, telefone, cnpj, descricao;
    private String estado, cidade, rua, numero;
    private String email, senha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_pj);

        mAuth = FirebaseAuth.getInstance();
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Etapas
        step1 = findViewById(R.id.step1);
        step2 = findViewById(R.id.step2);
        step3 = findViewById(R.id.step3);
        step4 = findViewById(R.id.step4);

        // Campos
        edtNome = findViewById(R.id.edtNome);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtCnpj = findViewById(R.id.edtCnpj);
        edtDescricao = findViewById(R.id.edtDescricao);
        edtEstado = findViewById(R.id.edtEstado);
        edtCidade = findViewById(R.id.edtCidade);
        edtRua = findViewById(R.id.edtRua);
        edtNumero = findViewById(R.id.edtNumero);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);

        // Mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                }
                mMap.setMyLocationEnabled(true);
                locationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        localSelecionado = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localSelecionado, 17f));
                        mMap.addMarker(new MarkerOptions().position(localSelecionado).title("Local atual"));
                    }
                });

                mMap.setOnMapClickListener(latLng -> {
                    localSelecionado = latLng;
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Local selecionado"));
                });
            });
        }

        // Botões de navegação entre etapas
        findViewById(R.id.btnProximo1).setOnClickListener(v -> {
            nome = edtNome.getText().toString().trim();
            telefone = edtTelefone.getText().toString().trim();
            cnpj = edtCnpj.getText().toString().trim();
            boolean erro = false;
            if (TextUtils.isEmpty(nome)) { edtNome.setError("Informe o nome"); erro = true; }
            if (TextUtils.isEmpty(telefone)) { edtTelefone.setError("Informe o telefone"); erro = true; }
            if (TextUtils.isEmpty(cnpj)) { edtCnpj.setError("Informe o CNPJ"); erro = true; }
            if (erro) return;
            step1.setVisibility(View.GONE);
            step2.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.btnProximo2).setOnClickListener(v -> {
            descricao = edtDescricao.getText().toString().trim();
            if (TextUtils.isEmpty(descricao)) {
                edtDescricao.setError("Informe a descrição");
                return;
            }
            step2.setVisibility(View.GONE);
            step3.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.btnProximo3).setOnClickListener(v -> {
            estado = edtEstado.getText().toString().trim();
            cidade = edtCidade.getText().toString().trim();
            rua = edtRua.getText().toString().trim();
            numero = edtNumero.getText().toString().trim();
            boolean erro = false;
            if (TextUtils.isEmpty(estado)) { edtEstado.setError("Informe o estado"); erro = true; }
            if (TextUtils.isEmpty(cidade)) { edtCidade.setError("Informe a cidade"); erro = true; }
            if (TextUtils.isEmpty(rua)) { edtRua.setError("Informe a rua"); erro = true; }
            if (TextUtils.isEmpty(numero)) { edtNumero.setError("Informe o número"); erro = true; }
            if (localSelecionado == null) {
                Snackbar.make(findViewById(R.id.rootLayout), "Selecione a localização no mapa", Snackbar.LENGTH_LONG).show();
                erro = true;
            }
            if (erro) return;
            step3.setVisibility(View.GONE);
            step4.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.btnVoltar1).setOnClickListener(v -> {
            step2.setVisibility(View.GONE);
            step1.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.btnVoltar2).setOnClickListener(v -> {
            step3.setVisibility(View.GONE);
            step2.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.btnVoltar3).setOnClickListener(v -> {
            step4.setVisibility(View.GONE);
            step3.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.btnCadastrar).setOnClickListener(v -> {
            email = edtEmail.getText().toString().trim();
            senha = edtSenha.getText().toString().trim();
            boolean erro = false;
            if (TextUtils.isEmpty(email)) { edtEmail.setError("Informe o e-mail"); erro = true; }
            if (TextUtils.isEmpty(senha)) { edtSenha.setError("Informe a senha"); erro = true; }
            if (erro) return;

            mAuth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();
                            UsuarioPJ usuario = new UsuarioPJ(uid, nome, cnpj, descricao, telefone, estado, cidade, rua, numero, localSelecionado.latitude, localSelecionado.longitude);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("usuarios")
                                    .child("pj")
                                    .child(uid)
                                    .setValue(usuario)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Intent intent = new Intent(CadastroPJActivity.this, LoginActivity.class);
                                            intent.putExtra("cadastro_sucesso", true);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Snackbar.make(findViewById(R.id.rootLayout), "Erro ao salvar dados.", Snackbar.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            String erroFB = task.getException().getMessage();
                            if (erroFB != null) {
                                if (erroFB.contains("email address is badly formatted")) {
                                    edtEmail.setError("E-mail inválido");
                                } else if (erroFB.contains("The email address is already in use")) {
                                    edtEmail.setError("Este e-mail já está em uso");
                                } else if (erroFB.contains("Password should be at least")) {
                                    edtSenha.setError("A senha deve ter pelo menos 6 caracteres");
                                } else {
                                    Snackbar.make(findViewById(R.id.rootLayout), "Erro: " + erroFB, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
        });
    }
}


