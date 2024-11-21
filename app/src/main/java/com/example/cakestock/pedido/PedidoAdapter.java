package com.example.cakestock.pedido;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.cakestock.R;
import java.util.List;

public class PedidoAdapter extends BaseAdapter {
    private Context context;
    private List<Pedido> pedidos;

    public PedidoAdapter(Context context, List<Pedido> pedidos) {
        this.context = context;
        this.pedidos = pedidos;
    }

    @Override
    public int getCount() {
        return pedidos.size();
    }

    @Override
    public Object getItem(int position) {
        return pedidos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_pedido, parent, false);
        }

        Pedido pedido = pedidos.get(position);

        TextView tvDescricao = convertView.findViewById(R.id.tvDescricao);
        TextView tvData = convertView.findViewById(R.id.tvData);
        TextView tvCliente = convertView.findViewById(R.id.tvCliente);

        tvDescricao.setText(pedido.getDescricao());
        tvData.setText(pedido.getData());
        tvCliente.setText(pedido.getCliente());

        return convertView;
    }
}
