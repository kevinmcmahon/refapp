package com.example.refapp.utils.http;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.apache.http.client.HttpClient;
import roboguice.util.Ln;

import java.lang.ref.SoftReference;

@Singleton
public class HttpClientProvider implements Provider<HttpClient> {
    SoftReference<GzipHttpClient> reference;
    String userAgent = null;

    @Inject
    public HttpClientProvider(@UserAgent String userAgent) {
        this.userAgent = userAgent;
        Ln.v("HttpClientProvider.ctor()");
    }

    public void release() {
        if (reference != null) {
            Ln.d("releasing resource for HttpClientProvider");
            GzipHttpClient instance = reference.get();
            if (instance != null) {
                Ln.d("releasing GZipHttpClient connection manager");
                instance.close();
            } else {
                Ln.w("possible leak if GzipHttpClient.close() was never called.");
            }
            reference.clear();
        } else {
           Ln.d("no HttpClientProvider resources to release");
        }
        reference = null;
    }

    @Override
    public ExtendedHttpClient get() {
        if (reference == null) {
            Ln.d("initializing new GzipHttpClient");
            reference = new SoftReference<GzipHttpClient>(GzipHttpClient.newInstance(userAgent));
        }

        GzipHttpClient instance = reference.get();

        if (instance == null) {
            Ln.d("re-initializing new GzipHttpClient");
            Ln.w("possible leak if GzipHttpClient.close() was never called.");
            instance = GzipHttpClient.newInstance(userAgent);
            reference.clear();
            reference = new SoftReference<GzipHttpClient>(instance);
        }
        return instance;
    }
}
