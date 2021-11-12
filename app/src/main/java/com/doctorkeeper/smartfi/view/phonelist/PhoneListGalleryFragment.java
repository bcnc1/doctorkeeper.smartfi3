package com.doctorkeeper.smartfi.view.phonelist;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import androidx.annotation.Nullable;

import com.doctorkeeper.smartfi.R;
import com.doctorkeeper.smartfi.network.BlabAPI;
import com.doctorkeeper.smartfi.view.BaseFragment;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PhoneListGalleryFragment extends BaseFragment implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener{

    private final Handler handler = new Handler();

    @BindView(R.id.cloud_list)
    GridView galleryView;

    private PhoneListGalleryAdapter phoneListGalleryAdapter;

    private SimpleDateFormat formatParser;
    private int currentScrollState;
    HashMap<String, String> pictureMap;

    @BindView(R.id.cloud_empty_textview)
    TextView emptyView;

    private ArrayList<HashMap<String, String>> imageInfoList;
    private final String TAG = com.doctorkeeper.smartfi.view.phonelist.PhoneListGalleryFragment.class.getSimpleName();

    public static com.doctorkeeper.smartfi.view.phonelist.PhoneListGalleryFragment newInstance() {
        com.doctorkeeper.smartfi.view.phonelist.PhoneListGalleryFragment f = new com.doctorkeeper.smartfi.view.phonelist.PhoneListGalleryFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cloud_gallery_frag, container, false);
        ButterKnife.bind(this, view);

        phoneListGalleryAdapter = new PhoneListGalleryAdapter(getActivity());
        getImagesList();
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
            if (phoneListGalleryAdapter==null)
                return;

                phoneListGalleryAdapter.setItems(imageInfoList);
            galleryView.setAdapter(phoneListGalleryAdapter);
            }
        });
        galleryView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        pictureMap = new HashMap<>();
        pictureMap = phoneListGalleryAdapter.getItem(position);

//        if(pictureMap.get("cameraKind") == "Video"){
//            Toast.makeText(getActivity(), "비디오파일은 미리보기가 제공되지 않습니다!", Toast.LENGTH_SHORT).show();
//        } else{
        String imageUrl = pictureMap.get("fullPath");
        String imageGuid = "";
        Log.v(TAG,"imageUrl"+imageUrl);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.cloud_detail_container, PhoneListPictureFragment.newInstance(phoneListGalleryAdapter.getItemHandle(position), imageUrl, imageGuid), null);
        ft.addToBackStack(null);
        ft.commit();
//        }

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
                    PhoneListGalleryAdapter.ViewHolder holder = (PhoneListGalleryAdapter.ViewHolder) child.getTag();
                    if (!holder.done) {
                        holder.done = true;
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
        File directory = new File(BlabAPI.getActivity().getExternalFilesDir(Environment.getExternalStorageState())+ File.separator);
        File[] files = directory.listFiles();
        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
//        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
//            Log.d("Files", "FileName:" + files[i].getName());
            String fileName = files[i].getName();
            if(fileName.indexOf("jpg")>0){
                HashMap<String, String> imageInfo = new HashMap<>();
                imageInfo.put("fullPath", files[i].getAbsolutePath());
                imageInfo.put("uploadDate", files[i].lastModified()+"");
                imageInfoList.add(0,imageInfo);
            }
        }
//        Log.i("List in CloudFragment",imageInfoList.size()+"");
    }


}
