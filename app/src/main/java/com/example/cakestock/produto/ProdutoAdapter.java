package com.example.cakestock.produto;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
            holder.btnExcluir = convertView.findViewById(R.id.btnExcluir);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Produto produto = produtoList.get(position);
        holder.textNomeProduto.setText(produto.getNome());
        holder.textQuantidadeProduto.setText(String.valueOf(produto.getQuantidade()));

        // Ação do botão Editar
        holder.btnEditarProduto.setOnClickListener(v -> {
            // Inicia a atividade de edição
            Intent intent = new Intent(context, CadastroProduto.class);
            intent.putExtra("produtoId", produto.getId());
            context.startActivity(intent);
        });

        // Ação do botão Excluir
        holder.btnExcluir.setOnClickListener(v -> {
            excluirProduto(produto.getId());
        });

        return convertView;
    }

    private void excluirProduto(String produtoId) {
        // Exibe um alerta de confirmação
        new AlertDialog.Builder(context)
                .setTitle("Confirmação")
                .setMessage("Você tem certeza que deseja excluir este produto?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    // Realiza a exclusão no Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    db.collection("Usuarios").document(userId).collection("Produtos").document(produtoId)
                            .delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "Produto excluído com sucesso.", Toast.LENGTH_SHORT).show();
                                    // Atualiza a lista
                                    notifyDataSetChanged();
                                } else {
                                    Toast.makeText(context, "Erro ao excluir produto.", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private static class ViewHolder {
        TextView textNomeProduto;
        TextView textQuantidadeProduto;
        ImageButton btnEditarProduto;
        ImageButton btnExcluir;
    }
}
