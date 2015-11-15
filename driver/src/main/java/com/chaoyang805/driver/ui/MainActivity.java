package com.chaoyang805.driver.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.chaoyang805.driver.R;
import com.chaoyang805.driver.adapter.PassengerInfoAdapter;
import com.chaoyang805.driver.api.DriverServiceCallback;
import com.chaoyang805.driver.api.OnLocationReceivedListener;
import com.chaoyang805.driver.baidulocation.LocationManager;
import com.chaoyang805.driver.model.Message;
import com.chaoyang805.driver.model.Passenger;
import com.chaoyang805.driver.service.DriverService;
import com.chaoyang805.driver.utils.LogHelper;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = LogHelper.makeLogTag(MainActivity.class);
    private static final int REQUEST_TAKE_ORDER = 0x1000;

    private ServiceConnection mServiceConnection;

    private DriverService.DriverServiceBinder mBinder;

    private double[] mCurrentLocation = new double[2];

    private LocationManager mLocationManager;

    private ListView mLvPassengerInfo;

    private PassengerInfoAdapter mAdapter;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initLocationManager();
        prepareService();
    }

    private void prepareService() {
        Intent service = new Intent(MainActivity.this, DriverService.class);
        String name = getIntent().getStringExtra(LoginActivity.EXTRA_NAME);
        String phoneNumber = getIntent().getStringExtra(LoginActivity.EXTRA_PHONE_NUMBER);
        service.putExtra(LoginActivity.EXTRA_NAME, name);
        service.putExtra(LoginActivity.EXTRA_PHONE_NUMBER, phoneNumber);

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogHelper.d(TAG, "onServiceConnected");
                mBinder = (DriverService.DriverServiceBinder) service;
                DriverServiceCallback callback = new DriverServiceCallback() {
                    @Override
                    public void onUpdatePassenger(String msg) {
                        updatePassenger(msg);
                    }

                    @Override
                    public void onPassengerTaken(String msg) {
                        passengerTaken(msg);
                    }

                    @Override
                    public void onPassengerCancel(String msg) {
                        passengerCancel(msg);
                    }

                    @Override
                    public void onPassengerOffline(String msg) {
                        passengerOffline(msg);
                    }
                };
                mBinder.addCallback(callback);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                LogHelper.d(TAG, "onServiceDisconnected");
            }
        };
        bindService(service, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void passengerOffline(String msg) {
        Message message = new Message(msg);
        final Passenger passenger = new Passenger(message.getPassengerName(), message.getPassengerPhoneNumber());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.removePassenger(passenger);
            }
        });

    }

    private void passengerCancel(String msg) {
        Message message = new Message(msg);
        final Passenger passenger = new Passenger(message.getPassengerName(), message.getPassengerPhoneNumber());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.removePassenger(passenger);
            }
        });
    }

    private void passengerTaken(String msg) {
        Message message = new Message(msg);
        final Passenger passenger = new Passenger(message.getPassengerName(), message.getPassengerPhoneNumber());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.removePassenger(passenger);
            }
        });
    }

    private void updatePassenger(String msg) {
        Message message = new Message(msg);
        final Passenger passenger = new Passenger(message.getPassengerName(), message.getPassengerPhoneNumber());
        passenger.setDestination(message.getDestination());
        passenger.setLocation(message.getLocation());
        LatLng passengerLocation = new LatLng(passenger.getLocation()[0], passenger.getLocation()[1]);
        //乘客和司机间的距离
        int distance = (int) DistanceUtil.getDistance(passengerLocation, new LatLng(mCurrentLocation[0], mCurrentLocation[1]));

        LogHelper.d(TAG, "distance = " + distance + "passengerLoc:" + passenger.getLocation()[0] + " " +
                passenger.getLocation()[1] + " driverLoc :" + mCurrentLocation[0] + " " + mCurrentLocation[1]);
        passenger.setDistance(distance);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.updatePassenger(passenger);
            }
        });
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mLvPassengerInfo = (ListView) findViewById(R.id.lv_passengers);
        mAdapter = new PassengerInfoAdapter(this);
        mLvPassengerInfo.setOnItemClickListener(this);
        mLvPassengerInfo.setAdapter(mAdapter);
    }

    private void initLocationManager() {
        mLocationManager = new LocationManager(this);
        mLocationManager.init(new OnLocationReceivedListener() {
            @Override
            public void onLocationReceived(BDLocation bdLocation) {
                LogHelper.d(TAG, "onLocationReceived");
                mCurrentLocation[0] = bdLocation.getLatitude();
                mCurrentLocation[1] = bdLocation.getLongitude();
                if (mBinder != null) {
                    mBinder.updateLocation(mCurrentLocation);
                }
            }
        });
        mLocationManager.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogHelper.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        mLocationManager.stop();
        super.onDestroy();
        LogHelper.d(TAG, "onDestory");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Passenger passenger = mAdapter.getItem(position);
        final String name = passenger.getName();
        final String phoneNumber = passenger.getPhoneNumber();
        final String adress = passenger.getDestination().getDetailAdress();
        final int destDistance = (int) passenger.getDestination().getDistance() / 1000;
        final int distance = passenger.getDistance();
        new AlertDialog.Builder(this)
                .setTitle("是否接单？")
                .setMessage(String.format("乘客：%s\n手机号：%s\n目的地：%s %d\n",
                        name, phoneNumber, adress, destDistance))
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBinder.driverAccept(passenger, mCurrentLocation);
                        Intent intent = new Intent(MainActivity.this, PassengerInfoActivity.class);
                        intent.putExtra(LoginActivity.EXTRA_NAME, name);
                        intent.putExtra(LoginActivity.EXTRA_PHONE_NUMBER, phoneNumber);
                        intent.putExtra(LoginActivity.EXTRA_ADRESS, adress);
                        intent.putExtra(LoginActivity.EXTRA_DEST_DISTANCE, destDistance);
                        intent.putExtra(LoginActivity.EXTRA_DISTANCE, distance);
                        startActivityForResult(intent, REQUEST_TAKE_ORDER);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_TAKE_ORDER && resultCode == Activity.RESULT_OK){

        }
    }
}
