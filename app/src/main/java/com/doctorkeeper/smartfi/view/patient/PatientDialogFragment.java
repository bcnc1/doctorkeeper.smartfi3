package com.doctorkeeper.smartfi.view.patient;

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
import com.doctorkeeper.smartfi.Constants;
import com.doctorkeeper.smartfi.R;
import com.doctorkeeper.smartfi.network.MadamfiveAPI;
import com.doctorkeeper.smartfi.util.SmartFiPreference;
import com.doctorkeeper.smartfi.view.log_in.LoginDialogFragment;
import com.doctorkeeper.smartfi.view.phone_camera.PhoneCameraFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class PatientDialogFragment extends DialogFragment {

    private final String TAG = PatientDialogFragment.class.getSimpleName();
    private ArrayList<HashMap<String, String>> patientInfoList;
    private PatientDialogAdapter adapter;
    private ListView patientListView;

    private ProgressBar patient_list_progressBar;
    private boolean patientInsertExtraOption = false;
    private boolean patientSearchDisplayExtraOption = false;
    private String name;
    private String chartNumber;
    private TextView nameTextView;
    private TextView chartNumberTextView;
    private Boolean fixedLandscapeExtraOption;

    public static PatientDialogFragment newInstance() {
        Bundle args = new Bundle();

        PatientDialogFragment f = new PatientDialogFragment();
        f.setArguments(args);
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        patientInsertExtraOption = SmartFiPreference.getSfInsertPatientOpt(getActivity());
        patientSearchDisplayExtraOption = SmartFiPreference.getSfInsertPatientOpt(getActivity());
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

                if(MadamfiveAPI.getNetworkStatus(getActivity())){
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
                String name = patientInfo.get("name");
                Toast.makeText(getActivity(), name + "님이 선택되었습니다", Toast.LENGTH_LONG).show();

                SmartFiPreference.setPatientId(getActivity(), patientInfo.get("categoryId"));
                SmartFiPreference.setSfPatientCustNo(getActivity(),patientInfo.get("custNo"));
                SmartFiPreference.setSfPatientName(getActivity(), name);
                SmartFiPreference.setPatientChart(getActivity(),patientInfo.get("chartNumber"));

//                SmartFiPreference.setSfPatientCustNo(getActivity(), patientInfo.get("custNo"));
//                SmartFiPreference.setPatientChart(getActivity(),patientInfo.get("chartNumber"));
//                SmartFiPreference.setSfPatientName(getActivity(),name);

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

        MadamfiveAPI.searchPatient(searchName, searchChart, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                Log.i(TAG, "onStart:");
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                patient_list_progressBar.setVisibility(View.INVISIBLE);

                try {
                    JSONObject response = new JSONObject(responseString);
                    JSONArray patientArray = response.getJSONArray("categories");

                    if (patientInsertExtraOption == true && patientArray.length() == 0) {
                        addPatientInfo(searchName, searchChart);
                    } else {
                        ArrayList<HashMap<String, String>> patientInfoList = new ArrayList<HashMap<String, String>>();
                        for (int i = 0; i < patientArray.length(); i++) {
                            JSONObject patientObject = patientArray.getJSONObject(i);
                            Log.i(TAG, "Inside patientObject : " + patientObject.toString());
                            HashMap<String, String> patientInfo = new HashMap<>();
                            if(patientSearchDisplayExtraOption) {

                                patientInfo.put("name", patientObject.getString("name").trim());
                                patientInfo.put("chartNumber", patientObject.getString("parentId"));
                                patientInfo.put("categoryId", patientObject.getString("id"));
                                patientInfo.put("customerNumber", patientObject.getString("description"));
                                try {
                                    JSONObject userData = patientObject.getJSONObject("userData");
                                    patientInfo.put("birthDate", userData.getString("birthDate"));
                                }catch(Exception e){
                                }
                            }else{
                                patientInfo.put("name", patientObject.getString("name").trim());
                                patientInfo.put("chartNumber", patientObject.getString("parentId"));
                                patientInfo.put("categoryId", patientObject.getString("id"));
                                patientInfo.put("customerNumber", patientObject.getString("description"));
                            }
//                            Log.i(TAG, "Inside HashMap : " + patientInfo.toString());
                            patientInfoList.add(patientInfo);
                        }
//                        Log.i(TAG, "list received! === length:" + patientInfoList.size());
                        adapter.setItems(patientInfoList);
                        adapter.notifyDataSetChanged();
                    }

                } catch (Exception e) {
                }

                if (statusCode == 400) {
                    Toast toast = Toast.makeText(getActivity(), "해당 환자가 없습니다", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                }

            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i(TAG, "HTTPb:" + statusCode + response.toString());
            }
        });

    }

    private void addPatientInfo(String inputName, String inputChart) {
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

                MadamfiveAPI.insertPatient(name, chartNumber, new JsonHttpResponseHandler() {
                @Override
                public void onStart() {
//                    Log.i(TAG, "onStart:");
                }
                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                    Log.i(TAG, "HTTPa:" + statusCode + responseString);
                    if (statusCode == 200) {
                        try {
                            JSONObject response = new JSONObject(responseString);
                            JSONObject patientObject = response.getJSONObject("category");

//                            HashMap<String, String> patientInfo = new HashMap<>();
//                            patientInfo.put("name", patientObject.getString("name"));
//                            patientInfo.put("chartNumber", patientObject.getString("parentId"));
//                            patientInfo.put("categoryId", patientObject.getString("id"));
//                            selectedPatientInfo = patientInfo;

                            SmartFiPreference.setPatientId(getActivity(), patientObject.getString("id"));
                            SmartFiPreference.setSfPatientName(getActivity(), patientObject.getString("name"));
                            SmartFiPreference.setPatientChart(getActivity(),patientObject.getString("parentId"));
                            SmartFiPreference.setSfPatientCustNo(getActivity(),patientObject.getString("description"));

                            Toast.makeText(getActivity(), name + "님이 선택되었습니다", Toast.LENGTH_SHORT).show();
                            dismiss();
                            dialog.dismiss();

                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.replace(R.id.fragment_container, PhoneCameraFragment.newInstance());
                            ft.commit();

                        } catch (Exception e) {
                        }
                    }
                }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                    // If the response is JSONObject instead of expected JSONArray
                    Log.i(TAG, "HTTPb:" + statusCode + response.toString());
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

    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = (int) (metrics.widthPixels * .85);
        int height = (int) (metrics.heightPixels * .60);
        fixedLandscapeExtraOption = SmartFiPreference.getSfDisplayLandscapeOpt(MadamfiveAPI.getActivity());
        if(fixedLandscapeExtraOption){
            height = (int) (metrics.heightPixels * .90);
            width= (int) (metrics.widthPixels * .60);
        }else {
            height = (int) (metrics.heightPixels * .60);
            width= (int) (metrics.widthPixels * .85);
        }
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
