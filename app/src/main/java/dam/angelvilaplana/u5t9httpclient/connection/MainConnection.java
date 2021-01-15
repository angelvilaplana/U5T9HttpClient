package dam.angelvilaplana.u5t9httpclient.connection;

public class MainConnection {

    private GeonamesPlaceConnection geonamesPlaceConnection;
    private WeatherPlaceConnection weatherPlaceConnection;

    public MainConnection(GeonamesPlaceConnection geonamesPlaceConnection, WeatherPlaceConnection weatherPlaceConnection) {
        this.geonamesPlaceConnection = geonamesPlaceConnection;
        this.weatherPlaceConnection = weatherPlaceConnection;
    }

    public GeonamesPlaceConnection getGeonamesPlaceConnection() {
        return geonamesPlaceConnection;
    }

    public WeatherPlaceConnection getWeatherPlaceConnection() {
        return weatherPlaceConnection;
    }

}
