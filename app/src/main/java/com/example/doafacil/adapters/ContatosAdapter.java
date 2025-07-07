package com.example.doafacil.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doafacil.R;
import java.util.List;

// Adapter para exibir uma lista de contatos em um RecyclerView.
public class ContatosAdapter extends RecyclerView.Adapter<ContatosAdapter.ContatoViewHolder> {

    // A lista de strings que contém os contatos a serem exibidos.
    private List<String> contatosList;
    // Listener para lidar com eventos de clique (exclusão).
    private OnContatoListener listener;

    // Interface que define um metodo para ser chamado quando o botão de excluir for clicado.
    public interface OnContatoListener {
        void onDeleteClick(String contato);
    }

    // Construtor do adapter.
    public ContatosAdapter(List<String> contatosList, OnContatoListener listener) {
        this.contatosList = contatosList;
        this.listener = listener;
    }

    // Chamado quando o RecyclerView precisa de um novo ViewHolder para representar um item.
    @NonNull
    @Override
    public ContatoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout do item de contato a partir do XML.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contato, parent, false);
        // Retorna uma nova instância do ViewHolder.
        return new ContatoViewHolder(view);
    }

    // Vincula os dados de um contato específico (na posição 'position') ao ViewHolder.
    @Override
    public void onBindViewHolder(@NonNull ContatoViewHolder holder, int position) {
        // Obtém o contato da lista na posição atual.
        String contato = contatosList.get(position);
        // Define o texto do TextView com o contato.
        holder.tvContato.setText(contato);
        // Configura o listener de clique para o botão de excluir.
        holder.btnExcluir.setOnClickListener(v -> listener.onDeleteClick(contato));
    }

    // Retorna o número total de itens na lista de contatos.
    @Override
    public int getItemCount() {
        return contatosList.size();
    }

    // Classe interna que representa a view de cada item na lista.
    static class ContatoViewHolder extends RecyclerView.ViewHolder {
        // Componentes de UI do item.
        TextView tvContato;
        ImageButton btnExcluir;

        // Construtor do ViewHolder.
        public ContatoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContato = itemView.findViewById(R.id.tvContato);
            btnExcluir = itemView.findViewById(R.id.btnExcluirContato);
        }
    }
}
