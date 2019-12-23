package com.thinoo.drcamlink2.services;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import com.thinoo.drcamlink2.models.PhotoModel;

import static com.thinoo.drcamlink2.util.Constants.Invoke.UPLOAD_FILE_KIND;
import static com.thinoo.drcamlink2.util.Constants.Invoke.UPLOAD_FILE_NAME;
import static com.thinoo.drcamlink2.util.Constants.Invoke.UPLOAD_FILE_PATH;
import static com.thinoo.drcamlink2.util.Constants.Invoke.UPLOAD_FILE_TYPE;
import static com.thinoo.drcamlink2.util.Constants.Invoke.UPLOAD_MESSAGE_CALLBACK;

public class UploadManager {
    private static final String TAG = "UploadManager";

    private String mFilePath;
    private String mFileName;
    private String mThumbPath;
    private String mKind;
    private Context mCon;
    private PhotoModel mPhotoModel;
    private Handler handler;
    private Long mUploadId;
    private Integer mUploadingStep = 0;  //0: 썸네일 2: 원본, 3: 체


    /*
     * The service will call the handler to send back information.
     */
//    private Handler handler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            Object path = msg.obj;
//            //Toast.makeText(getActivity().getBaseContext(), path.toString(), Toast.LENGTH_LONG).show();
//            return true;
//        }
//    });

    public UploadManager(Context context, String filePath,
                         String thumbPath, String fileName, String kind) {
        mCon = context;
        mFilePath = filePath;
        mFileName = fileName;
        mThumbPath = thumbPath;
        mKind = kind;

    }

    public UploadManager(Context context, PhotoModel photoModel) {
        mCon = context;
        mPhotoModel = photoModel;

       // mPhotoModel.getId();

    }

    //썸네일 업로드 - 원본업로드 -> 블록체인

    public void start(){
        //시작과 끝을 관리
        //mUploadId = mPhotoModel.getId();

        switch (mUploadingStep){
            case 0:
                if(uploadThumbImage(mPhotoModel) == true){
                    mUploadingStep ++ ;
                    uploadMediaObj(mPhotoModel);
                }
                break;

            case 1:
                if(uploadMediaObj(mPhotoModel) == true) {
                    mUploadingStep ++ ;
                    uploadChain();
                }
                break;

            case 2:
                uploadChain();
                break;
        }


//        switch (mPhotoModel.getThumbUploading()){
//            case 0:
//                uploadThumbImage();
//                break;
//
//            case 1:
//                // TODO: 2019-12-23 원본이미지업로드
//                uploadMediaObj();
//                break;
//
//            case 2:
//                uploadThumbImage();
//                break;
//        }


//        if(mPhotoModel.getThumbUploading() == 0){
//            uploadThumbImage();
//        }else if(mPhotoModel.getThumbUploading() == 1){
//
//        }

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                Object getMsg = msg.obj;

                if(getMsg.toString() == "file-upload success"){
                    Log.d(TAG, "mUploadingStep = "+mUploadingStep);
                    mUploadingStep ++ ;
                }else if(getMsg.toString() == "file-upload fail"){

                }

                return false;
            }
        });
    }

    private void uploadChain() {
    }

    private Boolean uploadMediaObj(PhotoModel model) {

        if(model.getUploading() == 0){
            //서비스로 구현
            Intent it = new Intent(mCon, UploadService.class);
            it.putExtra(UPLOAD_FILE_PATH, model.getFullpath());
            it.putExtra(UPLOAD_FILE_KIND, model.getMode());  //interger
            it.putExtra(UPLOAD_FILE_NAME, model.getFilename());
            it.putExtra(UPLOAD_FILE_TYPE, "media");
            model.setUploading(1); //업로드 시작

            Messenger messenger = new Messenger(handler);
            it.putExtra(UPLOAD_MESSAGE_CALLBACK, messenger);

            mCon.startService(it);
            return false;
        }else if(model.getUploading() == 1){
            Log.d(TAG,"업로드중..");
            return false;
        }else if(model.getUploading() == 2){
            Log.d(TAG,"업로드 완료..");
            return true;

        } else{
            Log.d(TAG,"업로드 실패..");
            //서비스로 구현
            Intent it = new Intent(mCon, UploadService.class);
            it.putExtra(UPLOAD_FILE_PATH, model.getFullpath());
            it.putExtra(UPLOAD_FILE_KIND, model.getMode());  //interger
            it.putExtra(UPLOAD_FILE_NAME, model.getFilename());
            it.putExtra(UPLOAD_FILE_TYPE, "media");
            model.setUploading(1); //업로드 시작

            Messenger messenger = new Messenger(handler);
            it.putExtra(UPLOAD_MESSAGE_CALLBACK, messenger);

            mCon.startService(it);
            return false;
        }
    }


    private Boolean uploadThumbImage(PhotoModel model){


        return false;

    }
}
