package com.example.financetracker.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;
import com.example.financetracker.model.Transaction;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView categoryText;
        private TextView descriptionText;
        private TextView timeText;
        private TextView amountText;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.categoryText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            timeText = itemView.findViewById(R.id.timeText);
            amountText = itemView.findViewById(R.id.amountText);
        }

        public void bind(Transaction transaction) {
            categoryText.setText(transaction.getCategory());
            descriptionText.setText(transaction.getDescription());
            timeText.setText(transaction.getTime());

            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.KOREA);
            String formattedAmount = formatter.format(transaction.getAmount()) + "원";

            if ("INCOME".equals(transaction.getType())) {
                amountText.setText("+" + formattedAmount);
                amountText.setTextColor(itemView.getContext().getResources()
                        .getColor(R.color.income_color));
            } else {
                amountText.setText("-" + formattedAmount);
                amountText.setTextColor(itemView.getContext().getResources()
                        .getColor(R.color.expense_color));
            }
        }
    }
}
