package com.qingniu.qnble.demo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qingniu.qnble.demo.R;
import com.qingniu.qnble.demo.bean.User;
import com.qingniu.qnble.demo.util.UserConst;
import com.qingniu.scale.config.DecoderAdapterManager;
import com.qingniu.scale.config.DecoderConfigAdapter;
import com.qingniu.scale.model.ScaleInfo;
import com.yolanda.health.qnblesdk.constant.QNIndicator;
import com.yolanda.health.qnblesdk.constant.QNScaleStatus;
import com.yolanda.health.qnblesdk.constant.UserGoal;
import com.yolanda.health.qnblesdk.constant.UserShape;
import com.yolanda.health.qnblesdk.listener.QNBleConnectionChangeListener;
import com.yolanda.health.qnblesdk.listener.QNDataListener;
import com.yolanda.health.qnblesdk.listener.QNResultCallback;
import com.yolanda.health.qnblesdk.out.QNBleApi;
import com.yolanda.health.qnblesdk.out.QNBleDevice;
import com.yolanda.health.qnblesdk.out.QNScaleData;
import com.yolanda.health.qnblesdk.out.QNScaleItemData;
import com.yolanda.health.qnblesdk.out.QNScaleStoreData;
import com.yolanda.health.qnblesdk.out.QNUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.qingniu.qnble.demo.R.id.connectBtn;

/**
 * author: yolanda-XY
 * date: 2018/3/23
 * package_name: com.qingniu.qnble.demo
 * description: ${设置用户信息界面}
 */

public class ConnectActivity extends AppCompatActivity implements View.OnClickListener {

    public static Intent getCallIntent(Context context, User user, QNBleDevice device) {
        return new Intent(context, ConnectActivity.class)
                .putExtra(UserConst.USER, user)
                .putExtra(UserConst.DEVICE, device);
    }

    @BindView(connectBtn)
    Button mConnectBtn;
    @BindView(R.id.statusTv)
    TextView mStatusTv;
    @BindView(R.id.weightTv)
    TextView mWeightTv;
    @BindView(R.id.back_tv)
    TextView mBackTv;
    @BindView(R.id.listView)
    ListView mListView;

    private QNBleDevice mBleDevice;
    private List<QNScaleItemData> mDatas = new ArrayList<>();
    private QNBleApi mQNBleApi;

    private User mUser;

    private boolean mIsConnected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        mQNBleApi = QNBleApi.getInstance(this);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initData() {
        initIntent();
        initBleConnectStatus();
        initUserData(); //设置数据监听器,返回数据,需在连接当前设备前设置
        //已经连接设备先断开设备,再连接
        if (mIsConnected) {
            doDisconnect();
        } else {
            connectQnDevice(mBleDevice); //连接当前设备
        }
    }

    private void initBleConnectStatus() {
        mQNBleApi.setBleConnectionChangeListener(new QNBleConnectionChangeListener() {
            //正在连接
            @Override
            public void onConnecting(QNBleDevice device) {
                setBleStatus(QNScaleStatus.STATE_CONNECTING);
            }

            //已连接
            @Override
            public void onConnected(QNBleDevice device) {
                setBleStatus(QNScaleStatus.STATE_CONNECTED);
            }

            @Override
            public void onServiceSearchComplete(QNBleDevice device) {

            }

            //正在断开连接，调用断开连接时，会马上回调
            @Override
            public void onDisconnecting(QNBleDevice device) {
                setBleStatus(QNScaleStatus.STATE_DISCONNECTING);
            }

            // 断开连接，断开连接后回调
            @Override
            public void onDisconnected(QNBleDevice device) {
                setBleStatus(QNScaleStatus.STATE_DISCONNECTED);
            }

            //出现了连接错误，错误码参考附表
            @Override
            public void onConnectError(QNBleDevice device, int errorCode) {
                Log.d("ConnectActivity", "onConnectError:" + errorCode);
                setBleStatus(QNScaleStatus.STATE_DISCONNECTED);
            }

            //测量过程中的连接状态
            @Override
            public void onScaleStateChange(QNBleDevice device, int status) {
                Log.d("ConnectActivity", "蓝牙状态是:" + status);
                setBleStatus(status);
            }
        });
    }

