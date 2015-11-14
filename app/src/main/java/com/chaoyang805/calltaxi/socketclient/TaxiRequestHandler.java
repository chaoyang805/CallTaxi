package com.chaoyang805.calltaxi.socketclient;

import android.util.Log;

import com.chaoyang805.calltaxi.api.OnMessageReceivedListener;
import com.chaoyang805.calltaxi.utils.LogHelper;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 * Created by chaoyang805 on 2015/9/20.
 */
public class TaxiRequestHandler extends IoHandlerAdapter {
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
            case "driver_accept":
                //接受消息
                if (mListener != null) {
                    mListener.onDriverAccept(detail);
                }
                break;
            case "update_driver":
                if (mListener != null) {
                    mListener.onUpdateDriver(detail);
                }
                break;
            case "driver_offline":
                //TODO driver is offline,remove the driver's marker from baidu mapview
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
