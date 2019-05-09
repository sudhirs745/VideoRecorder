package com.erlei.videorecorder.categoryHashtag.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.erlei.videorecorder.R;
import com.erlei.videorecorder.categoryHashtag.Model.CategoryModelPost;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CategoryPostadapter extends RecyclerView.Adapter<CategoryPostadapter.ViewHolder> {
    private ArrayList<CategoryModelPost> categoryModelPostsList;
    private Context context;

    public CategoryPostadapter(Context context,ArrayList<CategoryModelPost> categoryModelPostsList) {
        this.categoryModelPostsList = categoryModelPostsList;
        this.context = context;
    }

    @Override
    public CategoryPostadapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.category_row_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryPostadapter.ViewHolder viewHolder, int i) {

        viewHolder.tv_cat_name.setText(categoryModelPostsList.get(i).getCategoryName());
//        Picasso.with(context)
//                .load(categoryModelPostsList.get(i).getCategoryUrl())
//                .placeholder(R.drawable.comment)
//               // .resize(100,100)
//                .into(viewHolder.img_cat);

        Glide.with(context)
                .load(categoryModelPostsList.get(i).getCategoryUrl())
                .into(viewHolder.img_cat);

        //Picasso.with(context).load(categoryModelPostsList.get(i).getCategoryUrl()).resize(240, 120).into(viewHolder.img_cat);
    }

    @Override
    public int getItemCount() {
        return categoryModelPostsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_cat_name;
        private ImageView img_cat;
        public ViewHolder(View view) {
            super(view);

            tv_cat_name = view.findViewById(R.id.tv_cat_name);
            img_cat =  view.findViewById(R.id.img_cat);
        }
    }

}

