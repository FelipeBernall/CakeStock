package com.example.cakestock;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Alterado de Button para ImageButton
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class IngredienteAdapter extends RecyclerView.Adapter<IngredienteAdapter.IngredienteViewHolder> {

    private Context context;
    private List<Ingrediente> ingredientes;
    private OnIngredienteClickListener listener;

    public IngredienteAdapter(Context context, List<Ingrediente> ingredientes, OnIngredienteClickListener listener) {
        this.context = context;
        this.ingredientes = ingredientes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IngredienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingrediente, parent, false);
        return new IngredienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredienteViewHolder holder, int position) {
        Ingrediente ingrediente = ingredientes.get(position);
        holder.bind(ingrediente, listener);
    }

    @Override
    public int getItemCount() {
        return ingredientes.size();
    }

    public interface OnIngredienteClickListener {
        void onEditClick(Ingrediente ingrediente);
        void onDeleteClick(Ingrediente ingrediente);
    }

    public static class IngredienteViewHolder extends RecyclerView.ViewHolder {
        private TextView textNome;
        private TextView textQuantidade;
        private ImageButton btnEditar; // Alterado de Button para ImageButton
        private ImageButton btnExcluir; // ESTAVA CAUSANDO ERRO! PRESTAR ATENÇÃO!!!

        public IngredienteViewHolder(@NonNull View itemView) {
            super(itemView);
            textNome = itemView.findViewById(R.id.textNome);
            textQuantidade = itemView.findViewById(R.id.textQuantidade);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }

        public void bind(final Ingrediente ingrediente, final OnIngredienteClickListener listener) {
            textNome.setText(ingrediente.getNome());

            String tipoMedida = ingrediente.getTipoMedida();
            double quantidade = ingrediente.getQuantidade();
            double unidadeMedida = ingrediente.getUnidadeMedida();
            String quantidadeText;

            if (tipoMedida.equals("Unidades")) {
                quantidadeText = String.format("%.0f uni", quantidade);
            } else {
                double totalQuantidade = quantidade * unidadeMedida;
                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                if (tipoMedida.equals("Gramas")) {
                    if (totalQuantidade >= 1000) {
                        quantidadeText = String.format("%.1f Kg", totalQuantidade / 1000);
                    } else {
                        quantidadeText = String.format("%.0f g", totalQuantidade);
                    }
                } else if (tipoMedida.equals("Mililitros")) {
                    if (totalQuantidade >= 1000) {
                        quantidadeText = String.format("%.1f L", totalQuantidade / 1000);
                    } else {
                        quantidadeText = String.format("%.0f ml", totalQuantidade);
                    }
                } else {
                    quantidadeText = String.format("%.0f %s", totalQuantidade, tipoMedida);
                }
            }

            Log.d("DebugAdapter", "Ingrediente: " + ingrediente.getNome() + ", Quantidade exibida: " + quantidadeText);

            textQuantidade.setText(quantidadeText);

            btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(ingrediente);
                }
            });

            btnExcluir.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(ingrediente);
                }
            });
        }
    }
}
