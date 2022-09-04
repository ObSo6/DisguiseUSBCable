package com.obso6.disguiseapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.obso6.disguiseapp.R;

//首页启动图，3s后跳转MainActivity
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取消标题设置
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //自带标题栏隐藏
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }
        setContentView(R.layout.welcome_activity);
        //定时 3秒
        int time = 3000;
        mHandler.sendEmptyMessageDelayed(0, time);
    }

    //消息处理对象,负责发送与处理消息
    private final Handler mHandler = new Handler(msg -> {
        toMainActivity();
        return false;
    });

    //跳转MainActivity
    public void toMainActivity(){
        Intent intent = new Intent( WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