    private void connectQnDevice(QNBleDevice device) {
        mQNBleApi.connectDevice(device, createQNUser(), new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.d("ConnectActivity", "连接设备返回:" + msg);
            }
        });
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


    private void initUserData() {
        mQNBleApi.setDataListener(new QNDataListener() {
            @Override
            public void onGetUnsteadyWeight(QNBleDevice device, double weight) {
                Log.d("ConnectActivity", "体重是:" + weight);
                mWeightTv.setText(initWeight(weight));

            }

            @Override
            public void onGetScaleData(QNBleDevice device, QNScaleData data) {
                Log.d("ConnectActivity", "收到测量数据");
                onReceiveScaleData(data);
                QNScaleItemData fatValue = data.getItem(QNIndicator.TYPE_SUBFAT);
                if (fatValue != null) {
                    String value = fatValue.getValue() + "";
                    Log.d("ConnectActivity", "收到皮下脂肪数据:" + value);
                }
//                Log.d("ConnectActivity", "收到体脂肪:"+data.getItem(QNIndicator.TYPE_BODYFAT).getValue());
            }

            @Override
            public void onGetStoredScale(QNBleDevice device, List<QNScaleStoreData> storedDataList) {
                Log.d("ConnectActivity", "收到存储数据");
                if (storedDataList != null && storedDataList.size() > 0) {
                    QNScaleStoreData data = storedDataList.get(0);
                    Log.d("ConnectActivity", "收到存储数据:" + data.getWeight());
                    QNUser qnUser = createQNUser();
                    data.setUser(qnUser);
                    QNScaleData qnScaleData = data.generateScaleData();
                    onReceiveScaleData(qnScaleData);
                }
            }

            @Override
            public void onGetElectric(QNBleDevice device, int electric) {
                String text = "收到电池电量百分比:" + electric;
                Log.d("ConnectActivity", text);
                Toast.makeText(ConnectActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            mBleDevice = intent.getParcelableExtra(UserConst.DEVICE);
            mUser = intent.getParcelableExtra(UserConst.USER);
        }
    }

    private String initWeight(double weight) {
        int unit = mQNBleApi.getConfig().getUnit();
        return mQNBleApi.convertWeightWithTargetUnit(weight, unit);
    }

    private void initView() {
        mConnectBtn.setOnClickListener(this);
        mBackTv.setOnClickListener(this);
        mListView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }


    private BaseAdapter listAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mDatas.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, null);
            }
            TextView indicateNameTv = (TextView) convertView.findViewById(R.id.indicate_nameTv);
            TextView indicateValueTv = (TextView) convertView.findViewById(R.id.indicate_valueTv);
            TextView indicateLevelTv = (TextView) convertView.findViewById(R.id.indicate_levelTv);
            QNScaleItemData itemData = mDatas.get(position);

            indicateNameTv.setText(itemData.getName());
            //sdk返回的数据单位一直不变，用户需要自己去转化为自己需要的单位数据
            //和重量有关的指标
            if (itemData.getType() == QNIndicator.TYPE_WEIGHT || itemData.getType() == QNIndicator.TYPE_BONE
                    || itemData.getType() == QNIndicator.TYPE_MUSCLE_MASS) {
                indicateValueTv.setText(initWeight(itemData.getValue()));
            } else {
                indicateValueTv.setText(String.valueOf(itemData.getValue()));
            }
            return convertView;
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        doDisconnect();
        mQNBleApi.setBleConnectionChangeListener(null);
        mQNBleApi.setDataListener(null);
    }


    private void onReceiveScaleData(QNScaleData md) {
        mDatas.clear();
        mDatas.addAll(md.getAllItem());
        listAdapter.notifyDataSetChanged();
    }

    private void setBleStatus(int bleStatus) {
        String stateString;
        String btnString;
        switch (bleStatus) {
            case QNScaleStatus.STATE_CONNECTING: {
                stateString = "正在连接";
                btnString = "断开连接";
                mIsConnected = true;
                break;
            }
            case QNScaleStatus.STATE_CONNECTED: {
                stateString = "已连接";
                btnString = "断开连接";
                mIsConnected = true;
                break;
            }
            case QNScaleStatus.STATE_DISCONNECTING: {
                stateString = "正在断开连接";
                btnString = "连接";
                mIsConnected = false;

                break;
            }
            case QNScaleStatus.STATE_LINK_LOSS: {
                stateString = "连接已断开";
                btnString = "连接";
                mIsConnected = false;

                break;
            }
            case QNScaleStatus.STATE_START_MEASURE: {
                stateString = "正在测量";
                btnString = "断开连接";
                break;
            }
            case QNScaleStatus.STATE_REAL_TIME: {
                stateString = "正在测量实时体重";
                btnString = "断开连接";
                break;
            }
            case QNScaleStatus.STATE_BODYFAT: {
                stateString = "正在测量阻抗";
                btnString = "断开连接";
                break;
            }
            case QNScaleStatus.STATE_HEART_RATE: {
                stateString = "正在测量心率";
                btnString = "断开连接";
                break;
            }
            case QNScaleStatus.STATE_MEASURE_COMPLETED: {
                stateString = "测量完成";
                btnString = "断开连接";
                break;
            }
            default: {
                stateString = "连接已断开";
                btnString = "连接";
                mIsConnected = false;
                break;
            }
        }
        mStatusTv.setText(stateString);
        mConnectBtn.setText(btnString);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connectBtn:
                if (mIsConnected) {
                    //已经连接,断开连接
                    this.doDisconnect();
                } else {
                    //断开连接,就开始连接
                    mDatas.clear();
                    listAdapter.notifyDataSetChanged();
                    this.doConnect();
                }
                break;
            case R.id.back_tv:
                finish();
                break;
        }
    }


    private void doConnect() {
        if (mBleDevice == null || mUser == null) {
            return;
        }
        mQNBleApi.connectDevice(mBleDevice, createQNUser(), new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.d("ConnectActivity", "连接设备返回:" + msg);
                if (code == 0) {
                    mIsConnected = true;
                }
            }
        });

    }

    private void doDisconnect() {
        mQNBleApi.disconnectDevice(mBleDevice, new QNResultCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.d("ConnectActivity", "断开连接设备返回:" + msg);
            }
        });
    }
}
