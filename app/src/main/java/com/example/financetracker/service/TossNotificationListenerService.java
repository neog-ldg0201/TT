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

        // Create intent to open AddTransactionActivity
        Intent intent = new Intent(this, AddTransactionActivity.class);
        intent.putExtra("amount", amount);
        intent.putExtra("description", description);
        intent.putExtra("date", DateUtils.getCurrentDate());
        intent.putExtra("time", DateUtils.getCurrentTime());
        intent.putExtra("isFromNotification", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(String.format("%s원 %s - 등록하시겠습니까?",
                        amount, "EXPENSE".equals(type) ? "지출" : "수입"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_add, getString(R.string.register), pendingIntent);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Transaction notification shown");
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
