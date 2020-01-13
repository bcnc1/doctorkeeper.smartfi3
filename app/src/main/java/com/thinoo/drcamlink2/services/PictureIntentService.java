package com.thinoo.drcamlink2.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.thinoo.drcamlink2.Constants;
import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.activities.FileExploreActivity;
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

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class PictureIntentService extends IntentService {

    private static final String TAG = "PictureIntentService";
    private static final String EXTRA_PICTURE_ID = "com.thinoo.drcamlink2.services.extra.picture.id";

    private static String mAcccessToken = null;
    private static String mPatientId = null;
    private static String mHospitalId = null;
    private static String mDate =  null;
    private static String mMediaType = null;
    private static Context mCon;

    private static int mNotiId = Constants.Notification.NOTIFICATION_PICTURE_ID;
    private Messenger mMessenger = null;  //카메라에서 파일 읽어서 업로드시 진행상황체크를 위해..


    public PictureIntentService() {
        super("PictureIntentService");
        Log.d(TAG,"PictureIntentService 생성");
    }

    /**
     * Starts this service to perform upload with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startUploadPicture(Context context, long id) {
        mCon = context;
        Intent intent = new Intent(context, PictureIntentService.class);
        intent.putExtra(EXTRA_PICTURE_ID, id);
        mCon.startService(intent);
        Log.w(TAG,"startUploadPicture 호출");
    }

    public static void startUploadPicture(Context context, long id, Parcelable value) {
        mCon = context;
        Intent intent = new Intent(context, PictureIntentService.class);
        intent.putExtra(EXTRA_PICTURE_ID, id);
        intent.putExtra(Constants.MESSENGER, value);
        mCon.startService(intent);
        Log.w(TAG,"startUploadPicture 호출");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG,"onHandleIntent 호출");
        mAcccessToken = SmartFiPreference.getSfToken(getApplicationContext());
       // mChartNum = SmartFiPreference.getPatientChart(getApplicationContext());


        //mPatientId = SmartFiPreference.getSfPatientCustNo(getApplicationContext());
        mHospitalId = SmartFiPreference.getHospitalId(getApplicationContext());

        mDate = new SimpleDateFormat("yyyyMM").format(new Date());
        Log.w(TAG,"mdate = "+mDate);

        if (intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null){
                long id = extras.getLong(EXTRA_PICTURE_ID);
                mMessenger = (Messenger) extras.get(Constants.MESSENGER);
                Log.d(TAG,"id = "+id);
                PhotoModel photoModel = PhotoModelService.getPhotoModel(id);

                makeNoti("picture uploading...", 1);

                mPatientId = photoModel.getCustNo();
                Log.w(TAG, "mPatientId = "+mPatientId);
                uploadThumbnail(photoModel);


            }

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy, notiId = "+mNotiId);
       // mNotiId = Constants.Notification.NOTIFICATION_PICTURE_ID;

    }

    private void regPhototoEMR(final PhotoModel pm){
        Log.w(TAG,"regPhototoEMR");
        final String fileName = pm.getFilename();
        final long fileSize = pm.getFilesize();
        pm.setChainUploading(1);

        Thread t2 = new Thread(new Runnable() {

            String url = Constants.EMRAPI.BASE_URL + Constants.EMRAPI.REG_PHOTO;
            String filepath = "/"+mHospitalId+"/"+ mPatientId + "/pictures/"+ mDate+"/"+ fileName;

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

                        if(mMessenger != null){
                            Message msg = Message.obtain();
                            msg.obj = Constants.Upload.READ_FILE_UPLOAD_FAIL;

                            try {
                                mMessenger.send(msg);
                            } catch (android.os.RemoteException e1) {
                                Log.w(getClass().getName(), "Exception sending message", e1);
                            }

                        }
                    }else{
                        Log.w(TAG," regPhototoEMR 성공 ");


                        makeNoti("uploading success",0);
                        if(Constants.FILE_N_DB_DELETE){
                            PhotoModelService.deleteFileNPhotoModel(pm);
                        }else{
                            pm.setChainUploading(2);
                        }

                        Log.w(TAG," mMessenger =  "+mMessenger);
                        if(mMessenger != null){
                            Message msg = Message.obtain();
                            msg.obj = Constants.Upload.READ_FILE_UPLOAD_SUCCESS;

                            try {
                                mMessenger.send(msg);
                            } catch (android.os.RemoteException e1) {
                                Log.w(getClass().getName(), "Exception sending message", e1);
                            }

                        }
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    pm.setUploading(3); //업로드실패
                    makeNoti("uploading fail",0);

                    if(mMessenger != null){
                        Message msg = Message.obtain();
                        msg.obj = Constants.Upload.READ_FILE_UPLOAD_FAIL;

                        try {
                            mMessenger.send(msg);
                        } catch (android.os.RemoteException e1) {
                            Log.w(getClass().getName(), "Exception sending message", e1);
                        }

                    }
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
            String filepath = "/"+mHospitalId+"/"+ mPatientId + "/pictures/"+ mDate+"/"+ fileName;

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

                        if(mMessenger != null){
                            Message msg = Message.obtain();
                            msg.obj = Constants.Upload.READ_FILE_UPLOAD_FAIL;

                            try {
                                mMessenger.send(msg);
                            } catch (android.os.RemoteException e1) {
                                Log.w(getClass().getName(), "Exception sending message", e1);
                            }

                        }
                    }else{
                        Log.w(TAG," 체인 create 성공 ");


                        makeNoti("uploading success",0);
                        if(Constants.FILE_N_DB_DELETE){
                            PhotoModelService.deleteFileNPhotoModel(pm);
                        }else{
                            pm.setChainUploading(2);
                        }

                        Log.w(TAG," mMessenger =  "+mMessenger);
                        if(mMessenger != null){
                            Message msg = Message.obtain();
                            msg.obj = Constants.Upload.READ_FILE_UPLOAD_SUCCESS;

                            try {
                                mMessenger.send(msg);
                            } catch (android.os.RemoteException e1) {
                                Log.w(getClass().getName(), "Exception sending message", e1);
                            }

                        }
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    pm.setUploading(3); //업로드실패
                    makeNoti("uploading fail",0);

                    if(mMessenger != null){
                        Message msg = Message.obtain();
                        msg.obj = Constants.Upload.READ_FILE_UPLOAD_FAIL;

                        try {
                            mMessenger.send(msg);
                        } catch (android.os.RemoteException e1) {
                            Log.w(getClass().getName(), "Exception sending message", e1);
                        }

                    }
                }

            }
        });

        t2.start();
    }

    private void uploadPicture(final PhotoModel pm) {
        final String filePath = pm.getFullpath();

        if(pm.getMode() == 0 || pm.getMode() == 1){
            mMediaType = "pictures";
        }else if(pm.getMode() == 2){
            mMediaType = "videos";
        }

        final String fileName = pm.getFilename();

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
                                "/"+mHospitalId+"/"+ mPatientId + "/pictures/"+ mDate+"/"+ fileName))
                        .put(file_body)
                        .addHeader("X-Auth-Token",mAcccessToken)
                        .build();


                try {
                    okhttp3.Response response = client.newCall(request).execute();


                    if(!response.isSuccessful()){
                        Log.d(TAG," 원본, response code = "+response.code());

                        pm.setUploading(3);//업로드실패
                        makeNoti("uploading fail",0);

                        Log.w(TAG," mMessenger =  "+mMessenger);
                        if(mMessenger != null){
                            Message msg = Message.obtain();
                            msg.obj = Constants.Upload.READ_FILE_UPLOAD_FAIL;

                            try {
                                mMessenger.send(msg);
                            } catch (android.os.RemoteException e1) {
                                Log.w(getClass().getName(), "Exception sending message", e1);
                            }

                        }

                    }else{
                        Log.d(TAG," 원본 업로드 성공 ");
                        pm.setUploading(2);
                        //uploadChain(pm);
                        regPhototoEMR(pm);

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    pm.setUploading(3); //업로드실패
                    makeNoti("uploading fail",0);

                    Log.w(TAG," mMessenger =  "+mMessenger);
                    if(mMessenger != null){
                        Message msg = Message.obtain();
                        msg.obj = Constants.Upload.READ_FILE_UPLOAD_FAIL;

                        try {
                            mMessenger.send(msg);
                        } catch (android.os.RemoteException e1) {
                            Log.w(getClass().getName(), "Exception sending message", e1);
                        }

                    }
                }
            }
        });

        t1.start();
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
                                "/thumbnail"+"/"+mHospitalId+"/"+ mPatientId + "/pictures/"+ mDate+"/"+ fileName))
                        .put(file_body)
                        .addHeader("X-Auth-Token",mAcccessToken)
                        .build();


                try {
                    okhttp3.Response response = client.newCall(request).execute();


                    if(!response.isSuccessful()){
                        // throw new IOException("Error : "+response);
                        Log.w(TAG, " 썸네일, response = "+response.code());

                        pm.setThumbUploading(3);

                        makeNoti("uploading fail", 0);

                        Log.w(TAG," mMessenger =  "+mMessenger);
                        if(mMessenger != null){
                            Message msg = Message.obtain();
                            msg.obj = Constants.Upload.READ_FILE_UPLOAD_FAIL;

                            try {
                                mMessenger.send(msg);
                            } catch (android.os.RemoteException e1) {
                                Log.w(getClass().getName(), "Exception sending message", e1);
                            }

                        }

                    }else{

                        Log.d(TAG, " 썸네일, 업로드 성공 ");
                        pm.setThumbUploading(2);

                        uploadPicture(pm);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    pm.setThumbUploading(3); //업로드실패
                    makeNoti("uploading fail. please check network", 0);

                    Log.w(TAG," mMessenger =  "+mMessenger);
                    if(mMessenger != null){
                        Message msg = Message.obtain();
                        msg.obj = Constants.Upload.READ_FILE_UPLOAD_FAIL;

                        try {
                            mMessenger.send(msg);
                        } catch (android.os.RemoteException e1) {
                            Log.w(getClass().getName(), "Exception sending message", e1);
                        }

                    }
                }
            }
        });

        t.start();
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

        //Log.d(TAG, "makeNoti => id :  "+mNotiId + "input id = "+id);

        //Log.d(TAG, "after ==> makeNoti => id :  "+mNotiId + "input id = "+id);

        String CHANNEL_ID = "picture_upload_channel";

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "Picture Upload";
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

        Intent intent= new Intent(this, FileExploreActivity.class);

        PendingIntent pending= PendingIntent.getActivity(mCon, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.smartfi_icon)
                .setContentTitle(Constants.Notification.NOTIFICATION_TITLE)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());


        //Log.d(TAG, "exec  ==> makeNoti => id :  "+mNotiId + "input id = "+id);
        // Show the notification, if add noti please NOTIFICATION_PICTURE_ID to change pm id
        NotificationManagerCompat.from(getApplicationContext()).notify(Constants.Notification.NOTIFICATION_PICTURE_ID, builder.build());


    }
}
