package com.doctorkeeper.smartfi.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Gravity;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.doctorkeeper.smartfi.util.SSLConnect;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.doctorkeeper.smartfi.Constants;
import com.doctorkeeper.smartfi.R;
import com.doctorkeeper.smartfi.services.VideoIntentService;
import com.doctorkeeper.smartfi.util.SmartFiPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.protocol.HTTP;
import okhttp3.Cache;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static com.doctorkeeper.smartfi.MainActivity.countDownTimer;
import static com.loopj.android.http.AsyncHttpClient.log;


public class BlabAPI {

    private static final String TAG = BlabAPI.class.getSimpleName();
    private static String mAcccessToken = null;
    private static AsyncHttpClient client = new AsyncHttpClient();

    // Instantiate the cache
    private static Cache mCache;

    // Instantiate the cache
    private static Activity mActivity;
    private static Context mContext;

    // Set up the network to use HttpURLConnection as the HTTP client.
    private static Network mNetwork;

//    public static HashMap<String,String> selectedPatientInfo;
//    public static HashMap<String,String> selectedDoctor;

    private static String mPatientId = null;
    private static String mHospitalId = null;
    private static String mCateId = null;

    public static Boolean patientSearchDisplayExtraOption = false;
    public static Boolean patientInsertExtraOption = false;
    public static Boolean doctorSelectExtraOption = false;
    public static Boolean shootingImageDisplayExtraOption = false;

    public static boolean isCameraOn = false;
    public static HashMap<String,String> selectedDoctor;

    public static boolean isListViewOnPhoneCamera = true;


    public static String getAccessToken() {
        //mAcccessToken = read_mAcccessToken();
        mAcccessToken = "AUTH_tke22f9541a14840efb828d660658c780d";
        return mAcccessToken;
    }

    public static String getHospitalId() {
        mHospitalId = "abc";
        return mHospitalId;
    }

    public static String getPatientId() {
        mPatientId = "kimcy";
        return mPatientId;
    }

    /**
     * category를 가져온다
     * @return
     */
    public static String getCategoryId() {
        mCateId = "picture";
        return mCateId;
    }

    private static final String getMimeType(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
    }

    /**
     * 컨테이너명(병원이름)+환자명+사진/비디오+진료날짜(사진찍은날짜?,로그인한날짜?)+디바이스명(폰/dslr)+파일명
     * @param relativeUrl
     * @return
     */
    private static String getAbsoluteUrl(String relativeUrl) {
        return Constants.Storage.BASE_URL + "/" + relativeUrl;
    }

    public static void loginEMR(Context con, String id, String pw){
        String url = Constants.EMRAPI.BASE_URL +Constants.EMRAPI.LOGIN;
        StringEntity jsonEntity = null;

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("userId", id);
            jsonParams.put("pwd", pw);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            jsonEntity = new StringEntity(jsonParams.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");

        client.post(con, url, jsonEntity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.w(TAG,"성공 = "+response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.w(TAG,"실패");
            }
        });
    }

