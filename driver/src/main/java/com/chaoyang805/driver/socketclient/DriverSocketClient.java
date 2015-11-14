package com.chaoyang805.driver.socketclient;

import com.chaoyang805.driver.model.Message;
import com.chaoyang805.driver.model.Passenger;
import com.chaoyang805.driver.utils.LogHelper;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Created by chaoyang805 on 2015/9/20.
 */
public class DriverSocketClient {

    private static final String TAG = LogHelper.makeLogTag(DriverSocketClient.class);
    /**
     * socketConnector对象
     */
    private NioSocketConnector mConnector;
    /**
     * 处理消息的Handler对象
     */
    private DriverHandler mHandler;
    /**
     * 客户端连接成功后取得的会话对象
     */
    private IoSession mSession;
    /**
     * 是否连接到服务器端的标志位
     */
    private boolean isConnected = false;
    /**
     * 是否已经初始化完成
     */
    private boolean initiated = false;

    /**
     * 连接服务器超时的回调
     */
    private Callback mCallback;
    /**
     * 开启socket连接的用户名
     */
    private String mName;
    /**
     * 开启socket连接的手机号
     */
    private String mPhoneNumber;

    public DriverSocketClient(String name, String phoneNumber, Callback callback) {
        mHandler = new DriverHandler();
        mCallback = callback;
        mName = name;
        mPhoneNumber = phoneNumber;
    }

    /**
     * 初始化客户端
     */
    public void init() {
        //如果已经初始化。不再重复进行
        if (initiated) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                mConnector = new NioSocketConnector();
                mConnector.setHandler(mHandler);
                mConnector.getFilterChain().addLast("codec",
                        new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
                mConnector.setConnectTimeoutMillis(10000);
                ConnectFuture future = mConnector.connect(new InetSocketAddress("192.168.0.106", 9988));
                future.awaitUninterruptibly();
                try {
                    mSession = future.getSession();
                    isConnected = mSession.isConnected();
                    if (isConnected) {
                        if (mCallback != null) {
                            mCallback.onConnected();
                        }
                    }
                } catch (Exception e) {
                    reset();
                    if (mCallback != null) {
                        mCallback.onTimeOut();
                    }
                    e.printStackTrace();
                }
            }
        }.start();
        initiated = true;
    }

    /**
     * 重置客户端
     */
    public void reset() {
        mConnector.dispose();
        mConnector = null;
        initiated = false;
    }

    public DriverHandler getHandler() {
        return mHandler;
    }

    public void sendMessage(Message msg) {
        mSession.write((msg.toString()));
    }

    public void updateLocation(double[] location) {
        String type = "driver_update_location";
        Message updateMsg = new Message.MessageBuilder()
                .setRequestType(type)
                .setDriverName(mName)
                .setDriverPhoneNumber(mPhoneNumber)
                .setLocation(location)
                .build();
        sendMessage(updateMsg);
    }

    public void driverLogin() {
        String type = "driver_login";
        Message msg = new Message.MessageBuilder()
                .setRequestType(type)
                .setDriverName(mName)
                .setDriverPhoneNumber(mPhoneNumber)
                .build();
        sendMessage(msg);
    }

    public void driverAccept(Passenger passenger,double[] location) {
        String type = "driver_accept";
        Message msg = new Message.MessageBuilder()
                .setRequestType("driver_accept")
                .setDriverName(mName)
                .setDriverPhoneNumber(mPhoneNumber)
                .setPassengerName(passenger.getName())
                .setPassengerPhoneNumber(passenger.getPhoneNumber())
                .setLocation(location)
                .build();

    }


    /**
     * 关闭客户端连接的方法
     */
    public void disconnect() {
        LogHelper.d(TAG, "disconect");
        mCallback = null;
        if (mSession != null && isConnected) {
            mSession.close(true);
            mConnector.dispose();
        }
    }

    public interface Callback {
        void onTimeOut();

        void onConnected();
    }

}
