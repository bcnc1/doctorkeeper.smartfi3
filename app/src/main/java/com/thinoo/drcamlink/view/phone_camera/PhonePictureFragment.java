/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thinoo.drcamlink.view.phone_camera;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;
import com.thinoo.drcamlink.R;
import com.thinoo.drcamlink.view.BaseFragment;

import java.io.File;

public class PhonePictureFragment extends BaseFragment
//        implements GestureHandler
{

    private final String TAG = PhonePictureFragment.class.getSimpleName();

    public static PhonePictureFragment newInstance(int objectHandle, String fullPath) {
        Bundle args = new Bundle();
        args.putInt("handle", objectHandle);
        args.putString("fullPath", fullPath);
        PhonePictureFragment f = new PhonePictureFragment();
        f.setArguments(args);
        return f;
    }

//    private Handler handler;
//    private PictureView pictureView;
//    private int objectHandle;
//    private Bitmap picture;
//    private GestureDetector gestureDetector;
    private ProgressBar progressBar;
    private Button cloudBtnBack;

    private String fullPath;
    private Bitmap bitmap;

    private ImageView cloud_image_picasso;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    private float xCoOrdinate, yCoOrdinate;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        handler = new Handler();
//        objectHandle = getArguments().getInt("handle");
        fullPath = getArguments().getString("fullPath");

//        Log.i("WWWWWW===","fullPath ::: "+fullPath);

        View view = inflater.inflate(R.layout.cloud_picture_frag, container, false);
//        pictureView = (PictureView) view.findViewById(R.id.cloud_image);
        cloud_image_picasso = (ImageView) view.findViewById(R.id.cloud_image_picasso);
        progressBar = (ProgressBar) view.findViewById(R.id.cloud_progress);

        cloudBtnBack = (Button) view.findViewById(R.id.cloud_btn_back);
        cloudBtnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                getActivity().getFragmentManager().popBackStack();
            }
        });

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 2;
//        bitmap = BitmapFactory.decodeFile(fullPath,options);
        File f = new File(fullPath);
        Picasso.get().load(f).into(cloud_image_picasso,new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                //do smth when picture is loaded successfully
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onError(Exception e) {
            }
        });

//        pictureView.setPicture(bitmap);

//        gestureDetector = new GestureDetector(getActivity(), this);
//        pictureView.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                gestureDetector.onTouch(event);
//                return true;
//            }
//        });
        mScaleGestureDetector = new ScaleGestureDetector(getActivity(), new PhonePictureFragment.ScaleListener());

        view.setOnTouchListener(new View.OnTouchListener() {
            Float y1 = 0f, y2 = 0f;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleGestureDetector.onTouchEvent(event);

                Point size = new Point();
                getActivity().getWindowManager().getDefaultDisplay().getSize(size);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        xCoOrdinate = cloud_image_picasso.getX() - event.getRawX();
                        yCoOrdinate = cloud_image_picasso.getY() - event.getRawY();
                        Float puffer = 0f;
                        y1 = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        cloud_image_picasso.animate().y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                        cloud_image_picasso.animate().x(event.getRawX() + xCoOrdinate).y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });

//        progressBar.setVisibility(View.GONE);
//        enableUi(true);

        return view;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(1f,
                    Math.min(mScaleFactor, 10.0f));
            cloud_image_picasso.setScaleX(mScaleFactor);
            cloud_image_picasso.setScaleY(mScaleFactor);
            return true;
        }
    }

//    public void enableUi(boolean enabled) {
//
//        cloudBtnBack.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                getActivity().getFragmentManager().popBackStack();
//            }
//        });
//
//    }

//    @Override
//    public void onLongTouch(float posx, float posy) {
//
//    }
//
//    @Override
//    public void onPinchZoom(float pX, float pY, float distInPixel) {
//        pictureView.zoomAt(pX, pY, distInPixel);
//    }
//
//    @Override
//    public void onTouchMove(float dx, float dy) {
//        pictureView.pan(dx, dy);
//    }
//
//    @Override
//    public void onFling(float velx, float vely) {
//        pictureView.fling(velx, vely);
//    }
//
//    @Override
//    public void onStopFling() {
//        pictureView.stopFling();
//    }

}
