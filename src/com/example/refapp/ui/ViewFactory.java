package com.example.refapp.ui;

import android.content.Context;
import android.view.View;

public abstract class ViewFactory<T> {
    private int layoutResId;

    public ViewFactory() {}

    public ViewFactory(int layoutResId) {
        this.layoutResId = layoutResId;
    }

    public View getView(Context context) {
        View view = createNewView(context);
        view.setTag(newViewHolder(view));
        return view;
    }

    protected View createNewView(Context context) {
        if (layoutResId != 0) {
            return View.inflate(context, layoutResId, null);
        }

        throw new RuntimeException("You must either provide the resource id for the view layout in the " +
                "constructor or override the createNewView method");
    }

    protected abstract ViewHolder<T> newViewHolder(View parent);

    static abstract public class ViewHolder<T> {
        abstract public void update(T data);
    }
}
