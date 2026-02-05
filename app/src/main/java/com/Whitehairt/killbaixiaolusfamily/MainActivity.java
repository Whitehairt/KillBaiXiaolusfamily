package com.Whitehairt.killbaixiaolusfamily;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    
    private MaterialButton btnSign;
    private android.widget.TextView tvTotalSigns;
    private android.widget.TextView tvContinuousDays;
    private android.widget.TextView tvLastSignTime;
    
    private List<String> signDates = new ArrayList<>();
    private static final String SIGN_DATA_FILE = "sign_data.txt";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 隐藏状态栏逻辑
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, 
                           WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        
        setContentView(R.layout.activity_main);
        
        // 初始化视图
        btnSign = findViewById(R.id.btnSign);
        tvTotalSigns = findViewById(R.id.tvTotalSigns);
        tvContinuousDays = findViewById(R.id.tvContinuousDays);
        tvLastSignTime = findViewById(R.id.tvLastSignTime);
        
        // 加载签到数据
        loadSignData();
        
        // 更新UI
        updateUI();
        
        // 设置签到按钮点击事件
        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signToday();
            }
        });
    }
    
    /**
     * 今天签到
     */
    private void signToday() {
        String today = getCurrentDate();
        
        // 检查今天是否已经签到
        if (signDates.contains(today)) {
            Toast.makeText(this, "今天已经击杀过了", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 添加签到记录
        signDates.add(today);
        
        // 保存签到数据
        saveSignData();
        
        // 更新UI
        updateUI();
        
        // 禁用按钮并显示提示
        btnSign.setEnabled(false);
        btnSign.setText("今天已击杀");
        
        Toast.makeText(this, "杀死成功！", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 更新UI显示
     */
    private void updateUI() {
        // 更新总签到次数
        int totalSigns = signDates.size();
        tvTotalSigns.setText("击杀总次数: " + totalSigns);
        
        // 更新连续签到天数
        int continuousDays = calculateContinuousDays();
        tvContinuousDays.setText("连续击杀: " + continuousDays + "天");
        
        // 更新上次签到时间
        if (totalSigns > 0) {
            String lastSignDate = signDates.get(totalSigns - 1);
            tvLastSignTime.setText("上次击杀: " + formatDateForDisplay(lastSignDate));
        } else {
            tvLastSignTime.setText("上次击杀: 从未");
        }
        
        // 检查今天是否已经签到
        String today = getCurrentDate();
        if (signDates.contains(today)) {
            btnSign.setEnabled(false);
            btnSign.setText("今日已击杀");
        } else {
            btnSign.setEnabled(true);
            btnSign.setText("杀死白小鹿全家");
        }
    }
    
    /**
     * 计算连续签到天数
     */
    private int calculateContinuousDays() {
        if (signDates.isEmpty()) {
            return 0;
        }
        
        int continuousDays = 1;
        Calendar calendar = Calendar.getInstance();
        
        // 从最近一天开始往前检查
        for (int i = signDates.size() - 1; i > 0; i--) {
            try {
                Date currentDate = parseDate(signDates.get(i));
                Date previousDate = parseDate(signDates.get(i - 1));
                
                calendar.setTime(currentDate);
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                Date yesterday = calendar.getTime();
                
                // 检查是否是连续的一天
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                if (sdf.format(yesterday).equals(signDates.get(i - 1))) {
                    continuousDays++;
                } else {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        
        return continuousDays;
    }
    
    /**
     * 保存签到数据到私有目录
     */
    private void saveSignData() {
        File file = new File(getFilesDir(), SIGN_DATA_FILE);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String date : signDates) {
                writer.write(date);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存数据失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 从私有目录加载签到数据
     */
    private void loadSignData() {
        File file = new File(getFilesDir(), SIGN_DATA_FILE);
        
        if (!file.exists()) {
            return;
        }
        
        signDates.clear();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    signDates.add(line.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "加载数据失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 获取当前日期字符串
     */
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
    
    /**
     * 解析日期字符串
     */
    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
            return new Date();
        }
    }
    
    /**
     * 格式化日期显示
     */
    private String formatDateForDisplay(String dateStr) {
        try {
            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdfInput.parse(dateStr);
            
            SimpleDateFormat sdfOutput = new SimpleDateFormat("MM月dd日", Locale.getDefault());
            return sdfOutput.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回应用时检查是否是新的一天
        updateUI();
    }
}