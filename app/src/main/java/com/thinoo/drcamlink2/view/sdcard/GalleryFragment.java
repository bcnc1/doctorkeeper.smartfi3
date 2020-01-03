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

import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.madamfive.MadamfiveAPI;
import com.thinoo.drcamlink2.models.PhotoModel;
import com.thinoo.drcamlink2.ptp.Camera;
import com.thinoo.drcamlink2.ptp.PtpConstants;
import com.thinoo.drcamlink2.ptp.model.LiveViewData;
import com.thinoo.drcamlink2.ptp.model.ObjectInfo;
import com.thinoo.drcamlink2.services.PhotoModelService;
import com.thinoo.drcamlink2.view.SessionActivity;
import com.thinoo.drcamlink2.view.SessionFragment;
import com.thinoo.drcamlink2.view.phone_camera.PhoneCameraFragment;
import com.thinoo.drcamlink2.view.sdcard.GalleryAdapter.ViewHolder;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GalleryFragment extends SessionFragment
        implements Camera.StorageInfoListener,
        Camera.RetrieveImageListener,
        Camera.RetrieveImageInfoListener,
        OnScrollListener,
        OnItemClickListener {

    private final Handler handler = new Handler();

    @BindView(R.id.storage_spinner)
    Spinner storageSpinner;

    private StorageAdapter storageAdapter;

    @BindView(android.R.id.list)
    GridView galleryView;

    private GalleryAdapter galleryAdapter;

    @BindView(R.id.reverve_order_checkbox)
    CheckBox orderCheckbox;

    private SimpleDateFormat formatParser;
    boolean gotThumbWidth;
    private int currentScrollState;
    public HashMap<String,Object> pictureMap;

//    @BindView(R.id.empty_textview)
//    TextView emptyView;

    private int currentObjectHandle;
    private Bitmap currentBitmap;
    private final String TAG = GalleryFragment.class.getSimpleName();

    private ArrayList<PhotoModel> photoModelLists;
//    private HashMap<Integer,Boolean> multiSelectionMap;

    private ArrayList<Integer> selectedObjectHandles;
    private int selectedImageNumber;
    private boolean selectedToast;

    private Handler saveHandler;
    private HandlerThread saveHandlerThread;

    private ProgressBar multi_image_uploading_progressbar;
    private TextView multi_image_uploading_message;
    private int numberOfSendPhoto;
    private int progressBarPortionSum;

//    private ProgressTask mProgressTask;

    public static GalleryFragment newInstance() {
        GalleryFragment f = new GalleryFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        formatParser = new SimpleDateFormat("yyyyMMdd'T'HHmmss.S");
        currentScrollState = OnScrollListener.SCROLL_STATE_IDLE;

        View view = inflater.inflate(R.layout.gallery_frag, container, false);
        ButterKnife.bind(this, view);

        ((SessionActivity) getActivity()).setSessionView(this);

        //storageSpinner = (Spinner) view.findViewById(R.id.storage_spinner);
        storageAdapter = new StorageAdapter(getActivity());
        storageSpinner.setAdapter(storageAdapter);
        storageSpinner.setVisibility(View.GONE);   //만들고 여기서는 사용하지 않음

//        emptyView.setText(getString(R.string.gallery_loading));

        //galleryView = (GridView) view.findViewById(android.R.id.list);
        galleryAdapter = new GalleryAdapter(getActivity(), this);
        galleryAdapter.setReverseOrder(getSettings().isGalleryOrderReversed());
        galleryView.setAdapter(galleryAdapter);
        galleryView.setOnScrollListener(this);
//        galleryView.setEmptyView(emptyView);
        galleryView.setOnItemClickListener(this);

        //orderCheckbox = (CheckBox) view.findViewById(R.id.reverve_order_checkbox);
        orderCheckbox.setChecked(getSettings().isGalleryOrderReversed());
        orderCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onReverseOrderStateChanged(isChecked);
            }
        });
        orderCheckbox.setVisibility(View.GONE);

        enableUi(false);

        pictureMap = new HashMap<>();

        selectedImageNumber=0;
        selectedObjectHandles = new ArrayList<>();
