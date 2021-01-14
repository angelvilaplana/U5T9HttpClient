package dam.angelvilaplana.u5t9httpclient.task;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import dam.angelvilaplana.u5t9httpclient.model.GeonamesPlace;
import dam.angelvilaplana.u5t9httpclient.model.WeatherPlace;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherTask {

    private BackgroundTask backgroundTask;
    private ExecutorService executor;
    private ArrayList<WeatherPlace> weatherPlaces;
    private int resultData;

    private final static String APP_ID = "65d039bf11a1067cd648158ffc82bbf5";

    public WeatherTask(BackgroundTask backgroundTask) {
        this.backgroundTask = backgroundTask;
        this.weatherPlaces = new ArrayList<>();
    }

    public int getDataWeather(HttpURLConnection urlConnection, ArrayList<WeatherPlace> weatherPlaces) throws IOException, JSONException {
        // Check if response was OK (response = 200)
        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // Read data stream from url
            String resultStream = backgroundTask.readStream(urlConnection.getInputStream());

            // Create JSON object from result stream
            JSONObject json = new JSONObject(resultStream);

            JSONObject coord = (JSONObject) json.get("coord");
            double lat = coord.getDouble("lat");
            double lon = coord.getDouble("lon");

            JSONArray weather = json.getJSONArray("weather");
            String description = weather.getJSONObject(0).getString("description");

            JSONObject main = (JSONObject) json.get("main");
            double temp = main.getDouble("temp");
            int humidity = main.getInt("humidity");

            WeatherPlace weatherPlace = new WeatherPlace(lat, lon, temp, humidity, description);
            weatherPlaces.add(weatherPlace);

            return BackgroundTask.FOUND_JSON;
        } else {
            Log.i("URL", "ErrorCode: " + urlConnection.getResponseCode());
            // searchResult.add("HttpURLConnection ErrorCode: " + urlConnection.getResponseCode());
            return BackgroundTask.ERROR_JSON;
        }
    }

    private void startTask(final URL url) {
        final int CONNECTION_TIMEOUT = 10000;
        final int READ_TIMEOUT = 7000;

        executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;

                try {
                    // 1. Create connection object by invoking the openConnection method on URL
                    urlConnection = (HttpURLConnection) url.openConnection();

                    // 2. Setup connection parameters
                    urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
                    urlConnection.setReadTimeout(READ_TIMEOUT);

                    // 3. Connect to remote object (urlConnection)
                    urlConnection.connect();

                    // 4. The remote object becomes avaible.
                    // The header fields and the contents of the remote object can be accessed
                    resultData = getDataWeather(urlConnection, weatherPlaces);
                } catch (IOException e) {
                    Log.i("IOException", e.getMessage());
                    // searchResult.add("IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.i("JSONException", e.getMessage());
                    // searchResult.add("JSONException: " + e.getMessage());
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        });
    }

    private void buildWeatherPlace(GeonamesPlace geonamesPlace, ArrayList<WeatherPlace> weatherPlaces) {
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.openweathermap.org")
                    .appendPath("data").appendPath("2.5").appendPath("weather")
                    .appendQueryParameter("lat", String.valueOf(geonamesPlace.getLat()))
                    .appendQueryParameter("lon", String.valueOf(geonamesPlace.getLng()))
                    .appendQueryParameter("units", "metric")
                    .appendQueryParameter("appid", APP_ID);

            URL url = new URL(builder.build().toString());
            startTask(url);
        } catch (MalformedURLException e) {
            Log.i("URL", e.getMessage());
        }
    }

}
