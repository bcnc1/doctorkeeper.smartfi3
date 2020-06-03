package com.thinoo.drcamlink.view.cloud;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thinoo.drcamlink.R;
import com.thinoo.drcamlink.madamfive.MadamfiveAPI;
import com.thinoo.drcamlink.view.AspectRatioImageView;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

public class CloudGalleryAdapter extends BaseAdapter {

    private static final String TAG = "CloudGalleryAdapter";

    public static class ViewHolder {
        HashMap<String,String> photo;
        AspectRatioImageView image1;
//        ImageView image1;
        TextView filename;
        TextView date;
        public TextView dslr;
        boolean done;

        ImageView thumbView;
        ProgressBar progressBar;
    }

//    private List<PhotoModel> items;
    private List<HashMap<String,String>> items;
    private final LayoutInflater inflater;
    String accessToken;
    private Context mContext;
    private boolean reversed;
    private int handles[] = new int[0];

    public CloudGalleryAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext=context;
    }

    public void setItems(List<HashMap<String,String>> items) {
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
    public HashMap<String,String> getItem(int position) {
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

        if (holder.photo.get("cameraKind").equals("DSLR")) {                // 1 = DSLR
            holder.dslr.setVisibility(View.VISIBLE);
        }else if(holder.photo.get("cameraKind").equals("Video")){
            holder.dslr.setVisibility(View.VISIBLE);
            holder.dslr.setText("Video");
        }else {
            holder.dslr.setVisibility(View.INVISIBLE);
        }

        holder.progressBar.setVisibility(View.INVISIBLE);
        accessToken = MadamfiveAPI.getAccessToken();

        String imageURL = "http://api.doctorkeeper.com:7818/v1/posts/"+holder.photo.get("url")+
                "/attachments/"+holder.photo.get("guid")+"?size=small&accessToken="+ URLEncoder.encode(accessToken);
//        Log.w(TAG,"imageURL = "+imageURL);
        Picasso.get().load(imageURL).resize(120,120).centerCrop().into(holder.image1);
        holder.image1.setExpectedDimensions(120, 120);

        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String createdDate = df.format(Long.parseLong(holder.photo.get("uploadDate")));
        holder.date.setText(createdDate);
        holder.done = false;

        return view;
    }

}
