package com.thinoo.drcamlink2.madamfive;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.rackspacecloud.client.cloudfiles.FilesClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
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
    private static final String BASE_URL = "https://ssproxy.ucloudbiz.olleh.com/v1/AUTH_10b1107b-ce24-4cb4-a066-f46c53b474a3";

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
     * @return
     */
    public static String getAccessToken() {
        //mAcccessToken = read_mAcccessToken();
        mAcccessToken = "AUTH_tk9ac92ece3aeb46ba90c6f9beaeeb79e7";
        return mAcccessToken;
    }

    /**
     * 병원id를 가져온다
     * @return
     */
    public static String getHospitalId() {
        mHospitalId = "ab";
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

    private static String getMimeType(String path) {

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
    }

    /**
     * 컨테이너명(병원이름)+환자명+사진/비디오+진료날짜(사진찍은날짜?,로그인한날짜?)+디바이스명(폰/dslr)+파일명
     * @param relativeUrl
     * @return
     */
    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + "/" + relativeUrl;
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

    public static void ktStoreObject(final String filePath, final String cameraKind, final JsonHttpResponseHandler responseHandler) {
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
                        .url( getAbsoluteUrl(mHospitalId+"/"+ mPatientId+"/"+cameraKind+filePath))
                        .put(file_body)
                        .addHeader("X-Auth-Token",mAcccessToken)
                        .build();

                try {
                    okhttp3.Response response = client.newCall(request).execute();

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



                    deleteImage();

                    countDownTimer.cancel();
                    countDownTimer.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();

    }


    /**
     * 파일 path 기반의 API
     * @param filePath
     * @param cameraKind
     * @param responseHandler
     * token : 업로드 사용자별로 별도의 token이 필요
     */
    public static void createPut(final String filePath,final String cameraKind, final JsonHttpResponseHandler responseHandler) {

        mAcccessToken = getAccessToken();
        mPatientId = getPatientId();

//        final Map<String, String> params = new HashMap<String, String>();
//
//        String chartNumber = selectedPatientInfo.get("chartNumber");
//        chartNumber = chartNumber.replace("++++++",""); //환자 차트 번호
//        chartNumber.trim();
//
//        params.put("title", cameraKind);
//        params.put("type", "smartfi");
//        params.put("content", URLEncoder.encode(chartNumber));
//        params.put("accessToken", mAcccessToken);
//        //params.put("boardId", boardId);
//        params.put("categories[]", selectedPatientInfo.get("categoryId"));
//        params.put("currency", URLEncoder.encode(selectedPatientInfo.get("name")));
//
//        JSONObject attachmentJson = new JSONObject();
        final String fileName = UUID.randomUUID().toString();
        Log.i(TAG, "fileName: " + fileName);
//
//        try {
//            attachmentJson.put("guid", fileName);
//            attachmentJson.put("fileType", "image/jpeg");
//            attachmentJson.put("fileName", fileName);
//            attachmentJson.put("type", "none.ko");
//            params.put("attachments[]", URLEncoder.encode(attachmentJson.toString()));
//
//        } catch (JSONException e) {
//            Log.i("m5API",e.toString());
//        }
//
//        JSONObject userDataJson = new JSONObject();
//        try {
//            if(selectedDoctor!=null) {
//                userDataJson.put("doctorName", URLEncoder.encode(selectedDoctor.get("name")));
//                userDataJson.put("doctorNumber", URLEncoder.encode(selectedDoctor.get("doctorNumber")));
//            }
//            userDataJson.put("patient", URLEncoder.encode(selectedPatientInfo.get("name")));
//            params.put("userData", userDataJson.toString());
//        } catch (JSONException e) {
//            Log.i("m5API",e.toString());
//        }


        //VolleySingleton queue = VolleySingleton.getInstance(mContext);



//        final String url = "http://httpbin.org/get?param1=hello";
//
//// prepare the Request
//        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
//                new Response.Listener<JSONObject>()
//                {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        // display response
//                        Log.d("Response", response.toString());
//                    }
//                },
//                new Response.ErrorListener()
//                {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d("Error.Response", response);
//                    }
//                }
//        );


        StringRequest putRequest = new StringRequest(Request.Method.PUT,
                getAbsoluteUrl(getHospitalId() + "/"+getPatientId()+"/"+ getCategoryId()+"/"+"20191010"),
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", String.valueOf(error));
                    }
                }
        ) {

//            @Override
//            protected Map<String, String> getParams()
//            {
//                Map<String, String>  params = new HashMap<String, String> ();
//                params.put("name", "Alif");
//                params.put("domain", "http://itsalif.info");
//
//                return params;
//            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("X-Auth-Token", mAcccessToken);
                //return super.getHeaders();
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                  Log.d(TAG, "getBody");
//                final File root = new File((Environment.getExternalStorageDirectory() + File.separator + "DIR_NAME"));
//
//                FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
//
//
//
//                ByteArrayInputStream fileInputStream = new ByteArrayInputStream(dataFile.getContent());


                 byte[] buffer = new byte[4096];
                 BufferedInputStream bis = null;
                 ByteArrayOutputStream baos = null;
//                bytesAvailable = fileInputStream.available();
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                buffer = new byte[bufferSize

//                byte[] buffer;
//                int maxBufferSize = 1*1024*1024;

                try {
                     bis = new BufferedInputStream(new FileInputStream(filePath));
                     baos = new ByteArrayOutputStream();
                    int bytes = 0;
                    while ((bytes = bis.read(buffer, 0, buffer.length)) > 0) {
                        baos.write(buffer, 0, bytes);
                    }
                    //return super.getBody();
                    baos.close();
                    bis.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return baos.toByteArray();
            }


        };

        VolleySingleton.getInstance(mContext).addToRequestQueue(putRequest);






        //컨테이너명(병원이름)+환자명+사진/비디오+진료날짜(사진찍은날짜?,로그인한날짜?)+디바이스명(폰/dslr)+파일명
//        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.PUT,
//                getAbsoluteUrl("/ab/" + mPatientId+"/pictures"+"20191010"),
//                new Response.Listener<NetworkResponse>() {
//                    @Override
//                    public void onResponse(NetworkResponse response) {
//                        Log.i(TAG, "Response:%n %s" + new String(response.data));
//                        String resultResponse = new String(response.data);
//                        JSONObject resultJson = null;
//                        try {
//                            resultJson = new JSONObject(resultResponse);
//                            responseHandler.onSuccess(200, null, resultJson);
//                        } catch (JSONException e) {
//                            Log.i(TAG,e.toString());
//                            responseHandler.onSuccess(501, null, resultJson);
//                        }
//
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                responseHandler.onFailure(200, null, error.getLocalizedMessage(), null);
////                error.printStackTrace();
//            }
//        }) {
////            @Override
////            protected Map<String, String> getParams() {
////                return params;
////            }
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("X-Auth-Token", mAcccessToken);
//                return params;
//            }
//
//            @Override
//            protected Map<String, DataPart> getByteData() {
//
//                Map<String, DataPart> ImageParams = new HashMap<String, DataPart>();
//                // file name could found file base or direct access from real path
//                // for now just get bitmap data from ImageView
//                long imagename = System.currentTimeMillis(); //현재 시간을 이름으로 사용하기 위해..
//                Log.i(TAG, "imageBytes.length : " + imageBytes.length);
//                ImageParams.put("files[]", new DataPart(fileName, imageBytes, "image/jpeg"));
//
//                return ImageParams;
//            }
//        };
//
////        request.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 1, 1.0f));
//        request.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
//        VolleySingleton.getInstance(mContext).addToRequestQueue(request);

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity(),selectedPatientInfo.get("name")+"님 이미지 저장 완료!",Toast.LENGTH_SHORT).show();
            }
        });

        deleteImage();

        countDownTimer.cancel();
        countDownTimer.start();

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
