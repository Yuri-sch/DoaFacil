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

/**
 * Activity responsável pelo fluxo de cadastro de um novo utilizador Pessoa Física (PF).
 * O cadastro é dividido em 3 etapas para uma melhor experiência do utilizador.
 */
public class CadastroPFActivity extends AppCompatActivity {

    // --- Declaração dos Componentes de UI (Interface do Utilizador) ---

    // Layouts para cada etapa do formulário
    private LinearLayout step1, step2, step3;
    // Campos de texto para a inserção de dados
    private EditText edtNome, edtCpf, edtIdade, edtTelefone, edtEmail, edtSenha;
    // Layout especial para o campo de senha, que permite mostrar ícones e erros
    private TextInputLayout layoutSenha;
    // Indicador visual de progresso no topo da tela
    private LinearProgressIndicator progressIndicator;
    // Instância do Firebase Authentication para gerir a criação de contas
    private FirebaseAuth mAuth;

    /**
     * Método chamado quando a Activity é criada pela primeira vez.
     * É aqui que o layout é definido e os componentes são inicializados.
     * @param savedInstanceState Dados guardados de uma instância anterior, se houver.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Define o layout XML que esta Activity irá usar
        setContentView(R.layout.activity_cadastro_pf);

        // Configura a Toolbar (barra de título)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Adiciona o botão "voltar" na Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Inicializa a instância do Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        // Chama os métodos para inicializar as views e configurar os botões
        initViews();
        setupNavigation();
    }

    /**
     * Associa as variáveis da classe com os seus respectivos componentes no ficheiro de layout XML.
     * Este método centraliza toda a inicialização de views.
     */
    private void initViews(){
        // Associa os layouts de cada etapa
        step1 = findViewById(R.id.step1_pf);
        step2 = findViewById(R.id.step2_pf);
        step3 = findViewById(R.id.step3_pf);
        // Associa o indicador de progresso
        progressIndicator = findViewById(R.id.progress_indicator);
        // Associa os campos de texto da Etapa 1
        edtNome = findViewById(R.id.edtNomePF);
        edtCpf = findViewById(R.id.edtCpfPF);
        // Associa os campos de texto da Etapa 2
        edtIdade = findViewById(R.id.edtIdadePF);
        edtTelefone = findViewById(R.id.edtTelefonePF);
        // Associa os campos de texto da Etapa 3
        edtEmail = findViewById(R.id.edtEmailPF);
        edtSenha = findViewById(R.id.edtSenhaPF);
        layoutSenha = findViewById(R.id.layoutSenhaPF);
    }

    /**
     * Configura os listeners de clique para todos os botões de navegação (Próximo, Voltar, Finalizar).
     */
    private void setupNavigation() {
        // Listener do botão "Próximo" da Etapa 1
        findViewById(R.id.btnProximo1_pf).setOnClickListener(v -> {
            // Se os dados da Etapa 1 forem válidos...
            if (validaStep1()) {
                // ...esconde a etapa atual, mostra a próxima e atualiza o progresso.
                step1.setVisibility(View.GONE);
                step2.setVisibility(View.VISIBLE);
                progressIndicator.setProgress(2, true);
            }
        });

        // Listener do botão "Próximo" da Etapa 2
        findViewById(R.id.btnProximo2_pf).setOnClickListener(v -> {
            if (validaStep2()) {
                step2.setVisibility(View.GONE);
                step3.setVisibility(View.VISIBLE);
                progressIndicator.setProgress(3, true);
            }
        });

        // Listener do botão "Finalizar Cadastro" da Etapa 3
        findViewById(R.id.btnCadastrarPF).setOnClickListener(v -> {
            if (validaStep3()) {
                cadastrarUsuario();
            }
        });

        // Listener do botão "Voltar" da Etapa 2
        findViewById(R.id.btnVoltar1_pf).setOnClickListener(v -> {
            step2.setVisibility(View.GONE);
            step1.setVisibility(View.VISIBLE);
            progressIndicator.setProgress(1, true);
        });

        // Listener do botão "Voltar" da Etapa 3
        findViewById(R.id.btnVoltar2_pf).setOnClickListener(v -> {
            step3.setVisibility(View.GONE);
            step2.setVisibility(View.VISIBLE);
            progressIndicator.setProgress(2, true);
        });
    }

