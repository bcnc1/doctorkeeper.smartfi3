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
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.thinoo.drcamlink2.Constants;
import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.activities.FileExploreActivity;
import com.thinoo.drcamlink2.madamfive.MadamfiveAPI;
import com.thinoo.drcamlink2.models.PhotoModel;
import com.thinoo.drcamlink2.util.DisplayUtil;
import com.thinoo.drcamlink2.util.SmartFiPreference;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


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
    private Handler handler = new Handler();

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

//                makeNoti("picture uploading...", 0);

                mPatientId = photoModel.getCustNo();
//                Log.w(TAG, "mPatientId = "+mPatientId);
//                uploadThumbnail(photoModel, false);
                uploadPicture(photoModel);

            }

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy, notiId = "+mNotiId);

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

                File file  = new File(filePath);
                String content_type  = DisplayUtil.getMimeType(filePath);
//                File file = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState()), "/drcam/");
                byte[] bytes = null;
                try{
                    FileInputStream fis = new FileInputStream(file);
                    int nCount = fis.available();
                    if(nCount > 0){
                        bytes = new byte[nCount];
                        fis.read(bytes);
                    }
                    if(fis != null){
                        fis.close();
                    }
                }catch(Exception e){
                    Log.i(TAG,e.toString());
                }
                Log.i(TAG,"uploadImage => Read Bitmap");

                MadamfiveAPI.createPost(bytes, "Phone", new JsonHttpResponseHandler() {
                    @Override
                    public void onStart() {
                        Log.i("AsyncTask", "Uploading");
                    }
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                        Log.d("AsyncTask", "HTTP21:" + statusCode + responseString);
                    }
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                        Log.d("AsyncTask", "HTTP22:" + statusCode + response.toString());
                    }
                });
                Log.i(TAG,"uploadImage => Finished");

            }
        });

        t1.start();
    }

    private void makeNoti(final String message, int id) {

        NotificationCompat.Builder builder;

        String CHANNEL_ID = "picture_upload_channel";

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "Picture Upload";

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, importance);


            // Add the channel
            NotificationManager notificationManager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        } else{
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

        NotificationManagerCompat.from(getApplicationContext()).notify(Constants.Notification.NOTIFICATION_PICTURE_ID, builder.build());

    }
}
