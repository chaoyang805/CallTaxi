package com.chaoyang805.calltaxi.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.baidu.location.BDLocation;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.chaoyang805.calltaxi.R;
import com.chaoyang805.calltaxi.api.OnDriverUpdateCallback;
import com.chaoyang805.calltaxi.map.BaiduMapManager;
import com.chaoyang805.calltaxi.model.Destination;
import com.chaoyang805.calltaxi.model.Driver;
import com.chaoyang805.calltaxi.model.Message;
import com.chaoyang805.calltaxi.service.TaxiSocketService;
import com.chaoyang805.calltaxi.utils.LogHelper;
import com.chaoyang805.calltaxi.utils.ToastUtils;

import java.util.List;

public class MapActivity extends AppCompatActivity
        implements BaiduMapManager.OnLocationUpdateListener, View.OnClickListener {
    private static final String TAG = LogHelper.makeLogTag(MapActivity.class);

    private static final int REQUEST_GET_DEST = 0x1001;
    private static final int REQUEST_WAITING_ORDER = 0x1002;

    public static final String EXTRA_CITY = "extra_city";
    public static final String EXTRA_DEST = "destination_key";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";

    protected MapView mMapView;

    protected BaiduMapManager mMapManager;

    protected Button mBtnDestInPut;

    protected Button mBtnCallTaxi;

    private String mCity;


    private ServiceConnection mSocketServiceConnection;

    private TaxiSocketService.TaxiSocketServiceBinder mServiceBinder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidu_map);
        initViews();
        initBaiduMap();
        prepareService();
    }

    private void prepareService() {
        final String name = getIntent().getStringExtra(LoginActivity.EXTRA_NAME);
        final String phoneNumber = getIntent().getStringExtra(LoginActivity.EXTRA_PHONE_NUMBER);
        mSocketServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogHelper.d(TAG, "onServiceConnceted");
                OnDriverUpdateCallback callback = new OnDriverUpdateCallback() {
                    @Override
                    public void onUpdateDriver(String msg) {
                        updateDriver(msg);
                    }

                    @Override
                    public void onDriverOffline(String message) {
                        driverOffline(message);
                    }
                };
                mServiceBinder = (TaxiSocketService.TaxiSocketServiceBinder) service;
                mServiceBinder.setOnDriverUpdateCallback(callback);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServiceBinder.removeCallback();
                mServiceBinder = null;
            }
        };
        Intent intent = new Intent(this,TaxiSocketService.class);
        intent.putExtra(LoginActivity.EXTRA_NAME, name);
        intent.putExtra(LoginActivity.EXTRA_PHONE_NUMBER, phoneNumber);
        bindService(intent, mSocketServiceConnection, BIND_AUTO_CREATE);
    }

    private void driverOffline(String msg) {
        Message message = new Message(msg);
        mMapManager.removeDriver(message.getDriverPhoneNumber());
    }

    private void updateDriver(String msg) {
        Message message = new Message(msg);
        Driver driver = new Driver(message.getDriverName(), message.getDriverPhoneNumber());
        driver.setLocation(message.getLocation());
        mMapManager.showDriversOnMap(driver);
    }

    private void initBaiduMap() {
        mMapManager = new BaiduMapManager(this, mMapView);
        mMapManager.registerLocationListener(this);
    }

    private void initViews() {
        mMapView = (MapView) findViewById(R.id.bMapView);
        mBtnDestInPut = (Button) findViewById(R.id.btn_dest_input);
        mBtnCallTaxi = (Button) findViewById(R.id.btn_call_taxi);

        mBtnDestInPut.setOnClickListener(this);
        mBtnCallTaxi.setOnClickListener(this);
    }

    private double mCurrentLat = -1;
    private double mCurrentLng = -1;
    @Override
    public void onLocationUpdate(BDLocation bdLocation) {
        mCity = bdLocation.getCity();
        List<Poi> poiList = bdLocation.getPoiList();
        Poi poi = poiList.get(0);

        mCurrentLat = bdLocation.getLatitude();
        mCurrentLng = bdLocation.getLongitude();
        if (mServiceBinder != null) {
            LogHelper.d(TAG, "updateLocation");
            mServiceBinder.updateLocation(new double[]{mCurrentLat, mCurrentLng});
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapManager.onResume();
        mMapManager.init();
        mMapManager.requestLocation();
        LogHelper.d(TAG, "requestLocation");
    }

    @Override
    protected void onDestroy() {
        unbindService(mSocketServiceConnection);
        mMapManager.onDestroy();
        mSocketServiceConnection = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dest_input:
                Intent intent = new Intent(this, DestSearchActivity.class);
                if (mCity != null) {
                    intent.putExtra(EXTRA_CITY,mCity);
                }
                startActivityForResult(intent, REQUEST_GET_DEST);
                break;
            case R.id.btn_call_taxi:
                if (mCurrentLat <= 0 || mCurrentLng <= 0) {
                    ToastUtils.showToast(this, "等待定位...");
                    return;
                }
                if (mDestAdress == null ||
                        TextUtils.isEmpty(mDestAdress) ||
                        mDestLocation[0] <= 0 ||
                        mDestLocation[1] <= 0){
                    ToastUtils.showToast(this,"请先选择目的地");
                    return;
                }
                Destination destination = new Destination();
                destination.setDetailAdress(mDestAdress);
                destination.setLocation(mDestLocation);
                double distance = DistanceUtil.getDistance(new LatLng(mCurrentLat, mCurrentLng),
                        new LatLng(mDestLocation[0], mDestLocation[1]));
                destination.setDistance(distance);
                mServiceBinder.callTaxi(destination);
                startActivityForResult(new Intent(MapActivity.this, WaitingActivity.class), REQUEST_WAITING_ORDER);
                break;
        }
    }

    private double[] mDestLocation = new double[2];
    private String mDestAdress = null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GET_DEST:
                    if (data != null) {
                        mDestAdress = data.getStringExtra(EXTRA_DEST);
                        mBtnDestInPut.setText("目的地：" + mDestAdress);
                        mDestLocation[0] = data.getDoubleExtra(EXTRA_LATITUDE, -1);
                        mDestLocation[1] = data.getDoubleExtra(EXTRA_LONGITUDE, -1);
                    }
                    break;
                case REQUEST_WAITING_ORDER:
                    if (mServiceBinder != null) {
                        LogHelper.d(TAG, "passenger login again");
                        mServiceBinder.passengerLogin();
                    }
                    break;
            }

        }
    }

}
