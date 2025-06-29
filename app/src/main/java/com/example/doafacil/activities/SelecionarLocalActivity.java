package com.example.doafacil.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.doafacil.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SelecionarLocalActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnConfirmar;
    private Marker marcador;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_local);

        btnConfirmar = findViewById(R.id.btnConfirmarLocal);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnConfirmar.setOnClickListener(v -> {
            if (marcador != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", marcador.getPosition().latitude);
                resultIntent.putExtra("longitude", marcador.getPosition().longitude);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Selecione um local no mapa.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Verifica se recebeu uma localização existente da tela anterior
        if (getIntent().hasExtra("latitude_atual") && getIntent().hasExtra("longitude_atual")) {
            double lat = getIntent().getDoubleExtra("latitude_atual", 0);
            double lon = getIntent().getDoubleExtra("longitude_atual", 0);
            LatLng localizacaoExistente = new LatLng(lat, lon);

            // Move a câmera para o local existente e adiciona um marcador
            adicionarMarcador(localizacaoExistente);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacaoExistente, 17f));
        } else {
            // Se não, busca a localização atual do dispositivo
            buscarLocalizacaoAtual();
        }

        mMap.setOnMapClickListener(latLng -> {
            adicionarMarcador(latLng);
        });
    }

    private void buscarLocalizacaoAtual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissão de localização não concedida.", Toast.LENGTH_LONG).show();
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng atual = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(atual, 15f));
            } else {
                Toast.makeText(this, "Não foi possível obter a localização atual.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para adicionar ou mover o marcador
    private void adicionarMarcador(LatLng latLng) {
        if (marcador == null) {
            // Se não existe marcador, cria um novo
            marcador = mMap.addMarker(new MarkerOptions().position(latLng).title("Local Selecionado"));
        } else {
            // Se já existe, apenas move sua posição
            marcador.setPosition(latLng);
        }
    }
}



