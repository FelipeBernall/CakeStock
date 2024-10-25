package com.example.cakestock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ReceitaAdapter extends ArrayAdapter<Receita> {

    private int resourceLayout;
    private Context mContext;

    public ReceitaAdapter(@NonNull Context context, int resource, @NonNull List<Receita> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        // Verifica se a View já existe, caso contrário, infla o layout
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(resourceLayout, null);
        }

        // Obtém a receita atual
        Receita receita = getItem(position);

        if (receita != null) {
            // Configura o nome da receita no TextView
            TextView tvNomeReceita = view.findViewById(R.id.tv_nome_receita);
            if (tvNomeReceita != null) {
                tvNomeReceita.setText(receita.getNome());
            }
        }

        return view;
    }
}
