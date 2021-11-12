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
package com.doctorkeeper.smartfi.view.phonelist;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
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
import android.widget.Toast;

import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.doctorkeeper.smartfi.R;
import com.doctorkeeper.smartfi.network.BlabAPI;
import com.doctorkeeper.smartfi.services.PictureIntentService;
import com.doctorkeeper.smartfi.view.BaseFragment;


public class PhoneListPictureFragment extends BaseFragment {

    private final String TAG = com.doctorkeeper.smartfi.view.phonelist.PhoneListPictureFragment.class.getSimpleName();

    public static com.doctorkeeper.smartfi.view.phonelist.PhoneListPictureFragment newInstance(int objectHandle, String imageUrl, String imageGuid) {
        Bundle args = new Bundle();
        args.putInt("handle", objectHandle);
        args.putString("imageUrl", imageUrl);
        args.putString("imageGuid",imageGuid);
        com.doctorkeeper.smartfi.view.phonelist.PhoneListPictureFragment f = new com.doctorkeeper.smartfi.view.phonelist.PhoneListPictureFragment();
        f.setArguments(args);
        return f;
    }

    private ProgressBar progressBar;
    private Button cloudBtnBack;
    private Button cloudBtnUpload;

    private String accessToken;
    private String imageUrl;
    private String imageGuid;
    private String imageURL;
    private Bitmap bitmap;

    private ImageView phone_list_image_picasso;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    private float xCoOrdinate, yCoOrdinate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        imageUrl = getArguments().getString("imageUrl");
        imageGuid = getArguments().getString("imageGuid");
        Log.v(TAG,"imageUrl:::"+imageUrl);

        View view = inflater.inflate(R.layout.phone_list_picture_frag, container, false);

        phone_list_image_picasso = (ImageView) view.findViewById(R.id.phone_list_image_picasso);
        progressBar = (ProgressBar) view.findViewById(R.id.phone_list_progress);

        cloudBtnBack = (Button) view.findViewById(R.id.phone_list_btn_back);
        cloudBtnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                getActivity().getFragmentManager().popBackStack();
            }
        });

        cloudBtnUpload = (Button) view.findViewById(R.id.phone_list_btn_upload);
        cloudBtnUpload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
//                Toast.makeText(BlabAPI.getActivity(),"upload"+imageUrl, Toast.LENGTH_SHORT).show();
                PictureIntentService.startUploadPicture(BlabAPI.getActivity(), imageUrl);
            }
        });

        Glide.with(BlabAPI.getActivity()).load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
        }).into(phone_list_image_picasso);

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
                        xCoOrdinate = phone_list_image_picasso.getX() - event.getRawX();
                        yCoOrdinate = phone_list_image_picasso.getY() - event.getRawY();
                        Float puffer = 0f;
                        y1 = event.getRawY();

                        break;
                    case MotionEvent.ACTION_MOVE:
                        phone_list_image_picasso.animate().y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                        phone_list_image_picasso.animate().x(event.getRawX() + xCoOrdinate).y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                        break;
                    case MotionEvent.ACTION_UP:

                        break;
                }

                return true;
            }
        });

        return view;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(1f,
                    Math.min(mScaleFactor, 10.0f));
            phone_list_image_picasso.setScaleX(mScaleFactor);
            phone_list_image_picasso.setScaleY(mScaleFactor);
            return true;
        }
    }

}
