package com.chaoyang805.driver.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.chaoyang805.driver.api.DriverServiceCallback;
import com.chaoyang805.driver.api.OnMessageReceivedListenerAdapter;
import com.chaoyang805.driver.model.Passenger;
import com.chaoyang805.driver.socketclient.DriverSocketClient;
import com.chaoyang805.driver.ui.LoginActivity;
import com.chaoyang805.driver.utils.LogHelper;

/**
 * Created by chaoyang805 on 2015/11/9.
 */
public class DriverService extends Service {
    private static final String TAG = LogHelper.makeLogTag(DriverService.class);

    public static final String EXTRA_DRIVER_NAME = "extra_driver_name";

    public static final String EXTRA_DRIVER_PHONE_NUMBER = "extra_driver_phone_number";

    private DriverSocketClient mSocketClient;

    private DriverServiceBinder mBinder;


    private DriverServiceCallback mCallback;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogHelper.d(TAG, "service onBind");
        mBinder = new DriverServiceBinder();
        String name = intent.getStringExtra(LoginActivity.EXTRA_NAME);
        String phoneNumber = intent.getStringExtra(LoginActivity.EXTRA_PHONE_NUMBER);
        mSocketClient = new DriverSocketClient(name,phoneNumber,new DriverSocketClient.Callback() {
            @Override
            public void onTimeOut() {
                LogHelper.d(TAG, "onTimeOut");
            }
            @Override
            public void onConnected() {
                mBinder.passengerLogin();
                mSocketClient.getHandler().setOnMessageReceivedListener(new OnMessageReceivedListenerAdapter(){
                    @Override
                    public void onUpdatePassenger(String message) {
                        if (mCallback != null) {
                            mCallback.onUpdatePassenger(message);
                        }
                    }

                    @Override
                    public void onPassengerTaken(String message) {
                        if (mCallback != null) {
                            mCallback.onPassengerTaken(message);
                        }
                    }

                    @Override
                    public void onPassengerCancel(String message) {
                        if (mCallback != null) {
                            mCallback.onPassengerCancel(message);
                        }
                    }

                    @Override
                    public void onPassengerOffline(String message) {
                        if (mCallback != null){
                            mCallback.onPassengerOffline(message);
                        }
                    }
                });
            }
        });
        mSocketClient.init();
        return mBinder;
    }

    @Override
    public void onDestroy() {
        disconnect();
        super.onDestroy();
    }

    private void disconnect() {
        mSocketClient.disconnect();
    }

    public class DriverServiceBinder extends Binder {

        public void passengerLogin(){
            mSocketClient.driverLogin();
        }

        public void updateLocation(double[] location){
            mSocketClient.updateLocation(location);
        }

        public void driverAccept(Passenger passenger,double[] location){
            mSocketClient.driverAccept(passenger,location);
        }

        public void addCallback(DriverServiceCallback callback) {
            mCallback = callback;
        }

    }
}
