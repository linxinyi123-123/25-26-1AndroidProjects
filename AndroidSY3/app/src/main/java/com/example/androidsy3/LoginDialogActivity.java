package com.example.androidsy3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_dialog);

        // 显示登录对话框
        showLoginDialog();
    }

    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 加载自定义布局
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_login, null);
        builder.setView(dialogView);

        // 获取布局中的控件
        EditText etUsername = dialogView.findViewById(R.id.et_username);
        EditText etPassword = dialogView.findViewById(R.id.et_password);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSignIn = dialogView.findViewById(R.id.btn_sign_in);

        AlertDialog dialog = builder.create();

        // 设置取消按钮点击事件
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish(); // 关闭当前Activity
            }
        });

        // 设置登录按钮点击事件
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginDialogActivity.this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginDialogActivity.this,
                            "登录成功！\n用户名: " + username, Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    finish(); // 关闭当前Activity
                }
            }
        });

        // 设置对话框取消监听
        dialog.setOnCancelListener(dialogInterface -> {
            finish(); // 关闭当前Activity
        });

        dialog.show();
    }
}