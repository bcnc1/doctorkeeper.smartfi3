/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thinoo.drcamlink2.ptp.commands;

import android.graphics.Bitmap;
import android.util.Log;

import com.thinoo.drcamlink2.ptp.Camera.RetrieveImageInfoListener;
import com.thinoo.drcamlink2.ptp.PtpAction;
import com.thinoo.drcamlink2.ptp.PtpCamera;
import com.thinoo.drcamlink2.ptp.PtpCamera.IO;
import com.thinoo.drcamlink2.ptp.PtpConstants;
import com.thinoo.drcamlink2.ptp.PtpConstants.Response;
import com.thinoo.drcamlink2.ptp.model.ObjectInfo;

public class RetrieveImageInfoAction implements PtpAction {
    private final String TAG = RetrieveImageInfoAction.class.getSimpleName();
    private final PtpCamera camera;
    private final int objectHandle;
    private final RetrieveImageInfoListener listener;

    public RetrieveImageInfoAction(PtpCamera camera, RetrieveImageInfoListener listener, int objectHandle) {
        this.camera = camera;
        this.listener = listener;
        this.objectHandle = objectHandle;
    }

    @Override
    public void exec(IO io) {
        GetObjectInfoCommand getInfo = new GetObjectInfoCommand(camera, objectHandle);
        io.handleCommand(getInfo);

        if (getInfo.getResponseCode() != Response.Ok) {
            return;
        }

        ObjectInfo objectInfo = getInfo.getObjectInfo();
        if (objectInfo == null) {
            return;
        }

        Bitmap thumbnail = null;
        if (objectInfo.thumbFormat == PtpConstants.ObjectFormat.JFIF
                || objectInfo.thumbFormat == PtpConstants.ObjectFormat.EXIF_JPEG) {
            GetThumb getThumb = new GetThumb(camera, objectHandle);
            io.handleCommand(getThumb);
            if (getThumb.getResponseCode() == Response.Ok) {
                thumbnail = getThumb.getBitmap();
                //Log.i(TAG, thumbnail.getWidth()+"x"+thumbnail.getHeight());
            }
        }

        Log.d(TAG,"onImageInfoRetrieved => 호출");
        listener.onImageInfoRetrieved(objectHandle, getInfo.getObjectInfo(), thumbnail);
    }

    @Override
    public void reset() {
    }
}
