package com.example.doafacil.models;

/**
 * Representa uma instituição (Pessoa Jurídica) no sistema.
 * Armazenada no Firebase Realtime Database.
 */
public class UsuarioPJ {

    private String uid;         // UID do Firebase Auth
    private String nome;
    private String cnpj;
    private String descricao;
    private String telefone;
    private String cidade;
    private double latitude;
    private double longitude;

    public UsuarioPJ() {
        // Construtor vazio necessário para o Firebase
    }

    // Construtor
    public UsuarioPJ(String id, String nome, String cnpj, String descricao, String telefone, double latitude, double longitude) {
        this.uid = uid;
        this.nome = nome;
        this.cnpj = cnpj;
        this.descricao = descricao;
        this.telefone = telefone;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters e Setters

    public String getId() { return uid; }
    public void setId(String id) { this.uid = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
}
