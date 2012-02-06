package com.example.refapp.services;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import com.example.refapp.models.RequestParam;
import com.example.refapp.utils.constants.Config;
import com.google.inject.Inject;
import org.apache.http.client.HttpClient;
import roboguice.event.Observes;
import roboguice.service.RoboService;
import roboguice.util.Ln;
import roboguice.util.SafeAsyncTask;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DataService extends RoboService {
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;
    public static final int STATUS_FAILED = 3;
    public static final int STATUS_CANCELED = 4;
    public static final String EXTRA_ERROR_TYPE = "errorType";
    public static final String EXTRA_RESULT = "result";
    public static final String EXTRA_RESULT_TYPE = "resultType";
    public static final String EXTRA_REQUEST_TYPE = "requestType";
    public static final String EXTRA_REQUEST_PARAMS = "requestArgs";
    public static final String EXTRA_COMMAND = "command";
    public static final String EXTRA_RECEIVER = "receiver";

    @Inject
    HttpClient httpClient; /* Keep a strong reference for now, so we don't leak */

    @Inject
    RemoteClient remoteClient;

    final AtomicInteger workers = new AtomicInteger(0);
    final ConcurrentMap<DataRequestType, DataRequestTask<?>> taskMap =
            new ConcurrentHashMap<DataRequestType, DataRequestTask<?>>();

    @Override
    synchronized public void onStart(Intent intent, int startId) {
        if (Config.IS_LOGGABLE) {
            Ln.v("onStart %2$d - %1$s", intent.toURI(), startId);
        }
        handleCommand(intent);
    }

    @Override
    synchronized public int onStartCommand(Intent intent, int flags, int startId) {
        if (Config.IS_LOGGABLE) {
            Ln.v("onStartCommand %2$d - %1$s", intent.toURI(), startId);
        }
        handleCommand(intent);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void handleCommand(Intent intent) {
        if (intent == null) return;

        String className = intent.getStringExtra(EXTRA_RESULT_TYPE);
        Class resultType = null;
        try {
            resultType = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(String.format("Unknown class name[%s]", className));
        }
        //noinspection unchecked
        handleIntent(intent, resultType);
    }

    private <ResultT> void handleIntent(Intent intent, Class<ResultT> resultType) {
        assert intent.hasExtra(EXTRA_REQUEST_TYPE);

        DataRequestTask<ResultT> task = new DataRequestTask<ResultT>(eventManager, this,
                remoteClient, intent, resultType);

        handleWorkStarted(new TaskStartedEvent(task));

        task.execute();
    }

    void handleWorkStarted(TaskStartedEvent event) {
        int workerCount = workers.incrementAndGet();

        if (Config.IS_LOGGABLE) {
            Ln.d("Number of active workers [%d]", workerCount);
        }

        DataRequestTask<?> oldTask = taskMap.put(event.task.requestType, event.task);
        if (oldTask != null) {
            oldTask.cancel(true);
        }
    }

    synchronized void handleWorkCompleted(@Observes TaskCompletedEvent event) {
        if (workers.decrementAndGet() == 0) {
            Ln.i("Stopping");
            stopSelf();
            httpClient = null;
            taskMap.clear();
        }
    }

    @Override
    public void onDestroy() {
        new SafeAsyncTask<Void>() {
            @Override
            public Void call() throws Exception {
                // Release network resource in a background thread,
                // otherwise an android.os.NetworkOnMainThreadException will be thrown for
                // device with Honeycomb or higher
                remoteClient.release();
                return null;
            }
        }.execute();
        super.onDestroy();
    }

    public static void start(DataRequestType requestType, DataRequestResultReceiver resultReceiver,
                             ArrayList<RequestParam> params) {
        assert requestType != null;
        assert resultReceiver != null;

        Context context = resultReceiver.getContext();
        if (context == null) return;

        Intent intent = new Intent(context, DataService.class);
        intent.putExtra(EXTRA_RECEIVER, resultReceiver);
        intent.putExtra(EXTRA_REQUEST_TYPE, requestType.name());
        intent.putExtra(EXTRA_RESULT_TYPE, resultReceiver.getResultType().getName());

        if (params != null) {
            intent.putParcelableArrayListExtra(EXTRA_REQUEST_PARAMS, params);
        }

        context.startService(intent);
    }

    public static class TaskStartedEvent {
        public final DataRequestTask<?> task;

        public TaskStartedEvent(DataRequestTask<?> task) {
            this.task = task;
        }
    }

    public static class TaskCompletedEvent {
        public final DataRequestType requestType;

        public TaskCompletedEvent(DataRequestType requestType) {
            this.requestType = requestType;
        }
    }
}
