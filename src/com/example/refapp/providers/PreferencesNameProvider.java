package com.example.refapp.providers;

import android.app.Application;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * A provider to return the default Shared Preference name
 */
public class PreferencesNameProvider implements Provider<String> {
    private final String preferencesName;

    @Inject
    public PreferencesNameProvider(Application application) {
        preferencesName = application.getPackageName() + "_preferences" ;
    }

    @Override
    public String get() {
        return preferencesName;
    }
}
