package com.chaoyang805.calltaxi.api;

/**
 * Created by chaoyang805 on 2015/11/12.
 */
public class OnMessageReceivedListenerAdapter implements OnMessageReceivedListener {
    @Override
    public void onUpdateDriver(String message) {}
    @Override
    public void onDriverAccept(String message) {}
    @Override
    public void onDriverOffline(String detail) {}
}
