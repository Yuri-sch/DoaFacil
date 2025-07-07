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

// Activity que exibe uma lista de instituições ordenadas pela proximidade ao usuário.
public class ListaInstituicoesActivity extends AppCompatActivity {

    // Declaração dos componentes de UI e outros objetos.
    private RecyclerView rvInstituicoes; // Lista para exibir as instituições.
    private ProgressBar progressBar; // Indicador de progresso.
    private TextView tvNenhumaInstituicao; // Texto para quando não há resultados.
    private InstituicoesAdapter adapter; // Adapter para o RecyclerView.
    private List<InstituicaoDistancia> instituicoesList = new ArrayList<>(); // Lista de instituições com suas distâncias.
    private FusedLocationProviderClient fusedLocationClient; // Cliente para obter a localização do dispositivo.

    // Launcher para solicitar a permissão de localização ao usuário.
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) { // Se a permissão for concedida.
                    getCurrentLocationAndFetchInstitutions(); // Obtém a localização e busca as instituições.
                } else { // Se a permissão for negada.
                    Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    tvNenhumaInstituicao.setText("Permissão de localização é necessária para esta funcionalidade.");
                    tvNenhumaInstituicao.setVisibility(View.VISIBLE);
                }
            });

    // Metodo chamado na criação da Activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_instituicoes);

        // Configuração da Toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita o botão "voltar".

        // Associação dos componentes de UI e inicialização do cliente de localização.
        rvInstituicoes = findViewById(R.id.rvInstituicoes);
        progressBar = findViewById(R.id.progressBarLista);
        tvNenhumaInstituicao = findViewById(R.id.tvNenhumaInstituicao);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Configura a lista e inicia o processo de verificação de permissão.
        setupRecyclerView();
        checkLocationPermission();
    }

    // Configura o RecyclerView, seu adapter e o listener de clique.
    private void setupRecyclerView() {
        // Inicializa o adapter. Ao clicar em um item, abre a tela de detalhes da instituição.
        adapter = new InstituicoesAdapter(instituicoesList, uid -> {
            Intent intent = new Intent(ListaInstituicoesActivity.this, DetalheInstituicaoActivity.class);
            intent.putExtra("INSTITUICAO_UID", uid); // Passa o ID da instituição para a próxima tela.
            startActivity(intent);
        });
        rvInstituicoes.setAdapter(adapter);
    }

    // Verifica se o app já possui a permissão de localização.
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndFetchInstitutions(); // Se já tem, busca a localização.
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION); // Se não tem, solicita.
        }
    }

    // Obtém a última localização conhecida do dispositivo.
    private void getCurrentLocationAndFetchInstitutions() {
        // Verifica novamente a permissão antes de tentar obter a localização.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // Retorna se a permissão não foi concedida.
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) { // Se a localização for obtida com sucesso.
                fetchInstitutions(location); // Inicia a busca pelas instituições.
            } else { // Se não for possível obter a localização.
                Toast.makeText(this, "Não foi possível obter a localização. Tente novamente.", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // Busca todas as instituições (PJ) no Firebase e calcula a distância para cada uma.
    private void fetchInstitutions(Location userLocation) {
        DatabaseReference pjRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pj");
        pjRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                instituicoesList.clear(); // Limpa a lista antes de uma nova busca.
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UsuarioPJ instituicao = snapshot.getValue(UsuarioPJ.class);
                    // Verifica se a instituição existe e possui coordenadas válidas.
                    if (instituicao != null && instituicao.getLatitude() != 0) {
                        Location instituicaoLocation = new Location(""); // Cria um objeto Location para a instituição.
                        instituicaoLocation.setLatitude(instituicao.getLatitude());
                        instituicaoLocation.setLongitude(instituicao.getLongitude());

                        float distancia = userLocation.distanceTo(instituicaoLocation); // Calcula a distância em metros.
                        instituicoesList.add(new InstituicaoDistancia(instituicao, distancia)); // Adiciona à lista.
                    }
                }

                Collections.sort(instituicoesList); // Ordena a lista da menor para a maior distância.
                adapter.notifyDataSetChanged(); // Atualiza a UI com os dados ordenados.
                progressBar.setVisibility(View.GONE); // Esconde a barra de progresso.
                tvNenhumaInstituicao.setVisibility(instituicoesList.isEmpty() ? View.VISIBLE : View.GONE); // Mostra ou esconde o texto de lista vazia.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Lida com erros na busca ao Firebase.
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ListaInstituicoesActivity.this, "Erro ao buscar instituições.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Lida com o clique no botão "voltar" da Toolbar.
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Igual ao botão de voltar do dispositivo.
        return true;
    }
}
