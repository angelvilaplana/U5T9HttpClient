package dam.angelvilaplana.u5t9httpclient.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.Locale;

public class WeatherPlace implements Parcelable {

    private double lat;

    private double lng;

    private double temp;

    private int humidity;

    private String description;

    public WeatherPlace(double lat, double lng, double temp, int humidity, String description) {
        this.lat = lat;
        this.lng = lng;
        this.temp = temp;
        this.humidity = humidity;
        this.description = description;
    }

    protected WeatherPlace(Parcel in) {
        lat = in.readDouble();
        lng = in.readDouble();
        temp = in.readDouble();
        humidity = in.readInt();
        description = in.readString();
    }

    public static final Creator<WeatherPlace> CREATOR = new Creator<WeatherPlace>() {
        @Override
        public WeatherPlace createFromParcel(Parcel in) {
            return new WeatherPlace(in);
        }

        @Override
        public WeatherPlace[] newArray(int size) {
            return new WeatherPlace[size];
        }
    };

    @Override
    @NonNull
    public String toString() {
        return String.format(Locale.getDefault(),
                "Weather conditions for {%.2f, %.2f} \n" +
                "TEMP: %.2f Cº \n" +
                "HUMIDITY: %d %% \n" +
                "%s", lat, lng, temp, humidity, description);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeDouble(temp);
        dest.writeInt(humidity);
        dest.writeString(description);
    }

}
