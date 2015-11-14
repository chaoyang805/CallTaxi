package com.chaoyang805.calltaxi.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.chaoyang805.calltaxi.R;
import com.chaoyang805.calltaxi.utils.LogHelper;
import com.chaoyang805.calltaxi.utils.ToastUtils;

/**
 * Created by chaoyang805 on 2015/11/8.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String SP_USER_NAME = "sp_name";

    public static final String SP_PHONE_NUMBER = "sp_phone_number";

    public static final String EXTRA_NAME = "extra_name";

    public static final String EXTRA_PHONE_NUMBER = "extra_phone_number";
    /**
     * //{"passengerName":"chaoyang805","location":{"lat":22.593371,"lng":114.279487}}
     handlePassengerUpdateLocation
      passengerName:chaoyang805 update_driver://{"driverName":"我是司机","location":{"lng":114.279458,"lat":22.593403}}
     //{"destination":{"detailAdress":"深圳宝安机场","destLat":22.630564784549,"destLng":113.82012299723381,"distance":47333.79882022791}}
     distance=4.0
     */
    private EditText mEtName;

    private EditText mEtPhoneNumber;

    private Button mBtnLogin;

    private SharedPreferences mSharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSharedPreferences = getSharedPreferences("CallTaxi", Context.MODE_PRIVATE);
        initViews();

        debug();

    }

    private void debug() {
        LatLng passenger = new LatLng(22.593371,114.279487);
        LatLng driver = new LatLng(22.593403,114.279458);
        LatLng destination = new LatLng(22.630564784549,113.82012299723381);
        double p2d = DistanceUtil.getDistance(passenger, driver);
        double p2dest = DistanceUtil.getDistance(passenger,destination);
        LogHelper.d("LoginActivity", "passenger to driver " + p2d + " passenger to destination " + p2dest);
    }

    private void initViews() {
        mEtName = (EditText) findViewById(R.id.et_user_name);
        mEtPhoneNumber = (EditText) findViewById(R.id.et_phone_number);

        String userName = mSharedPreferences.getString(SP_USER_NAME,"");
        String phoneNumber = mSharedPreferences.getString(SP_PHONE_NUMBER,"");
        if (!TextUtils.isEmpty(userName)){
            mEtName.setText(userName);
        }
        if (!TextUtils.isEmpty(phoneNumber)){
            mEtPhoneNumber.setText(phoneNumber);
        }
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        saveUserInfo();
        login();
        finish();
    }

    /**
     * 保存用户的信息到SharedPreferences
     */
    private void saveUserInfo() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(SP_USER_NAME, mEtName.getText().toString());
        editor.putString(SP_PHONE_NUMBER,mEtPhoneNumber.getText().toString());
        editor.commit();
    }

    private void login() {
        String name = mEtName.getText().toString();
        String phoneNumber = mEtPhoneNumber.getText().toString();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phoneNumber)) {
            ToastUtils.showToast(this, "请输入用户名和手机号！");
            return;
        }
        Intent intent = new Intent(this,MapActivity.class);
        intent.putExtra(EXTRA_NAME,name);
        intent.putExtra(EXTRA_PHONE_NUMBER,phoneNumber);
        startActivity(intent);
    }
}
