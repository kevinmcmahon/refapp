package com.example.refapp.utils;

import android.content.Context;

public interface IContextDependent<TContext extends Context> {
    TContext getContext();
    void setContext(TContext context);
    void clearContext();
}
