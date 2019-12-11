package com.thinoo.drcamlink2.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import java.util.logging.LogRecord;

public class VideoIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public VideoIntentService(String name) {
        super(name);
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }


}
