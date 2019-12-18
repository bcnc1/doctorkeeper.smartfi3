package com.thinoo.drcamlink2.services;

import android.content.Context;

import com.thinoo.drcamlink2.models.PhotoModel;

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


}
