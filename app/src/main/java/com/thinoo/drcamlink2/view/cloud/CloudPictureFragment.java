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
package com.thinoo.drcamlink2.view.cloud;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;
import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.madamfive.MadamfiveAPI;
import com.thinoo.drcamlink2.view.BaseFragment;

import java.net.URLEncoder;

public class CloudPictureFragment extends BaseFragment
//        implements GestureHandler
{

    private final String TAG = CloudPictureFragment.class.getSimpleName();

    public static CloudPictureFragment newInstance(int objectHandle, String imageUrl, String imageGuid) {
        Bundle args = new Bundle();
        args.putInt("handle", objectHandle);
        args.putString("imageUrl", imageUrl);
        args.putString("imageGuid",imageGuid);
        CloudPictureFragment f = new CloudPictureFragment();
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

    private String accessToken;
    private String imageUrl;
    private String imageGuid;
    private String imageURL;
    private Bitmap bitmap;

    private ImageView cloud_image_picasso;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    private float xCoOrdinate, yCoOrdinate;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        handler = new Handler();
//        objectHandle = getArguments().getInt("handle");
        imageUrl = getArguments().getString("imageUrl");
        imageGuid = getArguments().getString("imageGuid");

        Log.i(TAG, "imageUrl = "+imageUrl);
        Log.i(TAG, "imageGuid = "+imageGuid);

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

        accessToken = MadamfiveAPI.getAccessToken();

        imageURL = "http://api.doctorkeeper.com:7818/v1/posts/"+imageUrl+
                "/attachments/"+imageGuid+"?size=medium&accessToken="+ URLEncoder.encode(accessToken);

        Picasso.get().load(imageURL).into(cloud_image_picasso,new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                //do smth when picture is loaded successfully
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onError(Exception e) {
            }
        });

        mScaleGestureDetector = new ScaleGestureDetector(getActivity(), new ScaleListener());

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


//        DownloadImageAsyncTask task = new DownloadImageAsyncTask();
//        task.execute();

//        pictureView.setPicture(bitmap);

//        gestureDetector = new GestureDetector(getActivity(), this);
//        pictureView.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                gestureDetector.onTouch(event);
//                return true;
//            }
//        });

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

//    private class DownloadImageAsyncTask extends AsyncTask <String,Integer,String>{
//        @Override
//        protected String doInBackground(String... strings) {
//
//            Log.i("Download Async","START =========>");
//            Log.i("Cloud clicked Image URL",imageURL);
//
//            try {
//                bitmap = BitmapFactory.decodeStream((InputStream)new URL(imageURL).getContent());
//            }catch (Exception e){
////                Log.i(TAG,"IMage URL"+e.toString());
//                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
//            }
//
//            Log.i("Download Async","FINISH =========>");
//
//            return "DONE";
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            pictureView.setPicture(bitmap);
//            progressBar.setVisibility(View.GONE);
//        }
//    }

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
//
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
