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

public class InstituicoesAdapter extends RecyclerView.Adapter<InstituicoesAdapter.InstituicaoViewHolder> {

    private List<InstituicaoDistancia> instituicaoList;
    private OnInstituicaoListener listener;

    public interface OnInstituicaoListener {
        void onInstituicaoClick(String uid);
    }

    public InstituicoesAdapter(List<InstituicaoDistancia> instituicaoList, OnInstituicaoListener listener) {
        this.instituicaoList = instituicaoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InstituicaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_instituicao, parent, false);
        return new InstituicaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstituicaoViewHolder holder, int position) {
        InstituicaoDistancia item = instituicaoList.get(position);
        holder.tvNome.setText(item.getInstituicao().getNome());
        holder.tvCidade.setText(String.format("%s, %s", item.getInstituicao().getCidade(), item.getInstituicao().getEstado()));

        // Formata a distância para ser mais legível
        float distanciaEmKm = item.getDistancia() / 1000;
        holder.tvDistancia.setText(String.format(Locale.getDefault(), "Aprox. %.1f km de distância", distanciaEmKm));

        holder.itemView.setOnClickListener(v -> listener.onInstituicaoClick(item.getInstituicao().getUid()));
    }

    @Override
    public int getItemCount() {
        return instituicaoList.size();
    }

    static class InstituicaoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvCidade, tvDistancia;

        public InstituicaoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNomeInstituicao);
            tvCidade = itemView.findViewById(R.id.tvCidadeInstituicao);
            tvDistancia = itemView.findViewById(R.id.tvDistancia);
        }
    }
}