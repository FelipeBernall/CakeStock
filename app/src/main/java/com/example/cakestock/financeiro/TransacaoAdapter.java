package com.example.cakestock.financeiro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.cakestock.R;

import java.util.List;

// Define como cada tranação aparece na LIsta
public class TransacaoAdapter extends BaseAdapter {

    private final Context context;
    private final List<Transacao> transacoes;

    // Construtor para inicializar o adapter com o contexto e a lista de transações
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

    // Método responsável por configurar a visualização de cada item da lista.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_transacao, parent, false);
        }

        Transacao transacao = transacoes.get(position);

        // Referências aos elementos visuais do layout
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
