package com.dypho.roughdict;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.ScrollView;
// import android.widget.SearchView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.widget.SearchView;
import com.dypho.roughdict.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private EditText editText;
    private ScrollView scrollView;
    private TextView righttextView;
    private LinearLayout linearLayout;
    private TextView textView;
    private String toshow = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and get instance of binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // set content view to binding's root
        setContentView(binding.getRoot());
        righttextView = binding.textView;
        scrollView = binding.scrollView;
        linearLayout = binding.layo;
        
        binding.searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        // 用户提交查询时执行的操作
                    new Thread() {
                            @Override
                            public void run() {

                                try {
                                    toshow = HttpsUtils.sendOnce(query);

                                } catch (Exception err) {
                                    err.printStackTrace();
                                }
                            }
                        }.start();
                        
                        splitAndCreateTextViews(query);
                    while (toshow == "null") {
//                            try {
//                              //  Thread.sleep(50);
//                            } catch (Exception err) {
//
//                            }
                        }
                        righttextView.setText(toshow);
                        righttextView.invalidate();
                        toshow = "null";
                        // TODO: 在这里处理查询
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        // 查询文本发生变化时执行的操作
                        // TODO: 在这里处理文本变化
                        return true;
                    }
                });
        EditText searchEditText = (EditText) binding.searchView.findViewById(androidx.appcompat.R.id.search_src_text);
searchEditText.setTextColor(Color.WHITE); // 设置文本颜色为白色
    }

    private void splitAndCreateTextViews(String inputString) {

        // 清除LinearLayout中的所有视图
        try {
            linearLayout.removeAllViews();
        } catch (Exception err) {

        }
        textView = new TextView(this);
        textView.setText(inputString);
        textView.setId(0); // 设置视图ID
        textView.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (int) (75 * getResources().getDisplayMetrics().density)));
        textView.setGravity(Gravity.CENTER);
        textView.setClickable(true);
        textView.setPadding(8, 8, 8, 8);
        textView.setFocusable(true);
        // textView.setBackgroundResource(android.R.attr.selectableItemBackground);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextColor(Color.WHITE);
        textView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread() {
                            @Override
                            public void run() {

                                try {
                                    toshow = HttpsUtils.sendOnce(inputString);

                                } catch (Exception err) {
                                    err.printStackTrace();
                                }
                            }
                        }.start();
                        while (toshow == "null") {
                            try {
                                Thread.sleep(50);
                            } catch (Exception err) {

                            }
                        }
                        righttextView.setText(toshow);
                        righttextView.invalidate();
                        toshow = "null";
                    }
                });
        // 将 TextView 添加到LinearLayout中
        try {
            linearLayout.addView(textView);
        } catch (Exception err) {
            err.printStackTrace();
        }

        // 使用正则表达式分割字符串
        String[] splitStrings = inputString.split("[，。？！“”.!?，…\\s]+");
        // 存储拆分后的字符串数组
        final String[] storedTexts = new String[splitStrings.length];

        for (int i = 0; i < splitStrings.length; i++) {
            // 创建 TextView 并设置属性
            textView = new TextView(this);
            textView.setText(splitStrings[i]);
            textView.setId(i + 1); // 设置视图ID
            textView.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            (int) (75 * getResources().getDisplayMetrics().density)));
            textView.setGravity(Gravity.CENTER);
            textView.setClickable(true);
            textView.setPadding(8, 8, 8, 8);
            textView.setFocusable(true);
            // textView.setBackgroundResource(android.R.attr.selectableItemBackground);
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextColor(Color.WHITE);

            // 存储文本到数组
            storedTexts[i] = splitStrings[i];

            // 设置点击事件监听器
            textView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 获取所点击的 TextView 的 ID
                            int clickedViewId = v.getId();
                            // 从存储的文本数组中获取文本
                            String clickedText = storedTexts[clickedViewId - 1];
                            // System.out.println("clickedtxt"+clickedText);
                            // 打印文本
                            new Thread() {
                                @Override
                                public void run() {

                                    try {
                                        toshow = HttpsUtils.sendOnce(clickedText);

                                    } catch (Exception err) {
                                        err.printStackTrace();
                                    }
                                }
                            }.start();
                            while (toshow == "null") {
                                try {
                                    Thread.sleep(50);
                                } catch (Exception err) {

                                }
                            }
                            righttextView.setText(toshow);
                            righttextView.invalidate();
                            toshow = "null";
                        }
                    });
            // 将 TextView 添加到LinearLayout中
            try {
                linearLayout.addView(textView);
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}
