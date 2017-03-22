package com.dysania.bluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DysaniazzZ on 20/03/2017.
 */
public class AvailableDevicesAdapter extends BaseAdapter {

    private Context mContext;
    private List<BluetoothDevice> mBluetoothDevices;

    public AvailableDevicesAdapter(Context context) {
        this.mContext = context;
        this.mBluetoothDevices = new ArrayList<>();
    }

    public void addItem(BluetoothDevice bluetoothDevice) {
        if (!mBluetoothDevices.contains(bluetoothDevice)) {
            mBluetoothDevices.add(bluetoothDevice);
        }
        this.notifyDataSetChanged();
    }

    public void clear() {
        mBluetoothDevices.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mBluetoothDevices.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return mBluetoothDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_device, parent, false);
            viewHolder.mTvItemDeviceName = (TextView) convertView.findViewById(R.id.tv_device_name);
            viewHolder.mTvItemDeviceAddress = (TextView) convertView.findViewById(R.id.tv_device_address);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        BluetoothDevice bluetoothDevice = mBluetoothDevices.get(position);
        String deviceName = bluetoothDevice.getName();
        viewHolder.mTvItemDeviceName.setText(TextUtils.isEmpty(deviceName) ? "N/A" : deviceName);
        viewHolder.mTvItemDeviceAddress.setText(bluetoothDevice.getAddress());
        return convertView;
    }

    static class ViewHolder {

        TextView mTvItemDeviceName;
        TextView mTvItemDeviceAddress;
    }
}
