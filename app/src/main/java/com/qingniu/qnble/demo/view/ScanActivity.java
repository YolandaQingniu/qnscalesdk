package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.SettingActivity;
import com.qingniu.qnble.demo.bean.Config;
import com.qingniu.qnble.demo.bean.User;
import com.qingniu.qnble.demo.util.AndroidPermissionCenter;
import com.qingniu.qnble.demo.util.ToastMaker;
import com.qingniu.qnble.demo.util.UserConst;
import com.qingniu.qnble.utils.QNLogUtils;
import com.yolanda.health.qnblesdk.constant.CheckStatus;
import com.yolanda.health.qnblesdk.constant.QNIndicator;
import com.yolanda.health.qnblesdk.constant.UserGoal;
import com.yolanda.health.qnblesdk.constant.UserShape;
import com.yolanda.health.qnblesdk.listener.QNBleDeviceDiscoveryListener;
import com.yolanda.health.qnblesdk.listener.QNResultCallback;
import com.yolanda.health.qnblesdk.out.QNBleApi;
import com.yolanda.health.qnblesdk.out.QNBleDevice;
import com.yolanda.health.qnblesdk.out.QNConfig;
import com.yolanda.health.qnblesdk.out.QNShareData;
import com.yolanda.health.qnblesdk.out.QNUser;
import com.yolanda.health.qnblesdk.out.QNUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScanActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    @BindView(R.id.scan_measuring)
    TextView mScanMeasuring;
    @BindView(R.id.scan_setting)
    TextView mScanSetting;
    @BindView(R.id.scan_appid)
    TextView mScanAppid;
    @BindView(R.id.scanBtn)
    Button mScanBtn;
    @BindView(R.id.stopBtn)
    Button mStopBtn;
    @BindView(R.id.scan_measuring_info)
    TextView mScanMeasuringInfo;
    @BindView(R.id.listView)
    ListView mListView;

    @BindView(R.id.qr_data_et)
    EditText qr_data_et;
    @BindView(R.id.qr_time_et)
    EditText qr_time_et;
    @BindView(R.id.qr_data_tv)
    TextView qr_data_tv;

    private QNBleApi mQNBleApi;
    private User mUser;
    private Config mConfig;
    private boolean isScanning;

    public static Intent getCallIntent(Context context, User user, Config mConfig) {
        return new Intent(context, ScanActivity.class)
                .putExtra(UserConst.CONFIG, mConfig)
                .putExtra(UserConst.USER, user);
    }

    private static final String TAG = "ScanActivity";

    private BaseAdapter listAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return devices.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, null);
            }
            TextView nameTv = (TextView) convertView.findViewById(R.id.nameTv);
            TextView modelTv = (TextView) convertView.findViewById(R.id.modelTv);
            TextView macTv = (TextView) convertView.findViewById(R.id.macTv);
            TextView rssiTv = (TextView) convertView.findViewById(R.id.rssiTv);

            QNBleDevice scanResult = devices.get(position);

            nameTv.setText(scanResult.getName());
            modelTv.setText(scanResult.getModeId());
            macTv.setText(scanResult.getMac());
            rssiTv.setText(String.valueOf(scanResult.getRssi()));


            return convertView;
        }
    };

    private List<QNBleDevice> devices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mQNBleApi = QNBleApi.getInstance(this);
        //动态申请权限(Android6.0以后需要)
