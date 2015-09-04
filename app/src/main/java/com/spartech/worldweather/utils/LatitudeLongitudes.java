package com.spartech.worldweather.utils;

import java.util.ArrayList;

/**
 * Created by HQ on 03-Sep-15.
 * countries and their capitals + latitudes & longitudes
 */
public class LatitudeLongitudes {

    private ArrayList<Model> mModels;

    public LatitudeLongitudes() {
        mModels.add(new Model("Argentina", "Buenos Aires", "36.30", "60.00"));
        mModels.add(new Model("Austria", "Vienna", "48.12", "16.22"));
        mModels.add(new Model("Brazil", "Brasilia", "15.47", "47.55"));
        mModels.add(new Model("Canada", "Ottawa", "45.27", "75.42"));
        mModels.add(new Model("Denmark", "Copenhagen", "55.41", "12.34"));
        mModels.add(new Model("Egypt", "Cairo", "30.01", "31.14"));
        mModels.add(new Model("Finland", "Helsinki", "60.15", "25.03"));
        mModels.add(new Model("France", "Paris", "48.50", "02.20"));
        mModels.add(new Model("Germany", "Berlin", "52.30", "13.25"));
        mModels.add(new Model("Iran", "Tehran", "35.44", "51.30"));
        mModels.add(new Model("Irak", "Baghdad", "33.20", "44.30"));
        mModels.add(new Model("Ireland", "Dublin", "53.21", "06.15"));
        mModels.add(new Model("Italy", "Rome", "41.54", "12.29"));
        mModels.add(new Model("Lebanon", "Beirut", "33.53", "35.31"));
        mModels.add(new Model("Mexico", "Mexico", "19.20", "99.10"));
        mModels.add(new Model("Netherlands", "Amsterdam", "52.23", "04.54"));
        mModels.add(new Model("New Zealand", "Wellington", "41.19", "174.46"));
        mModels.add(new Model("Norway", "Oslo", "59.55", "10.45"));
        mModels.add(new Model("Oman", "Masqat", "23.37", "58.36"));
        mModels.add(new Model("Poland", "Warsaw", "52.13", "21.00"));
        mModels.add(new Model("Portugal", "Lisbon", "38.42", "09.10"));
        mModels.add(new Model("Qatar", "Doha", "25.15", "51.35"));
        mModels.add(new Model("Russia", "Moscow", "55.45", "37.35"));
        mModels.add(new Model("Saudi Arabia", "Riyadh", "24.41", "46.42"));
        mModels.add(new Model("South Africa", "Johannesburg", "25.44", "28.12"));
        mModels.add(new Model("Spain", "Madrid", "40.25", "03.45"));
        mModels.add(new Model("Sweden", "Stockholm", "59.20", "18.03"));
        mModels.add(new Model("Switzerland", "Bern", "46.57", "07.28"));
        mModels.add(new Model("Syria", "Damascus", "33.30", "36.18"));
        mModels.add(new Model("Turkey", "Ankara", "39.57", "32.54"));
    }

    public class Model {
        private String mCountry;
        private String mCapital;
        private String mLatitude;
        private String mLongitude;

        public Model(String country, String capital, String latitude, String longitude) {
            mCountry = country;
            mCapital = capital;
            mLatitude = latitude;
            mLongitude = longitude;
        }
    }

}
