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
package com.thinoo.drcamlink2.view.log_in;

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

import com.loopj.android.http.JsonHttpResponseHandler;
import com.thinoo.drcamlink2.Constants;
import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.madamfive.BlabAPI;
import com.thinoo.drcamlink2.madamfive.MadamfiveAPI;
import com.thinoo.drcamlink2.util.SmartFiPreference;
import com.thinoo.drcamlink2.view.patient.PatientDialogFragment;

import org.json.JSONArray;
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




        final Button loginButton = (Button)view.findViewById(R.id.btn_login);

        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setEnabled(false);
                BlabAPI.loginEMR(getActivity(), usernameTextView.getText().toString(),passwordTextView.getText().toString(), new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Log.w(TAG,"성공 = "+response+" code = "+statusCode);
                        try {
                            String code =  response.get(Constants.EMRAPI.CODE).toString();
                            if(!code.equals(Constants.EMRAPI.CODE_200)){
                                Toast toast = Toast.makeText(getActivity(), "아이디 또는 비밀번호를 확인해 주세요", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                loginButton.setEnabled(true);
                            }else{
                                SmartFiPreference.setDoctorId(getActivity(),usernameTextView.getText().toString());
                                SmartFiPreference.setSfDoctorPw(getActivity(),passwordTextView.getText().toString());

                                try {

                                    JSONObject data = (JSONObject) response.get(Constants.EMRAPI.DATA);
                                    SmartFiPreference.setSfToken(getActivity(),data.getString("token"));
                                    SmartFiPreference.setHospitalId(getActivity(),data.getString("hsptId"));
                                    dismiss();
                                    loginButton.setEnabled(true);
                                    startSelectPatient();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.e(TAG," 파싱에러");
                                }
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }


                    }


                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        Log.w(TAG,"실패");
                        Toast toast = Toast.makeText(getActivity(), "아이디 또는 비밀번호를 확인해 주세요", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        loginButton.setEnabled(true);
                    }
                });

                //이전코드
//                MadamfiveAPI.login(usernameTextView.getText().toString(), passwordTextView.getText().toString(), new JsonHttpResponseHandler() {
//
//                    @Override
//                    public void onStart() {
//                        Log.w(TAG, "onStart:");
//                    }
//
//                    @Override
//                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
//                        Log.w(TAG, "HTTPa:" + statusCode + responseString);
//
//                        if(statusCode == 400) {
//                            Toast toast = Toast.makeText(getActivity(), "아이디 또는 비밀번호를 확인해 주세요", Toast.LENGTH_SHORT);
//                            toast.setGravity(Gravity.CENTER, 0, 0);
//                            toast.show();
//
//                            loginButton.setEnabled(true);
//                        }else{
//                            dismiss();
//                            loginButton.setEnabled(true);
//                            startSelectPatient();
//                        }
//
//                    }
//
//                    @Override
//                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
//                        // If the response is JSONObject instead of expected JSONArray
//                        Log.w(TAG, "HTTPb:" + statusCode + response.toString());
//
//                        dismiss();
//                        startSelectPatient();
//                    }
//
//                });

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
