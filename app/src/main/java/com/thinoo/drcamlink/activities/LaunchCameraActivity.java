package com.thinoo.drcamlink.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.thinoo.drcamlink.BuildConfig;
import com.thinoo.drcamlink.MainActivity;
import com.thinoo.drcamlink.R;
import com.thinoo.drcamlink.madamfive.BlabAPI;
import com.thinoo.drcamlink.models.PhotoModel;
import com.thinoo.drcamlink.services.PhotoModelService;
import com.thinoo.drcamlink.services.PictureIntentService;
import com.thinoo.drcamlink.util.DisplayUtil;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.thinoo.drcamlink.madamfive.MadamfiveAPI.getActivity;
import static com.thinoo.drcamlink.madamfive.MadamfiveAPI.getContext;

public class LaunchCameraActivity extends Activity {

    private final String TAG = LaunchCameraActivity.class.getSimpleName();

    private static final int CAMERA_REQUEST = 1;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private Button btnUpload;
    private OrientationListener orientationListener;
    private Uri imageToUploadUri;
    private String mFileName;
    private File mFile;
    private final String  DEVICE = "phone";
    private Context mCon;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    private String currentPhotoPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.launch_camera_activity_main);
        mCon = this;

        this.imageView = (ImageView) this.findViewById(R.id.imageviewForCameraApp);

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HHmmssSSS").format(new Date());
        mFileName = DEVICE + "_" + timeStamp+".jpg";
        mFile = new File(mCon.getExternalFilesDir(Environment.getExternalStorageState())  + File.separator + mFileName);
//        imageToUploadUri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID, mFile);
//        Log.i(TAG, "imageToUploadUri = "+imageToUploadUri);

//        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageToUploadUri );
//        startActivityForResult(cameraIntent, CAMERA_REQUEST);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();

            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.thinoo.drcamlink",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
        orientationListener = new OrientationListener(getContext());
        orientationListener.enable();
    }

    private File createImageFile() throws IOException {
        File storageDir = mCon.getExternalFilesDir(Environment.getExternalStorageState());
        String name = mFileName.substring(0,26);
        File image = File.createTempFile(
                name,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new
                        Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            Bitmap srcBmp = BitmapFactory.decodeFile(currentPhotoPath);
            int orientationValue = orientationListener.rotation;
            srcBmp = rotateImage(srcBmp, orientationValue);

            FileOutputStream out = new FileOutputStream(currentPhotoPath);
            srcBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        }catch(Exception e){
            Log.i(TAG,"rotation E"+e.toString());
        }

        Log.i(TAG,"onActivityResult 카매라에서돌아옴");
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                if(currentPhotoPath != null){
                    PhotoModel photoModel = PhotoModelService.addPhotoModel(mCon, currentPhotoPath, currentPhotoPath, mFileName, 0);
                    Long id = photoModel.getId();
                    PictureIntentService.startUploadPicture(mCon, id);
                }else{
                    Toast.makeText(mCon, R.string.make_error_thumbnail, Toast.LENGTH_SHORT);
                }
            }catch (Exception e){
                Log.e("INSIDE___",e.toString());
            }
        }

        Intent intent = new Intent(getApplication(), MainActivity.class);
        startActivity(intent);
    }


    private void savePhoto(byte[] bytes, String cameraKind, String filePath) {

        Log.i(TAG,"savePhoto => filepath = "+filePath);
        // 화일 저장
        int filePathLength = filePath.length();
        String filename = filePath.substring(0,filePathLength-2);
        filename = filename + ".JPG";
        final PhotoModel photoModel = PhotoModelService.savePhoto(bytes, filename, 0);

        Log.d(TAG, "카메라업로드시작:" );
        BlabAPI.ktStoreObject(photoModel.getFullpath(), "Phone", filename, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                Log.i("AsyncTask", "Uploading");
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.d("AsyncTask", "이미지 업로드 완료:" + statusCode + responseString);
                LaunchCameraActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(),"이미지 저장 완료!",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("AsyncTask", "HTTP22:" + statusCode + response.toString());
            }
        });

        Log.i(TAG,"Finished");
    }

    private class OrientationListener extends OrientationEventListener {

        final int ROTATION_O = 1;
        final int ROTATION_90 = 2;
        final int ROTATION_180 = 3;
        final int ROTATION_270 = 4;

        private int rotation = 0;

        public OrientationListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if ((orientation < 35 || orientation > 325) && rotation != ROTATION_O) { // PORTRAIT
                rotation = ROTATION_O;
            } else if (orientation > 145 && orientation < 215 && rotation != ROTATION_180) { // REVERSE PORTRAIT
                rotation = ROTATION_180;
            } else if (orientation > 55 && orientation < 125 && rotation != ROTATION_270) { // REVERSE LANDSCAPE
                rotation = ROTATION_270;
            } else if (orientation > 235 && orientation < 305 && rotation != ROTATION_90) { //LANDSCAPE
                rotation = ROTATION_90;
            }
        }
    }

    private Bitmap rotateImage(Bitmap image,int orientationValue)
    {
        try
        {
            Log.i(TAG,"Orientation value : "+orientationValue);

            if(orientationValue==1){
                image = rotate(image, 90);
            }else if(orientationValue==4){
                image = rotate(image, 180);
            }else if(orientationValue==3) {
                image = rotate(image, 270);
            }
        }
        catch(Exception e)
        {
            Log.e(TAG,e+"");
        }
        return image;
    }

    private Bitmap rotate(Bitmap bitmap, int degrees)
    {
        if(degrees != 0 && bitmap != null)
        {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            try
            {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if(bitmap != converted)
                {
                    bitmap.recycle();
                    bitmap = converted;
                }
            }
            catch(OutOfMemoryError ex)
            {

                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }
        return bitmap;
    }


}
