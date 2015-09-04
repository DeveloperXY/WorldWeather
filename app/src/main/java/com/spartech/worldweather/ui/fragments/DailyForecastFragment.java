package com.spartech.worldweather.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.spartech.worldweather.R;
import com.spartech.worldweather.adapters.DayAdapter;
import com.spartech.worldweather.weather.Day;

import butterknife.Bind;
import butterknife.ButterKnife;


public class DailyForecastFragment extends ListFragment {

    public static final String TAG = DailyForecastFragment.class.getSimpleName();
    private static final String DAILY_FRAGMENT_SAVE = "dailyFragmentSave";
    private static final String DAILY_FRAGMENT_LOCATION_SAVE = "dailyFragmentLocationSave";
    @Bind(R.id.locationLabel)
    TextView mLocationLabel;
    @Bind(android.R.id.list)
    ListView mListView;
    @Bind(android.R.id.empty)
    TextView mEmptyTextView;
    private static Day[] mDays;
    private static String mLocation;
    private DayAdapter mDayAdapter;
    private OnDailyFragmentInteractionListener mListener;

    public String getLocation() {
        return mLocation;
    }

    public void setDays(Day[] days) {
        mDays = days;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public DailyForecastFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void restoreComplexPreferences() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(DAILY_FRAGMENT_SAVE, "");
        Day[] days = gson.fromJson(json, Day[].class);
        String savedLocation = sharedPreferences.getString(DAILY_FRAGMENT_LOCATION_SAVE, "");

        mDays = days;
        updateDayAdapter(savedLocation);
    }

    private void saveComplexPreferences() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(mDays);

        editor.putString(DAILY_FRAGMENT_SAVE, json);
        editor.putString(DAILY_FRAGMENT_LOCATION_SAVE, mLocation);

        editor.apply();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_forecast, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void updateDayAdapter(String location) {
        mLocation = location;
        Log.i(TAG, "Day Adapter updater called");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDayAdapter = new DayAdapter(getActivity(), mDays);
                mListView.setAdapter(mDayAdapter);
                mLocationLabel.setText(mLocation);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        restoreComplexPreferences();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        updateDayAdapter(mLocation);
        mListView.setEmptyView(mEmptyTextView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String dayOfTheWeek = mDays[position].getDayOfTheWeek();
                String conditions = mDays[position].getSummary();
                String highTemp = mDays[position].getTemperatureMax() + "";
                String message = String.format("On %s the high will be %s and it will be %s",
                        dayOfTheWeek,
                        highTemp,
                        conditions);

                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        saveComplexPreferences();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDailyFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnDailyFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onDailyFragmentInteraction(Uri uri);
    }

}
