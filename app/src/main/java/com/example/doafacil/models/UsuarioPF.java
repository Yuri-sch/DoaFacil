package com.example.doafacil.models;

/**
 * Modelo de dados para representar um usuário do tipo PF (Pessoa Física).
 * Esses dados serão armazenados no Firebase Realtime Database.
 */
public class UsuarioPF {

    private String id;       // UID do Firebase Authentication
    private String nome;
    private String cpf;
    private int idade;
    private String telefone;

    public UsuarioPF() {
        // Construtor vazio exigido pelo Firebase
    }

    public UsuarioPF(String id, String nome, String cpf, int idade, String telefone) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.idade = idade;
        this.telefone = telefone;
    }

    // Getters e Setters (obrigatórios para Firebase funcionar corretamente)

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public int getIdade() { return idade; }
    public void setIdade(int idade) { this.idade = idade; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
