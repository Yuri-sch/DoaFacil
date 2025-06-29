package com.example.doafacil.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doafacil.R;
import com.example.doafacil.models.ObjetoDoacao; // MUDANÇA AQUI
import java.util.List;

public class ObjetosDoacaoAdapter extends RecyclerView.Adapter<ObjetosDoacaoAdapter.ObjetoViewHolder> {

    private List<ObjetoDoacao> objetoList; // MUDANÇA AQUI
    private OnItemListener listener;

    public interface OnItemListener {
        void onEditClick(ObjetoDoacao objeto); // MUDANÇA AQUI
        void onDeleteClick(ObjetoDoacao objeto); // MUDANÇA AQUI
    }

    public ObjetosDoacaoAdapter(List<ObjetoDoacao> objetoList, OnItemListener listener) { // MUDANÇA AQUI
        this.objetoList = objetoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ObjetoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // MUDANÇA AQUI para usar o novo layout de item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_objeto_doacao, parent, false);
        return new ObjetoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ObjetoViewHolder holder, int position) {
        ObjetoDoacao objeto = objetoList.get(position);
        holder.tvNomeObjeto.setText(objeto.getNome());
        holder.tvDescricaoObjeto.setText(objeto.getDescricao());
        holder.tvCategoriaObjeto.setText(objeto.getCategoria()); // NOVO

        holder.btnEditar.setOnClickListener(v -> listener.onEditClick(objeto));
        holder.btnExcluir.setOnClickListener(v -> listener.onDeleteClick(objeto));
    }

    @Override
    public int getItemCount() {
        return objetoList.size();
    }

    static class ObjetoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomeObjeto, tvDescricaoObjeto, tvCategoriaObjeto; // Adicionado tvCategoriaObjeto
        Button btnEditar, btnExcluir;

        public ObjetoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeObjeto = itemView.findViewById(R.id.tvNomeObjeto);
            tvDescricaoObjeto = itemView.findViewById(R.id.tvDescricaoObjeto);
            tvCategoriaObjeto = itemView.findViewById(R.id.tvCategoriaObjeto); // NOVO
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }
}
