package com.example.doafacil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import com.example.doafacil.R;
import com.example.doafacil.models.UsuarioPJ;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

public class CadastroPJActivity extends AppCompatActivity {

    // (As declarações de variáveis permanecem as mesmas)
    private LinearLayout step1, step2, step3, step4;
    private EditText edtNome, edtTelefone, edtCnpj;
    private EditText edtDescricao;
    private EditText edtEstado, edtCidade, edtRua, edtNumero;
    private EditText edtEmail, edtSenha;
    private Button btnAbrirMapa;
    private FragmentContainerView mapPreviewContainer; // Agora é FragmentContainerView
    private LinearProgressIndicator progressIndicator;
    private FirebaseAuth mAuth;
    private GoogleMap mMapPreview;
    private LatLng localSelecionado;
    private TextInputLayout layoutSenha;

    private String nome, telefone, cnpj, descricao;
    private String estado, cidade, rua, numero;
    private String email, senha;

    private final ActivityResultLauncher<Intent> mapResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    double latitude = result.getData().getDoubleExtra("latitude", 0);
                    double longitude = result.getData().getDoubleExtra("longitude", 0);
                    localSelecionado = new LatLng(latitude, longitude);

                    if (mMapPreview != null) {
                        updateMapPreview();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Esta linha irá falhar se o XML estiver incorreto.
        setContentView(R.layout.activity_cadastro_pj);

        mAuth = FirebaseAuth.getInstance();

        // Associa as variáveis aos componentes do XML
        step1 = findViewById(R.id.step1);
        step2 = findViewById(R.id.step2);
        step3 = findViewById(R.id.step3);
        step4 = findViewById(R.id.step4);

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
        btnAbrirMapa = findViewById(R.id.btnAbrirMapa);
        mapPreviewContainer = findViewById(R.id.mapPreview);
        progressIndicator = findViewById(R.id.progress_indicator);
        layoutSenha = findViewById(R.id.layoutSenha);

        // Configuração do mapa de preview
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapPreview);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMapPreview = googleMap;
                mMapPreview.getUiSettings().setAllGesturesEnabled(false);
            });
        }

        // Ação do botão para abrir o mapa de seleção
        // Se `btnAbrirMapa` for nulo aqui, o app irá quebrar.
        btnAbrirMapa.setOnClickListener(v -> {
            Intent intent = new Intent(CadastroPJActivity.this, SelecionarLocalActivity.class);
            mapResultLauncher.launch(intent);
        });

        setupNavigationButtons();
    }

    private void updateMapPreview(){
        if(mMapPreview != null && localSelecionado != null){
            mapPreviewContainer.setVisibility(View.VISIBLE);
            mMapPreview.clear();
            mMapPreview.addMarker(new MarkerOptions().position(localSelecionado).title("Local Selecionado"));
            mMapPreview.moveCamera(CameraUpdateFactory.newLatLngZoom(localSelecionado, 16f));
        }
    }

    private void setupNavigationButtons(){
        findViewById(R.id.btnProximo1).setOnClickListener(v -> {
            nome = edtNome.getText().toString().trim();
            telefone = edtTelefone.getText().toString().trim();
            String cnpjInput = edtCnpj.getText().toString();
            boolean erro = false;
            if (TextUtils.isEmpty(nome)) {
                edtNome.setError("Informe o nome");
                erro = true;
            }
            if (TextUtils.isEmpty(telefone)) {
                edtTelefone.setError("Informe o telefone");
                erro = true;
            }

            // --- VALIDAÇÃO COMPLETA DO CNPJ ---
            if (!isCnpjValido(cnpjInput)) {
                edtCnpj.setError("O CNPJ informado não é válido.");
                erro = true;
            }

            if (erro) return;

            // Se a validação passou, armazena apenas os números
            cnpj = cnpjInput.replaceAll("[^0-9]", "");
            step1.setVisibility(View.GONE);
            step2.setVisibility(View.VISIBLE);
            progressIndicator.setProgress(2, true);
        });

        findViewById(R.id.btnProximo2).setOnClickListener(v -> {
            descricao = edtDescricao.getText().toString().trim();
            if (TextUtils.isEmpty(descricao)) {
                edtDescricao.setError("Informe a descrição");
                return;
            }
            step2.setVisibility(View.GONE);
            step3.setVisibility(View.VISIBLE);
            progressIndicator.setProgress(3, true);
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
                Snackbar.make(findViewById(android.R.id.content), "Selecione a localização no mapa", Snackbar.LENGTH_LONG).show();
                erro = true;
            }
            if (erro) return;
            step3.setVisibility(View.GONE);
            step4.setVisibility(View.VISIBLE);
            progressIndicator.setProgress(4, true);
        });

        findViewById(R.id.btnVoltar1).setOnClickListener(v -> {
            step2.setVisibility(View.GONE);
            step1.setVisibility(View.VISIBLE);
            progressIndicator.setProgress(1, true);
        });

        findViewById(R.id.btnVoltar2).setOnClickListener(v -> {
            step3.setVisibility(View.GONE);
            step2.setVisibility(View.VISIBLE);
            progressIndicator.setProgress(2, true);
        });

        findViewById(R.id.btnVoltar3).setOnClickListener(v -> {
            step4.setVisibility(View.GONE);
            step3.setVisibility(View.VISIBLE);
            progressIndicator.setProgress(3, true);
        });

        // --- CORREÇÃO AQUI ---
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
                                    .addOnCompleteListener(taskDb -> {
                                        if (taskDb.isSuccessful()) {
                                            Intent intent = new Intent(CadastroPJActivity.this, LoginActivity.class);
                                            intent.putExtra("cadastro_sucesso", true);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Snackbar.make(findViewById(android.R.id.content), "Erro ao salvar dados no banco.", Snackbar.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            // LÓGICA DE ERRO RESTAURADA E CORRIGIDA
                            String erroFB = "";
                            // Garante que a exceção não é nula antes de pegar a mensagem
                            if (task.getException() != null) {
                                erroFB = task.getException().getMessage();
                            }

                            // Verifica o tipo de erro e mostra a mensagem apropriada
                            if (erroFB != null) {
                                if (erroFB.contains("email address is badly formatted")) {
                                    edtEmail.setError("O formato do e-mail é inválido");
                                } else if (erroFB.contains("The email address is already in use")) {
                                    edtEmail.setError("Este endereço de e-mail já está em uso");
                                } else if (erroFB.contains("Password")) {
                                    layoutSenha.setError("A senha deve ter no mínimo 6 caracteres, 1 número, 1 letra maíuscula e 1 letra miníscula");
                                } else {
                                    // Para outros erros do Firebase, mostra a mensagem completa
                                    Snackbar.make(findViewById(android.R.id.content), "Erro: " + erroFB, Snackbar.LENGTH_LONG).show();
                                }
                            } else {
                                // Caso o erro seja nulo por algum motivo
                                Snackbar.make(findViewById(android.R.id.content), "Ocorreu um erro desconhecido no cadastro.", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
        });
    }

    public static boolean isCnpjValido(String cnpj) {
        // Remove caracteres não numéricos
        cnpj = cnpj.replaceAll("[^0-9]", "");

        // 1. Verifica se o tamanho é 14
        if (cnpj.length() != 14) {
            return false;
        }

        // 2. Verifica se todos os dígitos são iguais (ex: 00.000.000/0000-00), o que é inválido
        try {
            Long.parseLong(cnpj);
            if (cnpj.matches("(\\d)\\1{13}")) {
                return false;
            }
        } catch (NumberFormatException e){
            // Caso não seja possível converter para Long, também é inválido
            return false;
        }


        // 3. Cálculo do Primeiro Dígito Verificador
        int soma = 0;
        int peso = 2;
        for (int i = 11; i >= 0; i--) {
            int num = Integer.parseInt(cnpj.substring(i, i + 1));
            soma += num * peso;
            peso++;
            if (peso == 10) {
                peso = 2;
            }
        }

        int resto = soma % 11;
        int digitoVerificador1 = (resto < 2) ? 0 : 11 - resto;

        // 4. Verifica o primeiro dígito
        if (digitoVerificador1 != Integer.parseInt(cnpj.substring(12, 13))) {
            return false;
        }

        // 5. Cálculo do Segundo Dígito Verificador
        soma = 0;
        peso = 2;
        for (int i = 12; i >= 0; i--) {
            int num = Integer.parseInt(cnpj.substring(i, i + 1));
            soma += num * peso;
            peso++;
            if (peso == 10) {
                peso = 2;
            }
        }

        resto = soma % 11;
        int digitoVerificador2 = (resto < 2) ? 0 : 11 - resto;

        // 6. Verifica o segundo dígito e retorna o resultado final
        return digitoVerificador2 == Integer.parseInt(cnpj.substring(13, 14));
    }
}


