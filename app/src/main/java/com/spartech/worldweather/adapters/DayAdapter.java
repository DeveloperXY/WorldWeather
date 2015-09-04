package com.spartech.worldweather.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.spartech.worldweather.R;
import com.spartech.worldweather.weather.Day;

/**
 * Created by HQ on 01-Sep-15.
 */
public class DayAdapter extends BaseAdapter {

    private Context mContext;
    private Day[] mDays;

    public DayAdapter(Context context, Day[] days) {
        mContext = context;
        mDays = days;
    }

    @Override
    public int getCount() {
        return mDays == null ? 0 : mDays.length;
    }

    @Override
    public Object getItem(int position) {
        return mDays[position];
    }

    @Override
    public long getItemId(int position) {
        return 0; // we aren't going to use this. it's used to tag items for easy reference
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            // brand new
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.daily_list_item, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            holder.temperatureLabel = (TextView) convertView.findViewById(R.id.dailyTemperatureLabel);
            holder.dayLabel = (TextView) convertView.findViewById(R.id.dayNameLabel);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Day day = mDays[position];

        holder.iconImageView.setImageResource(day.getIconId());
        holder.temperatureLabel.setText(day.getTemperatureMax() + "");
        if(position == 0)
            holder.dayLabel.setText("Today");
        else if(position == 1)
            holder.dayLabel.setText("Tomorrow");
        else
            holder.dayLabel.setText(day.getDayOfTheWeek());

        return convertView;
    }

    private static class ViewHolder {
        ImageView iconImageView;
        TextView temperatureLabel;
        TextView dayLabel;

    }
}
