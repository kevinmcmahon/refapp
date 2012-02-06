package com.example.refapp.services;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.Toast;
import com.example.refapp.R;
import com.example.refapp.utils.IContextDependent;
import com.example.refapp.utils.IDisposable;
import com.example.refapp.utils.constants.Constants;
import com.example.refapp.utils.errors.ErrorType;
import org.typetools.TypeResolver;
import roboguice.util.Ln;

import java.lang.ref.WeakReference;

public abstract class DataRequestResultReceiver<TContext extends Context, TResult> extends ResultReceiver
        implements IDisposable, IContextDependent<TContext> {
    private WeakReference<TContext> contextRef;
    private WeakReference<Handler> handlerRef;
    private final Class<TResult> resultType;

    public DataRequestResultReceiver(TContext context, Handler handler) {
        super(handler);
        contextRef = new WeakReference<TContext>(context);
        handlerRef = new WeakReference<Handler>(handler);

        Class<?>[] typeArguments = TypeResolver.resolveArguments(getClass(), DataRequestResultReceiver.class);

        resultType = (Class<TResult>) typeArguments[1];
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        TContext context = getContext();
        if (context == null) {
            Ln.w("Lost reference to activity, result ignored.");
            return;
        }

        if (resultCode != DataService.STATUS_RUNNING) {
            onPostExecute(context);
        }

        String msg;
        switch (resultCode) {
            case DataService.STATUS_RUNNING:
                onPreExecute(context);
                break;
            case DataService.STATUS_FINISHED:
                beforeResultHandled(context);
                TResult result = null;
                if (resultData.containsKey(DataService.EXTRA_RESULT)) {
                    //noinspection unchecked
                    result = (TResult) resultData.get(DataService.EXTRA_RESULT);
                }

                onSuccess(context, result);
                break;
            case DataService.STATUS_CANCELED:
                onCancel(context);
                break;
            case DataService.STATUS_ERROR:
                beforeResultHandled(context);
                msg = resultData.getString(Intent.EXTRA_TEXT);
                ErrorType errorType = (ErrorType) resultData.get(DataService.EXTRA_ERROR_TYPE);
                errorType = errorType == null ? ErrorType.GENERAL : errorType;


                String command = resultData.getString(DataService.EXTRA_COMMAND);
                Ln.e("Result receiver - Command[%s] ErrorType[%s] Message[%s]",
                        command,
                        errorType == null ? Constants.STR_EMPTY : errorType.toString(),
                        msg);

                if (ErrorType.LOGIN_REQUIRED.equals(errorType)) {
                    onLoginRequired(context);
                } else if (ErrorType.CONNECTION_ERROR.equals(errorType)) {
                    onConnectionError(context, msg);
                } else if (ErrorType.INVALID_CREDENTIAL.equals(errorType)) {
                    onInvalidCredential(context);
                } else {
                    onError(context, msg);
                }
                break;
            case DataService.STATUS_FAILED:
                beforeResultHandled(context);
                msg = resultData.getString(Intent.EXTRA_TEXT);
                onFailure(context, msg);
                break;
        }
    }

    abstract protected void onSuccess(TContext context, TResult result);

    public void sendError(String msg) {
        Bundle bundle = new Bundle();
        bundle.putString(Intent.EXTRA_TEXT, msg);
        send(DataService.STATUS_ERROR, bundle);
    }

    public TContext getContext() {
        return contextRef == null ? null : contextRef.get();
    }

    public void setContext(TContext context) {
        if (contextRef != null) {
            contextRef.clear();
            contextRef = null;
        }

        contextRef = new WeakReference<TContext>(context);
    }

    public Handler getHandler() {
        return handlerRef == null ? null : handlerRef.get();
    }

    protected void onPreExecute(TContext context) {
    }

    protected void beforeResultHandled(TContext context) {
        // Do nothing
    }

    protected void onLoginRequired(TContext context) {
        Toast.makeText(context, "You are required to sign in to perform this action.",
                Toast.LENGTH_SHORT).show();
    }

    protected void onInvalidCredential(TContext context) {
        // Do nothing
    }

    protected void onConnectionError(TContext context, String msg) {
        Toast.makeText(context, R.string.msg_network_error,
                Toast.LENGTH_SHORT).show();
    }

    protected void onFailure(TContext context, String msg) {
        Ln.w("Failure is not handled. Message[%s]", msg);
    }

    protected void onCancel(TContext context) {
        Ln.d("Task Canceled");
    }

    protected void onError(TContext context, String msg) {
        Ln.w("Error is not handled. Message[%s]", msg);
        Toast.makeText(context, R.string.msg_server_error,
                Toast.LENGTH_SHORT).show();
    }

    protected void onPostExecute(TContext context) {

    }

    public Class<TResult> getResultType() {
        return resultType;
    }

    public void clearContext() {
        if (contextRef != null) {
            contextRef.clear();
            contextRef = null;
        }
    }

    public void dispose() {
        clearContext();

        if (handlerRef != null) {
            handlerRef.clear();
            handlerRef = null;
        }
    }
}
