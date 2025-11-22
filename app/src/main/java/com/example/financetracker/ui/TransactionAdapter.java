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
    private OnTransactionClickListener clickListener;
    private OnTransactionLongClickListener longClickListener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    public interface OnTransactionLongClickListener {
        void onTransactionLongClick(Transaction transaction);
    }

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnTransactionLongClickListener(OnTransactionLongClickListener listener) {
        this.longClickListener = listener;
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

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTransactionClick(transaction);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onTransactionLongClick(transaction);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView categoryText;
        private TextView descriptionText;
        private TextView dateTimeText;
        private TextView amountText;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.categoryText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            dateTimeText = itemView.findViewById(R.id.dateTimeText);
            amountText = itemView.findViewById(R.id.amountText);
        }

        public void bind(Transaction transaction) {
            categoryText.setText(transaction.getCategory());
            descriptionText.setText(transaction.getDescription());

            // Format date and time together
            String dateTime = formatDateTime(transaction.getDate(), transaction.getTime());
            dateTimeText.setText(dateTime);

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

        private String formatDateTime(String date, String time) {
            try {
                // Convert 2024-01-01 to 2024.01.01
                String formattedDate = date.replace("-", ".");
                return formattedDate + " " + time;
            } catch (Exception e) {
                return date + " " + time;
            }
        }
    }
}
