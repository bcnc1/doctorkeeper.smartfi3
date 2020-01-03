package com.thinoo.drcamlink2.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
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
import com.thinoo.drcamlink2.MainActivity;
import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.models.PhotoModel;
import com.thinoo.drcamlink2.services.PhotoModelService;
import com.thinoo.drcamlink2.services.PictureIntentService;
import com.thinoo.drcamlink2.services.UploadService;
import com.thinoo.drcamlink2.services.VideoIntentService;
import com.thinoo.drcamlink2.util.DisplayUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.thinoo.drcamlink2.Constants.Invoke.UPLOAD_FILE_KIND;
import static com.thinoo.drcamlink2.Constants.Invoke.UPLOAD_FILE_NAME;
import static com.thinoo.drcamlink2.Constants.Invoke.UPLOAD_FILE_PATH;
import static com.thinoo.drcamlink2.Constants.Invoke.VIDEO_RECORD;


public class LaunchVrecordActivity extends Activity {
    private final String TAG = LaunchVrecordActivity.class.getSimpleName();

    private static final int VREC_REQUEST = 2100;
    private Context mCon;
    private final int MaxMin = 30;
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
            //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            mFileName = DEVICE + "_" + timeStamp + ".mp4";
            mFileNameThumb = DEVICE + "_" + timeStamp + ".jpg";


            //mFile = new File(mCon.getExternalFilesDir(Environment.getExternalStorageState()) + File.separator + "drcam" + File.separator + mFilename);
            mFile = new File(mCon.getExternalFilesDir(Environment.getExternalStorageState())  + File.separator + mFileName);

            contentUri = FileProvider.getUriForFile(mCon, BuildConfig.APPLICATION_ID , mFile);

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

        Toast.makeText(mCon, "비디오에서 돌아옴", Toast.LENGTH_SHORT).show();

        if(requestCode == VREC_REQUEST && resultCode == Activity.RESULT_OK){

            if(contentUri != null){

            }

            //서비스로 구현
//            Intent it = new Intent(mCon, UploadService.class);
//            it.putExtra(UPLOAD_FILE_PATH, mFile.toString());
//            it.putExtra(UPLOAD_FILE_KIND, "video");
//            it.putExtra(UPLOAD_FILE_NAME, mFilename);


            //썸네일 테스트
//            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mFile.toString(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
//            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, 255, 170);
//
//            File file = new File(mCon.getExternalFilesDir(Environment.getExternalStorageState()), "/thumbnail/");
//            if (!file.isDirectory()) {
//                file.mkdir();
//            }
//            try {
//
//
//               // FileOutputStream outStream = new FileOutputStream(mCon.getExternalFilesDir(Environment.getExternalStorageState())+ File.separator +"videoThumb.jpg"); //파일저장
//                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
//                outStream.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }



            // 비디오 썸 테스트 end

           // startService(it);

            //using uploadmanager
            //1. data 저장.


            //kimcy 업로드 테스트
//            BlabAPI.ktStoreObject(mFile.toString(),"video", mFilename, new JsonHttpResponseHandler(){
//                @Override
//                public void onStart() {
//                    super.onStart();
//                    Log.i(TAG, "video-Uploading => 시작");
//                }
//
//
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                    super.onSuccess(statusCode, headers, responseString);
//                    Log.i(TAG, "video-Uploading => 성공");
//                }
//
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                    super.onFailure(statusCode, headers, responseString, throwable);
//                    Log.i(TAG, "video-Uploading => 실패");
//                }
//
//                @Override
//                public void onRetry(int retryNo) {
//                    super.onRetry(retryNo);
//                }
//            }  );

            String path = DisplayUtil.storeThumbVideoImage(mFile.toString(),
                    mCon.getExternalFilesDir(Environment.getExternalStorageState()),mFileNameThumb);

            if(path != null){
                PhotoModel photoModel = PhotoModelService.addPhotoModel(mFile.toString(),path, mFileName, 2);

                Long id = photoModel.getId();
                VideoIntentService.startUploadVideo(mCon, id);

            }else{
                Toast.makeText(mCon, R.string.make_error_thumbnail, Toast.LENGTH_SHORT);

            }

            Intent intent = new Intent(getApplication(), MainActivity.class);
            intent.putExtra(VIDEO_RECORD, false);
            startActivity(intent);
        }else{
            Log.e(TAG,"비디오에서 못 돌아옴");
        }

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
