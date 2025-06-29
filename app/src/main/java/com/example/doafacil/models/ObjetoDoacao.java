package com.example.doafacil.models;

public class ObjetoDoacao {
    private String id;
    private String nome;
    private String descricao;
    private String categoria; // NOVO CAMPO

    // Construtor vazio é necessário para o Firebase
    public ObjetoDoacao() {
    }

    // Construtor atualizado
    public ObjetoDoacao(String id, String nome, String descricao, String categoria) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
    }

    // Getters e Setters para todos os campos, incluindo o novo
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getCategoria() { return categoria; } // NOVO
    public void setCategoria(String categoria) { this.categoria = categoria; } // NOVO
}