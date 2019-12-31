/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thinoo.drcamlink2.services;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.orm.query.Select;
import com.thinoo.drcamlink2.models.PhotoModel;
import com.thinoo.drcamlink2.view.phone_camera.PhoneCameraFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.getActivity;


public class PhotoModelService {

    private final static String TAG = PhotoModelService.class.getSimpleName();

    public static List<PhotoModel> findAll() {
        return (ArrayList<PhotoModel>) Select.from(PhotoModel.class)
                .orderBy("created desc").list();
    }

    public static ArrayList<PhotoModel> findImageListOld() {
        return (ArrayList<PhotoModel>) Select.from(PhotoModel.class)
                .orderBy("created asc")
                .list();
    }

    public static void deletePhotoModel(Long id){
        PhotoModel photoModel = PhotoModel.findById(PhotoModel.class, id);
        photoModel.delete();
    }

    public static PhotoModel getPhotoModel(Long id){
        PhotoModel photoModel = PhotoModel.findById(PhotoModel.class, id);
        long id1 = photoModel.getId();
        return photoModel;
    }


//    public static PhotoModel saveThumb(Bitmap bitmap, String filename, final int mode) {
//        File file = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState()), "/drcam/");
//
//        if (!file.isDirectory()) {
//            file.mkdir();
//        }
//
//        FileOutputStream outStream = null;
//
//        try {
//
//            Log.d(TAG, "파일저장 패스 = "+file.getAbsolutePath()+filename);
//            outStream = new FileOutputStream(file.getAbsolutePath()+filename); //파일저장
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
//            outStream.close();
//        } catch (FileNotFoundException e) { // <10>
//            Log.e(TAG,e.toString());
//        } catch (IOException e) {
//            Log.e(TAG,e.toString());
//        }
//
//
//        final PhotoModel photoModel = new PhotoModel();
//        photoModel.setThumbpath(file.getAbsolutePath()+filename);
//
//
//    }

    public static void makeDir(Context con, final String subDir){
        File file = new File(con.getExternalFilesDir(Environment.getExternalStorageState()), subDir);

        if (!file.isDirectory()) {
            file.mkdir();
        }
    }

    //mode: 카메라(0), dslr(1), 비디오(2)
    public static PhotoModel addPhotoModel( final String sourcePath, final String thumbPath, String filename, final int mode) {

        final PhotoModel photoModel = new PhotoModel();

        photoModel.setFullpath(sourcePath);
        photoModel.setThumbpath(thumbPath);
        photoModel.setFilename(filename);
        photoModel.setMode(mode);
        photoModel.setCreated(new Date());
        photoModel.save();

        return photoModel;
    }

    public static PhotoModel savePhoto(byte[] bytes, String filename, final int mode) {


        File file = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState()), "/drcam/");

        if (!file.isDirectory()) {
            file.mkdir();
        }

        FileOutputStream outStream = null;

        try {
            // Write to SD Card
//            outStream = new FileOutputStream(folder + filename);
            Log.d(TAG, "파일저장 패스 = "+file.getAbsolutePath()+filename);
            outStream = new FileOutputStream(file.getAbsolutePath()+filename); //파일저장
            outStream.write(bytes);
            outStream.close();
        } catch (FileNotFoundException e) { // <10>
            Log.i("PhotoModelService",e.toString());
        } catch (IOException e) {
            Log.i("PhotoModelService",e.toString());
        } finally {
        }

//        if(mode == 1) {
//
//            Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), 120, 120);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//
////        String thumbfilename = "";
////        int dot = filename.indexOf('.');
////        thumbfilename = filename.substring(0,dot);
////        thumbfilename = thumbfilename + "_thumb.JPG";
//
//            try {
//                // Write to SD Card
//
//                outStream = new FileOutputStream(folder + filename + "_thumb");
//                outStream.write(baos.toByteArray());
//                outStream.close();
//            } catch (FileNotFoundException e) { // <10>
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//            }
//        }

        final PhotoModel photoModel = new PhotoModel();
//        photoModel.setFullpath(folder + filename);
        Log.d(TAG, "path = "+file.getAbsolutePath() + filename);

        photoModel.setFullpath(file.getAbsolutePath() + filename);
        photoModel.setFilename(filename);
        photoModel.setUploaded(true);
        photoModel.setMode(mode); // CAMERA
 //       photoModel.setTargetId("");
//        photoModel.setThumb(bytes);
//        photoModel.setBitmap(bytes);
 //       photoModel.setTargetName("");
        photoModel.setCreated(new Date());
        photoModel.save();




        return photoModel;
    }


//    public static PhotoModel saveVideo(byte[] bytes, String filename, final int mode) {
//        return photoModel;
//    }
}
