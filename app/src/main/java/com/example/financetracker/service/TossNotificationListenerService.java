package com.example.financetracker.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.financetracker.R;
import com.example.financetracker.data.NotificationHistoryDao;
import com.example.financetracker.database.AppDatabase;
import com.example.financetracker.model.NotificationHistory;
import com.example.financetracker.ui.AddTransactionActivity;
import com.example.financetracker.utils.DateUtils;
import com.example.financetracker.utils.TossNotificationParser;

import java.util.Map;

public class TossNotificationListenerService extends NotificationListenerService {

    private static final String TAG = "TossNotificationListener";
    private static final String TOSS_PACKAGE = "viva.republica.toss";
    private static final String CHANNEL_ID = "finance_tracker_channel";
    private static final int NOTIFICATION_ID = 1000;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Log.d(TAG, "TossNotificationListenerService created");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        // Check if notification is from Toss
        if (!TOSS_PACKAGE.equals(packageName)) {
            return;
        }

        Log.d(TAG, "Toss notification detected");

        Notification notification = sbn.getNotification();
        if (notification == null) {
            return;
        }

        // Extract notification content
        CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);

        if (title == null || text == null) {
            return;
        }

        String titleStr = title.toString();
        String textStr = text.toString();

        Log.d(TAG, "Title: " + titleStr);
        Log.d(TAG, "Text: " + textStr);

        // Parse notification content
        Map<String, String> parsedData = TossNotificationParser.parse(titleStr, textStr);

        if (parsedData != null && parsedData.containsKey("amount")) {
            showTransactionNotification(parsedData);
        }
    }

    private void showTransactionNotification(Map<String, String> data) {
        String amount = data.get("amount");
        String description = data.get("description");
        String type = data.get("type"); // "INCOME" or "EXPENSE"

        // Save to notification history
        String currentDate = DateUtils.getCurrentDate();
        String currentTime = DateUtils.getCurrentTime();
        long timestamp = System.currentTimeMillis();

        NotificationHistory notificationHistory = new NotificationHistory(
                amount, description, type, currentDate, currentTime, timestamp
        );

        new Thread(() -> {
            NotificationHistoryDao dao = AppDatabase.getInstance(this).notificationHistoryDao();
            dao.insert(notificationHistory);
            Log.d(TAG, "Notification saved to history");
        }).start();

        // Format amount with commas for display
        String formattedAmount = formatAmount(amount);
        String typeText = "INCOME".equals(type) ? "입금" : "결제";

        // Create intent to open AddTransactionActivity
        Intent intent = new Intent(this, AddTransactionActivity.class);
        intent.putExtra("amount", amount);
        intent.putExtra("description", description);
        intent.putExtra("type", type);  // Pass transaction type
        intent.putExtra("date", DateUtils.getCurrentDate());
        intent.putExtra("time", DateUtils.getCurrentTime());
        intent.putExtra("isFromNotification", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(), // Unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification with clear message
        String notificationTitle = String.format("💰 %s원 %s", formattedAmount, typeText);
        String notificationText = String.format("%s - 가계부에 등록하시겠습니까?", description);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_add, "등록하기", pendingIntent);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            // Use unique notification ID for each transaction
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Transaction notification shown - Amount: " + amount + ", Type: " + type);
        }
    }

    private String formatAmount(String amount) {
        try {
            long value = Long.parseLong(amount);
            return String.format("%,d", value);
        } catch (NumberFormatException e) {
            return amount;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "거래 알림";
            String description = "토스 거래 감지 알림";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Handle notification removal if needed
    }
}
