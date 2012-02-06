package com.example.refapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.refapp.R;
import com.example.refapp.managers.SearchManager;
import com.example.refapp.models.SearchCriteria;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

public class MainActivity extends RoboActivity
{
    @InjectView(R.id.search_text)
    EditText searchQueryEditText;

    @InjectView(R.id.btn_search)
    Button searchButton;

    @Inject
    private SearchManager searchManager;

    private SearchManager.InitialSearchResultReceiver searchResultReceiver;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        searchResultReceiver = new SearchManager.InitialSearchResultReceiver(this, new Handler());

        // Wire UI interaction
        searchQueryEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          android.view.KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    doSearch(v.getText().toString());
                    return true;
                }
                return false;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = searchQueryEditText.getText().toString();
                doSearch(query);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Reset the activity back to the original state
        reset();
    }

    private void reset() {
        if(searchResultReceiver != null)
            searchResultReceiver.dispose();

        Handler handler = new Handler();
        searchResultReceiver = new SearchManager.InitialSearchResultReceiver(this, handler); 
    }

    @Override
    protected void onDestroy() {

        if (searchResultReceiver != null) {
            searchResultReceiver.clearContext();
        }

        super.onDestroy();
    }

    void doSearch(String searchText) {
        Ln.d("Do search");

        SearchCriteria searchCriteria = new SearchCriteria(searchText);
        searchManager.performSearch(searchCriteria, 0, searchResultReceiver);
    }
}