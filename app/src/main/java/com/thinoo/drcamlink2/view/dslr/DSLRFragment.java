/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thinoo.drcamlink2.view.dslr;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.activities.LaunchCameraActivity;
import com.thinoo.drcamlink2.madamfive.BlabAPI;
import com.thinoo.drcamlink2.madamfive.MadamfiveAPI;
import com.thinoo.drcamlink2.models.PhotoModel;
import com.thinoo.drcamlink2.ptp.Camera;
import com.thinoo.drcamlink2.ptp.PtpConstants;
import com.thinoo.drcamlink2.ptp.model.LiveViewData;
import com.thinoo.drcamlink2.ptp.model.ObjectInfo;
import com.thinoo.drcamlink2.services.PhotoModelService;
import com.thinoo.drcamlink2.services.UploadManager;
import com.thinoo.drcamlink2.util.DisplayUtil;
import com.thinoo.drcamlink2.view.phone_camera.PhoneCameraFragment;
import com.thinoo.drcamlink2.view.SessionActivity;
import com.thinoo.drcamlink2.view.SessionFragment;
import com.thinoo.drcamlink2.view.sdcard.StorageAdapter;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.getActivity;

public class DSLRFragment extends SessionFragment implements
        Camera.RetrieveImageListener,
        Camera.WorkerListener,
        Camera.RetrieveImageInfoListener,
        Camera.StorageInfoListener,
        OnScrollListener,
        OnItemClickListener {

    private final Handler handler = new Handler();

    private DSLRPhotoAdapter galleryAdapter;
    private ArrayList<PhotoModel> photoList;
    private SimpleDateFormat formatParser;

    private Spinner storageSpinner;
    private StorageAdapter storageAdapter;
    private TextView emptyView;

    @BindView(R.id.dslr_btn_usb_linked)
    ImageView connectedImageView;

    @BindView(R.id.dslr_description_notice)
    TextView dslrTextView;

    @BindView(R.id.dslr_btn_back)
    Button backBtn;

//    @BindView(R.id.dslr_btn_liveview)
//    Button liveViewBtn;

    @BindView(R.id.dslr_upload_Notice)
    TextView upload_Notice;

    @BindView(R.id.dslr_camera_ready_Notice)
    TextView camera_ready_Notice;

    @BindView(R.id.dslr_read_Image)
    ImageView readImage;

    private int currentScrollState;
    private int currentObjectHandle;
    private Bitmap currentBitmap;

    private final String TAG = DSLRFragment.class.getSimpleName();

    private boolean storageRead = false;

    private Fragment displayPictureFragment;
    private Fragment galleryFragment;

//    private MyAsyncTask myAsyncTask;
    private Handler uploadHandler;
    private HandlerThread uploadHandlerThread;

    private int objectHandleNumber = 0;
//    private boolean isD5500=false;

    public static DSLRFragment newInstance() {
        DSLRFragment f = new DSLRFragment();
        return f;
    }

    private String mFileName;
    private File mFile;
    private final String  DEVICE = "dslr";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        formatParser = new SimpleDateFormat("yyyyMMdd'T'HHmmss.S");
        currentScrollState = OnScrollListener.SCROLL_STATE_IDLE;

        View view = inflater.inflate(R.layout.fragment_dslr, container, false);
        ButterKnife.bind(this, view);

        ((SessionActivity) getActivity()).setSessionView(this);

//        liveViewBtn.setVisibility(View.GONE);
        storageAdapter = new StorageAdapter(getActivity());
//        galleryAdapter = new DSLRPhotoAdapter(getActivity());

        enableUi(false);
//        enableUi(true);

// HandlerThread를 이용하여 업로드를 별도 thread에서 처리
         // 기존코드 삭제 예
//        uploadHandlerThread = new HandlerThread("imageUploadThread");
//        uploadHandlerThread.start();
//        uploadHandler = new Handler(uploadHandlerThread.getLooper()){
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                HashMap<String,Object> hashMap = (HashMap<String, Object>) msg.obj;
//                String filename = hashMap.get("filename").toString();
//                PhotoModel photoModel = (PhotoModel) hashMap.get("photoModel");
//
//                Log.d(TAG,"dslr 파일패스 = "+photoModel.getFullpath());
//                //uploadImage(filename);
//                uploadDslrImage(photoModel.getFullpath(), filename);
//
//                photoModel.setUploaded(true);  //db에 업로드 했다는, 비동기라 아직 업로드 전인데 이걸 먼저해도 되나?
//                photoModel.save();  //kimcy 왜 한번 더 저장하지?? 위에서 저렇게 하고 나서 기록하는...
//            }
//        };
// ===> end


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (camera() != null) {
            cameraStarted(camera());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
//        getSettings().setGalleryOrderReversed(orderCheckbox.isChecked());
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.replace(R.id.fragment_container, PhoneCameraFragment.newInstance(), null);
//        ft.addToBackStack(null);
//        ft.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
//            cameraStarted(camera());
        Log.i(TAG,"DSLR fragment onResume");
//        enableUi(false);
        ((SessionActivity) getActivity()).setSessionView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG,"DSLR fragment onPause");
//        cameraStopped(camera());
    }

    @Override
    public void enableUi(final boolean enabled) {
        //galleryView.setEnabled(enabled);
        Log.i(TAG, "DSLR EnableUi..." + enabled);

        if (getActivity()==null)
            return;

        (getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (enabled) {

                    dslrTextView.setText("카메라가 연결되었습니다.");
                    connectedImageView.setImageDrawable(getResources().getDrawable(R.drawable.disconnected));
                    camera_ready_Notice.setVisibility(View.VISIBLE);
                } else {
                    dslrTextView.setText("카메라가 연결되지 않았습니다.");
                    connectedImageView.setImageDrawable(getResources().getDrawable(R.drawable.connected));
                    camera_ready_Notice.setVisibility(View.GONE);
                    readImage.setVisibility(View.GONE);
                }
            }
        });

    }

    @OnClick(R.id.dslr_btn_back)
    public  void backBtnClicked(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, PhoneCameraFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();
    }

