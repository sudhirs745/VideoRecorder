package com.erlei.videorecorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.erlei.videorecorder.categoryHashtag.Adapter.CategoryPostadapter;
import com.erlei.videorecorder.categoryHashtag.Model.CategoryModelPost;

import java.util.ArrayList;

public class PostCategory extends AppCompatActivity {


    RecyclerView recyclerView ;

    private final String android_version_names[] = {
            "Donut",
            "Eclair",
            "Froyo",
            "Gingerbread",
            "Honeycomb",
            "Ice Cream Sandwich",
            "Jelly Bean",
            "KitKat",
            "Lollipop",
            "Marshmallow"
    };

    private final String android_image_urls[] = {
            "http://api.learn2crack.com/android/images/donut.png",
            "http://api.learn2crack.com/android/images/eclair.png",
            "http://api.learn2crack.com/android/images/froyo.png",
            "http://api.learn2crack.com/android/images/ginger.png",
            "http://api.learn2crack.com/android/images/honey.png",
            "http://api.learn2crack.com/android/images/icecream.png",
            "http://api.learn2crack.com/android/images/jellybean.png",
            "http://api.learn2crack.com/android/images/kitkat.png",
            "http://api.learn2crack.com/android/images/lollipop.png",
            "http://api.learn2crack.com/android/images/marshmallow.png"
    };

    private final String colorbg[] = {
            "#e06377",
            "#e3eaa7",
            "#b5e7a0",
            "#86af49",
            "#b2b2b2",
            "#f18973",
            "#bc5a45",
            "#c5d5c5",
            "#e3e0cc",
            "#d9ecd0"
    };

    private final String colorText[] = {
            "#FFFFFF",
            "#FFFFFF",
            "#FFFFFF",
            "#FFFFFF",
            "#FFFFFF",
            "#FFFFFF",
            "#FFFFFF",
            "#FFFFFF",
            "#FFFFFF",
            "#FFFFFF"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_category);

        recyclerView = findViewById(R.id.filter_dialog_listview) ;

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),3);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<CategoryModelPost> androidVersions = prepareData();
        CategoryPostadapter adapter = new CategoryPostadapter(getApplicationContext(),androidVersions);
        recyclerView.setAdapter(adapter);



    }

    private ArrayList<CategoryModelPost> prepareData(){

        ArrayList<CategoryModelPost> categoryModelPostArrayList = new ArrayList<>();
        for(int i=0;i<android_version_names.length;i++){
            CategoryModelPost  categoryModelPost = new CategoryModelPost(i+"",android_version_names[i],android_image_urls[i], colorbg[i],colorText[i]);

            categoryModelPostArrayList.add(categoryModelPost);
        }
        return categoryModelPostArrayList;
    }

}
