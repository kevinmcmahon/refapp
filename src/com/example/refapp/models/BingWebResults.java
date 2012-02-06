package com.example.refapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import org.codehaus.jackson.annotate.JsonProperty;

public class BingWebResults implements Parcelable {

    @JsonProperty("Total")
    public int totalResults;

    @JsonProperty("Offset")
    public int offset;

    @JsonProperty("Results")
    public BingResult[] results;

    public BingWebResults() {}

    public BingWebResults(Parcel src) {
        totalResults = src.readInt();
        offset = src.readInt();
        results = src.createTypedArray(BingResult.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(totalResults);
        parcel.writeInt(offset);
        parcel.writeTypedArray(results,0);
    }

    public static final Parcelable.Creator<BingWebResults> CREATOR = new Parcelable.Creator<BingWebResults>() {
        public BingWebResults createFromParcel(Parcel in) {
            return new BingWebResults(in);
        }

        public BingWebResults[] newArray(int size) {
            return new BingWebResults[size];
        }
    };
}