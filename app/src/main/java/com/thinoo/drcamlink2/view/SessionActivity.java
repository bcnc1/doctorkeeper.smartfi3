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
package com.thinoo.drcamlink2.view;

import android.support.v4.app.FragmentActivity;

import com.thinoo.drcamlink2.AppSettings;
import com.thinoo.drcamlink2.ptp.Camera;

public abstract class SessionActivity extends FragmentActivity {

    public abstract Camera getCamera();

    public abstract void setSessionView(SessionView view);

    public abstract AppSettings getSettings();
}