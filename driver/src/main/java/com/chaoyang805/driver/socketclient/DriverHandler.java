package com.chaoyang805.driver.socketclient;

import android.util.Log;

import com.chaoyang805.driver.api.OnMessageReceivedListener;
import com.chaoyang805.driver.utils.LogHelper;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 * Created by chaoyang805 on 2015/9/20.
 */
public class DriverHandler extends IoHandlerAdapter {
    private static final String TAG = "TaxiRequestHandler";

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        Log.e(TAG, cause.getMessage(), cause);
        super.exceptionCaught(session, cause);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        super.messageReceived(session, message);
        //接受服务端的消息
        Log.d(TAG, (String) message);
        String msg = (String) message;
        String[] results = msg.split("://");
        String requestType = results[0];
        String detail = results[1];
        //根据action判断消息的类型
        switch (requestType) {
            case "update_passenger":
                //接受消息
                if (mListener != null) {
                    mListener.onUpdatePassenger(detail);
                }
                break;
            case "passenger_cancel_call":
                if (mListener != null) {
                    mListener.onPassengerCancel(detail);
                }
                break;
            case "passenger_taken":
                if (mListener != null) {
                    mListener.onPassengerTaken(detail);
                }
                break;
            case "passenger_offline":
                if (mListener != null) {
                    mListener.onPassengerOffline(detail);
                }
                break;
            default:
                LogHelper.d(TAG, "unknown request type:" + requestType);
                break;
        }
    }

    private OnMessageReceivedListener mListener;

    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        mListener = listener;
    }

}
