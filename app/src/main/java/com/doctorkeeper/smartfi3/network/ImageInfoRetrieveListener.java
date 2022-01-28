package com.doctorkeeper.smartfi3.network;

import android.graphics.Bitmap;

import com.doctorkeeper.smartfi3.ptp.Camera;
import com.doctorkeeper.smartfi3.ptp.model.ObjectInfo;

/**
 * Created by thinoo on 6/30/17.
 */

public class ImageInfoRetrieveListener implements Camera.RetrieveImageInfoListener{
    @Override
    public void onImageInfoRetrieved(int objectHandle, ObjectInfo objectInfo, Bitmap thumbnail) {

    }
}
