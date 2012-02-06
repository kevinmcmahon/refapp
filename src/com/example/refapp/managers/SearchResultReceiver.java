package com.example.refapp.managers;

import android.app.Activity;
import android.os.Handler;
import com.example.refapp.models.BingData;
import com.example.refapp.models.SearchCriteria;
import com.example.refapp.services.DataRequestResultReceiver;

abstract public class SearchResultReceiver<TActivity extends Activity> extends
        DataRequestResultReceiver<TActivity, BingData> {

    public SearchCriteria searchCriteria;

    public SearchResultReceiver(TActivity tActivity, Handler handler) {
        super(tActivity, handler);
    }
}
