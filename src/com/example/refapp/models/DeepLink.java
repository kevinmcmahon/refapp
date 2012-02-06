package com.example.refapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import org.codehaus.jackson.annotate.JsonProperty;

public class DeepLink  implements Parcelable{

    @JsonProperty("Title")
    public String title;

    @JsonProperty("Url")
    public String url;

    public DeepLink() {}

    public DeepLink(Parcel src) {
        title = src.readString();
        url = src.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(url);
    }

    public static final Parcelable.Creator<DeepLink> CREATOR = new Parcelable.Creator<DeepLink>() {
        public DeepLink createFromParcel(Parcel in) {
            return new DeepLink(in);
        }

        public DeepLink[] newArray(int size) {
            return new DeepLink[size];
        }
    };
}
