package com.example.cakestock.receita;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import android.widget.ArrayAdapter;

import com.example.cakestock.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

// Exibe o histórico de produções na Lista
public class HistoricoAdapter extends ArrayAdapter<Producao> {
    public HistoricoAdapter(Context context, ArrayList<Producao> producoes) {
        super(context, 0, producoes);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Obtém o item de produção atual
        Producao producao = getItem(position);

        // Verifica se a view existente está sendo reutilizada, caso contrário, infla a view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_producao, parent, false);
        }

        // Referência para os elementos de interface no layout item_producao
        TextView nomeReceita = convertView.findViewById(R.id.nomeReceita);
        TextView quantidadeProduzida = convertView.findViewById(R.id.quantidadeProduzida);
        TextView dataProducao = convertView.findViewById(R.id.dataProducao);

        // Define os valores de produção nos elementos visuais
        nomeReceita.setText(producao.getNomeReceita());
        quantidadeProduzida.setText(String.valueOf(producao.getQuantidadeProduzida()));

        // Formata a data para o padrão brasileiro: dd-MM-yyyy HH:mm
        String dataFormatada = formatarData(producao.getDataProducao());
        dataProducao.setText(dataFormatada);

        return convertView;
    }

    // Método para formatar a data no padrão desejado
    private String formatarData(String dataOriginal) {
        // Define o formato original da data recebida
        SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        // Define o novo formato para a data
        SimpleDateFormat formatoDesejado = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        try {
            // Faz a conversão
            Date data = formatoOriginal.parse(dataOriginal);
            return formatoDesejado.format(data);
        } catch (ParseException e) {
            e.printStackTrace();
            // Em caso de erro, retorna a data original sem alterações
            return dataOriginal;
        }
    }
}
