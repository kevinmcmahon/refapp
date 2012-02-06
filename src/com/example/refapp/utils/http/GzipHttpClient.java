/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.refapp.utils.http;

import org.apache.http.*;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * <p>
 * Modified version of the AndroidHttpClient that adds handlers for automatic gzip and works on api level 4
 * <p/>
 * Subclass of the Apache {@link org.apache.http.impl.client.DefaultHttpClient} that is configured with
 * reasonable default settings and registered schemes for Android, and also lets
 * the user add {@link org.apache.http.HttpRequestInterceptor} classes. Don't create this
 * directly, use the {@link #newInstance} factory method.
 * </p>
 * <p/>
 * <p>
 * This client processes cookies but does not retain them by default. To retain
 * cookies, simply add a cookie store to the HttpContext:
 * <p/>
 * <pre>
 * context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
 * </pre>
 * <p/>
 * </p>
 */
public final class GzipHttpClient implements ExtendedHttpClient {

    final static HttpRequestInterceptor AddGzipToRequestInterceptor = new HttpRequestInterceptor() {
        public final void process(final HttpRequest request, final HttpContext context) throws HttpException,
				IOException {
            if (!request.containsHeader("Accept-Encoding")) {
                request.addHeader("Accept-Encoding", "gzip");
            }
        }
    };

    final static HttpResponseInterceptor AddGzipToResponseInterceptor = new HttpResponseInterceptor() {
        public final void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
            final HttpEntity entity = response.getEntity();
            final Header contentEncodingHeader = entity.getContentEncoding();
            if (contentEncodingHeader != null) {
                String contentEncoding = contentEncodingHeader.getValue();
                if (contentEncoding != null && contentEncoding.contains("gzip")) {
                    response.setEntity(new GzipDecompressingEntity(entity));
                }
            }
        }
    };

    private final static class DelegateHttpClient extends DefaultHttpClient {

        private DelegateHttpClient(final ClientConnectionManager ccm, final HttpParams params) {
            super(ccm, params);
            addRequestInterceptor(AddGzipToRequestInterceptor);
            addResponseInterceptor(AddGzipToResponseInterceptor);
        }

        @Override
        protected final HttpContext createHttpContext() {
            // Same as DefaultHttpClient.createHttpContext() minus the
            // cookie store.
            final HttpContext context = new BasicHttpContext();
            context.setAttribute(ClientContext.AUTHSCHEME_REGISTRY, getAuthSchemes());
            context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, getCookieSpecs());
            context.setAttribute(ClientContext.CREDS_PROVIDER, getCredentialsProvider());
            return context;
        }
    }

    final static class GzipDecompressingEntity extends HttpEntityWrapper {

        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public final InputStream getContent() throws IOException, IllegalStateException {

            // the wrapped entity's getContent() decides about repeatability
            final InputStream wrappedin = wrappedEntity.getContent();
            return new GZIPInputStream(wrappedin);
        }

        @Override
        public final long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }

    }

    /**
     * Create a new HttpClient with reasonable defaults (which you can update).
     *
     * @param userAgent to report in your HTTP requests.
     * @return AndroidHttpClient for you to use for all your requests.
     */
    public static GzipHttpClient newInstance(final String userAgent) {
        final HttpParams params = new BasicHttpParams();

        // Turn off stale checking. Our connections break all the time anyway,
        // and it's not worth it to pay the penalty of checking every time.
        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        // Default connection and socket timeout of 20 seconds. Tweak to taste.
        HttpConnectionParams.setConnectionTimeout(params, 40 * 1000);
        HttpConnectionParams.setSoTimeout(params, 40 * 1000);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        // Don't handle redirects -- return them to the caller. Our code
        // often wants to re-POST after a redirect, which we must do ourselves.
        HttpClientParams.setRedirecting(params, false);

        // Set the specified user agent and register standard protocols.
        if (userAgent != null) {
            HttpProtocolParams.setUserAgent(params, userAgent);
        }
        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        final ClientConnectionManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);

        // We use a factory method to modify superclass initialization
        // parameters without the funny call-a-static-method dance.
        return new GzipHttpClient(manager, params);
    }

    private final DelegateHttpClient delegate;

    private GzipHttpClient(final ClientConnectionManager ccm, final HttpParams params) {
        this.delegate = new DelegateHttpClient(ccm, params);
    }

    /**
     * Release resources associated with this client. You must call this, or
     * significant resources (sockets and memory) may be leaked.
     */
    public void close() {
        getConnectionManager().shutdown();
    }

    public HttpResponse execute(final HttpHost target, final HttpRequest request) throws IOException {
        return delegate.execute(target, request);
    }

    public HttpResponse execute(final HttpHost target, final HttpRequest request,
                                final HttpContext context) throws IOException {
        return delegate.execute(target, request, context);
    }

    public <T> T execute(final HttpHost target, final HttpRequest request,
                         final ResponseHandler<? extends T> responseHandler) throws IOException {
        return delegate.execute(target, request, responseHandler);
    }

    public <T> T execute(final HttpHost target, final HttpRequest request,
                         final ResponseHandler<? extends T> responseHandler, final HttpContext context)
            throws IOException {
        return delegate.execute(target, request, responseHandler, context);
    }

    public HttpResponse execute(final HttpUriRequest request) throws IOException {
        return delegate.execute(request);
    }

    public HttpResponse execute(final HttpUriRequest request, final HttpContext context)
            throws IOException {
        return delegate.execute(request, context);
    }

    public <T> T execute(final HttpUriRequest request,
                         final ResponseHandler<? extends T> responseHandler) throws IOException {
        return delegate.execute(request, responseHandler);
    }

    public <T> T execute(final HttpUriRequest request,
                         final ResponseHandler<? extends T> responseHandler, final HttpContext context)
            throws IOException {
        return delegate.execute(request, responseHandler, context);
    }

    public ClientConnectionManager getConnectionManager() {
        return delegate.getConnectionManager();
    }

    public HttpParams getParams() {
        return delegate.getParams();
    }
}