package com.example.refapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.example.refapp.R;
import com.example.refapp.activities.events.OnRetainLastNonConfigurationInstanceEvent;
import com.example.refapp.managers.SearchManager;
import com.example.refapp.managers.SearchResultReceiver;
import com.example.refapp.models.BingData;
import com.example.refapp.models.BingResult;
import com.example.refapp.models.SearchCriteria;
import com.example.refapp.ui.AutoPagingAdapter;
import com.example.refapp.ui.ViewFactory;
import com.example.refapp.utils.CollectionUtils;
import com.example.refapp.utils.IRefreshable;
import com.example.refapp.utils.constants.Constants;
import com.google.inject.Inject;
import roboguice.activity.RoboListActivity;
import roboguice.activity.event.OnCreateEvent;
import roboguice.event.Observes;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BingSearchResultsActivity extends RoboListActivity {

    @InjectView(android.R.id.list)
    protected ListView listView;

    @InjectExtra(Constants.EXTRA_SEARCH_CRITERIA)
    protected SearchCriteria searchCriteria;

    @InjectExtra(Constants.EXTRA_SEARCH_RESULT)
    protected BingData firstSearchResult;

    @Inject
    SearchManager searchManager;

    private List<BingResult> resultList;

    private BingResultReceiver searchResultReceiver;
    private AutoPagingAdapter pagingAdapter;
    private static final String KEY_SEARCH_RESULT_RECEIVER = "searchResultReceiver";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bing_list);

        pagingAdapter = new AutoPagingAdapter<BingSearchResultAdapter>(this,
                new BingSearchResultAdapter(this, resultList, createBingResultViewFactory()),
                R.layout.pending_search_result);

        pagingAdapter.setOnLoadMoreListener(new AutoPagingAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(AutoPagingAdapter adapter, int currentSize) {
                Ln.d("Request for more... next starting index[%d]", currentSize);
                loadNextResultPage();
            }
        });

        listView.setAdapter(pagingAdapter);
        listView.setItemsCanFocus(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                BingResult result = resultList.get(position);
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result.url)));
                Intent i = new Intent(BingSearchResultsActivity.this,WebResultDetailActivity.class);
                i.putExtra(Constants.EXTRA_BING_WEB_RESULT,result);
                startActivity(i);
            }
        });

        if (firstSearchResult != null) {
            refreshResultStatus();

            if (CollectionUtils.isEmpty(resultList)) {
                appendResult(firstSearchResult);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(Constants.EXTRA_SEARCH_RESULT, (ArrayList<BingResult>) resultList);
    }

    private ViewFactory<BingResult> createBingResultViewFactory() {
        return new BingResultViewFactory();
    }

    private void handleSearchResult(BingData searchResult) {
        if (searchResult != null) {
            appendResult(searchResult);
        }
    }

    private void appendResult(BingData searchResult) {
        if (searchResult.searchResponse.webResults != null
                && !CollectionUtils.isEmpty(searchResult.searchResponse.webResults.results)) {
            resultList.addAll(Arrays.asList(searchResult.searchResponse.webResults.results));
            refreshList();
        }
    }

    void refreshList() {
        ((ArrayAdapter<BingResult>) pagingAdapter.getWrappedAdapter()).notifyDataSetChanged();

    }

    void loadNextResultPage() {
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

    protected void refreshResultStatus() {

        pagingAdapter.fullListCount = getFullResultCount();
    }

    protected int getFullResultCount() {
        return firstSearchResult == null || firstSearchResult.searchResponse.webResults == null ? 0 : firstSearchResult.searchResponse.webResults.totalResults;
    }

    private void restoreActivityState(@Observes OnCreateEvent event) {

        Bundle savedInstanceState = event.getSavedInstanceState();

        if (savedInstanceState != null) {
            resultList = savedInstanceState.getParcelableArrayList(Constants.EXTRA_SEARCH_RESULT);
        }

        if (resultList == null) {
            resultList = new ArrayList<BingResult>();
        }

        // Restore saved instance state
        // Restore non configuration instance
        Map<String, Object> instanceMap = getLastNonConfigurationInstanceMap();

        if (instanceMap != null) {
            searchResultReceiver =
                    (BingResultReceiver) instanceMap.get(KEY_SEARCH_RESULT_RECEIVER);
        }

        if (searchResultReceiver == null) {
            searchResultReceiver = new BingResultReceiver(this, new Handler());
        }
    }

    private void retainNonConfigurationInstance(@Observes OnRetainLastNonConfigurationInstanceEvent event) {
        event.instanceMap.put(KEY_SEARCH_RESULT_RECEIVER, searchResultReceiver);
    }

    /**
     * Do not call getLastNonConfigurationInstance directly, use getLastNonConfigurationInstanceMap instead
     *
     * @return Map of non-configuration instances
     */
    @SuppressWarnings({"unchecked"})
    public Map<String, Object> getLastNonConfigurationInstanceMap() {
        Object obj = getLastNonConfigurationInstance();

        if (obj == null) return null;

        assert obj instanceof Map;
        return (Map<String, Object>) obj;
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
        protected TextView txtDesc;
        protected BingResult data;

        public BingResultViewHolder(View parent) {
            txtTitle = (TextView) parent.findViewById(R.id.txt_title);
            txtDesc = (TextView) parent.findViewById(R.id.txt_desc);
        }

        @Override
        public void update(BingResult data) {
            this.data = data;
            refresh();
        }

        @Override
        public void refresh() {
            txtTitle.setText(data.title);
            txtDesc.setText(data.displayUrl);
        }
    }
}