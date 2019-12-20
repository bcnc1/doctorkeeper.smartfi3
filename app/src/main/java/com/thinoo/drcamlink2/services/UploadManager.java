package com.thinoo.drcamlink2.services;

import android.content.Context;
import android.content.Intent;

import com.thinoo.drcamlink2.models.PhotoModel;

import static com.thinoo.drcamlink2.util.Constants.Invoke.UPLOAD_FILE_KIND;
import static com.thinoo.drcamlink2.util.Constants.Invoke.UPLOAD_FILE_NAME;
import static com.thinoo.drcamlink2.util.Constants.Invoke.UPLOAD_FILE_PATH;

public class UploadManager {
    private String mFilePath;
    private String mFileName;
    private String mThumbPath;
    private String mKind;
    private Context mCon;
    private PhotoModel mPhotoModel;

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

    //썸네일 업로드 -> 원본업로드 -> 블록체인

    public void start(){
        //시작과 끝을 관리
    }

    private void uploadThumbImage(){
        //서비스로 구현
        Intent it = new Intent(mCon, UploadService.class);
        it.putExtra(UPLOAD_FILE_PATH, mPhotoModel.getThumbpath());
        it.putExtra(UPLOAD_FILE_KIND, mPhotoModel.getMode());  //interger
        it.putExtra(UPLOAD_FILE_NAME, mPhotoModel.getFilename());

        
    }
}
