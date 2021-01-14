package dam.angelvilaplana.u5t9httpclient.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class GeonamesPlace implements Parcelable {

    private String summary;

    private double lat;

    private double lng;

    public GeonamesPlace(String summary, double lat, double lng) {
        this.summary = summary;
        this.lat = lat;
        this.lng = lng;
    }

    protected GeonamesPlace(Parcel in) {
        summary = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
    }

    public static final Creator<GeonamesPlace> CREATOR = new Creator<GeonamesPlace>() {
        @Override
        public GeonamesPlace createFromParcel(Parcel in) {
            return new GeonamesPlace(in);
        }

        @Override
        public GeonamesPlace[] newArray(int size) {
            return new GeonamesPlace[size];
        }
    };

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    @Override
    @NonNull
    public String toString() {
        return summary + "\n" +
                "LAT = " + lat + ", LON = " + lng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(summary);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }

}
