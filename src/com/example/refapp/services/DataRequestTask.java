package com.example.refapp.services;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;
import com.example.refapp.models.RequestParam;
import com.example.refapp.utils.constants.Config;
import com.example.refapp.utils.errors.RefAppException;
import roboguice.event.EventManager;
import roboguice.util.Ln;
import roboguice.util.RoboAsyncTask;

import java.util.List;

class DataRequestTask<ResultT> extends RoboAsyncTask<ResultT> {
    private final ResultReceiver receiver;
    private final Bundle requestBundle;
    private int resultStatus;
    public final Class<? extends ResultT> resultType;
    private final EventManager eventManager;
    public final DataRequestType requestType;
    final RemoteClient remoteClient;
    private final List<RequestParam> requestParams;
    volatile private boolean isCanceled = false;

    public DataRequestTask(final EventManager eventManager, final Context context,
                           final RemoteClient remoteClient, final Intent intent,
                           Class<? extends ResultT> resultType) {
        super(context);
        this.eventManager = eventManager;
        this.remoteClient = remoteClient;
        this.receiver = intent.getParcelableExtra(DataService.EXTRA_RECEIVER);
        this.requestBundle = intent.getExtras();
        this.resultType = resultType;
        String requestTypeString = intent.getStringExtra(DataService.EXTRA_REQUEST_TYPE);
        this.requestType = DataRequestType.valueOf(requestTypeString);
        this.requestParams = intent.getParcelableArrayListExtra(DataService.EXTRA_REQUEST_PARAMS);
    }

    /**
     * @throws Exception, captured on passed to onException() if present.
     */
    @Override
    protected void onPreExecute() throws Exception {
        super.onPreExecute();
        receiver.send(DataService.STATUS_RUNNING, Bundle.EMPTY);
    }

    @Override
    public ResultT call() throws Exception {
        return remoteClient.execute(requestType, requestParams, resultType);
    }

    /**
     * @param result the result of {@link #call()}
     * @throws Exception, captured on passed to onException() if present.
     */
    @Override
    protected void onSuccess(ResultT result) throws Exception {
        if (isCancelled()) return;

        if (Parcelable.class.isAssignableFrom(resultType)) {
            requestBundle.putParcelable(DataService.EXTRA_RESULT, (Parcelable) result);
        } else if (Parcelable[].class.isAssignableFrom(resultType)) {
            requestBundle.putParcelableArray(DataService.EXTRA_RESULT, (Parcelable[]) result);
        } else if (String.class.isAssignableFrom(resultType)) {
            requestBundle.putString(DataService.EXTRA_RESULT, (String) result);
        } else if (String[].class.isAssignableFrom(resultType)) {
            requestBundle.putStringArray(DataService.EXTRA_RESULT, (String[]) result);
        } else {
            throw new RuntimeException("Result type must be parcelable");
        }

        resultStatus = DataService.STATUS_FINISHED;
    }

    /**
     * Called when the thread has been interrupted, likely because
     * the task was canceled.
     * <p/>
     * By default, calls {@link #onException(Exception)}, but this method
     * may be overridden to handle interruptions differently than other
     * exceptions.
     *
     * @param e an InterruptedException or InterruptedIOException
     */
    @Override
    protected void onInterrupted(Exception e) {
        if (Config.IS_LOGGABLE) {
            Ln.i("Task is cancelled");
        }
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        if (isCancelled()) return;

        super.onException(e);

        requestBundle.putString(Intent.EXTRA_TEXT, e.toString());

        resultStatus = DataService.STATUS_ERROR;

        if (e instanceof RefAppException) {
            requestBundle.putSerializable(DataService.EXTRA_ERROR_TYPE, ((RefAppException) e).errorType);
        }
    }

    @Override
    protected void onFinally() throws RuntimeException {
        eventManager.fire(new DataService.TaskCompletedEvent(requestType));
        receiver.send(resultStatus, requestBundle);
        super.onFinally();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        isCanceled = true;
        resultStatus = DataService.STATUS_CANCELED;
        return super.cancel(mayInterruptIfRunning);
    }

    public boolean isCancelled() {
        return isCanceled;
    }
}

