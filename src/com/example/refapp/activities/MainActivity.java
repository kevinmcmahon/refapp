package com.example.refapp.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.example.refapp.R;
import com.example.refapp.managers.SearchManager;
import com.example.refapp.models.SearchCriteria;
import com.example.refapp.ui.DialogHelper;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

@ContentView(R.layout.main)
public class MainActivity extends RoboActivity {

    @InjectView(tag = "search_query")
    EditText searchQueryEditText;

    @InjectView(R.id.btn_search)
    Button searchButton;

    @InjectView(R.id.adv_options_layout)
    RadioGroup advOptionsLayout;

    @InjectView(R.id.btn_adv_options)
    Button advOptionsButton;

    @Inject
    private SearchManager searchManager;

    private SearchManager.InitialSearchResultReceiver searchResultReceiver;
    private boolean toggle = true;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchResultReceiver = new SearchManager.InitialSearchResultReceiver(this, new Handler());

        // Wire UI interaction
        searchQueryEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
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

        advOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (advOptionsLayout.getVisibility() == View.VISIBLE) {
                    advOptionsLayout.setVisibility(View.GONE);
                }
                else {
                    advOptionsLayout.setVisibility(View.VISIBLE);
                }
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
        if (searchResultReceiver != null)
            searchResultReceiver.dispose();

        searchResultReceiver = new SearchManager.InitialSearchResultReceiver(this, new Handler());
    }

    @Override
    protected void onDestroy() {

        if (searchResultReceiver != null) {
            searchResultReceiver.clearContext();
        }

        super.onDestroy();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return onCreateDialog(id, null);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        return DialogHelper.createDialog(this, id, args);
    }

    void doSearch(String searchText) {
        Ln.d("Do search");

        SearchCriteria searchCriteria = new SearchCriteria(searchText);
        searchManager.performSearch(searchCriteria, 0, searchResultReceiver);
    }
}