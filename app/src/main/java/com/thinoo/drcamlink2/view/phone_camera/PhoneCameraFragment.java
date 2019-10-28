package com.thinoo.drcamlink2.view.phone_camera;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.usb.UsbManager;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thinoo.drcamlink2.BuildConfig;
import com.thinoo.drcamlink2.activities.LaunchCameraActivity;
import com.thinoo.drcamlink2.view.doctor.DoctorDialogFragment;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.madamfive.MadamfiveAPI;
import com.thinoo.drcamlink2.models.PhotoModel;
import com.thinoo.drcamlink2.services.PhotoModelService;
import com.thinoo.drcamlink2.view.BaseFragment;
import com.thinoo.drcamlink2.view.dslr.DSLRFragment;
import com.thinoo.drcamlink2.view.cloud.CloudFragment;
import com.thinoo.drcamlink2.view.patient.PatientDialogFragment;
import com.thinoo.drcamlink2.view.sdcard.SDCardFragment;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.support.v4.content.FileProvider.getUriForFile;
import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.read_doctorSelectExtraOption;
import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.selectedDoctor;
import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.selectedPatientInfo;


public class PhoneCameraFragment extends BaseFragment {

    private final String TAG = PhoneCameraFragment.class.getSimpleName();

    //@BindView(R.id.camera)
    private CameraView cameraView;
    private ArrayList<PhotoModel> photoList;
    private PhoneCameraPhotoAdapter phoneCameraPhotoAdapter;
    private RelativeLayout photo_container;

    private Animation rotate1Animation;
    private Animation rotate2Animation;
    private Animation rotate3Animation;
    private Animation rotate4Animation;

//    private boolean listviewFlag;
    private Handler saveHandler;
    private HandlerThread saveHandlerThread;

    private boolean cameraIsReady;
    private TextView patient_name;
    private TextView doctor_name;

    //@BindView(R.id.listview_photo)
    RecyclerView listviewPhoto;

    @BindView(R.id.button_list)
    ImageButton btnList;

    @BindView(R.id.button_dslr)
    ImageButton btnDslr;

    @BindView(R.id.button_patient)
    ImageButton btnPatient;

    @BindView(R.id.button_sdcard)
    ImageButton btnSDCard;

//    @BindView(R.id.btn_hide_listview)
//    Button btnHideListview;

    @BindView(R.id.btn_launch_cameraApp)
    Button btnLaunchCameraApp;

    //kimcy: add video
    @BindView(R.id.btn_launch_videoApp)
    Button btnLaunchVideoApp;

    @BindView(R.id.button_capture)
    ImageButton btnCamera;

    @BindView(R.id.button_doctor)
    ImageButton btnDoctor;


//    @BindView(R.id.textview_patient_name)
//    TextView textview_patient_name;


    private OrientationListener orientationListener;

    public static PhoneCameraFragment newInstance() {
        PhoneCameraFragment f = new PhoneCameraFragment();
        return f;
    }

