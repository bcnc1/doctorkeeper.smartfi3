package com.thinoo.drcamlink2.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.util.SmartFiPreference;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.thinoo.drcamlink2.util.Constants.Invoke.UPLOAD_FILE_KIND;
import static com.thinoo.drcamlink2.util.Constants.Invoke.UPLOAD_FILE_NAME;
import static com.thinoo.drcamlink2.util.Constants.Invoke.UPLOAD_FILE_PATH;
import static com.thinoo.drcamlink2.util.Constants.Storage.BASE_URL;


public class UploadService extends Service {
    private final String TAG = "UploadService";
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private String mFPath, mFKind, mFContainer;
    private static String mAcccessToken = "AUTH_tkc098dfae46d9490b833add56c39e1f4b";
    private Context mCon;
    private String mHospitalId = "abc";
    private String mPatientId = "kimcy";
    int NotID = 1;
    NotificationManager nm;

    private  final String getMimeType(String path) {

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
    }

    private  String getAbsoluteUrl(String relativeUrl) {
        Log.i(TAG,"파일명  = "+ BASE_URL + "/" + relativeUrl);
        return BASE_URL + "/" + relativeUrl;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("UploadService-start", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mCon = this;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;  //needed for stop.
        msg.setData(intent.getExtras());
        mServiceHandler.sendMessage(msg);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int startId = msg.arg1;
            //Object someObject = msg.obj;
            Bundle extras = msg.getData();
            if(extras != null){
                mFPath = extras.getString(UPLOAD_FILE_PATH);
                mFKind = extras.getString(UPLOAD_FILE_KIND);
                mFContainer = extras.getString(UPLOAD_FILE_NAME);

                //추후 사용할 코드임
               // mAcccessToken = SmartFiPreference.getSfToken(mCon);
                makenoti("비디오 업로딩..");
                Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        File f = new File(mFPath);
                        String contentType = getMimeType(mFPath);

                        OkHttpClient client = new OkHttpClient();

                        RequestBody file_body = RequestBody.create(MediaType.parse(contentType),f);

                        okhttp3.Request request = new okhttp3.Request.Builder()
                                .url( getAbsoluteUrl(mHospitalId+"/"+ mPatientId+"/"+mFKind+mFContainer))
                                .put(file_body)
                                .addHeader("X-Auth-Token",mAcccessToken)
                                .build();



                        try{
                            Response response = client.newCall(request).execute();

                            if(!response.isSuccessful()){
                                makenoti("비디오 업로딩 실패");
                                throw new IOException("Error : "+response);
                            }else{
                                Log.i(TAG, "업로드 성공 = "+ response.code());
                                makenoti("비디오 업로딩 성공");
                            }
                        } catch (IOException e){
                            e.printStackTrace();
                        }

                    }
                });

                t.start();
            }


            // Do some processing
            boolean stopped = stopSelfResult(startId);
            // stopped is true if the service is stopped
        }
    }

    private void makenoti(String message) {

        Notification noti = new NotificationCompat.Builder(getApplicationContext() /*, MainActivity.id*/)
                .setSmallIcon(R.drawable.ic_launcher)
                .setWhen(System.currentTimeMillis())  //When the event occurred, now, since noti are stored by time.

                .setContentTitle("Service")   //Title message top row.
                .setContentText(message)  //message when looking at the notification, second row
                .setAutoCancel(true)   //allow auto cancel when pressed.
                .build();  //finally build and return a Notification.

        //Show the notification
        nm.notify(NotID, noti);
       // NotID++;
    }
}
