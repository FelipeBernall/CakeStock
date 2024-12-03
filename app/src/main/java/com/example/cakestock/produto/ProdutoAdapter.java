package com.example.cakestock.produto;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.cakestock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

// Gerencia como os produtos são exibidos na lista
public class ProdutoAdapter extends BaseAdapter {
    private Context context;
    private List<Produto> produtoList;

    // Construtor para inicializar o adapter com o contexto e a lista de produtos
    public ProdutoAdapter(Context context, List<Produto> produtoList) {
        this.context = context;
        this.produtoList = produtoList;
    }

    // Retorna a quantidade de itens na lista
    @Override
    public int getCount() {
        return produtoList.size();
    }

    // Retorna o produto em uma posição específica
    @Override
    public Object getItem(int position) {
        return produtoList.get(position);
    }

    // Retorna o ID do item na posição especificada (a posição é usada como ID)
    @Override
    public long getItemId(int position) {
        return position;
    }

    // Método que cria ou atualiza a exibição de cada item na lista
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // Infla o layout do item da lista se ainda não foi criado
            convertView = LayoutInflater.from(context).inflate(R.layout.item_produto, parent, false);
            holder = new ViewHolder();
            holder.textNomeProduto = convertView.findViewById(R.id.textNomeProduto);
            holder.textQuantidadeProduto = convertView.findViewById(R.id.textQuantidadeProduto);
            holder.btnEditarProduto = convertView.findViewById(R.id.btnEditarProduto);
            holder.btnAdicionar = convertView.findViewById(R.id.btnAdicionar);
            convertView.setTag(holder);
        } else {
            // Reutiliza o ViewHolder existente para evitar recriação desnecessária
            holder = (ViewHolder) convertView.getTag();
        }

        // Obtém o produto correspondente à posição atual
        Produto produto = produtoList.get(position);
        // Exibe o nome e a quantidade do produto
        holder.textNomeProduto.setText(produto.getNome());
        holder.textQuantidadeProduto.setText(String.valueOf(produto.getQuantidade()));

        // Configura a ação do botão Editar
        holder.btnEditarProduto.setOnClickListener(v -> {
            // Abre a tela de cadastro para edição, passando o ID do produto
            Intent intent = new Intent(context, CadastroProduto.class);
            intent.putExtra("produtoId", produto.getId());
            context.startActivity(intent);
        });

        // Ação do botão Adicionar
        holder.btnAdicionar.setOnClickListener(v -> {
            // Mostra um diálogo para adicionar quantidade ao estoque
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Adicionar Estoque");

            final EditText input = new EditText(context);
            input.setHint("Quantidade a adicionar"); // Sugestão de entrada
            builder.setView(input);

            builder.setPositiveButton("Confirmar", (dialog, which) -> {
                String quantidadeStr = input.getText().toString();
                if (!quantidadeStr.isEmpty()) {
                    int quantidadeAdicionar = Integer.parseInt(quantidadeStr);
                    atualizarEstoque(produto, quantidadeAdicionar); // Atualiza o estoque no Firebas
                } else {
                    Toast.makeText(context, "Por favor, insira uma quantidade.", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });

        // Clique longo para excluir produto
        convertView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)        // Mostra um diálogo para confirmar a exclusão
                    .setTitle("Excluir Produto")
                    .setMessage("Você tem certeza que deseja excluir este produto?")
                    .setPositiveButton("Sim", (dialog, which) -> excluirProduto(produto))
                    .setNegativeButton("Não", null)
                    .show();
            return true; // Indica que o clique longo foi consumido
        });

        return convertView;
    }

    // Método para atualizar o estoque de um produto no Firebase
    private void atualizarEstoque(Produto produto, int quantidadeAdicionar) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        int novaQuantidade = produto.getQuantidade() + quantidadeAdicionar; // Atualiza a quantidade localmente
        db.collection("Usuarios").document(userId).collection("Produtos").document(produto.getId())
                .update("quantidade", novaQuantidade) // Atualiza no Firebase
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) { // Atualiza a lista local
                        produto.setQuantidade(novaQuantidade);
                        notifyDataSetChanged(); // Notifica o adapter para atualizar a lista
                        Toast.makeText(context, "Estoque atualizado.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Erro ao atualizar estoque.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método para excluir um produto no Firebase
    private void excluirProduto(Produto produto) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Usuarios").document(userId).collection("Produtos").document(produto.getId())
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        produtoList.remove(produto); // Remove o produto da lista local
                        notifyDataSetChanged();
                        Toast.makeText(context, "Produto excluído com sucesso.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Erro ao excluir produto.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ViewHolder para armazenar referências aos componentes da interface
    private static class ViewHolder {
        TextView textNomeProduto;
        TextView textQuantidadeProduto;
        ImageButton btnEditarProduto;
        ImageButton btnAdicionar;
    }
}
