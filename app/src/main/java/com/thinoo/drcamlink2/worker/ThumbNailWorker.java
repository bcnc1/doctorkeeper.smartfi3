package com.thinoo.drcamlink2.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ThumbNailWorker extends Worker {

    private static final String TAG = ThumbNailWorker.class.getSimpleName();

    public ThumbNailWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        return null;
    }
}
