package com.doctorkeeper.smartfi.view.phonelist;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.doctorkeeper.smartfi.R;
import com.doctorkeeper.smartfi.view.AspectRatioImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;


public class PhoneListGalleryAdapter extends BaseAdapter {

    private static final String TAG = "PhoneListGalleryAdapter";

    public static class ViewHolder {
        HashMap<String, String> photo;
        AspectRatioImageView image1;
//        ImageView image1;
        TextView filename;
        TextView date;
        public TextView dslr;
        boolean done;

        ImageView thumbView;
        ProgressBar progressBar;
    }

    private List<HashMap<String, String>> items;
    private final LayoutInflater inflater;
    String accessToken;
    private Context mContext;
    private boolean reversed;
    private int handles[] = new int[0];

    public PhoneListGalleryAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext=context;
    }

    public void setItems(List<HashMap<String, String>> items) {
        this.items = items;
//        Log.w(TAG,"갯수 = "+items);
        notifyDataSetChanged();
    }

    public void setReverseOrder(boolean reversed) {
        if (this.reversed == reversed) {
            return;
        }
        this.reversed = reversed;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (items==null) {
            return 0;
        }
        return items.size();
    }

    public void setHandles(int handles[]) {
        this.handles = handles;
        notifyDataSetChanged();
    }

    public int getItemHandle(int position) {
//        return handles[reversed ? handles.length - position - 1 : position];
        return position;
    }

    @Override
    public HashMap<String, String> getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = inflater.inflate(R.layout.dslr_item, parent, false);
            ViewHolder holder = new ViewHolder();
            view.setTag(holder);
            holder.image1 = (AspectRatioImageView) view.findViewById(R.id.image1);
            holder.filename = (TextView) view.findViewById(R.id.filename_field);
            holder.date = (TextView) view.findViewById(R.id.date_field);
            holder.progressBar = (ProgressBar) view.findViewById(R.id.thumb_uploading);
            holder.thumbView = (ImageView) view.findViewById(R.id.thumb_uploaded);
            holder.dslr = (TextView) view.findViewById(R.id.textview_dslr);
        }

        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.photo = getItem(position);
//        log.i(TAG,"holder.photo : " + holder.photo);
//        if (holder.photo.get("cameraKind").equals("DSLR")) {                // 1 = DSLR
//            holder.dslr.setVisibility(View.VISIBLE);
//        }else if(holder.photo.get("cameraKind").equals("Video")){
//            holder.dslr.setVisibility(View.VISIBLE);
//            holder.dslr.setText("Video");
//        }else {
//            holder.dslr.setVisibility(View.INVISIBLE);
//        }

        holder.progressBar.setVisibility(View.INVISIBLE);
//        accessToken = MadamfiveAPI.getAccessToken();

//        String imageURL = Constants.m5.BASE_URL+"/v1/posts/"+holder.photo.getrl")+
//                "/attachments/"+holder.photo.get("guid")+"?size=small&accessToken="+ URLEncoder.encode(accessToken);
//        Log.i(TAG,"imageURL in Cloud"+imageURL);
//        Picasso.get().load(imageURL).resize(120,120).centerCrop().into(holder.image1);
        Glide.with(mContext).load(holder.photo.get("fullPath")).centerCrop().into(holder.image1);
        holder.image1.setExpectedDimensions(120, 120);

        SimpleDateFormat df = new SimpleDateFormat("MM.dd HH:mm:ss");
        String createdDate = df.format(Long.parseLong(holder.photo.get("uploadDate")));
        holder.date.setText(createdDate);


        String fullPath = holder.photo.get("fullPath");
        String filename=fullPath.substring(fullPath.lastIndexOf("/")+1);
        String checkPath = mContext.getExternalFilesDir(null).getAbsolutePath() + "/uploadCheck/";
//        String checkPath = "/storage/emulated/0/Android/data/com.doctorkeeper.smartfi.tnh/files/uploadCheck/";
        String checkFullPath = checkPath + filename;
//        log.i(TAG,"파일이름 : " + checkFullPath);
        File files = new File(checkFullPath);
        if(files.exists()) {
            holder.thumbView.setVisibility(View.VISIBLE);
        } else {
            holder.thumbView.setVisibility(View.INVISIBLE);
//            SmartFiPreference.setSfUploadResult(BlabAPI.getActivity(),true);
//            log.i(TAG,"SmartFiPreference.getSfUploadResult(BlabAPI.getActivity()) : " + SmartFiPreference.getSfUploadResult(BlabAPI.getActivity()));
//            holder.filename.setText("failed");
        }

        holder.done = false;

        return view;
    }

}