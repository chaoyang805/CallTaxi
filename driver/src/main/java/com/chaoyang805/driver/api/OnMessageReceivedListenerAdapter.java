package com.chaoyang805.driver.api;

/**
 * Created by chaoyang805 on 2015/11/12.
 */
public class OnMessageReceivedListenerAdapter implements OnMessageReceivedListener {

    @Override
    public void onUpdatePassenger(String message) {}

    @Override
    public void onPassengerTaken(String message) {}

    @Override
    public void onPassengerCancel(String message) {}

    @Override
    public void onPassengerOffline(String message) {}


}
