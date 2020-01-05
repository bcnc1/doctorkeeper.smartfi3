package com.thinoo.drcamlink2.madamfive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

//import com.android.volley.AuthFailureError;
//import com.android.volley.Cache;
//import com.android.volley.DefaultRetryPolicy;
//import com.android.volley.Network;
//import com.android.volley.NetworkResponse;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.thinoo.drcamlink2.Constants;
import com.thinoo.drcamlink2.services.VideoIntentService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import okhttp3.Cache;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static com.thinoo.drcamlink2.MainActivity.countDownTimer;

//https://stackoverflow.com/questions/52149016/how-to-upload-video-in-android-using-volley(비디오업로드 참고)
//https://stackoverflow.com/questions/49166938/record-and-upload-audio-and-video-files-to-the-server-in-android
//http://www.itsalif.info/content/android-volley-tutorial-http-get-post-put 멀티파트리퀘스트 사용불가
//https://www.youtube.com/watch?v=K48jnbM8yS4 large file 동영
//file:///Users/kimcy/dev/kt-storage/ucloudstorage-sdk%20(1)/java_docs/index.html kt sdk 문서

public class BlabAPI {
    private static final String TAG = BlabAPI.class.getSimpleName();
   // private static final String BASE_URL = "https://ssproxy.ucloudbiz.olleh.com/v1/AUTH_10b1107b-ce24-4cb4-a066-f46c53b474a3";

    private static String mAcccessToken = null;

    private static AsyncHttpClient client = new AsyncHttpClient();

    // Instantiate the cache
    private static Cache mCache;

    // Instantiate the cache
    private static Activity mActivity;
    private static Context mContext;

    // Set up the network to use HttpURLConnection as the HTTP client.
    private static Network mNetwork;

    public static HashMap<String,String> selectedPatientInfo;
    public static HashMap<String,String> selectedDoctor;


    private static String mPatientId = null;
    private static String mHospitalId = null;
    private static String mCateId = null;


    public static Boolean patientSearchDisplayExtraOption = false;
    public static Boolean patientInsertExtraOption = false;
    public static Boolean doctorSelectExtraOption = false;
    public static Boolean shootingImageDisplayExtraOption = false;

    public static boolean isCameraOn = false;

    public static boolean isListViewOnPhoneCamera = true;


    /**
     * 토큰을 가져온다
     * curl -i -H'X-Auth-New-Token:true' -H'x-storage-user:doctorkeeper:abc' -H'x-storage-pass: abc1234' https://ssproxy.ucloudbiz.olleh.com/auth/v1.0 -XGET
     * @return
     */
    public static String getAccessToken() {
        //mAcccessToken = read_mAcccessToken();
        mAcccessToken = "AUTH_tke22f9541a14840efb828d660658c780d";
        return mAcccessToken;
    }

    /**
     * 병원id를 가져온다
     * @return
     */
    public static String getHospitalId() {
        mHospitalId = "abc";
        return mHospitalId;
    }

    /**
     * 환자id를 가져온다
     * @return
     */
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

//        client.post(con, url, jsonEntity, "application/json", new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                super.onSuccess(statusCode, headers, response);
//                Log.w(TAG,"성공 = "+response);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                super.onFailure(statusCode, headers, responseString, throwable);
//                Log.w(TAG,"실패");
//            }
//        });
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

    public static void storeObject(final String container,final String cameraKind, final JsonHttpResponseHandler responseHandler) {
        FilesClient smartFiClient = new FilesClient("ab", "1234"); //계정명(병원명==컨테이너명), 패스워드
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


                      //추후구현
//                    deleteImage();
//
                    countDownTimer.cancel();
                    countDownTimer.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();

    }


    public static void S3UploadIntentService(final String filePath, final String cameraKind, final  String fileName, final JsonHttpResponseHandler responseHandler) {
        Intent it = new Intent(getActivity(), VideoIntentService.class);
        //it.putExtra()
        getActivity().startService(it);

    }


//    public static void createPost(ByteArrayOutputStream baos, String cameraKind, JsonHttpResponseHandler responseHandler) {
//        final byte[] imageBytes = baos.toByteArray();
//        createPost(imageBytes, cameraKind, responseHandler);
//    }



    /**
     * 파일 패스 기반의 업로드, 파일패스를 받아 바이트arry
     * @param fileName
     * @param cameraKind
     * @param responseHandler
     * @throws FileNotFoundException
     * @throws IOException
     */
//    public static void createPost(String fileName, String cameraKind, JsonHttpResponseHandler responseHandler) throws FileNotFoundException,
//            IOException {
//
//        byte[] buffer = new byte[4096];
//        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        int bytes = 0;
//        while ((bytes = bis.read(buffer, 0, buffer.length)) > 0) {
//            baos.write(buffer, 0, bytes);
//        }
//        createPost(baos, cameraKind, responseHandler);
//        baos.close();
//        bis.close();
//
//    }

    /**
     *  이미지삭제
     */
    public static void deleteImage() {

        File myDir = getActivity().getExternalFilesDir(Environment.getExternalStorageState());
        if(myDir.exists()&&myDir.isDirectory()){
            File[] files = myDir.listFiles();
            int numberOfFiles = files.length;
            Arrays.sort(files, new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    if(((File)o1).lastModified() > ((File)o2).lastModified()) {
                        return -1;
                    }else if(((File)o1).lastModified() < ((File)o2).lastModified()){
                        return  +1;
                    }else {
                        return 0;
                    }
                }
            });
            for(int i=20;i<numberOfFiles;i++){
                if(files[i].isFile()==true){
                    files[i].delete();
                }
            }
        }
    }
}
