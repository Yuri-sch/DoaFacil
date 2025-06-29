package com.example.doafacil.models;

import java.util.HashMap;
import java.util.Map;

public class UsuarioPJ {

    private String uid;
    private String nome;
    private String cnpj;
    private String descricao;
    private String telefone;
    private String estado;
    private String cidade;
    private String rua;
    private String numero;
    private double latitude;
    private double longitude;

    // CAMPOS ADICIONADOS
    private String fotoUrl;
    private String fotoBase64;
    private Map<String, String> contatos;

    public UsuarioPJ() {
        // Construtor vazio para o Firebase
    }

    public UsuarioPJ(String uid, String nome, String cnpj, String descricao, String telefone, String estado, String cidade, String rua, String numero, double latitude, double longitude) {
        this.uid = uid;
        this.nome = nome;
        this.cnpj = cnpj;
        this.descricao = descricao;
        this.telefone = telefone;
        this.estado = estado;
        this.cidade = cidade;
        this.rua = rua;
        this.numero = numero;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // --- Getters e Setters existentes ---
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getRua() { return rua; }
    public void setRua(String rua) { this.rua = rua; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    // --- GETTERS E SETTERS NOVOS ---
    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getFotoBase64() {
        return fotoBase64;
    }

    public void setFotoBase64(String fotoBase64) {
        this.fotoBase64 = fotoBase64;
    }

    public Map<String, String> getContatos() {
        if (contatos == null) {
            contatos = new HashMap<>(); // Evita NullPointerException
        }
        return contatos;
    }

    public void setContatos(Map<String, String> contatos) {
        this.contatos = contatos;
    }
}


