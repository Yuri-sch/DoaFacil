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

// Activity que exibe um mapa para o usuário selecionar uma localização.
public class SelecionarLocalActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Declaração dos componentes do mapa e de UI.
    private GoogleMap mMap; // O objeto do mapa do Google.
    private Button btnConfirmar; // Botão para confirmar a localização selecionada.
    private Marker marcador; // Marcador que indica o local selecionado no mapa.
    private FusedLocationProviderClient fusedLocationProviderClient; // Client para obter a localização do dispositivo.

    // Metodo executado quando a activity é criada.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_local);

        // Associação dos componentes e inicialização do client de localização.
        btnConfirmar = findViewById(R.id.btnConfirmarLocal);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtém o fragmento do mapa e o prepara de forma assíncrona.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Define a ação do botão de confirmar.
        btnConfirmar.setOnClickListener(v -> {
            if (marcador != null) {
                Intent resultIntent = new Intent();
                // Coloca a latitude e longitude do marcador na Intent.
                resultIntent.putExtra("latitude", marcador.getPosition().latitude);
                resultIntent.putExtra("longitude", marcador.getPosition().longitude);
                // Define o resultado como OK e envia a Intent com os dados.
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Selecione um local no mapa.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Callback chamado quando o mapa está pronto para ser usado.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap; // Atribui o objeto do mapa à variável da classe.

        // Verifica se recebeu uma localização existente da activity anterior.
        if (getIntent().hasExtra("latitude_atual") && getIntent().hasExtra("longitude_atual")) {
            double lat = getIntent().getDoubleExtra("latitude_atual", 0);
            double lon = getIntent().getDoubleExtra("longitude_atual", 0);
            LatLng localizacaoExistente = new LatLng(lat, lon); // Cria um LatLng com as coordenadas.

            // Move a câmera para o local existente e adiciona um marcador.
            adicionarMarcador(localizacaoExistente);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacaoExistente, 17f));
        } else {
            // Se não recebeu uma localização, busca a localização atual do dispositivo.
            buscarLocalizacaoAtual();
        }

        // Define um listener para cliques no mapa.
        mMap.setOnMapClickListener(latLng -> {
            adicionarMarcador(latLng); // Adiciona um marcador no local clicado.
        });
    }

    // Busca a última localização conhecida do dispositivo.
    private void buscarLocalizacaoAtual() {
        // Verifica se a permissão de localização foi concedida.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissão de localização não concedida.", Toast.LENGTH_LONG).show();
            return;
        }

        // Tenta obter a última localização.
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) { // Se a localização foi obtida.
                LatLng atual = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(atual, 15f)); // Move a câmera para o local atual.
            } else { // Se não foi possível obter.
                Toast.makeText(this, "Não foi possível obter a localização atual.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adiciona um novo marcador ao mapa ou move o existente.
    private void adicionarMarcador(LatLng latLng) {
        if (marcador == null) {
            marcador = mMap.addMarker(new MarkerOptions().position(latLng).title("Local Selecionado"));
        } else {
            marcador.setPosition(latLng);
        }
    }
}



