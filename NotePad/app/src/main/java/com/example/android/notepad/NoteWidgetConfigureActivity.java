package com.example.android.notepad;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 便签小部件配置界面
 * 用于选择要显示在便签中的笔记
 */
public class NoteWidgetConfigureActivity extends Activity {

    private static final String TAG = "WidgetConfigure";
    private static final String PREFS_NAME = "com.example.android.notepad.NoteWidgetProvider";
    private static final String PREF_PREFIX_KEY = "appwidget_";

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private ListView mNotesListView;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Log.d(TAG, "配置Activity启动");

        // 设置为取消结果（如果用户退出）
        setResult(RESULT_CANCELED);

        // 设置布局
        setContentView(R.layout.widget_configure);

        // 获取小部件ID
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
            );
        }

        Log.d(TAG, "小部件ID: " + mAppWidgetId);

        // 如果ID无效，结束Activity
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e(TAG, "无效的小部件ID");
            finish();
            return;
        }

        mNotesListView = findViewById(R.id.notes_list);

        // 加载笔记列表
        loadNotesList();

        // 设置列表点击事件
        mNotesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "用户选择了笔记，ID: " + id);

                final Context context = NoteWidgetConfigureActivity.this;

                // 保存选择的笔记ID
                saveNoteIdPref(context, mAppWidgetId, (int) id);

                // 立即更新小部件
                updateWidget(context, mAppWidgetId);

                // 设置结果为成功
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);

                Toast.makeText(context, "便签已创建！返回桌面查看", Toast.LENGTH_LONG).show();
                Log.d(TAG, "配置完成，准备结束Activity");

                finish();
            }
        });
    }

    /**
     * 加载笔记列表
     */
    private void loadNotesList() {
        try {
            // 查询所有笔记
            Cursor cursor = getContentResolver().query(
                    NotePad.Notes.CONTENT_URI,
                    new String[] {
                            NotePad.Notes._ID,
                            NotePad.Notes.COLUMN_NAME_TITLE,
                            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE
                    },
                    null, null, NotePad.Notes.DEFAULT_SORT_ORDER
            );

            if (cursor == null || cursor.getCount() == 0) {
                Toast.makeText(this, "没有找到笔记", Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建适配器
            String[] from = new String[] {
                    NotePad.Notes.COLUMN_NAME_TITLE,
                    NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE
            };

            int[] to = new int[] {
                    android.R.id.text1,
                    android.R.id.text2
            };

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_list_item_2,
                    cursor,
                    from,
                    to,
                    0
            );

            adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if (columnIndex == 2) { // 修改日期列
                        TextView textView = (TextView) view;
                        long timestamp = cursor.getLong(columnIndex);
                        String formattedTime = formatTimestamp(timestamp);
                        textView.setText(formattedTime);
                        return true;
                    }
                    return false;
                }
            });

            mNotesListView.setAdapter(adapter);
            Log.d(TAG, "加载了 " + cursor.getCount() + " 条笔记");

        } catch (Exception e) {
            Log.e(TAG, "加载笔记列表失败", e);
            Toast.makeText(this, "加载笔记失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 格式化时间戳
     */
    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) {
            return "未知时间";
        }

        java.util.Date date = new java.util.Date(timestamp);
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
        java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);

        return dateFormat.format(date) + " " + timeFormat.format(date);
    }

    /**
     * 更新小部件
     */
    private void updateWidget(Context context, int appWidgetId) {
        try {
            Log.d(TAG, "开始更新小部件: " + appWidgetId);

            // 直接调用小部件更新方法
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            NoteWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId);

            // 发送广播强制更新
            Intent updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
            updateIntent.setComponent(new android.content.ComponentName(context, NoteWidgetProvider.class));
            context.sendBroadcast(updateIntent);

            Log.d(TAG, "小部件更新完成");
        } catch (Exception e) {
            Log.e(TAG, "更新小部件失败", e);
        }
    }

    // 保存笔记ID到SharedPreferences
    static void saveNoteIdPref(Context context, int appWidgetId, int noteId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId, noteId);
        prefs.apply();
        Log.d(TAG, "保存笔记ID: " + noteId + " 到小部件: " + appWidgetId);
    }

    // 从SharedPreferences读取笔记ID
    static int loadNoteIdPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int noteId = prefs.getInt(PREF_PREFIX_KEY + appWidgetId, -1);
        Log.d(TAG, "读取小部件 " + appWidgetId + " 的笔记ID: " + noteId);
        return noteId;
    }

    // 删除笔记ID配置
    static void deleteNoteIdPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
        Log.d(TAG, "删除小部件配置: " + appWidgetId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "配置Activity销毁");
    }
}