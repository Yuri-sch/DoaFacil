package com.example.doafacil.models;

public class ObjetoDoacao {
    private String id;
    private String nome;
    private String descricao;

    // Construtor vazio é necessário para o Firebase
    public ObjetoDoacao() {
    }

    public ObjetoDoacao(String id, String nome, String descricao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
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
}