//        selectedToast=false;

        saveHandlerThread = new HandlerThread("imageUploadThread_SDcard");
        saveHandlerThread.start();
        saveHandler = new Handler(saveHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
//                int h = (Integer) msg.obj;
//                retrieveDirection(h);
                ArrayList<Integer> tempList = (ArrayList<Integer>) msg.obj;
                retrieveDirection(tempList);
            }
        };

        multi_image_uploading_progressbar = (ProgressBar)view.findViewById(R.id.multi_image_uploading_progressbar);
        multi_image_uploading_progressbar.setVisibility(View.INVISIBLE);
        multi_image_uploading_message = view.findViewById(R.id.multi_image_uploading_message);
        multi_image_uploading_message.setVisibility(View.INVISIBLE);
//        mProgressTask = new ProgressTask(multi_image_uploading_progressbar,MadamfiveAPI.getContext());

        return view;
    }


    private void retrieveDirection(ArrayList<Integer> list){
        for(int h : list) {
            camera().retrieveImage(this, h);
        }
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
        getSettings().setGalleryOrderReversed(orderCheckbox.isChecked());
    }

    protected void onReverseOrderStateChanged(boolean isChecked) {
        galleryAdapter.setReverseOrder(isChecked);
    }

    @Override
    public void enableUi(boolean enabled) {

        Log.i(TAG, "READ Storage EnableUi..." + enabled);

        storageSpinner.setEnabled(enabled);
        galleryView.setEnabled(enabled);
        orderCheckbox.setEnabled(enabled);

        // 이미 저장되어 있는 PhotoModel db 읽어오기
        photoModelLists = PhotoModelService.findImageListOld();

        numberOfSendPhoto=0;
        progressBarPortionSum=0;

        //        galleryView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
//
//                String currentObject = galleryAdapter.getItem(i).toString();
//                String currentObjectInfo = pictureMap.get(currentObject)+"";
//                Bitmap bitmap = (Bitmap) pictureMap.get(currentObject+"_data");
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                byte[] byteArray = stream.toByteArray();
//
////        Toast.makeText(getActivity(),"이미지에 따라 15초이상 소요될 수도 있습니다",Toast.LENGTH_LONG).show();
////        FragmentTransaction ft = getFragmentManager().beginTransaction();
////        ft.replace(R.id.storage_container, PictureFragment.newInstance(galleryAdapter.getItemHandle(position), currentObjectInfo), null);
//
//                FragmentTransaction ft = getFragmentManager().beginTransaction();
//                ft.replace(R.id.storage_container, SDcardPictureFragment.newInstance(galleryAdapter.getItemHandle(i), currentObjectInfo, byteArray), null);
//
//                ft.addToBackStack(null);
//                ft.commit();
//                return false;
//            }
//        });

//        if (!enabled) {
//            emptyView.setText("카메라가 연결되지 않았습니다");
//        }
//      else{
//            dslrTextView.setText("카메라가 연결되었습니다. 촬영하세요.");
//            connectedImageView.setImageDrawable(getResources().getDrawable(R.drawable.disconnected));
//      }

    }

    @Override
    public void cameraStarted(Camera camera) {
        enableUi(true);
        camera.retrieveStorages(this);
//        emptyView.setText(getString(R.string.gallery_loading));
        Log.i(TAG, "Reading Storage...");
    }

    @Override
    public void cameraStopped(Camera camera) {
        Log.d(TAG,"cameraStopped");
        enableUi(false);
        galleryAdapter.setHandles(new int[0]);
    }

    @Override
    public void propertyChanged(int property, int value) {
//        if (property == 7) {
//            camera().retrieveStorages(this);
//        }
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
//                camera().retrieveImage(this, handle);
            }
        }
        if (camera() == null) {
            return;
        }

        if (format == PtpConstants.ObjectFormat.EXIF_JPEG) {
//            camera().retrieveImageInfo(this, handle);
        }

        enableUi(false);

