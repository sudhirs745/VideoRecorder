package com.erlei.videorecorder.categoryHashtag.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.erlei.videorecorder.FilterInterface;
import com.erlei.videorecorder.FilterModel;
import com.erlei.videorecorder.R;
import com.erlei.videorecorder.categoryHashtag.Model.HashTagModel;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;


public class HashTagAdapter extends RecyclerView.Adapter<HashTagAdapter.ViewHolder> {

    private static final String TAG = "CategoryAdapter";

    FilterInterface filterInterface ;
    //vars
    private ArrayList<HashTagModel> hashTagModelArrayList = new ArrayList<>();
    private Context mContext;

    public HashTagAdapter(ArrayList<HashTagModel> hashTagModelArrayList, Context mContext , FilterInterface filterInterface ){
        this.hashTagModelArrayList=hashTagModelArrayList;
        this.mContext = mContext;
        this.filterInterface= filterInterface;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hash_tag_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d("Gett", "onBindViewHolder: called.");
        final HashTagModel hashTagModel=hashTagModelArrayList.get(position);

        holder.name.setText(hashTagModel.getName());

    }

    @Override
    public int getItemCount() {
        return hashTagModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);

        }
    }
}