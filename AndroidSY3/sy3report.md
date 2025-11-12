# 实验三 Android界面组件实验  
## 1. MainActivity - 主入口页面   
### 逻辑功能：  
作为应用的主界面，提供四个功能模块的导航按钮  
使用Intent跳转到对应的Activity  
![主入口页面截图 ](https://github.com/linxinyi123-123/25-26-1AndroidProjects/blob/master/AndroidSY3/firstPage.png)
## 2. ListViewActivity - ListView示例  
### 逻辑功能：  
使用SimpleAdapter显示动物列表（名称+图片）  
处理Android 13+的通知权限  
点击列表项显示Toast并发送通知  
### 布局文件：  
activity_list_view.xml: 简单的ListView容器  
list_item.xml: 列表项布局（动物名称+图片）   

### SimpleAdapter工作流程  
数据映射关系：  
Map数据: {"name": "Lion", "image": R.drawable.lion}  
        ↓  
布局文件: list_item.xml  
        ↓  
视图绑定: name → R.id.animal_name (TextView)  
         image → R.id.animal_image (ImageView)  

### Android 13+ 通知权限检查  
```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
        // 请求权限
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                PERMISSION_REQUEST_CODE);
    } else {
        initializeListView(); // 已有权限，初始化列表
    }
} else {
    // Android 13以下版本不需要动态权限
    initializeListView();
}
```
### 通知系统实现
```java
private void createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // 创建通知渠道（Android 8.0+必需）
        CharSequence name = "动物列表通知";
        String description = "用于显示动物选择通知";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
        channel.setDescription(description);
        channel.setShowBadge(true);
        
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}

private void sendNotification(String animalName) {
    // 再次检查权限
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "请先授予通知权限", Toast.LENGTH_LONG).show();
            return;
        }
    }

    // 构建通知
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)  // 通知小图标
            .setContentTitle("动物选择通知")                   // 通知标题
            .setContentText("您选择了: " + animalName)         // 通知内容
            .setPriority(NotificationCompat.PRIORITY_HIGH)    // 优先级
            .setAutoCancel(true);                             // 点击后自动取消

    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    
    // 发送通知（使用时间戳作为唯一ID）
    int notificationId = (int) System.currentTimeMillis();
    notificationManager.notify(notificationId, builder.build());
    
    Toast.makeText(this, "通知已发送! ID: " + notificationId, Toast.LENGTH_SHORT).show();
}
```
![List发送toast截图 ](https://github.com/linxinyi123-123/25-26-1AndroidProjects/blob/master/AndroidSY3/listToast.png)
![List发送notice截图 ](https://github.com/linxinyi123-123/25-26-1AndroidProjects/blob/master/AndroidSY3/listNotice.png)
## 3. LoginDialogActivity - 登录对话框  
### 逻辑功能：
显示自定义布局的AlertDialog  
处理用户名密码输入验证  
登录成功/取消的逻辑处理  
### 细节：  
用户名和密码的EditText再选中时下边框会变蓝色，这是通过设置`android:background="@drawable/edittext_border"`在edittext_border.xml设置当
`<item android:state_focused="true">`是有选中态的形式  
```xml
<!-- 白色背景和圆角 -->
            <item>
                <shape android:shape="rectangle">
                    <solid android:color="#FFFFFF" />
                    <corners android:radius="4dp" />
                </shape>
            </item>
            <!-- 蓝色底边 -->
            <item android:top="-1dp" android:left="-1dp" android:right="-1dp" android:bottom="0dp">
                <shape android:shape="rectangle">
                    <stroke android:width="1dp" android:color="#2196F3" />
                    <corners android:radius="4dp" />
                </shape>
            </item>
```

```java
private void showLoginDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_login, null);
    builder.setView(dialogView);  // 设置自定义视图
    
    // 登录按钮点击事件
    btnSignIn.setOnClickListener(v -> {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "登录成功！\n用户名: " + username, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        }
    });
}
```
![登录对话框截图 ](https://github.com/linxinyi123-123/25-26-1AndroidProjects/blob/master/AndroidSY3/login.png)
![登录成功发送toast截图 ](https://github.com/linxinyi123-123/25-26-1AndroidProjects/blob/master/AndroidSY3/loginToast.png)
## 4. MenuTestActivity - 菜单测试
### 逻辑功能：
使用XML定义选项菜单  
实现字体大小和颜色的动态修改  
处理菜单项点击事件  
放原始字体和变大变红的字体对比图  
### 细节：  
1.这里和下一题都需要用到actionbar,而现在默认的actionbar是隐藏的，所以要在AndroidManifest.xml的activity声明里确定theme  

`<activity android:name=".MenuTestActivity" android:theme="@style/Theme.AppCompat.Light.DarkActionBar"/>`

2.字体和颜色这两个子菜单是用了menu的嵌套  
![字体改变前截图 ](https://github.com/linxinyi123-123/25-26-1AndroidProjects/blob/master/AndroidSY3/fontChange1.png)
![字体改变后截图 ](https://github.com/linxinyi123-123/25-26-1AndroidProjects/blob/master/AndroidSY3/fontChange2.png)
## 5. ActionModeActivity - 上下文操作模式
### 逻辑功能：
实现多选模式的上下文菜单  
显示选中数量并支持删除操作  
使用ActionMode处理上下文操作  


### 设置多选模态监听器
```java
listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        // 更新选中数量
        int selectedCount = listView.getCheckedItemCount();
        mode.setTitle(selectedCount + " selected");
    }
    
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {
            deleteSelectedItems();  // 删除选中项
            mode.finish();
            return true;
        }
        return false;
    }
});
```
![上下文菜单截图 ](https://github.com/linxinyi123-123/25-26-1AndroidProjects/blob/master/AndroidSY3/contextMenu.png)
![上下文菜单删除后截图 ](https://github.com/linxinyi123-123/25-26-1AndroidProjects/blob/master/AndroidSY3/contextMenuDel.png)