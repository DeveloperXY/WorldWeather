package com.spartech.worldweather.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.spartech.worldweather.R;
import com.spartech.worldweather.weather.Hour;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by HQ on 01-Sep-15.
 */
public class HourAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int EMPTY_VIEW = 10;
    private Hour[] mHours;
    private Context mContext;


    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }


    public HourAdapter(Context context, Hour[] hours) {
        mContext = context;
        mHours = hours;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View v;

        if (viewType == EMPTY_VIEW) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.empty_view, viewGroup, false);
            EmptyViewHolder evh = new EmptyViewHolder(v);
            return evh;
        }

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.hourly_list_item, viewGroup, false);
        RecyclerView.ViewHolder viewHolder = new HourViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder hourViewHolder, int i) {
        if(mHours != null)
            ((HourViewHolder) hourViewHolder).bindHour(mHours[i]);
    }

    @Override
    public int getItemCount() {
        return mHours == null ? 1 : mHours.length;
    }

    @Override
    public int getItemViewType(int position) {
        if (mHours == null) {
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }

    public class HourViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        @Bind(R.id.timeLabel)
        TextView mTimeLabel;
        @Bind(R.id.summaryLabel)
        TextView mSummaryLabel;
        @Bind(R.id.dailyTemperatureLabel)
        TextView mTemperatureLabel;
        @Bind(R.id.iconImageView)
        ImageView mIconImageView;

        public HourViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bindHour(Hour hour) {
            mTimeLabel.setText(hour.getHour());
            mSummaryLabel.setText(hour.getSummary());
            mTemperatureLabel.setText(hour.getTemperature() + "");
            mIconImageView.setImageResource(hour.getIconId());
        }

        @Override
        public void onClick(View v) {
            String time = mTimeLabel.getText().toString();
            String temperature = mTemperatureLabel.getText().toString();
            String summary = mSummaryLabel.getText().toString();
            String message = String.format("At %s it will be %s and %s",
                    time,
                    temperature,
                    summary);

            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
    }

}
