package com.thinoo.drcamlink2.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.thinoo.drcamlink2.R;

public class LaunchVrecordActivity extends Activity {
    private final String TAG = LaunchVrecordActivity.class.getSimpleName();

    private static final int VREC_REQUEST = 2100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.launch_vrecord_activity_main);
    }
}