//    @OnClick(R.id.dslr_btn_liveview)
//    public  void liveViewBtnClicked(){
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.replace(R.id.fragment_container, LiveViewFragment.newInstance(), null);
//        ft.addToBackStack(null);
//        ft.commit();
//    }

    @Override
    public void cameraStarted(Camera camera) {
        enableUi(true);
        //camera.retrieveStorages(this);
        //emptyView.setText(getString(R.string.gallery_loading));
        Log.i(TAG, "Loading...");
//        Log.i(TAG,camera.getDeviceInfo()+"");
    }

    @Override
    public void cameraStopped(Camera camera) {
        enableUi(false);
//        galleryAdapter.setItems(null);
        MadamfiveAPI.isCameraOn = false;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, PhoneCameraFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void propertyChanged(int property, int value) {
        Log.i(TAG, "propertyChanged " + property + ":" + value);
        if (property == 7) {
            //camera().retrieveStorages(this);
        }
    }

    @Override
    public void propertyDescChanged(int property, int[] values) {
    }

    @Override
    public void setCaptureBtnText(String text) {
    }

    @Override
    public void focusStarted() {
    }

    @Override
    public void focusEnded(boolean hasFocused) {
    }

    @Override
    public void liveViewStarted() {
    }

    @Override
    public void liveViewStopped() {
    }

    @Override
    public void liveViewData(LiveViewData data) {
    }

    @Override
    public void capturedPictureReceived(int objectHandle, String filename, Bitmap thumbnail, Bitmap bitmap) {
        Log.i(TAG, "BITMAP:capturedPictureReceived:" + bitmap.getWidth() + "x" + bitmap.getHeight());
    }

    @Override
    public void objectAdded(int handle, int format) {
        Log.i(TAG, "OBJECT:Added:" + handle + ":" + format);

        if (camera() != null) {
            if (format == PtpConstants.ObjectFormat.EXIF_JPEG) {
                Log.i(TAG, "OBJECT:retrieveImage:");
                camera().retrieveImage(this, handle);
            }
        }
        if (camera() == null) {
            return;
        }

        if (format == PtpConstants.ObjectFormat.EXIF_JPEG) {
            camera().retrieveImageInfo(this, handle);
        }

    }

    @Override
    public void onImageInfoRetrieved(final int objectHandle, final ObjectInfo objectInfo, final Bitmap thumbnail) {

        Log.d(TAG,"dslr받음 => onImageInfoRetrieved");
        handler.post(new Runnable() {
            @Override
            public void run() {
                Camera camera = camera();
                if (!inStart || camera == null) {
                    return;
                }

                if (currentObjectHandle == objectHandle) {

                    Log.i(TAG, "1:onImageInfoRetrieved ###### [" + objectHandle + "] " + objectInfo.filename + "#####");


                    camera_ready_Notice.setVisibility(View.GONE);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    if(displayPictureFragment!=null){
                        ft.remove(displayPictureFragment).commit();
                    }
                    if(galleryFragment != null){
                        ft.remove(galleryFragment).commit();
                        storageRead = false;
                    }
                    if(readImage.isEnabled())  readImage.setVisibility(View.GONE);
                    upload_Notice.setVisibility(View.VISIBLE);

                    sendPhoto(currentObjectHandle, objectInfo, thumbnail, currentBitmap);
                    displayPhoto(objectHandle,currentBitmap);

                }

            }
        });

    }

    private void getImage(int objectHandle){
        camera().retrieveImage(this, objectHandle);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        currentScrollState = scrollState;

        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE: {
                Camera camera = camera();
                if (!inStart || camera == null) {
                    break;
                }
                break;
            }
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    /**
     * Camera.RetrieveImageListener
     *
     * @param objectHandle
     * @param image
     */
    @Override
    public void onImageRetrieved(int objectHandle, Bitmap image) {

        Camera camera = camera();
        if (camera == null) {
            return;
        }

        currentObjectHandle = objectHandle;
        currentBitmap = image;

        camera.retrieveImageInfo(this, objectHandle);

    }

    private void sendPhoto(int objectHandle, ObjectInfo info, Bitmap thumb, Bitmap bitmap) {

        //기존코드 사용(추후 삭제예정)
//        currentObjectHandle = 0;
//        Log.d(TAG,"sendPhoto ObjectInfo = "+info +" Bitmap = "+thumb.getByteCount());
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//
//        try {
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        }catch(Exception e){
//            Log.d(TAG,e.toString());
//            int nh = (int) ( bitmap.getHeight() * (3072.0 / bitmap.getWidth()) );
//            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 3072, nh, true);
//            scaled.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        }
//        Log.i(TAG,"COMPRESSED");
//        final byte[] bytes = baos.toByteArray();
//        final PhotoModel photoModel = PhotoModelService.savePhoto(bytes, info.filename, 1);
//        Log.d(TAG, "저장된 id  = "+photoModel.getId());
//        Log.d(TAG, "업로딩  = "+photoModel.getUploading());



        currentObjectHandle = 0;
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        mFileName = DEVICE + "_" + timeStamp+".jpg";

        PhotoModelService.makeDir(getActivity(), "/thumbnail/");

        String root = getActivity().getExternalFilesDir(Environment.getExternalStorageState()).toString();
        String oriPath = root+ File.separator +mFileName;

        String thumbPath = root+ File.separator +"thumbnail"+File.separator+mFileName;

        try {
            FileOutputStream outSource = new FileOutputStream(oriPath); //파일저장
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outSource);
            outSource.close();

            FileOutputStream outThumb = new FileOutputStream(thumbPath); //파일저장
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, outThumb);
            outThumb.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final PhotoModel photoModel = PhotoModelService.addPhotoModel(oriPath, thumbPath, mFileName,1);

        new UploadManager(getActivity(), photoModel);

// 기존 코드 삭제 예
//        Log.i(TAG,"sendPhoto ==> SAVED");
//
//
//        HashMap<String,Object> taskInfo = new HashMap<>();
//        taskInfo.put("filename",info.filename);
//        taskInfo.put("photoModel",photoModel);
//
//        Message msg = uploadHandler.obtainMessage();
//        msg.obj = taskInfo;
//        uploadHandler.sendMessage(msg);
//        Log.i(TAG,"sendPhoto => Finished");


    }

    private void uploadImage(String filename){
        Log.d(TAG,"Started");
        String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/drcam/" + filename;
        Log.d(TAG,"imagePath = "+imagePath);

        Log.d(TAG,"외부 11 = "+Environment.getExternalStorageDirectory());
        Log.d(TAG,"외부 22 = "+Environment.getExternalStorageDirectory().getAbsolutePath());

//        Log.d(TAG,"외부 = "+Environment.getExternalStorageState());
//
//        File file = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState()), "/drcam/");
//        //test
          File file = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState()), "/kimcy/");
