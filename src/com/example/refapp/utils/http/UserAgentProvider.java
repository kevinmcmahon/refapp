package com.example.refapp.utils.http;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import roboguice.inject.RoboApplicationProvider;
import roboguice.util.Ln;

@Singleton
public class UserAgentProvider implements Provider<String> {
    Context application;
    TelephonyManager manager;
    volatile String userAgent;

    @Inject
    public UserAgentProvider(RoboApplicationProvider<Application> applicationProvider) {
        this.application = applicationProvider.get();
        manager = (TelephonyManager)application.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public String get() {
        if (userAgent == null) {
            synchronized (UserAgentProvider.class) {
                if (userAgent == null) {
                    userAgent = createUserAgentString();
                    Ln.i("User-Agent: %1$s", userAgent);
                }
            }
        }
        return userAgent;
    }

    private String createUserAgentString() {
        String appName = "";
        String appVersion = "";
        int height = 0;
        int width = 0;
        DisplayMetrics display = application.getResources().getDisplayMetrics();
        Configuration config = application.getResources().getConfiguration();

        // Always send screen dimension for portrait mode
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            height = display.widthPixels;
            width = display.heightPixels;
        } else {
            width = display.widthPixels;
            height = display.heightPixels;
        }

        try {
            PackageInfo packageInfo = application.getPackageManager().getPackageInfo(application.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            appName = packageInfo.packageName;
            appVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignore) {
            // this should never happen, we are looking up ourselves
        }

        // Tries to conform to default android UA string without the Safari / webkit noise, plus adds the screen dimensions
        return String.format("%1$s/%2$s (%3$s; U; Android %4$s; %5$s-%6$s; %12$s Build/%7$s; %8$s) %9$dX%10$d %11$s %12$s %13$s"
				, appName
				, appVersion
				, System.getProperty("os.name", "Linux")
				, Build.VERSION.RELEASE
				, config.locale.getLanguage().toLowerCase()
				, config.locale.getCountry().toLowerCase()
				, Build.ID
				, Build.BRAND
				, width
				, height
                , manager.getNetworkOperatorName()
				, Build.MANUFACTURER
				, Build.MODEL);
    }
}
