package com.thinoo.drcamlink2.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.thinoo.drcamlink2.Constants;
import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.models.PhotoModel;
import com.thinoo.drcamlink2.util.DisplayUtil;
import com.thinoo.drcamlink2.util.SmartFiPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.logging.LogRecord;

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
    private static String mChartNum =  null;
    private static String mMediaType = null;
    private static int mNotiId = Constants.Notification.NOTIFICATION_VIDEO_ID;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *  name Used to name the worker thread, important only for debugging.
     */
    public VideoIntentService() {
        super("VideoIntentService");
    }


    public static void startUploadVideo(Context context, long id) {
        Intent intent = new Intent(context, VideoIntentService.class);
        intent.putExtra(EXTRA_VIDEO_ID, id);
        context.startService(intent);

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.d(TAG,"onHandleIntent 호출");
        mAcccessToken = SmartFiPreference.getSfToken(getApplicationContext());

        mChartNum = SmartFiPreference.getPatientChart(getApplicationContext());

        mPatientId = SmartFiPreference.getSfPatientCustNo(getApplicationContext());
        mHospitalId = SmartFiPreference.getHospitalId(getApplicationContext());

        if (intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null){
                long id = extras.getLong(EXTRA_VIDEO_ID);
                Log.d(TAG,"id = "+id);
                PhotoModel photoModel = PhotoModelService.getPhotoModel(id);

                makeNoti("video uploading...", 1);

                uploadThumbnail(photoModel);


            }

        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e(TAG, "비디오 인텐트서비스 종료");
    }


    private void uploadThumbnail(final PhotoModel pm) {



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
                                "/thumbnail"+"/"+mHospitalId+"/"+ mPatientId + "/videos/"+ mChartNum+"/"+ fileName))
                        .put(file_body)
                        .addHeader("X-Auth-Token",mAcccessToken)
                        .build();


                try {
                    okhttp3.Response response = client.newCall(request).execute();


                    if(!response.isSuccessful()){
                        // throw new IOException("Error : "+response);
                        Log.d(TAG, " 썸네일, response = "+response.code());

                        pm.setThumbUploading(3);

                        makeNoti("uploading fail", 0);

                    }else{

                        Log.d(TAG, " 썸네일, 업로드 성공 ");
                        pm.setThumbUploading(2);

                        uploadVideo(pm);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    pm.setThumbUploading(3); //업로드실패
                    makeNoti("uploading fail. please check network", 0);
                }
            }
        });

        t.start();
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
                                "/"+mHospitalId+"/"+ mPatientId + "/videos/"+ mChartNum+"/"+ fileName))
                        .put(file_body)
                        .addHeader("X-Auth-Token",mAcccessToken)
                        .build();


                try {
                    okhttp3.Response response = client.newCall(request).execute();


                    if(!response.isSuccessful()){
                        Log.d(TAG," 원본, response code = "+response.code());

                        pm.setUploading(3);//업로드실패
                        makeNoti("uploading fail",0);

                    }else{
                        Log.w(TAG," 원본 업로드 성공 ");
                        pm.setUploading(2);
                        //uploadChain(pm);
                        regVideoEMR(pm);

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    pm.setUploading(3); //업로드실패
                    makeNoti("uploading fail",0);
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
            String filepath = "/"+mHospitalId+"/"+ mPatientId + "/pictures/"+ mChartNum+"/"+ fileName;

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
                        makeNoti("uploading fail",0);

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
                    makeNoti("uploading fail",0);

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
            String filepath = "/"+mHospitalId+"/"+ mPatientId + "/videos/"+ mChartNum+"/"+ fileName;

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
                        makeNoti("uploading fail",0);

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
                    makeNoti("uploading fail",0);
                }

            }
        });

        t2.start();
    }

    private static String getAbsoluteUrl(String relativeUrl) {
//        String encString =  null;
//
//        try {
//            encString = URLEncoder.encode(Constants.Storage.BASE_URL + "/" + relativeUrl, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        return encString;
        return Constants.Storage.BASE_URL + "/" + relativeUrl;
    }

    private void makeNoti(String message, int id) {

        Log.d(TAG, "makeNoti => id :  "+mNotiId + "input id = "+id);


        Log.d(TAG, "after ==> makeNoti => id :  "+mNotiId + "input id = "+id);

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
        }

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.smartfi_icon)
                .setContentTitle(Constants.Notification.NOTIFICATION_TITLE)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());


        Log.d(TAG, "exec  ==> makeNoti => id :  "+mNotiId + "input id = "+id);
        // Show the notification
        NotificationManagerCompat.from(getApplicationContext()).notify(Constants.Notification.NOTIFICATION_VIDEO_ID, builder.build());


    }
}
