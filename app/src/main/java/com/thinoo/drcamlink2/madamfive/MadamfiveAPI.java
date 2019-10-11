package com.thinoo.drcamlink2.madamfive;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.android.volley.toolbox.JsonObjectRequest;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.thinoo.drcamlink2.util.SSLConnect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cz.msebera.android.httpclient.conn.ssl.SSLConnectionSocketFactory;

import static com.thinoo.drcamlink2.MainActivity.countDownTimer;


public class MadamfiveAPI {

    private static final String TAG = MadamfiveAPI.class.getSimpleName();
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

    private static final String BASE_URL = "http://api.doctorkeeper.com:7818/v1";
    private static final String mAPIKey = "NTlFUG5qdkNBV1VJWDRjL0tBMU5TMlZOY1UvaTBVQVVVU3h2eW5aRlkwND0K.gXttoBDWfyPc3z92HxRurTXo56s4NBT2khGTsBskfYM=";
    private static String boardId = null;

    private static String mAcccessToken = null;

    public static Boolean patientSearchDisplayExtraOption = false;
    public static Boolean patientInsertExtraOption = false;
    public static Boolean doctorSelectExtraOption = false;
    public static Boolean shootingImageDisplayExtraOption = false;

    public static boolean isCameraOn = false;

    public static boolean isListViewOnPhoneCamera = true;

    public static String getAccessToken() {
        mAcccessToken = read_mAcccessToken();
        return mAcccessToken;
    }

