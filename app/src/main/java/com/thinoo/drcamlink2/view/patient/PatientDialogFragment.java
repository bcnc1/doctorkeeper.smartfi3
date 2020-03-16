package com.thinoo.drcamlink2.view.patient;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.thinoo.drcamlink2.Constants;
import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.madamfive.BlabAPI;
import com.thinoo.drcamlink2.util.SmartFiPreference;
import com.thinoo.drcamlink2.view.log_in.LoginDialogFragment;
import com.thinoo.drcamlink2.view.phone_camera.PhoneCameraFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;


public class PatientDialogFragment extends DialogFragment {

    private final String TAG = PatientDialogFragment.class.getSimpleName();
    private ArrayList<HashMap<String, String>> patientInfoList;
    private PatientDialogAdapter adapter;
    private ListView patientListView;

    private ProgressBar patient_list_progressBar;
    private boolean patientInsertExtraOption = false;
    private String name;
    private String chartNumber;
    private TextView nameTextView;
    private TextView chartNumberTextView;

    public static PatientDialogFragment newInstance() {
        Bundle args = new Bundle();

        PatientDialogFragment f = new PatientDialogFragment();
        f.setArguments(args);
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        patientInsertExtraOption = SmartFiPreference.getSfInsertPatientOpt(getActivity());

        //환자검색버튼을 누르면 해당 다이얼로그 호출
        if(SmartFiPreference.getSfToken(getActivity()).equals(Constants.EMRAPI.UNDEFINED)){
            showLoginDialog();
        }



        getDialog().setTitle("환자 검색");
        final View view = inflater.inflate(R.layout.activity_search_patient, container, false);

        PatientDialogFragment.this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        nameTextView = (TextView) view.findViewById(R.id.search_name);
        chartNumberTextView = (TextView) view.findViewById(R.id.search_chartNumber);

        adapter = new PatientDialogAdapter(getActivity());
        patientListView = (ListView) view.findViewById(R.id.patient_list);
        patientListView.setAdapter(adapter);
        patient_list_progressBar = (ProgressBar) view.findViewById(R.id.patient_list_progressBar);


        view.findViewById(R.id.btn_search_patient).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if(BlabAPI.getNetworkStatus(getActivity())){
                    patient_list_progressBar.setVisibility(View.VISIBLE);

                    String keyword = "";
                    name = nameTextView.getText().toString();
                    chartNumber = chartNumberTextView.getText().toString();
                    boolean searchName = true;


                    if (name != null && name.length() != 0) {
                        keyword = name;
                    }

                    if (chartNumber != null && chartNumber.length() != 0) {
                        keyword = chartNumber;
                        searchName = false;
                    }


                    nameTextView.clearFocus();
                    chartNumberTextView.clearFocus();
                    final String loginCheck = keyword;

                    searchPatient(name, chartNumber);
                }else {
                    Toast.makeText(getActivity(), getString(R.string.check_network), Toast.LENGTH_SHORT).show();
                }
            }
        });

        patientListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                HashMap<String, String> patientInfo = (HashMap<String, String>) adapterView.getItemAtPosition(i);
               // selectedPatientInfo = patientInfo;

                //String name = selectedPatientInfo.get("name");
                String name = patientInfo.get("name");
                Toast.makeText(getActivity(), name + "님이 선택되었습니다", Toast.LENGTH_LONG).show();

                SmartFiPreference.setSfPatientCustNo(getActivity(), patientInfo.get("custNo"));
                SmartFiPreference.setPatientChart(getActivity(),patientInfo.get("chartNumber"));
                SmartFiPreference.setSfPatientName(getActivity(),name);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, PhoneCameraFragment.newInstance());