//        if (format == PtpConstants.ObjectFormat.EXIF_JPEG) {
//            if (isPro && liveViewToggle.isChecked() && showCapturedPictureNever) {
//                camera().retrieveImageInfo(this, handle);
//                handler.post(liveViewRestarterRunner);
//            } else {
//                camera().retrievePicture(handle);
//            }
//        }
    }

    @Override
    public void onStorageFound(final int handle, final String label) {
        Log.d(TAG, "onStorageFound");
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

    @Override
    public void onAllStoragesFound() {
        Log.d(TAG, "onAllStoragesFound");
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
                    storageSpinner.setEnabled(false);
                }
                storageSpinner.setSelection(0);
                camera().retrieveImageHandles(GalleryFragment.this, storageAdapter.getItemHandle(0),
                        PtpConstants.ObjectFormat.EXIF_JPEG);
            }
        });
    }

    @Override
    public void onImageHandlesRetrieved(final int[] handles) {
        Log.d(TAG, "onImageHandlesRetrieved");
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

                galleryAdapter.setHandles(handles);

//                for(int k=0;k<handles.length;k++) {
//                    multiSelectionMap.put(handles[k], false);
//                }
            }
        });
    }

    @Override
    public void onImageInfoRetrieved(final int objectHandle, final ObjectInfo objectInfo, final Bitmap thumbnail) {

        Log.d(TAG, "onImageInfoRetrieved");
        handler.post(new Runnable() {

            @Override
            public void run() {

                final Camera camera = camera();

                if (!inStart || camera == null) {
                    return;
                }

                if (currentObjectHandle == objectHandle) {
                    Log.i(TAG, "1:onImageInfoRetrieved ###### [" + objectHandle + "] " + objectInfo.filename + "#####");
                }
                if (!gotThumbWidth && thumbnail != null) {
                    gotThumbWidth = true;
                    galleryAdapter.setThumbDimensions(thumbnail.getWidth(), thumbnail.getHeight());
                }

                for (int i = 0; i < galleryView.getChildCount(); ++i) {

                    //View child = galleryView.getChildAt(i );
                    View child = galleryView.getChildAt(i );

                    Log.d(TAG,"child :"+child);
                    if (child == null) {
                        Log.d(TAG,"child is null !!");
                        continue;
                    }
                    final ViewHolder holder = (ViewHolder) child.getTag();
                    if (holder.objectHandle == objectHandle) {

                        pictureMap.put(objectHandle+"",objectInfo.filename); //업로드할 파일들..
                        pictureMap.put(objectHandle+"_data",thumbnail);

                        holder.image1.setImageBitmap(thumbnail);  //썸네일

                        Boolean uploadCheck=false;
                        for(int d = 0 ;d < photoModelLists.size();d++){
                            PhotoModel p = photoModelLists.get(d);
                            if(objectInfo.filename.equals(p.getFilename())){
                                uploadCheck = p.getUploaded();
                                break;
                            }
                        }
//                        PhotoModel pm = PhotoModel.findById(PhotoModel.class,photoModelLists.get(0).getId());
                        if(uploadCheck==true) {
                            holder.sdcard_image_upload_check.setVisibility(View.VISIBLE);
                        }

                        if (!"".equals(objectInfo.captureDate)) {
                            try {
                                Date date = formatParser.parse(objectInfo.captureDate);
                                DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                                String createdDate = df.format(date);
                                holder.date.setText(createdDate);
//                                holder.date.setText(date.toLocaleString());
                            } catch (ParseException e) {
                            }
                        }
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.d(TAG,"onScrollStateChanged = "+scrollState);
        currentScrollState = scrollState;
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE: {
                Camera camera = camera();   //연결된 카메라를 말함..
                if (!inStart || camera == null) {
                    break;
                }
                for (int i = 0; i < galleryView.getChildCount(); ++i) {

                    View child = view.getChildAt(i);
                    if (child == null) {
                        continue;
                    }
                    ViewHolder holder = (ViewHolder) child.getTag();
                    Log.d(TAG,"holder.done = "+holder.done);
                    if (!holder.done) {
                        Log.d(TAG,"holder.done => false 신규요청??? ");
                        holder.done = true;
                        camera.retrieveImageInfo(this, holder.objectHandle);
                    }
                }

                break;
            }
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    public void onNewListItemCreated(ViewHolder holder) {
        Log.d(TAG,"onNewListItemCreated");
        if (currentScrollState == SCROLL_STATE_IDLE) {
            Camera camera = camera();
            if (camera == null) {
                return;
            }
            holder.done = true;
            //Log.i(TAG, "1:onNewListItemCreated ###### [" + holder.objectHandle + "] #####");
            camera.retrieveImageInfo(this, holder.objectHandle);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Log.i(TAG,"item selected");

//        View child = galleryView.getChildAt(position);
        View child = galleryView.getChildAt(position - parent.getFirstVisiblePosition());
//
        final ViewHolder holder = (ViewHolder) child.getTag();

        if(selectedObjectHandles.contains(galleryAdapter.getItemHandle(position))){
            int remove = selectedObjectHandles.indexOf(galleryAdapter.getItemHandle(position));
            selectedObjectHandles.remove(remove);
            holder.sdcard_image_need_upload.setVisibility(View.INVISIBLE);
            selectedImageNumber--;
        }else{
            selectedObjectHandles.add(galleryAdapter.getItemHandle(position));
            holder.sdcard_image_need_upload.setVisibility(View.VISIBLE);
            selectedImageNumber++;
        }

        LayoutInflater inflater =  (LayoutInflater) MadamfiveAPI.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.toast_multiple_selection_sdcard,  (ViewGroup) view.findViewById(R.id.toast_container));

        TextView text = (TextView) layout.findViewById(R.id.sdcard_toast_textview);
        Button sdcard_toast_select = (Button) layout.findViewById(R.id.sdcard_toast_select);
        Button sdcard_toast_upload = (Button) layout.findViewById(R.id.sdcard_toast_upload);

        final Toast toast = new Toast(MadamfiveAPI.getContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 500);
        toast.setDuration(Toast.LENGTH_SHORT);
        text.setText(selectedImageNumber+"개 이미지 선택");
        toast.setView(layout);
        toast.show();

        sdcard_toast_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toast.cancel();
            }
        });

        sdcard_toast_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toast.cancel();

//                mProgressTask.execute(25);
                MadamfiveAPI.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        multi_image_uploading_progressbar.setVisibility(View.VISIBLE);
                        multi_image_uploading_message.setVisibility(View.VISIBLE);

                        int[] tempHandles =new int[0];
                        galleryAdapter.setHandles(tempHandles);

                        new CountDownTimer(60000, 2000) {
                            int tick = 0;
                            public void onTick(long millisUntilFinished) {
                                tick++;
                                multi_image_uploading_progressbar.setProgress(20+tick);
                            }
                            public void onFinish() {
                                multi_image_uploading_progressbar.setProgress(50);
                            }
                        }.start();

                    }
                });

                uploadSelectedHandles();
            }
        });

    }


    /**
     * Camera.RetrieveImageListener
     *
     * @param objectHandle
     * @param image
     */
    @Override
    public void onImageRetrieved(int objectHandle, Bitmap image) {
        Log.d(TAG,"onImageRetrieved");
        Camera camera = camera();
        if (camera == null) {
            return;
        }

        currentObjectHandle = objectHandle;
        currentBitmap = image;
        String currentObjectInfo = pictureMap.get(currentObjectHandle+"")+"";

        Log.i(TAG,"retrieveImage+++Done"+currentObjectInfo);

        sendPhoto(currentObjectInfo, currentBitmap);

    }

    private void uploadSelectedHandles(){

        Log.i(TAG,"uploadSelectedHandles");

        Message msg = saveHandler.obtainMessage();
        msg.obj = selectedObjectHandles;
        saveHandler.sendMessage(msg);

    }

    private void sendPhoto(String filename, Bitmap bitmap) {

        Log.i(TAG,"sendPhoto"+filename);

        int progressBarPortion = 50 / selectedObjectHandles.size();
        numberOfSendPhoto++;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }catch(Exception e){
            Log.d(TAG,e.toString());
            int nh = (int) ( bitmap.getHeight() * (3072.0 / bitmap.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 3072, nh, true);
            scaled.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }

        final byte[] bytes = baos.toByteArray();

        final PhotoModel photoModel = PhotoModelService.savePhoto(bytes, filename, 1);

        MadamfiveAPI.createPost(bytes, "DSLR", new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                Log.i("CAMERA", "onStart2:");
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.d("CAMERA", "HTTP21:" + statusCode + responseString);
                photoModel.setUploaded(true);
                photoModel.save();
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                Log.d("CAMERA", "HTTP22:" + statusCode + response.toString());
            }
        });

        progressBarPortionSum = 50 + progressBarPortion + progressBarPortionSum;

        MadamfiveAPI.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                multi_image_uploading_progressbar.setProgress(progressBarPortionSum);
            }
        });

        if(numberOfSendPhoto==selectedObjectHandles.size()){
//            multi_image_uploading_progressbar.setVisibility(View.INVISIBLE);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, PhoneCameraFragment.newInstance(), null);
            ft.addToBackStack(null);
            ft.commit();
        }

    }


}