    public static void loginEMR(Context con, String id, String pw, ResponseHandlerInterface responseHandler){

        String url = Constants.EMRAPI.BASE_URL +Constants.EMRAPI.LOGIN;
        StringEntity jsonEntity = null;

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("userId", id);
            jsonParams.put("pwd", pw);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            jsonEntity = new StringEntity(jsonParams.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        client.post(con, url, jsonEntity, "application/json",responseHandler);

    }

    public static void loginSyncEMR(Context con, String id, String pw, ResponseHandlerInterface responseHandler){

        SyncHttpClient syncClient = new SyncHttpClient();

        String url = Constants.EMRAPI.BASE_URL +Constants.EMRAPI.LOGIN;
        StringEntity jsonEntity = null;

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("userId", id);
            jsonParams.put("pwd", pw);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            jsonEntity = new StringEntity(jsonParams.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        syncClient.addHeader("Accept", "application/json");
        syncClient.addHeader("Content-Type", "application/json");
        syncClient.post(con, url, jsonEntity, "application/json",responseHandler);

    }

    /**
     * 환자명에 대해서는 like검색까지 지원한다.
     * 전체검색, like검
     * @param con
     * @param searchByName
     * @param searchByChart
     * @param responseHandler
     */
    public static void searchPatient(Context con, String searchByName, String searchByChart, ResponseHandlerInterface responseHandler){

        if(!getNetworkStatus(con)){
            Toast.makeText(con, con.getString(R.string.check_network), Toast.LENGTH_SHORT);
            return;
        }

        String url = Constants.EMRAPI.BASE_URL +Constants.EMRAPI.SEARCH_PATIENT;
        RequestParams requestParams = new RequestParams();
        requestParams.put(Constants.EMRAPI.UID, SmartFiPreference.getDoctorId(con));

        if(!searchByChart.equals("")){
            Log.w(TAG,"입력된 차트는 = "+searchByChart);
            requestParams.put(Constants.EMRAPI.CHART_NO, searchByChart);
        }

        if(!searchByName.equals("")){
            Log.w(TAG,"입력된 환자는 = "+searchByName);
            requestParams.put(Constants.EMRAPI.CUST_NM, searchByName);
        }

        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        client.addHeader("X-Auth-Token", SmartFiPreference.getSfToken(con));
        client.get(con, url, requestParams ,responseHandler);

    }

    public static void insertPatientForEMR(Context con, String Name, String Chart, ResponseHandlerInterface responseHandler){

        Log.w(TAG,"등록환자명 = "+Name);
        Log.w(TAG,"차트 = "+Chart);
        Log.w(TAG,"id = "+SmartFiPreference.getDoctorId(con));
        Log.w(TAG,"token = "+SmartFiPreference.getSfToken(con));
        if(!getNetworkStatus(con)){
            Toast.makeText(con, con.getString(R.string.check_network), Toast.LENGTH_SHORT);
            return;
        }

        String url = Constants.EMRAPI.BASE_URL +Constants.EMRAPI.INSERT_PATIENT;
        StringEntity jsonEntity = null;

        JSONObject jsonParams = new JSONObject();
        try {

            jsonParams.put(Constants.EMRAPI.UID, SmartFiPreference.getDoctorId(con));
            jsonParams.put(Constants.EMRAPI.CUST_NM, Name);
            jsonParams.put(Constants.EMRAPI.CHART_NO, Chart);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonEntity = new StringEntity(jsonParams.toString(),HTTP.UTF_8);

        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        client.addHeader("X-Auth-Token", SmartFiPreference.getSfToken(con));
        client.post(con, url, jsonEntity, "application/json",responseHandler);

    }

    public static void getPatientImages(Context con, int page, int pageSize, String custNo, ResponseHandlerInterface responseHandler){
        if(!getNetworkStatus(con)){
            Toast.makeText(con, con.getString(R.string.check_network), Toast.LENGTH_SHORT);
            return;
        }

        String url = Constants.EMRAPI.BASE_URL +Constants.EMRAPI.FIND_PHOTOS;
        RequestParams requestParams = new RequestParams();
        requestParams.put(Constants.EMRAPI.UID, SmartFiPreference.getDoctorId(con));
        requestParams.put(Constants.EMRAPI.P_IDX, Integer.toString(page));
        requestParams.put(Constants.EMRAPI.P_SIZE, Integer.toString(pageSize));
        requestParams.put(Constants.EMRAPI.CUST_NO, custNo);

        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        client.addHeader("X-Auth-Token", SmartFiPreference.getSfToken(con));

        client.get(con, url, requestParams ,responseHandler);

    }

    public static void getPatientImagesAll(Context con,  String custNo, ResponseHandlerInterface responseHandler){
        if(!getNetworkStatus(con)){
            Toast.makeText(con, con.getString(R.string.check_network), Toast.LENGTH_SHORT);
            return;
        }

        String url = Constants.EMRAPI.BASE_URL +Constants.EMRAPI.FIND_PHOTOS_ALL;
        RequestParams requestParams = new RequestParams();

        requestParams.put(Constants.EMRAPI.UID, SmartFiPreference.getDoctorId(con));
        requestParams.put(Constants.EMRAPI.CUST_NO, custNo);

        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        client.addHeader("X-Auth-Token", SmartFiPreference.getSfToken(con));
        client.get(con, url, requestParams ,responseHandler);

    }

    public static Activity getActivity() {
        return mActivity;
    }

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Activity activity, Context context) {
        mActivity = activity;
        mContext = context;
        Log.d(TAG, "mActivity = "+mActivity+" mContext = "+mContext);
    }

    public static void ktStoreObject(final String filePath, final String cameraKind, final  String fileName, final JsonHttpResponseHandler responseHandler) {
        mAcccessToken = getAccessToken(); //token
        mPatientId = getPatientId(); // 환자명
        mHospitalId = getHospitalId(); //병원id이면서 containerName

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                File f  = new File(filePath);
                String content_type  = getMimeType(filePath);

                OkHttpClient client = new OkHttpClient();
                RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url( getAbsoluteUrl(mHospitalId+"/"+ mPatientId+"/"+cameraKind+fileName))
                        .put(file_body)
                        .addHeader("X-Auth-Token",mAcccessToken)
                        .build();

                try {
                    okhttp3.Response response = client.newCall(request).execute();

                    //response.body()

                    if(!response.isSuccessful()){
                        // throw new IOException("Error : "+response);
                        responseHandler.onFailure(response.code(), null, response.toString(), null);
                    }else{
                        responseHandler.onSuccess(response.code(), null, "");
                        //구현완료
//                        getActivity().runOnUiThread(new Runnable() {
//                            public void run() {
//                                Toast.makeText(getActivity(),"이미지 저장 완료!",Toast.LENGTH_SHORT).show();
//                            }
//                        });
                    }

                    countDownTimer.cancel();
                    countDownTimer.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();

    }

    public static boolean getNetworkStatus(Context con){

        ConnectivityManager connectivityManager = (ConnectivityManager) con.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(mobile.isConnected() || wifi.isConnected()){
            return true;
        }else{
            return false;
        }

    }

    public static void loginDoctorKeeper(Context con, String id, String pw, JsonHttpResponseHandler responseHandler){
//        String url = "http://211.252.85.83:3100/api/v1/user/login";
        String url = Constants.bcnc.BASE_URL + "/api/v1/user/login";
        StringEntity jsonEntity = null;
        StringEntity jsonEntityUTF8 = null;
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("id", id);
            jsonParams.put("pwd", pw);
        } catch (JSONException e) {
            e.printStackTrace();
            log.w(TAG,e+"");
        }

        //            jsonEntity = new StringEntity(jsonParams.toString());
        jsonEntityUTF8 = new StringEntity(jsonParams.toString(), org.apache.http.protocol.HTTP.UTF_8);

        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        client.post(con, url, jsonEntityUTF8, "application/json",responseHandler);

    }

    public static void uploadImage(final String path, byte[] image, JsonHttpResponseHandler handler){
        String url = Constants.Storage.BASE_URL;
//        String url = "http://ssproxy.ucloudbiz.olleh.com/v1/AUTH_8c4583d1-b030-4cc2-8e65-7e747563dbeb/";
        String doctorId = SmartFiPreference.getDoctorId(getContext());
        String[] files = path.split("/");
        String fileName = files[files.length-1];
        final String urlTarget = url + doctorId + "/" + fileName;
        String token = SmartFiPreference.getSfToken(getContext());
        log.i(TAG,"url:::"+url);
        log.i(TAG,"doctorId:::"+doctorId+"token:::"+token);

        Thread t = new Thread(() -> {
            log.i(TAG,"path:::"+path);

            // Extract file name
            String filename=path.substring(path.lastIndexOf("/")+1);
            Log.v(TAG,"filename : " + filename);

            File f = new File(path);
            log.i(TAG,"f:::"+f);
            String content_type = getMimeType(path);
            OkHttpClient client = new OkHttpClient();
            RequestBody file_body = RequestBody.create(MediaType.parse(content_type), f);

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(urlTarget)
                    .put(file_body)
                    .addHeader("X-Auth-Token", token)
                    .build();

            try {
                okhttp3.Response response = client.newCall(request).execute();
                log.w(TAG, response.toString());
                //response.body()

                if (!response.isSuccessful()) {
                    Toast toast = Toast.makeText(mActivity.getBaseContext(), "이미지 업로드 실패..", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    // throw new IOException("Error : "+response);
                    handler.onFailure(response.code(), null, response.toString(), null);
                } else {
                    handler.onSuccess(response.code(), null, "");
                    getActivity().runOnUiThread(() -> {
                        Toast toast = Toast.makeText(mActivity.getBaseContext(), "이미지 업로드 성공!", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        String CheckFolderPath = mContext.getExternalFilesDir(null).getAbsolutePath() + "/uploadCheck";

                        // write empty file
                        Log.v(TAG,"check name : "+ CheckFolderPath + "/" + filename);
                        try {
                            FileOutputStream fos = new FileOutputStream(CheckFolderPath + "/" + filename, true);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                log.w(TAG, e.toString());
            }
        });
        t.start();

    }

    public static void getImageLists(Context con, JsonHttpResponseHandler handler) {
        String url = Constants.Storage.BASE_URL;
        String hostipalId = SmartFiPreference.getHospitalId(getContext());
        final String urlTarget = url + hostipalId + "/?limit=1000&format=json";
        String token = SmartFiPreference.getSfToken(getContext());
        log.i(TAG, "url:::" + urlTarget);

        client.addHeader("X-Auth-Token", token);
        client.get(con, urlTarget, handler);

    }

    public static void getPatientList(String name, String chartNo, JsonHttpResponseHandler handler){
        String url = Constants.bcnc.BASE_URL + "/api/v1/patient/search?";
        StringEntity jsonEntity = null;
        String hospitalId = SmartFiPreference.getHospitalId(getContext());
        String token = SmartFiPreference.getSfToken(getContext());

        RequestParams params = new RequestParams();
        params.put("id", hospitalId);
        if(!name.isEmpty()) {
            params.put("name", name);
        }
        if(!chartNo.isEmpty()) {
            params.put("chno", chartNo);
        }

        log.w(TAG,params.toString());
        client.addHeader("X-Auth-Token", token);
        client.get(getContext(), url, params,handler);
    }

    public static void insertPatient(Context con, String name, String chno, JsonHttpResponseHandler responseHandler){
        log.i(TAG, "insertPatient:::" + name + ":::" + chno);
        String hospitalId = SmartFiPreference.getHospitalId(getContext());
        String token = SmartFiPreference.getSfToken(getContext());
        String url = Constants.bcnc.BASE_URL + "/api/v1/patient/create";
        StringEntity jsonEntityUTF8;
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("id", hospitalId);
            jsonParams.put("name", name);
            jsonParams.put("chno", chno);
        } catch (JSONException e) {
            e.printStackTrace();
            log.w(TAG,e+"");
        }
        jsonEntityUTF8 = new StringEntity(jsonParams.toString(), org.apache.http.protocol.HTTP.UTF_8);

        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        client.addHeader("X-Auth-Token", token);
        client.post(con, url, jsonEntityUTF8, "application/json",responseHandler);

    }

    public static void getDoctorList(String name, String dno, JsonHttpResponseHandler handler){
        String url = Constants.bcnc.BASE_URL + "/api/v1/doctor/search?";
        StringEntity jsonEntity = null;
        String hospitalId = SmartFiPreference.getHospitalId(getContext());
        String token = SmartFiPreference.getSfToken(getContext());

        RequestParams params = new RequestParams();
        params.put("id", hospitalId);
        if(!name.isEmpty()) {
            params.put("name", name);
        }
        if(!dno.isEmpty()) {
            params.put("dno", dno);
        }

        log.w(TAG,params.toString());
        client.addHeader("X-Auth-Token", token);
        client.get(getContext(), url, params,handler);
    }

    public static void insertDoctor(Context con, String name, String dno, JsonHttpResponseHandler responseHandler){
        log.i(TAG, "insertPatient:::" + name + ":::" + dno);
        String hospitalId = SmartFiPreference.getHospitalId(getContext());
        String token = SmartFiPreference.getSfToken(getContext());
        String url = Constants.bcnc.BASE_URL + "/api/v1/doctor/create";
        StringEntity jsonEntityUTF8;
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("id", hospitalId);
            jsonParams.put("name", name);
            jsonParams.put("dno", dno);
        } catch (JSONException e) {
            e.printStackTrace();
            log.w(TAG,e+"");
        }
        jsonEntityUTF8 = new StringEntity(jsonParams.toString(), org.apache.http.protocol.HTTP.UTF_8);

        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        client.addHeader("X-Auth-Token", token);
        client.post(con, url, jsonEntityUTF8, "application/json",responseHandler);

    }

}
