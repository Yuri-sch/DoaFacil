package com.example.doafacil.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

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

public class SelecionarLocalActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng localSelecionado;
    private Marker marcadorAtual;
    private FusedLocationProviderClient locationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_local);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnConfirmar = findViewById(R.id.btnConfirmarLocal);
        btnConfirmar.setOnClickListener(v -> {
            if (localSelecionado != null) {
                Intent result = new Intent();
                result.putExtra("latitude", localSelecionado.latitude);
                result.putExtra("longitude", localSelecionado.longitude);
                setResult(RESULT_OK, result);
                finish();
            } else {
                Toast.makeText(this, "Selecione uma localização no mapa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Permissão de localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        mMap.setMyLocationEnabled(true);

        // Obter última localização conhecida do dispositivo
        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng posicaoAtual = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicaoAtual, 17f));
            } else {
                // fallback para o Brasil
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-15.78, -47.93), 4f));
            }
        });

        // Ao clicar no mapa, adiciona marcador
        mMap.setOnMapClickListener(latLng -> {
            localSelecionado = latLng;
            if (marcadorAtual != null) marcadorAtual.remove();
            marcadorAtual = mMap.addMarker(new MarkerOptions().position(latLng).title("Local selecionado"));
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recreate(); // reinicia para aplicar a permissão concedida
        }
    }
}



