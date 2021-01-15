package dam.angelvilaplana.u5t9httpclient.connection;

import android.util.Log;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class Connection {

    public final static int FOUND_JSON = 1;
    public final static int NO_FOUND_JSON = 2;
    public final static int ERROR_JSON = 3;

    private int resultData;

    public void runConnection(final URL url) {
        final int CONNECTION_TIMEOUT = 10000;
        final int READ_TIMEOUT = 7000;
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
        } catch (JSONException e) {
            Log.i("JSONException", e.getMessage());
            // searchResult.add("JSONException: " + e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    // Read from url connection
    protected String readStream(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String nextLine = "";
        while ((nextLine = reader.readLine()) != null) {
            stringBuilder.append(nextLine);
        }

        return stringBuilder.toString();
    }

    protected abstract int getData(HttpURLConnection urlConnection) throws IOException, JSONException;

    public int getResultData() {
        return resultData;
    }

}
