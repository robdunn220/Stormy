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

    final double latitude = 42.3314;
    final double longitude = -83.0458;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getForecast();
    }

    private void getForecast() {
        final ActivityMainBinding binding = DataBindingUtil.setContentView(MainActivity.this,
                R.layout.activity_main);

        TextView darkSky = findViewById(R.id.darkSkyAttribution);
        darkSky.setMovementMethod(LinkMovementMethod.getInstance());

        iconImageView = findViewById(R.id.weatherIconView);

        String apiKey = "107078ea928d559a65d02fcb25c909fc";
        String forecastURL = "https://api.darksky.net/forecast/" + apiKey + "/" + latitude + "," + longitude;

        if (isNetworkAvailable()) {
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

    @NotNull
    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        JSONObject currently = forecast.getJSONObject("currently");
        String timezone = forecast.getString("timezone");

        // Geocoder to get location name
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        String location = null;
        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);

            if (address.getCountryCode().equals("US")) {
                location = address.getLocality() + ", " + address.getAdminArea();
            } else {
                location = address.getAdminArea() + ", " + address.getFeatureName();
                System.out.println(address);
            }
        }

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

    public void refreshOnClick(View view) {
        Toast.makeText(this, "Refreshing data", Toast.LENGTH_LONG).show();
        getForecast();
    }
}
