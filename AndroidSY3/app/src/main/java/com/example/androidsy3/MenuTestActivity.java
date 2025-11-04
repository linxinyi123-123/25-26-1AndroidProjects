package com.example.androidsy3;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MenuTestActivity extends AppCompatActivity {

    private TextView testTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_test);

        testTextView = findViewById(R.id.test_text_view);
        testTextView.setText("用于测试的内容");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_small_font) {
            testTextView.setTextSize(10);
            return true;
        } else if (id == R.id.menu_medium_font) {
            testTextView.setTextSize(16);
            return true;
        } else if (id == R.id.menu_large_font) {
            testTextView.setTextSize(20);
            return true;
        } else if (id == R.id.menu_normal_item) {
            Toast.makeText(this, "普通菜单项被点击", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_red_color) {
            testTextView.setTextColor(Color.RED);
            return true;
        } else if (id == R.id.menu_black_color) {
            testTextView.setTextColor(Color.BLACK);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}