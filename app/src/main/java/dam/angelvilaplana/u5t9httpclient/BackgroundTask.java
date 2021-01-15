package dam.angelvilaplana.u5t9httpclient;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import dam.angelvilaplana.u5t9httpclient.connection.Connection;
import dam.angelvilaplana.u5t9httpclient.connection.GeonamesPlaceConnection;
import dam.angelvilaplana.u5t9httpclient.connection.MainConnection;
import dam.angelvilaplana.u5t9httpclient.model.GeonamesPlace;
import dam.angelvilaplana.u5t9httpclient.model.WeatherPlace;

import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundTask {

    private final static String URL_GEONAMES = "http://api.geonames.org/wikipediaSearchJSON";
    private final static String URL_OPENWEATHERMAP = "http://api.openweathermap.org/data/2.5/weather";

    private final Context context;
    private ExecutorService executor;
    private final MainConnection mainConnection;
    private final ListView lvSearchResult;
    private final ProgressBar progressBar;
    private ArrayList<GeonamesPlace> geonamesPlacesList;
    private WeatherPlace[] weatherPlaceArray;

    public BackgroundTask(Context context, MainConnection mainConnection, ListView lvSearchResult, ProgressBar progressBar) {
        this.context = context;
        this.mainConnection = mainConnection;
        this.lvSearchResult = lvSearchResult;
        this.progressBar = progressBar;
        this.geonamesPlacesList = new ArrayList<>();
    }


    private void updateAdapter(int resultData) {
        if (resultData == Connection.FOUND_JSON || resultData == Connection.NO_FOUND_JSON) {
            // Set new results to list adapter
            ArrayAdapter<String> adapter = (ArrayAdapter<String >) lvSearchResult.getAdapter();
            adapter.clear();
            if (resultData == Connection.NO_FOUND_JSON) {
                adapter.add("No information found at geonames");
            } else {
                geonamesPlacesList = mainConnection.getGeonamesPlaceConnection().getGeonamesPlaceList();
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
                    context,
                    "Not posible to contact " + URL_GEONAMES,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    public void updateAdapter(Bundle savedInstanceState) {
        this.geonamesPlacesList = savedInstanceState.getParcelableArrayList("geonamesPlacesList");;
        this.weatherPlaceArray = (WeatherPlace[]) savedInstanceState.getParcelableArray("weatherPlaceArray");

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) lvSearchResult.getAdapter();
        for (GeonamesPlace geonamesPlace : geonamesPlacesList) {
            adapter.add(geonamesPlace.toString());
        }
        adapter.notifyDataSetChanged();
    }

    public void startBackgroundMainTask(final URL url) {
        // TODO Activity 2 - Add a ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                mainConnection.getGeonamesPlaceConnection().runConnection(url);
                final int resultData =  mainConnection.getGeonamesPlaceConnection().getResultData();

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

    public void startBackgroundWeatherTask(final int position) {
        executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                URL url = mainConnection.getWeatherPlaceConnection().build(geonamesPlacesList.get(position));
                mainConnection.getWeatherPlaceConnection().runConnection(url);
                final int resultData =  mainConnection.getWeatherPlaceConnection().getResultData();

                if (resultData == Connection.FOUND_JSON || resultData == Connection.NO_FOUND_JSON) {
                    weatherPlaceArray[position] = mainConnection.getWeatherPlaceConnection().getWeatherPlace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (weatherPlaceArray[position] != null) {
                            Toast.makeText(context, weatherPlaceArray[position].toString(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(
                                    context,
                                    "Not posible to contact " + URL_OPENWEATHERMAP,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });
            }
        });
    }

    public void shutDownExecutor() {
        // shutdown() method will allow previously submitted tasks to execute before terminating,
        // shutdownNow() method prevents waiting tasks from starting and attempts to stop currently executing tasks
        if (executor != null) {
            executor.shutdown();
        }
    }

    public GeonamesPlaceConnection getGeonamesPlaceConnection() {
        return mainConnection.getGeonamesPlaceConnection();
    }

    public ArrayList<GeonamesPlace> getGeonamesPlacesList() {
        return geonamesPlacesList;
    }

    public WeatherPlace[] getWeatherPlaceArray() {
        return weatherPlaceArray;
    }

}
