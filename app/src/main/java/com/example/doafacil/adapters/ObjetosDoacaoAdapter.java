package com.example.doafacil.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doafacil.R;
import com.example.doafacil.models.ObjetoDoacao;
import java.util.List;

// Adapter para exibir e gerenciar uma lista de objetos requisitados para doação.
public class ObjetosDoacaoAdapter extends RecyclerView.Adapter<ObjetosDoacaoAdapter.ObjetoViewHolder> {

    // Lista que armazena os objetos de doação a serem exibidos.
    private List<ObjetoDoacao> objetoList;
    // Listener para lidar com os cliques nos botões de editar e excluir.
    private OnItemListener listener;

    // Interface que define os métodos para os eventos de clique.
    public interface OnItemListener {
        void onEditClick(ObjetoDoacao objeto);
        void onDeleteClick(ObjetoDoacao objeto);
    }

    // Construtor do adapter.
    public ObjetosDoacaoAdapter(List<ObjetoDoacao> objetoList, OnItemListener listener) {
        this.objetoList = objetoList;
        this.listener = listener;
    }

    // Cria um novo ViewHolder para representar um item na lista.
    @NonNull
    @Override
    public ObjetoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout customizado do item a partir do XML.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_objeto_doacao, parent, false);
        return new ObjetoViewHolder(view);
    }

    // Associa os dados de um objeto específico (na posição 'position') ao ViewHolder.
    @Override
    public void onBindViewHolder(@NonNull ObjetoViewHolder holder, int position) {
        // Obtém o objeto da lista na posição atual.
        ObjetoDoacao objeto = objetoList.get(position);
        // Define os textos dos TextViews com os dados do objeto.
        holder.tvNomeObjeto.setText(objeto.getNome());
        holder.tvDescricaoObjeto.setText(objeto.getDescricao());
        holder.tvCategoriaObjeto.setText(objeto.getCategoria());

        // Configura os listeners de clique para os botões de editar e excluir.
        holder.btnEditar.setOnClickListener(v -> listener.onEditClick(objeto));
        holder.btnExcluir.setOnClickListener(v -> listener.onDeleteClick(objeto));
    }

    // Retorna o número total de objetos na lista.
    @Override
    public int getItemCount() {
        return objetoList.size();
    }

    // Classe interna que gerencia as views de cada item da lista.
    static class ObjetoViewHolder extends RecyclerView.ViewHolder {
        // Componentes de UI do layout do item.
        TextView tvNomeObjeto, tvDescricaoObjeto, tvCategoriaObjeto;
        Button btnEditar, btnExcluir;

        // Construtor do ViewHolder.
        public ObjetoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeObjeto = itemView.findViewById(R.id.tvNomeObjeto);
            tvDescricaoObjeto = itemView.findViewById(R.id.tvDescricaoObjeto);
            tvCategoriaObjeto = itemView.findViewById(R.id.tvCategoriaObjeto);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }
}
