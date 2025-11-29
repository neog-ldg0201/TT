package com.example.financetracker.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;
import com.example.financetracker.data.NotificationHistoryDao;
import com.example.financetracker.database.AppDatabase;
import com.example.financetracker.model.NotificationHistory;

import java.util.ArrayList;
import java.util.List;

public class NotificationHistoryActivity extends AppCompatActivity {

    private RecyclerView notificationRecyclerView;
    private NotificationHistoryAdapter adapter;
    private NotificationHistoryDao notificationDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_history);

        notificationDao = AppDatabase.getInstance(this).notificationHistoryDao();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        notificationRecyclerView = findViewById(R.id.notificationRecyclerView);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new NotificationHistoryAdapter(new ArrayList<>(), notification -> {
            showDeleteConfirmDialog(notification);
        });
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationRecyclerView.setAdapter(adapter);
    }

    private void loadNotifications() {
        LiveData<List<NotificationHistory>> notifications = notificationDao.getAllNotifications();
        notifications.observe(this, notificationList -> {
            if (notificationList != null) {
                adapter.setNotifications(notificationList);
            }
        });
    }

    private void showDeleteConfirmDialog(NotificationHistory notification) {
        new AlertDialog.Builder(this)
                .setTitle("알림 삭제")
                .setMessage("이 알림 내역을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    new Thread(() -> {
                        notificationDao.delete(notification);
                        runOnUiThread(() -> Toast.makeText(this, "알림이 삭제되었습니다", Toast.LENGTH_SHORT).show());
                    }).start();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
