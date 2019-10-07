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
package com.thinoo.drcamlink2.view.sdcard;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.models.PhotoModel;
import com.thinoo.drcamlink2.ptp.Camera;
import com.thinoo.drcamlink2.ptp.model.LiveViewData;
import com.thinoo.drcamlink2.view.phone_camera.PhoneCameraFragment;
import com.thinoo.drcamlink2.view.SessionActivity;
import com.thinoo.drcamlink2.view.SessionFragment;
import com.thinoo.drcamlink2.view.dslr.DSLRPhotoAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SDCardFragment extends SessionFragment
//        implements
//        Camera.RetrieveImageListener,
//        Camera.WorkerListener,
//        Camera.RetrieveImageInfoListener,
//        Camera.StorageInfoListener,
//        OnScrollListener,
//        OnItemClickListener
{

    private final Handler handler = new Handler();

    private DSLRPhotoAdapter galleryAdapter;
    private ArrayList<PhotoModel> photoList;
    private SimpleDateFormat formatParser;

    private Spinner storageSpinner;
    private StorageAdapter storageAdapter;
    private TextView emptyView;

    @BindView(R.id.btn_usb_linked)
    ImageView connectedImageView;

    @BindView(R.id.dslr_description)
    TextView dslrTextView;

    @BindView(R.id.btn_back)
    Button backBtn;

    @BindView(R.id.btn_display_Storage)
    Button StorageBtn;

    @BindView(R.id.upload_Notice)
    TextView upload_Notice;

//    @BindView(R.id.camera_ready_Notice)
//    TextView camera_ready_Notice;

    @BindView(R.id.read_Image)
    ImageView readImage;

    private int currentScrollState;
    private int currentObjectHandle;
    private Bitmap currentBitmap;

    private final String TAG = SDCardFragment.class.getSimpleName();

    private boolean storageRead = false;

    private Fragment displayPictureFragment;
    private Fragment galleryFragment;

//    private MyAsyncTask myAsyncTask;
    private Handler uploadHandler;
    private HandlerThread uploadHandlerThread;

    public static SDCardFragment newInstance() {
        SDCardFragment f = new SDCardFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        formatParser = new SimpleDateFormat("yyyyMMdd'T'HHmmss.S");
        currentScrollState = OnScrollListener.SCROLL_STATE_IDLE;

        View view = inflater.inflate(R.layout.dslr_frag, container, false);
        ButterKnife.bind(this, view);

        ((SessionActivity) getActivity()).setSessionView(this);

//        galleryAdapter = new DSLRPhotoAdapter(getActivity());

        enableUi(false);
        //enableUi(true);

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
    }

    @Override
    public void onResume() {
        super.onResume();
//            cameraStarted(camera());
        Log.i(TAG,"DSLR fragment onResume");
//        enableUi(false);
//        ((SessionActivity) getActivity()).setSessionView(this);
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
//                    camera_ready_Notice.setVisibility(View.VISIBLE);
                } else {
                    dslrTextView.setText("카메라가 연결되지 않았습니다.");
                    connectedImageView.setImageDrawable(getResources().getDrawable(R.drawable.connected));
//                    camera_ready_Notice.setVisibility(View.GONE);
//                    readImage.setVisibility(View.GONE);
                }
            }
        });

    }

    @OnClick(R.id.btn_back)
    public  void backBtnClicked(){
//        getFragmentManager().popBackStackImmediate();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, PhoneCameraFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();
    }

    @OnClick(R.id.btn_display_Storage)
    public  void storageBtnClicked(){

        readImage.setVisibility(View.GONE);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if(!storageRead) {
//            camera_ready_Notice.setVisibility(View.GONE);

            galleryFragment = GalleryFragment.newInstance();
            ft.replace(R.id.storage_container, galleryFragment, "galleryFragment");
            ft.addToBackStack(null);
            storageRead = true;
        }else{
            ft.remove(galleryFragment);
            ft.replace(R.id.fragment_container, SDCardFragment.newInstance(), null);
            ft.addToBackStack(null);
            storageRead = false;
        }
        ft.commit();

    }

    @Override
    public void cameraStarted(Camera camera) {
        enableUi(true);
        //camera.retrieveStorages(this);
        //emptyView.setText(getString(R.string.gallery_loading));
        Log.i(TAG, "Loading...");//+camera.getDeviceInfo());
    }

    @Override
    public void cameraStopped(Camera camera) {
        enableUi(false);
//        galleryAdapter.setItems(null);
    }

    @Override
    public void propertyChanged(int property, int value) {
        //Log.i(TAG, "propertyChanged " + property + ":" + value);
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
//        Log.i(TAG, "OBJECT:Added:" + handle + ":" + format);
//
//        if (camera() != null) {
//            if (format == PtpConstants.ObjectFormat.EXIF_JPEG) {
//                Log.i(TAG, "OBJECT:retrieveImage:");
//                camera().retrieveImage(this, handle);
//            }
//        }
//        if (camera() == null) {
//            return;
//        }
//
//        if (format == PtpConstants.ObjectFormat.EXIF_JPEG) {
//            camera().retrieveImageInfo(this, handle);
//        }

    }
