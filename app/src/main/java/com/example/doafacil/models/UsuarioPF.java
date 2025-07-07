package com.example.doafacil.models;

// Model para usuários PF.
public class UsuarioPF {

    private String id;
    private String nome;
    private String cpf;
    private int idade;
    private String telefone;

    // Construtor vazio exigido pelo Firebase.
    public UsuarioPF() {
    }

    // Construtor principal.
    public UsuarioPF(String id, String nome, String cpf, int idade, String telefone) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.idade = idade;
        this.telefone = telefone;
    }

    // Getters e Setters para permitir o acesso e modificação dos campos da classe.
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
