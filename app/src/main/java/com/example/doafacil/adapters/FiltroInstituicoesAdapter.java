package com.example.doafacil.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doafacil.R;
import com.example.doafacil.models.UsuarioPJ;
import java.util.List;

// Adapter para exibir uma lista de instituições filtradas em um RecyclerView.
public class FiltroInstituicoesAdapter extends RecyclerView.Adapter<FiltroInstituicoesAdapter.ViewHolder> {

    // Lista de objetos UsuarioPJ que representam as instituições a serem exibidas.
    private List<UsuarioPJ> instituicaoList;
    // Listener para lidar com cliques nos itens da lista.
    private OnInstituicaoListener listener;

    // Define um metodo a ser chamado quando um item da lista é clicado.
    public interface OnInstituicaoListener {
        void onInstituicaoClick(String uid);
    }

    // Construtor do adapter.
    public FiltroInstituicoesAdapter(List<UsuarioPJ> instituicaoList, OnInstituicaoListener listener) {
        this.instituicaoList = instituicaoList;
        this.listener = listener;
    }

    // Cria um novo ViewHolder sempre que o RecyclerView precisa de um novo item.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout XML customizado para o item da instituição.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_instituicao_filtro, parent, false);
        return new ViewHolder(view);
    }

    // Vincula os dados de uma instituição específica ao ViewHolder.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtém a instituição da lista na posição atual.
        UsuarioPJ instituicao = instituicaoList.get(position);
        // Define o nome da instituição no TextView.
        holder.tvNome.setText(instituicao.getNome());
        // Formata e exibe o endereço da instituição.
        String endereco = instituicao.getRua() + ", " + instituicao.getNumero() + " - " + instituicao.getCidade();
        holder.tvEndereco.setText(endereco);
        // Configura o listener para o clique no item inteiro da lista.
        holder.itemView.setOnClickListener(v -> listener.onInstituicaoClick(instituicao.getUid()));
    }

    // Retorna a quantidade total de instituições na lista.
    @Override
    public int getItemCount() {
        return instituicaoList.size();
    }

    // Classe interna que armazena as referências das views de cada item.
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvEndereco; // Componentes de UI do item.

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNomeInstituicaoFiltro);
            tvEndereco = itemView.findViewById(R.id.tvEnderecoFiltro);
        }
    }
}
