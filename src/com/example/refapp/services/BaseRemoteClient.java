package com.example.refapp.services;

import android.app.Application;
import android.net.Uri;
import android.util.TimingLogger;
import com.example.refapp.models.RequestParam;
import com.example.refapp.utils.CollectionUtils;
import com.example.refapp.utils.constants.Config;
import com.example.refapp.utils.errors.ErrorType;
import com.example.refapp.utils.errors.RefAppException;
import com.example.refapp.utils.http.HttpClientProvider;
import com.example.refapp.utils.json.JsonSerializer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import roboguice.util.Ln;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
public class BaseRemoteClient implements RemoteClient {
    private static final String TAG_TIMING = "RemoteClient";

    private final HttpClientProvider httpProvider;
    private final Provider<Application> applicationProvider;

    @Inject
    protected JsonSerializer serializer;

    private final ReentrantReadWriteLock httpClientLockPair = new ReentrantReadWriteLock();

    @Inject
    BaseRemoteClient(Provider<Application> applicationProvider,
                     HttpClientProvider httpProvider) {
        this.httpProvider = httpProvider;
        this.applicationProvider = applicationProvider;
    }

    public <T> T execute(final DataRequestType requestType, List<RequestParam> requestParams, Class<? extends T> resultType)
            throws Exception {
        String fullUri = CollectionUtils.isEmpty(requestParams)?
                requestType.getServicePath(applicationProvider.get()) :
                requestType.getServicePath(applicationProvider.get(), requestParams);

        String postBody = null;
        int inPathParamCount = requestType.inPathParams.size();

        // Add additional params to URI
        if (!CollectionUtils.isEmpty(requestParams) && inPathParamCount != requestParams.size()) {
            Uri newUri = Uri.parse(fullUri);
            Uri.Builder newUriBuilder = newUri.buildUpon();
            for (RequestParam eachParam : requestParams) {
                ParamType type = eachParam.type;

                if (type == ParamType.POST_BODY) {
                    postBody = eachParam.value;
                }  else if (inPathParamCount == 0 || !requestType.inPathParams.contains(type)) {
                    newUriBuilder.appendQueryParameter(type.paramName, eachParam.value);
                }
            }
            fullUri = newUriBuilder.build().toString();
        }

        fullUri = touchUpUrl(fullUri, requestType, requestParams);

        Ln.d("url[%s]", fullUri);

        HttpUriRequest request = null;
        if (postBody == null) {
            // Get request
            request = new HttpGet(fullUri);
        } else {
            // Post request
            HttpPost httpPost = new HttpPost(fullUri);
            httpPost.setEntity(new StringEntity(postBody, HTTP.UTF_8));
            request = httpPost;

            if (Config.IS_LOGGABLE) {
                Ln.d("PostBody[%s]", postBody);
            }
        }

        configureHttpRequestBeforeExecution(request, requestType, requestParams);

        return executeRequest(request, resultType);
    }

    protected String touchUpUrl(String fullUrl, final DataRequestType requestType, final List<RequestParam> requestParams) {
        return fullUrl;
    }

    protected void configureHttpRequestBeforeExecution(HttpUriRequest httpUriRequest,
                                                       final DataRequestType requestType,
                                                       final List<RequestParam> requestParams) {
        // Do nothing
    }

    private <T> T executeRequest(HttpUriRequest request, Class<? extends T> resultType) {

        String fullUri = request.getURI().toString();
        if (Config.IS_LOGGABLE) {
            if (Ln.isDebugEnabled()) {
                for(Header header : request.getAllHeaders()) {
                    Ln.d("Request Header[%s:%s]", header.getName(), header.getValue());
                }
                Ln.d("executeRequest(): method[%s] uri[%s]", request.getMethod(), fullUri);
            }
        }

        T response = null;
        TimingLogger log = null;
        httpClientLockPair.readLock().lock();
        try {
            HttpClient httpClient = httpProvider.get();

            // handle redirects
            HttpClientParams.setRedirecting(httpClient.getParams(), true);

            HttpResponse httpResponse;

            log = new TimingLogger(TAG_TIMING, fullUri);

            httpResponse = httpClient.execute(request);
            log.addSplit("executed");

            int responseCode = httpResponse.getStatusLine().getStatusCode();
            if (Config.IS_LOGGABLE) {
                if (Ln.isDebugEnabled()) {
                    for(Header header : httpResponse.getAllHeaders()) {
                        Ln.d("Response Header[%s:%s]", header.getName(), header.getValue());
                    }
                }
            }

            if (responseCode != HttpStatus.SC_OK) {
                throw new RefAppException(ErrorType.SERVER_ERROR,"Web Service Call Error. HttpCode[" + responseCode + "]");
            }

            HttpEntity entity = httpResponse.getEntity();
            log.addSplit("deserialize");

            if (Config.IS_LOGGABLE) {
                Ln.w("JsonSerializer ResultType[%s]", resultType.getName());
            }
            if (String.class.isAssignableFrom(resultType)) {
                BufferedHttpEntity buffered = new BufferedHttpEntity(entity);

                String responseString = serializer.slurp(buffered.getContent());
                if (Config.IS_LOGGABLE) {
                    Ln.d("Response String[%s]", responseString);
                }
                //noinspection unchecked
                response = (T)responseString;
            } else {
                response = serializer.deserialize(entity, resultType);
            }
        } catch (ClientProtocolException e) {
            Ln.e(e);
            throw new RefAppException(ErrorType.CONNECTION_ERROR);
        } catch (IOException e) {
            Ln.e(e);
            throw new RefAppException(ErrorType.CONNECTION_ERROR);
        } finally {
            if (log != null) {
                log.dumpToLog();
            }
            httpClientLockPair.readLock().unlock();
        }
        Ln.d("returning response");
        return response;
    }

    @Override
    public void release() {
        httpClientLockPair.writeLock().lock();
        try {
            httpProvider.release();
        } finally {
            httpClientLockPair.writeLock().unlock();
        }
    }
}