package com.example.androidsy3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListViewActivity extends AppCompatActivity {

    private String[] animalNames = {"Lion", "Cat", "Monkey", "Dog", "Elephant", "Tiger"};
    private int[] animalImages = {
            R.drawable.lion,
            R.drawable.cat,
            R.drawable.monkey,
            R.drawable.dog,
            R.drawable.elephant,
            R.drawable.tiger,
    };
    private String NOTIFICATION_CHANNEL_ID = "MY_LISTVIEW_CHANNEL";
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        // 1. 创建通知渠道
        createNotificationChannel();

        // 2. 检查并请求通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // 请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            } else {
                // 已经有权限，初始化列表
                initializeListView();
            }
        } else {
            // Android 13以下版本不需要动态权限
            initializeListView();
        }
    }

    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，初始化列表
                initializeListView();
                Toast.makeText(this, "通知权限已获得", Toast.LENGTH_SHORT).show();
            } else {
                // 权限被拒绝
                initializeListView();
                Toast.makeText(this, "通知权限被拒绝，将无法显示通知", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeListView() {
        // 准备数据源
        List<Map<String, Object>> listItems = new ArrayList<>();
        for (int i = 0; i < animalNames.length; i++) {
            Map<String, Object> listItem = new HashMap<>();
            listItem.put("name", animalNames[i]);
            listItem.put("image", animalImages[i]);
            listItems.add(listItem);
        }

        // 创建 SimpleAdapter
        SimpleAdapter simpleAdapter = new SimpleAdapter(
                this,
                listItems,
                R.layout.list_item,
                new String[]{"name", "image"},
                new int[]{R.id.animal_name, R.id.animal_image}
        );

        // 获取 ListView 控件并设置适配器
        ListView listView = findViewById(R.id.my_list_view);
        listView.setAdapter(simpleAdapter);

        // 为 ListView 设置项目点击监听器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedAnimal = animalNames[position];

                // 使用Toast显示选中的列表项信息
                String toastMessage = "您选择了: " + selectedAnimal;
                Toast.makeText(ListViewActivity.this, toastMessage, Toast.LENGTH_SHORT).show();

                // 发送通知
                sendNotification(selectedAnimal);
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "动物列表通知";
            String description = "用于显示动物选择通知";
            int importance = NotificationManager.IMPORTANCE_HIGH;  // 高重要性
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String animalName) {
        try {
            // 检查权限（Android 13+）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "请先授予通知权限", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            // 构建通知
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)  // 使用系统图标确保显示
                    .setContentTitle("动物选择通知")
                    .setContentText("您选择了: " + animalName)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // 使用固定ID便于测试
            int notificationId = (int) System.currentTimeMillis(); // 使用时间戳作为唯一ID
            notificationManager.notify(notificationId, builder.build());

            Toast.makeText(this, "通知已发送! ID: " + notificationId, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "通知发送失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}