package com.obso6.disguiseapp.view;

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
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.obso6.disguiseapp.R;
import com.obso6.disguiseapp.config.BluetoothChatService;
import com.obso6.disguiseapp.config.DaoScript;
import com.obso6.disguiseapp.config.Exploit;
import com.obso6.disguiseapp.config.Script;
import com.obso6.disguiseapp.config.ScriptTable;
import com.obso6.disguiseapp.config.SppBle;
import com.obso6.disguiseapp.config.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HidActivity extends AppCompatActivity {

    private TextView subtitle;
    //按钮
    private ImageView refresh;
    private ImageView stop;
    //加载框
    private ProgressBar mProgressBar;
    //提示
    private TextView toastText;
    //listView
    private ListView scriptList;
    //dao
    private final DaoScript mDaoSctipt = new DaoScript(this);
    //消息相关
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    //相关
    public static final String DEVICE_NAME="";
    public static final String TOAST = "";
    private BluetoothDevice mBluetoothDevice;
    private SppBle mSppBle;
    private BluetoothChatService mChatService;
    //设置取值
    private String devType;
    //蓝牙设备类型
    private Boolean devTypes;
    //UUID
    private UUID bleSevice,txo,rxi;
    //时间
    private Timer delayStart;
    //执行脚本转换引擎
    public Exploit mexploit;
    //脚本列表适配器
    private ScriptListAdapter mScriptListAdapter;
    //activity跳转传参
    public static final String ITEM_ID = "itemID";
    public static final String ITEM_Name = "itemName";
    public static final String ITEM_Content = "itemContent";
    public static final String ITEM_Introduce = "itemIntroduce";
    //设备状态
    private int connectFlag=0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hid_activity);
        //初始化view
        initView();
    }

    //查询数据库
    private void queryDataBase() {
        mScriptListAdapter.clear();
        //list
        List<ScriptTable> mScriptTable = mDaoSctipt.quereyScript();
        if(mScriptTable.isEmpty()){
            toastText.setText("脚本列表--列表为空，请添加脚本！");
        }else{
            //update ui
            scriptList.setAdapter(mScriptListAdapter);
            for(int i = 0; i< mScriptTable.size(); i++)
            {
                mScriptListAdapter.addScript(mScriptTable.get(i).getId(), mScriptTable.get(i).getName(), mScriptTable.get(i).getContent(), mScriptTable.get(i).getIntroduce());
                mScriptListAdapter.notifyDataSetChanged();
            }
            //点击列表
            scriptList.setOnItemClickListener((adapterView, view, i, l) -> {

                AlertDialog.Builder builder = new AlertDialog.Builder(HidActivity.this);
                builder.setTitle("执行当前脚本吗？");
                builder.setPositiveButton("执行", (dialog, which) -> {
                    try {
                        executePayload(mScriptListAdapter.getScriptContent(i));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                //设置一个NegativeButton
                builder.setNegativeButton("取消", (dialog, which) -> {
                });
                //    显示出该对话框
                builder.show();
            });
            //长按
            scriptList.setOnItemLongClickListener((adapterView, view, i, l) -> {
                if (connectFlag == 2) {
                    tryConnect();
                }
                String scriptId = mScriptListAdapter.getScriptId(i);
                String scriptName = mScriptListAdapter.getScriptName(i);
                String scriptContent = mScriptListAdapter.getScriptContent(i);
                String scriptIntroduce= mScriptListAdapter.getScriptIntroduce(i);
                //编辑跳转
                Intent intent = new Intent(HidActivity.this, EditActivity.class);
                if (devType.equals("[BLE]")) {
                    intent.putExtra(ITEM_ID,scriptId);
                    intent.putExtra(ITEM_Name,scriptName);
                    intent.putExtra(ITEM_Content,scriptContent);
                    intent.putExtra(ITEM_Introduce,scriptIntroduce);
                }
                startActivity(intent);
                return true;
            });
        }
    }

    //执行badusb的payload
    private void executePayload(String cmd) throws Exception {
        //脚本
        Script mScript = new Script(cmd);
        mexploit = new Exploit(mSppBle);
        mexploit.executeScript(mScript,"");
    }

    private void initView() {
        //加载框
        mProgressBar = findViewById(R.id.title_bar).findViewById(R.id.progress);
        mProgressBar.setVisibility(View.GONE);
        //View
        View addScript = findViewById(R.id.add_bar);
        addScript.setOnClickListener(view -> {
            if (connectFlag == 2) {
                tryConnect();
            }
            //添加跳转
            Intent intent = new Intent(HidActivity.this, EditActivity.class);
            startActivity(intent);
        });
        scriptList = findViewById(R.id.hid_bar).findViewById(R.id.script_list);
        toastText = (TextView) findViewById(R.id.toast_text);
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
            Intent intent=new Intent(HidActivity.this,SettingActivity.class);
            startActivity(intent);
        });
        //update ui
        title.setText("脚 本");
        toastText.setText("脚本列表");
        mScriptListAdapter = new ScriptListAdapter();
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

    private class ScriptListAdapter extends BaseAdapter {

        private final ArrayList<String> mId;
        private final ArrayList<String> mName;
        private final ArrayList<String> mContent;
        private final ArrayList<String> mIntroduce;
        private final LayoutInflater mInflator;

        public ScriptListAdapter() {
            super();
            mId = new ArrayList<>();
            mName = new ArrayList<>();
            mContent = new ArrayList<>();
            mIntroduce = new ArrayList<>();
            mInflator = HidActivity.this.getLayoutInflater();
        }

        public void addScript(String id, String name, String content, String introduce) {
            mId.add(id);
            mName.add(name);
            mContent.add(content);
            mIntroduce.add(introduce);
        }

        public String getScriptId(int position) {
            return mId.get(position);
        }

        public String getScriptName(int position) {
            return mName.get(position);
        }

        public String getScriptContent(int position) {
            return mContent.get(position);
        }

        public String getScriptIntroduce(int position) {
            return mIntroduce.get(position);
        }

        public void clear() {
            mId.clear();
            mName.clear();
            mContent.clear();
            mIntroduce.clear();
        }

        @Override
        public int getCount() {
            return mId.size();
        }

        @Override
        public Object getItem(int i) {
            return mId.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder mViewHolder;
            //生成listview
            if (view == null) {
                view = mInflator.inflate(R.layout.script_listitem, null);
                mViewHolder = new HidActivity.ViewHolder();
                mViewHolder.id = (TextView) view.findViewById(R.id.id);
                mViewHolder.name = (TextView) view.findViewById(R.id.name);
                mViewHolder.introduce = (TextView) view.findViewById(R.id.introduce);
                view.setTag(mViewHolder);
            } else {
                mViewHolder = (HidActivity.ViewHolder) view.getTag();
            }
            mViewHolder.id.setText("[Num."+mId.get(i)+"]");
            mViewHolder.name.setText("名称："+mName.get(i));
            mViewHolder.introduce.setText("介绍："+mIntroduce.get(i));
            mViewHolder.id.setTextColor(0xff636e72);
            mViewHolder.name.setTextColor(0xff636e72);
            mViewHolder.introduce.setTextColor(0xff636e72);
            return view;
        }
    }

    static class ViewHolder {
        TextView id;
        TextView name;
        TextView introduce;
    }

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
        super.onStart();
        //设置
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        devType = mSharedPreferences.getString("device_type","");
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
        queryDataBase();
    }

}
