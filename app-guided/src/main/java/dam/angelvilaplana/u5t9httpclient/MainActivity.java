package dam.angelvilaplana.u5t9httpclient;

import android.content.Context;
import android.net.*;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Constants
    private final static String URL_GEONAMES = "http://api.geonames.org/wikipediaSearchJSON";
    private final static String USER_NAME = "angelvil";
    private final static int ROWS = 10;

    // Attributes
    private EditText etPlaceName;
    private Button btSearch;
    private ListView lvSearchResult;
    private ArrayList<String> listSearchResult;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUI();
    }

    private void setUI() {
        etPlaceName = findViewById(R.id.etPlaceName);
        btSearch = findViewById(R.id.btSearch);
        btSearch.setOnClickListener(this);

        // Create an empty ArrayList
        listSearchResult = new ArrayList<>();

        lvSearchResult = findViewById(R.id.lvSearchResult);

        // Set adapter to listView
        lvSearchResult.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                listSearchResult)
        );
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

    private void startBackgroundTask(final URL url) {
        final int CONNECTION_TIMEOUT = 10000;
        final int READ_TIMEOUT = 7000;

        executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                final  ArrayList<String> searchResult = new ArrayList<>();

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
                    getData(urlConnection, searchResult);
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
                        if (searchResult.size() > 0) {
                            // Set new results to list adapter
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) lvSearchResult.getAdapter();
                            adapter.clear();
                            adapter.addAll(searchResult);
                            adapter.notifyDataSetChanged();
                        } else {
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

    private void getData(HttpURLConnection urlConnection, ArrayList<String> searchResult) throws IOException, JSONException {
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
                    searchResult.add(item.getString("summary"));
                }
            } else {
                searchResult.add("No information found at geonames");
            }
        } else {
            Log.i("URL", "ErrorCode: " + urlConnection.getResponseCode());
            // searchResult.add("HttpURLConnection ErrorCode: " + urlConnection.getResponseCode());
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
                URL url;
                try {
                    // url = new URL(URL_GEONAMES + "?q=" + place + "&maxRows=" + ROWS + "&userName=" + USER_NAME);
                    // Use better a builder to avoid a malformed query string
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("http")
                            .authority("api.geonames.org")
                            .appendPath("wikipediaSearchJSON")
                            .appendQueryParameter("q", place)
                            .appendQueryParameter("maxRows", String.valueOf(ROWS))
                            .appendQueryParameter("username", USER_NAME);

                    url = new URL(builder.build().toString());
                    startBackgroundTask(url);
                } catch (MalformedURLException e) {
                    Log.i("URL", e.getMessage());
                }
            } else {
                Toast.makeText(this, "Write a place to search", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Sorry, network is not available", Toast.LENGTH_LONG).show();
        }
    }

}