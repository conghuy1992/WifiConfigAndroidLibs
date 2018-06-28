package jp.co.alpine.wifisetingsample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by macbook on 1/23/18.
 */

public class WifiListAdapter extends BaseAdapter{
    private LayoutInflater mInflater;
    private List<String> mData;
    private Context ct;

    public WifiListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        ct = context;
    }

    public WifiListAdapter(Context context, List<String> data) {
        mInflater = LayoutInflater.from(context);
        ct = context;
        mData = data;
    }


    public void setData(List<String> data) {
        mData = data;
    }

    public int getCount() {
        return (mData == null) ? 0 : mData.size();
    }

    public Object getItem(int position) {
        return (mData == null) ? null : mData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.wifi_list_item, null);
            holder = new ViewHolder();
            holder.tvWifiName = (TextView) convertView.findViewById(R.id.wifi_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String item = mData.get(position);

        holder.tvWifiName.setText(item);

        return convertView;
    }

    static class ViewHolder {
        TextView tvWifiName;
    }
}