//        if ()
        AndroidPermissionCenter.verifyPermissions(this);
        mUser = getIntent().getParcelableExtra(UserConst.USER);
        mConfig = getIntent().getParcelableExtra(UserConst.CONFIG);
        initData();

        mListView.setAdapter(this.listAdapter);

        mListView.setOnItemClickListener(this);

        mQNBleApi.setBleDeviceDiscoveryListener(new QNBleDeviceDiscoveryListener() {
            @Override
            public void onDeviceDiscover(QNBleDevice device) {
                devices.add(device);
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onStartScan() {
                QNLogUtils.log("ScanActivity", "onStartScan");
                isScanning = true;
            }

            @Override
            public void onStopScan() {
                QNLogUtils.log("ScanActivity", "onStopScan");
                isScanning = false;

            }

            @Override
            public void onScanFail(int code) {
                isScanning = false;
                QNLogUtils.log("ScanActivity", "onScanFail:" + code);
                Toast.makeText(ScanActivity.this, "扫描异常，请重启手机蓝牙!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initData() {
        mScanAppid.setText("UserId : " + mUser.getUserId());
        QNConfig mQnConfig = mQNBleApi.getConfig();//获取上次设置的对象,未设置获取的是默认对象
        mQnConfig.setAllowDuplicates(mConfig.isAllowDuplicates());
        mQnConfig.setDuration(mConfig.getDuration());
        mQnConfig.setScanOutTime(mConfig.getScanOutTime());
        mQnConfig.setConnectOutTime(mConfig.getConnectOutTime());
        mQnConfig.setUnit(mConfig.getUnit());
        mQnConfig.setOnlyScreenOn(mConfig.isOnlyScreenOn());
        //设置扫描对象
        mQnConfig.save(new QNResultCallback() {
            @Override
            public void onResult(int i, String s) {
                Log.d("ScanActivity", "initData:" + s);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void startScan() {
        mQNBleApi.startBleDeviceDiscovery(new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.d("ScanActivity", "code:" + code + ";msg:" + msg);
            }
        });
    }

    private void stopScan() {
        mQNBleApi.stopBleDeviceDiscovery(new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                if (code == CheckStatus.OK.getCode()) {
                    isScanning = false;
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position < 0 || position >= this.devices.size()) {
            return;
        }
        stopScan();
        QNBleDevice device = this.devices.get(position);
        //连接设备
        connectDevice(device);
    }

    private void connectDevice(QNBleDevice device) {
        startActivity(ConnectActivity.getCallIntent(this, mUser, device));
    }


    @OnClick({R.id.scan_setting, R.id.scanBtn, R.id.stopBtn, R.id.qr_test_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.scan_setting:
                startActivity(SettingActivity.getCallIntent(this));
                finish();
                break;
            case R.id.scanBtn:
                if (!isScanning) {
                    this.devices.clear();

                    listAdapter.notifyDataSetChanged();
                    startScan();
                } else {
                    ToastMaker.show(this, "正在扫描");
                }
                break;
            case R.id.stopBtn:
                if (isScanning) {
                    stopScan();
                } else {
                    ToastMaker.show(this, "已经停止扫描");
                }
                break;
            case R.id.qr_test_btn:
                String qrcode = qr_data_et.getText().toString().trim();
                long validSecond = -1L;
                try {
                    String qrValid = qr_time_et.getText().toString().trim();
                    validSecond = Long.parseLong(qrValid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (validSecond == -1) {
                    ToastMaker.show(this, "请输入正确的有效时间!");
                    return;
                }
                QNShareData qnShareData = QNUtils.decodeShareData(qrcode, validSecond, createQNUser(), new QNResultCallback() {
                    @Override
                    public void onResult(int code, String msg) {
                        QNLogUtils.log(TAG, "code:" + code);
                    }
                });
                String result = "解析失败";
                if (qnShareData != null) {
                    result = "qnShareData--sn：" + qnShareData.getSn() +
                            ";\nweight:" + qnShareData.getQNScaleData().getItemValue(QNIndicator.TYPE_WEIGHT) +
                            ";\nfat:" + qnShareData.getQNScaleData().getItemValue(QNIndicator.TYPE_BODYFAT);

                }
                qr_data_tv.setText(result);
                break;
        }
    }

    private QNUser createQNUser() {
        UserShape userShape;
        switch (mUser.getChoseShape()) {
            case 0:
                userShape = UserShape.SHAPE_NONE;
                break;
            case 1:
                userShape = UserShape.SHAPE_SLIM;
                break;
            case 2:
                userShape = UserShape.SHAPE_NORMAL;
                break;
            case 3:
                userShape = UserShape.SHAPE_STRONG;
                break;
            case 4:
                userShape = UserShape.SHAPE_PLIM;
                break;
            default:
                userShape = UserShape.SHAPE_NONE;
                break;
        }

        UserGoal userGoal;
        switch (mUser.getChoseGoal()) {
            case 0:
                userGoal = UserGoal.GOAL_NONE;
                break;
            case 1:
                userGoal = UserGoal.GOAL_LOSE_FAT;
                break;
            case 2:
                userGoal = UserGoal.GOAL_STAY_HEALTH;
                break;
            case 3:
                userGoal = UserGoal.GOAL_GAIN_MUSCLE;
                break;
            case 4:
                userGoal = UserGoal.POWER_OFTEN_EXERCISE;
                break;
            case 5:
                userGoal = UserGoal.POWER_LITTLE_EXERCISE;
                break;
            case 6:
                userGoal = UserGoal.POWER_OFTEN_RUN;
                break;
            default:
                userGoal = UserGoal.GOAL_NONE;
                break;
        }

        return mQNBleApi.buildUser(mUser.getUserId(),
                mUser.getHeight(), mUser.getGender(), mUser.getBirthDay(), mUser.getAthleteType(), userShape, userGoal, new QNResultCallback() {
                    @Override
                    public void onResult(int code, String msg) {
                        Log.d("ConnectActivity", "创建用户信息返回:" + msg);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AndroidPermissionCenter.REQUEST_EXTERNAL_STORAGE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "" + "权限" + permissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "" + "权限" + permissions[i] + "申请失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
