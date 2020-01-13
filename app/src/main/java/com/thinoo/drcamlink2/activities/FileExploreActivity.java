package com.thinoo.drcamlink2.activities;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.thinoo.drcamlink2.R;
import com.thinoo.drcamlink2.models.PhotoModel;
import com.thinoo.drcamlink2.services.PhotoModelService;
import com.thinoo.drcamlink2.services.RetryUploadIntentService;
import com.thinoo.drcamlink2.view.phone_camera.PhoneCameraFragment;
import com.thinoo.drcamlink2.view.sdcard.GalleryFragment;

import java.io.File;
import java.util.ArrayList;

public class FileExploreActivity extends AppCompatActivity {

    private static final String TAG = "FileExploreActivity";

    //private String mRoot;
    private Context mCon;
    private File mRoot;
    private ListView mFileList;
    private ArrayList<String > files;
    private ArrayList<Long > mUploadList = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private Button mBtnUpload, mBtnSelectAll;
    private int numberOfSendPhoto;
    private ProgressBar multi_image_uploading_progressbar_ex;
    private FrameLayout mframelayout;

    private Handler msgHandler = new Handler(new Handler.Callback() {
        int uploadCount = 0;
        @Override
        public boolean handleMessage(Message msg) {
            Object path = msg.obj;
            Log.w(TAG,"msgHandler 호출..");
            if(GalleryFragment.getInstance() != null){
                uploadCount++;
                numberOfSendPhoto++;
                multi_image_uploading_progressbar_ex.setProgress(uploadCount);

//                if(numberOfSendPhoto==selectedObjectHandles.size()){  //마지막 값이면..
//                    multi_image_uploading_progressbar_ex.setVisibility(View.INVISIBLE);
//                    //Log.e(TAG,"완료.. => progressBarPortionSum = "+progressBarPortionSum + "  numberOfSendPhoto = "+numberOfSendPhoto);
//
////                    FragmentTransaction ft = getFragmentManager().beginTransaction();
////                    ft.replace(R.id.fragment_container, PhoneCameraFragment.newInstance(), null);
////                    ft.addToBackStack(null);
////                    ft.commit();
//                    // TODO: 2020-01-13 완료되면 맨 처음 화면으로??? 또는 종료
//                }

            }else{
                Log.w(TAG,"back key 누른 경우..");
            }
            // TODO: 2020-01-04 프로그래스브 바 처리 , 메모리 릭 유의, tost메세지 유무확인 필요!!
            //Toast.makeText(getActivity().getBaseContext(), path.toString(), Toast.LENGTH_LONG).show();
            return true;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mCon = this;

        mRoot = mCon.getExternalFilesDir(Environment.getExternalStorageState());
        Log.w(TAG,"mFile = "+mRoot.toString());

        initUI();



        boolean result = getDirFiles(mRoot);

        if(result == false){
            Log.e(TAG,"업로드 실패 파일 없음");
            return;
        }

    }

    private void initUI() {

        setContentView(R.layout.activity_file_explore);

        mBtnUpload = findViewById(R.id.btn_upload);
        mBtnSelectAll = findViewById(R.id.btn_selectAll);
        multi_image_uploading_progressbar_ex = (ProgressBar)findViewById(R.id.multi_image_uploading_progressbar);

        mFileList = findViewById(R.id.filelist);
        mframelayout = findViewById(R.id.bg_progress);


        files = new ArrayList<>();
        listAdapter =  new ArrayAdapter<String>(mCon, android.R.layout.simple_list_item_multiple_choice, files);


        mFileList.setAdapter(listAdapter);


        mFileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.w(TAG, position + " : " + files.get(position).toString());
                Log.w(TAG, id + " : " + id);
                CheckedTextView checkedTextView = view.findViewById(android.R.id.text1);
                boolean checked = checkedTextView.isChecked();
                Log.w(TAG,"checked = "+checked);

            }
        });


        mBtnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = mFileList.getCount();
                Log.w(TAG,"count = "+count);

                SparseBooleanArray checkedItems = mFileList.getCheckedItemPositions();

                Log.w(TAG," 값은 =  "+checkedItems);


                for(int i = 0; i< count; i++){
                    if(checkedItems.get(i)){
                        Log.w(TAG, "파일 = "+ files.get(i));
                        Long id = PhotoModelService.getPhotoModelIdByName(files.get(i));
                        mUploadList.add(id);
                    }
                }

                if(mUploadList.size() > 0){
                    startUpload();

                }
                else{
                    Toast.makeText(mCon,"선택된 파일이 없습니다.",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void startUpload() {
        Messenger messenger = new Messenger(msgHandler);

        RetryUploadIntentService.startRetryUpload(mCon, mUploadList.get(0),messenger);
        mframelayout.setVisibility(View.VISIBLE);

    }

    private boolean getDirFiles(File root) {
        String[] fileList = root.list();

        if(fileList == null){
            Toast.makeText(mCon, "항목이 없습니다.", Toast.LENGTH_SHORT).show();
            return false;
        }else{

            for(int i =0; i<fileList.length; i++){
                files.add(fileList[i]);
            }
        }
        listAdapter.notifyDataSetChanged();
        return true;
    }
}
