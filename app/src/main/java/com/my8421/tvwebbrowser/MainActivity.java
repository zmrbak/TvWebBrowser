package com.my8421.tvwebbrowser;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.view.View.SCROLLBARS_OUTSIDE_OVERLAY;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private String url = "";
    private PowerManager.WakeLock mWakeLock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //禁止休眠，
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        //u盘位置
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        //U盘上的文件夹
        String videoDir = "tvshow";
        //目录下的文件列表
        File files[] = null;
        //目录不存在，则创建目录
        File dir = new File(filePath + "/" + videoDir);
        if (!dir.exists()) {
            dir.mkdirs();
            Toast.makeText(getApplicationContext(), "请在U盘的" + videoDir + "文件夹中放一个*.txt文件!", Toast.LENGTH_LONG).show();
            return;
        }

        //查找目录下的所有文件

        files = dir.listFiles();
        if (files == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "空目录，请在U盘的" + videoDir + "文件夹中放一个*.txt文件！", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        //提取url
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                continue;
            } else {
                if (files[i].getName().toLowerCase().endsWith("txt")) {
                    String filename = files[i].getAbsolutePath();
                    InputStreamReader isr = null;
                    String lineTxt = null;
                    try {
                        isr = new InputStreamReader(new FileInputStream(filename));
                        BufferedReader br = new BufferedReader(isr);
                        lineTxt = br.readLine();
                        isr.close();
                        br.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (lineTxt!=null   && lineTxt != "") {
                        url = lineTxt;
                    }
                }
            }
        }

        if (url == "") {
            Toast toast = Toast.makeText(getApplicationContext(), "没找到文件,请在U盘的" + videoDir + "文件夹中放一个*.txt文件！", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        webView = this.findViewById(R.id.myWeb);
        webView.getSettings().setDefaultTextEncodingName("gbk");
        //支持javascript
        webView.getSettings().setJavaScriptEnabled(true);
        //取消滚动条
        webView.setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);
        //主页
        webView.loadUrl(url);

        //用于网页跳转 并跳转至当前webView
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        registerReceiver(mMasterResetReciever, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,"类名");
        mWakeLock.acquire();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mWakeLock != null){
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    BroadcastReceiver mMasterResetReciever = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent) {
            try {
                Intent i = new Intent();
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setClass(context, MainActivity.class);
                context.startActivity(i);
            } catch (Exception e) {
                Log.i("Output:", e.toString());
            }
        }
    } ;

}
