package com.thinoo.drcamlink2.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.thinoo.drcamlink2.Constants;
import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.models.PhotoModel;
import com.thinoo.drcamlink2.util.DisplayUtil;
import com.thinoo.drcamlink2.util.SmartFiPreference;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import cz.msebera.android.httpclient.Header;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.thinoo.drcamlink2.Constants.Storage.BASE_URL;
import static com.thinoo.drcamlink2.MainActivity.countDownTimer;

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
    private static String mChartNum =  null;
    private static String mMediaType = null;
    private static int mNotiId = Constants.Notification.NOTIFICATION_PICTURE_ID;

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
    // TODO: Customize helper method
    public static void startUploadPicture(Context context, long id) {
        Intent intent = new Intent(context, PictureIntentService.class);
        intent.putExtra(EXTRA_PICTURE_ID, id);
        context.startService(intent);
        Log.d(TAG,"startUploadPicture 호출");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG,"onHandleIntent 호출");
        mAcccessToken = SmartFiPreference.getSfToken(getApplicationContext());
        //mChartNum = SmartFiPreference.getPatientChart(getApplicationContext());
        mChartNum = "101010";

        mPatientId = SmartFiPreference.getPatientId(getApplicationContext());
        mHospitalId = SmartFiPreference.getHospitalId(getApplicationContext());

        if (intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null){
                long id = extras.getLong(EXTRA_PICTURE_ID);
                Log.d(TAG,"id = "+id);
                PhotoModel photoModel = PhotoModelService.getPhotoModel(id);

                makeNoti("picture uploading...", 1);

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

    private void uploadChain(final PhotoModel pm) {

        Log.d(TAG,"uploadChain");

//        final String fileName = pm.getFilename();
//        pm.setChainUploading(1);
//
//        RequestParams params = new RequestParams();
//        params.put("hospital", mHospitalId);
//        params.put("patient", mPatientId);
//        params.put("file", "/"+mHospitalId+"/"+ mPatientId + "/pictures/"+ mChartNum+"/"+ fileName);
//
//
//        SyncHttpClient client = new SyncHttpClient();
//        client.addHeader("Content-Type", "application/json");
//        client.addHeader("X-Auth-Token", mAcccessToken);
//
//        String url = Constants.Chain.BASE_URL + Constants.Chain.CREATE;
//
//        client.post(url, params, new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                pm.setChainUploading(2);
//                makeNoti("uploading success",0);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                Log.d(TAG, "chain error = "+statusCode);
//                Log.d(TAG, "chain error body = "+responseBody.hashCode());
//                pm.setChainUploading(3);
//                makeNoti("uploading fail",0);
//            }
//        });

        final String fileName = pm.getFilename();
        pm.setChainUploading(1);

        Thread t2 = new Thread(new Runnable() {

            String urlproof = Constants.Chain.BASE_URL + Constants.Chain.CREATE;
            String filepath = "/"+mHospitalId+"/"+ mPatientId + "/pictures/"+ mChartNum+"/"+ fileName;

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
                        Log.d(TAG," 체인 create 싪패 , response code = "+response.code());

                        pm.setChainUploading(3);//업로드실패
                        makeNoti("uploading fail",0);

                    }else{
                        Log.d(TAG," 체인 create 성공 ");
                        pm.setChainUploading(2);

                        makeNoti("uploading success",0);
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
                                "/"+mHospitalId+"/"+ mPatientId + "/pictures/"+ mChartNum+"/"+ fileName))
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
                        Log.d(TAG," 원본 업로드 성공 ");
                        pm.setUploading(2);
                        uploadChain(pm);

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
                                "/thumbnail"+"/"+mHospitalId+"/"+ mPatientId + "/pictures/"+ mChartNum+"/"+ fileName))
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

                        uploadPicture(pm);

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
        int notiID;

//        if(id == 0){
//            notiID = mNotiId;
//        }else{
//            notiID = mNotiId++;
//        }

        Log.d(TAG, "after ==> makeNoti => id :  "+mNotiId + "input id = "+id);

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
        NotificationManagerCompat.from(getApplicationContext()).notify(Constants.Notification.NOTIFICATION_PICTURE_ID, builder.build());


    }
}
