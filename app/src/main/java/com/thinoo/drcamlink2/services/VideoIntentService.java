package com.thinoo.drcamlink2.services;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.thinoo.drcamlink2.Constants;
import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.activities.FileExploreActivity;
import com.thinoo.drcamlink2.madamfive.BlabAPI;
import com.thinoo.drcamlink2.models.PhotoModel;
import com.thinoo.drcamlink2.util.DisplayUtil;
import com.thinoo.drcamlink2.util.SmartFiPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class VideoIntentService extends IntentService {

    private static final String TAG = "VideoIntentService";
    private static final String EXTRA_VIDEO_ID = "com.thinoo.drcamlink2.services.extra.video.id";
    private static String mAcccessToken = null;
    private static String mPatientId = null;
    private static String mHospitalId = null;
    private static String mDate =  null;
    private static String mMediaType = null;
    //private static int mNotiId = Constants.Notification.NOTIFICATION_VIDEO_ID;
    private static Context mCon;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *  name Used to name the worker thread, important only for debugging.
     */
    public VideoIntentService() {
        super("VideoIntentService");
    }


    public static void startUploadVideo(Context context, long id) {
        mCon = context;
        Intent intent = new Intent(context, VideoIntentService.class);
        intent.putExtra(EXTRA_VIDEO_ID, id);
        context.startService(intent);

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.d(TAG,"onHandleIntent 호출");
        mAcccessToken = SmartFiPreference.getSfToken(getApplicationContext());
        mDate = new SimpleDateFormat("yyyyMM").format(new Date());
        mHospitalId = SmartFiPreference.getHospitalId(getApplicationContext());

        if (intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null){
                long id = extras.getLong(EXTRA_VIDEO_ID);
                Log.d(TAG,"id = "+id);
                PhotoModel photoModel = PhotoModelService.getPhotoModel(id);

                mPatientId = photoModel.getCustNo();

                makeNoti("video uploading...", 0);

                uploadThumbnail(photoModel, false);


            }

        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e(TAG, "비디오 인텐트서비스 종료");
    }


    private void uploadThumbnail(final PhotoModel pm, final boolean isRetry) {



        final String filePath = pm.getThumbpath();

        if(pm.getMode() == 0 || pm.getMode() == 1){
            mMediaType = "pictures";
        }else if(pm.getMode() == 2){
            mMediaType = "videos";
        }

        final String fileName = pm.getFilename();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                pm.setThumbUploading(1);

                File f  = new File(filePath);
                String content_type  = DisplayUtil.getMimeType(filePath);

                OkHttpClient client = new OkHttpClient();
                RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url( getAbsoluteUrl(mHospitalId+"$"+ mPatientId+
                                "/thumbnail"+"/"+mHospitalId+"/"+ mPatientId + "/videos/"+ mDate+"/"+ fileName))
                        .put(file_body)
                        .addHeader("X-Auth-Token",mAcccessToken)
                        .build();


                try {
                    okhttp3.Response response = client.newCall(request).execute();


                    if(!response.isSuccessful()){

                        Log.d(TAG, " 썸네일, response = "+response.code());
                        if(response.code() == 401 && ! isRetry){
                            Log.w(TAG, " 신규토큰 필요 ");

                            getTokenNupLoad(pm);
                            return;
                        }

                        pm.setThumbUploading(3);

                        makeNoti("uploading fail", 1);

                    }else{

                        Log.d(TAG, " 썸네일, 업로드 성공 ");
                        pm.setThumbUploading(2);

                        uploadVideo(pm);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    pm.setThumbUploading(3); //업로드실패
                    makeNoti("uploading fail. please check network", 1);
                }
            }
        });

        t.start();
    }

    private void getTokenNupLoad(final PhotoModel pm) {
        String id = SmartFiPreference.getDoctorId(mCon);
        String pw = SmartFiPreference.getSfDoctorPw(mCon);

        Log.w(TAG,"id =  "+id);
        Log.w(TAG,"pw =  "+pw);
        BlabAPI.loginSyncEMR(mCon, id,pw, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    String code =  response.get(Constants.EMRAPI.CODE).toString();
                    if(!code.equals(Constants.EMRAPI.CODE_200)){
                        Log.w(TAG,"응답실패 ");
                        //todo 신규로그인이 필요
                        pm.setThumbUploading(3);

                        makeNoti("로그인이 필요합니다.", 0);


                    }else{

                        try {

                            JSONObject data = (JSONObject) response.get(Constants.EMRAPI.DATA);
                            SmartFiPreference.setSfToken(mCon,data.getString("token"));

                            uploadThumbnail(pm, true);

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

    private void uploadVideo(final PhotoModel pm) {
        final String filePath = pm.getFullpath();

        if(pm.getMode() == 0 || pm.getMode() == 1){
            mMediaType = "pictures";
        }else if(pm.getMode() == 2){
            mMediaType = "videos";
        }

        final String fileName = pm.getFilename();

        Log.w(TAG,"비디오 업로드 시작");

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                pm.setThumbUploading(1);

                File f  = new File(filePath);
                String content_type  = DisplayUtil.getMimeType(filePath);

                OkHttpClient client = new OkHttpClient();
                RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url( getAbsoluteUrl(mHospitalId+"$"+ mPatientId+
                                "/"+mHospitalId+"/"+ mPatientId + "/videos/"+ mDate+"/"+ fileName))
                        .put(file_body)
                        .addHeader("X-Auth-Token",mAcccessToken)
                        .build();


                try {
                    okhttp3.Response response = client.newCall(request).execute();


                    if(!response.isSuccessful()){
                        Log.w(TAG," 원본, response code = "+response.code());

                        pm.setUploading(3);//업로드실패
                        makeNoti("uploading fail",1);

                    }else{
                        Log.w(TAG," 원본 업로드 성공 ");
                        pm.setUploading(2);
                        //uploadChain(pm);
                        regVideoEMR(pm);

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    pm.setUploading(3); //업로드실패
                    makeNoti("uploading fail",1);
                }
            }
        });

        t1.start();
    }

    private void regVideoEMR(final PhotoModel pm){
        Log.w(TAG,"regVideoEMR");
        final String fileName = pm.getFilename();
        final long fileSize = pm.getFilesize();
        pm.setChainUploading(1);

        Thread t2 = new Thread(new Runnable() {

            String url = Constants.EMRAPI.BASE_URL + Constants.EMRAPI.REG_PHOTO;
            String filepath = "/"+mHospitalId+"/"+ mPatientId + "/videos/"+ mDate+"/"+ fileName;

            @Override
            public void run() {

                JSONObject jsonObject = new JSONObject();
                JSONObject data = new JSONObject();
                JSONArray req_arry = new JSONArray();

                try {
                    jsonObject.put("userId", SmartFiPreference.getDoctorId(getApplicationContext()));
                    jsonObject.put("custNo", SmartFiPreference.getSfPatientCustNo(getApplicationContext()));
                    data.put("phtoFileNm", fileName);
                    data.put("phtoFilePath", filepath);
                    data.put("imgSize",fileSize);
                    req_arry.put(data);
                    jsonObject.put("photoList", req_arry);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                OkHttpClient client = new OkHttpClient();


                RequestBody reqBody = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        jsonObject.toString()
                );

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(url)
                        .addHeader("Accept","application/json")
                        .addHeader("X-Auth-Token",mAcccessToken)
                        .addHeader("Content-Type", "application/json")
                        .post(reqBody)
                        .build();

                try {
                    okhttp3.Response response = client.newCall(request).execute();


                    if(!response.isSuccessful()){
                        Log.w(TAG," regPhototoEMR 싪패 , response code = "+response.code());

                        pm.setChainUploading(3);//업로드실패
                        makeNoti("uploading fail",1);

//                        if(mMessenger != null){
//                            Message msg = Message.obtain();
//                            msg.obj = Constants.Upload.READ_FILE_UPLOAD_FAIL;
//
//                            try {
//                                mMessenger.send(msg);
//                            } catch (android.os.RemoteException e1) {
//                                Log.w(getClass().getName(), "Exception sending message", e1);
//                            }
//
//                        }
                    }else{
                        Log.w(TAG," regVideoEMR 성공 ");


                        makeNoti("uploading success",0);
                        if(Constants.FILE_N_DB_DELETE){
                            PhotoModelService.deleteFileNPhotoModel(pm);
                        }else{
                            pm.setChainUploading(2);
                        }

//                        Log.w(TAG," mMessenger =  "+mMessenger);
//                        if(mMessenger != null){
//                            Message msg = Message.obtain();
//                            msg.obj = Constants.Upload.READ_FILE_UPLOAD_SUCCESS;
//
//                            try {
//                                mMessenger.send(msg);
//                            } catch (android.os.RemoteException e1) {
//                                Log.w(getClass().getName(), "Exception sending message", e1);
//                            }
//
//                        }
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    pm.setUploading(3); //업로드실패
                    makeNoti("uploading fail",1);

//                    if(mMessenger != null){
//                        Message msg = Message.obtain();
//                        msg.obj = Constants.Upload.READ_FILE_UPLOAD_FAIL;
//
//                        try {
//                            mMessenger.send(msg);
//                        } catch (android.os.RemoteException e1) {
//                            Log.w(getClass().getName(), "Exception sending message", e1);
//                        }
//
//                    }
                }

            }
        });

        t2.start();
    }

    private void uploadChain(final PhotoModel pm) {

        Log.w(TAG,"uploadChain");


        final String fileName = pm.getFilename();
        pm.setChainUploading(1);

        Thread t2 = new Thread(new Runnable() {

            String urlproof = Constants.Chain.BASE_URL + Constants.Chain.CREATE;
            String filepath = "/"+mHospitalId+"/"+ mPatientId + "/videos/"+ mDate+"/"+ fileName;

            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                RequestBody formBody = new FormBody.Builder()
                        .add("hospital", mHospitalId)
                        .add("patient", mPatientId)
                        .add("file", filepath)
                        .build();


                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(urlproof)
                        .addHeader("X-Auth-Token",mAcccessToken)
                        .addHeader("Content-Type", "application/json")
                        .post(formBody)
                        .build();

                try {
                    okhttp3.Response response = client.newCall(request).execute();


                    if(!response.isSuccessful()){
                        Log.w(TAG," 체인 create 싪패 , response code = "+response.code());

                        pm.setChainUploading(3);//업로드실패
                        makeNoti("uploading fail",1);

                    }else{
                        Log.w(TAG," 체인 create 성공 ");
                        //pm.setChainUploading(2);

                        makeNoti("uploading success",0);
                        if(Constants.FILE_N_DB_DELETE){
                            PhotoModelService.deleteFileNPhotoModel(pm);
                        }else{
                            pm.setChainUploading(2);
                        }
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    pm.setUploading(3); //업로드실패
                    makeNoti("uploading fail",1);
                }

            }
        });

        t2.start();
    }

    private static String getAbsoluteUrl(String relativeUrl) {

        return Constants.Storage.BASE_URL + "/" + relativeUrl;
    }

    private void makeNoti(final String message, int id) {

        NotificationCompat.Builder builder;

        String CHANNEL_ID = "video_upload_channel";

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "Video Upload";
            //String description = Constants.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, importance);
            //channel.setDescription(description);

            // Add the channel
            NotificationManager notificationManager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }else{
            Activity activity = (Activity) mCon;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCon, message, Toast.LENGTH_SHORT).show();
                }
            });

        }

        if(id == 1){
            Intent intent= new Intent(this, FileExploreActivity.class);

            PendingIntent pending= PendingIntent.getActivity(mCon, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Create the notification
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.smartfi_icon)
                    .setContentTitle(Constants.Notification.NOTIFICATION_TITLE)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pending)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis());
        } else{

            // Create the notification
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.smartfi_icon)
                    .setContentTitle(Constants.Notification.NOTIFICATION_TITLE)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis());
        }


        // Show the notification
        NotificationManagerCompat.from(getApplicationContext()).notify(Constants.Notification.NOTIFICATION_VIDEO_ID, builder.build());


    }
}
