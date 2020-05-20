package com.thinoo.drcamlink2.view.cloud;

import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.thinoo.drcamlink2.Constants;
import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.madamfive.BlabAPI;
import com.thinoo.drcamlink2.madamfive.MadamfiveAPI;
import com.thinoo.drcamlink2.util.SmartFiPreference;
import com.thinoo.drcamlink2.view.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;


public class CloudGalleryFragment extends BaseFragment implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener{

    private final Handler handler = new Handler();

    @BindView(R.id.cloud_list)
    GridView galleryView;

    private CloudGalleryAdapter cloudGalleryAdapter;

    private SimpleDateFormat formatParser;
    private int currentScrollState;
    HashMap<String,String> pictureMap;

    @BindView(R.id.cloud_empty_textview)
    TextView emptyView;

//    private int currentObjectHandle;
//    private Bitmap currentBitmap;
    private ArrayList<HashMap<String,String>> imageInfoList;
//    String accessToken;
//    private int mPageIdx = 0;
//    private final int mPageSize = 30;
//    private int mTotalSize, mTotalPage, mCurPage, mReqPage, mTotalImages;

    private final String TAG = CloudGalleryFragment.class.getSimpleName();

    public static CloudGalleryFragment newInstance() {
        CloudGalleryFragment f = new CloudGalleryFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.cloud_gallery_frag, container, false);
        ButterKnife.bind(this, view);

        cloudGalleryAdapter = new CloudGalleryAdapter(getActivity());
//        Log.i(TAG, "CloudGalleryFragment STARTED");
        getImagesList();
       // accessToken = MadamfiveAPI.getAccessToken();
        enableUi(true);

        return view;
    }

    public void enableUi(final boolean enabled) {
        galleryView.setEnabled(enabled);
//        Log.i(TAG, "Cloud enableUi..." + enabled);
        if (getActivity()==null)
            return;

        (getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
            if (cloudGalleryAdapter==null)
                return;

            cloudGalleryAdapter.setItems(imageInfoList);
            galleryView.setAdapter(cloudGalleryAdapter);
            }
        });
        galleryView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        pictureMap = new HashMap<>();
        pictureMap = cloudGalleryAdapter.getItem(position);

        if(pictureMap.get("cameraKind") == "Video"){
            Toast.makeText(getActivity(), "비디오파일은 미리보기가 제공되지 않습니다!", Toast.LENGTH_SHORT).show();
        } else{
            String imageUrl = pictureMap.get("url");

            // TODO: 2020-01-30 삭제예정
            String imageGuid = pictureMap.get("guid");

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.cloud_detail_container, CloudPictureFragment.newInstance(cloudGalleryAdapter.getItemHandle(position), imageUrl, imageGuid), null);
            ft.addToBackStack(null);
            ft.commit();
        }

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        currentScrollState = scrollState;

        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE: {
                for (int i = 0; i < galleryView.getChildCount(); ++i) {

                    View child = view.getChildAt(i);
                    if (child == null) {
                        continue;
                    }
                    CloudGalleryAdapter.ViewHolder holder = (CloudGalleryAdapter.ViewHolder) child.getTag();
                    if (!holder.done) {
                        holder.done = true;
//                        camera.retrieveImageInfo(this, holder.objectHandle);
                    }
                }

                break;
            }
        }

    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
    }

    private void getImagesList(){
//        Log.i(TAG, "CloudGalleryFragment getImagesList");
        imageInfoList = new ArrayList<HashMap<String, String>>();

        MadamfiveAPI.getImageURL("0",new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.i("CLoud Approach", "onStart2:");
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {

                Log.d(TAG, "HTTP21:" + statusCode + responseString);

                try {

                    JSONObject response = new JSONObject(responseString);
                    JSONArray imagesArray = response.getJSONArray("posts");

                    for(int i=0;i<imagesArray.length();i++){
                        JSONObject imagesObject = imagesArray.getJSONObject(i);
//                        Log.i(TAG,"Inside JSON Array");
                        Log.i(TAG,"Inside value : "+ imagesObject.get("id").toString());

                        HashMap<String,String> imageInfo = new HashMap<>();
                        imageInfo.put("url",imagesObject.getString("id"));
                        imageInfo.put("uploadDate",imagesObject.getString("created"));

                        try{
                            imageInfo.put("cameraKind",imagesObject.getString("title"));
                        }catch(Exception e){
                            imageInfo.put("cameraKind","DSLR");
                        }

                        try {
                            JSONArray attachmentArray = imagesArray.getJSONObject(i).getJSONArray("attachments");
                            JSONObject attach = attachmentArray.getJSONObject(0);
                            imageInfo.put("guid", attach.getString("guid"));
                        }catch (Exception e){
                            imageInfo.put("guid", "none");
                        }

                        Log.i(TAG,"Inside HashMap : "+ imageInfo.toString());
                        imageInfoList.add(imageInfo);
                    }

                    cloudGalleryAdapter.setItems(imageInfoList);
                    cloudGalleryAdapter.notifyDataSetChanged();
                    Log.i("CloudFragment","list received! === length:"+imageInfoList.size());

                }catch (Exception e){
                }
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("CLoud", "HTTP22:" + statusCode + response.toString());
//                galleryAdapter.notifyDataSetChanged();
            }
        });
        Log.i("List in CloudFragment",imageInfoList.size()+"");
    }


}
