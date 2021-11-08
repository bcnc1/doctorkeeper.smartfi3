package com.doctorkeeper.smartfi.services;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;
import android.os.Parcelable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.doctorkeeper.smartfi.network.BlabAPI;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.doctorkeeper.smartfi.Constants;
import com.doctorkeeper.smartfi.R;
import com.doctorkeeper.smartfi.activities.FileExploreActivity;
import com.doctorkeeper.smartfi.network.MadamfiveAPI;
import com.doctorkeeper.smartfi.models.PhotoModel;
import com.doctorkeeper.smartfi.util.SmartFiPreference;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

import static com.doctorkeeper.smartfi.network.BlabAPI.getActivity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class PictureIntentService extends IntentService {

    private static final String TAG = "PictureIntentService";
    private static final String EXTRA_PICTURE_ID = "com.doctorkeeper.drcamlink.services.extra.picture.id";

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
    public static void startUploadPicture(Context context, String Path) {
        mCon = context;
        Intent intent = new Intent(context, PictureIntentService.class);
        intent.putExtra(EXTRA_PICTURE_ID, Path);
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
                String Path = extras.getString(EXTRA_PICTURE_ID);
                mMessenger = (Messenger) extras.get(Constants.MESSENGER);
                Log.d(TAG,"Path = "+Path);
//                PhotoModel photoModel = PhotoModelService.getPhotoModel(id);
//                mPatientId = photoModel.getCustNo();
                uploadPicture(Path);

            }

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy, notiId = "+mNotiId);

    }

    private void uploadPicture(final String path) {
//        final String filePath = pm.getFullpath();
//        final Long photoModelId = pm.getId();

//        if(pm.getMode() == 0 || pm.getMode() == 1){
            mMediaType = "pictures";
//        }else if(pm.getMode() == 2){
//            mMediaType = "videos";
//        }

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

//                pm.setThumbUploading(1);

                File file  = new File(path);
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

//                String cameraKind="";
//                if(pm.getMode() == 0 ){
//                    cameraKind = "Phone";
//                }else if(pm.getMode() == 1){
//                    cameraKind = "DSLR";
//                }

                BlabAPI.uploadImage(path, bytes, new JsonHttpResponseHandler(){
                    @Override
                    public void onStart() {
                        Log.i(TAG, "Uploading");
                    }
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Log.d(TAG, "Success:" + statusCode + response);
                        Toast.makeText(getActivity(),"이미지 저장 완료!",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        Log.d(TAG, "Failure:" + statusCode + responseString);
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
