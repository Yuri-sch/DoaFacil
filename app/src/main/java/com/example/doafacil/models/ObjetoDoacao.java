package com.example.doafacil.models;

// Model para os objetos de doação.
public class ObjetoDoacao {
    private String id;
    private String nome;
    private String descricao;
    private String categoria;

    // Construtor vazio necessário para o Firebase.
    public ObjetoDoacao() {
    }

    // Construtor principal.
    public ObjetoDoacao(String id, String nome, String descricao, String categoria) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}