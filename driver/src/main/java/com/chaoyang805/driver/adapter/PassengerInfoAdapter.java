package com.chaoyang805.driver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chaoyang805.driver.R;
import com.chaoyang805.driver.model.Passenger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaoyang805 on 2015/11/13.
 */
public class PassengerInfoAdapter extends BaseAdapter {

    private Context mContext;
    private List<Passenger> mPassengers;
    private List<String> mPassengerNumbers = new ArrayList<>();

    public PassengerInfoAdapter(Context context) {
        mContext = context;
        mPassengers = new ArrayList<>();
    }

    public void updatePassenger(Passenger passenger) {
        if (mPassengerNumbers.contains(passenger.getPhoneNumber())) {
            int position = mPassengerNumbers.indexOf(passenger.getPhoneNumber());
            Passenger p = mPassengers.get(position);
            p.setLocation(passenger.getLocation());
            p.setDestination(passenger.getDestination());
            p.setDistance(passenger.getDistance());
        } else {
            mPassengers.add(passenger);
            mPassengerNumbers.add(passenger.getPhoneNumber());
        }
        notifyDataSetChanged();
    }

    public void passengerTaken(Passenger passenger) {
        if (mPassengerNumbers.contains(passenger.getPhoneNumber())) {
            int position = mPassengerNumbers.indexOf(passenger.getPhoneNumber());
            mPassengers.remove(position);
            mPassengerNumbers.remove(position);
            notifyDataSetChanged();
        }
    }

    public void passengerCancel(Passenger passenger) {
        if (mPassengerNumbers.contains(passenger.getPhoneNumber())) {
            int position = mPassengerNumbers.indexOf(passenger.getPhoneNumber());
            mPassengers.remove(position);
            mPassengerNumbers.remove(position);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mPassengers.size();
    }

    @Override
    public Passenger getItem(int position) {
        return mPassengers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Passenger passenger = getItem(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_passenger_info, parent, false);
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_passenger_name);
            holder.tvDestination = (TextView) convertView.findViewById(R.id.tv_destination);
            holder.tvDistance = (TextView) convertView.findViewById(R.id.tv_distance);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvName.setText(passenger.getName());
        holder.tvDistance.setText(passenger.getDistance() + "m");
        int destDistance = (int)(passenger.getDestination().getDistance()) / 1000;
        holder.tvDestination.setText(passenger.getDestination().getDetailAdress() + " " + destDistance + "km");
        return convertView;
    }

    static class ViewHolder {
        TextView tvName;
        TextView tvDistance;
        TextView tvDestination;
    }
}
