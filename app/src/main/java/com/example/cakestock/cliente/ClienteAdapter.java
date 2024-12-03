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

// Define como cada cliente aparece na lista
public class ClienteAdapter extends ArrayAdapter<Cliente> {
    private Context context;
    private List<Cliente> clientes;
    private FirebaseFirestore db; // Para interagir com o Firebase

    // Construtor para inicializar o adapter com o contexto e a lista de clientes
    public ClienteAdapter(Context context, List<Cliente> clientes) {
        super(context, 0, clientes);
        this.context = context;
        this.clientes = clientes;
        this.db = FirebaseFirestore.getInstance(); // Inicializa o Firestore
    }

    // Método responsável por configurar a visualização de cada item da lista.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Obtém o cliente correspondente à posição atual
        Cliente cliente = getItem(position);

        // Infla o layout se ele ainda não foi criado
        if (convertView == null) {
            // Aqui, alteramos para utilizar o layout 'item_cliente' personalizado
            convertView = LayoutInflater.from(context).inflate(R.layout.item_cliente, parent, false);
        }

        // Referências aos elementos visuais do layout
        TextView nomeTextView = convertView.findViewById(R.id.textViewNome);
        TextView telefoneTextView = convertView.findViewById(R.id.textViewTelefone);
        Switch switchAtivo = convertView.findViewById(R.id.switchAtivo);
        ImageButton buttonEditar = convertView.findViewById(R.id.buttonEditar);

        // Definindo os valores do cliente
        nomeTextView.setText(cliente.getNome());
        telefoneTextView.setText(cliente.getTelefone());

        // Configura o estado do switch de ativação
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

        // Configura o listener para ativar/desativar o cliente ao clicar no switch
        switchAtivo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                // Confirmação antes de desativar o cliente
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
                // Confirmação antes de ativar o cliente
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

    // Atualiza o status de ativação do cliente no Firestore.
    private void atualizarStatusCliente(Cliente cliente) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Usuarios").document(userId).collection("Clientes").document(cliente.getId())
                .set(cliente)
                .addOnSuccessListener(aVoid -> {
                    String mensagem = cliente.isAtivo() ? "Cliente ativado com sucesso." : "Cliente desativado com sucesso.";
                    Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show();
                    // Atualizar a transparência da view
                    notifyDataSetChanged(); // Atualiza a lista
                })
                .addOnFailureListener(e -> {
                    String mensagem = cliente.isAtivo() ? "Erro ao ativar o cliente." : "Erro ao desativar o cliente.";
                    Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show();
                    // Reverter o estado do Switch
                    switchAtivoRevert(cliente); // Reverte o estado caso ocorra um erro
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
