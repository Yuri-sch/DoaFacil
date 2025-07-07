package com.example.doafacil.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doafacil.R;
import com.example.doafacil.models.InstituicaoDistancia;
import java.util.List;
import java.util.Locale;

// Adapter para exibir uma lista de instituições e suas distâncias em um RecyclerView.
public class InstituicoesAdapter extends RecyclerView.Adapter<InstituicoesAdapter.InstituicaoViewHolder> {

    // Lista de objetos que contêm a instituição e a distância calculada.
    private List<InstituicaoDistancia> instituicaoList;
    // Listener para notificar quando um item da lista é clicado.
    private OnInstituicaoListener listener;

    // Define o metodo a ser chamado quando uma instituição é clicada.
    public interface OnInstituicaoListener {
        void onInstituicaoClick(String uid);
    }

    // Construtor do adapter.
    public InstituicoesAdapter(List<InstituicaoDistancia> instituicaoList, OnInstituicaoListener listener) {
        this.instituicaoList = instituicaoList;
        this.listener = listener;
    }

    // Cria um novo ViewHolder para um item da lista.
    @NonNull
    @Override
    public InstituicaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout do item a partir do arquivo XML.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_instituicao, parent, false);
        return new InstituicaoViewHolder(view);
    }

    // Vincula os dados de uma instituição específica ao ViewHolder.
    @Override
    public void onBindViewHolder(@NonNull InstituicaoViewHolder holder, int position) {
        // Obtém o item da lista na posição atual.
        InstituicaoDistancia item = instituicaoList.get(position);
        // Define o nome e a cidade da instituição nos TextViews.
        holder.tvNome.setText(item.getInstituicao().getNome());
        holder.tvCidade.setText(String.format("%s, %s", item.getInstituicao().getCidade(), item.getInstituicao().getEstado()));

        // Converte a distância de metros para quilômetros para melhor legibilidade.
        float distanciaEmKm = item.getDistancia() / 1000;
        // Formata e exibe a distância aproximada.
        holder.tvDistancia.setText(String.format(Locale.getDefault(), "Aprox. %.1f km de distância", distanciaEmKm));

        // Define a ação de clique para o item inteiro.
        holder.itemView.setOnClickListener(v -> listener.onInstituicaoClick(item.getInstituicao().getUid()));
    }

    // Retorna o número total de itens na lista.
    @Override
    public int getItemCount() {
        return instituicaoList.size();
    }

    // Classe que armazena as referências das views de cada item para otimização.
    static class InstituicaoViewHolder extends RecyclerView.ViewHolder {
        // Declaração dos componentes de UI.
        TextView tvNome, tvCidade, tvDistancia;

        public InstituicaoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Associa as variáveis aos seus respectivos IDs no layout XML.
            tvNome = itemView.findViewById(R.id.tvNomeInstituicao);
            tvCidade = itemView.findViewById(R.id.tvCidadeInstituicao);
            tvDistancia = itemView.findViewById(R.id.tvDistancia);
        }
    }
}