    private final BroadcastReceiver usbOnReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.i(TAG,"intent === "+intent);
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            btnDslr.setVisibility(View.VISIBLE);
                            btnSDCard.setVisibility(View.VISIBLE);
                            MadamfiveAPI.isCameraOn = true;
                        }
                    },
                    2000);
        }
    };

    private final BroadcastReceiver usbOffReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.i(TAG,"intent === "+intent);
            btnDslr.setVisibility(View.INVISIBLE);
            btnSDCard.setVisibility(View.INVISIBLE);
            MadamfiveAPI.isCameraOn = true;
        }
    };

    /**
     * OnCreateView fragment override
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.phone_camera_frag, container, false);
        ButterKnife.bind(this, view);

        cameraIsReady = true;

        photoList = new ArrayList<>();

        listviewPhoto = (RecyclerView)view.findViewById(R.id.listview_photo);

        photo_container = (RelativeLayout) view.findViewById(R.id.photo_container);
        photo_container.setVisibility(View.INVISIBLE);

        btnDslr.setVisibility(View.INVISIBLE);
        btnSDCard.setVisibility(View.INVISIBLE);
        if(MadamfiveAPI.isCameraOn == true){
            btnDslr.setVisibility(View.VISIBLE);
            btnSDCard.setVisibility(View.VISIBLE);
        }

        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        listviewPhoto.setLayoutManager(horizontalLayoutManagaer);

        cameraView = (CameraView) view.findViewById(R.id.camera);

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {
                switch (cameraKitEvent.getType()) {
                    case CameraKitEvent.TYPE_CAMERA_OPEN:
//                        canTakePicture = true;
                        break;

                    case CameraKitEvent.TYPE_CAMERA_CLOSE:
//                        canTakePicture = false;
                        break;
                }
            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                byte[] picture = cameraKitImage.getJpeg();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String filename = "DRCAM_" + timeStamp + "_";
                Log.i("CAMERA", "===:" + filename);
                savePhoto(picture, "phone",filename);

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        List<PhotoModel> cameraPhotoList = PhotoModelService.findAll();
        for (PhotoModel photo : cameraPhotoList) {
            String mode = "DLSR";
            if (photo.getMode() == 0) {
                mode = "CAM";
            }
            photoList.add(photo);
            Log.d("#F", photo.getFilname() + "(" + photo.getUploaded() + "/" + mode + ")");
        }

        phoneCameraPhotoAdapter = new PhoneCameraPhotoAdapter(photoList);
        listviewPhoto.setAdapter(phoneCameraPhotoAdapter);

        rotate1Animation = AnimationUtils.loadAnimation(MadamfiveAPI.getContext(), R.anim.rotate_1);
        rotate2Animation = AnimationUtils.loadAnimation(MadamfiveAPI.getContext(), R.anim.rotate_2);
        rotate3Animation = AnimationUtils.loadAnimation(MadamfiveAPI.getContext(), R.anim.rotate_3);
        rotate4Animation = AnimationUtils.loadAnimation(MadamfiveAPI.getContext(), R.anim.rotate_4);

        orientationListener = new OrientationListener(MadamfiveAPI.getContext());
        orientationListener.enable();

        // HandlerThread를 이용하여 업로드를 별도 thread에서 처리
        saveHandlerThread = new HandlerThread("imageUploadThread");
        saveHandlerThread.start();
        saveHandler = new Handler(saveHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                HashMap<String,Object> hashMap = (HashMap<String, Object>) msg.obj;
                String filename = hashMap.get("filename").toString();
                PhotoModel photoModel = (PhotoModel) hashMap.get("photoModel");

                uploadImage(filename);

                photoModel.setUploaded(true);
                photoModel.save();
            }
        };

        patient_name = (TextView)view.findViewById(R.id.patient_name);
        if(selectedPatientInfo!=null){
            patient_name.setText(selectedPatientInfo.get("name"));
        }

        MadamfiveAPI.read_doctorSelectExtraOption();
        doctor_name = (TextView)view.findViewById(R.id.doctor_name);
        if(!MadamfiveAPI.doctorSelectExtraOption){
            btnDoctor.setVisibility(View.INVISIBLE);
            doctor_name.setVisibility(View.INVISIBLE);
        }
        MadamfiveAPI.read_doctorInfo();
        if(selectedDoctor!=null){
            doctor_name.setText(selectedDoctor.get("name"));
        }
        MadamfiveAPI.read_ShootingImageDisplayExtraOption();
        doctor_name = (TextView)view.findViewById(R.id.doctor_name);
        if(MadamfiveAPI.shootingImageDisplayExtraOption){
            photo_container.setVisibility(View.VISIBLE);
        }

        IntentFilter on = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        IntentFilter off = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        MadamfiveAPI.getContext().registerReceiver(usbOnReciever,on);
        MadamfiveAPI.getContext().registerReceiver(usbOffReciever,off);

//        if(MadamfiveAPI.isListViewOnPhoneCamera){
//            photo_container.setVisibility(View.VISIBLE);
//        }else{
//            photo_container.setVisibility(View.INVISIBLE);
//        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.start();

        photoList.clear();
        List<PhotoModel> cameraPhotoList = PhotoModelService.findAll();
        for (PhotoModel photo : cameraPhotoList) {
            String mode = "DLSR";
            if (photo.getMode() == 0) {
                mode = "CAM";
            }
            photoList.add(photo);
            Log.d("#F", photo.getFilname() + "(" + photo.getUploaded() + "/" + mode + ")");
        }
        phoneCameraPhotoAdapter = new PhoneCameraPhotoAdapter(photoList);
        listviewPhoto.setAdapter(phoneCameraPhotoAdapter);

        Log.i(TAG, "PhoneCameraFragment onResume >>>>>>>>>>");
//        Log.i(TAG,"MadamfiveAPI.isCameraOn ::: "+MadamfiveAPI.isCameraOn);

    }

    @Override
    public void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
//        cameraView.stop();
    }

    public void savePhoto(byte[] bytes, String cameraKind, String filePath) {

        int filePathLength = filePath.length();
        String filename = filePath.substring(0,filePathLength-1);
        filename = filename + ".JPG";

        int orientationValue = orientationListener.rotation;
        byte[] rotatedBytes = rotateImage(bytes,orientationValue);

        final PhotoModel photoModelRotated = PhotoModelService.savePhoto(rotatedBytes, filename, 0);
        photoList.add(0, photoModelRotated);

        phoneCameraPhotoAdapter.notifyDataSetChanged();
        cameraIsReady = true;

        // THread 처리위한 부분
        HashMap<String,Object> taskInfo = new HashMap<>();
        taskInfo.put("filename",photoModelRotated.getFilname());
        taskInfo.put("photoModel",photoModelRotated);
//        taskInfo.put("bitmap",bitmap);
        Message msg = saveHandler.obtainMessage();
        msg.obj = taskInfo;
        saveHandler.sendMessage(msg);
        Log.i("sendPhoto","Finished");
        // thread 처리 end

    }

//    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//    String filename = "DRCAM_" + timeStamp + ".mp4";
//    File file = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState()), "/drcam/");
//
//    private File createVideoFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "DRCAM_" + timeStamp + "_";
//        File storageDir = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState()), "/drcam/");
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//
//        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
//        return image;
//    }

    @OnClick(R.id.button_capture)
    public void onTakePhoto(View view) {
        if(cameraIsReady) {

            MediaActionSound sound = new MediaActionSound();
            sound.play(MediaActionSound.SHUTTER_CLICK);
            cameraView.captureImage();
            cameraIsReady = false;
//            btnCamera.setClickable(false);
        }
    }

    @OnClick(R.id.button_dslr)
    public void onShowDslr(View view) {

        try {
            cameraView.stop();
        }catch(Exception e){
            Log.i(TAG,"ERROR~~~"+e);
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, DSLRFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();
    }

    @OnClick(R.id.button_list)
    public void onToggleList(View view) {

        try {
            cameraView.stop();
        }catch(Exception e){
            Log.i(TAG,"ERROR~~~"+e);
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, CloudFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();

    }

    @OnClick(R.id.btn_launch_cameraApp)
    public void launchCameraApp(View view) {

        try {
            cameraView.stop();
        }catch(Exception e){
            Log.i(TAG,"ERROR~~~"+e);
        }

        Intent intent = new Intent(getActivity(), LaunchCameraActivity.class);
        startActivity(intent);

    }

    //https://stackoverflow.com/questions/17004705/why-is-the-file-saving-here
    @OnClick(R.id.btn_launch_videoApp)
    public void launchVideoApp(View view) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "DRCAM_" + timeStamp + ".mp4";


        File file = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState()) + File.separator + "drcam" + File.separator + filename);

        Uri contentUri = FileProvider.getUriForFile(this.getActivity(), BuildConfig.APPLICATION_ID , file);

        Log.d(TAG,"contentUri = "+ contentUri);

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, contentUri );
        intent.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 1); //high quality
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60*10);  //10분
        this.getActivity().grantUriPermission(this.getActivity().getPackageName(), contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION );

//        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
//        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION );
        //intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1048576);  //size제한, 1MB
        startActivity(intent);

    }


    @OnClick(R.id.button_patient)
    public void onSearchPatient(View veiw){
        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        PatientDialogFragment patientDialogFragment = PatientDialogFragment.newInstance();
        changelogTx.add(patientDialogFragment, "환자검색");
        changelogTx.commit();
    }

    @OnClick(R.id.button_doctor)
    public void onSearchDoctor(View veiw){
        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        DoctorDialogFragment doctorDialogFragment = DoctorDialogFragment.newInstance();
        changelogTx.add(doctorDialogFragment, "의사검색");
        changelogTx.commit();
    }

    @OnClick(R.id.button_sdcard)
    public void onReadSDCard(View veiw){

        try {
            cameraView.stop();
        }catch(Exception e){
            Log.i(TAG,"ERROR~~~"+e);
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, SDCardFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();
    }

//    @OnClick(R.id.btn_hide_listview)
//    public void onHideListview(View view){
//
////        photo_container = (RelativeLayout) view.findViewById(R.id.photo_container);
//
////        Log.i(TAG,"HIDE BTN clicked");
//        if(MadamfiveAPI.isListViewOnPhoneCamera){
//            photo_container.setVisibility(View.INVISIBLE);
//            MadamfiveAPI.isListViewOnPhoneCamera = !MadamfiveAPI.isListViewOnPhoneCamera;
//        }else{
//            photo_container.setVisibility(View.VISIBLE);
//            MadamfiveAPI.isListViewOnPhoneCamera = !MadamfiveAPI.isListViewOnPhoneCamera;
//        }
////        if(listviewFlag){
////            photo_container.setVisibility(View.INVISIBLE);
////            listviewFlag = false;
////        }else{
////            photo_container.setVisibility(View.VISIBLE);
////            listviewFlag = true;
////        }
//    }


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
//                btnDslr.startAnimation(rotate1Animation);
                btnList.startAnimation(rotate1Animation);
                btnPatient.startAnimation(rotate1Animation);
            } else if (orientation > 145 && orientation < 215 && rotation != ROTATION_180) { // REVERSE PORTRAIT
                rotation = ROTATION_180;
//                btnDslr.startAnimation(rotate3Animation);
                btnList.startAnimation(rotate3Animation);
                btnPatient.startAnimation(rotate3Animation);
            } else if (orientation > 55 && orientation < 125 && rotation != ROTATION_270) { // REVERSE LANDSCAPE
                rotation = ROTATION_270;
//                btnDslr.startAnimation(rotate4Animation);
                btnList.startAnimation(rotate4Animation);
                btnPatient.startAnimation(rotate4Animation);
            } else if (orientation > 235 && orientation < 305 && rotation != ROTATION_90) { //LANDSCAPE
                rotation = ROTATION_90;
//                btnDslr.startAnimation(rotate2Animation);
                btnList.startAnimation(rotate2Animation);
                btnPatient.startAnimation(rotate2Animation);
            }
        }
    }

    private byte[] rotateImage(byte[] bytes,int orientationValue)
    {

        try
        {

            Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);;

            Log.i("Orientation Listener","value : "+orientationValue+"==============================");

            if(orientationValue==6){
                image = rotate(image, 90);
            }else if(orientationValue==8){
                image = rotate(image, 180);
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            bytes = stream.toByteArray();

        }
        catch(Exception e)
        {
            Log.e(TAG,e+"");
        }

        return bytes;
    }

    public Bitmap rotate(Bitmap bitmap, int degrees)
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

    private void uploadImage(String filename){
        Log.i(TAG,"uploadImage => Started");
//        String imagePath = Environment.getExternalStorageDirectory() + "/drcam/" + filename;
        File file = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState()), "/drcam/");

//        Bitmap bitmap = null;
//        File f = new File(imagePath);
        byte[] bytes = null;
        try{
//            FileInputStream fis = new FileInputStream(imagePath);
            FileInputStream fis = new FileInputStream(file.getAbsolutePath()+filename);
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
//                        photoModel.setUploaded(true);
//                        photoModel.save();
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("AsyncTask", "HTTP22:" + statusCode + response.toString());
            }
        });
        Log.i(TAG,"uploadImage => Finished");
    }





}