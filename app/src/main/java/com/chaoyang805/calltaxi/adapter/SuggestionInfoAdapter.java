package com.chaoyang805.calltaxi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.mapapi.search.sug.SuggestionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaoyang805 on 2015/11/8.
 */
public class SuggestionInfoAdapter extends BaseAdapter {

    private Context mContext;

    private List<SuggestionResult.SuggestionInfo> mInfoList;

    public SuggestionInfoAdapter(Context context){
        mContext = context;
        mInfoList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mInfoList.size();
    }

    public void addAll(List<SuggestionResult.SuggestionInfo> list) {
        mInfoList.clear();
        mInfoList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public SuggestionResult.SuggestionInfo getItem(int position) {
        return mInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
            holder.tvDest = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvDest.setText(getItem(position).key);
        return convertView;
    }

    static class ViewHolder {
        TextView tvDest;
    }
}
