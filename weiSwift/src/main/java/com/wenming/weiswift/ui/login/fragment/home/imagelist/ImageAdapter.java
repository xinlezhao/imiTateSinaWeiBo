package com.wenming.weiswift.ui.login.fragment.home.imagelist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cesards.cropimageview.CropImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.wenming.weiswift.R;
import com.wenming.weiswift.ui.common.FillContent;

import java.util.ArrayList;

/**
 * Created by wenmingvs on 16/1/3.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private ArrayList<String> mData;
    private Context mContext;
    private DisplayImageOptions options;

    public ImageAdapter(ArrayList<String> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.message_image_default)
                .showImageForEmptyUri(R.drawable.message_image_default)
                .showImageOnFail(R.drawable.message_image_default)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.mainfragment_weiboitem_imageitem, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        FillContent.fillImageList(mContext, mData, position, holder.imageItem, holder.imageType);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void setData(ArrayList<String> data) {
        this.mData = data;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CropImageView imageItem;
        public ImageView imageType;

        public ViewHolder(View itemView) {
            super(itemView);
            imageItem = (CropImageView) itemView.findViewById(R.id.imageItem);
            imageType = (ImageView) itemView.findViewById(R.id.imageType);

        }
    }


}
