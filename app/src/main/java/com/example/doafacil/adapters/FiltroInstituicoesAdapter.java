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

public class FiltroInstituicoesAdapter extends RecyclerView.Adapter<FiltroInstituicoesAdapter.ViewHolder> {

    private List<UsuarioPJ> instituicaoList;
    private OnInstituicaoListener listener;

    public interface OnInstituicaoListener {
        void onInstituicaoClick(String uid);
    }

    public FiltroInstituicoesAdapter(List<UsuarioPJ> instituicaoList, OnInstituicaoListener listener) {
        this.instituicaoList = instituicaoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_instituicao_filtro, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsuarioPJ instituicao = instituicaoList.get(position);
        holder.tvNome.setText(instituicao.getNome());
        String endereco = instituicao.getRua() + ", " + instituicao.getNumero() + " - " + instituicao.getCidade();
        holder.tvEndereco.setText(endereco);
        holder.itemView.setOnClickListener(v -> listener.onInstituicaoClick(instituicao.getUid()));
    }

    @Override
    public int getItemCount() {
        return instituicaoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvEndereco;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNomeInstituicaoFiltro);
            tvEndereco = itemView.findViewById(R.id.tvEnderecoFiltro);
        }
    }
}
