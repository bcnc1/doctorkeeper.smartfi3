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
package com.doctorkeeper.smartfi.view.log_in;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.doctorkeeper.smartfi.network.BlabAPI;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.doctorkeeper.smartfi.R;
import com.doctorkeeper.smartfi.util.SmartFiPreference;
import com.doctorkeeper.smartfi.view.patient.PatientDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LoginDialogFragment extends DialogFragment {

    private final String TAG = LoginDialogFragment.class.getSimpleName();

    public static LoginDialogFragment newInstance() {
        Bundle args = new Bundle();

        LoginDialogFragment f = new LoginDialogFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle("Login");
        View view = inflater.inflate(R.layout.activity_login, container, false);

        LoginDialogFragment.this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        final TextView usernameTextView;
        final TextView passwordTextView;

        usernameTextView = (TextView) view.findViewById(R.id.input_email);
        passwordTextView = (TextView) view.findViewById(R.id.input_password);

        String spId = SmartFiPreference.getHospitalId(BlabAPI.getActivity());
        String spPwd = SmartFiPreference.getSfDoctorPw(BlabAPI.getActivity());
        Log.i(TAG, "spId:" + spId);
        if(!spId.isEmpty() && spId != "undefined"){
            usernameTextView.setText(spId);
            passwordTextView.setText(spPwd);
        }
        final Button loginButton = (Button)view.findViewById(R.id.btn_login);

        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setEnabled(false);

                BlabAPI.loginDoctorKeeper(BlabAPI.getContext(), usernameTextView.getText().toString(), passwordTextView.getText().toString(), new JsonHttpResponseHandler(){
                    @Override
                    public void onStart() {
                        Log.i(TAG, "onStart:" + BlabAPI.getContext());
                    }
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        Log.i(TAG, "HTTPa:" + statusCode + responseString);
                    }
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                        // If the response is JSONObject instead of expected JSONArray
                        Log.i(TAG, "HTTPb:" + statusCode + response.toString());

                        try {
                            if(Integer.parseInt(response.getString("code")) != 200) {
                                Log.i(TAG, " login failed ");
                                Toast toast = Toast.makeText(getActivity(), "아이디 또는 비밀번호를 확인해 주세요", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                loginButton.setEnabled(true);
                            }else{
                                dismiss();
                                SmartFiPreference.setDoctorId(getActivity(),usernameTextView.getText().toString());
                                SmartFiPreference.setSfDoctorPw(getActivity(),passwordTextView.getText().toString());
                                SmartFiPreference.setHospitalId(getActivity(),usernameTextView.getText().toString());
                                Log.i(TAG,"responseString:::"+response);
                                try{
                                    JSONObject obj = response;
                                    SmartFiPreference.setSfToken(getActivity(),obj.getString("token"));
                                    Log.i(TAG,"token:::"+obj.getString("token"));
                                }catch(Exception e){
                                    Log.d(TAG,e.toString());
                                }
                                loginButton.setEnabled(true);
                                startSelectPatient();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                    }
                });
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

    private void startSelectPatient() {

        if (getFragmentManager() != null) {
            FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
            PatientDialogFragment patientDialogFragment = PatientDialogFragment.newInstance();
            changelogTx.add(patientDialogFragment, "환자검색");
            changelogTx.commit();
        }
    }
}
