package com.example.doafacil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doafacil.R;
import com.example.doafacil.models.UsuarioPF;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class CadastroPFActivity extends AppCompatActivity {

    // Campos do formulário
    private EditText edtNome, edtCpf, edtIdade, edtTelefone, edtEmail, edtSenha;

    // Containers das etapas
    private LinearLayout step1, step2, step3;

    // Firebase
    private FirebaseAuth mAuth;

    // Variáveis temporárias
    private String nome, cpf, telefone, email, senha;
    private int idade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_pf);

        // Inicializa Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Conecta campos
        edtNome = findViewById(R.id.edtNome);
        edtCpf = findViewById(R.id.edtCpf);
        edtIdade = findViewById(R.id.edtIdade);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);

        // Conecta containers
        step1 = findViewById(R.id.step1);
        step2 = findViewById(R.id.step2);
        step3 = findViewById(R.id.step3);

        // Etapa 1: Próximo
        findViewById(R.id.btnProximo1).setOnClickListener(v -> {
            nome = edtNome.getText().toString().trim();
            cpf = edtCpf.getText().toString().trim();
            boolean erro = false;
            if (TextUtils.isEmpty(nome)) {
                edtNome.setError("Preencha o nome");
                erro = true;
            }
            if (TextUtils.isEmpty(cpf)) {
                edtCpf.setError("Preencha o CPF");
                erro = true;
            }
            if (!isCpfValido(cpf)) {
                edtCpf.setError("CPF inválido");
                erro = true;
            }
            if (erro) return;
            step1.setVisibility(View.GONE);
            step2.setVisibility(View.VISIBLE);
        });

        // Etapa 2: Próximo
        findViewById(R.id.btnProximo2).setOnClickListener(v -> {
            String idadeStr = edtIdade.getText().toString().trim();
            telefone = edtTelefone.getText().toString().trim();
            boolean erro = false;
            if (TextUtils.isEmpty(idadeStr)) {
                edtIdade.setError("Informe a idade");
                erro = true;
            }
            if (TextUtils.isEmpty(telefone)) {
                edtTelefone.setError("Informe o telefone");
                erro = true;
            }
            if (erro) return;
            idade = Integer.parseInt(idadeStr);
            step2.setVisibility(View.GONE);
            step3.setVisibility(View.VISIBLE);
        });

        // Etapa 2: Voltar
        findViewById(R.id.btnVoltar1).setOnClickListener(v -> {
            step2.setVisibility(View.GONE);
            step1.setVisibility(View.VISIBLE);
        });

        // Etapa 3: Voltar
        findViewById(R.id.btnVoltar2).setOnClickListener(v -> {
            step3.setVisibility(View.GONE);
            step2.setVisibility(View.VISIBLE);
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
            // Cria usuário com Auth
            mAuth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();
                            UsuarioPF usuario = new UsuarioPF(uid, nome, cpf, idade, telefone);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("usuarios")
                                    .child("pf")
                                    .child(uid)
                                    .setValue(usuario)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            // Redireciona imediatamente e envia flag para mostrar Snackbar no LoginActivity
                                            Intent intent = new Intent(CadastroPFActivity.this, LoginActivity.class);
                                            intent.putExtra("cadastro_sucesso", true);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // Exibe mensagem de erro ao salvar no banco
                                            String erroSalvar = task1.getException() != null
                                                    ? task1.getException().getMessage()
                                                    : "Erro desconhecido ao salvar dados";
                                            Snackbar.make(findViewById(R.id.rootLayout), "Erro: " + erroSalvar, Snackbar.LENGTH_LONG).show();
                                            Log.e("FIREBASE_DB", "Erro ao salvar dados", task1.getException());
                                        }
                                    });

                        } else {
                            // Captura a mensagem de erro e exibe no campo correto
                            String erroFB = task.getException().getMessage();

                            if (erroFB != null) {
                                if (erroFB.contains("email address is badly formatted")) {
                                    edtEmail.setError("E-mail inválido");
                                } else if (erroFB.contains("password")) {
                                    edtSenha.setError("A senha deve conter pelo menos 6 caracteres, 1 número, 1 letra minúscula e 1 letra maiúscula");
                                } else if (erroFB.contains("The email address is already in use")) {
                                    edtEmail.setError("Este e-mail já está em uso");
                                } else {
                                    // Erro desconhecido, mostra como fallback
                                    Toast.makeText(this, "Erro: " + erroFB, Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });

        });
    }

    // Função que valida se um CPF é válido, com base na lógica dos dígitos verificadores
    private boolean isCpfValido(String cpf) {
        // Remove tudo que não for número (pontos, traços, espaços)
        cpf = cpf.replaceAll("[^\\d]", "");

        // Verifica se o CPF tem 11 dígitos e não é uma sequência repetida (ex: 111.111.111-11)
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false;

        try {
            // ======= CÁLCULO DO PRIMEIRO DÍGITO VERIFICADOR =======
            int soma = 0;
            // Multiplica os 9 primeiros dígitos por 10, 9, ..., 2
            for (int i = 0; i < 9; i++) {
                int digito = cpf.charAt(i) - '0';  // Converte caractere para número
                soma += digito * (10 - i);
            }

            // Calcula o primeiro dígito verificador
            int primeiroDigito = 11 - (soma % 11);
            if (primeiroDigito >= 10) primeiroDigito = 0;

            // Compara com o 10º dígito do CPF
            if (primeiroDigito != (cpf.charAt(9) - '0')) return false;

            // ======= CÁLCULO DO SEGUNDO DÍGITO VERIFICADOR =======
            soma = 0;
            // Multiplica os 10 primeiros dígitos por 11, 10, ..., 2
            for (int i = 0; i < 10; i++) {
                int digito = cpf.charAt(i) - '0';
                soma += digito * (11 - i);
            }

            // Calcula o segundo dígito verificador
            int segundoDigito = 11 - (soma % 11);
            if (segundoDigito >= 10) segundoDigito = 0;

            // Compara com o 11º dígito do CPF
            return segundoDigito == (cpf.charAt(10) - '0');

        } catch (Exception e) {
            // Em caso de erro de conversão ou índice, retorna false
            return false;
        }
    }

}

