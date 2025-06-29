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

public class FotoPJActivity extends AppCompatActivity {

    private ImageView imgFotoInstituicao;
    private Button btnTirarNovaFoto, btnEscolherGaleria;
    private ProgressBar progressBarFoto;

    private DatabaseReference userRef;
    private Uri tempImageUri; // URI temporária apenas para a câmera

    // Launchers
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String> requestGalleryPermissionLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_pj);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        imgFotoInstituicao = findViewById(R.id.imgFotoInstituicao);
        btnTirarNovaFoto = findViewById(R.id.btnTirarNovaFoto);
        btnEscolherGaleria = findViewById(R.id.btnEscolherGaleria);
        progressBarFoto = findViewById(R.id.progressBarFoto);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("usuarios").child("pj").child(currentUser.getUid());

        setupLaunchers();
        loadCurrentImage();

        btnTirarNovaFoto.setOnClickListener(v -> checkCameraPermission());
        btnEscolherGaleria.setOnClickListener(v -> checkGalleryPermission());
    }

    private void setupLaunchers() {
        requestGalleryPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) openGallery();
            else Toast.makeText(this, "Permissão para galeria negada.", Toast.LENGTH_SHORT).show();
        });
        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) openCamera();
            else Toast.makeText(this, "Permissão da câmera negada.", Toast.LENGTH_SHORT).show();
        });

        // Launcher da galeria chama o método de conversão
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                convertUriToBase64AndUpload(uri);
            }
        });

        // Launcher da câmera chama o método de conversão usando a URI temporária
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // A foto foi salva na tempImageUri, agora vamos processá-la
                        convertUriToBase64AndUpload(tempImageUri);
                    }
                });
    }

    // MÉTODO DA CÂMERA CORRIGIDO PARA USAR REALTIME DATABASE
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Erro ao criar arquivo de imagem.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gera um URI seguro para o arquivo temporário usando o FileProvider
        tempImageUri = FileProvider.getUriForFile(this,
                getApplicationContext().getPackageName() + ".provider",
                photoFile);

        // Passa o URI para o app da câmera, que salvará a foto lá
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);
        cameraLauncher.launch(takePictureIntent);
    }

    // MÉTODO DE CONVERSÃO E UPLOAD PARA REALTIME DATABASE
    private void convertUriToBase64AndUpload(Uri uri) {
        try {
            progressBarFoto.setVisibility(View.VISIBLE);
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } else {
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Comprime a imagem para não sobrecarregar o banco de dados
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // Qualidade de 50%
            byte[] byteArray = baos.toByteArray();
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Salva a string Base64 no Realtime Database
            userRef.child("fotoBase64").setValue(base64Image)
                    .addOnSuccessListener(aVoid -> {
                        progressBarFoto.setVisibility(View.GONE);
                        Toast.makeText(this, "Foto atualizada!", Toast.LENGTH_SHORT).show();
                        // Recarrega a imagem na tela
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

    // MÉTODO ATUALIZADO PARA CARREGAR DA STRING BASE64
    private void loadCurrentImage() {
        progressBarFoto.setVisibility(View.VISIBLE);
        userRef.child("fotoBase64").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String base64Image = snapshot.getValue(String.class);
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
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

    // --- Métodos de suporte e permissão (sem alteração) ---
    private void openGallery() {
        pickImageLauncher.launch("image/*");
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void checkGalleryPermission() {
        String permission;
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

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}