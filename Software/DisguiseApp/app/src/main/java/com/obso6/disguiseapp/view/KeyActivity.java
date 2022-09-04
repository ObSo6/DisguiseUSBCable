package com.obso6.disguiseapp.view;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.obso6.disguiseapp.R;
import com.obso6.disguiseapp.config.BluetoothChatService;
import com.obso6.disguiseapp.config.Exploit;
import com.obso6.disguiseapp.config.Script;
import com.obso6.disguiseapp.config.SppBle;
import com.obso6.disguiseapp.config.Timer;
import java.util.UUID;

public class KeyActivity extends AppCompatActivity {

    private TextView subtitle;
    //按钮
    private ImageView refresh;
    private ImageView stop;
    //加载框
    private ProgressBar mProgressBar;
    //输入区域
    private EditText stringText;
    private int focusFalg = 0;
    //按键
    private TextView keyEsc,keyF1,keyF2,keyF3,keyF4,keyF5,keyF6,keyF7,keyF8,keyF9,keyF10,keyF11,keyF12,keyPrt,keyDel;
    private TextView keyWave,key1,key2,key3,key4,key5,key6,key7,key8,key9,key0,keyShort,keyAdd,keyBack;
    private TextView keyTab;
    private TextView keyQ;
    private TextView keyW;
    private TextView keyE;
    private TextView keyR;
    private TextView keyY;
    private TextView keyU;
    private TextView keyI;
    private TextView keyO;
    private TextView keyP;
    private TextView keyBracketsLeft;
    private TextView keyBracketsRight;
    private TextView keyVer;
    private TextView keyCaps,keyA,keyS,keyD,keyF,keyG,keyH,keyJ,keyK,keyL,keyColon,keyQuo,keyEnter;
    private TextView keyShift,keyZ,keyX,keyC,keyV,keyB,keyN,keyM,keyAngleLeft,keyAngleRight,keyQue,keyHome,keyUp,keyIns;
    private TextView keyCtrl,keyWin,keyAlt,keySpace,keyApplication,keyLeft,keyDown,keyRight;
    //消息相关
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    //相关
    public static final String DEVICE_NAME = "";
    public static final String TOAST = "";
    private BluetoothDevice mBluetoothDevice;
    private SppBle mSppBle;
    private BluetoothChatService mChatService;
    //蓝牙设备类型
    private Boolean devTypes;
    //UUID
    private UUID bleSevice,txo,rxi;
    //时间
    private Timer delayStart;
    //执行脚本转换引擎
    public Exploit mexploit;
    //设备状态
    private int connectFlag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //防止键盘顶起
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.key_activity);
        //设置为横屏
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        //初始化view
        initView();
    }

    //执行badusb的payload
    private void executePayload(String cmd) throws Exception {
        //脚本
        Script mScript = new Script(cmd);
        mexploit = new Exploit(mSppBle);
        mexploit.executeScript(mScript,"");
    }

    //初始化，同时忽略点击警告
    @SuppressLint({"ClickableViewAccessibility"})
    private void initView() {
        //加载框
        mProgressBar = findViewById(R.id.title_bar).findViewById(R.id.progress);
        mProgressBar.setVisibility(View.GONE);
        //绑定控件
        //标题
        TextView title = findViewById(R.id.title_bar).findViewById(R.id.title);
        subtitle = findViewById(R.id.title_bar).findViewById(R.id.subtitle);
        refresh = findViewById(R.id.title_bar).findViewById(R.id.refresh);
        //点击连接按钮
        refresh.setOnClickListener(view -> {
            refresh.setVisibility(View.GONE);
            stop.setVisibility(View.VISIBLE);
            tryConnect();
        });
        //点击停止连接按钮
        stop = findViewById(R.id.title_bar).findViewById(R.id.stop);
        stop.setVisibility(View.GONE);
        stop.setOnClickListener(view -> {
            refresh.setVisibility(View.VISIBLE);
            stop.setVisibility(View.GONE);
            tryConnect();
        });
        //设置按钮
        ImageView setting = findViewById(R.id.title_bar).findViewById(R.id.setting);
        setting.setOnClickListener(view -> {
            Intent intent=new Intent(KeyActivity.this,SettingActivity.class);
            startActivity(intent);
        });
        //update ui
        title.setText("键 盘");
        stringText  = (EditText) findViewById(R.id.key_bar).findViewById(R.id.string_text);
        keyEsc = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_esc);
        keyF1 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_f1);
        keyF2 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_f2);
        keyF3 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_f3);
        keyF4 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_f4);
        keyF5 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_f5);
        keyF6 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_f6);
        keyF7 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_f7);
        keyF8 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_f8);
        keyF9 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_f9);
        keyF10 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_f10);
        keyF11 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_f11);
        keyF12 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_f12);
        keyPrt = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_prt);
        keyDel = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_del);
        keyWave = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_wave);
        key1 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_1);
        key2 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_2);
        key3 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_3);
        key4 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_4);
        key5 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_5);
        key6 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_6);
        key7 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_7);
        key8 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_8);
        key9 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_9);
        key0 = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_0);
        keyShort = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_short);
        keyAdd = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_add);
        keyBack = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_back);
        keyTab = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_tab);
        keyQ = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_q);
        keyW = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_w);
        keyE = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_e);
        keyR = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_r);
        TextView keyT = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_t);
        keyY = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_y);
        keyU = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_u);
        keyI = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_i);
        keyO = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_o);
        keyP = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_p);
        keyBracketsLeft = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_bracketsleft);
        keyBracketsRight = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_bracketsright);
        keyVer = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_ver);
        keyCaps = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_caps);
        keyA = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_a);
        keyS = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_s);
        keyD = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_d);
        keyF = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_f);
        keyG = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_g);
        keyH = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_h);
        keyJ = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_j);
        keyK = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_k);
        keyL = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_l);
        keyColon = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_colon);
        keyQuo = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_quo);
        keyEnter = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_enter);
        keyShift = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_shift);
        keyZ = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_z);
        keyX = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_x);
        keyC = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_c);
        keyV = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_v);
        keyB = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_b);
        keyN = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_n);
        keyM = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_m);
        keyAngleLeft = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_angleleft);
        keyAngleRight = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_angleright);
        keyQue = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_que);
        keyHome = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_home);
        keyUp = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_up);
        keyIns = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_ins);
        keyCtrl = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_ctrl);
        keyWin = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_win);
        keyAlt = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_alt);
        keySpace = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_space);
        keyApplication = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_application);
        keyLeft = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_left);
        keyDown = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_down);
        keyRight = (TextView) findViewById(R.id.key_bar).findViewById(R.id.key_right);
        //输入区域
        stringText.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                //获取焦点
                focusFalg = focusFalg+1;
            }else{
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                if(focusFalg == 1){
                    //判断有值
                    String res = stringText.getText().toString();
                    if(res.length()>0){
                        try {
                            executePayload("STRINGE "+res);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        focusFalg = 0;
                    }
                }
            }
        });
        //按键点击
        keyEsc.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyEsc.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY ESC");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyEsc.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyEsc.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyF1.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyF1.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY F1");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyF1.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyF1.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyF2.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyF2.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY F2");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyF2.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyF2.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyF3.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyF3.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY F3");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyF3.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyF3.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyF4.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyF4.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY F4");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyF4.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyF4.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyF5.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyF5.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY F5");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyF5.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyF5.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyF6.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyF6.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY F6");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyF6.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyF6.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyF7.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyF7.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY F7");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyF7.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyF7.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyF8.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyF8.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY F8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyF8.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyF8.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyF9.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyF9.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY F9");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyF9.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyF9.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyF10.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyF10.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY F10");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyF10.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyF10.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyF11.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyF11.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY F11");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyF11.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyF11.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyF12.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyF12.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY F12");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyF12.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyF12.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyPrt.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyPrt.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY PRT");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyPrt.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyPrt.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyDel.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyDel.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY DEL");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyDel.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyDel.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyWave.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyWave.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY WAVE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyWave.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyWave.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        key1.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                key1.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY 1");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                key1.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                key1.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        key2.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                key2.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY 2");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                key2.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                key2.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        key3.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                key3.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY 3");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                key3.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                key3.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        key4.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                key4.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY 4");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                key4.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                key4.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        key5.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                key5.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY 5");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                key5.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                key5.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        key6.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                key6.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY 6");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                key6.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                key6.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        key7.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                key7.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY 7");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                key7.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                key7.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        key8.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                key8.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY 8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                key8.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                key8.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        key9.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                key9.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY 9");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                key9.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                key9.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        key0.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                key0.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY 0");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                key0.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                key0.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyShort.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyShort.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY SHORT");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyShort.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyShort.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyAdd.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyAdd.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY ADD");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyAdd.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyAdd.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyBack.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyBack.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY BACK");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyBack.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyBack.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyTab.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyTab.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY TAB");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyTab.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyTab.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyQ.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyQ.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY Q");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyQ.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyQ.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyW.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyW.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY W");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyW.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyW.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyE.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyE.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY E");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyE.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyE.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyR.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyR.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY R");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyR.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyR.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyT.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyR.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY T");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyR.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyR.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyY.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyY.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY Y");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyY.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyY.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyU.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyU.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY U");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyU.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyU.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyI.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyI.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY I");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyI.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyI.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyO.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyO.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY O");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyO.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyO.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyP.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyP.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY P");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyP.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyP.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyBracketsLeft.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyBracketsLeft.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY BRACKETSLEFT");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyBracketsLeft.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyBracketsLeft.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyBracketsRight.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyBracketsRight.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY BRACKETSRIGHT");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyBracketsRight.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyBracketsRight.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyVer.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyVer.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY VER");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyVer.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyVer.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyCaps.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyCaps.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY CAPS");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyCaps.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyCaps.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyA.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyA.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY A");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyA.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyA.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyS.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyS.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY S");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyS.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyS.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyD.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyD.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY D");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyD.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyD.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyF.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyF.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY F");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyF.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyF.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyG.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyG.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY G");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyG.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyG.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyH.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyH.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY H");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyH.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyH.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyJ.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyJ.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY J");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyJ.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyJ.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyK.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyK.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY K");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyK.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyK.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyL.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyL.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY L");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyL.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyL.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyColon.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyColon.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY COLON");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyColon.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyColon.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyQuo.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyQuo.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY QUO");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyQuo.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyQuo.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyEnter.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyEnter.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY ENTER");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyEnter.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyEnter.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyShift.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyShift.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY SHIFT");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyShift.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyShift.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyZ.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyZ.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY Z");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyZ.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyZ.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyX.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyX.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY X");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyX.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyX.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyC.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyC.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY C");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyC.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyC.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyV.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyV.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY V");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyV.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyV.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyB.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyB.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY B");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyB.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyB.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyN.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyN.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY N");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyN.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyN.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyM.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyM.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY M");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyM.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyM.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyAngleLeft.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyAngleLeft.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY ANGLELEFT");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyAngleLeft.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyAngleLeft.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyAngleRight.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyAngleRight.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY ANGLERIGHT");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyAngleRight.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyAngleRight.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyQue.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyQue.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY QUE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyQue.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyQue.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyHome.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyHome.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY HOME");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyHome.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyHome.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyUp.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyUp.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY UP");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyUp.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyUp.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyIns.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyIns.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY INS");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyIns.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyIns.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyCtrl.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyCtrl.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY CTRL");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyCtrl.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyCtrl.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyWin.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyWin.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY WIN");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyWin.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyWin.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyAlt.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyAlt.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY ALT");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyAlt.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyAlt.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keySpace.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keySpace.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY SPACE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keySpace.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keySpace.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyApplication.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyApplication.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY APPLICATION");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyApplication.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyApplication.setBackgroundResource(R.drawable.key_blue);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyLeft.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyLeft.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY LEFT");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyLeft.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyLeft.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyDown.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyDown.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY DONW");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyDown.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyDown.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        keyRight.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                keyRight.setBackgroundResource(R.drawable.noactive);
                try {
                    executePayload("KEY RIGHT");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                keyRight.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(event.getAction() == MotionEvent.ACTION_CANCEL){
                keyRight.setBackgroundResource(R.drawable.key);
                try {
                    executePayload("KEY RELEASE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
    }

    //连接设备
    private void tryConnect() {
        if(mBluetoothDevice == null)return;
        //BLE设备
        if(devTypes){
            if(mSppBle == null)return;
            if(mSppBle.getConnectionState() == 3){
                //断开连接
                mSppBle.disconnect();
                return;
            }
            mSppBle.setUUID(bleSevice,txo,rxi);
            //断开上次
            mSppBle.disconnect();
            mSppBle.connect(mBluetoothDevice, this);
        }else {
            //SPP
            if(mChatService == null)return;
            if(mChatService.getState() == 3){
                mChatService.stop();
            }
            //不连接SPP设备
            //mChatService.connect(mBluetoothDevice);
        }
    }

    //消息
    private final Handler mHandler = new Handler(msg -> {
        switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                    case BluetoothChatService.STATE_CONNECTED:
                        subtitle.setText("连接成功");
                        stop.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                        refresh.setVisibility(View.GONE);
                        connectFlag = 2;
                        break;
                    case BluetoothChatService.STATE_CONNECTING:
                        subtitle.setText("正在连接");
                        stop.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        refresh.setVisibility(View.GONE);
                        connectFlag = 1;
                        break;
                    case BluetoothChatService.STATE_LISTEN:
                    case BluetoothChatService.STATE_NONE:
                        subtitle.setText("连接断开");
                        stop.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.GONE);
                        refresh.setVisibility(View.VISIBLE);
                        connectFlag = 0;
                        break;
                }
                break;
        }
        return false;
    });

    //toast通知
    private void toast(int message) {
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.toast_bar, null);
        TextView title = (TextView) view.findViewById(R.id.toast);
        title.setText(message);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.TOP, 0, 128);
        toast.setView(view);
        toast.show();
    }

    //双击退出
    private long exittime;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //按下键盘上返回按钮
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(System.currentTimeMillis() - exittime >1200) //
            {
                toast(R.string.double_click_exit);
                exittime = System.currentTimeMillis();
            }
            else
            {
                //断开连接
                if (connectFlag == 2 || connectFlag == 1) {
                    toast(R.string.exit_toast);
                }else{
                    finish();
                }
            }
            return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }

    //触摸
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View mEditText = (EditText) findViewById(R.id.key_bar).findViewById(R.id.string_text);
            //判断是否被点击
            if (!isTouchPointInView(mEditText, (int) ev.getX(), (int) ev.getY())) {
                mEditText.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    //触摸
    private boolean isTouchPointInView(View view, int x, int y) {
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        return (y >= top && y <= bottom && x >= left && x <= right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        connectFlag = 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSppBle.disconnect();
    }

    @Override
    protected void onStart() {
        //设置
        super.onStart();
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String devType = mSharedPreferences.getString("device_type", "");
        //判断设备类型，默认为BLE，此处不连接SPP设备
        if (devType.equals("[BLE]")) {
            devTypes = true;
            String UART_UUID = "0000FFE0-0000-1000-8000-00805F9B34FB";
            String TXO_UUID = "0000FFE1-0000-1000-8000-00805F9B34FB";
            String RXI_UUID = "0000FFE1-0000-1000-8000-00805F9B34FB";
            bleSevice = UUID.fromString(UART_UUID);
            txo = UUID.fromString(TXO_UUID);
            rxi = UUID.fromString(RXI_UUID);
        } else {
            devTypes = false;
        }
        //蓝牙服务
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mSharedPreferences.getString("device_uuid", ""));
        mChatService = new BluetoothChatService(this, mHandler);
        mSppBle = new SppBle(this, mHandler);
        if(connectFlag == 0){
            delayStart = new Timer(100, () -> {
                tryConnect();
                delayStart.stop();
            });
            delayStart.restart();
        }
    }
}


