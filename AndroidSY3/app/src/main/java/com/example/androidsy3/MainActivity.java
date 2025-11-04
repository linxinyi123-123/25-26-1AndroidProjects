package com.example.androidsy3;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnListView, btnLoginDialog, btnMenuTest, btnActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setClickListeners();
    }

    private void initViews() {
        btnListView = findViewById(R.id.btn_list_view);
        btnLoginDialog = findViewById(R.id.btn_login_dialog);
        btnMenuTest = findViewById(R.id.btn_menu_test);
        btnActionMode = findViewById(R.id.btn_action_mode);
    }

    private void setClickListeners() {
        btnListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListViewActivity.class);
                startActivity(intent);
            }
        });

        btnLoginDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginDialogActivity.class);
                startActivity(intent);
            }
        });

        btnMenuTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MenuTestActivity.class);
                startActivity(intent);
            }
        });

        btnActionMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ActionModeActivity.class);
                startActivity(intent);
            }
        });
    }
}