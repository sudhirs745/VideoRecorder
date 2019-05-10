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
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;


public class HashTagAdapter extends RecyclerView.Adapter<HashTagAdapter.ViewHolder> {

    private static final String TAG = "CategoryAdapter";

    FilterInterface filterInterface ;
    //vars
    private ArrayList<FilterModel> filterModelArrayList = new ArrayList<>();
    private Context mContext;

    public HashTagAdapter(ArrayList<FilterModel> filterModelArrayList, Context mContext , FilterInterface filterInterface ){
        this.filterModelArrayList=filterModelArrayList;
        this.mContext = mContext;
        this.filterInterface= filterInterface;
    }




    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d("Gett", "onBindViewHolder: called.");
        final FilterModel filterModel=filterModelArrayList.get(position);


        holder.name.setText(filterModel.getFilterName());
        holder.image.setImageResource(R.mipmap.header_icon_1);
//        holder.image.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                filterInterface.onItemClick(filterModel.getFilterKey());
////            }
////        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterInterface.onItemClick(filterModel.getFilterKey(),filterModel.getFilterType());
            }
        });

//        holder.image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return filterModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircularImageView image;
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_view);
            name = itemView.findViewById(R.id.name);

        }
    }
}