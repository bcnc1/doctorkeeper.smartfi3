package com.thinoo.drcamlink2.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.TypedValue;
import android.webkit.MimeTypeMap;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DisplayUtil {

    public static final float dpToPx(Context c, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }

    public static boolean saveBitmap(Bitmap bitmap, String destination, boolean recyle){
        if(bitmap == null || TextUtils.isEmpty(destination)){
            return false;
        }

        BufferedOutputStream bos = null;
        try{
            FileOutputStream fos = new FileOutputStream(destination);
            bos = new BufferedOutputStream(fos);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false ;
        }finally {
            if (bos != null) {
                try {
                    bos.flush();
                    bos.close();
                } catch (IOException e) {
                }
            }
            if (recyle) {
                bitmap.recycle();
            }
        }
    }


//    public static final String getMimeType(String path) {
//
//        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
//
//        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
//    }

}
