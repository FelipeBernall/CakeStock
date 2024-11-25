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

public class ProdutoAdapter extends BaseAdapter {
    private Context context;
    private List<Produto> produtoList;

    public ProdutoAdapter(Context context, List<Produto> produtoList) {
        this.context = context;
        this.produtoList = produtoList;
    }

    @Override
    public int getCount() {
        return produtoList.size();
    }

    @Override
    public Object getItem(int position) {
        return produtoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_produto, parent, false);
            holder = new ViewHolder();
            holder.textNomeProduto = convertView.findViewById(R.id.textNomeProduto);
            holder.textQuantidadeProduto = convertView.findViewById(R.id.textQuantidadeProduto);
            holder.btnEditarProduto = convertView.findViewById(R.id.btnEditarProduto);
            holder.btnAdicionar = convertView.findViewById(R.id.btnAdicionar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Produto produto = produtoList.get(position);
        holder.textNomeProduto.setText(produto.getNome());
        holder.textQuantidadeProduto.setText(String.valueOf(produto.getQuantidade()));

        // Ação do botão Editar
        holder.btnEditarProduto.setOnClickListener(v -> {
            Intent intent = new Intent(context, CadastroProduto.class);
            intent.putExtra("produtoId", produto.getId());
            context.startActivity(intent);
        });

        // Ação do botão Adicionar
        holder.btnAdicionar.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Adicionar Estoque");

            final EditText input = new EditText(context);
            input.setHint("Quantidade a adicionar");
            builder.setView(input);

            builder.setPositiveButton("Confirmar", (dialog, which) -> {
                String quantidadeStr = input.getText().toString();
                if (!quantidadeStr.isEmpty()) {
                    int quantidadeAdicionar = Integer.parseInt(quantidadeStr);
                    atualizarEstoque(produto, quantidadeAdicionar);
                } else {
                    Toast.makeText(context, "Por favor, insira uma quantidade.", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });

        // Clique longo para excluir produto
        convertView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Excluir Produto")
                    .setMessage("Você tem certeza que deseja excluir este produto?")
                    .setPositiveButton("Sim", (dialog, which) -> excluirProduto(produto))
                    .setNegativeButton("Não", null)
                    .show();
            return true;
        });

        return convertView;
    }

    private void atualizarEstoque(Produto produto, int quantidadeAdicionar) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        int novaQuantidade = produto.getQuantidade() + quantidadeAdicionar;
        db.collection("Usuarios").document(userId).collection("Produtos").document(produto.getId())
                .update("quantidade", novaQuantidade)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        produto.setQuantidade(novaQuantidade);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Estoque atualizado.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Erro ao atualizar estoque.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void excluirProduto(Produto produto) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Usuarios").document(userId).collection("Produtos").document(produto.getId())
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        produtoList.remove(produto);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Produto excluído com sucesso.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Erro ao excluir produto.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static class ViewHolder {
        TextView textNomeProduto;
        TextView textQuantidadeProduto;
        ImageButton btnEditarProduto;
        ImageButton btnAdicionar;
    }
}
