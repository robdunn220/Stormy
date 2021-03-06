package com.example.stormy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import android.widget.TextView;

import com.example.stormy.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather currentWeather;
    private ImageView iconImageView;
    final double latitude = 39.7684;
    final double longitude = -86.1581;

    /* Method calls getForecast() to create a new instance of CurrentWeather on app refresh */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getForecast();
    }

    /* Method to retrieve data from DarkSky API and Geocode API. Data is then used to create instance of CurrentWeather */
    private void getForecast() {
        /* Used to send attributes to the UI at the end of the function */
        final ActivityMainBinding binding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);

        TextView darkSky = findViewById(R.id.darkSkyAttribution);
        darkSky.setMovementMethod(LinkMovementMethod.getInstance());

        /* Finds the weather icon image controller for the UI, which is set when the data is retrieved from the API call */
        iconImageView = findViewById(R.id.weatherIconView);

        String apiKey = "107078ea928d559a65d02fcb25c909fc";
        String forecastURL = "https://api.darksky.net/forecast/" + apiKey + "/" + latitude + "," + longitude;

        /* Checks network availability */
        if (isNetworkAvailable()) {
            /* Creates a connection instance, which is used to make a request to the API */
            OkHttpClient client = new OkHttpClient();
            Request DarkSkyReq = new Request.Builder().url(forecastURL).build();
            Call DarkSkyCall = client.newCall(DarkSkyReq);
            DarkSkyCall.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {}

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    try {
                        String jsonData = Objects.requireNonNull(response.body()).string();
                        if (response.isSuccessful()) {
                            /* Sends response to getCurrent details, which sets the attributes needed for the CurrentWeather class */
                            currentWeather = getCurrentDetails(jsonData);
                            CurrentWeather displayWeather = new CurrentWeather(
                                    currentWeather.getLocationLabel(),
                                    currentWeather.getIcon(),
                                    currentWeather.getTime(),
                                    currentWeather.getTemperature(),
                                    currentWeather.getHumidity(),
                                    currentWeather.getPrecipChance(),
                                    currentWeather.getSummary(),
                                    currentWeather.getTimezone()
                            );
                            /* Sends CurrentWeather instance to the UI */
                            binding.setWeather(displayWeather);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Drawable drawable = getResources().getDrawable(displayWeather.getIconID());
                                    iconImageView.setImageDrawable(drawable);
                                }
                            });
                        } else {
                            alertUserError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "IO Exception caught ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Exception caught: ", e);
                    }
                }
            });
        }
    }

    /* Response from API call is sent here, where essential data is extracted */
    @NotNull
    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        JSONObject currently = forecast.getJSONObject("currently");
        String timezone = forecast.getString("timezone");

        /* Reverse Geocode to get location name */
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        String location = null;
        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /* Countries outside of US have data organized differently,
        so it's necessary to institute checks to find the correct data when setting location name */
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            if (address.getCountryCode().equals("US")) {
                location = address.getLocality() + ", " + address.getAdminArea();
            } else {
                if (address.getLocality() != null) {
                    location = address.getLocality() + ", " + address.getCountryCode();
                } else {
                    location = address.getAdminArea() + ", " + address.getCountryCode();
                }
                System.out.println(address);
            }
        }

        /* Sets the gathered attributes for the CurrentWeather instance */
        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setLocationLabel(location);
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimezone(timezone);
        
        return currentWeather;
    }

    /* Checks for network connectivity. This is called before any other process begins */
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null;
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        } else {
            Toast.makeText(this, R.string.network_unavailable, Toast.LENGTH_LONG).show();
        }

        return isAvailable;
    }

    private void alertUserError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getSupportFragmentManager(), "error_dialog");
    }

    /* Refreshes the page with current information. Triggered by refresh button in the UI */
    public void refreshOnClick(View view) {
        Toast.makeText(this, "Refreshing data", Toast.LENGTH_LONG).show();
        getForecast();
    }
}
