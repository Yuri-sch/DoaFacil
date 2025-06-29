package com.example.doafacil.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doafacil.R;
import com.example.doafacil.adapters.InstituicoesAdapter;
import com.example.doafacil.models.InstituicaoDistancia;
import com.example.doafacil.models.UsuarioPJ;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListaInstituicoesActivity extends AppCompatActivity {

    private RecyclerView rvInstituicoes;
    private ProgressBar progressBar;
    private TextView tvNenhumaInstituicao;
    private InstituicoesAdapter adapter;
    private List<InstituicaoDistancia> instituicoesList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocationAndFetchInstitutions();
                } else {
                    Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    tvNenhumaInstituicao.setText("Permissão de localização é necessária para esta funcionalidade.");
                    tvNenhumaInstituicao.setVisibility(View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_instituicoes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvInstituicoes = findViewById(R.id.rvInstituicoes);
        progressBar = findViewById(R.id.progressBarLista);
        tvNenhumaInstituicao = findViewById(R.id.tvNenhumaInstituicao);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupRecyclerView();
        checkLocationPermission();
    }

    private void setupRecyclerView() {
        adapter = new InstituicoesAdapter(instituicoesList, uid -> {
            Intent intent = new Intent(ListaInstituicoesActivity.this, DetalheInstituicaoActivity.class);
            intent.putExtra("INSTITUICAO_UID", uid);
            startActivity(intent);
        });
        rvInstituicoes.setAdapter(adapter);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndFetchInstitutions();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getCurrentLocationAndFetchInstitutions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // Permissão já foi verificada em checkLocationPermission
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                fetchInstitutions(location);
            } else {
                Toast.makeText(this, "Não foi possível obter a localização. Tente novamente.", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void fetchInstitutions(Location userLocation) {
        DatabaseReference pjRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pj");
        pjRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                instituicoesList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UsuarioPJ instituicao = snapshot.getValue(UsuarioPJ.class);
                    if (instituicao != null && instituicao.getLatitude() != 0) {
                        Location instituicaoLocation = new Location("");
                        instituicaoLocation.setLatitude(instituicao.getLatitude());
                        instituicaoLocation.setLongitude(instituicao.getLongitude());

                        float distancia = userLocation.distanceTo(instituicaoLocation);
                        instituicoesList.add(new InstituicaoDistancia(instituicao, distancia));
                    }
                }

                Collections.sort(instituicoesList); // Ordena a lista pela distância
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                tvNenhumaInstituicao.setVisibility(instituicoesList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ListaInstituicoesActivity.this, "Erro ao buscar instituições.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