    /**
     * Valida os campos da Etapa 1 (Nome e CPF).
     * @return true se todos os campos forem válidos, false caso contrário.
     */
    private boolean validaStep1() {
        if (TextUtils.isEmpty(edtNome.getText()) || TextUtils.isEmpty(edtCpf.getText())) {
            Snackbar.make(findViewById(android.R.id.content), "Nome e CPF são obrigatórios.", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        // Chama a função de validação matemática do CPF
        if (!isCpfValido(edtCpf.getText().toString())) {
            edtCpf.setError("O CPF informado não é válido.");
            return false;
        } else {
            edtCpf.setError(null); // Limpa o erro se o CPF for válido
        }
        return true;
    }

    /**
     * Valida os campos da Etapa 2 (Telefone e Idade).
     * @return true se todos os campos forem válidos, false caso contrário.
     */
    private boolean validaStep2() {
        if (TextUtils.isEmpty(edtTelefone.getText()) || TextUtils.isEmpty(edtIdade.getText())) {
            Snackbar.make(findViewById(android.R.id.content), "Telefone e Idade são obrigatórios.", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Valida os campos da Etapa 3 (Email e Senha).
     * @return true se todos os campos forem válidos, false caso contrário.
     */
    private boolean validaStep3() {
        if (TextUtils.isEmpty(edtEmail.getText()) || TextUtils.isEmpty(edtSenha.getText())) {
            Snackbar.make(findViewById(android.R.id.content), "E-mail e senha são obrigatórios.", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Executa o processo de cadastro do utilizador no Firebase.
     * Primeiro cria a conta de autenticação e, se bem-sucedido,
     * salva os dados do utilizador no Realtime Database.
     */
    private void cadastrarUsuario() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        // 1. Cria o utilizador no Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    // Se a criação da conta de autenticação foi bem-sucedida...
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        // Cria um objeto UsuarioPF com todos os dados recolhidos
                        UsuarioPF usuario = new UsuarioPF(
                                uid,
                                edtNome.getText().toString().trim(),
                                edtCpf.getText().toString().replaceAll("[^0-9]", ""), // Salva o CPF limpo
                                Integer.parseInt(edtIdade.getText().toString().trim()),
                                edtTelefone.getText().toString().trim()
                        );
                        // 2. Salva o objeto do utilizador no Realtime Database
                        FirebaseDatabase.getInstance().getReference().child("usuarios").child("pf").child(uid).setValue(usuario)
                                .addOnCompleteListener(dbTask -> {
                                    // Se os dados foram salvos com sucesso...
                                    if (dbTask.isSuccessful()) {
                                        // ...redireciona para a tela de Login, informando o sucesso.
                                        Intent intent = new Intent(CadastroPFActivity.this, LoginActivity.class);
                                        intent.putExtra("cadastro_sucesso", true);
                                        startActivity(intent);
                                        finish(); // Fecha a tela de cadastro para o utilizador não voltar
                                    } else {
                                        // Mostra erro se falhar ao salvar no banco de dados
                                        Snackbar.make(findViewById(android.R.id.content), "Erro ao salvar dados.", Snackbar.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        // Trata erros comuns na criação da conta de autenticação
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

    /**
     * Algoritmo para validar um número de CPF.
     * Verifica o tamanho, dígitos repetidos e os dois dígitos verificadores.
     * @param cpf O CPF a ser validado (pode conter pontos e traços).
     * @return true se o CPF for matematicamente válido, false caso contrário.
     */
    public static boolean isCpfValido(String cpf) {
        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("[^0-9]", "");
        // Verifica se tem 11 dígitos ou se todos os dígitos são iguais
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false;
        try {
            // Cálculo do primeiro dígito verificador
            int[] peso = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
            int soma = 0;
            for (int i = 0; i < 9; i++) soma += Integer.parseInt(cpf.substring(i, i+1)) * peso[i+1];
            int dv1 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);
            if (dv1 != Integer.parseInt(cpf.substring(9,10))) return false;

            // Cálculo do segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) soma += Integer.parseInt(cpf.substring(i, i+1)) * peso[i];
            int dv2 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);
            return dv2 == Integer.parseInt(cpf.substring(10,11));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Lida com o clique no botão "voltar" da Toolbar.
     * @return true para indicar que o evento foi tratado.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Simula o comportamento do botão "voltar" do dispositivo
        return true;
    }
}


