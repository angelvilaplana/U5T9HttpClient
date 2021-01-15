package dam.angelvilaplana.u5t9httpclient.connection;

import android.net.Uri;
import android.util.Log;
import dam.angelvilaplana.u5t9httpclient.model.GeonamesPlace;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GeonamesPlaceConnection extends Connection {

    private final String language;
    private final int rows;
    private final String userName;
    private final ArrayList<GeonamesPlace> geonamesPlaceList;

    public GeonamesPlaceConnection(String languag, int rows, String userName) {
        this.language = languag;
        this.rows = rows;
        this.userName = userName;
        this.geonamesPlaceList = new ArrayList<>();
    }

    public URL build(String place) {
        try {
            // url = new URL(URL_GEONAMES + "?q=" + place + "&maxRows=" + ROWS + "&userName=" + USER_NAME);
            // Use better a builder to avoid a malformed query string
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.geonames.org")
                    .appendPath("wikipediaSearchJSON")
                    .appendQueryParameter("q", place)
                    .appendQueryParameter("lang", language)
                    .appendQueryParameter("maxRows", String.valueOf(rows))
                    .appendQueryParameter("username", userName);

            return new URL(builder.build().toString());
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
            JSONArray jArray = json.getJSONArray("geonames");

            if (jArray.length() > 0) {
                // Fill list with data form summary attribute
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject item = jArray.getJSONObject(i);
                    String summary = item.getString("summary");
                    double lat = item.getDouble("lat");
                    double lng = item.getDouble("lng");
                    GeonamesPlace geonamesPlace = new GeonamesPlace(summary, lat, lng);
                    geonamesPlaceList.add(geonamesPlace);
                    // buildWeatherPlace(geonamesPlace);
                }

                return FOUND_JSON;
            } else {
                return NO_FOUND_JSON;
            }
        } else {
            Log.i("URL", "ErrorCode: " + urlConnection.getResponseCode());
            // searchResult.add("HttpURLConnection ErrorCode: " + urlConnection.getResponseCode());
            return ERROR_JSON;
        }
    }

    public ArrayList<GeonamesPlace> getGeonamesPlaceList() {
        return geonamesPlaceList;
    }

}
