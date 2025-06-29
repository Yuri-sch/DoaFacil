package com.example.doafacil.models;

// Classe auxiliar para ordenar as instituições por distância
public class InstituicaoDistancia implements Comparable<InstituicaoDistancia> {
    private UsuarioPJ instituicao;
    private float distancia; // em metros

    public InstituicaoDistancia(UsuarioPJ instituicao, float distancia) {
        this.instituicao = instituicao;
        this.distancia = distancia;
    }

    public UsuarioPJ getInstituicao() {
        return instituicao;
    }

    public float getDistancia() {
        return distancia;
    }

    // O método compareTo é usado para ordenar a lista.
    // Ele coloca os objetos com menor distância primeiro.
    @Override
    public int compareTo(InstituicaoDistancia outra) {
        return Float.compare(this.distancia, outra.distancia);
    }
}
