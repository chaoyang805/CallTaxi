package com.chaoyang805.driver.baidulocation;

import android.content.Context;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.chaoyang805.driver.api.OnLocationReceivedListener;

/**
 * Created by chaoyang805 on 2015/11/13.
 */
public class LocationManager {

    private LocationClient mLocationClient;

    private int mScanSpanInMillis = 3000;

    private DriverLocationListener mLocationListener;

    public LocationManager(Context context){
        mLocationClient = new LocationClient(context);
    }

    public void init(OnLocationReceivedListener listener) {
        LocationClientOption option = new LocationClientOption();
        mListener = listener;
        option.setScanSpan(mScanSpanInMillis);
        option.setNeedDeviceDirect(true);
        option.setOpenGps(true);
        option.setEnableSimulateGps(true);
        option.setIsNeedAddress(true);
        option.setCoorType("bd09ll");
        mLocationClient.setLocOption(option);
        mLocationListener = new DriverLocationListener();
        mLocationClient.registerLocationListener(mLocationListener);
    }

    public void start() {
        mLocationClient.start();
    }

    public void stop(){
        mLocationClient.unRegisterLocationListener(mLocationListener);
        mLocationListener = null;
        mLocationClient.stop();
    }

    class DriverLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (mListener != null) {
                mListener.onLocationReceived(bdLocation);
            }
        }
    }

    private OnLocationReceivedListener mListener;

}
