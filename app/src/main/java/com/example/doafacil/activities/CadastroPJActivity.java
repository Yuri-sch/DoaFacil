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

// Activity para o fluxo de cadastro de uma nova instituição (Pessoa Jurídica).
public class CadastroPJActivity extends AppCompatActivity {

    // Declaração dos componentes.
    private LinearLayout step1, step2, step3, step4;
    private EditText edtNome, edtTelefone, edtCnpj;
    private EditText edtDescricao;
    private EditText edtEstado, edtCidade, edtRua, edtNumero;
    private EditText edtEmail, edtSenha;
    private Button btnAbrirMapa;
    private FragmentContainerView mapPreviewContainer; // Container para exibir o preview do mapa.
    private LinearProgressIndicator progressIndicator; // Barra de progresso do formulário.
    private FirebaseAuth mAuth; // Instância do Firebase Authentication.
    private GoogleMap mMapPreview; // Objeto do Google Map para o preview.
    private LatLng localSelecionado; // Armazena as coordenadas do local selecionado no mapa.
    private TextInputLayout layoutSenha; // Layout especial para o campo de senha.

    // Variáveis para armazenar os dados inseridos pelo usuário.
    private String nome, telefone, cnpj, descricao, estado, cidade, rua, numero, email, senha;

    // Prepara o launcher que aguarda o resultado da activity do mapa.
    private final ActivityResultLauncher<Intent> mapResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Verifica se a activity retornou um resultado OK e se há dados.
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Extrai a latitude e longitude retornadas.
                    double latitude = result.getData().getDoubleExtra("latitude", 0);
                    double longitude = result.getData().getDoubleExtra("longitude", 0);
                    // Cria um novo objeto LatLng com as coordenadas.
                    localSelecionado = new LatLng(latitude, longitude);
                    // Se o mapa de preview já estiver pronto, atualiza-o.
                    if (mMapPreview != null) {
                        updateMapPreview();
                    }
                }
            }
    );

    // Metodo principal, chamado na criação da Activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_pj);

        mAuth = FirebaseAuth.getInstance(); // Inicializa o Firebase Auth.

        // Associa as variáveis aos componentes do XML.
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

        // Obtém o fragmento do mapa e o prepara de forma assíncrona.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapPreview);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMapPreview = googleMap; // Atribui o mapa ao nosso objeto.
                mMapPreview.getUiSettings().setAllGesturesEnabled(false); // Desabilita gestos no preview.
            });
        }

        // Listener do botão para abrir a tela de seleção no mapa.
        btnAbrirMapa.setOnClickListener(v -> {
            Intent intent = new Intent(CadastroPJActivity.this, SelecionarLocalActivity.class);
            mapResultLauncher.launch(intent);
        });

        setupNavigationButtons(); // Configura todos os botões de navegação do formulário.
    }

    // Atualiza o mapa de preview com a localização selecionada.
    private void updateMapPreview(){
        if(mMapPreview != null && localSelecionado != null){
            mapPreviewContainer.setVisibility(View.VISIBLE); // Torna o container do mapa visível.
            mMapPreview.clear(); // Limpa marcadores antigos.
            mMapPreview.addMarker(new MarkerOptions().position(localSelecionado).title("Local Selecionado")); // Adiciona um novo marcador.
            mMapPreview.moveCamera(CameraUpdateFactory.newLatLngZoom(localSelecionado, 16f)); // Centraliza e aproxima a câmera.
        }
    }

    // Configura os listeners de clique para os botões de avançar e voltar.
    private void setupNavigationButtons(){
        findViewById(R.id.btnProximo1).setOnClickListener(v -> {
            nome = edtNome.getText().toString().trim();
            telefone = edtTelefone.getText().toString().trim();
            String cnpjInput = edtCnpj.getText().toString();
            boolean erro = false;
            if (TextUtils.isEmpty(nome)) { edtNome.setError("Informe o nome"); erro = true; }
            if (TextUtils.isEmpty(telefone)) { edtTelefone.setError("Informe o telefone"); erro = true; }
            if (!isCnpjValido(cnpjInput)) { edtCnpj.setError("O CNPJ informado não é válido."); erro = true; }
            if (erro) return;

            cnpj = cnpjInput.replaceAll("[^0-9]", ""); // Armazena apenas os números do CNPJ.
            step1.setVisibility(View.GONE);
            step2.setVisibility(View.VISIBLE);
            progressIndicator.setProgress(2, true); // Avança para a próxima etapa.
        });

        findViewById(R.id.btnProximo2).setOnClickListener(v -> {
            descricao = edtDescricao.getText().toString().trim();
            if (TextUtils.isEmpty(descricao)) { edtDescricao.setError("Informe a descrição"); return; }
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

        // Configura os botões de "Voltar".
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

        // Configura o botão final de cadastro.
        findViewById(R.id.btnCadastrar).setOnClickListener(v -> {
            email = edtEmail.getText().toString().trim();
            senha = edtSenha.getText().toString().trim();
            boolean erro = false;
            if (TextUtils.isEmpty(email)) { edtEmail.setError("Informe o e-mail"); erro = true; }
            if (TextUtils.isEmpty(senha)) { edtSenha.setError("Informe a senha"); erro = true; }
            if (erro) return;

            // Cria o usuário no Firebase Authentication com email e senha.
            mAuth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) { // Se a autenticação for criada com sucesso.
                            String uid = mAuth.getCurrentUser().getUid(); // Pega o ID único do novo usuário.
                            // Cria um objeto UsuarioPJ com todos os dados coletados.
                            UsuarioPJ usuario = new UsuarioPJ(uid, nome, cnpj, descricao, telefone, estado, cidade, rua, numero, localSelecionado.latitude, localSelecionado.longitude);
                            // Salva o objeto do usuário no Realtime Database.
                            FirebaseDatabase.getInstance().getReference().child("usuarios").child("pj").child(uid).setValue(usuario)
                                    .addOnCompleteListener(taskDb -> {
                                        if (taskDb.isSuccessful()) {
                                            Intent intent = new Intent(CadastroPJActivity.this, LoginActivity.class);
                                            intent.putExtra("cadastro_sucesso", true); // Passa um mensagem de succeso para a tela de login.
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Snackbar.make(findViewById(android.R.id.content), "Erro ao salvar dados no banco.", Snackbar.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            // Lógica para tratar erros do cadastro do login.
                            String erroFB = task.getException() != null ? task.getException().getMessage() : "Erro desconhecido";
                            if (erroFB.contains("email address is badly formatted")) { edtEmail.setError("O formato do e-mail é inválido"); }
                            else if (erroFB.contains("The email address is already in use")) { edtEmail.setError("Este endereço de e-mail já está em uso"); }
                            else if (erroFB.contains("Password")) { layoutSenha.setError("A senha deve ter no mínimo 6 caracteres, uma número, uma letra maiúscula e uma letra minúscula"); }
                            else { Snackbar.make(findViewById(android.R.id.content), "Erro: " + erroFB, Snackbar.LENGTH_LONG).show(); }
                        }
                    });
        });
    }

    // Algoritmo para validar um número de CNPJ através dos dígitos verificadores.
    public static boolean isCnpjValido(String cnpj) {
        cnpj = cnpj.replaceAll("[^0-9]", ""); // Remove caracteres não numéricos.
        if (cnpj.length() != 14) return false; // Verifica se o tamanho é 14.
        if (cnpj.matches("(\\d)\\1{13}")) return false; // Verifica se todos os dígitos são iguais.

        try {
            // Cálculo do primeiro dígito verificador.
            int soma = 0;
            int peso = 2;
            for (int i = 11; i >= 0; i--) {
                soma += Integer.parseInt(cnpj.substring(i, i + 1)) * peso;
                peso = (peso == 9) ? 2 : peso + 1;
            }
            int dv1 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);
            if (dv1 != Integer.parseInt(cnpj.substring(12, 13))) return false;

            // Cálculo do segundo dígito verificador.
            soma = 0;
            peso = 2;
            for (int i = 12; i >= 0; i--) {
                soma += Integer.parseInt(cnpj.substring(i, i + 1)) * peso;
                peso = (peso == 9) ? 2 : peso + 1;
            }
            int dv2 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);
            return dv2 == Integer.parseInt(cnpj.substring(13, 14)); // Retorna true se ambos os dígitos forem válidos.
        } catch (Exception e) {
            return false;
        }
    }
}


