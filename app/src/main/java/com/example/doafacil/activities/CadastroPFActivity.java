package com.example.doafacil.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.doafacil.R;
import com.example.doafacil.models.UsuarioPF;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import android.widget.LinearLayout;

// Activity para o fluxo de cadastro de um novo utilizador Pessoa Física (PF).
public class CadastroPFActivity extends AppCompatActivity {

    // Declaração dos Componentes.
    private LinearLayout step1, step2, step3; // Layouts para cada etapa do formulário.
    private EditText edtNome, edtCpf, edtIdade, edtTelefone, edtEmail, edtSenha; // Campos de texto para a inserção de dados.
    private TextInputLayout layoutSenha; // Layout especial para o campo de senha, que permite mostrar ícones e erros.
    private LinearProgressIndicator progressIndicator; // Indicador visual de progresso no topo da tela.
    private FirebaseAuth mAuth; // Instância do Firebase Authentication para gerir a criação de contas.

    // Metodo chamado quando a Activity é criada pela primeira vez.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_pf); // Define o xml

        Toolbar toolbar = findViewById(R.id.toolbar); // Configura a Toolbar (barra de título).
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Adiciona o botão "voltar" na Toolbar.

        mAuth = FirebaseAuth.getInstance(); // Inicializa a instância do Firebase Authentication.
        initViews(); // Chama o metodo para inicializar as views.
        setupNavigation(); // Chama o metodo para configurar os botões de navegação.
    }

    // Associa as variáveis com os seus respectivos componentes XML.
    private void initViews(){
        step1 = findViewById(R.id.step1_pf);
        step2 = findViewById(R.id.step2_pf);
        step3 = findViewById(R.id.step3_pf);
        progressIndicator = findViewById(R.id.progress_indicator);
        edtNome = findViewById(R.id.edtNomePF);
        edtCpf = findViewById(R.id.edtCpfPF);
        edtIdade = findViewById(R.id.edtIdadePF);
        edtTelefone = findViewById(R.id.edtTelefonePF);
        edtEmail = findViewById(R.id.edtEmailPF);
        edtSenha = findViewById(R.id.edtSenhaPF);
        layoutSenha = findViewById(R.id.layoutSenhaPF);
    }

    // Configura os listeners de clique para todos os botões de navegação.
    private void setupNavigation() {
        // Listener do botão "Próximo" da Etapa 1.
        findViewById(R.id.btnProximo1_pf).setOnClickListener(v -> {
            if (validaStep1()) {
                step1.setVisibility(View.GONE);
                step2.setVisibility(View.VISIBLE);
                progressIndicator.setProgress(2, true);
            }
        });

        // Listener do botão "Próximo" da Etapa 2.
        findViewById(R.id.btnProximo2_pf).setOnClickListener(v -> {
            if (validaStep2()) {
                step2.setVisibility(View.GONE);
                step3.setVisibility(View.VISIBLE);
                progressIndicator.setProgress(3, true);
            }
        });

        // Listener do botão "Finalizar Cadastro" da Etapa 3.
        findViewById(R.id.btnCadastrarPF).setOnClickListener(v -> {
            if (validaStep3()) {
                cadastrarUsuario();
            }
        });

        // Listener do botão "Voltar" da Etapa 2.
        findViewById(R.id.btnVoltar1_pf).setOnClickListener(v -> {
            step2.setVisibility(View.GONE);
            step1.setVisibility(View.VISIBLE);
            progressIndicator.setProgress(1, true);
        });

        // Listener do botão "Voltar" da Etapa 3.
        findViewById(R.id.btnVoltar2_pf).setOnClickListener(v -> {
            step3.setVisibility(View.GONE);
            step2.setVisibility(View.VISIBLE);
            progressIndicator.setProgress(2, true);
        });
    }

    // Valida os campos da Etapa 1 (Nome e CPF).
    private boolean validaStep1() {
        if (TextUtils.isEmpty(edtNome.getText()) || TextUtils.isEmpty(edtCpf.getText())) {
            Snackbar.make(findViewById(android.R.id.content), "Nome e CPF são obrigatórios.", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        // Chama a função de validação matemática do CPF.
        if (!isCpfValido(edtCpf.getText().toString())) {
            edtCpf.setError("O CPF informado não é válido.");
            return false;
        } else {
            // Limpa o erro se o CPF for válido.
            edtCpf.setError(null);
        }
        return true;
    }

    // Valida os campos da Etapa 2 (Telefone e Idade).
    private boolean validaStep2() {
        if (TextUtils.isEmpty(edtTelefone.getText()) || TextUtils.isEmpty(edtIdade.getText())) {
            Snackbar.make(findViewById(android.R.id.content), "Telefone e Idade são obrigatórios.", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Valida os campos da Etapa 3 (Email e Senha).
    private boolean validaStep3() {
        if (TextUtils.isEmpty(edtEmail.getText()) || TextUtils.isEmpty(edtSenha.getText())) {
            Snackbar.make(findViewById(android.R.id.content), "E-mail e senha são obrigatórios.", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Executa o processo de cadastro do usuário no Firebase.
    private void cadastrarUsuario() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        // Cria o usuário no Firebase Authentication.
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        // Cria um objeto UsuarioPF com todos os dados obtidos.
                        UsuarioPF usuario = new UsuarioPF(
                                uid,
                                edtNome.getText().toString().trim(),
                                edtCpf.getText().toString().replaceAll("[^0-9]", ""), // Salva o CPF limpo.
                                Integer.parseInt(edtIdade.getText().toString().trim()),
                                edtTelefone.getText().toString().trim()
                        );
                        // Salva o objeto do utilizador no Realtime Database.
                        FirebaseDatabase.getInstance().getReference().child("usuarios").child("pf").child(uid).setValue(usuario)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        // redireciona para a tela de Login.
                                        Intent intent = new Intent(CadastroPFActivity.this, LoginActivity.class);
                                        intent.putExtra("cadastro_sucesso", true);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Snackbar.make(findViewById(android.R.id.content), "Erro ao salvar dados.", Snackbar.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        // Tratamento de erros de autenticação.
                        String erro = task.getException() != null ? task.getException().getMessage() : "Erro desconhecido";
                        if (erro.contains("Password")) {
                            layoutSenha.setError("A senha deve ter pelo menos 6 caracteres.");
                        } else if (erro.contains("email address is already in use")) {
                            edtEmail.setError("Este e-mail já está em uso.");
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "Erro no cadastro: " + erro, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // Algoritmo matemático para validar um número de CPF usando os dígitos verificadores.
    public static boolean isCpfValido(String cpf) {
        cpf = cpf.replaceAll("[^0-9]", ""); // Remove caracteres não numéricos.
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false; // Verifica tamanho e dígitos repetidos.
        try {
            int[] peso = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2}; // Peso para cálculo do DV.
            int soma = 0;
            for (int i = 0; i < 9; i++) soma += Integer.parseInt(cpf.substring(i, i+1)) * peso[i+1];
            int dv1 = (soma % 11 < 2) ? 0 : 11 - (soma % 11); // Cálculo do primeiro dígito verificador.
            if (dv1 != Integer.parseInt(cpf.substring(9,10))) return false;

            soma = 0;
            for (int i = 0; i < 10; i++) soma += Integer.parseInt(cpf.substring(i, i+1)) * peso[i];
            int dv2 = (soma % 11 < 2) ? 0 : 11 - (soma % 11); // Cálculo do segundo dígito verificador.
            return dv2 == Integer.parseInt(cpf.substring(10,11));
        } catch (Exception e) {
            return false;
        }
    }

    // Lida com o clique no botão "voltar" da Toolbar.
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // igual o comportamento do botão "voltar" do dispositivo.
        return true;
    }
}


