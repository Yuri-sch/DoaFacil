package com.example.doafacil.models;

// Classe model auxiliar para ordenar as instituições por distância.
public class InstituicaoDistancia implements Comparable<InstituicaoDistancia> {
    private UsuarioPJ instituicao; // Armazena o objeto da instituição.
    private float distancia; // Guarda a distância em metros.

    // Construtor da classe.
    public InstituicaoDistancia(UsuarioPJ instituicao, float distancia) {
        this.instituicao = instituicao;
        this.distancia = distancia;
    }

    // Retorna o objeto da instituição.
    public UsuarioPJ getInstituicao() {
        return instituicao;
    }

    // Retorna a distância calculada.
    public float getDistancia() {
        return distancia;
    }

    // Compara esta instância com outra para fins de ordenação.
    @Override
    public int compareTo(InstituicaoDistancia outra) {
        // Ordena os objetos do menor para o maior valor de distância.
        return Float.compare(this.distancia, outra.distancia);
    }
}