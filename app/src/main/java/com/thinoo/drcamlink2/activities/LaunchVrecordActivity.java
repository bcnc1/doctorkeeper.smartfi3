package com.thinoo.drcamlink2.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.thinoo.drcamlink2.BuildConfig;
import com.thinoo.drcamlink2.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LaunchVrecordActivity extends Activity {
    private final String TAG = LaunchVrecordActivity.class.getSimpleName();

    private static final int VREC_REQUEST = 2100;
    private Context mCon;
    private final int MaxMin = 30;


    private final int PERMISSION_ALL = 1;
    private final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.launch_vrecord_activity_main);

        mCon = this;

        if(!hasPermissions(mCon, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else{
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "DRCAM_" + timeStamp + ".mp4";


            File file = new File(mCon.getExternalFilesDir(Environment.getExternalStorageState()) + File.separator + "drcam" + File.separator + filename);

            Uri contentUri = FileProvider.getUriForFile(mCon, BuildConfig.APPLICATION_ID , file);

            Log.d(TAG,"contentUri = "+ contentUri);

            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, contentUri );
            intent.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 1); //high quality
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60*MaxMin);
            grantUriPermission(getPackageName(), contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

            //intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1048576);  //size제한, 1MB
            startActivityForResult(intent, VREC_REQUEST);
        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_ALL){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED ){

            }else{
                Toast.makeText(mCon, "요청한 모든 권한사용에 동의하셔야 합니다.!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private  boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


}
