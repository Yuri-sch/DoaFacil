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

public class ContatosAdapter extends RecyclerView.Adapter<ContatosAdapter.ContatoViewHolder> {

    private List<String> contatosList;
    private OnContatoListener listener;

    public interface OnContatoListener {
        void onDeleteClick(String contato);
    }

    public ContatosAdapter(List<String> contatosList, OnContatoListener listener) {
        this.contatosList = contatosList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContatoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contato, parent, false);
        return new ContatoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContatoViewHolder holder, int position) {
        String contato = contatosList.get(position);
        holder.tvContato.setText(contato);
        holder.btnExcluir.setOnClickListener(v -> listener.onDeleteClick(contato));
    }

    @Override
    public int getItemCount() {
        return contatosList.size();
    }

    static class ContatoViewHolder extends RecyclerView.ViewHolder {
        TextView tvContato;
        ImageButton btnExcluir;

        public ContatoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContato = itemView.findViewById(R.id.tvContato);
            btnExcluir = itemView.findViewById(R.id.btnExcluirContato);
        }
    }
}
