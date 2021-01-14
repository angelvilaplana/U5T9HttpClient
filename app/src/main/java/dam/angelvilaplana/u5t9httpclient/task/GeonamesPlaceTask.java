package dam.angelvilaplana.u5t9httpclient.task;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeonamesPlaceTask {

    private BackgroundTask backgroundTask;
    private ExecutorService executor;
    private ArrayList<GeonamesPlace> listGeonamesPlace;
    private int resultData;

    private final static String URL_GEONAMES = "http://api.geonames.org/wikipediaSearchJSON";
    private final static String USER_NAME = "angelvil";
    private final static int ROWS = 10;
    private final static String LANGUAGE = "es";

    public GeonamesPlaceTask(BackgroundTask backgroundTask) {
        this.backgroundTask = backgroundTask;
        this.listGeonamesPlace = new ArrayList<>();
    }

    private int startTask(final URL url) {
        final int CONNECTION_TIMEOUT = 10000;
        final int READ_TIMEOUT = 7000;

        executor = Executors.newSingleThreadExecutor();

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
                    resultData = getData(urlConnection);
                } catch (IOException e) {
                    Log.i("IOException", e.getMessage());
                    // searchResult.add("IOException: " + e.getMessage());
                    resultData = BackgroundTask.ERROR_JSON;
                } catch (JSONException e) {
                    Log.i("JSONException", e.getMessage());
                    // searchResult.add("JSONException: " + e.getMessage());
                    resultData = BackgroundTask.ERROR_JSON;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        });

        return resultData;
    }

    private int getData(HttpURLConnection urlConnection) throws IOException, JSONException {
        // Check if response was OK (response = 200)
        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // Read data stream from url
            String resultStream = backgroundTask.readStream(urlConnection.getInputStream());

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
                    listGeonamesPlace.add(geonamesPlace);
                }

                return BackgroundTask.FOUND_JSON;
            } else {
                return BackgroundTask.NO_FOUND_JSON;
            }
        } else {
            Log.i("URL", "ErrorCode: " + urlConnection.getResponseCode());
            // searchResult.add("HttpURLConnection ErrorCode: " + urlConnection.getResponseCode());
            return BackgroundTask.ERROR_JSON;
        }
    }

    private void buildGeonamesPlace(String place) {
        URL url;
        try {
            // url = new URL(URL_GEONAMES + "?q=" + place + "&maxRows=" + ROWS + "&userName=" + USER_NAME);
            // Use better a builder to avoid a malformed query string
            // TODO Activity 1 - Edit search, create GeonamesPlace object & hide keyboard
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.geonames.org")
                    .appendPath("wikipediaSearchJSON")
                    .appendQueryParameter("q", place)
                    .appendQueryParameter("lang", LANGUAGE)
                    .appendQueryParameter("maxRows", String.valueOf(ROWS))
                    .appendQueryParameter("username", USER_NAME);

            url = new URL(builder.build().toString());
            startTask(url);
        } catch (MalformedURLException e) {
            Log.i("URL", e.getMessage());
        }
    }

}
