# **Stormy**

## **Introduction**

Stormy is a single-page Android application that takes a location, in longitude-latitude form,
and displays basic weather information about that location.

<img src="https://i.imgur.com/tdyQ1TE.png"
	title="Screenshot" width="350" height="700" />

## **Purpose**

I created this application to learn more intermediate Android development skills, including testing skills such as Toast's and exceptions. I also wanted to be more adept at using xml in manipulating the UI.

## **Languages/Tools**

I designed the app in Android Studio. I employed Java for the backend and xml for the UI.
I used the API's from DarkSky and Google Maps for the weather and location data.

## **Technical Details**

The application requires a coordinate set for the location, which are easily found thru Google.
A network connectivity check is performed, and after it passes, a connection is made with the DarkSky API. Upon a succesful response, the neccesary data is aquired from the response. After a series of checks, an instance of the CurrentWeather class is created with the data.

```java
/* Checks network availability */
if (isNetworkAvailable()) {
    /* Creates a connection instance, 
    which is used to make a request to the API */
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
                    /* Sends response to getCurrentDetails, which sets the
                    attributes needed for the CurrentWeather class */
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
```

The function getCurrentDetails is where the data parsing occurs. It is also where the call to the Google Maps API is made. The same coordinates are sent and, upon success, the JSON data for that location is returned. Checks are performed to make sure the relevant data is there. This is neccesary because the organization of the data may change based on the country and region of the location.

```java
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
so it's necessary to institute checks to find the 
correct data when setting location name */
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
```

Once the creation of the instance is done, that object is saved and bound to the main activity xml file. This allows for easy data binding and referncing in the xml. This is essential in getting the correct data displayed in the UI.

Once bound to the xml file, it's just a matter of setting variable names for the data. Those variables are simply linked to the desired UI elements that are created.

## **Test it yourself!**

To test this app, a few things are required:

* Android Studio, or another Java SDK with an emulator
* An internet connection, for the API calls
* Clone or download this repository

After completing these, all that is left is to is open the project in Android Studio. An emulator has to be selected (or it can be ran on an actual device). After the emulator is installed, simply press run and the app should start on the emulator.

Default coordinates are already in the code, near the top of the main class in MainActivity.java. However, any location can be tested. Google is an easy resource for finding coordinates. Just copy and paste your coordinates into the values of the existing variables.

```java
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather currentWeather;
    private ImageView iconImageView;
    final double latitude = 39.7684;
    final double longitude = -86.1581;
```

Be careful, though, because depending on the hemisphere, the latitude and longitude can have a negative value that Google doesn't always show in the results.

## **Future Goals**

I would like to add more functionality to this app. Having the default location as the users current location (with permission), another page of more detailed weather information, and a preset list of popular locations to choose from are on the bucket list.