////        Bitmap bitmap = null;
////        File f = new File(imagePath);
            byte[] bytes = null;
            try{
                Log.d(TAG,"파일패스 = "+ file.getAbsolutePath()+filename);
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
        Log.i("Upload Image","Read Bitmap");

        MadamfiveAPI.createPost(bytes, "DSLR", new JsonHttpResponseHandler() {
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
        Log.i("Upload Image","Finished");


    }


    private void uploadDslrImage(String filePath, String filename){

        Log.i("Upload Image","Read Bitmap");

        BlabAPI.ktStoreObject(filePath, "DSLR", filename, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                Log.i("AsyncTask", "Uploading");
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.d("AsyncTask", "이미지 업로드 완료:" + statusCode + responseString);
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(),"DSLR 이미지 저장 완료!",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("AsyncTask", "HTTP22:" + statusCode + response.toString());
            }
        });
        Log.i("Upload Image","Finished");
    }
    @Override
    public void onWorkerStarted() {
    }

    @Override
    public void onWorkerEnded() {
    }

    ////////////////////////////////////////////////////////////////////
    // Camera.StorageInfoListener Override Methods
    ///////////////////////////////////////////////////////////////////

    @Override
    public void onImageHandlesRetrieved(final int[] handles) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!inStart) {
                    return;
                }
                if (handles.length == 0) {
//                    emptyView.setText(getString(R.string.gallery_empty));
                }
                Log.i(TAG, "onImageHandlesRetrieved:" + handles.length);
