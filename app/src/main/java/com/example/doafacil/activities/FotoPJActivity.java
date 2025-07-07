package com.example.doafacil.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.doafacil.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Activity para gerenciar a foto de perfil de uma instituição (PJ).
public class FotoPJActivity extends AppCompatActivity {

    // Declaração dos componentes de interface e variáveis.
    private ImageView imgFotoInstituicao;
    private Button btnTirarNovaFoto, btnEscolherGaleria;
    private ProgressBar progressBarFoto; // Barra de progresso para feedback de carregamento.

    private DatabaseReference userRef; // Referência para o nó do usuário no Firebase.
    private Uri tempImageUri; // Armazena a URI da imagem temporária criada pela câmera.

    // Launchers para lidar com os resultados das activities de permissão e seleção de imagem.
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String> requestGalleryPermissionLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    // Metodo executado quando a activity é criada.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_pj);

        // Configuração da Toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Associação dos componentes de interface com as variáveis.
        imgFotoInstituicao = findViewById(R.id.imgFotoInstituicao);
        btnTirarNovaFoto = findViewById(R.id.btnTirarNovaFoto);
        btnEscolherGaleria = findViewById(R.id.btnEscolherGaleria);
        progressBarFoto = findViewById(R.id.progressBarFoto);

        // Verifica a autenticação do usuário.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Define a referência para o nó do usuário no Firebase.
        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pj").child(currentUser.getUid());

        // Configura os launchers e carrega a imagem atual.
        setupLaunchers();
        loadCurrentImage();

        // Define os listeners de clique para os botões.
        btnTirarNovaFoto.setOnClickListener(v -> checkCameraPermission());
        btnEscolherGaleria.setOnClickListener(v -> checkGalleryPermission());
    }

    // Inicializa todos os ActivityResultLaunchers.
    private void setupLaunchers() {
        // Launcher para pedir permissão da galeria.
        requestGalleryPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) openGallery();
            else Toast.makeText(this, "Permissão para galeria negada.", Toast.LENGTH_SHORT).show();
        });

        // Launcher para pedir permissão da câmera.
        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) openCamera();
            else Toast.makeText(this, "Permissão da câmera negada.", Toast.LENGTH_SHORT).show();
        });

        // Launcher para buscar imagem da galeria e processá-la.
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                convertUriToBase64AndUpload(uri);
            }
        });

        // Launcher para receber o resultado da câmera e processar a foto.
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // A foto foi salva na 'tempImageUri', agora vamos processá-la.
                        convertUriToBase64AndUpload(tempImageUri);
                    }
                });
    }

    // Prepara e abre a intent da câmera.
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile(); // Cria um arquivo temporário para a foto.
        } catch (IOException ex) {
            Toast.makeText(this, "Erro ao criar arquivo de imagem.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gera uma URI segura para o arquivo temporário usando o FileProvider.
        tempImageUri = FileProvider.getUriForFile(this,
                getApplicationContext().getPackageName() + ".provider",
                photoFile);

        // Passa a URI para a câmera, que salvará a foto nesse local.
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);
        cameraLauncher.launch(takePictureIntent);
    }

    // Converte a imagem (de URI) para uma string Base64 e a salva no Firebase.
    private void convertUriToBase64AndUpload(Uri uri) {
        try {
            progressBarFoto.setVisibility(View.VISIBLE);
            Bitmap bitmap;
            // Decodifica a imagem da URI para um Bitmap.
            if (Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } else {
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Comprime a imagem em formato JPEG com 50% de qualidade para reduzir o tamanho.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] byteArray = baos.toByteArray();
            // Converte o array de bytes da imagem para uma string Base64.
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Salva a string Base64 no nó "fotoBase64" do usuário no Realtime Database.
            userRef.child("fotoBase64").setValue(base64Image)
                    .addOnSuccessListener(aVoid -> {
                        progressBarFoto.setVisibility(View.GONE);
                        Toast.makeText(this, "Foto atualizada!", Toast.LENGTH_SHORT).show();
                        // Recarrega a imagem na tela usando a biblioteca Glide.
                        Glide.with(this).load(byteArray).into(imgFotoInstituicao);
                    })
                    .addOnFailureListener(e -> {
                        progressBarFoto.setVisibility(View.GONE);
                        Toast.makeText(this, "Falha ao salvar a foto no banco de dados.", Toast.LENGTH_SHORT).show();
                    });

        } catch (IOException e) {
            progressBarFoto.setVisibility(View.GONE);
            Toast.makeText(this, "Erro ao processar imagem.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Carrega a foto atual do usuário (em Base64) do Firebase.
    private void loadCurrentImage() {
        progressBarFoto.setVisibility(View.VISIBLE);
        userRef.child("fotoBase64").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) { // Verifica se o campo "fotoBase64" existe.
                    String base64Image = snapshot.getValue(String.class);
                    // Decodifica a string Base64 de volta para um array de bytes.
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                    // Usa o Glide para carregar o array de bytes na ImageView.
                    Glide.with(FotoPJActivity.this).load(decodedString).into(imgFotoInstituicao);
                }
                progressBarFoto.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBarFoto.setVisibility(View.GONE);
                Toast.makeText(FotoPJActivity.this, "Erro ao carregar foto.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Abre a galeria de imagens do dispositivo.
    private void openGallery() {
        pickImageLauncher.launch("image/*");
    }

    // Verifica a permissão da câmera antes de abri-la.
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // Verifica a permissão da galeria antes de abri-la.
    private void checkGalleryPermission() {
        String permission;
        // A permissão de leitura de mídia mudou a partir do Android 13 (Tiramisu).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestGalleryPermissionLauncher.launch(permission);
        }
    }

    // Cria um arquivo de imagem temporário para a câmera salvar a foto.
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }

    // Lida com o clique no botão "voltar" da Toolbar.
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}