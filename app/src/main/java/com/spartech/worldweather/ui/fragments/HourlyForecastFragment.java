package com.spartech.worldweather.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.spartech.worldweather.R;
import com.spartech.worldweather.adapters.HourAdapter;
import com.spartech.worldweather.ui.MainActivity;
import com.spartech.worldweather.utils.FragmentActivityInterfaceConstants;
import com.spartech.worldweather.utils.save.MainFragSave;
import com.spartech.worldweather.weather.Forecast;
import com.spartech.worldweather.weather.Hour;

import butterknife.Bind;
import butterknife.ButterKnife;


public class HourlyForecastFragment extends Fragment {

    public static final String TAG = HourlyForecastFragment.class.getSimpleName();
    private static final String HOURLY_FRAGMENT_SAVE = "hourlyFragmentSave";

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private static Hour[] mHours;
    private static HourAdapter mHourAdapter;

    public void setHours(Hour[] hours) {
        mHours = hours;
    }

    private OnHourlyFragmentInteractionListener mListener;

    public HourlyForecastFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void restoreComplexPreferences() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(HOURLY_FRAGMENT_SAVE, "");
        Hour[] hours = gson.fromJson(json, Hour[].class);

        mHours = hours;
        updateHourAdapter();
    }

    private void saveComplexPreferences() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(mHours);

        editor.putString(HOURLY_FRAGMENT_SAVE, json);
        editor.apply();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hourly_forecast, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void updateHourAdapter() {
        Log.i(TAG, "Hour Adapter updater called");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHourAdapter = new HourAdapter(getActivity(), mHours);
                mRecyclerView.setAdapter(mHourAdapter);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        restoreComplexPreferences();
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager.setOrientation(LinearLayout.VERTICAL);
        } else {
            layoutManager.setOrientation(LinearLayout.HORIZONTAL);
        }

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnHourlyFragmentInteractionListener) activity;
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

    @Override
    public void onPause() {
        super.onPause();

        saveComplexPreferences();

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnHourlyFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onHourlyFragmentInteraction(Uri uri);
    }

}
