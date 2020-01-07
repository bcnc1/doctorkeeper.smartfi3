package com.thinoo.drcamlink2.view.patient;

import android.app.DialogFragment;
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
import android.app.FragmentTransaction;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.thinoo.drcamlink2.Constants;
import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.madamfive.BlabAPI;
import com.thinoo.drcamlink2.madamfive.MadamfiveAPI;
import com.thinoo.drcamlink2.util.SmartFiPreference;
import com.thinoo.drcamlink2.view.log_in.LoginDialogFragment;
import com.thinoo.drcamlink2.view.phone_camera.PhoneCameraFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

//import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.patientInsertExtraOption;
import cz.msebera.android.httpclient.Header;

import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.patientSearchDisplayExtraOption;
import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.selectedPatientInfo;

public class PatientDialogFragment extends DialogFragment {

    private final String TAG = PatientDialogFragment.class.getSimpleName();
    private ArrayList<HashMap<String, String>> patientInfoList;
    private PatientDialogAdapter adapter;
    private ListView patientListView;

    private ProgressBar patient_list_progressBar;
    private boolean patientInsertExtraOption = false;
    private String name;
    private String chartNumber;


    public static PatientDialogFragment newInstance() {
        Bundle args = new Bundle();

        PatientDialogFragment f = new PatientDialogFragment();
        f.setArguments(args);
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //이전code. todo 삭제예정
//        if (MadamfiveAPI.getAccessToken() == null) {
//            showLoginDialog();
//        }
//        if (MadamfiveAPI.getBoardId() == null) {
//            showLoginDialog();
//        }
//        Log.i("+++++",MadamfiveAPI.getAccessToken()+":::"+MadamfiveAPI.getBoardId());
//end

        patientInsertExtraOption = SmartFiPreference.getSfInsertPatientOpt(getActivity());

        //환자검색버튼을 누르면 해당 다이얼로그 호출
        if(SmartFiPreference.getSfToken(getActivity()).equals(Constants.EMRAPI.UNDEFINED)){
            showLoginDialog();
        }



        getDialog().setTitle("환자 검색");
        final View view = inflater.inflate(R.layout.activity_search_patient, container, false);

        PatientDialogFragment.this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        final TextView nameTextView;
        final TextView chartNumberTextView;

        nameTextView = (TextView) view.findViewById(R.id.search_name);
        chartNumberTextView = (TextView) view.findViewById(R.id.search_chartNumber);

        adapter = new PatientDialogAdapter(getActivity());
        patientListView = (ListView) view.findViewById(R.id.patient_list);
        patientListView.setAdapter(adapter);
        patient_list_progressBar = (ProgressBar) view.findViewById(R.id.patient_list_progressBar);

        //todo 삭제예정
        //read_patientSearchDisplayExtraOption();
        //read_patientInsertExtraOption();
        //end

        view.findViewById(R.id.btn_search_patient).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                patient_list_progressBar.setVisibility(View.VISIBLE);

                String keyword = "";
                name = nameTextView.getText().toString();
                chartNumber = chartNumberTextView.getText().toString();
                boolean searchName = true;

//                Log.i(TAG, "name :::" + name + "chartNumber :::" + chartNumber);

                if (name != null && name.length() != 0) {
                    keyword = name;
                }

                if (chartNumber != null && chartNumber.length() != 0) {
                    keyword = chartNumber;
                    searchName = false;
                }

                //이전코드 삭제 예
//                if (keyword == null || keyword.length() == 0) {
//                    Toast.makeText(getActivity(), "이름 또는 차트번호를 넣어주세요", Toast.LENGTH_SHORT).show();
//                }
                //end

//                Log.i(TAG, "keyword :::" + keyword + "searchName :::" + searchName);

                nameTextView.clearFocus();
                chartNumberTextView.clearFocus();
                final String loginCheck = keyword;

                searchPatient(name, chartNumber);


                // TODO: 2020-01-06 죽음으로 수정해야 한다.
//                MadamfiveAPI.searchPatient(keyword, searchName, new JsonHttpResponseHandler() {
//                    @Override
//                    public void onStart() {
//                        Log.i(TAG, "onStart:");
//                    }
//
//                    @Override
//                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
////                        Log.i(TAG, "HTTPa:" + statusCode + responseString);
//                        patient_list_progressBar.setVisibility(View.INVISIBLE);
//
//                        try {
//                            JSONObject response = new JSONObject(responseString);
//                            JSONArray patientArray = response.getJSONArray("categories");
//
//                            if (patientArray.length() == 0) {
//
//                                if(patientInsertExtraOption) {
//
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
//
//                                    builder.setTitle("스마트파이");
//                                    builder.setMessage("해당 환자가 없습니다. 추가하시겠습니까?");
//
//                                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
//
//                                        public void onClick(final DialogInterface dialog, int which) {
//                                            final String name = nameTextView.getText().toString();
//                                            final String chartNumber = chartNumberTextView.getText().toString();
//
//                                            if (name == null || name.length() == 0) {
//                                                Toast.makeText(getActivity(), "이름을 입력해 주세요", Toast.LENGTH_SHORT).show();
//                                                dialog.dismiss();
//                                                return;
//                                            }
//
//                                            if (chartNumber == null || chartNumber.length() == 0) {
//                                                Toast.makeText(getActivity(), "차트번호를 입력해 주세요", Toast.LENGTH_SHORT).show();
//                                                dialog.dismiss();
//                                                return;
//                                            }
//
//                                            // TODO:2020-01-06 환자정보등록
//                                            MadamfiveAPI.insertPatient(name, chartNumber, new JsonHttpResponseHandler() {
//
//                                                @Override
//                                                public void onStart() {
//                                                    Log.i(TAG, "onStart:");
//                                                }
//
//                                                @Override
//                                                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
//
//                                                    Log.i(TAG, "HTTPa:" + statusCode + responseString);
//
//                                                    if (statusCode == 200) {
//
//                                                        try {
//                                                            JSONObject response = new JSONObject(responseString);
//                                                            JSONObject patientObject = response.getJSONObject("category");
//
//                                                            HashMap<String, String> patientInfo = new HashMap<>();
//                                                            patientInfo.put("name", patientObject.getString("name"));
//                                                            patientInfo.put("chartNumber", patientObject.getString("parentId"));
//                                                            patientInfo.put("categoryId", patientObject.getString("id"));
//
//                                                            selectedPatientInfo = patientInfo;
//
//                                                            Toast.makeText(getActivity(), name + "님이 선택되었습니다", Toast.LENGTH_SHORT).show();
//                                                            dismiss();
//                                                            dialog.dismiss();
//
//                                                            FragmentTransaction ft = getFragmentManager().beginTransaction();
//                                                            ft.replace(R.id.fragment_container, PhoneCameraFragment.newInstance());
//                                                            ft.commit();
//
//                                                        } catch (Exception e) {
//                                                        }
//                                                    }
//                                                }
//
//                                                @Override
//                                                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
//                                                    // If the response is JSONObject instead of expected JSONArray
//                                                    Log.i(TAG, "HTTPb:" + statusCode + response.toString());
//                                                    dialog.dismiss();
//                                                }
//                                            });
//
//                                        }
//                                    });
//
//                                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
//
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            dialog.dismiss();
//                                        }
//                                    });
//
//                                    AlertDialog alert = builder.create();
//                                    alert.show();
//
//                                }else{
//                                    if (loginCheck == null || loginCheck.length() == 0) {
//                                        Toast toast = Toast.makeText(getActivity(), "다시 로그인 해주세요", Toast.LENGTH_LONG);
//                                        toast.setGravity(Gravity.CENTER, 0, -100);
//                                        toast.show();
//                                    }else {
//                                        Toast toast = Toast.makeText(getActivity(), "해당 환자가 없습니다", Toast.LENGTH_LONG);
//                                        toast.setGravity(Gravity.CENTER, 0, -100);
//                                        toast.show();
//                                    }
//                                }
//
//                            } else {
//
//                                ArrayList<HashMap<String, String>> patientInfoList = new ArrayList<HashMap<String, String>>();
//
//                                for (int i = 0; i < patientArray.length(); i++) {
//                                    JSONObject patientObject = patientArray.getJSONObject(i);
////                                    Log.i(TAG, "Inside patientObject : " + patientObject.toString());
//                                    HashMap<String, String> patientInfo = new HashMap<>();
//                                    if(patientSearchDisplayExtraOption) {
//
//                                        patientInfo.put("name", patientObject.getString("name").trim());
//                                        patientInfo.put("chartNumber", patientObject.getString("parentId"));
//                                        patientInfo.put("categoryId", patientObject.getString("id"));
//                                        patientInfo.put("customerNumber", patientObject.getString("description"));
//                                        try {
//                                            JSONObject userData = patientObject.getJSONObject("userData");
//                                            patientInfo.put("birthDate", userData.getString("birthDate"));
//                                        }catch(Exception e){
//                                        }
//                                    }else{
//                                        patientInfo.put("name", patientObject.getString("name").trim());
//                                        patientInfo.put("chartNumber", patientObject.getString("parentId"));
//                                        patientInfo.put("categoryId", patientObject.getString("id"));
//                                        patientInfo.put("customerNumber", patientObject.getString("description"));
//                                    }
////                                    Log.i(TAG, "Inside HashMap : " + patientInfo.toString());
//                                    patientInfoList.add(patientInfo);
//                                }
//                                Log.i(TAG, "list received! === length:" + patientInfoList.size());
//                                adapter.setItems(patientInfoList);
//                                adapter.notifyDataSetChanged();
//                            }
//
//                        } catch (Exception e) {
//                        }
//
//                        if (statusCode == 400) {
//                            Toast toast = Toast.makeText(getActivity(), "해당 환자가 없습니다", Toast.LENGTH_LONG);
//                            toast.setGravity(Gravity.CENTER, 0, 0);
//                            toast.show();
//                        } else {
//                        }
//
//                    }
//
//                    @Override
//                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
//                        // If the response is JSONObject instead of expected JSONArray
//                        Log.i(TAG, "HTTPb:" + statusCode + response.toString());
//                    }
//                });
            }
        });

        patientListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                HashMap<String, String> patientInfo = (HashMap<String, String>) adapterView.getItemAtPosition(i);
                selectedPatientInfo = patientInfo;

                String name = selectedPatientInfo.get("name");
                Toast.makeText(getActivity(), name + "님이 선택되었습니다", Toast.LENGTH_LONG).show();

                //todo 삭제 예
                //MadamfiveAPI.write_patientInfo();
                SmartFiPreference.setPatientId(getActivity(), selectedPatientInfo.get("custNo"));
                SmartFiPreference.setPatientChart(getActivity(),selectedPatientInfo.get("chartNumber"));

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, PhoneCameraFragment.newInstance());
//                ft.addToBackStack(null);
                ft.commit();

                dismiss();
            }
        });

        return view;
    }

    private void searchPatient(String searchName, final String searchChart) {
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

                            for (int i = 0; i < patientInfo.length(); i++) {
                                JSONObject patient = patientInfo.getJSONObject(i);

                                HashMap<String, String> patientElement = new HashMap<>();

                                patientElement.put("custNo",patient.getString("custNo"));
                                patientElement.put("name",patient.getString("custNm"));
                                patientElement.put("chartNumber",patient.getString("chrtNo"));

                                patientInfoList.add(patientElement);
                            }

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

//            @Override
//            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                super.onFailure(statusCode, headers, responseString, throwable);
//                Log.w(TAG,"환자검색 실패 = "+responseString);
//            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.w(TAG,"환자검색 실패 = "+statusCode);

                if(statusCode == 401 || statusCode == 403){
                    // TODO: 2020-01-07 무한 반복될 경우는???
                    Log.e(TAG,"인증 및 권한오류");
                    autoGetToken();
                }
            }
        });
    }


    private void autoGetToken() {
        Log.w(TAG,"autoGetToken");
        String id = SmartFiPreference.getDoctorId(getActivity());
        String pw = SmartFiPreference.getSfDoctorPw(getActivity());
        BlabAPI.loginEMR(getActivity(), id,pw, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    String code =  response.get(Constants.EMRAPI.CODE).toString();
                    if(!code.equals(Constants.EMRAPI.CODE_200)){
                        Log.e(TAG,"응답에러, ID, pw 확인필요");
                    }else{

                        try {

                            JSONObject data = (JSONObject) response.get(Constants.EMRAPI.DATA);
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

        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        LoginDialogFragment loginDialogFragment = LoginDialogFragment.newInstance();
        changelogTx.add(loginDialogFragment, "Login");
        changelogTx.commit();

    }

}
