package dam.angelvilaplana.u5t9httpclient;

import android.content.Context;
import android.net.*;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import dam.angelvilaplana.u5t9httpclient.connection.GeonamesPlaceConnection;
import dam.angelvilaplana.u5t9httpclient.connection.MainConnection;
import dam.angelvilaplana.u5t9httpclient.connection.WeatherPlaceConnection;
import dam.angelvilaplana.u5t9httpclient.model.WeatherPlace;

import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    // Constants
    private final static String USER_NAME = "angelvil";
    private final static int ROWS = 10;
    private final static String LANGUAGE = "es";
    private final static String APP_ID = "65d039bf11a1067cd648158ffc82bbf5";

    // Attributes
    private EditText etPlaceName;
    private Button btSearch;
    private ListView lvSearchResult;
    private ProgressBar progressBar;
    private BackgroundTask backgroundTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GeonamesPlaceConnection geonamesPlaceConnection = new GeonamesPlaceConnection(LANGUAGE, ROWS, USER_NAME);
        WeatherPlaceConnection weatherPlaceConnection = new WeatherPlaceConnection(APP_ID);
        MainConnection mainConnection = new MainConnection(geonamesPlaceConnection, weatherPlaceConnection);

        setUI(savedInstanceState);

        // TODO Activity 3 - Lifecycle update data when rotate screen
        backgroundTask = new BackgroundTask(this, mainConnection, lvSearchResult, progressBar);
        if (savedInstanceState != null) {
            backgroundTask.updateAdapter(savedInstanceState);
        }
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
    }

    // TODO Activity 3 - Lifecycle save data when rotate screen
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList("geonamesPlacesList", backgroundTask.getGeonamesPlacesList());
        outState.putParcelableArray("weatherPlaceArray", backgroundTask.getWeatherPlaceArray());
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

    private void hideSoftKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // TODO Activity 3 - Lifecycle shutdown ExecutorService
    @Override
    protected void onDestroy() {
        super.onDestroy();
        backgroundTask.shutDownExecutor();
    }

    @Override
    public void onClick(View v) {
        if (isNetWorkAvaible()) {
            // Check if user has written a place
            String place = etPlaceName.getText().toString();

            if (!place.isEmpty()) {
                // TODO Activity 1 - Edit search, create GeonamesPlace object & hide keyboard
                URL url = backgroundTask.getGeonamesPlaceConnection().build(place);
                if (url != null) {
                    hideSoftKeyboard();
                    backgroundTask.startBackgroundMainTask(url);
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
        // TODO Activity 1 - Add WeatherPlace API
        WeatherPlace weatherPlace = backgroundTask.getWeatherPlaceArray()[position];
        if (weatherPlace == null) {
            backgroundTask.startBackgroundWeatherTask(position);
        } else {
            Toast.makeText(this, weatherPlace.toString(), Toast.LENGTH_LONG).show();
        }
    }

}
