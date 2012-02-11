package com.example.refapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.example.refapp.services.ParamType;

import java.util.ArrayList;

public class SearchCriteria implements Parcelable {
    public static final String SEARCH_TYPE_WEB = "web";
    public static final String BING_APP_ID = "0BFE168E13D637883346DBDF05B1DCCEFD7AB9F5";
    public static final String PAGE_SIZE = "25";


    private String searchText;

    public SearchCriteria(String searchQuery) {
        this.searchText = searchQuery;
    }

    public SearchCriteria(Parcel src) {
        searchText = src.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(searchText);
    }

    public ArrayList<RequestParam> toRequestParams(int noOfItemsToBeSkipped) {
        ArrayList<RequestParam> params = new ArrayList<RequestParam>();
        params.add(new RequestParam(ParamType.APP_ID,BING_APP_ID));
        params.add(new RequestParam(ParamType.SOURCES,SEARCH_TYPE_WEB));
        params.add(new RequestParam(ParamType.SEARCH_TEXT,searchText));
        params.add(new RequestParam(ParamType.COUNT,PAGE_SIZE));
        params.add(new RequestParam(ParamType.OFFSET,Integer.toString(noOfItemsToBeSkipped)));
        return params;
    }

    public static final Parcelable.Creator<SearchCriteria> CREATOR = new Parcelable.Creator<SearchCriteria>() {
        public SearchCriteria createFromParcel(Parcel in) {
            return new SearchCriteria(in);
        }

        public SearchCriteria[] newArray(int size) {
            return new SearchCriteria[size];
        }
    };
}