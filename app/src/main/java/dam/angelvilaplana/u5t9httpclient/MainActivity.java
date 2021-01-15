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
import dam.angelvilaplana.u5t9httpclient.connection.Connection;
import dam.angelvilaplana.u5t9httpclient.connection.GeonamesPlaceConnection;
import dam.angelvilaplana.u5t9httpclient.connection.WeatherPlaceConnection;
import dam.angelvilaplana.u5t9httpclient.model.GeonamesPlace;
import dam.angelvilaplana.u5t9httpclient.model.WeatherPlace;

import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    // Constants
    private final static String URL_GEONAMES = "http://api.geonames.org/wikipediaSearchJSON";
    private final static String URL_OPENWEATHERMAP = "http://api.openweathermap.org/data/2.5/weather";
    private final static String USER_NAME = "angelvil";
    private final static int ROWS = 10;
    private final static String LANGUAGE = "es";
    private final static String APP_ID = "65d039bf11a1067cd648158ffc82bbf5";

    // Attributes
    private EditText etPlaceName;
    private Button btSearch;
    private ListView lvSearchResult;
    private ProgressBar progressBar;
    private ExecutorService executor;
    private GeonamesPlaceConnection geonamesPlaceConnection;
    private WeatherPlaceConnection weatherPlaceConnection;
    private ArrayList<GeonamesPlace> geonamesPlacesList;
    private WeatherPlace[] weatherPlaceArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        geonamesPlaceConnection = new GeonamesPlaceConnection(LANGUAGE, ROWS, USER_NAME);
        weatherPlaceConnection = new WeatherPlaceConnection(APP_ID);

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
            geonamesPlacesList = savedInstanceState.getParcelableArrayList("geonamesPlacesList");
            weatherPlaceArray = (WeatherPlace[]) savedInstanceState.getParcelableArray("weatherPlaceArray");

            ArrayAdapter<String> adapter = (ArrayAdapter<String>) lvSearchResult.getAdapter();
            for (GeonamesPlace geonamesPlace : geonamesPlacesList) {
                adapter.add(geonamesPlace.toString());
            }
            adapter.notifyDataSetChanged();
        } else {
            geonamesPlacesList = new ArrayList<>();
        }
    }

    // TODO Activity 3 - Lifecycle save data when rotate screen
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList("geonamesPlacesList", geonamesPlacesList);
        outState.putParcelableArray("weatherPlaceArray", weatherPlaceArray);
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

    private void updateAdapter(int resultData) {
        if (resultData == Connection.FOUND_JSON || resultData == Connection.NO_FOUND_JSON) {
            // Set new results to list adapter
            ArrayAdapter<String> adapter = (ArrayAdapter<String >) lvSearchResult.getAdapter();
            adapter.clear();
            if (resultData == Connection.NO_FOUND_JSON) {
                adapter.add("No information found at geonames");
            } else {
                geonamesPlacesList = geonamesPlaceConnection.getGeonamesPlaceList();
                weatherPlaceArray = new WeatherPlace[geonamesPlacesList.size()];
                for (GeonamesPlace geonamesPlace : geonamesPlacesList) {
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
                    "Not posible to contact " + URL_OPENWEATHERMAP,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void startBackgroundMainTask(final URL url) {
        // TODO Activity 2 - Add a ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                geonamesPlaceConnection.runConnection(url);
                final int resultData = geonamesPlaceConnection.getResultData();

                // Finally, update UI
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateAdapter(resultData);
                    }
                });
            }
        });
    }

    private void startBackgroundWeatherTask(final int position) {
        executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                URL url = weatherPlaceConnection.build(geonamesPlacesList.get(position));
                weatherPlaceConnection.runConnection(url);
                final int resultData = geonamesPlaceConnection.getResultData();

                if (resultData == Connection.FOUND_JSON || resultData == Connection.NO_FOUND_JSON) {
                    weatherPlaceArray[position] = weatherPlaceConnection.getWeatherPlace();
                } else {
                    progressBar.setVisibility(View.GONE);
                    // Use if possible ApplicationContext
                    Toast.makeText(
                            getApplicationContext(),
                            "Not posible to contact " + URL_GEONAMES,
                            Toast.LENGTH_LONG
                    ).show();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (weatherPlaceArray[position] != null) {
                            Toast.makeText(getApplicationContext(), weatherPlaceArray[position].toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private void hideSoftKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
                URL url = geonamesPlaceConnection.build(place);
                if (url != null) {
                    hideSoftKeyboard();
                    startBackgroundMainTask(url);
                }
            } else {
                Toast.makeText(this, "Write a place to search", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Sorry, network is not available", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if (weatherPlaceArray[position] == null) {
            startBackgroundWeatherTask(position);
        } else {
            Toast.makeText(this, weatherPlaceArray[position].toString(), Toast.LENGTH_LONG).show();
        }
    }

}