    public static String getBoardId() {
        String boardId = read_boardId();
        return boardId;
    }

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
    }

    public static void login(String username, String password, final JsonHttpResponseHandler responseHandler) {

        HashMap<String, String> params = new HashMap<String, String>();

        params.put("username", username);
        params.put("password", password);
        params.put("fetchBoards", "true");

        JsonObjectRequest request = new JsonObjectRequest(getAbsoluteUrl("/login"), new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            if (response.has("accessToken") == true) {
                                mAcccessToken = URLDecoder.decode(response.getString("accessToken"));

                                write_mAcccessToken(mAcccessToken);

                                Log.i(TAG, "mAcccessToken : " + mAcccessToken);
                                responseHandler.onSuccess(200, null, response.toString());

                                JSONArray boards = response.getJSONArray("boards");
                                for(int i=0;i<boards.length();i++){
                                    JSONObject board = boards.getJSONObject(i);
                                    Log.i(TAG,"Inside JSON Array");
                                    Log.i(TAG,"Inside type value : "+ board.get("type").toString());

                                    if(board.get("type").toString().equals("hospital")){
                                        boardId = board.get("id").toString();
                                        write_boardId(boardId);
                                        Log.i(TAG,"Board Id : =========" + boardId);
                                        break;
                                    }
                                }

                            }else{
                                responseHandler.onSuccess(400, null, response.toString());
                            }
                            Log.i(TAG, "Response:%n %s" + response.toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Error: " + error.getMessage());
                responseHandler.onFailure(0, null, error.getLocalizedMessage(), null);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("X-Madamfive-APIKey", mAPIKey);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(mContext).addToRequestQueue(request);

    }

    public static void createPost(String fileName, String cameraKind, JsonHttpResponseHandler responseHandler) throws FileNotFoundException,
            IOException {

        byte[] buffer = new byte[4096];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytes = 0;
        while ((bytes = bis.read(buffer, 0, buffer.length)) > 0) {
            baos.write(buffer, 0, bytes);
        }
        createPost(baos, cameraKind, responseHandler);
        baos.close();
        bis.close();

    }

    public static void createPost(Bitmap bitmap, String cameraKind, JsonHttpResponseHandler responseHandler) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        createPost(baos, cameraKind, responseHandler);

    }

    public static void createPost(ByteArrayOutputStream baos, String cameraKind, JsonHttpResponseHandler responseHandler) {
        final byte[] imageBytes = baos.toByteArray();
        createPost(imageBytes, cameraKind, responseHandler);
    }

    // savePhoto()에서 사용되는 Method =====================================================================================
    public static void createPost(final byte[] imageBytes, final String cameraKind, final JsonHttpResponseHandler responseHandler) {

        mAcccessToken = getAccessToken();
        boardId = getBoardId();

        final Map<String, String> params = new HashMap<String, String>();

        String chartNumber = selectedPatientInfo.get("chartNumber");
        chartNumber = chartNumber.replace("++++++",""); //환자 차트 번호
        chartNumber.trim();

        params.put("title", cameraKind);
        params.put("type", "smartfi");
        params.put("content", URLEncoder.encode(chartNumber));
        params.put("accessToken", mAcccessToken);
        params.put("boardId", boardId);
        params.put("categories[]", selectedPatientInfo.get("categoryId"));
        params.put("currency", URLEncoder.encode(selectedPatientInfo.get("name")));

        JSONObject attachmentJson = new JSONObject();
        final String fileName = UUID.randomUUID().toString();

        try {
            attachmentJson.put("guid", fileName);
            attachmentJson.put("fileType", "image/jpeg");
            attachmentJson.put("fileName", fileName);
            attachmentJson.put("type", "none.ko");
            params.put("attachments[]", URLEncoder.encode(attachmentJson.toString()));

        } catch (JSONException e) {
            Log.i("m5API",e.toString());
        }

        JSONObject userDataJson = new JSONObject();
        try {
            if(selectedDoctor!=null) {
                userDataJson.put("doctorName", URLEncoder.encode(selectedDoctor.get("name")));
                userDataJson.put("doctorNumber", URLEncoder.encode(selectedDoctor.get("doctorNumber")));
            }
            userDataJson.put("patient", URLEncoder.encode(selectedPatientInfo.get("name")));
            params.put("userData", userDataJson.toString());
        } catch (JSONException e) {
            Log.i("m5API",e.toString());
        }


        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, getAbsoluteUrl("/boards/" + boardId+"/posts"),
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Log.i(TAG, "Response:%n %s" + new String(response.data));
                        String resultResponse = new String(response.data);
                        JSONObject resultJson = null;
                        try {
                            resultJson = new JSONObject(resultResponse);
                            responseHandler.onSuccess(200, null, resultJson);
                        } catch (JSONException e) {
                            Log.i(TAG,e.toString());
                            responseHandler.onSuccess(501, null, resultJson);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseHandler.onFailure(200, null, error.getLocalizedMessage(), null);
//                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("X-Madamfive-APIKey", mAPIKey);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {

                Map<String, DataPart> ImageParams = new HashMap<String, DataPart>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                Log.i(TAG, "imageBytes.length : " + imageBytes.length);
                ImageParams.put("files[]", new DataPart(fileName, imageBytes, "image/jpeg"));

                return ImageParams;
            }
        };

//        request.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 1, 1.0f));
        request.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(mContext).addToRequestQueue(request);

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity(),selectedPatientInfo.get("name")+"님 이미지 저장 완료!",Toast.LENGTH_SHORT).show();
            }
        });

        deleteImage();

        countDownTimer.cancel();
        countDownTimer.start();

    }

    public static void getImageURL (String page, final JsonHttpResponseHandler responseHandler){

        if (mAcccessToken==null) {
            return;
        }

        boardId = getBoardId();

        String queryString = "type=smartfi&fetchTotalCount=true&orderDirection=desc&mode=all";
        queryString = queryString+"&limit=100&page="+page+"&accessToken="+URLEncoder.encode(mAcccessToken);
        String relativeURL = "boards/"+boardId+"/posts?"+queryString;

        Log.i("URL=====", getAbsoluteUrl(relativeURL).toString());

        JsonObjectRequest request = new JsonObjectRequest(getAbsoluteUrl(relativeURL), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            if (response.has("total")) {
                                responseHandler.onSuccess(200, null, response.toString());
                            }else{
                                responseHandler.onSuccess(400, null, response.toString());
                            }
//                            Log.i(TAG, "Response:%n %s" + response.get("total").toString());
                        } catch (Exception e) {
//                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Error: " + error.getMessage());
                responseHandler.onFailure(0, null, error.getLocalizedMessage(), null);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("X-Madamfive-APIKey", mAPIKey);
                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(mContext).addToRequestQueue(request);

    }

    public static void searchPatient (String keyword,boolean searchByName, final JsonHttpResponseHandler responseHandler){

        mAcccessToken = getAccessToken();
        boardId = getBoardId();

        String queryString = "";
        if(searchByName){
            queryString = "type=patient&keyword="+URLEncoder.encode(keyword);
        }else{
            queryString = "type=patient&keyword="+URLEncoder.encode(keyword)+"&parentId="+URLEncoder.encode(keyword);
        }
        queryString = queryString+"&accessToken="+URLEncoder.encode(mAcccessToken);
        String relativeURL = "boards/" + boardId + "/categories/search?" + queryString;
        if(keyword.isEmpty()) {
            relativeURL = "boards/" + boardId + "/categories/search?limit=20&" + queryString;
        }

        Log.i("URL=====", getAbsoluteUrl(relativeURL).toString());

        JsonObjectRequest request = new JsonObjectRequest(getAbsoluteUrl(relativeURL), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            responseHandler.onSuccess(200, null, response.toString());
                            Log.i(TAG, "Response:%n %s" + response.toString());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

//                        callBack.onSuccess(imageInfoList);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Error: " + error.getMessage());
                responseHandler.onFailure(0, null, error.getLocalizedMessage(), null);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("X-Madamfive-APIKey", mAPIKey);
                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(mContext).addToRequestQueue(request);

    }

    public static void searchDoctor (final JsonHttpResponseHandler responseHandler){

        mAcccessToken = getAccessToken();
        boardId = getBoardId();

        String relativeURL = "https://dashboard.doctorkeeper.com/v1/boards/SVCBoard_371113691246594/my/posts?limit=200&accessToken="+URLEncoder.encode(mAcccessToken);
//        Log.i("URL=====", relativeURL.toString());

        SSLConnect ssl = new SSLConnect();
        ssl.postHttps(relativeURL,1000,1000);

        JsonObjectRequest request = new JsonObjectRequest(relativeURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            responseHandler.onSuccess(200, null, response.toString());
                            Log.i(TAG, "Response:%n %s" + response.toString());

                        } catch (Exception e) {
                            Log.i(TAG,"Errr:::"+e.toString());
                        }

//                        callBack.onSuccess(imageInfoList);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Error: " + error.getMessage());
                responseHandler.onFailure(0, null, error.getLocalizedMessage(), null);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("X-Madamfive-APIKey", mAPIKey);
                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(mContext).addToRequestQueue(request);

    }

    public static void insertPatient (String name, String chartNumber, final JsonHttpResponseHandler responseHandler){

        mAcccessToken = getAccessToken();
        boardId = getBoardId();

        String queryString = "?name="+URLEncoder.encode(name)+"&code="+URLEncoder.encode(chartNumber);
        queryString = queryString+"&parentId="+URLEncoder.encode(chartNumber)+"&description=";
        queryString = queryString+"&checkParentId=true"+"&type=patient&published=true";
        queryString = queryString+"&accessToken="+URLEncoder.encode(mAcccessToken);

        String relativeURL = "boards/"+boardId+"/categories"+queryString;

        Log.i("URL=====", getAbsoluteUrl(relativeURL).toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,getAbsoluteUrl(relativeURL), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            responseHandler.onSuccess(200, null, response.toString());
                            Log.i(TAG, "Response:%n %s" + response.toString());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

//                        callBack.onSuccess(imageInfoList);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Error: " + error.getMessage());
                responseHandler.onFailure(0, null, error.getLocalizedMessage(), null);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("X-Madamfive-APIKey", mAPIKey);
                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(mContext).addToRequestQueue(request);

    }

    private static String read_mAcccessToken (){

        try {
            FileInputStream input = getActivity().openFileInput("token.txt");
            byte[] b = new byte[input.available()];
            input.read(b);
            mAcccessToken = new String(b);
            input.close();
        }catch (Exception e){
        }

        return mAcccessToken;
    }

    private static void write_mAcccessToken (String mAcccessToken){

        FileOutputStream outputStream;
        try {
            outputStream = getActivity().openFileOutput("token.txt", Context.MODE_PRIVATE);
            outputStream.write(mAcccessToken.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String read_boardId (){

        String boardId = null;
        try {
            FileInputStream input = getActivity().openFileInput("board.txt");
            byte[] b = new byte[input.available()];
            input.read(b);
            boardId = new String(b);
            input.close();
        }catch (Exception e){
        }

        return boardId;
    }

    private static void write_boardId (String boardId){

        FileOutputStream outputStream;
        try {
            outputStream = getActivity().openFileOutput("board.txt", Context.MODE_PRIVATE);
            outputStream.write(boardId.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void write_patientInfo(){

        if(selectedPatientInfo!=null) {
            try {
                ObjectOutputStream outputStream = new ObjectOutputStream(getActivity().openFileOutput("patient.txt", Context.MODE_PRIVATE));
                outputStream.writeObject(selectedPatientInfo);
                outputStream.close();
            } catch (Exception e) {
            }
        }
    }

    public static void read_patientInfo(){

        HashMap<String,String> data = new HashMap<>();
        try {
            FileInputStream input = getActivity().openFileInput("patient.txt");
            ObjectInputStream ois = new ObjectInputStream(input);
            selectedPatientInfo = (HashMap<String, String>) ois.readObject();
            ois.close();
        }catch (Exception e){
        }
//        Log.i(TAG,"selectedPatientInfo++++"+selectedPatientInfo);
    }

    public static void write_doctorInfo(){

        if(selectedDoctor!=null) {
            try {
                ObjectOutputStream outputStream = new ObjectOutputStream(getActivity().openFileOutput("doctor.txt", Context.MODE_PRIVATE));
                outputStream.writeObject(selectedDoctor);
                outputStream.close();
            } catch (Exception e) {
            }
        }
    }

    public static void read_doctorInfo(){

        HashMap<String,String> data = new HashMap<>();
        try {
            FileInputStream input = getActivity().openFileInput("doctor.txt");
            ObjectInputStream ois = new ObjectInputStream(input);
            selectedDoctor = (HashMap<String, String>) ois.readObject();
            ois.close();
        }catch (Exception e){
        }
//        Log.i(TAG,"selectedPatientInfo++++"+selectedPatientInfo);
    }

    public static void read_patientSearchDisplayExtraOption(){
        File file = getActivity().getFileStreamPath("option1.txt");
        if(file.exists()) {
            try {
                FileInputStream input = getActivity().openFileInput("option1.txt");
                ObjectInputStream ois = new ObjectInputStream(input);
                patientSearchDisplayExtraOption = (Boolean) ois.readObject();
                ois.close();
            } catch (Exception e) {
            }
        }else{
            patientSearchDisplayExtraOption = false;
        }

    }

    public static void write_patientSearchDisplayExtraOption(){
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(getActivity().openFileOutput("option1.txt", Context.MODE_PRIVATE));
            outputStream.writeObject(patientSearchDisplayExtraOption);
            outputStream.close();
        } catch (Exception e) {
        }

    }

    public static void read_patientInsertExtraOption(){
        File file = getActivity().getFileStreamPath("option2.txt");
        if(file.exists()) {
            try {
                FileInputStream input = getActivity().openFileInput("option2.txt");
                ObjectInputStream ois = new ObjectInputStream(input);
                patientInsertExtraOption = (Boolean) ois.readObject();
                ois.close();
            } catch (Exception e) {
            }
        }else{
            patientInsertExtraOption = false;
        }

    }

    public static void write_patientInsertExtraOption(){
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(getActivity().openFileOutput("option2.txt", Context.MODE_PRIVATE));
            outputStream.writeObject(patientInsertExtraOption);
            outputStream.close();
        } catch (Exception e) {
        }

    }

    public static void read_doctorSelectExtraOption(){
        File file = getActivity().getFileStreamPath("option3.txt");
        if(file.exists()) {
            try {
                FileInputStream input = getActivity().openFileInput("option3.txt");
                ObjectInputStream ois = new ObjectInputStream(input);
                doctorSelectExtraOption = (Boolean) ois.readObject();
                ois.close();
            } catch (Exception e) {
            }
        }else{
            patientInsertExtraOption = false;
        }

    }

    public static void write_doctorSelectExtraOption(){
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(getActivity().openFileOutput("option3.txt", Context.MODE_PRIVATE));
            outputStream.writeObject(doctorSelectExtraOption);
            outputStream.close();
        } catch (Exception e) {
        }

    }

    public static void read_ShootingImageDisplayExtraOption(){
        File file = getActivity().getFileStreamPath("option4.txt");
        if(file.exists()) {
            try {
                FileInputStream input = getActivity().openFileInput("option4.txt");
                ObjectInputStream ois = new ObjectInputStream(input);
                shootingImageDisplayExtraOption = (Boolean) ois.readObject();
                ois.close();
            } catch (Exception e) {
            }
        }else{
            patientInsertExtraOption = false;
        }

    }

    public static void write_ShootingImageDisplayExtraOption(){
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(getActivity().openFileOutput("option4.txt", Context.MODE_PRIVATE));
            outputStream.writeObject(shootingImageDisplayExtraOption);
            outputStream.close();
        } catch (Exception e) {
        }

    }

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