package com.example.refapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import org.codehaus.jackson.annotate.JsonProperty;

public class BingResult implements Parcelable{

    @JsonProperty("Title")
    public String title;
    
    @JsonProperty("Description")
    public String description;
    
    @JsonProperty("Url")
    public String url;
    
    @JsonProperty("CacheUrl")
    public String cacheUrl;
    
    @JsonProperty("DisplayUrl")
    public String displayUrl;
    
    @JsonProperty("DateTime")
    public String dateTime;

    @JsonProperty("DeepLinks")
    public DeepLink[] deepLinks;

    public BingResult() {}

    public BingResult(Parcel src) {
        title = src.readString();
        description = src.readString();
        url = src.readString();
        cacheUrl = src.readString();
        displayUrl = src.readString();
        dateTime = src.readString();
        deepLinks = src.createTypedArray(DeepLink.CREATOR);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(url);
        parcel.writeString(cacheUrl);
        parcel.writeString(displayUrl);
        parcel.writeString(dateTime);
        parcel.writeTypedArray(deepLinks,0);
    }

    public static final Parcelable.Creator<BingResult> CREATOR = new Parcelable.Creator<BingResult>() {
        public BingResult createFromParcel(Parcel in) {
            return new BingResult(in);
        }

        public BingResult[] newArray(int size) {
            return new BingResult[size];
        }
    };
}
