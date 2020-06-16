package com.thinoo.drcamlink.activities;

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

import com.thinoo.drcamlink.BuildConfig;
import com.thinoo.drcamlink.MainActivity;
import com.thinoo.drcamlink.R;
import com.thinoo.drcamlink.models.PhotoModel;
import com.thinoo.drcamlink.services.PhotoModelService;
import com.thinoo.drcamlink.services.VideoIntentService;
import com.thinoo.drcamlink.util.DisplayUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.thinoo.drcamlink.Constants.Invoke.VIDEO_RECORD;


public class LaunchVrecordActivity extends Activity {
    private final String TAG = LaunchVrecordActivity.class.getSimpleName();

    private static final int VREC_REQUEST = 2100;
    private Context mCon;
    private final int MaxMin = 10;
    private File mFile;
    private String mFileName;
    private String mFileNameThumb;
    private final String  DEVICE = "phone";
    private Uri contentUri;

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
            gotoVideoRecord();
        }
    }

    private void gotoVideoRecord() {

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HHmmssSSS").format(new Date());
        mFileName = DEVICE + "_" + timeStamp + ".mp4";
        mFileNameThumb = DEVICE + "_" + timeStamp + ".jpg";

        mFile = new File(mCon.getExternalFilesDir(Environment.getExternalStorageState())  + File.separator + mFileName);
        contentUri = FileProvider.getUriForFile(mCon, BuildConfig.APPLICATION_ID , mFile);
//        Log.d(TAG,"contentUri = "+ contentUri);

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, contentUri );
        intent.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 0);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60*MaxMin);
        grantUriPermission(getPackageName(), contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        //intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1048576);  //size제한, 1MB
        startActivityForResult(intent, VREC_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(mCon, "비디오에서 돌아옴", Toast.LENGTH_SHORT).show();
        if(requestCode == VREC_REQUEST && resultCode == Activity.RESULT_OK){

            String path = DisplayUtil.storeThumbVideoImage(mFile.toString(),
                    mCon.getExternalFilesDir(Environment.getExternalStorageState()),mFileNameThumb);
//            if(path != null){
                PhotoModel photoModel = PhotoModelService.addPhotoModel(mCon, mFile.toString(),path, mFileName, 2);
                Long id = photoModel.getId();
                VideoIntentService.startUploadVideo(mCon, id);

//            }else{
//                Toast.makeText(mCon, R.string.make_error_thumbnail, Toast.LENGTH_SHORT);
//
//            }


        }else{
            Log.e(TAG,"비디오에서 못 돌아옴");
        }

        //어쨋거나 메인으로..
        Intent intent = new Intent(getApplication(), MainActivity.class);
        intent.putExtra(VIDEO_RECORD, false);
        startActivity(intent);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_ALL){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED ){
                gotoVideoRecord();

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