//                ft.addToBackStack(null);
                ft.commit();

                dismiss();
            }
        });

        return view;
    }

    private void searchPatient(final String searchName, final String searchChart) {
        BlabAPI.searchPatient(getActivity(), searchName, searchChart, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.w(TAG,"환자검색 = "+response);

                try {
                    String code =  response.get(Constants.EMRAPI.CODE).toString();

                    Log.e(TAG,"code = "+code);
                     if(code.equals(Constants.EMRAPI.CODE_200)){

                        patient_list_progressBar.setVisibility(View.INVISIBLE);

                        try {

                            patientInfoList = new ArrayList<HashMap<String, String>>();


                            JSONArray patientInfo = response.getJSONArray((Constants.EMRAPI.DATA));

                            Log.w(TAG,"검색결과 = "+patientInfo.length());
                            if(patientInsertExtraOption == true && patientInfo.length() == 0){
                                // TODO: 2020-01-16 환자추가.
                                addParientInfo(searchName, searchChart);
                            } else{
                                if(patientInfo.length() == 0){
                                    Toast toast = Toast.makeText(getActivity(), "해당 환자가 없습니다", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, -100);
                                    toast.show();
                                }

                                for (int i = 0; i < patientInfo.length(); i++) {
                                    JSONObject patient = patientInfo.getJSONObject(i);

                                    HashMap<String, String> patientElement = new HashMap<>();

                                    patientElement.put("custNo",patient.getString("custNo"));
                                    patientElement.put("name",patient.getString("custNm"));
                                    patientElement.put("chartNumber",patient.getString("chrtNo"));

                                    patientInfoList.add(patientElement);
                                }
                            }

//                            for (int i = 0; i < patientInfo.length(); i++) {
//                                JSONObject patient = patientInfo.getJSONObject(i);
//
//                                HashMap<String, String> patientElement = new HashMap<>();
//
//                                patientElement.put("custNo",patient.getString("custNo"));
//                                patientElement.put("name",patient.getString("custNm"));
//                                patientElement.put("chartNumber",patient.getString("chrtNo"));
//
//                                patientInfoList.add(patientElement);
//                            }

                            adapter.setItems(patientInfoList);
                            adapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG," 응답에러");
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.w(TAG,"환자검색 실패 = "+statusCode);

                if(statusCode == 401 || statusCode == 403){
                    Log.e(TAG,"인증 및 권한오류");
                    autoGetToken();
                }
            }
        });
    }

    private void addParientInfo(String inputName, String inputChart) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("스마트파이");
        builder.setMessage("해당 환자가 없습니다. 추가하시겠습니까?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, int which) {
                final String name = nameTextView.getText().toString();
                final String chartNumber = chartNumberTextView.getText().toString();

                if (name == null || name.length() == 0) {
                    Toast.makeText(getActivity(), "이름을 입력해 주세요", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                if (chartNumber == null || chartNumber.length() == 0) {
                    Toast.makeText(getActivity(), "차트번호를 입력해 주세요", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                BlabAPI.insertPatientForEMR(getActivity(), name, chartNumber, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                       // super.onSuccess(statusCode, headers, response);

                        try {
                            String code =  response.get(Constants.EMRAPI.CODE).toString();

                            if(code.equals(Constants.EMRAPI.CODE_200)){
                                Log.e(TAG,"응답 = "+response);

                                JSONObject patientInfo = response.getJSONObject((Constants.EMRAPI.DATA));

//                                HashMap<String, String> patientInfo = new HashMap<>();
//                                patientInfo.put("name", name);
//                                patientInfo.put("chartNumber", chartNumber);

                               // selectedPatientInfo = patientInfo;

                                SmartFiPreference.setSfPatientCustNo(getActivity(), patientInfo.getString("custNo"));
                                SmartFiPreference.setSfPatientName(getActivity(), patientInfo.getString("custNm"));
                                SmartFiPreference.setPatientChart(getActivity(),patientInfo.getString("chrtNo"));
//                                SmartFiPreference.setSfPatientName(getActivity(),name);

                                Toast.makeText(getActivity(), name + "님이 선택되었습니다", Toast.LENGTH_SHORT).show();
                                dismiss();
                                dialog.dismiss();

                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.replace(R.id.fragment_container, PhoneCameraFragment.newInstance());
                                ft.commit();
                            }else{
                                Log.e(TAG,"환자입력에러 !!");
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                       // super.onFailure(statusCode, headers, throwable, errorResponse);
                        Log.e(TAG,"환자입력에러 code = "+statusCode);
                        dialog.dismiss();
                    }

//                    @Override
//                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
//                        super.onFailure(statusCode, headers, throwable, errorResponse);
//                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        Log.e(TAG,"환자입력에러 code = "+statusCode);
                        dialog.dismiss();
                    }
                });
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }


    private void autoGetToken() {

        String id = SmartFiPreference.getDoctorId(getActivity());
        String pw = SmartFiPreference.getSfDoctorPw(getActivity());
        Log.w(TAG,"autoGetToken id = "+ id+" "+"pw = "+pw);

        BlabAPI.loginEMR(getActivity(), id,pw, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    String code =  response.get(Constants.EMRAPI.CODE).toString();
                    if(!code.equals(Constants.EMRAPI.CODE_200)){
                        Log.e(TAG,"응답에러, ID, pw 확인필요");
                        //강제로그아웃 후 재로그인하게..
                        //makeAutoLogout();
                        showLoginDialog();

                    }else{

                        try {

                            JSONObject data = (JSONObject) response.get(Constants.EMRAPI.DATA);
                            Log.w(TAG,"신규토큰 = "+data.getString("token"));
                            SmartFiPreference.setSfToken(getActivity(),data.getString("token"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG," 응답에러");
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
            }
        });
    }

//    private void makeAutoLogout() {
//        SmartFiPreference.setDoctorId(getActivity(), Constants.EMRAPI.UNDEFINED);
//        SmartFiPreference.setSfDoctorPw(getActivity(),Constants.EMRAPI.UNDEFINED);
//
//        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
//        LoginDialogFragment loginDialogFragment = LoginDialogFragment.newInstance();
//        changelogTx.add(loginDialogFragment, "Login");
//        changelogTx.commit();
//    }


    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = (int) (metrics.widthPixels * .85);
        int height = (int) (metrics.heightPixels * .60);
        window.setLayout(width, height);
        window.setGravity(Gravity.CENTER);
    }

    private void showLoginDialog() {

        SmartFiPreference.setDoctorId(getActivity(), Constants.EMRAPI.UNDEFINED);
        SmartFiPreference.setSfDoctorPw(getActivity(),Constants.EMRAPI.UNDEFINED);

        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        LoginDialogFragment loginDialogFragment = LoginDialogFragment.newInstance();
        changelogTx.add(loginDialogFragment, "Login");
        changelogTx.commit();

    }

}
