package com.chaoyang805.calltaxi.api;

/**
 * Created by chaoyang805 on 2015/11/12.
 */
public interface OnMessageReceivedListener {

    void onUpdateDriver(String message);

    void onDriverAccept(String message);

    void onSessionClosed();
}
