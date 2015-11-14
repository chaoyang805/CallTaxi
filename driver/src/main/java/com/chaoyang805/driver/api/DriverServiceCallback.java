package com.chaoyang805.driver.api;

/**
 * Created by chaoyang805 on 2015/11/12.
 */
public interface DriverServiceCallback {

    void onUpdatePassenger(String msg);

    void onPassengerTaken(String msg);

    void onPassengerCancel(String msg);
}
