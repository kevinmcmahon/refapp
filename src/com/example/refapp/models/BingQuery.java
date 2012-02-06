package com.example.refapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import org.codehaus.jackson.annotate.JsonProperty;

public class BingQuery implements Parcelable {

    @JsonProperty("SearchTerms")
    public String searchTerms;

    public BingQuery() {}

    public BingQuery(Parcel src) {
        searchTerms = src.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(searchTerms);
    }

    public static final Parcelable.Creator<BingQuery> CREATOR = new Parcelable.Creator<BingQuery>() {
        public BingQuery createFromParcel(Parcel in) {
            return new BingQuery(in);
        }

        public BingQuery[] newArray(int size) {
            return new BingQuery[size];
        }
    };
}
