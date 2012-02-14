package com.example.refapp.activities;


import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;
import com.example.refapp.R;
import com.example.refapp.models.BingResult;
import com.example.refapp.utils.constants.Constants;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

@ContentView(R.layout.web_result_detail)
public class WebResultDetailActivity extends RoboActivity {

    @InjectExtra(Constants.EXTRA_BING_WEB_RESULT)
    private BingResult webBingResult;

    @InjectView(R.id.txt_title)
    private TextView txtTitle;

    @InjectView(R.id.txt_desc)
    private TextView txtDescription;

    @InjectView(R.id.web_view)
    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        if(webBingResult != null) {
            txtTitle.setText(webBingResult.title);
            txtDescription.setText(webBingResult.description);
            webView.loadUrl(webBingResult.url);
        }
        else {
            Ln.w("Web detail result is NULL");
        }
    }
}
