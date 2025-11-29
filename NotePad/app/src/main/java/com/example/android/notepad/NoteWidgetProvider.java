package com.example.android.notepad;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * 笔记便签桌面小部件提供器
 * 支持显示单个笔记的便签小部件
 */
public class NoteWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "NoteWidgetProvider";
    public static final String ACTION_WIDGET_UPDATE = "com.example.android.notepad.ACTION_WIDGET_UPDATE";
    public static final String EXTRA_NOTE_ID = "note_id";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate 被调用, 小部件数量: " + appWidgetIds.length);

        // 更新所有小部件实例
        for (int appWidgetId : appWidgetIds) {
            Log.d(TAG, "更新小部件: " + appWidgetId);
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_WIDGET_UPDATE.equals(intent.getAction())) {
            // 收到更新请求，更新所有小部件
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, NoteWidgetProvider.class));
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d(TAG, "updateAppWidget 被调用, ID: " + appWidgetId);

        try {
            // 获取小部件配置中存储的笔记ID
            int noteId = NoteWidgetConfigureActivity.loadNoteIdPref(context, appWidgetId);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            if (noteId == -1) {
                // 未配置笔记，显示提示
                Log.d(TAG, "小部件未配置，显示提示");
                views.setTextViewText(R.id.widget_title, "点击配置便签");
                views.setTextViewText(R.id.widget_content, "选择要显示的笔记");
                views.setTextViewText(R.id.widget_category, "未配置");
                views.setTextViewText(R.id.widget_date, "");

                // 设置点击打开配置界面
                Intent configIntent = new Intent(context, NoteWidgetConfigureActivity.class);
                configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent configPendingIntent = PendingIntent.getActivity(
                        context, appWidgetId, configIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.widget_root, configPendingIntent);

            } else {
                Log.d(TAG, "开始查询笔记数据，笔记ID: " + noteId);

                // 先进行调试检查
                debugCheckNoteData(context, noteId);

                // 使用安全的查询方式
                Cursor cursor = null;
                try {
                    // 方法1：使用带条件的查询（更安全）
                    cursor = context.getContentResolver().query(
                            NotePad.Notes.CONTENT_URI,
                            new String[] {
                                    NotePad.Notes._ID,
                                    NotePad.Notes.COLUMN_NAME_TITLE,
                                    NotePad.Notes.COLUMN_NAME_NOTE,
                                    NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
                                    NotePad.Notes.COLUMN_NAME_CATEGORY
                            },
                            NotePad.Notes._ID + " = ?",  // WHERE 条件
                            new String[] { String.valueOf(noteId) },  // 参数
                            null
                    );

                    if (cursor != null && cursor.moveToFirst()) {
                        Log.d(TAG, "查询成功，找到笔记数据");

                        // 安全地获取列索引
                        int titleIndex = cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE);
                        int noteIndex = cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
                        int dateIndex = cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE);
                        int categoryIndex = cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_CATEGORY);

                        // 检查列索引是否有效
                        if (titleIndex >= 0 && noteIndex >= 0 && dateIndex >= 0 && categoryIndex >= 0) {
                            String title = cursor.getString(titleIndex);
                            String content = cursor.getString(noteIndex);
                            long modificationDate = cursor.getLong(dateIndex);
                            String category = cursor.getString(categoryIndex);

                            Log.d(TAG, "成功读取笔记数据 - 标题: " + title + ", 分类: " + category);

                            // 处理可能为null的值
                            if (title == null) title = "无标题";
                            if (content == null) content = "";
                            if (category == null) category = "默认分类";

                            // 格式化内容
                            String displayContent = content;
                            if (displayContent.length() > 100) {
                                displayContent = displayContent.substring(0, 100) + "...";
                            }

                            // 格式化日期
                            String formattedDate = formatTimestamp(modificationDate);

                            // 设置显示内容
                            views.setTextViewText(R.id.widget_title, title);
                            views.setTextViewText(R.id.widget_content, displayContent);
                            views.setTextViewText(R.id.widget_category, category);
                            views.setTextViewText(R.id.widget_date, formattedDate);

                            Log.d(TAG, "小部件内容更新成功");
                        } else {
                            Log.e(TAG, "列索引无效 - title:" + titleIndex + " note:" + noteIndex +
                                    " date:" + dateIndex + " category:" + categoryIndex);
                            throw new Exception("数据库列不存在");
                        }

                    } else {
                        Log.w(TAG, "没有找到笔记，ID: " + noteId);
                        views.setTextViewText(R.id.widget_title, "笔记不存在");
                        views.setTextViewText(R.id.widget_content, "该笔记可能已被删除");
                        views.setTextViewText(R.id.widget_category, "");
                        views.setTextViewText(R.id.widget_date, "");

                        // 清除无效配置
                        NoteWidgetConfigureActivity.deleteNoteIdPref(context, appWidgetId);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "查询笔记数据失败: " + e.getMessage(), e);

                    // 尝试备用查询方法
                    if (!tryAlternativeQuery(context, noteId, views)) {
                        views.setTextViewText(R.id.widget_title, "数据加载失败");
                        views.setTextViewText(R.id.widget_content, "错误: " + e.getClass().getSimpleName());
                        views.setTextViewText(R.id.widget_category, "请重新配置");
                        views.setTextViewText(R.id.widget_date, "");
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

                // 设置点击打开笔记编辑界面
                Intent intent = new Intent(context, NoteEditor.class);
                intent.setAction(Intent.ACTION_EDIT);
                intent.setData(ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, noteId));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        context, appWidgetId, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
            }

            // 更新小部件
            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.d(TAG, "小部件更新完成: " + appWidgetId);

        } catch (Exception e) {
            Log.e(TAG, "更新小部件时发生错误", e);
        }
    }

    /**
     * 简化的调试方法
     */
    private static void debugCheckNoteData(Context context, int noteId) {
        Log.d(TAG, "=== 开始调试笔记数据检查 ===");
        Log.d(TAG, "检查笔记ID: " + noteId);

        Cursor cursor = null;
        try {
            // 简单查询所有笔记
            cursor = context.getContentResolver().query(
                    NotePad.Notes.CONTENT_URI,
                    new String[] { NotePad.Notes._ID, NotePad.Notes.COLUMN_NAME_TITLE },
                    null, null, null
            );

            if (cursor != null) {
                Log.d(TAG, "数据库中的笔记总数: " + cursor.getCount());
                boolean found = false;
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(0);
                    String title = cursor.getString(1);
                    Log.d(TAG, "笔记ID: " + id + ", 标题: " + title);
                    if (id == noteId) {
                        found = true;
                    }
                }
                Log.d(TAG, "目标笔记ID " + noteId + " 是否存在: " + found);
            } else {
                Log.d(TAG, "查询返回null cursor");
            }
        } catch (Exception e) {
            Log.e(TAG, "调试查询失败: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Log.d(TAG, "=== 结束调试笔记数据检查 ===");
    }

    /**
     * 备用查询方法 - 使用更简单直接的方式
     */
    private static boolean tryAlternativeQuery(Context context, int noteId, RemoteViews views) {
        Log.d(TAG, "尝试备用查询方法");

        Cursor cursor = null;
        try {
            // 方法2：直接查询所有笔记然后筛选
            cursor = context.getContentResolver().query(
                    NotePad.Notes.CONTENT_URI,
                    new String[] {
                            NotePad.Notes._ID,
                            NotePad.Notes.COLUMN_NAME_TITLE,
                            NotePad.Notes.COLUMN_NAME_NOTE,
                            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
                            NotePad.Notes.COLUMN_NAME_CATEGORY
                    },
                    null, null, null
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int currentId = cursor.getInt(0); // _ID 列
                    if (currentId == noteId) {
                        // 找到匹配的笔记
                        String title = cursor.getString(1); // 标题列
                        String content = cursor.getString(2); // 内容列
                        long modificationDate = cursor.getLong(3); // 日期列
                        String category = cursor.getString(4); // 分类列

                        // 处理null值
                        if (title == null) title = "无标题";
                        if (content == null) content = "";
                        if (category == null) category = "默认分类";

                        // 格式化内容
                        String displayContent = content;
                        if (displayContent.length() > 100) {
                            displayContent = displayContent.substring(0, 100) + "...";
                        }

                        // 格式化日期
                        String formattedDate = formatTimestamp(modificationDate);

                        // 设置显示内容
                        views.setTextViewText(R.id.widget_title, title);
                        views.setTextViewText(R.id.widget_content, displayContent);
                        views.setTextViewText(R.id.widget_category, category);
                        views.setTextViewText(R.id.widget_date, formattedDate);

                        Log.d(TAG, "备用查询成功");
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "备用查询也失败: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return false;
    }

    private static String formatTimestamp(long timestamp) {
        try {
            if (timestamp <= 0) {
                return "未知时间";
            }

            // 使用更简单可靠的日期格式化
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yy HH:mm", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(timestamp));
        } catch (Exception e) {
            Log.e(TAG, "格式化时间失败: " + e.getMessage());
            // 如果格式化失败，返回原始时间戳
            return String.valueOf(timestamp);
        }
    }
}