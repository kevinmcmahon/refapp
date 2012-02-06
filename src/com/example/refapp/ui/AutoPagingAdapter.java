package com.example.refapp.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

/**
 * A wrapper adapter provides auto paging functionality.  Developer can implement and set the
 * OnLoadMoreListener to handle the data loading request.
 */
public class AutoPagingAdapter<T extends ListAdapter> extends BaseAdapter {
    static public final int PENDING_ROW_ITEM_ID = Integer.MIN_VALUE;

    private final T wrappedAdapter;
    private int pendingRowResourceId = Integer.MIN_VALUE;
    public int fullListCount = Integer.MAX_VALUE;
    private final Context context;
    private View pendingView;
    private int lastLoadMoreDataCount = -1;
    private OnLoadMoreListener onLoadMoreListener;

    public AutoPagingAdapter(Context context, T wrappedAdapter) {
        this.context = context;
        this.wrappedAdapter = wrappedAdapter;
        wrappedAdapter.registerDataSetObserver(new DataSetObserver() {
            public void onChanged() {
                notifyDataSetChanged();
            }

            public void onInvalidated() {
                notifyDataSetInvalidated();
            }
        });
    }

    public AutoPagingAdapter(Context context, T wrappedAdapter, int pendingResourceId) {
        this(context, wrappedAdapter);
        this.pendingRowResourceId = pendingResourceId;
    }

    protected Context getContext() {
        return context;
    }

    public boolean hasMore() {
        return wrappedAdapter.getCount() < fullListCount;
    }

    @Override
    public Object getItem(int position) {
        return wrappedAdapter.getItem(position);
    }

    @Override
    public int getCount() {
        if (hasMore())
            return wrappedAdapter.getCount() + 1;
        else
            return wrappedAdapter.getCount();
    }

    /**
     * Number of items in the data set represented by this Adapter.  Use this to get the real data count,
     * since getCount() might return a number that includes the "Load More" row
     *
     * @return int
     */
    public int getDataCount() {
        return wrappedAdapter.getCount();
    }

    @Override
    public int getViewTypeCount() {
        return wrappedAdapter.getViewTypeCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadingPosition(position))
            return IGNORE_ITEM_VIEW_TYPE;
        else
            return wrappedAdapter.getItemViewType(position);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return wrappedAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        if (isLoadingPosition(position)) return false;
        return wrappedAdapter.isEnabled(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (isLoadingPosition(position)) {
            if (pendingView == null) {
                pendingView = getPendingView(parent);
            }

            int dataCount = getDataCount();
            if (lastLoadMoreDataCount != dataCount) {
                lastLoadMoreDataCount = dataCount;
                if (onLoadMoreListener != null) {
                    onLoadMoreListener.onLoadMore(this, dataCount);
                }
            }
            return pendingView;
        }

        return wrappedAdapter.getView(position, convertView, parent);
    }

    @Override
    public boolean hasStableIds() {
        return wrappedAdapter.hasStableIds();
    }

    @Override
    public long getItemId(int position) {
        if (isLoadingPosition(position)) return PENDING_ROW_ITEM_ID;
        return wrappedAdapter.getItemId(position);
    }

    protected boolean isLoadingPosition(int position) {
        return position == wrappedAdapter.getCount();
    }

    public T getWrappedAdapter() {
        return wrappedAdapter;
    }

    protected View getPendingView(ViewGroup parent) {
        if (pendingRowResourceId != Integer.MIN_VALUE) {
            return View.inflate(context, pendingRowResourceId, null);
        } else {
            throw new RuntimeException(
                    "You must either override getPendingView() or supply a pending View resource via the constructor");
        }
    }

    public OnLoadMoreListener getOnLoadMoreListener() {
        return onLoadMoreListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore(AutoPagingAdapter adapter, int currentSize);
    }
}