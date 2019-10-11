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


import android.os.Environment;
import android.util.Log;

import com.orm.query.Select;
import com.thinoo.drcamlink2.models.PhotoModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.thinoo.drcamlink2.madamfive.MadamfiveAPI.getActivity;


public class PhotoModelService {

    public static List<PhotoModel> findAll() {
        return (ArrayList<PhotoModel>) Select.from(PhotoModel.class)
                .orderBy("created desc").list();
    }

    public static ArrayList<PhotoModel> findImageListOld() {
        return (ArrayList<PhotoModel>) Select.from(PhotoModel.class)
                .orderBy("created asc")
                .list();
    }

    public static void deleteImageDBDate(Long id){
        PhotoModel photoModel = PhotoModel.findById(PhotoModel.class, id);
        photoModel.delete();
    }

    public static PhotoModel savePhoto(byte[] bytes, String filename, final int mode) {

//        String folder = Environment.getExternalStorageDirectory() + "/drcam/";
//        File file = new File(folder);

        File file = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState()), "/drcam/");

        if (!file.isDirectory()) {
            file.mkdir();
        }

        FileOutputStream outStream = null;

        try {
            // Write to SD Card
//            outStream = new FileOutputStream(folder + filename);
            outStream = new FileOutputStream(file.getAbsolutePath()+filename);
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
        photoModel.setFullpath(file.getAbsolutePath() + filename);
        photoModel.setFilname(filename);
        photoModel.setUploaded(true);
        photoModel.setMode(mode); // CAMERA
        photoModel.setTargetId("");
//        photoModel.setThumb(bytes);
//        photoModel.setBitmap(bytes);
        photoModel.setTargetName("");
        photoModel.setCreated(new Date());
        photoModel.save();

        return photoModel;
    }


}