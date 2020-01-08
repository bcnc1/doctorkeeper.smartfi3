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

/**
 * 클라우드에 올라간 이미지를 grid view 형태로 보여주기 위해..
 */

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

    private int currentObjectHandle;
    private Bitmap currentBitmap;
    private ArrayList<HashMap<String,String>> imageInfoList;
    String accessToken;
    private int mPageIdx = 0;
    private final int mPageSize = 30;
    private int mTotalSize, mTotalPage, mCurPage, mReqPage, mTotalImages;

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

        getImagesList();
        accessToken = MadamfiveAPI.getAccessToken();

        enableUi(true);

        return view;
    }

    public void enableUi(final boolean enabled) {
        galleryView.setEnabled(enabled);
        Log.i(TAG, "Cloud enableUi..." + enabled);

        if (getActivity()==null)
            return;

        (getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {

            if (cloudGalleryAdapter==null)                    return;

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
        String imageUrl = pictureMap.get("url");
        String imageGuid = pictureMap.get("guid");

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.cloud_detail_container, CloudPictureFragment.newInstance(cloudGalleryAdapter.getItemHandle(position), imageUrl,imageGuid), null);
        ft.addToBackStack(null);
        ft.commit();

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

        imageInfoList = new ArrayList<HashMap<String, String>>();

        // TODO: 2020-01-08 환자 사진이 극단적으로 많을 경우 페이징조회를 구현 필요..
        if(Constants.PATIENT_HAS_MANY_IMAGES){
            BlabAPI.getPatientImages(getActivity(),mPageIdx, mPageSize, SmartFiPreference.getSfPatientCustNo(getActivity()), new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Log.w(TAG,"목록조회 성공");

                    try{
                        String code =  response.get(Constants.EMRAPI.CODE).toString();
                        if(code.equals(Constants.EMRAPI.CODE_200)){

                            JSONObject des = (JSONObject) response.get("data");

                            mCurPage = des.getInt("pageIdx");
                            mTotalPage = des.getInt("totalPage");  //스크롤할 수 있는 최대값
                            mTotalImages = des.getInt("totalSize"); //현재 저장된 이미지 전체 갯수

                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                        Log.e(TAG," 응답에러");
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Log.w(TAG,"목록조회 실패 = "+statusCode);
                }
            });
        }else{
            BlabAPI.getPatientImagesAll(getActivity(), SmartFiPreference.getSfPatientCustNo(getActivity()), new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);

                    try{
                        String code =  response.get(Constants.EMRAPI.CODE).toString();
                        if(code.equals(Constants.EMRAPI.CODE_200)){

                            JSONArray imgInfolist = response.getJSONArray((Constants.EMRAPI.DATA));

                            for(int i=0; i<imgInfolist.length(); i++){
                                //JSONObject imgObj = imgInfolist.getJSONObject(i);
                                HashMap<String,String> imageInfo = new HashMap<>();
                                imageInfo.put("uploadDate", imgInfolist.getJSONObject(i).getString("updDttm"));
                                imageInfo.put("thumurl", imgInfolist.getJSONObject(i).getString("thnlFilePath"));
                                imageInfo.put("url", imgInfolist.getJSONObject(i).getString("phtoFilePath"));
                                imageInfo.put("uploadDate", imgInfolist.getJSONObject(i).getString("regDttm"));

                                String fileName = imgInfolist.getJSONObject(i).getString("phtoFileNm");
                                if(fileName.contains("phone") && fileName.contains(".mp4")){
                                    imageInfo.put("cameraKind", "Video");
                                } else if(fileName.contains("dslr")){
                                    imageInfo.put("cameraKind", "DSLR");
                                }else{
                                    imageInfo.put("cameraKind", "Phone");
                                }

                                imageInfoList.add(imageInfo);
                            }

                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                        Log.e(TAG," 응답에러");
                    }

                    cloudGalleryAdapter.setItems(imageInfoList);
                    cloudGalleryAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                }
            });
        }




//        MadamfiveAPI.getImageURL("0",new JsonHttpResponseHandler() {
//
//            @Override
//            public void onStart() {
//                Log.i("CLoud Approach", "onStart2:");
//            }
//
//            @Override
//            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
//
//                Log.d("CLoud", "HTTP21:" + statusCode + responseString);
//
//                try {
//
//                    JSONObject response = new JSONObject(responseString);
//                    JSONArray imagesArray = response.getJSONArray("posts");
//
//                    for(int i=0;i<imagesArray.length();i++){
//                        JSONObject imagesObject = imagesArray.getJSONObject(i);
////                        Log.i(TAG,"Inside JSON Array");
////                        Log.i(TAG,"Inside value : "+ imagesObject.get("id").toString());
//
//                        HashMap<String,String> imageInfo = new HashMap<>();
//                        imageInfo.put("url",imagesObject.getString("id"));
//                        imageInfo.put("uploadDate",imagesObject.getString("created"));
//
//                        try{
//                            imageInfo.put("cameraKind",imagesObject.getString("title"));
//                        }catch(Exception e){
//                            imageInfo.put("cameraKind","DSLR");
//                        }
//
//                        try {
//                            JSONArray attachmentArray = imagesArray.getJSONObject(i).getJSONArray("attachments");
//                            JSONObject attach = attachmentArray.getJSONObject(0);
//                            imageInfo.put("guid", attach.getString("guid"));
//                        }catch (Exception e){
//                            imageInfo.put("guid", "none");
//                        }
//
//                        Log.i(TAG,"Inside HashMap : "+ imageInfo.toString());
//                        imageInfoList.add(imageInfo);
//                    }
//
//                    cloudGalleryAdapter.setItems(imageInfoList);
//                    cloudGalleryAdapter.notifyDataSetChanged();
//                    Log.i("CloudFragment","list received! === length:"+imageInfoList.size());
//
//                }catch (Exception e){
//                }
////                photoModel.setUploaded(true);
////                photoModel.save();
////                galleryAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
//                // If the response is JSONObject instead of expected JSONArray
//                Log.d("CLoud", "HTTP22:" + statusCode + response.toString());
////                galleryAdapter.notifyDataSetChanged();
//            }
//        });
        Log.i("List in CloudFragment",imageInfoList.size()+"");
    }


}
