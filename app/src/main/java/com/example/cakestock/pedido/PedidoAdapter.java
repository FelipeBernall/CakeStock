package com.example.cakestock.pedido;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.cakestock.R;
import java.util.List;

// Gerencia como cada item da lista de pedidos será exibido
public class PedidoAdapter extends BaseAdapter {
    private Context context;
    private List<Pedido> pedidos;

    // Construtor que inicializa o contexto e a lista de pedidos
    public PedidoAdapter(Context context, List<Pedido> pedidos) {
        this.context = context;
        this.pedidos = pedidos;
    }

    // Retorna o número de itens na lista
    @Override
    public int getCount() {
        return pedidos.size();
    }

    // Retorna o pedido na posição especificada
    @Override
    public Object getItem(int position) {
        return pedidos.get(position);
    }

    // Retorna o ID do item (neste caso, a posição)
    @Override
    public long getItemId(int position) {
        return position;
    }

    // Define como cada item da lista será exibido
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_pedido, parent, false);
        }

        // Recupera o pedido correspondente à posição atual
        Pedido pedido = pedidos.get(position);

        // Referencia os componentes de texto no layout
        TextView tvDescricao = convertView.findViewById(R.id.tvDescricao);
        TextView tvData = convertView.findViewById(R.id.tvData);
        TextView tvCliente = convertView.findViewById(R.id.tvCliente);

        // Preenche os textos com as informações do pedido
        tvDescricao.setText(pedido.getDescricao());
        tvData.setText(pedido.getData());
        tvCliente.setText(pedido.getCliente());

        return convertView; // Retorna a view preenchida
    }
}
