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
package com.thinoo.drcamlink.ptp.commands.eos;

import java.nio.ByteBuffer;

import com.thinoo.drcamlink.ptp.EosCamera;
import com.thinoo.drcamlink.ptp.PtpConstants;
import com.thinoo.drcamlink.ptp.PtpCamera.IO;
import com.thinoo.drcamlink.ptp.PtpConstants.Operation;
import com.thinoo.drcamlink.ptp.PtpConstants.Response;

public class EosSetPcModeCommand extends EosCommand {

    public EosSetPcModeCommand(EosCamera camera) {
        super(camera);
    }

    @Override
    public void exec(IO io) {
        io.handleCommand(this);
        if (responseCode != Response.Ok) {
            camera.onPtpError(String.format("Couldn't initialize main! setting PC Mode failed, error code %s",
                    PtpConstants.responseToString(responseCode)));
        }
    }

    @Override
    public void encodeCommand(ByteBuffer b) {
        encodeCommand(b, Operation.EosSetPCConnectMode, 1);
    }
}