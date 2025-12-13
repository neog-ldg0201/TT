package com.example.financetracker.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.financetracker.R;
import com.example.financetracker.utils.BackupManager;
import com.example.financetracker.utils.DateUtils;

public class DataManagementActivity extends AppCompatActivity {

    private BackupManager backupManager;
    private Button exportButton;
    private Button importButton;

    private ActivityResultLauncher<String> exportLauncher;
    private ActivityResultLauncher<String[]> importLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_management);

        backupManager = new BackupManager(this);

        setupToolbar();
        setupLaunchers();
        initViews();
        setupButtons();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupLaunchers() {
        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/json"),
                uri -> {
                    if (uri != null) {
                        exportData(uri);
                    }
                }
        );

        importLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        importData(uri);
                    }
                }
        );
    }

    private void initViews() {
        exportButton = findViewById(R.id.exportButton);
        importButton = findViewById(R.id.importButton);
    }

    private void setupButtons() {
        exportButton.setOnClickListener(v -> {
            String fileName = "동계부_백업_" + DateUtils.getCurrentDate() + ".json";
            exportLauncher.launch(fileName);
        });

        importButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("데이터 불러오기")
                    .setMessage("데이터를 불러오면 기존 데이터가 모두 삭제됩니다. 계속하시겠습니까?")
                    .setPositiveButton("계속", (dialog, which) -> {
                        importLauncher.launch(new String[]{"application/json"});
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });
    }

    private void exportData(Uri uri) {
        backupManager.exportData(uri, new BackupManager.ExportCallback() {
            @Override
            public void onSuccess(int count) {
                runOnUiThread(() -> {
                    Toast.makeText(DataManagementActivity.this,
                            count + "개의 거래 내역이 저장되었습니다",
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(DataManagementActivity.this,
                            "저장 실패: " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void importData(Uri uri) {
        backupManager.importData(uri, new BackupManager.ImportCallback() {
            @Override
            public void onSuccess(int count) {
                runOnUiThread(() -> {
                    Toast.makeText(DataManagementActivity.this,
                            count + "개의 거래 내역이 복원되었습니다",
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(DataManagementActivity.this,
                            "불러오기 실패: " + error,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
