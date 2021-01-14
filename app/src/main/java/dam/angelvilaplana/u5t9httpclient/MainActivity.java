package dam.angelvilaplana.u5t9httpclient;

import android.content.Context;
import android.net.*;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import dam.angelvilaplana.u5t9httpclient.model.GeonamesPlace;
import dam.angelvilaplana.u5t9httpclient.model.WeatherPlace;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    // Constants
    private final static String URL_GEONAMES = "http://api.geonames.org/wikipediaSearchJSON";
    private final static String USER_NAME = "angelvil";
    private final static int ROWS = 10;
    private final static String LANGUAGE = "es";
    private final static String APP_ID = "65d039bf11a1067cd648158ffc82bbf5";
    private final static int FOUND_JSON = 1;
    private final static int NO_FOUND_JSON = 2;
    private final static int ERROR_JSON = 3;

    // Attributes
    private EditText etPlaceName;
    private Button btSearch;
    private ListView lvSearchResult;
    private ProgressBar progressBar;
    private ExecutorService executor;
    private int resultData;
    private ArrayList<GeonamesPlace> searchGeonamesPlace;
    private ArrayList<WeatherPlace> weatherPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUI(savedInstanceState);
    }

    private void setUI(Bundle savedInstanceState) {
        etPlaceName = findViewById(R.id.etPlaceName);
        progressBar = findViewById(R.id.progressBar);
        btSearch = findViewById(R.id.btSearch);
        btSearch.setOnClickListener(this);
        lvSearchResult = findViewById(R.id.lvSearchResult);
        lvSearchResult.setOnItemClickListener(this);

        // Set adapter to listView
        lvSearchResult.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>())
        );

        if (savedInstanceState != null) {
            searchGeonamesPlace = savedInstanceState.getParcelableArrayList("searchGeonamesPlace");
            weatherPlaces = savedInstanceState.getParcelableArrayList("weatherPlaces");

            ArrayAdapter<String> adapter = (ArrayAdapter<String >) lvSearchResult.getAdapter();
            for (GeonamesPlace geonamesPlace : searchGeonamesPlace) {
                adapter.add(geonamesPlace.toString());
            }
            adapter.notifyDataSetChanged();
        } else {
            searchGeonamesPlace = new ArrayList<>();
            weatherPlaces = new ArrayList<>();
        }
    }

    // TODO Activity 3 - Lifecycle save data when rotate screen
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList("searchGeonamesPlace", searchGeonamesPlace);
        outState.putParcelableArrayList("weatherPlaces", weatherPlaces);
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("deprecation")
    private Boolean isNetWorkAvaible() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null)  {
                return false;
            }

            NetworkCapabilities actNetwork = connectivityManager.getNetworkCapabilities(network);

            return actNetwork != null && (actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    private void startBackgroundMainTask(final URL url) {
        final int CONNECTION_TIMEOUT = 10000;
        final int READ_TIMEOUT = 7000;

        // TODO Activity 2 - Add a ProgressBar
        progressBar.setVisibility(View.VISIBLE);

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
                    resultData = getDataGeonamesPlace(urlConnection);
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

                // Finally, update UI
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (resultData == FOUND_JSON || resultData == NO_FOUND_JSON) {
                            // Set new results to list adapter
                            ArrayAdapter<String> adapter = (ArrayAdapter<String >) lvSearchResult.getAdapter();
                            adapter.clear();
                            if (resultData == NO_FOUND_JSON) {
                                adapter.add("No information found at geonames");
                            } else {
                                for (GeonamesPlace geonamesPlace : searchGeonamesPlace) {
                                    adapter.add(geonamesPlace.toString());
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            // Use if possible ApplicationContext
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Not posible to contact " + URL_GEONAMES,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });
            }
        });
    }

    private void startBackgroundWeatherTask(final URL url) {
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
                    resultData = getDataWeather(urlConnection);
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

    private int getDataGeonamesPlace(HttpURLConnection urlConnection) throws IOException, JSONException {
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
                    searchGeonamesPlace.add(geonamesPlace);
                    buildWeatherPlace(geonamesPlace);
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

    private int getDataWeather(HttpURLConnection urlConnection) throws IOException, JSONException {
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

            WeatherPlace weatherPlace = new WeatherPlace(lat, lon, temp, humidity, description);
            weatherPlaces.add(weatherPlace);

            return FOUND_JSON;
        } else {
            Log.i("URL", "ErrorCode: " + urlConnection.getResponseCode());
            // searchResult.add("HttpURLConnection ErrorCode: " + urlConnection.getResponseCode());
            return ERROR_JSON;
        }
    }

    private void hideSoftKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Read from url connection
    private String readStream(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String nextLine = "";
        while ((nextLine = reader.readLine()) != null) {
            stringBuilder.append(nextLine);
        }

        return stringBuilder.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // shutdown() method will allow previously submitted tasks to execute before terminating,
        // shutdownNow() method prevents waiting tasks from starting and attempts to stop currently executing tasks
        if (executor != null) {
            executor.shutdown();
            Log.i("EXECUTOR", "ALL TASKS CANCELLED !!!!!!");
        }
    }

    @Override
    public void onClick(View v) {
        if (isNetWorkAvaible()) {
            // Check if user has written a place
            String place = etPlaceName.getText().toString();

            if (!place.isEmpty()) {
                buildGeonamesPlace(place);
            } else {
                Toast.makeText(this, "Write a place to search", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Sorry, network is not available", Toast.LENGTH_LONG).show();
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
            startBackgroundMainTask(url);
            hideSoftKeyboard();
        } catch (MalformedURLException e) {
            Log.i("URL", e.getMessage());
        }
    }

    private void buildWeatherPlace(GeonamesPlace geonamesPlace) {
        if (isNetWorkAvaible()) {
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
                startBackgroundWeatherTask(url);
            } catch (MalformedURLException e) {
                Log.i("URL", e.getMessage());
            }
        } else {
            Toast.makeText(this, "Sorry, network is not available", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, weatherPlaces.get(position).toString(), Toast.LENGTH_LONG).show();
    }

}