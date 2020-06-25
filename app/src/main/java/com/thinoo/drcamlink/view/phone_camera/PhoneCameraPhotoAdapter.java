package com.thinoo.drcamlink.view.phone_camera;

import android.app.FragmentTransaction;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.thinoo.drcamlink.R;
import com.thinoo.drcamlink.madamfive.MadamfiveAPI;
import com.thinoo.drcamlink.models.PhotoModel;
import com.thinoo.drcamlink.services.PhotoModelService;

import java.io.File;
import java.util.List;

import static com.thinoo.drcamlink.madamfive.MadamfiveAPI.getActivity;


public class PhoneCameraPhotoAdapter extends RecyclerView.Adapter<PhoneCameraPhotoAdapter.MyViewHolder> {

    private List<PhotoModel> photoModelList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public ImageView thumbView;
        public ProgressBar progressBar;
        public TextView dslr;

        public MyViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.thumb_image);
            thumbView = (ImageView) view.findViewById(R.id.thumb_uploaded);
            progressBar = (ProgressBar) view.findViewById(R.id.thumb_uploading);
            dslr = (TextView) view.findViewById(R.id.textview_dslr);

//            view.setOnLongClickListener(new View.OnLongClickListener(){
//                @Override
//                public boolean onLongClick(View view) {
//                    int position = getAdapterPosition();
//                    final PhotoModel photo = photoModelList.get(position);
//                    String fileName = photo.getFilename();
//                    Long photoId = photo.getId();
//                    //Toast.makeText(view.getContext(),"name"+photo.getFilname()+"/id:"+photo.getId(),Toast.LENGTH_SHORT).show();
//                    deleteImage(fileName,photoId);
//                    return false;
//                }
//            });

            view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    final PhotoModel photo = photoModelList.get(position);
                    String fullPath = photo.getFullpath();

                    FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                    ft.replace(R.id.phone_picture_container, PhonePictureFragment.newInstance(position, fullPath, photo), null);
                    ft.addToBackStack(null);
                    ft.commit();

                }
            });
        }
    }


    public PhoneCameraPhotoAdapter(List<PhotoModel> photoModelList) {
        this.photoModelList = photoModelList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.phone_camera_thumb_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final PhotoModel photo = photoModelList.get(position);
        Log.i("XXX", "position:"+photo.getFilename()+","+position);

        File f = new File(photo.getFullpath());
        Glide.with(MadamfiveAPI.getActivity()).load(f).centerCrop().into(holder.imageView);

        if (photo.getMode()==1) {
            holder.dslr.setVisibility(View.VISIBLE);
        }
        else {
            holder.dslr.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        if(photoModelList.size()>20){
            return 20;
        }else {
            return photoModelList.size();
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    private void deleteImage(String fileName,Long photoId){

        File myDir = new File(Environment.getExternalStorageDirectory() + "/drcam");
        new File(myDir,fileName).delete();

        PhotoModel pm = PhotoModel.findById(PhotoModel.class,photoId);
        pm.delete();

        Toast t = Toast.makeText(getActivity(),"이미지가 삭제되었습니다",Toast.LENGTH_SHORT);
        t.setGravity(Gravity.TOP,0,200);
        t.show();

        photoModelList = PhotoModelService.findAll();
        this.notifyDataSetChanged();

    }


}
