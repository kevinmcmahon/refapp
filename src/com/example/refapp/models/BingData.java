package com.example.refapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import org.codehaus.jackson.annotate.JsonProperty;

public class BingData implements Parcelable {

    @JsonProperty("SearchResponse")
    public SearchResponse searchResponse;

    public BingData() {}

    public BingData(Parcel src) {
        searchResponse = src.readParcelable(SearchResponse.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(searchResponse,0);
    }

    public static final Parcelable.Creator<BingData> CREATOR = new Parcelable.Creator<BingData>() {
        public BingData createFromParcel(Parcel in) {
            return new BingData(in);
        }

        public BingData[] newArray(int size) {
            return new BingData[size];
        }
    };
}
