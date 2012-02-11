package com.example.refapp.managers;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import com.example.refapp.R;
import com.example.refapp.activities.BingSearchResultsActivity;
import com.example.refapp.models.BingData;
import com.example.refapp.models.RequestParam;
import com.example.refapp.models.SearchCriteria;
import com.example.refapp.services.DataRequestType;
import com.example.refapp.services.DataService;
import com.example.refapp.utils.constants.Constants;
import com.google.inject.Singleton;
import roboguice.util.Ln;

import java.util.ArrayList;

@Singleton
public class SearchManager {

    public void performSearch(SearchCriteria searchCriteria, int noOfItemsToBeSkipped, SearchResultReceiver resultReceiver) {
        resultReceiver.searchCriteria = searchCriteria;

        ArrayList<RequestParam> params = searchCriteria.toRequestParams(noOfItemsToBeSkipped);
        DataService.start(DataRequestType.WEB_SEARCH, resultReceiver, params);
    }

    static public class InitialSearchResultReceiver<TActivity extends Activity> extends
            SearchResultReceiver<TActivity> {
        public int loadingDialogResId = R.id.dialog_id_searching;

        public InitialSearchResultReceiver(TActivity activity, Handler handler) {
            super(activity, handler);
        }

        @Override
        protected void onPreExecute(TActivity activity) {
            activity.showDialog(loadingDialogResId);
        }

        @Override
        protected void onSuccess(TActivity activity, BingData searchResult) {
            startNextActivity(activity, searchResult);
        }

        protected void startNextActivity(TActivity activity, BingData searchResult) {

            Intent intent = new Intent(activity, BingSearchResultsActivity.class);
            intent.putExtra(Constants.EXTRA_SEARCH_CRITERIA, searchCriteria);
            intent.putExtra(Constants.EXTRA_SEARCH_RESULT, searchResult.searchResponse);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
        }

        @Override
        protected void onPostExecute(TActivity activity) {
            try {
                activity.removeDialog(loadingDialogResId);
            } catch (Throwable t) {
                // Do nothing
                Ln.w(t, "Error missing dialog");
            }
        }
    }
}