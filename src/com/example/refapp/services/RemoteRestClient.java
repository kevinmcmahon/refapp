package com.example.refapp.services;

import android.app.Application;
import android.text.TextUtils;
import com.example.refapp.models.RequestParam;
import com.example.refapp.utils.http.HttpClientProvider;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import java.util.List;

@Singleton
public class RemoteRestClient extends BaseRemoteClient {
    private static final String HEADER_AUTHORIZATION = "Authorization";

    private static final long TOKEN_EXPIRES_IN = 5 * 60 * 1000; // in millisecond
    private String cachedToken = null;
    private long tokenTimestamp;

    @Inject
    RemoteRestClient(Provider<Application> applicationProvider, HttpClientProvider httpProvider) {
        super(applicationProvider, httpProvider);
    }

    @Override
    protected void configureHttpRequestBeforeExecution(HttpUriRequest httpUriRequest,
                                                       final DataRequestType requestType,
                                                       List<RequestParam> requestParams) {
        if (!TextUtils.isEmpty(requestType.contentType)) {
            httpUriRequest.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, requestType.contentType));
        }
    }
}