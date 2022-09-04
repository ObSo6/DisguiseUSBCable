package com.obso6.disguiseapp.view;

import static com.obso6.disguiseapp.view.HidActivity.ITEM_Content;
import static com.obso6.disguiseapp.view.HidActivity.ITEM_ID;
import static com.obso6.disguiseapp.view.HidActivity.ITEM_Introduce;
import static com.obso6.disguiseapp.view.HidActivity.ITEM_Name;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.obso6.disguiseapp.R;
import com.obso6.disguiseapp.config.BluetoothChatService;
import com.obso6.disguiseapp.config.DaoScript;
import com.obso6.disguiseapp.config.Exploit;
import com.obso6.disguiseapp.config.Script;
import com.obso6.disguiseapp.config.ScriptTable;
import com.obso6.disguiseapp.config.SppBle;
import com.obso6.disguiseapp.config.Timer;
import java.util.UUID;

public class EditActivity extends AppCompatActivity {

    private TextView subtitle;
    //按钮
    private ImageView refresh;
    private ImageView stop;
    //加载框
    private ProgressBar mProgressBar;
    //dao
    private final DaoScript mDaoSctipt = new DaoScript(this);
    //编辑框
    private EditText scriptName, scriptIntroduce, scriptContent;
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
    private String scriptIdParam = null;
    private String scriptNameParam = null;
    private String scriptContentParam= null;
    private String scriptIntroduceParam = null;
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity);
        //获取数据
        //设置
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String devType = mSharedPreferences.getString("device_type", "");
        scriptIdParam = getIntent().getStringExtra(ITEM_ID);
        scriptNameParam = getIntent().getStringExtra(ITEM_Name);
        scriptContentParam = getIntent().getStringExtra(ITEM_Content);
        scriptIntroduceParam = getIntent().getStringExtra(ITEM_Introduce);
        //判断设备类型，默认为BLE，此处不连接SPP设备
        if(devType.equals("[BLE]")){
            devTypes = true;
            String UART_UUID = "0000FFE0-0000-1000-8000-00805F9B34FB";
            String TXO_UUID = "0000FFE1-0000-1000-8000-00805F9B34FB";
            String RXI_UUID = "0000FFE1-0000-1000-8000-00805F9B34FB";
            bleSevice = UUID.fromString(UART_UUID);
            txo = UUID.fromString(TXO_UUID);
            rxi = UUID.fromString(RXI_UUID);
        }else {
            devTypes = false;
        }
        //蓝牙服务
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothDevice  =  mBluetoothAdapter.getRemoteDevice(mSharedPreferences.getString("device_uuid",""));
        mSppBle = new SppBle(this,mHandler);
        mChatService = new BluetoothChatService(this, mHandler);
        delayStart = new Timer(100, () -> {
            tryConnect();
            delayStart.stop();
        });
        delayStart.restart();
        //初始化view
        initView();
    }

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
            Intent intent=new Intent(EditActivity.this,SettingActivity.class);
            startActivity(intent);
        });
        scriptName = findViewById(R.id.edit_bar).findViewById(R.id.script_name);
        scriptIntroduce = findViewById(R.id.edit_bar).findViewById(R.id.script_introduce);
        scriptContent = findViewById(R.id.edit_bar).findViewById(R.id.script_content);
        TextView run = findViewById(R.id.edit_bar).findViewById(R.id.run);
        TextView save = findViewById(R.id.edit_bar).findViewById(R.id.save);
        TextView del = findViewById(R.id.edit_bar).findViewById(R.id.del);
        run.setOnClickListener(view -> {
            try {
                executePayload(scriptContent.getText().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        save.setOnClickListener(view -> {
            if(scriptIdParam == null){
                //新增脚本
                int databaseSize = mDaoSctipt.quereyScriptSize();
                ScriptTable scriptTable = new ScriptTable(databaseSize+"", scriptIntroduce.getText().toString(),scriptContent.getText().toString(),scriptIntroduce.getText().toString());
                mDaoSctipt.addScript(scriptTable);
                toast(scriptName.getText().toString() + " 保存成功！");
            }else{
                //编辑脚本
                ScriptTable scriptTable = new ScriptTable(scriptIdParam, scriptIntroduce.getText().toString(),scriptContent.getText().toString(),scriptIntroduce.getText().toString());
                mDaoSctipt.editScript(scriptTable);
                toast(scriptName.getText().toString() + " 修改成功！");
            }

        });
        del.setOnClickListener(view -> {
            if(scriptIdParam == null){
                scriptName.setText("");
                scriptIntroduce.setText("");
                scriptContent.setText("");
            }else{
                mDaoSctipt.deleteScript(scriptIdParam);
                toast(scriptNameParam+" 删除成功！");
            }
        });
        //update ui
        title.setText("编 辑");
        if(scriptNameParam != null){
            scriptName.setText(scriptNameParam);
        }
        if(scriptContentParam != null){
            scriptContent.setText(scriptContentParam);
        }
        if(scriptIntroduceParam != null){
            scriptIntroduce.setText(scriptIntroduceParam);
        }
    }

    //执行badusb的payload
    private void executePayload(String cmd) throws Exception {
        //脚本
        Script mScript = new Script(cmd);
        mexploit = new Exploit(mSppBle);
        mexploit.executeScript(mScript,"");
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
    private void toast(String message) {
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
                toast("再次点击返回按钮退出");
                exittime = System.currentTimeMillis();
            }
            else
            {
                //断开连接
                if (connectFlag == 2) {
                    tryConnect();
                }
                finish();
            }
            return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }
}
