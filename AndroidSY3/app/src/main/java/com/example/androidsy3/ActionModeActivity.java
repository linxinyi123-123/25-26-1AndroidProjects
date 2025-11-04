package com.example.androidsy3;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class ActionModeActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList;
    private ActionMode actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_mode);

        // 设置标题
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("上下文操作模式示例");
        }

        initData();
        initListView();
    }

    private void initData() {
        dataList = new ArrayList<>();
        dataList.add("One");
        dataList.add("Two");
        dataList.add("Three");
        dataList.add("Four");
        dataList.add("Five");
    }

    private void initListView() {
        listView = findViewById(R.id.action_mode_list_view);

        // 使用支持激活状态的布局
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, dataList);
        listView.setAdapter(adapter);

        // 设置多选模态
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // 更新标题显示选中数量
                int selectedCount = listView.getCheckedItemCount();
                mode.setTitle(selectedCount + " selected");

                // 可选：手动更新选中状态的可视化
                updateSelectionVisual();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // 加载上下文菜单
                getMenuInflater().inflate(R.menu.context_menu, menu);
                actionMode = mode;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.menu_delete) {
                    // 删除选中的项目
                    deleteSelectedItems();
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // 清除所有选择状态
                clearSelections();
                actionMode = null;
            }
        });
    }

    private void deleteSelectedItems() {
        // 获取选中的位置
        List<Integer> selectedPositions = new ArrayList<>();

        for (int i = 0; i < listView.getCount(); i++) {
            if (listView.isItemChecked(i)) {
                selectedPositions.add(i);
            }
        }

        // 从后往前删除，避免索引问题
        for (int i = selectedPositions.size() - 1; i >= 0; i--) {
            int position = selectedPositions.get(i);
            dataList.remove(position);
        }

        adapter.notifyDataSetChanged();
        Toast.makeText(this, "删除了 " + selectedPositions.size() + " 个项目", Toast.LENGTH_SHORT).show();
    }

    private void updateSelectionVisual() {
        // 这个方法可以确保选中状态正确显示
        // simple_list_item_activated_1 会自动处理背景色
        adapter.notifyDataSetChanged();
    }

    private void clearSelections() {
        // 清除所有选中状态
        for (int i = 0; i < listView.getCount(); i++) {
            listView.setItemChecked(i, false);
        }
        adapter.notifyDataSetChanged();
    }
}