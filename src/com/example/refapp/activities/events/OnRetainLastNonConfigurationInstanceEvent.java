package com.example.refapp.activities.events;

import java.util.Map;

public class OnRetainLastNonConfigurationInstanceEvent {
    public final Map<String, Object> instanceMap;

    public OnRetainLastNonConfigurationInstanceEvent(Map<String, Object> instanceMap) {
        this.instanceMap = instanceMap;
    }
}