//                galleryAdapter.setHandles(handles);
                if(objectHandleNumber == 0){
                    objectHandleNumber = handles.length;
                }

                if(objectHandleNumber!=handles.length) {

                    int end = handles.length;
                    if(end!=0)  getImage(handles[end - 1]);
                }
                Log.i(TAG,"objectHandleNumber:::>>>"+objectHandleNumber);


            }
        });
    }

    @Override
    public void onAllStoragesFound() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!inStart || camera() == null) {
                    return;
                }
                if (storageAdapter.getCount() == 0) {
//                    emptyView.setText(getString(R.string.gallery_empty));
                    return;
                } else if (storageAdapter.getCount() == 1) {
//                    storageSpinner.setEnabled(false);
                }
//                storageSpinner.setSelection(0);
                camera().retrieveImageHandles(DSLRFragment.this, storageAdapter.getItemHandle(0),
                        PtpConstants.ObjectFormat.EXIF_JPEG);
            }
        });
    }

    @Override
    public void onStorageFound(final int handle, final String label) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!inStart) {
                    return;
                }
                storageAdapter.add(handle, label);
            }
        });
    }

    private void displayPhoto(int objectHandle,Bitmap currentBitmap){

        upload_Notice.setVisibility(View.GONE);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap resized = Bitmap.createScaledBitmap(currentBitmap, 300, 200, true);

        readImage.setImageBitmap(resized);
        readImage.setEnabled(true);
        readImage.setVisibility(View.VISIBLE);

    }


}
