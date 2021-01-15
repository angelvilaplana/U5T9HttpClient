package dam.angelvilaplana.u5t9httpclient.connection;

import android.net.Uri;
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

public class WeatherPlaceConnection extends Connection {

    private final String appid;
    private WeatherPlace weatherPlace;

    public WeatherPlaceConnection(String appid) {
        this.appid = appid;
    }

    public URL build(GeonamesPlace geonamesPlace) {
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.openweathermap.org")
                    .appendPath("data").appendPath("2.5").appendPath("weather")
                    .appendQueryParameter("lat", String.valueOf(geonamesPlace.getLat()))
                    .appendQueryParameter("lon", String.valueOf(geonamesPlace.getLng()))
                    .appendQueryParameter("units", "metric")
                    .appendQueryParameter("appid", appid);

            return new URL(builder.build().toString());
            // startBackgroundWeatherTask(url);
        } catch (MalformedURLException e) {
            Log.i("URL", e.getMessage());
            return null;
        }
    }

    @Override
    protected int getData(HttpURLConnection urlConnection) throws IOException, JSONException {
        // Check if response was OK (response = 200)
        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // Read data stream from url
            String resultStream = readStream(urlConnection.getInputStream());

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

            weatherPlace = new WeatherPlace(lat, lon, temp, humidity, description);
            return FOUND_JSON;
        } else {
            Log.i("URL", "ErrorCode: " + urlConnection.getResponseCode());
            // searchResult.add("HttpURLConnection ErrorCode: " + urlConnection.getResponseCode());
            return ERROR_JSON;
        }
    }

    public WeatherPlace getWeatherPlace() {
        return weatherPlace;
    }

}
