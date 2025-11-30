package com.example.financetracker.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;
import com.example.financetracker.model.NotificationHistory;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class NotificationHistoryAdapter extends RecyclerView.Adapter<NotificationHistoryAdapter.ViewHolder> {

    private List<NotificationHistory> notifications;
    private final OnNotificationClickListener clickListener;
    private final OnNotificationDeleteListener deleteListener;

    public interface OnNotificationClickListener {
        void onClick(NotificationHistory notification);
    }

    public interface OnNotificationDeleteListener {
        void onDelete(NotificationHistory notification);
    }

    public NotificationHistoryAdapter(List<NotificationHistory> notifications,
                                     OnNotificationClickListener clickListener,
                                     OnNotificationDeleteListener deleteListener) {
        this.notifications = notifications;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationHistory notification = notifications.get(position);

        String typeText = "INCOME".equals(notification.getType()) ? "입금" : "결제";

        try {
            long amount = Long.parseLong(notification.getAmount());
            String formattedAmount = NumberFormat.getNumberInstance(Locale.KOREA).format(amount);
            holder.amountText.setText(formattedAmount + "원 " + typeText);
        } catch (NumberFormatException e) {
            holder.amountText.setText(notification.getAmount() + "원 " + typeText);
        }

        holder.descriptionText.setText(notification.getDescription());
        holder.dateTimeText.setText(notification.getDate() + " " + notification.getTime());

        // Click on the item to navigate to AddTransactionActivity
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(notification);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    public void setNotifications(List<NotificationHistory> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView amountText;
        TextView descriptionText;
        TextView dateTimeText;
        ImageButton deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            amountText = itemView.findViewById(R.id.notificationAmountText);
            descriptionText = itemView.findViewById(R.id.notificationDescriptionText);
            dateTimeText = itemView.findViewById(R.id.notificationDateTimeText);
            deleteButton = itemView.findViewById(R.id.deleteNotificationButton);
        }
    }
}
