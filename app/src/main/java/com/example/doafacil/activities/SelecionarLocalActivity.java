package com.example.doafacil.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;

import com.example.doafacil.R;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class SelecionarLocalActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng localSelecionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_local);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.btnConfirmarLocal).setOnClickListener(v -> {
            if (localSelecionado != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", localSelecionado.latitude);
                resultIntent.putExtra("longitude", localSelecionado.longitude);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Posição inicial (ex: Brasil)
        LatLng centroBrasil = new LatLng(-15.77972, -47.92972);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centroBrasil, 4f));

        // Clique no mapa para selecionar ponto
        mMap.setOnMapClickListener(latLng -> {
            localSelecionado = latLng;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Local selecionado"));
        });
    }
}

