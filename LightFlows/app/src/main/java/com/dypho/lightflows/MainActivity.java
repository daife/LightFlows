
package com.dypho.lightflows;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.view.LayoutInflater;
import com.dypho.lightflows.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;
    private TextView textViewEditParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and get instance of binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // set content view to binding's root
        setContentView(binding.getRoot());
        showMessage();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        textViewEditParams = findViewById(R.id.textView_editParams);
        // Show alert dialog
        textViewEditParams.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 在这里执行您的自定义代码
            showAlert();
        }
    });
        binding.textViewInputMethodSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        binding.textViewChooseInputMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showInputMethodPicker();
                }
            }
        });
    }
    private void showAlert() {
    LayoutInflater inflater = this.getLayoutInflater();
    final View dialogView = inflater.inflate(R.layout.dialog_settings, null);
                // Pre-populate the dialog with existing values
        ((EditText) dialogView.findViewById(R.id.cameraIndex)).setText(String.valueOf(sharedPreferences.getInt("cameraIndex", 0)));
        ((EditText) dialogView.findViewById(R.id.keycode)).setText(String.valueOf(sharedPreferences.getInt("keycode", 5)));
          ((EditText) dialogView.findViewById(R.id.fps)).setText(String.valueOf(sharedPreferences.getInt("fps", 15)));
           ((EditText) dialogView.findViewById(R.id.dy)).setText(String.valueOf(sharedPreferences.getInt("dy", 10)));
     //    ((EditText) dialogView.findViewById(R.id.imgWidth)).setText(String.valueOf(sharedPreferences.getInt("imgWidth", 75)));
           ((EditText) dialogView.findViewById(R.id.templateWidth)).setText(String.valueOf(sharedPreferences.getInt("templateWidth", 40)));
        ((EditText) dialogView.findViewById(R.id.threshold)).setText(String.valueOf(sharedPreferences.getInt("threshold", 10)));
         ((EditText) dialogView.findViewById(R.id.matchdegree)).setText(String.valueOf(sharedPreferences.getInt("matchdegree", 70)));
        ((EditText) dialogView.findViewById(R.id.ifleft)).setText(String.valueOf(sharedPreferences.getBoolean("ifleft", false)));
         ((EditText) dialogView.findViewById(R.id.filepath)).setText(sharedPreferences.getString("filepath", "/sdcard/Pictures/tmp.jpg"));
        ((EditText) dialogView.findViewById(R.id.ocrapi)).setText(String.valueOf(sharedPreferences.getInt("ocrapi", 1)));
    ScrollView scrollView = new ScrollView(this);
    scrollView.addView(dialogView);

    new AlertDialog.Builder(this)
        .setTitle("设置参数")
        .setView(scrollView) // 设置为ScrollView
        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // 获取输入值并保存设置
                // ...
                       // Get the input values
                Catcher.cameraIndex = Integer.parseInt(((EditText) dialogView.findViewById(R.id.cameraIndex)).getText().toString());
                    FastInputIME.keycode = Integer.parseInt(((EditText) dialogView.findViewById(R.id.keycode)).getText().toString());
                    Catcher.fps = Integer.parseInt(((EditText) dialogView.findViewById(R.id.fps)).getText().toString());
                    Catcher.dy = Integer.parseInt(((EditText) dialogView.findViewById(R.id.dy)).getText().toString());
                //    Catcher.imgWidth = (double) Integer.parseInt(((EditText) dialogView.findViewById(R.id.imgWidth)).getText().toString());
                    Catcher.templateWidth = Integer.parseInt(((EditText) dialogView.findViewById(R.id.templateWidth)).getText().toString());
                    Catcher.threshold = Integer.parseInt(((EditText) dialogView.findViewById(R.id.threshold)).getText().toString());
                    Catcher.matchdegree = Integer.parseInt(((EditText) dialogView.findViewById(R.id.matchdegree)).getText().toString());
                    FastInputIME.ifleft = Boolean.parseBoolean(((EditText) dialogView.findViewById(R.id.ifleft)).getText().toString());
                    Catcher.filePath = ((EditText) dialogView.findViewById(R.id.filepath)).getText().toString();
                    Catcher.ocrapi = Integer.parseInt(((EditText) dialogView.findViewById(R.id.ocrapi)).getText().toString());
                    Catcher.init();

                    // Save the settings
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("cameraIndex", Catcher.cameraIndex);
                    editor.putInt("keycode", FastInputIME.keycode);
                    editor.putInt("fps", Catcher.fps);
                    editor.putInt("dy", Catcher.dy);
                    //editor.putInt("imgWidth", (int)Catcher.imgWidth);
                    editor.putInt("templateWidth", Catcher.templateWidth);
                    editor.putInt("threshold", Catcher.threshold);
                    editor.putInt("matchdegree", Catcher.matchdegree);
                    editor.putBoolean("ifleft", FastInputIME.ifleft);
                    editor.putString("filepath",Catcher.filePath);
                    editor.putInt("ocrapi", Catcher.ocrapi);
                    editor.apply();
            }
        })
        .show();
}

    private void showMessage() {
        new AlertDialog.Builder(this)
            .setTitle("使用条款") // 设置对话框的标题
            .setMessage("本软件目前阶段为完全免费使用。任何形式的修改、再分发或销售均属于侵权行为。我们强烈反对未经授权的二次销售，并将采取法律行动以保护知识产权。\n" +
"\n" +
"若您发现有人在未经许可的情况下销售本软件，请立即向我们举报。更新源位于酷安（Github已停止更新):"+"\n"+"@云都望月"+"\n" +
"\n" +
"用户可以在此地址上报告软件中的任何问题（bug）并获取最新的教程和信息。我们鼓励用户通过官方渠道进行交流，以确保获取准确和安全的信息。\n" +
"\n" +
"隐私条款：\n" +
"在使用本软件时，用户必须遵守所有适用的法律和规定。用户应保护自己的隐私和数据，不应将软件用于任何非法或未经授权的目的。用户在使用本软件时，同意对其数据的合法使用负责。\n" +
"\n" +
"免责声明：\n" +
"用户在使用本软件时，必须承担起作为消费者的责任。任何不合法的行为均为用户个人行为，与软件开发者无关。本软件的所有功能和信息仅供学习和交流目的。用户应自行承担使用软件所产生的一切后果。\n" +
"\n" +
"谢谢您的理解与支持。"+"\n"
            +" © 2024 酷安用户@云都望月") // 设置对话框的内容
            .setPositiveButton("继续即同意", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // 点击确认按钮后的操作
                }
            })
            .show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}
