package com.example.cakestock.ingrediente;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cakestock.R;

import java.text.DecimalFormat;
import java.util.List;

// Adapter que vincula a lista de ingredientes à interface de usuário (conecta os dados com a lista)
public class IngredienteAdapter extends RecyclerView.Adapter<IngredienteAdapter.IngredienteViewHolder> {

    // Campos de entrada de dados
    private Context context;
    private List<Ingrediente> ingredientes;
    private OnIngredienteClickListener listener;

    // Construtor que recebe o contexto, lista de ingredientes e o listener
    public IngredienteAdapter(Context context, List<Ingrediente> ingredientes, OnIngredienteClickListener listener) {
        this.context = context;
        this.ingredientes = ingredientes;
        this.listener = listener;
    }

    // Cria uma nova visualização (view) para cada item na lista
    @NonNull
    @Override
    public IngredienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Infla o layout do item de ingrediente
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingrediente, parent, false);
        return new IngredienteViewHolder(view); // Retorna o ViewHolder com o item inflado
    }

    // Vincula os dados do ingrediente com o ViewHolder (armazena as referências das views de um item na Lista)
    @Override
    public void onBindViewHolder(@NonNull IngredienteViewHolder holder, int position) {
        Ingrediente ingrediente = ingredientes.get(position);
        holder.bind(ingrediente, listener);
    }

    // Retorna o número total de itens na lista de ingredientes
    @Override
    public int getItemCount() {
        return ingredientes.size();
    }


    // Interface para definir os métodos de interação com os itens (editar, excluir, adicionar quantidade)
    public interface OnIngredienteClickListener {
        void onEditClick(Ingrediente ingrediente);
        void onDeleteClick(Ingrediente ingrediente);
        void onAddQuantityClick(Ingrediente ingrediente); // Novo método adicionado
    }

    // ViewHolder que representa cada item da lista (ingrediente)
    public static class IngredienteViewHolder extends RecyclerView.ViewHolder {
        private TextView textNome;
        private TextView textQuantidade;
        private ImageButton btnEditar;
        private ImageButton btnAdicionar; // Botão "+" para adicionar quantidade

        // Construtor que inicializa os componentes da interface
        public IngredienteViewHolder(@NonNull View itemView) {
            super(itemView);
            textNome = itemView.findViewById(R.id.textNome);
            textQuantidade = itemView.findViewById(R.id.textQuantidade);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnAdicionar = itemView.findViewById(R.id.btnAdicionar);
        }

        // Método que vincula os dados do ingrediente ao ViewHolder
        public void bind(final Ingrediente ingrediente, final OnIngredienteClickListener listener) {
            // Define o nome do ingrediente, se não for nulo
            textNome.setText(ingrediente.getNome() != null ? ingrediente.getNome() : "Nome indisponível");

            // Calcula a quantidade de acordo com a unidade de medida (gramas, mililitros, unidades)
            String tipoMedida = ingrediente.getTipoMedida() != null ? ingrediente.getTipoMedida() : "";
            double quantidade = ingrediente.getQuantidade();
            double unidadeMedida = ingrediente.getUnidadeMedida();
            String quantidadeText;

            // Formata a quantidade para exibição com base no tipo de medida
            if (tipoMedida.equals("Unidades")) {
                quantidadeText = String.format("%.0f uni", quantidade); // Exibe unidades
            } else {
                double totalQuantidade = quantidade * unidadeMedida; // Calcula a quantidade total considerando a unidade
                DecimalFormat decimalFormat = new DecimalFormat("#.##"); // Formatação para 2 casas decimais

                if (tipoMedida.equals("Gramas")) {
                    if (totalQuantidade >= 1000) {
                        quantidadeText = String.format("%.1f Kg", totalQuantidade / 1000); // Exibe em Kg se maior que 1000g
                    } else {
                        quantidadeText = String.format("%.0f g", totalQuantidade); // Exibe em gramas
                    }
                } else if (tipoMedida.equals("Mililitros")) {
                    if (totalQuantidade >= 1000) {
                        quantidadeText = String.format("%.1f L", totalQuantidade / 1000); // Exibe em Litros se maior que 1000ml
                    } else {
                        quantidadeText = String.format("%.0f ml", totalQuantidade); // Exibe em mililitros
                    }
                } else {
                    quantidadeText = String.format("%.0f %s", totalQuantidade, tipoMedida); // Exibe o valor com a unidade
                }
            }

            // Log para depuração (exibe os valores de nome e quantidade)
            Log.d("DebugAdapter", "Ingrediente: " + ingrediente.getNome() + ", Quantidade exibida: " + quantidadeText);

            // Exibe a quantidade formatada
            textQuantidade.setText(quantidadeText);


            // Configura o clique no botão de editar para abrir a tela de edição
            btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(ingrediente); // Aciona o método de edição
                }
            });


            // Configura o clique no botão de adicionar quantidade
            btnAdicionar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddQuantityClick(ingrediente); // Aciona o método de add (btn +)
                }
            });

            // Configura o clique longo para excluir o ingrediente
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(ingrediente); // Aciona o método de exclusão
                }
                return true; // Retorna verdadeiro para indicar que o evento foi tratado
            });
        }
    }
}
