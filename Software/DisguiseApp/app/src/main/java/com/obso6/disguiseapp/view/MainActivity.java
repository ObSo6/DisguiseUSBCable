package com.obso6.disguiseapp.view;

import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_CLASSIC;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_DUAL;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_LE;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.obso6.disguiseapp.R;
import com.obso6.disguiseapp.config.DatabaseHelp;
import java.util.ArrayList;

//蓝牙扫描
public class MainActivity extends AppCompatActivity {
    //按钮
    private ImageView refresh;
    //加载框
    private ProgressBar mProgressBar;
    //默认扫描20s
    private String scanTime = "20000";
    //扫描标志位
    private boolean scanFlag = false;
    //蓝牙设备适配器列表
    private LeDeviceListAdapter mLeDeviceListAdapter;
    //蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter mBtAdapter;
    //Handler
    private Handler mHandler;
    //设置
    private SharedPreferences mSharedPreferences;
    //自动连接
    private String macAddress;

    //onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        //初始化操作
        initView();
        //获取权限
        getPermits();
        //初始化蓝牙
        initBluetooth();
        //初始化数据库
        initDatabase();
        //设置页面
        getConfig();
    }

    //后期做配置相关的
    private void getConfig() {
        //自动连接
        boolean autoConnect = mSharedPreferences.getBoolean("auto_connect", false);
        macAddress = mSharedPreferences.getString("mac_address","");
        //自动连接
        //48:87:2D:64:FB:DD
        if(autoConnect && macAddress.length()>0){
            //弹窗连接
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("模式");
            builder.setMessage("请选择模式！");
            //设置一个NegativeButton
            builder.setPositiveButton("HID攻击", (dialog, which) -> {
                //点击结果
                Intent intent = new Intent(MainActivity.this, HidActivity.class);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("device_uuid", macAddress);
                editor.putString("device_type", "[BLE]");
                editor.commit();
                startActivity(intent);
            });
            builder.setNegativeButton("键盘模拟", (dialog, which) -> {
                //点击结果
                Intent intent = new Intent(MainActivity.this, KeyActivity.class);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("device_uuid", macAddress);
                editor.putString("device_type", "[BLE]");
                editor.commit();
                startActivity(intent);
            });
            //    显示出该对话框
            builder.show();
        }
        //扫描时间自定义,默认20000
        scanTime = mSharedPreferences.getString("scan_time","20000");
    }

    //初始化数据库
    private void initDatabase() {
        //数据库实例
        DatabaseHelp mDatabaseHelp = new DatabaseHelp(getApplicationContext());
        SQLiteDatabase readableDatabase = mDatabaseHelp.getReadableDatabase();
    }

    //打开蓝牙弹窗回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            //自动扫描
            scanLeDevice(true);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //弹窗打开蓝牙
    private void initBluetooth() {
        //蓝牙硬件是否存在
        if (mBluetoothAdapter != null) {
            //蓝牙是否打开
            if (!mBluetoothAdapter.isEnabled()) {
                //弹窗问是否打开
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                            5);
                }
                startActivityForResult(intent, 0);
            }
        } else {
            toast(R.string.no_bluetooth_devices);
            //没有蓝牙硬件退出APP
            finish();
        }
    }

    //权限申请的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            //没有获取到权限，提示用户
            case 1: // If request is cancelled, the result arrays are empty.
            case 2:
                toast(R.string.no_local_permits);
            default:
                //检查蓝牙是否打开
                new Handler().postDelayed(() -> {
                    if (!mBluetoothAdapter.isEnabled()) {
                        toast(R.string.no_bluetooth_enable);
                    }
                }, 3000);
                break;
        }
    }

    //蓝牙和地理位置授权
    private void getPermits() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    2);
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED) {
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                    3);
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                    4);
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    5);
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    6);
        }
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!(gps || network)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("提示");
            builder.setMessage("蓝牙扫描需要打开位置定位！");
            //设置一个NegativeButton
            builder.setPositiveButton("跳转", (dialog, which) -> {
                //点击结果
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            });
            builder.setNegativeButton("取消", (dialog, which) -> {
                //点击结果

            });
            //    显示出该对话框
            builder.show();
        }
    }

    //发现设备
    private void doDiscovery() {
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        mBtAdapter.startDiscovery();
    }

    //扫描蓝牙设备
    @SuppressLint("NewApi")
    private void scanLeDevice(boolean enable) {
        //清楚上次扫描线程
        mHandler.removeCallbacks(mStopScan);
        if (enable) {
            //发现设备
            doDiscovery();
            //扫描然后自动停止
            mHandler.postDelayed(mStopScan, Long.parseLong(scanTime));
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            //加载动画
            mProgressBar.setVisibility(View.VISIBLE);
            refresh.setVisibility(View.GONE);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mProgressBar.setVisibility(View.GONE);
            refresh.setVisibility(View.VISIBLE);
        }
        //更新扫描标志
        scanFlag = enable;
    }

    //停止扫描
    final Runnable mStopScan = () -> {
        //停止扫描
        scanLeDevice(false);
    };

    //扫描回调
    @SuppressLint("NewApi")
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = (device, rssi, scanRecord) -> runOnUiThread(() -> {
        final String deviceName = device.getName();
        if ((device != null) && (deviceName != null && deviceName.length() > 0)) {
            System.out.println(deviceName + "\n" + device.getAddress() + "\n" + rssi);
        }
    });

    //初始化操作
    private void initView() {
        //Handler
        mHandler = new Handler();
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        //确保支持蓝牙设备
        if (mBluetoothAdapter == null) {
            toast(R.string.no_bluetooth_devices);
            finish();
        }
        //确保支持蓝牙BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toast(R.string.no_ble_devices);
            finish();
        }
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        //加载框
        mProgressBar = findViewById(R.id.title_bar).findViewById(R.id.progress);
        mProgressBar.setVisibility(View.GONE);
        //注册广播-发现设备
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        //注册广播-扫描完成
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        //绑定控件
        //标题
        TextView title = findViewById(R.id.title_bar).findViewById(R.id.title);
        refresh = findViewById(R.id.title_bar).findViewById(R.id.refresh);
        ImageView stop = findViewById(R.id.title_bar).findViewById(R.id.stop);
        stop.setVisibility(View.GONE);
        //点击扫描按钮
        refresh.setOnClickListener(view -> {
            //clear the adapter
            mLeDeviceListAdapter.clear();
            mLeDeviceListAdapter.notifyDataSetChanged();
            //开始扫描
            if (!scanFlag) {
                scanLeDevice(true);
            }
        });
        //设置按钮
        ImageView setting = findViewById(R.id.title_bar).findViewById(R.id.setting);
        setting.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        });
        //扫描结果列表
        ListView scanList = findViewById(R.id.scan_bar).findViewById(R.id.scan_list);
        scanList.setOnItemClickListener(new ItemClickEvent());
        //set the adapter
        scanList.setAdapter(mLeDeviceListAdapter);
        //设置
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this) ;
        //更新UI
        title.setText("主 页");

    }

    //广播事件
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //当发现设备时
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //从Intent得到BluetoothDevice对象
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                String deviceName = device.getName();
                if ((device != null) && (deviceName != null && deviceName.length() > 0)) {
                    mLeDeviceListAdapter.addDevice(rssi, device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
                //发现完成时
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                //发现不到设备
                if (mLeDeviceListAdapter.getCount() == 0) {
                    //权限
                }
            }
        }
    };

    //列表点击
    private class ItemClickEvent implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> l, View v, int position, long id) {

            BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
            if (device == null) return;
            String type = mLeDeviceListAdapter.getDeviceType(position);
            //弹窗选择跳转
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("模式");
            builder.setMessage("请选择模式！");
            //设置一个NegativeButton
            builder.setPositiveButton("HID攻击", (dialog, which) -> {
                //点击结果
                Intent intent = new Intent(MainActivity.this, HidActivity.class);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("device_uuid", String.valueOf(device));
                editor.putString("device_type", type);
                editor.commit();
                startActivity(intent);
            });
            builder.setNegativeButton("键盘模拟", (dialog, which) -> {
                //点击结果
                Intent intent = new Intent(MainActivity.this, KeyActivity.class);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("device_uuid", String.valueOf(device));
                editor.putString("device_type", type);
                editor.commit();
                startActivity(intent);
            });
            //    显示出该对话框
            builder.show();
        }
    }

    private class LeDeviceListAdapter extends BaseAdapter {
        private final ArrayList<BluetoothDevice> mLeDevices;
        private final ArrayList<Integer> mRssi;
        private final ArrayList<String> type;
        private final LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            mRssi = new ArrayList<>();
            type = new ArrayList<>();
            mInflator = MainActivity.this.getLayoutInflater();
        }

        public void addDevice(int rssi, BluetoothDevice device) {
            String mType;
            boolean isduab = false;
            switch (device.getType()) {
                case DEVICE_TYPE_CLASSIC:
                    mType = "[SPP]";
                    break;
                case DEVICE_TYPE_LE:
                    mType = "[BLE]";
                    break;
                case DEVICE_TYPE_DUAL:
                    mType = "[BLE]";
                    isduab = true;
                    break;
                default:
                    mType = "[UNKNOWN]";
                    break;
            }
            if (!mLeDevices.contains(device)) {
                if (isduab) {
                    mLeDevices.add(device);
                    type.add("[SPP]");
                    mRssi.add(rssi);
                    mLeDevices.add(device);
                    type.add("[BLE]");
                } else {
                    mLeDevices.add(device);
                    type.add(mType);
                }
                mRssi.add(rssi);
            } else {
                //给定Rssi信号
                for (int i = 0; i < mLeDevices.size(); i++) {
                    if (mLeDevices.get(i).equals(device)) {
                        mRssi.set(i, rssi);
                    }
                }
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public String getDeviceType(int pos) {
            return type.get(pos);
        }

        public void clear() {
            mLeDevices.clear();
            mRssi.clear();
            type.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
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
                view = mInflator.inflate(R.layout.devices_listitem, null);
                mViewHolder = new ViewHolder();
                mViewHolder.deviceAddress = (TextView) view.findViewById(R.id.address);
                mViewHolder.deviceName = (TextView) view.findViewById(R.id.name);
                mViewHolder.deviceRssi = (TextView) view.findViewById(R.id.rssi);
                mViewHolder.backG = (View) view.findViewById(R.id.background);
                view.setTag(mViewHolder);
            } else {
                mViewHolder = (ViewHolder) view.getTag();
            }

            // get the device information
            BluetoothDevice device = mLeDevices.get(i);
            // set the list item show information
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0){
                mViewHolder.deviceName.setText(getName(device.getAddress(), deviceName));
            }else {
                mViewHolder.deviceName.setText(getName(device.getAddress(), "unknown device"));
            }
            mViewHolder.deviceAddress.setText(device.getAddress());
            mViewHolder.deviceRssi.setText(type.get(i)+" RSSI:" + mRssi.get(i));
            mViewHolder.deviceRssi.setTextColor(0xff636e72);
            mViewHolder.deviceAddress.setTextColor(0xff636e72);
            mViewHolder.deviceName.setTextColor(0xff636e72);
            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRssi;
        View backG;
    }

    static String getName(String add, String dname) {
        return dname;
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
                finish();
            }
            return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }
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

    @Override
    protected void onPause() {
        //Activity被覆盖，停止扫描
        if (scanFlag) {
            scanLeDevice(false);
        }
        //清除
        mLeDeviceListAdapter.clear();
        mLeDeviceListAdapter.notifyDataSetChanged();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止发现
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        //卸载广播
        this.unregisterReceiver(mReceiver);
    }
}