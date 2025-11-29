/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      `http://www.apache.org/licenses/LICENSE-2.0`  
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.notepad;

import com.example.android.notepad.NotePad;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays a list of notes. Will display notes from the {@link Uri}
 * provided in the incoming Intent if there is one, otherwise it defaults to displaying the
 * contents of the {@link NotePadProvider}.
 */
public class NotesList extends ListActivity {

    private static final String TAG = "NotesList";

    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, // 2
            NotePad.Notes.COLUMN_NAME_CATEGORY, // 3
    };

    private static final int COLUMN_INDEX_TITLE = 1;
    private static final int COLUMN_INDEX_MODIFICATION_DATE = 2;
    private static final int COLUMN_INDEX_CATEGORY = 3;

    // 分类颜色映射
    private static final Map<String, Integer> CATEGORY_COLORS = new HashMap<String, Integer>();
    static {
        CATEGORY_COLORS.put("默认分类", 0xFF2196F3);
        CATEGORY_COLORS.put("工作", 0xFF4CAF50);
        CATEGORY_COLORS.put("学习", 0xFFFF9800);
        CATEGORY_COLORS.put("生活", 0xFF9C27B0);
        CATEGORY_COLORS.put("想法", 0xFF607D8B);
        CATEGORY_COLORS.put("购物清单", 0xFFFF5722);
    }

    private static final int[] COLOR_OPTIONS = {
            0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF3F51B5,
            0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4, 0xFF009688, 0xFF4CAF50,
            0xFF8BC34A, 0xFFCDDC39, 0xFFFFEB3B, 0xFFFFC107, 0xFFFF9800,
            0xFFFF5722, 0xFF795548, 0xFF9E9E9E, 0xFF607D8B
    };

    // 分类筛选状态
    private String mCurrentFilterCategory = null;
    private String mCurrentSearchQuery = null;
    private SimpleCursorAdapter mAdapter;
    private Cursor mCursor;
    private int selectedColor = 0xFF2196F3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getListView().setBackgroundColor(getResources().getColor(R.color.background_light));
        getListView().setDivider(null);
        getListView().setDividerHeight(0);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }

        getListView().setOnCreateContextMenuListener(this);

        // 初始化列表
        initializeList();
    }

    /**
     * 初始化列表数据
     */
    private void initializeList() {

        Cursor cursor = managedQuery(
                getIntent().getData(),
                PROJECTION,
                null,
                null,
                NotePad.Notes.DEFAULT_SORT_ORDER
        );

        String[] dataColumns = {
                NotePad.Notes.COLUMN_NAME_TITLE,
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
                NotePad.Notes.COLUMN_NAME_CATEGORY
        };

        int[] viewIDs = {
                android.R.id.text1,
                R.id.text2,
                R.id.category_label
        };

        mAdapter = new SimpleCursorAdapter(
                this,
                R.layout.noteslist_item,
                cursor,
                dataColumns,
                viewIDs
        );

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                if (columnIndex == COLUMN_INDEX_MODIFICATION_DATE) {

                    TextView textView = (TextView) view;
                    long timestamp = cursor.getLong(columnIndex);
                    String formattedTime = formatTimestamp(timestamp);
                    textView.setText(formattedTime);
                    return true;

                } else if (columnIndex == COLUMN_INDEX_CATEGORY) {

                    TextView textView = (TextView) view;
                    String category = cursor.getString(columnIndex);
                    textView.setText(category);

                    // 设置分类标签颜色
                    Integer color = CATEGORY_COLORS.get(category);
                    if (color != null) {
                        textView.setTextColor(color);
                    } else {
                        textView.setTextColor(getResources().getColor(R.color.text_secondary));
                    }
                    return true;
                }
                return false;
            }
        });

        // 设置列表适配器
        setListAdapter(mAdapter);
    }

    /**
     * 格式化时间戳为易读的日期时间字符串
     */
    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) {
            return "Unknown time";
        }

        Date date = new Date(timestamp);
        java.text.DateFormat dateFormat = DateFormat.getDateFormat(this);
        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(this);

        return dateFormat.format(date) + " " + timeFormat.format(date);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 加载菜单资源
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_options_menu, menu);

        // 设置搜索视图
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
            searchView.setQueryHint("搜索笔记标题或内容...");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // 搜索提交时处理
                    mCurrentSearchQuery = query;
                    refreshNotesList();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // 实时搜索
                    mCurrentSearchQuery = newText;
                    refreshNotesList();
                    return true;
                }
            });

            // 清除搜索时重置
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    mCurrentSearchQuery = null;
                    refreshNotesList();
                    return false;
                }
            });
        }

        // 生成其他可执行的操作
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // 如果剪贴板中有数据，则启用粘贴菜单项
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        MenuItem mPasteItem = menu.findItem(R.id.menu_paste);

        if (clipboard.hasPrimaryClip()) {
            mPasteItem.setEnabled(true);
        } else {
            mPasteItem.setEnabled(false);
        }

        // 更新筛选菜单项状态
        MenuItem filterItem = menu.findItem(R.id.menu_filter);
        if (mCurrentFilterCategory != null && !mCurrentFilterCategory.equals("所有分类")) {
            filterItem.setTitle("取消筛选 (" + mCurrentFilterCategory + ")");
        } else {
            filterItem.setTitle("分类筛选");
        }

        // 获取当前显示的笔记数量
        final boolean haveItems = getListAdapter().getCount() > 0;

        // 如果列表中有笔记，则生成替代操作
        if (haveItems) {

            // 获取选中项的URI
            Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());

            // 创建Intent数组
            Intent[] specifics = new Intent[1];

            // 设置Intent为编辑操作
            specifics[0] = new Intent(Intent.ACTION_EDIT, uri);

            // 创建菜单项数组
            MenuItem[] items = new MenuItem[1];

            // 创建Intent
            Intent intent = new Intent(null, uri);
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);

            // 添加替代选项到菜单
            menu.addIntentOptions(
                    Menu.CATEGORY_ALTERNATIVE,
                    Menu.NONE,
                    Menu.NONE,
                    null,
                    specifics,
                    intent,
                    Menu.NONE,
                    items
            );
            
            if (items[0] != null) {
                items[0].setShortcut('1', 'e');
            }
        } else {
            // 如果列表为空，移除所有替代操作
            menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_add) {
            // 启动新的Activity创建笔记
            startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
            return true;
        } else if (id == R.id.menu_paste) {
            // 启动新的Activity粘贴笔记
            startActivity(new Intent(Intent.ACTION_PASTE, getIntent().getData()));
            return true;
        } else if (id == R.id.menu_categories) {
            // 显示分类管理对话框
            showCategoryManagementDialog();
            return true;
        } else if (id == R.id.menu_filter) {
            // 显示分类筛选对话框
            if (mCurrentFilterCategory != null && !mCurrentFilterCategory.equals("所有分类")) {
                // 如果已经有筛选，点击则取消筛选
                clearFilter();
            } else {
                showCategoryFilterDialog();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 显示分类筛选对话框
     */
    private void showCategoryFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择分类筛选");

        // 加载分类列表
        final List<String> categories = loadCategories();
        categories.add(0, "所有分类"); // 添加"所有分类"选项

        final String[] categoryArray = categories.toArray(new String[0]);

        builder.setSingleChoiceItems(categoryArray,
                categories.indexOf(mCurrentFilterCategory != null ? mCurrentFilterCategory : "所有分类"),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedCategory = categoryArray[which];
                        if (selectedCategory.equals("所有分类")) {
                            clearFilter();
                        } else {
                            applyFilter(selectedCategory);
                        }
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * 应用分类筛选
     */
    private void applyFilter(String category) {
        mCurrentFilterCategory = category;
        refreshNotesList();
        Toast.makeText(this, "已筛选分类: " + category, Toast.LENGTH_SHORT).show();
    }

    /**
     * 清除筛选
     */
    private void clearFilter() {
        mCurrentFilterCategory = null;
        mCurrentSearchQuery = null;
        refreshNotesList();
        Toast.makeText(this, "已清除筛选", Toast.LENGTH_SHORT).show();
    }

    /**
     * 从数据库加载分类列表
     */
    private List<String> loadCategories() {
        List<String> categories = new ArrayList<>();

        Cursor cursor = getContentResolver().query(
                NotePad.Categories.CONTENT_URI,
                new String[] { NotePad.Categories.COLUMN_NAME_NAME },
                null, null, NotePad.Categories.DEFAULT_SORT_ORDER
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String categoryName = cursor.getString(0);
                categories.add(categoryName);
            }
            cursor.close();
        }

        // 确保至少有一个默认分类
        if (categories.isEmpty()) {
            categories.add("默认分类");
        }

        return categories;
    }

    /**
     * 显示分类管理对话框
     */
    private void showCategoryManagementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.category_dialog, null);
        builder.setView(dialogView);

        // 初始化对话框组件
        final EditText categoryNameInput = dialogView.findViewById(R.id.category_name_input);
        Button addCategoryButton = dialogView.findViewById(R.id.add_category_button);
        final ListView categoriesList = dialogView.findViewById(R.id.categories_list);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button confirmButton = dialogView.findViewById(R.id.confirm_button);

        // 加载分类列表
        final CategoryAdapter categoryAdapter = new CategoryAdapter();
        categoriesList.setAdapter(categoryAdapter);

        // 添加分类按钮点击事件
        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryName = categoryNameInput.getText().toString().trim();
                if (!TextUtils.isEmpty(categoryName)) {
                    addCategory(categoryName);
                    categoryNameInput.setText("");
                    categoryAdapter.refreshData();
                } else {
                    Toast.makeText(NotesList.this, "请输入分类名称", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final AlertDialog dialog = builder.create();

        // 设置取消按钮关闭对话框
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // 设置确定按钮关闭对话框并刷新列表
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshNotesList();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * 添加新分类
     */
    private void addCategory(String categoryName) {
        ContentValues values = new ContentValues();
        values.put(NotePad.Categories.COLUMN_NAME_NAME, categoryName);
        values.put(NotePad.Categories.COLUMN_NAME_COLOR, 0xFF2196F3); // 默认蓝色

        try {
            getContentResolver().insert(NotePad.Categories.CONTENT_URI, values);
            CATEGORY_COLORS.put(categoryName, 0xFF2196F3); // 添加到颜色映射
            Toast.makeText(this, "分类添加成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "分类添加失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to add category", e);
        }
    }

    /**
     * 编辑分类
     */
    private void editCategory(final String oldName, final int categoryId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_category_dialog, null);
        builder.setView(dialogView);

        final EditText categoryNameEdit = dialogView.findViewById(R.id.edit_category_name);
        final LinearLayout colorPalette = dialogView.findViewById(R.id.color_palette);
        Button cancelButton = dialogView.findViewById(R.id.edit_cancel_button);
        Button saveButton = dialogView.findViewById(R.id.edit_save_button);

        categoryNameEdit.setText(oldName);

        // 创建颜色选择器
        createColorPalette(colorPalette, CATEGORY_COLORS.get(oldName));

        final AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = categoryNameEdit.getText().toString().trim();
                if (!TextUtils.isEmpty(newName)) {
                    updateCategory(categoryId, oldName, newName, selectedColor);
                    dialog.dismiss();
                } else {
                    Toast.makeText(NotesList.this, "分类名称不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * 创建颜色选择面板
     */
    private void createColorPalette(LinearLayout palette, int currentColor) {
        selectedColor = currentColor;
        palette.removeAllViews();

        for (final int color : COLOR_OPTIONS) {
            // 创建外层容器
            LinearLayout colorContainer = new LinearLayout(this);
            int size = getResources().getDimensionPixelSize(R.dimen.color_button_size);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(size, size);
            containerParams.setMargins(4, 4, 4, 4);
            colorContainer.setLayoutParams(containerParams);
            colorContainer.setOrientation(LinearLayout.VERTICAL);
            colorContainer.setGravity(android.view.Gravity.CENTER);

            // 创建颜色视图
            View colorView = new View(this);
            int innerSize = getResources().getDimensionPixelSize(R.dimen.color_inner_size);
            LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams(innerSize, innerSize);
            colorView.setLayoutParams(colorParams);
            colorView.setBackgroundColor(color);
            colorView.setTag(color);

            // 为当前选中的颜色添加边框
            if (color == currentColor) {
                colorContainer.setBackgroundResource(R.drawable.color_selected_border);
            } else {
                colorContainer.setBackgroundColor(Color.TRANSPARENT);
            }

            colorContainer.addView(colorView);
            colorContainer.setTag(color);

            colorContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedColor = color;
                    // 更新所有颜色视图的选中状态
                    for (int i = 0; i < palette.getChildCount(); i++) {
                        View child = palette.getChildAt(i);
                        int childColor = (Integer) child.getTag();
                        if (childColor == color) {
                            child.setBackgroundResource(R.drawable.color_selected_border);
                        } else {
                            child.setBackgroundColor(Color.TRANSPARENT);
                        }
                    }
                }
            });

            palette.addView(colorContainer);
        }
    }

    /**
     * 更新分类
     */
    private void updateCategory(int categoryId, String oldName, String newName, int color) {
        ContentValues values = new ContentValues();
        values.put(NotePad.Categories.COLUMN_NAME_NAME, newName);
        values.put(NotePad.Categories.COLUMN_NAME_COLOR, color);

        Uri categoryUri = ContentUris.withAppendedId(NotePad.Categories.CONTENT_URI, categoryId);

        try {
            getContentResolver().update(categoryUri, values, null, null);

            // 更新颜色映射
            CATEGORY_COLORS.remove(oldName);
            CATEGORY_COLORS.put(newName, color);

            // 更新所有使用该分类的笔记
            updateNotesCategory(oldName, newName);

            Toast.makeText(this, "分类更新成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "分类更新失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to update category", e);
        }
    }

    /**
     * 更新笔记的分类
     */
    private void updateNotesCategory(String oldCategory, String newCategory) {
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_CATEGORY, newCategory);

        getContentResolver().update(
                NotePad.Notes.CONTENT_URI,
                values,
                NotePad.Notes.COLUMN_NAME_CATEGORY + " = ?",
                new String[] { oldCategory }
        );
    }

    /**
     * 删除分类
     */
    private void deleteCategory(final int categoryId, final String categoryName) {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除分类 \"" + categoryName + "\" 吗？所有属于该分类的笔记将被移动到\"默认分类\"。")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 先将该分类下的笔记移动到默认分类
                        updateNotesCategory(categoryName, "默认分类");

                        // 然后删除分类
                        Uri categoryUri = ContentUris.withAppendedId(NotePad.Categories.CONTENT_URI, categoryId);
                        getContentResolver().delete(categoryUri, null, null);

                        // 从颜色映射中移除
                        CATEGORY_COLORS.remove(categoryName);

                        Toast.makeText(NotesList.this, "分类已删除", Toast.LENGTH_SHORT).show();
                        refreshNotesList();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 刷新笔记列表
     */
    private void refreshNotesList() {
        // 构建查询条件
        String selection = null;
        String[] selectionArgs = null;

        // 处理分类筛选
        if (mCurrentFilterCategory != null && !mCurrentFilterCategory.equals("所有分类")) {
            selection = NotePad.Notes.COLUMN_NAME_CATEGORY + " = ?";
            selectionArgs = new String[] { mCurrentFilterCategory };
        }

        // 处理搜索查询
        if (mCurrentSearchQuery != null && !mCurrentSearchQuery.isEmpty()) {
            if (selection == null) {
                selection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " +
                        NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?";
                selectionArgs = new String[] {
                        "%" + mCurrentSearchQuery + "%",
                        "%" + mCurrentSearchQuery + "%"
                };
            } else {
                selection += " AND (" + NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " +
                        NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?)";
                selectionArgs = new String[] {
                        mCurrentFilterCategory,
                        "%" + mCurrentSearchQuery + "%",
                        "%" + mCurrentSearchQuery + "%"
                };
            }
        }

        // 执行查询
        Cursor newCursor = managedQuery(
                getIntent().getData(),
                PROJECTION,
                selection,
                selectionArgs,
                NotePad.Notes.DEFAULT_SORT_ORDER
        );

        // 更新适配器的游标
        if (mAdapter != null) {
            mAdapter.changeCursor(newCursor);
        }

        // 更新界面状态
        updateUIState();
    }

    /**
     * 更新界面状态显示
     */
    private void updateUIState() {
        // 更新标题显示筛选状态
        if (mCurrentFilterCategory != null && !mCurrentFilterCategory.equals("所有分类")) {
            setTitle("笔记 - " + mCurrentFilterCategory);
        } else {
            setTitle("笔记列表");
        }

        // 显示空列表提示 - 修复类型转换问题
        View emptyView = getListView().getEmptyView();
        if (emptyView instanceof TextView) {
            TextView emptyTextView = (TextView) emptyView;
            if (mAdapter != null && mAdapter.isEmpty()) {
                if (mCurrentFilterCategory != null && !mCurrentFilterCategory.equals("所有分类")) {
                    emptyTextView.setText("该分类下没有笔记");
                } else if (mCurrentSearchQuery != null && !mCurrentSearchQuery.isEmpty()) {
                    emptyTextView.setText("没有找到匹配的笔记");
                } else {
                    emptyTextView.setText("还没有笔记，点击菜单按钮创建新笔记");
                }
            }
        }
    }

    /**
     * 分类列表适配器
     */
    private class CategoryAdapter extends BaseAdapter {
        private List<Category> categories = new ArrayList<>();

        public CategoryAdapter() {
            refreshData();
        }

        public void refreshData() {
            categories.clear();
            Cursor cursor = getContentResolver().query(
                    NotePad.Categories.CONTENT_URI,
                    new String[] {
                            NotePad.Categories._ID,
                            NotePad.Categories.COLUMN_NAME_NAME,
                            NotePad.Categories.COLUMN_NAME_COLOR
                    },
                    null, null, NotePad.Categories.DEFAULT_SORT_ORDER
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Category category = new Category();
                    category.id = cursor.getInt(0);
                    category.name = cursor.getString(1);
                    category.color = cursor.getInt(2);
                    categories.add(category);

                    // 更新颜色映射
                    CATEGORY_COLORS.put(category.name, category.color);
                }
                cursor.close();
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return categories.size();
        }

        @Override
        public Category getItem(int position) {
            return categories.get(position);
        }

        @Override
        public long getItemId(int position) {
            return categories.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.category_item, parent, false);
                holder = new ViewHolder();
                holder.colorView = convertView.findViewById(R.id.category_color);
                holder.nameView = convertView.findViewById(R.id.category_name);
                holder.editButton = convertView.findViewById(R.id.edit_button);
                holder.deleteButton = convertView.findViewById(R.id.delete_button);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Category category = getItem(position);
            holder.nameView.setText(category.name);
            holder.colorView.setBackgroundColor(category.color);

            // 编辑按钮点击事件
            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editCategory(category.name, category.id);
                }
            });

            // 删除按钮点击事件
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteCategory(category.id, category.name);
                }
            });

            return convertView;
        }

        class ViewHolder {
            View colorView;
            TextView nameView;
            ImageButton editButton;
            ImageButton deleteButton;
        }
    }

    /**
     * 分类数据模型
     */
    private static class Category {
        int id;
        String name;
        int color;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

        // 尝试获取长按项在ListView中的位置
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        // 获取选中项的数据
        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);

        if (cursor == null) {
            return;
        }

        // 加载上下文菜单资源
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);

        // 设置菜单标题为选中笔记的标题
        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));

        // 添加其他Activity可以处理的操作
        Intent intent = new Intent(null, Uri.withAppendedPath(getIntent().getData(),
                Integer.toString((int) info.id)));
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);
        
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // 获取菜单项的额外信息
        AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        // 构建选中笔记的URI
        Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);

        // 获取菜单项的ID并比较
        int id = item.getItemId();
        if (id == R.id.context_open) {
            // 打开笔记进行查看/编辑
            startActivity(new Intent(Intent.ACTION_EDIT, noteUri));
            return true;
        } else if (id == R.id.context_copy) {
            // 复制笔记URI到剪贴板
            ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newUri(
                    getContentResolver(),
                    "Note",
                    noteUri));
            return true;
        } else if (id == R.id.context_delete) {
            // 从提供者中删除笔记
            getContentResolver().delete(
                    noteUri,
                    null,
                    null
            );
            return true;
        } else if (id == R.id.context_create_widget) {
            // 创建便签小部件
            createNoteWidget(info.id);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        // 构建新的URI
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

        // 获取传入的操作
        String action = getIntent().getAction();

        // 处理数据请求
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {
            // 启动编辑Activity
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }

    /**
     * 当从其他Activity返回时刷新列表
     */
    @Override
    protected void onResume() {
        super.onResume();
        // 刷新列表以显示可能的更改
        refreshNotesList();
    }

    /**
     * 创建笔记便签小部件
     */
    private void createNoteWidget(long noteId) {
        // 获取笔记标题用于显示
        Cursor cursor = getContentResolver().query(
                ContentUris.withAppendedId(getIntent().getData(), noteId),
                new String[]{NotePad.Notes.COLUMN_NAME_TITLE},
                null, null, null);

        String noteTitle = "笔记";
        if (cursor != null && cursor.moveToFirst()) {
            noteTitle = cursor.getString(0);
            cursor.close();
        }

        // 显示更友好的提示信息
        Toast.makeText(this, "便签功能已就绪！\n如需新便签，请从桌面添加", Toast.LENGTH_LONG).show();

        // 可选：提供直接打开小部件配置的选项
        showWidgetCreationDialog(noteId, noteTitle);
    }

    /**
     * 显示便签创建对话框
     */
    private void showWidgetCreationDialog(final long noteId, final String noteTitle) {
        new AlertDialog.Builder(this)
                .setTitle("创建便签")
                .setMessage("您想为笔记《" + noteTitle + "》创建便签吗？\n\n" +
                        "• 如需新便签：请从桌面添加小部件\n" +
                        "• 如需更新现有便签：长按桌面便签重新配置")
                .setPositiveButton("从桌面添加", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 指导用户如何添加
                        Toast.makeText(NotesList.this,
                                "请长按桌面 → 选择小部件 → 找到\"笔记便签\"",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}