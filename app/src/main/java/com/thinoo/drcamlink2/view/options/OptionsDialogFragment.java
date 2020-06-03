/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thinoo.drcamlink2.view.options;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.madamfive.MadamfiveAPI;
import com.thinoo.drcamlink2.util.SmartFiPreference;

//import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.doctorSelectExtraOption;
//import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.patientInsertExtraOption;
//import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.patientSearchDisplayExtraOption;
//import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.shootingImageDisplayExtraOption;


public class OptionsDialogFragment extends DialogFragment {

    private final String TAG = OptionsDialogFragment.class.getSimpleName();

    private Switch switch_options_patient_info;
    private Switch switch_options_patient_insert_activate;
    private Switch switch_options_doctor_insert_activate;
    private Switch switch_options_shooting_image_display;

    private Boolean doctorSelectExtraOption;
    private Boolean patientInsertExtraOption;
    private Boolean patientSearchDisplayExtraOption;
    private Boolean shootingImageDisplayExtraOption;

    public static OptionsDialogFragment newInstance() {
        Bundle args = new Bundle();

        OptionsDialogFragment f = new OptionsDialogFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle("기타설정");
        View view = inflater.inflate(R.layout.activity_options, container, false);

        OptionsDialogFragment.this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        /// Option 1
        switch_options_patient_info = (Switch) view.findViewById(R.id.switch_options_patient_info);
        switch_options_patient_info.setChecked(false);

        patientSearchDisplayExtraOption = SmartFiPreference.getSfDisplayExtraOpt(MadamfiveAPI.getActivity());
        if(patientSearchDisplayExtraOption){
            switch_options_patient_info.setChecked(true);
        }

        switch_options_patient_info.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(patientSearchDisplayExtraOption){
                    patientSearchDisplayExtraOption = false;
                    switch_options_patient_info.setChecked(false);
                }else {
                    patientSearchDisplayExtraOption = true;
                    switch_options_patient_info.setChecked(true);
                }
                SmartFiPreference.setSfDisplayExtraOpt(MadamfiveAPI.getActivity(),patientSearchDisplayExtraOption);
            }
        });

        /// Option 2
        switch_options_patient_insert_activate = (Switch) view.findViewById(R.id.switch_options_patient_insert_activate);
        switch_options_patient_insert_activate.setChecked(false);

        patientInsertExtraOption = SmartFiPreference.getSfInsertPatientOpt(MadamfiveAPI.getActivity());

        Log.w(TAG,"patientInsertExtraOption = "+patientInsertExtraOption);
        if(patientInsertExtraOption){
            switch_options_patient_insert_activate.setChecked(true);
        }

        switch_options_patient_insert_activate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(patientInsertExtraOption){
                    patientInsertExtraOption = false;
                    switch_options_patient_insert_activate.setChecked(false);
                    SmartFiPreference.setSfInsertPatientOpt(MadamfiveAPI.getActivity(),patientInsertExtraOption);
                }else {
                    patientInsertExtraOption = true;
                    switch_options_patient_insert_activate.setChecked(true);
                    SmartFiPreference.setSfInsertPatientOpt(MadamfiveAPI.getActivity(),patientInsertExtraOption);
                }
            }
        });

        /// Option 3
        switch_options_doctor_insert_activate = (Switch) view.findViewById(R.id.switch_options_doctor_insert_activate);
        switch_options_doctor_insert_activate.setChecked(false);

        doctorSelectExtraOption = SmartFiPreference.getSfInsertDoctorOpt(MadamfiveAPI.getActivity());
        if(doctorSelectExtraOption){
            switch_options_doctor_insert_activate.setChecked(true);
        }

        switch_options_doctor_insert_activate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(doctorSelectExtraOption){
                    doctorSelectExtraOption = false;
                    switch_options_doctor_insert_activate.setChecked(false);
                }else {
                    doctorSelectExtraOption = true;
                    switch_options_doctor_insert_activate.setChecked(true);
                }
                SmartFiPreference.setSfInsertDoctorOpt(MadamfiveAPI.getActivity(),doctorSelectExtraOption);
            }
        });

        /// optins4
        switch_options_shooting_image_display = (Switch) view.findViewById(R.id.switch_options_shooting_image_display);
        switch_options_shooting_image_display.setChecked(false);

        shootingImageDisplayExtraOption = SmartFiPreference.getSfShootDisplayOpt(MadamfiveAPI.getActivity());
        if(shootingImageDisplayExtraOption){
            switch_options_shooting_image_display.setChecked(true);
        }

        switch_options_shooting_image_display.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(shootingImageDisplayExtraOption){
                    shootingImageDisplayExtraOption = false;
                    switch_options_shooting_image_display.setChecked(false);
                }else {
                    shootingImageDisplayExtraOption = true;
                    switch_options_shooting_image_display.setChecked(true);
                }
                SmartFiPreference.setSfShootDisplayOpt(MadamfiveAPI.getActivity(),shootingImageDisplayExtraOption);
            }
        });

        return view;
    }

    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = (int) (metrics.widthPixels * .85);
        int height = (int) (metrics.heightPixels * .60);
        window.setLayout(width, height);
        window.setGravity(Gravity.CENTER);
    }

}
