# 实验二 Android界面布局实验  
## 1.线性布局LinearLayout  
1.结构外层垂直，内层水平  

2.统一设置了botton的部分格式在drawable下button_border.xml  

    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
             android:orientation="vertical"

        <LinearLayout
            android:orientation="horizontal"
            ……
        </LinearLayout>

        <LinearLayout
            android:orientation="horizontal"
            ……
            </LinearLayout>

        <LinearLayout
            android:orientation="horizontal"
            ……
            </LinearLayout>

        <LinearLayout
            android:orientation="horizontal"
            ……
            </LinearLayout>
     </LinearLayout>
![alt text](image.png)
## 2.表格布局TableLayout  
1.这题我是外层线性布局，把标题的TextView和表格布局并列一个层次。  
  
2.为了表格一行TableRow左侧按钮占据所有可用宽度，右侧按钮保持紧凑，使用android:layout_width="0dp" + android:layout_weight="1"  
组合功能：在LinearLayout中按权重分配宽度  
效果：
layout_width="0dp"：忽略内容宽度
layout_weight="1"：按权重1分配剩余空间  
  
3.分隔线就是用一个View,hight=1dp  
![alt text](image-1.png)  
## 3.约束布局计算器界面ConstraintLayout  
1.约束布局首先要在build.gradle加一个依赖implementation("androidx.constraintlayout:constraintlayout:2.1.4")  
然后要写完整androidx.constraintlayout.widget.ConstraintLayout  

2.上面三个框用相对约束，每个都约束了Start,End,Top。  

3.下面按钮的部分同一排就用水平Chains约束，chainStyle="spread"。每一排第一个按钮Top与上一排的Bottom间隔30dp，后面三个按钮的Start、End前后相连,Top就和第一个按钮Top对齐。
![alt text](image-2.png)
## 4.约束布局界面二ConostraintLayout  
1.用到了相对约束、偏向约束Bias。  
```  
        app:layout_constraintBottom_toTopOf="@+id/depart"
        app:layout_constraintEnd_toStartOf="@+id/galaxy"
        app:layout_constraintHorizontal_bias="0.735"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/DCA"
        app:layout_constraintVertical_bias="0.409"
```
2.图片的引入ImageView，要先把本地图片放在drawable文件夹，然后android:src="@drawable/rocket_icon"

3.顶部的阴影框用的是androidx.cardview.widget.CardView，需要先引入implementation("androidx.cardview:cardview:1.0.0")  

4.One Way开关是Switch  
![alt text](image-5.png)