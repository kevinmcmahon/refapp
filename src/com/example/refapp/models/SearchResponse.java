package com.example.refapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import org.codehaus.jackson.annotate.JsonProperty;

public class SearchResponse implements Parcelable {

    @JsonProperty("Version")
    public String version;

    @JsonProperty("Query")
    public BingQuery query;

    @JsonProperty("Web")
    public BingWebResults webResults;

    public SearchResponse() {}

    public SearchResponse(Parcel src) {
        version = src.readString();
        query = src.readParcelable(BingQuery.class.getClassLoader());
        webResults = src.readParcelable(BingWebResults.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(version);
        parcel.writeParcelable(query,0);
        parcel.writeParcelable(webResults,0);
    }

    public static final Parcelable.Creator<SearchResponse> CREATOR = new Parcelable.Creator<SearchResponse>() {
        public SearchResponse createFromParcel(Parcel in) {
            return new SearchResponse(in);
        }

        public SearchResponse[] newArray(int size) {
            return new SearchResponse[size];
        }
    };
}