//
//    @Override
//    public void onImageInfoRetrieved(final int objectHandle, final ObjectInfo objectInfo, final Bitmap thumbnail) {
//
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                Camera camera = camera();
//                if (!inStart || camera == null) {
//                    return;
//                }
//
//                if (currentObjectHandle == objectHandle) {
//
//                    Log.i(TAG, "1:onImageInfoRetrieved ###### [" + objectHandle + "] " + objectInfo.filename + "#####");
//
////                    camera_ready_Notice.setVisibility(View.GONE);
////                    FragmentTransaction ft = getFragmentManager().beginTransaction();
////                    if(displayPictureFragment!=null){
////                        ft.remove(displayPictureFragment).commit();
////                    }
////                    if(galleryFragment != null){
////                        ft.remove(galleryFragment).commit();
////                        storageRead = false;
////                    }
////                    if(readImage.isEnabled())  readImage.setVisibility(View.GONE);
////                    upload_Notice.setVisibility(View.VISIBLE);
//
////                    sendPhoto(currentObjectHandle, objectInfo, thumbnail, currentBitmap);
////                    displayPhoto(objectHandle,currentBitmap);
//
//                }
//
//            }
//        });
//
//    }
//
//    @Override
//    public void onScrollStateChanged(AbsListView view, int scrollState) {
//
//        currentScrollState = scrollState;
//
//        switch (scrollState) {
//            case OnScrollListener.SCROLL_STATE_IDLE: {
//                Camera camera = camera();
//                if (!inStart || camera == null) {
//                    break;
//                }
//                break;
//            }
//        }
//
//    }
//
//    @Override
//    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//    }
//
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//    }
//
//    /**
//     * Camera.RetrieveImageListener
//     *
//     * @param objectHandle
//     * @param image
//     */
//    @Override
//    public void onImageRetrieved(int objectHandle, Bitmap image) {
//
//        Camera camera = camera();
//        if (camera == null) {
//            return;
//        }
//
//        currentObjectHandle = objectHandle;
//        currentBitmap = image;
//
//        camera.retrieveImageInfo(this, objectHandle);
//
//    }
//
//    @Override
//    public void onWorkerStarted() {
//    }
//
//    @Override
//    public void onWorkerEnded() {
//    }
//
//    ////////////////////////////////////////////////////////////////////
//    // Camera.StorageInfoListener Override Methods
//    ///////////////////////////////////////////////////////////////////
//
//    @Override
//    public void onImageHandlesRetrieved(final int[] handles) {
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (!inStart) {
//                    return;
//                }
//                if (handles.length == 0) {
//                    emptyView.setText(getString(R.string.gallery_empty));
//                }
//                Log.i(TAG, "onImageHandlesRetrieved:" + handles.length);
//                galleryAdapter.setHandles(handles);
//            }
//        });
//    }
//
//    @Override
//    public void onAllStoragesFound() {
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (!inStart || camera() == null) {
//                    return;
//                }
//                if (storageAdapter.getCount() == 0) {
//                    emptyView.setText(getString(R.string.gallery_empty));
//                    return;
//                } else if (storageAdapter.getCount() == 1) {
//                    storageSpinner.setEnabled(false);
//                }
//                storageSpinner.setSelection(0);
//                camera().retrieveImageHandles(SDCardFragment.this, storageAdapter.getItemHandle(0),
//                        PtpConstants.ObjectFormat.EXIF_JPEG);
//            }
//        });
//    }
//
//    @Override
//    public void onStorageFound(final int handle, final String label) {
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
////                if (!inStart) {
////                    return;
////                }
//                storageAdapter.add(handle, label);
//            }
//        });
//    }
//

}
