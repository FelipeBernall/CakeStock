package com.example.cakestock.cliente;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.cakestock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ClienteAdapter extends ArrayAdapter<Cliente> {
    private Context context;
    private List<Cliente> clientes;
    private FirebaseFirestore db; // Para interagir com o Firebase

    public ClienteAdapter(Context context, List<Cliente> clientes) {
        super(context, 0, clientes);
        this.context = context;
        this.clientes = clientes;
        this.db = FirebaseFirestore.getInstance(); // Inicializa o Firestore
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Cliente cliente = getItem(position);

        if (convertView == null) {
            // Aqui, alteramos para utilizar o layout 'item_cliente' personalizado
            convertView = LayoutInflater.from(context).inflate(R.layout.item_cliente, parent, false);
        }

        // Pegando as referências dos componentes do layout 'item_cliente.xml'
        TextView nomeTextView = convertView.findViewById(R.id.textViewNome);
        TextView telefoneTextView = convertView.findViewById(R.id.textViewTelefone);
        Switch switchAtivo = convertView.findViewById(R.id.switchAtivo);
        ImageButton buttonEditar = convertView.findViewById(R.id.buttonEditar);

        // Definindo os valores do cliente
        nomeTextView.setText(cliente.getNome());
        telefoneTextView.setText(cliente.getTelefone());

        // Evitar que alterações programáticas acionem o listener
        switchAtivo.setOnCheckedChangeListener(null);
        switchAtivo.setChecked(cliente.isAtivo());

        // Ajustando a transparência com base no status do cliente
        convertView.setAlpha(cliente.isAtivo() ? 1.0f : 0.5f);

        // Clique para editar o cliente
        buttonEditar.setOnClickListener(v -> {
            // Abrir a tela de edição do cliente
            Intent intent = new Intent(context, CadastroCliente.class);
            intent.putExtra("clienteId", cliente.getId());
            context.startActivity(intent);
        });

        // Configura o listener para o Switch
        // Dentro da classe ClienteAdapter

        switchAtivo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                new AlertDialog.Builder(context)
                        .setTitle("Desativar Cliente")
                        .setMessage("Deseja realmente desativar este cliente? Ele não poderá ser relacionado a futuras vendas.")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            cliente.setAtivo(false);
                            atualizarStatusCliente(cliente);
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            // Reverter a alteração do Switch sem acionar o listener
                            switchAtivo.setOnCheckedChangeListener(null);
                            switchAtivo.setChecked(true);  // Mantém o cliente como ativo
                            switchAtivo.setOnCheckedChangeListener(this::handleSwitchChange);
                        })
                        .setCancelable(false)
                        .show();
            } else {
                new AlertDialog.Builder(context)
                        .setTitle("Ativar Cliente")
                        .setMessage("Deseja realmente ativar este cliente?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            cliente.setAtivo(true);
                            atualizarStatusCliente(cliente);
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            // Reverter a alteração do Switch sem acionar o listener
                            switchAtivo.setOnCheckedChangeListener(null);
                            switchAtivo.setChecked(false); // Mantém o cliente como inativo
                            switchAtivo.setOnCheckedChangeListener(this::handleSwitchChange);
                        })
                        .setCancelable(false)
                        .show();
            }
        });


        return convertView;
    }

    private void atualizarStatusCliente(Cliente cliente) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Usuarios").document(userId).collection("Clientes").document(cliente.getId())
                .set(cliente)
                .addOnSuccessListener(aVoid -> {
                    String mensagem = cliente.isAtivo() ? "Cliente ativado com sucesso." : "Cliente desativado com sucesso.";
                    Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show();
                    // Atualizar a transparência da view
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    String mensagem = cliente.isAtivo() ? "Erro ao ativar o cliente." : "Erro ao desativar o cliente.";
                    Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show();
                    // Reverter o estado do Switch
                    switchAtivoRevert(cliente);
                });
    }

    // Método auxiliar para reverter o Switch em caso de falha na atualização
    private void switchAtivoRevert(Cliente cliente) {
        // Procurar a posição do cliente na lista
        int position = clientes.indexOf(cliente);
        if (position != -1) {
            Cliente clienteAtual = getItem(position);
            if (clienteAtual != null) {
                clienteAtual.setAtivo(!cliente.isAtivo());
                notifyDataSetChanged();
            }
        }
    }

    // Método para lidar com alterações do Switch (usado para reatribuir o listener)
    private void handleSwitchChange(android.widget.CompoundButton buttonView, boolean isChecked) {
        // Este método pode ser usado para reatribuir o listener se necessário
    }
}
