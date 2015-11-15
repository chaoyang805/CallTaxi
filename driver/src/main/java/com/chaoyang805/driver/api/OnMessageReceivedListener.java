package com.chaoyang805.driver.api;

/**
 * Created by chaoyang805 on 2015/11/12.
 */
public interface OnMessageReceivedListener {

    void onUpdatePassenger(String message);

    void onPassengerTaken(String message);

    void onPassengerCancel(String message);

    void onPassengerOffline(String message);

}
