package com.chaoyang805.driver.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.chaoyang805.driver.R;

/**
 * Created by chaoyang805 on 2015/11/15.
 */
public class PassengerInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTvPassengerInfo;
    private String mPassengerPhoneNumber;
    private AlertDialog mCompleteDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_info);
        initViews();
    }

    private void initViews() {
        mTvPassengerInfo = (TextView) findViewById(R.id.tv_passenger_info);
        Intent intent = getIntent();
        String name = intent.getStringExtra(LoginActivity.EXTRA_NAME);
        mPassengerPhoneNumber = intent.getStringExtra(LoginActivity.EXTRA_PHONE_NUMBER);
        String adress = intent.getStringExtra(LoginActivity.EXTRA_ADRESS);
        int destDistance = intent.getIntExtra(LoginActivity.EXTRA_DEST_DISTANCE, -1);
        int distance = intent.getIntExtra(LoginActivity.EXTRA_DISTANCE, -1);

        mTvPassengerInfo.setText(getString(R.string.passenger_info, name, mPassengerPhoneNumber, adress, destDistance, distance));
        findViewById(R.id.btn_call_passenger).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        callPassenger();
    }

    private void callPassenger() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("tel:"+getIntent().getStringExtra(LoginActivity.EXTRA_PHONE_NUMBER)));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (mCompleteDialog == null) {
            createDoneDialog();
        } else {
            if (mCompleteDialog.isShowing()) {
                mCompleteDialog.dismiss();
            }else {
                mCompleteDialog.show();
            }
        }
    }

    public void btnHandlerComplete(View view) {
        if (mCompleteDialog == null) {
            createDoneDialog();
        } else {
            if (mCompleteDialog.isShowing()){
                mCompleteDialog.dismiss();
            }else {
                mCompleteDialog.show();
            }
        }
    }
    private void createDoneDialog() {
        mCompleteDialog = new AlertDialog.Builder(this)
                .setTitle("将离开本页面")
                .setMessage("您确定已经记下了乘客的信息？\n手机：" + mPassengerPhoneNumber)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                })
                .show();
    }
}
