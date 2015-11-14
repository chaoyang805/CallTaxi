package com.chaoyang805.driver.ui;

import android.content.ComponentName;
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

                    @Override
                    public void onPassengerTaken(String msg) {
                        Message message = new Message(msg);
                        final Passenger passenger = new Passenger(message.getPassengerName(), message.getPassengerPhoneNumber());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.passengerTaken(passenger);
                            }
                        });
                    }

                    @Override
                    public void onPassengerCancel(String msg) {
                        Message message = new Message(msg);
                        final Passenger passenger = new Passenger(message.getPassengerName(), message.getPassengerPhoneNumber());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.passengerCancel(passenger);
                            }
                        });
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
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mLocationManager.stop();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //TODO show alert dialog
    }
}
