package com.spartech.worldweather.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.spartech.worldweather.R;
import com.spartech.worldweather.utils.FragmentActivityInterfaceConstants;
import com.spartech.worldweather.utils.State;
import com.spartech.worldweather.utils.save.MainFragSave;
import com.spartech.worldweather.weather.Current;
import com.spartech.worldweather.weather.Day;
import com.spartech.worldweather.weather.Forecast;
import com.spartech.worldweather.weather.Hour;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainWeatherFragment extends Fragment {
    public static final String TAG = MainWeatherFragment.class.getSimpleName();
    private static final String KEY_TIME_VALUE = "timeValue";
    private static final String KEY_TEMPERATURE_VALUE = "temperatureValue";
    private static final String KEY_HUMIDITY_VALUE = "humidityValue";
    private static final String KEY_PRECIP_VALUE = "precipValue";
    private static final String KEY_SUMMARY_VALUE = "summaryValue";
    private static final String KEY_ICON_VALUE = "iconValue";
    private static final String KEY_LOCATION_VALUE = "locationValue";
    private static final String MAIN_FRAGMENT_SAVE = "mainFragmentSave";
    private static boolean FORECAST_SENT_TO_ACTIVITY = false;
    @Bind(R.id.timeLabel)
    TextView mTimeLabel;
    @Bind(R.id.dailyTemperatureLabel)
    TextView mTemperatureLabel;
    @Bind(R.id.humidityValue)
    TextView mHumidityValue;
    @Bind(R.id.precipValue)
    TextView mPrecipValue;
    @Bind(R.id.summaryLabel)
    TextView mSummaryLabel;
    @Bind(R.id.iconImageView)
    ImageView mIconImageView;
    @Bind(R.id.refreshImageView)
    ImageView mRefreshImageView;
    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;
    @Bind(R.id.locationLabel)
    TextView mLocationLabel;
    @Bind(R.id.navList)
    ListView mDrawerList;
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @Bind(R.id.drawerIcon)
    ImageView mDrawerIcon;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayAdapter<String> mAdapter;
    private OnMainFragmentInteractionListener mMainListener;
    private Forecast mForecast; // data model that holds the whole forecast info
    private Location mLocation; // current user location or more precisely, the user's last known location

    // Click Listener for the Refresh button
    private View.OnClickListener mOnRefreshClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                // Get forecast if there was internet access, otherwise
                // mLocation will be null, and a NullPointerException will
                // be thrown, leading to the catch block
                if (isNetworkAvailable())
                    mMainListener.onMainFragmentInteraction(FragmentActivityInterfaceConstants.START_LOCATION_UPDATES);
                else
                    Toast.makeText(getActivity(), getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getActivity(), getString(R.string.error_occured), Toast.LENGTH_LONG).show();
            }

        }
    };

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Restore the recent activity's state in case of :
    // (Screen orientation change)
    // (Closing & re-opening the app)
    private void restoreComplexPreferences() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(MAIN_FRAGMENT_SAVE, "");
        MainFragSave save = gson.fromJson(json, MainFragSave.class);

        if (save != null) {
            mTimeLabel.setText(save.getTime());
            mHumidityValue.setText(save.getHumidity());
            mLocationLabel.setText(save.getLocation());
            mPrecipValue.setText(save.getPrecip());
            mSummaryLabel.setText(save.getSummary());
            mTemperatureLabel.setText(save.getTemperature());
            mIconImageView.setImageDrawable(getResources().getDrawable(Forecast.getIconId(save.getIcon())));
        }
    }

    // Save the recent activity's state
    private void saveComplexPreferences() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        MainFragSave save = new MainFragSave(
                mTimeLabel.getText().toString(),
                mHumidityValue.getText().toString(),
                mLocationLabel.getText().toString(),
                mPrecipValue.getText().toString(),
                mSummaryLabel.getText().toString(),
                mTemperatureLabel.getText().toString(),
                null // throws NullPointerException, I'm handling it below
        );
        try {
            save.setIcon(mForecast.getCurrent().getIcon());
        } catch (NullPointerException e) {
            save.setIcon("clear-day");
        }

        String json = gson.toJson(save);

        editor.putString(MAIN_FRAGMENT_SAVE, json);
        editor.commit();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);
        addDrawerItems();
        setupDrawer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_weather, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        restoreComplexPreferences();

        // Hide progress bar
        mProgressBar.setVisibility(View.INVISIBLE);
        // The refresh icon's click listener
        mRefreshImageView.setOnClickListener(mOnRefreshClickListener);
        mDrawerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        saveComplexPreferences();

    }

    @Override
    public void onStop() {
        Log.i(TAG, "ON STOP METHOD CALLED");
        super.onStop();
//        mMainListener.onMainFragmentInteraction(FragmentActivityInterfaceConstants.STOP_LOCATION_UPDATES_AND_DISCONNECT_GOOGLE_API);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mMainListener = (OnMainFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMainListener = null;
    }

    private void addDrawerItems() {
        String[] osArray = {"Android", "iOS", "Windows", "OS X", "Linux"};
        mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    public void toggleRefreshEnabled(boolean state) {
        mRefreshImageView.setEnabled(state);
    }

    public void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        mLocation = location;

        getForecast(mLocation.getLatitude(), mLocation.getLongitude());
    }

    private void getForecast(double latitude, double longitude) {
        String apiKey = "6816e7cfc3b40e5b30170337aedbb7f4";
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey + "/" + latitude + "," + longitude;

        if (isNetworkAvailable()) {

            toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);

                        if (response.isSuccessful()) {
                            mForecast = parseForecastDetails(jsonData);
                            if (!FORECAST_SENT_TO_ACTIVITY) {
                                Log.i(TAG, "Forecast sent " + mForecast);
                                mMainListener.sendForecastToActivity(mForecast);
                                FORECAST_SENT_TO_ACTIVITY = true;
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });

                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });

        }
    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException, IOException {

        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));

        return forecast;

    }

    private Day[] getDailyForecast(String jsonData) throws JSONException, IOException {

        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];

        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();

            day.setSummary(jsonDay.getString("summary"));
            day.setIcon(jsonDay.getString("icon"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            day.setTime(jsonDay.getLong("time"));
            day.setTimezone(timezone);

            days[i] = day;
        }

        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException, IOException {

        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        Hour[] hours = new Hour[data.length()];

        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();

            hour.setSummary(jsonHour.getString("summary"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setTemperature(jsonHour.getInt("temperature"));
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimezone(timezone);

            hours[i] = hour;
        }

        return hours;
    }

    private Current getCurrentDetails(String jsonData) throws JSONException, IOException {

        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject currently = forecast.getJSONObject("currently");
        Current current = new Current();
        State st = new State();
        String city = "Unknown";
        String country = "Unknown";

        Geocoder gcd = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
            if (addresses.size() > 0) {

                if (addresses.get(0).getLocality() != null)
                    city = addresses.get(0).getLocality();

                if (addresses.get(0).getCountryName() != null) {
                    if (addresses.get(0).getCountryName().equals("United States") || addresses.get(0).getCountryName().equals("Canada"))
                        country = st.getStates().get(addresses.get(0).getAdminArea());
                    else
                        country = addresses.get(0).getCountryName();
                }
            }
        }
        catch(IOException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        current.setLocation(city, country);
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimezone(timezone);

        Log.d(TAG, current.getFormattedTime());

        return current;
    }

    private boolean isNetworkAvailable() {

        if (getActivity() == null)
            Log.i(TAG, "getActivity ============================ NULL");
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    private void alertUserAboutError() {

        AlertDialogFragment dialog = new AlertDialogFragment();

        dialog.show(getActivity().getFragmentManager(), "error_dialog");
        dialog.getDialog().setCanceledOnTouchOutside(true);
    }

    private void updateDisplay() {
        Current current = mForecast.getCurrent();

        mTemperatureLabel.setText(current.getTemperature() + ""); // convert to String
        mTimeLabel.setText("At " + current.getFormattedTime());
        mHumidityValue.setText(current.getHumidity() + "");
        mPrecipValue.setText(current.getPrecipChance() + "%");
        mSummaryLabel.setText(current.getSummary());
        mLocationLabel.setText(current.getLocation());

        Drawable drawable = getResources().getDrawable(current.getIconId());
        mIconImageView.setImageDrawable(drawable);
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
    public interface OnMainFragmentInteractionListener {
        // TODO: Update argument type and name
        void onMainFragmentInteraction(int task);

        void sendForecastToActivity(Forecast forecast);
    }

}
