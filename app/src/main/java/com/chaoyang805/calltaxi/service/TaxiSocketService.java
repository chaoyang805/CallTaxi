package com.chaoyang805.calltaxi.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.chaoyang805.calltaxi.api.OnDriverUpdateCallback;
import com.chaoyang805.calltaxi.api.OnMessageReceivedListenerAdapter;
import com.chaoyang805.calltaxi.model.Destination;
import com.chaoyang805.calltaxi.model.Message;
import com.chaoyang805.calltaxi.socketclient.TaxiSocketClient;
import com.chaoyang805.calltaxi.ui.LoginActivity;
import com.chaoyang805.calltaxi.ui.WaitingActivity;
import com.chaoyang805.calltaxi.utils.LogHelper;

/**
 * Created by chaoyang805 on 2015/11/9.
 */
public class TaxiSocketService extends Service {
    private static final String TAG = LogHelper.makeLogTag(TaxiSocketService.class);

    public static final String EXTRA_DRIVER_NAME = "extra_driver_name";

    public static final String EXTRA_DRIVER_PHONE_NUMBER = "extra_driver_phone_number";

    private TaxiSocketClient mSocketClient;

    private TaxiSocketServiceBinder mBinder;

    private boolean isWaitingOrdered = false;

    private OnDriverUpdateCallback mCallback;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogHelper.d(TAG, "service onBind");
        mBinder = new TaxiSocketServiceBinder();
        String name = intent.getStringExtra(LoginActivity.EXTRA_NAME);
        String phoneNumber = intent.getStringExtra(LoginActivity.EXTRA_PHONE_NUMBER);
        mSocketClient = new TaxiSocketClient(name,phoneNumber,new TaxiSocketClient.Callback() {
            @Override
            public void onTimeOut() {
                LogHelper.d(TAG, "onTimeOut");
            }
            @Override
            public void onConnected() {
                mBinder.passengerLogin();
                mSocketClient.getHandler().setOnMessageReceivedListener(new OnMessageReceivedListenerAdapter(){

                    @Override
                    public void onDriverAccept(String msg) {
                        super.onDriverAccept(msg);
                        LogHelper.d(TAG,"onDriverAccept");
                        Message message = new Message(msg);
                        Intent intent = new Intent(WaitingActivity.TaxiOrderReceiver.ACTION_DRIVER_ACCEPT);
                        intent.putExtra(EXTRA_DRIVER_NAME,message.getDriverName());
                        intent.putExtra(EXTRA_DRIVER_PHONE_NUMBER,message.getDriverPhoneNumber());
                        sendBroadcast(intent);
                    }
                    @Override
                    public void onUpdateDriver(String message) {
                        super.onUpdateDriver(message);
                        if (mCallback != null) {
                            mCallback.onUpdateDriver(message);
                        }
                    }

                    @Override
                    public void onDriverOffline(String message) {
                        if (mCallback != null){
                            mCallback.onDriverOffline(message);
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
        super.onDestroy();
        disconnect();
    }

    private void disconnect() {
        mSocketClient.disconnect();
    }

    public class TaxiSocketServiceBinder extends Binder {

        public void passengerLogin(){
            mSocketClient.passengerLogin();
        }

        public void setOnDriverUpdateCallback(OnDriverUpdateCallback callback) {
            mCallback = callback;
        }

        public void removeCallback(){
            mCallback = null;
        }

        public void updateLocation(double[] location){
            mSocketClient.updateLocation(location);
        }

        public void callTaxi(Destination destination) {
            isWaitingOrdered = true;
            mSocketClient.callTaxi(destination);
        }

        public void cancelCall() {
            if (isWaitingOrdered) {
                isWaitingOrdered = false;
                mSocketClient.cancelCall();
            }
        }
    }
}
