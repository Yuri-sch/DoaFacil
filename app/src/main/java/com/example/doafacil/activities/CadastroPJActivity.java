package com.example.doafacil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doafacil.R;
import com.example.doafacil.models.UsuarioPJ;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class CadastroPJActivity extends AppCompatActivity {

    private EditText edtNome, edtCnpj, edtDescricao, edtTelefone, edtCidade, edtEmail, edtSenha;
    private LinearLayout step1, step2, step3;
    private FirebaseAuth mAuth;

    private double latitude = 0.0;
    private double longitude = 0.0;
    private static final int REQUEST_LOCAL = 1;

    private String nome, cnpj, descricao, telefone, cidade, email, senha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_pj);

        mAuth = FirebaseAuth.getInstance();

        // Campos
        edtNome = findViewById(R.id.edtNome);
        edtCnpj = findViewById(R.id.edtCnpj);
        edtDescricao = findViewById(R.id.edtDescricao);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtCidade = findViewById(R.id.edtCidade);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);

        // Etapas
        step1 = findViewById(R.id.step1);
        step2 = findViewById(R.id.step2);
        step3 = findViewById(R.id.step3);

        // Etapa 1: Nome + CNPJ
        findViewById(R.id.btnProximo1).setOnClickListener(v -> {
            nome = edtNome.getText().toString().trim();
            cnpj = edtCnpj.getText().toString().trim();
            boolean erro = false;
            if (TextUtils.isEmpty(nome)) {
                edtNome.setError("Informe o nome");
                erro = true;
            }
            if (TextUtils.isEmpty(cnpj)) {
                edtCnpj.setError("Informe o CNPJ");
                erro = true;
            } else if (!isCnpjValido(cnpj)) {
                edtCnpj.setError("CNPJ inválido");
                erro = true;
            }
            if (erro) return;
            step1.setVisibility(LinearLayout.GONE);
            step2.setVisibility(LinearLayout.VISIBLE);
        });

        // Etapa 2: Descrição + Telefone + Localização
        findViewById(R.id.btnProximo2).setOnClickListener(v -> {
            descricao = edtDescricao.getText().toString().trim();
            telefone = edtTelefone.getText().toString().trim();
            cidade = edtCidade.getText().toString().trim();
            boolean erro = false;
            if (TextUtils.isEmpty(descricao)) {
                edtDescricao.setError("Informe a descrição");
                erro = true;
            }
            if (TextUtils.isEmpty(telefone)) {
                edtTelefone.setError("Informe o telefone");
                erro = true;
            }
            if (TextUtils.isEmpty(cidade)) {
                edtCidade.setError("Informe a cidade");
                erro = true;
            }
            if (erro) return;
            step2.setVisibility(LinearLayout.GONE);
            step3.setVisibility(LinearLayout.VISIBLE);
        });

        // Etapa 2: Localização
        findViewById(R.id.btnSelecionarLocal).setOnClickListener(v -> {
            Intent intent = new Intent(this, SelecionarLocalActivity.class);
            startActivityForResult(intent, REQUEST_LOCAL);
        });

        // Etapa 2: Voltar
        findViewById(R.id.btnVoltar1).setOnClickListener(v -> {
            step2.setVisibility(LinearLayout.GONE);
            step1.setVisibility(LinearLayout.VISIBLE);
        });

        // Etapa 3: Voltar
        findViewById(R.id.btnVoltar2).setOnClickListener(v -> {
            step3.setVisibility(LinearLayout.GONE);
            step2.setVisibility(LinearLayout.VISIBLE);
        });

        // Etapa 3: Cadastrar
        findViewById(R.id.btnCadastrar).setOnClickListener(v -> {
            email = edtEmail.getText().toString().trim();
            senha = edtSenha.getText().toString().trim();
            boolean erro = false;
            if (TextUtils.isEmpty(email)) {
                edtEmail.setError("Informe o e-mail");
                erro = true;
            }
            if (TextUtils.isEmpty(senha)) {
                edtSenha.setError("Informe a senha");
                erro = true;
            }
            if (erro) return;

            // Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();
                            UsuarioPJ usuario = new UsuarioPJ(uid, nome, cnpj, descricao, telefone, latitude, longitude);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("usuarios").child("pj").child(uid)
                                    .setValue(usuario)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Intent intent = new Intent(CadastroPJActivity.this, LoginActivity.class);
                                            intent.putExtra("cadastro_sucesso", true);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Snackbar.make(findViewById(R.id.rootLayout), "Erro ao salvar dados.", Snackbar.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            String erroFB = task.getException().getMessage();
                            if (erroFB != null) {
                                if (erroFB.contains("email address is badly formatted")) {
                                    edtEmail.setError("E-mail inválido");
                                } else if (erroFB.contains("The email address is already in use")) {
                                    edtEmail.setError("Este e-mail já está em uso");
                                } else if (erroFB.contains("Password should be at least")) {
                                    edtSenha.setError("A senha deve ter pelo menos 6 caracteres");
                                } else {
                                    Snackbar.make(findViewById(R.id.rootLayout), "Erro: " + erroFB, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
        });
    }

    // Validador de CNPJ (estrutura e dígitos verificadores)
    private boolean isCnpjValido(String cnpj) {
        cnpj = cnpj.replaceAll("[^\\d]", "");
        if (cnpj.length() != 14 || cnpj.matches("(\\d)\\1{13}")) return false;
        try {
            int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int soma = 0;
            for (int i = 0; i < 12; i++)
                soma += (cnpj.charAt(i) - '0') * peso1[i];
            int dig1 = soma % 11 < 2 ? 0 : 11 - (soma % 11);

            soma = 0;
            for (int i = 0; i < 13; i++)
                soma += (cnpj.charAt(i) - '0') * peso2[i];
            int dig2 = soma % 11 < 2 ? 0 : 11 - (soma % 11);

            return dig1 == (cnpj.charAt(12) - '0') && dig2 == (cnpj.charAt(13) - '0');
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOCAL && resultCode == RESULT_OK) {
            latitude = data.getDoubleExtra("latitude", 0.0);
            longitude = data.getDoubleExtra("longitude", 0.0);
            TextView txt = findViewById(R.id.txtLocalSelecionado);
            txt.setText("Local selecionado: " + latitude + ", " + longitude);
        }
    }
}

