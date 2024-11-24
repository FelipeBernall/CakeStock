package com.example.cakestock.financeiro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.cakestock.R;

import java.util.List;

public class TransacaoAdapter extends BaseAdapter {

    private final Context context;
    private final List<Transacao> transacoes;

    public TransacaoAdapter(Context context, List<Transacao> transacoes) {
        this.context = context;
        this.transacoes = transacoes;
    }

    @Override
    public int getCount() {
        return transacoes.size();
    }

    @Override
    public Object getItem(int position) {
        return transacoes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_transacao, parent, false);
        }

        Transacao transacao = transacoes.get(position);

        TextView tvDescricao = convertView.findViewById(R.id.tv_descricao);
        TextView tvData = convertView.findViewById(R.id.tv_data);
        TextView tvValor = convertView.findViewById(R.id.tv_valor);

        tvDescricao.setText(transacao.getDescricao());
        tvData.setText(transacao.getData());
        tvValor.setText(String.format("R$ %.2f", Math.abs(transacao.getValorTotal())));

        // Define a cor do valor (verde para vendas, vermelho para despesas)
        if (transacao.getValorTotal() >= 0) {
            tvValor.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvValor.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }

        return convertView;
    }

}
