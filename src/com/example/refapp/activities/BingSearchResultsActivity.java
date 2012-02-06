package com.example.refapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.example.refapp.R;
import com.example.refapp.managers.SearchManager;
import com.example.refapp.managers.SearchResultReceiver;
import com.example.refapp.models.BingData;
import com.example.refapp.models.BingResult;
import com.example.refapp.models.SearchCriteria;
import com.example.refapp.models.SearchResponse;
import com.example.refapp.ui.AutoPagingAdapter;
import com.example.refapp.ui.ViewFactory;
import com.example.refapp.utils.CollectionUtils;
import com.example.refapp.utils.IRefreshable;
import com.example.refapp.utils.constants.Constants;
import com.google.inject.Inject;
import roboguice.activity.RoboListActivity;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

import java.util.Arrays;
import java.util.List;

public class BingSearchResultsActivity extends RoboListActivity {

    @InjectView(android.R.id.list)
    protected ListView listView;

    @InjectExtra(Constants.EXTRA_SEARCH_CRITERIA)
    protected SearchCriteria searchCriteria;

    @Inject
    SearchManager searchManager;

    @InjectExtra(Constants.EXTRA_SEARCH_RESULT)
    protected SearchResponse firstSearchResult;

    private List<BingResult> resultList;
    private AutoPagingAdapter<BingSearchResultAdapter> pagingAdapter;

    private BingResultReceiver searchResultReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bing_list);
        resultList = Arrays.asList(firstSearchResult.webResults.results);

        listView.setAdapter(new BingSearchResultAdapter(this, resultList, createBingResultViewFactory()));
        listView.setItemsCanFocus(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                BingResult result = resultList.get(position);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result.url)));
            }
        });
    }

    private ViewFactory<BingResult> createBingResultViewFactory() {
        return new BingResultViewFactory();
    }

    private void handleSearchResult(BingData searchResult) {
        if (searchResult != null) {
            appendResult(searchResult);
        }

        refreshResultStatus();
    }

    private void appendResult(BingData searchResult) {
        if (searchResult.searchResponse.webResults != null
                && !CollectionUtils.isEmpty(searchResult.searchResponse.webResults.results)) {
            resultList.addAll(Arrays.asList(searchResult.searchResponse.webResults.results));
            refreshList();
        }
    }

    private void refreshList() {

    }

    private void refreshResultStatus() {

    }

    protected void loadNextResultPage() {
        searchManager.performSearch(searchCriteria,
                resultList.size(),
                searchResultReceiver);
    }

    static class BingResultReceiver extends SearchResultReceiver<BingSearchResultsActivity> {
        BingResultReceiver(BingSearchResultsActivity activity, Handler handler) {
            super(activity, handler);
        }

        @Override
        protected void onPreExecute(BingSearchResultsActivity activity) {
            super.onPreExecute(activity);
            activity.showDialog(R.id.dialog_id_loading);
        }

        @Override
        protected void onSuccess(BingSearchResultsActivity activity, BingData searchResult) {
            activity.handleSearchResult(searchResult);
        }

        @Override
        protected void onPostExecute(BingSearchResultsActivity activity) {
            try {
                activity.removeDialog(R.id.dialog_id_loading);
            } catch (Throwable t) {
                // Do nothing
            }
            super.onPostExecute(activity);
        }
    }

    static class BingSearchResultAdapter extends ArrayAdapter<BingResult> {
        private final ViewFactory<BingResult> viewFactory;

        public BingSearchResultAdapter(Context context, List<BingResult> objects, ViewFactory<BingResult> viewFactory) {
            super(context, android.R.id.text1, objects);
            this.viewFactory = viewFactory;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BingResultViewHolder viewHolder;
            if (convertView == null) {
                Context context = getContext();
                convertView = viewFactory.getView(context);
            }

            viewHolder = (BingResultViewHolder) convertView.getTag();
            viewHolder.update(getItem(position));
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }

    static protected class BingResultViewFactory extends ViewFactory<BingResult> {
        public BingResultViewFactory() {
            super(R.layout.bing_list_row);
        }

        @Override
        protected ViewHolder<BingResult> newViewHolder(View parent) {
            return new BingResultViewHolder(parent);
        }
    }

    static protected class BingResultViewHolder extends ViewFactory.ViewHolder<BingResult> implements IRefreshable {
        protected TextView txtTitle;
        protected BingResult data;

        public BingResultViewHolder(View parent) {
            txtTitle = (TextView) parent.findViewById(R.id.txt_title);
        }

        @Override
        public void update(BingResult data) {
            this.data = data;
            refresh();
        }

        @Override
        public void refresh() {
            txtTitle.setText(data.title);
        }
    }
}