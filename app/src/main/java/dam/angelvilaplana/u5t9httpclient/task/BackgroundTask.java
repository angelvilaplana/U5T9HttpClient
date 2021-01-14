package dam.angelvilaplana.u5t9httpclient.task;

import android.app.Application;
import android.content.Context;
import android.net.*;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
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

public class BackgroundTask {

    private final Application application;

    public final static int FOUND_JSON = 1;
    public final static int NO_FOUND_JSON = 2;
    public final static int ERROR_JSON = 3;

    public BackgroundTask(Application application) {
        this.application = application;
    }

    @SuppressWarnings("deprecation")
    private Boolean isNetWorkAvaible() {
        ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);

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

    // Read from url connection
    public String readStream(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String nextLine = "";
        while ((nextLine = reader.readLine()) != null) {
            stringBuilder.append(nextLine);
        }

        return stringBuilder.toString();
    }

}
