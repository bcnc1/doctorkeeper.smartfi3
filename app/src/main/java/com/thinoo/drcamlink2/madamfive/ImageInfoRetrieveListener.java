package com.thinoo.drcamlink2.madamfive;

import android.graphics.Bitmap;

import com.thinoo.drcamlink2.ptp.Camera;
import com.thinoo.drcamlink2.ptp.model.ObjectInfo;

/**
 * Created by thinoo on 6/30/17.
 */

public class ImageInfoRetrieveListener implements Camera.RetrieveImageInfoListener{
    @Override
    public void onImageInfoRetrieved(int objectHandle, ObjectInfo objectInfo, Bitmap thumbnail) {

    }
}
