package com.chaoyang805.calltaxi.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.chaoyang805.calltaxi.R;
import com.chaoyang805.calltaxi.service.TaxiSocketService;

/**
 * Created by chaoyang805 on 2015/11/12.
 */
public class WaitingActivity extends AppCompatActivity {

    private ServiceConnection mServiceConnection;

    private TextView mTvWaiting;
    private TextView mTvOrdered;
    private TaxiSocketService.TaxiSocketServiceBinder mBinder = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);
        initViews();
        bindService();
    }

    private void initViews() {
        mTvWaiting = (TextView) findViewById(R.id.tv_waiting);
        mTvOrdered = (TextView) findViewById(R.id.tv_ordered);
        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinder != null) {
                    mBinder.cancelCall();
                    finish();
                }
            }
        });
    }

    private void bindService() {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinder = (TaxiSocketService.TaxiSocketServiceBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBinder = null;
            }
        };
        Intent intent = new Intent(this, TaxiSocketService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        //TODO 显示对话框 是否取消约车
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mServiceConnection = null;
    }

    public class TaxiOrderReceiver extends BroadcastReceiver{

        public static final String ACTION_DRIVER_ACCEPT = "driver_accept";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_DRIVER_ACCEPT)) {
                String driverName = intent.getStringExtra(TaxiSocketService.EXTRA_DRIVER_NAME);
                final String driverPhoneNumber = intent.getStringExtra(TaxiSocketService.EXTRA_DRIVER_PHONE_NUMBER);
                mTvWaiting.setVisibility(View.GONE);
                mTvOrdered.setVisibility(View.VISIBLE);
                mTvOrdered.setText(getString(R.string.ordered, driverName, driverPhoneNumber));
                mTvOrdered.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent dialIntent = new Intent(Intent.ACTION_VIEW);
                        dialIntent.setData(Uri.parse("tel:"+driverPhoneNumber));
                        startActivity(dialIntent);
                    }
                });
            }
        }
    }
}
