package com.spartech.worldweather.ui;

import java.util.Locale;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.spartech.worldweather.R;
import com.spartech.worldweather.adapters.SmartFragmentStatePagerAdapter;
import com.spartech.worldweather.ui.fragments.DailyForecastFragment;
import com.spartech.worldweather.ui.fragments.HourlyForecastFragment;
import com.spartech.worldweather.ui.fragments.MainWeatherFragment;
import com.spartech.worldweather.utils.FragmentActivityInterfaceConstants;
import com.spartech.worldweather.utils.viewpagertransformers.DepthPageTransformer;
import com.spartech.worldweather.utils.viewpagertransformers.ZoomOutPageTransformer;
import com.spartech.worldweather.weather.Forecast;


public class MainActivity extends FragmentActivity implements // Google Maps API interfaces
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        MainWeatherFragment.OnMainFragmentInteractionListener,
        HourlyForecastFragment.OnHourlyFragmentInteractionListener,
        DailyForecastFragment.OnDailyFragmentInteractionListener,
        ViewPager.OnPageChangeListener {

    public static final String TAG = MainActivity.class.getSimpleName(); // Tag used in 'Log' statements
    private static final String MAIN_ACTIVITY_SAVE = "mainActivitySave";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int LOCATION_REQUEST_FASTEST_INTERVAL = 10; //
    private final static int LOCATION_REQUEST_MAX_INTERVAL = 60; // time between location requests
    private final static int GET_WIFI_SETTINGS = 1; // request code for wifi settings
    private final static int GET_MOBILE_NETWORK_SETTINGS = 1; // request code for mobile data settings
    private static MainActivity ins; // static instance of this activity to access and modify
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLocation; // current user location or more precisely, the user's last known location
    private AlertDialog.Builder mDialogBuilder;
    private AlertDialog mAlertDialog; // an alert dialog displayed in case of no connectivity
    private ConnectionReceiver mConnectionReceiver;
    //  the Activity's UI from within the connection broadcast receiver
    private Forecast mForecast;

    public static MainActivity getInstance() { // MainActivity's static instance getter
        return ins;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ins = this; // Necessary for the Connection receiver to work
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_blue_gradient));

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mViewPager.setCurrentItem(1);

        // Initialize the GoogleAPIClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_REQUEST_MAX_INTERVAL * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL * 1000); // 10 seconds, in milliseconds

        Log.i(TAG, "Main Activity On Create method is done.");
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mConnectionReceiver == null) {
            mConnectionReceiver = new ConnectionReceiver(new Handler()); // Create the receiver
            registerReceiver(mConnectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)); // Register receiver
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mAlertDialog != null)
            if (mAlertDialog.isShowing())
                mAlertDialog.dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mGoogleApiClient.disconnect();
        unregisterReceiver(mConnectionReceiver);
    }

    @Override
    public void onDailyFragmentInteraction(Uri uri) {

    }

    @Override
    public void onHourlyFragmentInteraction(Uri uri) {

    }

    @Override
    public void onMainFragmentInteraction(int task) {
        switch (task) {
            case FragmentActivityInterfaceConstants.START_LOCATION_UPDATES:
                startLocationUpdates();
                break;
            case FragmentActivityInterfaceConstants.STOP_LOCATION_UPDATES_AND_DISCONNECT_GOOGLE_API:
                if (mGoogleApiClient.isConnected()) {
                    // Just like we have to disconnect our client, we also need to explicitly
                    // remove location updates after requesting them. We do this with the opposite method,
                    // which we can call in onPause() when we disconnect our Google API client:
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }
                break;
        }
    }

    @Override
    public void sendForecastToActivity(Forecast forecast) { // TMainActivity receives the whole forecast from the main fragment
        // and stores it so that it would be available to all other fragments
        Log.i(TAG, "sendForecastToActivity is called");
        if (forecast != null) {
            mForecast = forecast;
            // Save forecast
            mPagerAdapter.getHourlyForecastFragment().setHours(mForecast.getHourlyForecast());
            mPagerAdapter.getHourlyForecastFragment().updateHourAdapter();
            mPagerAdapter.getDailyForecastFragment().setDays(mForecast.getDailyForecast());
            mPagerAdapter.getDailyForecastFragment().updateDayAdapter(mForecast.getCurrent().getLocation());
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        // TODO Use the interface tasks to send data from MainActivity to Hourly & Daily Fragments

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    // request new location and update UI accordingly
    public void startLocationUpdates() {
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MainActivity.this);

        mPagerAdapter.getMainWeatherFragment().handleNewLocation(mLocation);
        Log.i(TAG, "Main Activity startLocationUpdates method is done.");
    }

    // this method is automatically executed once the GoogleAPIClient is connected
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "CONNECTEDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.getLatitude() != mLocation.getLatitude() || location.getLongitude() != mLocation.getLongitude())
            mPagerAdapter.mMainWeatherFragment.handleNewLocation(location);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    //************************************************************************

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public static class SectionsPagerAdapter extends SmartFragmentStatePagerAdapter {

        private static int NUM_ITEMS = 3;
        private static MainWeatherFragment mMainWeatherFragment; // won't work if not static
        private static HourlyForecastFragment mHourlyForecastFragment;
        private static DailyForecastFragment mDailyForecastFragment;
        // Sparse array to keep track of registered fragments in memory
        private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
        private Context mContext;

        public SectionsPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            mContext = context;
        }

        public DailyForecastFragment getDailyForecastFragment() {
            return mDailyForecastFragment;
        }

        public void setDailyForecastFragment(DailyForecastFragment dailyForecastFragment) {
            mDailyForecastFragment = dailyForecastFragment;
        }

        public HourlyForecastFragment getHourlyForecastFragment() {
            return mHourlyForecastFragment;
        }

        public void setHourlyForecastFragment(HourlyForecastFragment hourlyForecastFragment) {
            mHourlyForecastFragment = hourlyForecastFragment;
        }

        public MainWeatherFragment getMainWeatherFragment() {
            return mMainWeatherFragment;
        }

        public void setMainWeatherFragment(MainWeatherFragment mainWeatherFragment) {
            mMainWeatherFragment = mainWeatherFragment;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    if (mHourlyForecastFragment == null)
                        mHourlyForecastFragment = new HourlyForecastFragment();
                    return mHourlyForecastFragment;
                case 1:
                    if (mMainWeatherFragment == null)
                        mMainWeatherFragment = new MainWeatherFragment();
                    return mMainWeatherFragment;
                case 2:
                    if (mDailyForecastFragment == null)
                        mDailyForecastFragment = new DailyForecastFragment();
                    return mDailyForecastFragment;
            }

            return null;

        }

        // Register the fragment when the item is instantiated
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        // Unregister when the item is inactive
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return NUM_ITEMS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return mContext.getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return mContext.getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return mContext.getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }


    //**********************************************************
    // Broadcast Receiver to detect connection state changes
    //***********************************************************

    public static class ConnectionReceiver extends BroadcastReceiver {

        private final Handler handler; // Handler used to execute code on the UI thread

        public ConnectionReceiver(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void onReceive(final Context context, Intent intent) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();
            if (netInfo != null && (netInfo.getType() == ConnectivityManager.TYPE_WIFI ||
                    netInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (MainActivity.getInstance().mAlertDialog != null)
                                if (MainActivity.getInstance().mAlertDialog.isShowing())
                                    MainActivity.getInstance().mAlertDialog.dismiss();

//                            MainActivity.getInstance().mPagerAdapter.getMainWeatherFragment().toggleRefreshEnabled(true);

                            if (!MainActivity.getInstance().mGoogleApiClient.isConnected())
                                MainActivity.getInstance().mGoogleApiClient.connect();
                            else
                                MainActivity.getInstance().startLocationUpdates();
                        } catch (Exception e) {
                            Log.e(TAG, "Exception caught: ", e);
                        }
                    }
                });

            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {

//                        MainActivity.getInstance().mPagerAdapter.getMainWeatherFragment().toggleRefreshEnabled(false);

                        // Show an alertDialog as soon as the connection receiver detects
                        // that there is no Internet connection
                        final MainActivity ins = MainActivity.getInstance();

                        if (ins == null)
                            Log.i(TAG, "INS is NULLLLLLLLLLLLLLLLLLL");

                        if (ins.mDialogBuilder == null) {
                            ins.mDialogBuilder = new AlertDialog.Builder(ins);
                            ins.mDialogBuilder.setMessage(ins.getResources().getString(R.string.network_unavailable));
                            ins.mDialogBuilder.setPositiveButton(ins.getResources().getString(R.string.open_wifi_settings), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                                    Intent myIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                                    ins.startActivityForResult(myIntent, GET_WIFI_SETTINGS);
                                }
                            });
                            ins.mDialogBuilder.setNeutralButton(ins.getResources().getString(R.string.open_mobile_network_settings), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                                    Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                                    ins.startActivityForResult(intent, GET_MOBILE_NETWORK_SETTINGS);
                                }
                            });
                            ins.mDialogBuilder.setNegativeButton(ins.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    ins.finish();
                                }
                            });

                            ins.mAlertDialog = ins.mDialogBuilder.create();
                            ins.mAlertDialog.setCanceledOnTouchOutside(true);
                            ins.mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {

                                }
                            });
                            ins.mAlertDialog.show();
                        }
                    }
                });
            }

        }
    }

}
