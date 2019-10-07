package com.thinoo.drcamlink2.activities;

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
import com.thinoo.drcamlink2.MainActivity;
import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.madamfive.MadamfiveAPI;
import com.thinoo.drcamlink2.models.PhotoModel;
import com.thinoo.drcamlink2.services.PhotoModelService;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.getActivity;
import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.getContext;

public class LaunchCameraActivity extends Activity {

    private final String TAG = LaunchCameraActivity.class.getSimpleName();

    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private Button btnUpload;
    private OrientationListener orientationListener;
    private Uri imageToUploadUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.launch_camera_activity_main);

        this.imageView = (ImageView) this.findViewById(R.id.imageviewForCameraApp);

//        Button photoButton = (Button) this.findViewById(R.id.btn_CameraApp);
//        photoButton.setOnClickListener(new View.OnClickListener() {

//            @Override
//            public void onClick(View v) {
//                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
//                } else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                    File f = new File(Environment.getExternalStorageDirectory(), "tempImage.jpg");
                    File f = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState()), "/tempImage.jpg");

                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getContext(), "com.thinoo.drcamlink2", f));
//                    imageToUploadUri = Uri.fromFile(f);
                    imageToUploadUri = FileProvider.getUriForFile(getContext(), "com.thinoo.drcamlink2", f);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
//                }
//            }
//        });

//        btnUpload = (Button) this.findViewById(R.id.btn_UploadAtCameraApp);
//        btnUpload.setVisibility(View.INVISIBLE);

//        Button btnBack = (Button) this.findViewById(R.id.btn_backToMain);
//        btnBack.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {


//            }
//        });

        orientationListener = new OrientationListener(getContext());
        orientationListener.enable();
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

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            try {

                if(imageToUploadUri != null) {
//                    Uri selectedImageUri = data.getData();
                    Bitmap srcBmp = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(imageToUploadUri), null, null);
//            final Bitmap photo = (Bitmap) data.getExtras().get("data");
//            imageView.setImageBitmap(photo);

//            btnUpload.setVisibility(View.VISIBLE);
//            btnUpload.setOnClickListener(new View.OnClickListener(){
//                @Override
//                public void onClick(View view) {

                    int orientationValue = orientationListener.rotation;
                    srcBmp = rotateImage(srcBmp, orientationValue);

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    srcBmp.compress(Bitmap.CompressFormat.JPEG, 80, bos);
//            photo.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    byte[] imageData = bos.toByteArray();

                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String filename = "DRCAM_" + timeStamp + "_";
                    Log.i("CAMERA", "===:" + filename);


                    savePhoto(imageData, "phone", filename);

//                    deleteGalleryFile();

                    Intent intent = new Intent(getApplication(), MainActivity.class);
                    intent.putExtra("fragment", "phonefragment");
                    startActivity(intent);
                }
            }catch (Exception e){
                Log.i("INSIDE___",e.toString());
            }
//                }
//            });

        }
    }

    private void savePhoto(byte[] bytes, String cameraKind, String filePath) {

        // 화일 저장
        int filePathLength = filePath.length();
        String filename = filePath.substring(0,filePathLength-2);
        filename = filename + ".JPG";
        final PhotoModel photoModel = PhotoModelService.savePhoto(bytes, filename, 0);

        // 화일 업로드
//        Log.i("Upload Image","Started");

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
                // If the response is JSONObject instead of expected JSONArray
                Log.d("AsyncTask", "HTTP22:" + statusCode + response.toString());
            }
        });
        Log.i("Upload Image","Finished");
    }

    private void deleteGalleryFile(){

        File galleryDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Log.i(TAG,"path:"+galleryDirectory);

        String[] projection = new String[]{MediaStore.Images.ImageColumns._ID,MediaStore.Images.ImageColumns.DATA,MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,MediaStore.Images.ImageColumns.DATE_TAKEN,MediaStore.Images.ImageColumns.MIME_TYPE};
        final Cursor cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
        if(cursor != null){
            cursor.moveToFirst();
            //you can access last taken pics here.
            for(int i=0;i<cursor.getColumnCount();i++){
                Log.i(TAG,"cursorColumn:"+cursor.getColumnName(i));
                Log.i(TAG,"cursorColumn:"+cursor.getString(i));
            }

        }

//        if (galleryDirectory.exists()) {
//            galleryDirectory.delete();
//        }
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
            Log.i("Orientation Listener","value : "+orientationValue+"==============================");

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
