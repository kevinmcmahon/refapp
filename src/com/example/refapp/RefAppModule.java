package com.example.refapp;

import com.example.refapp.managers.SearchManager;
import com.example.refapp.providers.PreferencesNameProvider;
import com.example.refapp.services.RemoteClient;
import com.example.refapp.services.RemoteRestClient;
import com.example.refapp.utils.http.HttpClientProvider;
import com.example.refapp.utils.http.UserAgent;
import com.example.refapp.utils.http.UserAgentProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.apache.http.client.HttpClient;
import roboguice.inject.SharedPreferencesName;

/**
 * A class extends {@link AbstractModule} to provide additional injection configuration using RoboGuice.
 */
public class RefAppModule extends AbstractModule {

    /**
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(String.class).annotatedWith(SharedPreferencesName.class).toProvider(PreferencesNameProvider.class);
        bind(String.class).annotatedWith(UserAgent.class).toProvider(UserAgentProvider.class);
        bind(SearchManager.class).in(Scopes.SINGLETON);
        bind(HttpClient.class).toProvider(HttpClientProvider.class);
        bind(RemoteClient.class).to(RemoteRestClient.class);
//		bind(RemoteClient.class).to(MockRemoteClient.class);
    }
}