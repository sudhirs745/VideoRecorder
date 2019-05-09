package com.erlei.videorecorder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.erlei.videorecorder.categoryHashtag.Adapter.FilterRecyclerAdapter;
import com.erlei.videorecorder.categoryHashtag.Adapter.FilterValRecyclerAdapter;
import com.erlei.videorecorder.categoryHashtag.Model.FilterDefaultMultipleListModel;
import com.erlei.videorecorder.categoryHashtag.Model.MainFilterModel;
import com.erlei.videorecorder.network.ApiService;
import com.erlei.videorecorder.network.RetrofitInstance;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PoastActivity extends AppCompatActivity {

    ProgressDialog dialog ;
    String  image_or_video_path;

    RecyclerView filterListView = null;
    private RecyclerView filterValListView;
    private FilterRecyclerAdapter adapter;
    private FilterValRecyclerAdapter filterValAdapter;
    private ArrayList<String> sizes = new ArrayList<>();
    private ArrayList<String> styles = new ArrayList<>();
    private ArrayList<String> colors = new ArrayList<>();
    private ArrayList<FilterDefaultMultipleListModel> sizeMultipleListModels = new ArrayList<>();
    private ArrayList<FilterDefaultMultipleListModel> styleMultipleListModels = new ArrayList<>();
    private ArrayList<FilterDefaultMultipleListModel> colorMultipleListModels = new ArrayList<>();
    private ArrayList<MainFilterModel> filterModels = new ArrayList<>();
    private List<String> rootFilters;

    private ArrayList<String> sizeSelected = new ArrayList<String>();
    private ArrayList<String> colorSelected = new ArrayList<String>();
    private ArrayList<String> styleSelected = new ArrayList<String>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poast);

        image_or_video_path=getIntent().getStringExtra("file_path");
        Log.e("video_come",image_or_video_path +" ");

        ImageView imageView =findViewById(R.id.thum_image);

        try {
            Bitmap imagebitmap = ConstantClass.retriveVideoFrameFromVideo(image_or_video_path);
            imageView.setImageBitmap(imagebitmap);
        }catch (Throwable e){
            Log.e("error", e.toString());
        }

        Button  bt_post = findViewById(R.id.bt_post);
       bt_post.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               uploadFile(new File(image_or_video_path),
                      "1" ,
                       "dfg",
                       "hfjh",
                       "1",
                       "11,2",
                       "1.54",
                       "1",
                       "1",
                       "1",
                       "1",
                       "1",
                       "1",
                       "1",
                       "1",
                       "1"
                       );

           }
       });

//        rootFilters = Arrays.asList(this.getResources().getStringArray(R.array.filter_category));
//        for (int i = 0; i < rootFilters.size(); i++) {
//
//            /* Create new MainFilterModel object and set array value to @model
//             * Description:
//             * -- Class: MainFilterModel.java
//             * -- Package:main.shop.javaxerp.com.shoppingapp.model
//             * */
//            MainFilterModel model = new MainFilterModel();
//            /*Title for list item*/
//            model.setTitle(rootFilters.get(i));
//            /*Subtitle for list item*/
//            model.setSub("All");
//            /*Example:
//             * --------------------------------------------
//             * Brand => title
//             * All => subtitle
//             * --------------------------------------------
//             * Color => title
//             * All => subtitle
//             * --------------------------------------------
//             * */
//
//            /*add MainFilterModel object @model to ArrayList*/
//            filterModels.add(model);
//        }
//
//        filterListView =  findViewById(R.id.filter_dialog_listview);
//        adapter = new FilterRecyclerAdapter(this, R.layout.filter_list_item_layout, filterModels);
//        filterListView.setAdapter(adapter);
//        filterListView.setLayoutManager(new LinearLayoutManager(this));
//        filterListView.setHasFixedSize(true);
//
//        filterValListView =  findViewById(R.id.filter_value_listview);
//        filterValAdapter = new FilterValRecyclerAdapter(this,R.layout.filter_list_val_item_layout, sizeMultipleListModels, MainFilterModel.SIZE);
//        filterValListView.setAdapter(filterValAdapter);
//        filterValListView.setLayoutManager(new LinearLayoutManager(this));
//        filterValListView.setHasFixedSize(true);
//
////        btnFilter = (Button) findViewById(R.id.btn_filter);
////        btnFilter.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                for (FilterDefaultMultipleListModel model : sizeMultipleListModels) {
////                    if (model.isChecked()) {
////                        filterModels.get(MainFilterModel.INDEX_SIZE).getSubtitles().add(model.getName());
////                    }
////                }
////
////                for (FilterDefaultMultipleListModel model : colorMultipleListModels) {
////                    if (model.isChecked()) {
////                        filterModels.get(MainFilterModel.INDEX_COLOR).getSubtitles().add(model.getName());
////                    }
////                }
////
////                for (FilterDefaultMultipleListModel model : styleMultipleListModels) {
////                    if (model.isChecked()) {
////                        filterModels.get(MainFilterModel.INDEX_STYLE).getSubtitles().add(model.getName());
////                    }
////
////                }
////                /*Get value from checked of size checkbox*/
////                sizeSelected = filterModels.get(MainFilterModel.INDEX_SIZE).getSubtitles();
////                filterModels.get(MainFilterModel.INDEX_SIZE).setSubtitles(new ArrayList<String>());
////
////                /*Get value from checked of color checkbox*/
////                colorSelected = filterModels.get(MainFilterModel.INDEX_COLOR).getSubtitles();
////                filterModels.get(MainFilterModel.INDEX_COLOR).setSubtitles(new ArrayList<String>());
////
////                /*Get value from checked of price checkbox*/
////                styleSelected = filterModels.get(MainFilterModel.INDEX_STYLE).getSubtitles();
////                filterModels.get(MainFilterModel.INDEX_STYLE).setSubtitles(new ArrayList<String>());
////
////                if(sizeSelected.isEmpty() && colorSelected.isEmpty() && styleSelected.isEmpty()){
////                    Toast.makeText(PoastActivity.this,"Please select size,color,brand", Toast.LENGTH_SHORT).show();
////                }
////
////                if(!sizeSelected.isEmpty() || !colorSelected.isEmpty() || !styleSelected.isEmpty()){
////                    Toast.makeText(PoastActivity.this,"Selected Size is "+sizeSelected.toString()+"\n"+"Selected Color is "+colorSelected.toString() +"\n"+"Selected Brand is "+styleSelected.toString(),Toast.LENGTH_LONG).show();
////                }
////            }
////        });
//
////        btnClear = (Button) findViewById(R.id.btn_clear);
////        /*TODO: Clear user selected */
////        btnClear.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////
////
////                for (FilterDefaultMultipleListModel selectedModel : sizeMultipleListModels) {
////                    selectedModel.setChecked(false);
////
////                }
////
////                for (FilterDefaultMultipleListModel selectedModel : styleMultipleListModels) {
////                    selectedModel.setChecked(false);
////
////                }
////
////                for (FilterDefaultMultipleListModel selectedModel : colorMultipleListModels) {
////                    selectedModel.setChecked(false);
////
////                }
////                adapter.notifyDataSetChanged();
////                filterValAdapter.notifyDataSetChanged();
////            }
////        });
//
//
//        adapter.setOnItemClickListener(new FilterRecyclerAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View v, int position) {
//                filterItemListClicked(position, v);
//                adapter.setItemSelected(position);
//            }
//        });
//        filterItemListClicked(0, null);
//        adapter.setItemSelected(0);
//
//        sizes = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.filter_size)));
//        for (String size : sizes) {
//
//            /* Create new FilterDefaultMultipleListModel object for brand and set array value to brand model {@model}
//             * Description:
//             * -- Class: FilterDefaultMultipleListModel.java
//             * -- Package:main.shop.javaxerp.com.shoppingapp.model
//             * NOTE: #checked value @FilterDefaultMultipleListModel is false;
//             * */
//            FilterDefaultMultipleListModel model = new FilterDefaultMultipleListModel();
//            model.setName(size);
//
//            /*add brand model @model to ArrayList*/
//            sizeMultipleListModels.add(model);
//        }
//        styles = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.filter_brand)));
//        for (String style : styles) {
//
//            /* Create new FilterDefaultMultipleListModel object for brand and set array value to brand model {@model}
//             * Description:
//             * -- Class: FilterDefaultMultipleListModel.java
//             * -- Package:main.shop.javaxerp.com.shoppingapp.model
//             * NOTE: #checked value @FilterDefaultMultipleListModel is false;
//             * */
//            FilterDefaultMultipleListModel model = new FilterDefaultMultipleListModel();
//            model.setName(style);
//
//            /*add brand model @model to ArrayList*/
//            styleMultipleListModels.add(model);
//        }
//        colors = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.filter_color)));
//        for (String color : colors) {
//
//            /* Create new FilterDefaultMultipleListModel object for brand and set array value to brand model {@model}
//             * Description:
//             * -- Class: FilterDefaultMultipleListModel.java
//             * -- Package:main.shop.javaxerp.com.shoppingapp.model
//             * NOTE: #checked value @FilterDefaultMultipleListModel is false;
//             * */
//            FilterDefaultMultipleListModel model = new FilterDefaultMultipleListModel();
//            model.setName(color);
//
//            /*add brand model @model to ArrayList*/
//            colorMultipleListModels.add(model);
//        }


        Button hastagBt= findViewById(R.id.bt_hashtag);
        hastagBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity( new Intent(PoastActivity.this,PostCategory.class));
            }
        });


    }


    private void uploadFile(
                  File      file,
                  String    user_id1,
                  String    title1,
                  String    description1,
                  String    type1,
                  String    latitute1,
                  String    longitute1,
                  String    counry_id1,
                  String    state_id1,
                  String    city_id1,
                  String    view_status1,
                  String    can_likes1,
                  String    can_comment1,
                  String    category1,
                  String    can_shared1,
                  String    audio_id1 ) {

        RequestBody requestBody = RequestBody.create(MediaType.parse("video/mp4"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("video_clip", file.getName(), requestBody);


        RequestBody user_id = RequestBody.create(
                MediaType.parse("text/plain"),
                user_id1);
        RequestBody title = RequestBody.create(
                MediaType.parse("text/plain"),
                title1);
        RequestBody description = RequestBody.create(
                MediaType.parse("text/plain"),
                description1);

        RequestBody type = RequestBody.create(
                MediaType.parse("text/plain"),
                type1);

        RequestBody latitute = RequestBody.create(
                MediaType.parse("text/plain"),
                latitute1);
        RequestBody longitute = RequestBody.create(
                MediaType.parse("text/plain"),
                longitute1);
        RequestBody counry_id = RequestBody.create(
                MediaType.parse("text/plain"),
                counry_id1);
        RequestBody state_id = RequestBody.create(
                MediaType.parse("text/plain"),
                state_id1);

        RequestBody city_id = RequestBody.create(
                MediaType.parse("text/plain"),
                city_id1);
        RequestBody view_status = RequestBody.create(
                MediaType.parse("text/plain"),
                view_status1);
        RequestBody can_likes = RequestBody.create(
                MediaType.parse("text/plain"),
                can_likes1);
        RequestBody can_comment = RequestBody.create(
                MediaType.parse("text/plain"),
                can_comment1);

        RequestBody category = RequestBody.create(
                MediaType.parse("text/plain"),
                category1);
        RequestBody can_shared = RequestBody.create(
                MediaType.parse("text/plain"),
                can_shared1);
        RequestBody audio_id = RequestBody.create(
                MediaType.parse("text/plain"),
                audio_id1);


        dialog = new ProgressDialog(PoastActivity.this);
        dialog.setMessage("please wait...");
        dialog.show();

        ApiService getResponse = RetrofitInstance.getImageRetrofit().create(ApiService.class);
        Call<JsonObject> call = getResponse.Videoupload(
                fileToUpload,
                user_id,
                title,
                description,
                type,
                latitute,
                longitute,
                counry_id,
                state_id,
                city_id,
                view_status,
                can_likes,
                can_comment,
                category,
                can_shared,
                audio_id
                );

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.e("response", "Getting response from server : " + response.raw().toString());

                if (response.isSuccessful()) {
                    dialog.dismiss();
                    Log.d("response", "Getting response from server : " + response.body().toString());
                    String getResponse = response.body().toString();
                    try {
                        JSONObject jsonObject = new JSONObject(getResponse);
                        String message = jsonObject.getString("status");
                        String responseMessage = jsonObject.getString("response");

                        if (message.equals("1")) {
                            dialog.dismiss();

                            Toast.makeText(PoastActivity.this, responseMessage,Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(PoastActivity.this, responseMessage,Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            //  Toasty.error(CreateChannel.this, "Database Error", Toast.LENGTH_LONG, true).show();
                        }
                    } catch (JSONException e) {
                        Log.e("error maeeage ", e.toString());
                        e.printStackTrace();
                    }

                    Log.d("Gett", response.body().toString());
                    Log.d("Gett", response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                dialog.dismiss();
                  Log.e(" onFailure message .. " , t.toString()) ;

            }

        });
    }

//    private void filterItemListClicked(int position, View v) {
//        if (position == 0) {
//            filterValAdapter = new FilterValRecyclerAdapter(this, R.layout.filter_list_val_item_layout, sizeMultipleListModels, MainFilterModel.SIZE);
//        } else if (position == 1) {
//            filterValAdapter = new FilterValRecyclerAdapter(this, R.layout.filter_list_val_item_layout, colorMultipleListModels, MainFilterModel.COLOR);
//        } else {
//            filterValAdapter = new FilterValRecyclerAdapter(this, R.layout.filter_list_val_item_layout, styleMultipleListModels, MainFilterModel.STYLE);
//        }
//
//        filterValListView.setAdapter(filterValAdapter);
//
//        filterValAdapter.setOnItemClickListener(new FilterValRecyclerAdapter.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(View view, int position) {
//                filterValitemListClicked(position);
//            }
//        });
//        filterValAdapter.notifyDataSetChanged();
//    }
//
//    private void filterValitemListClicked(int position) {
//        filterValAdapter.setItemSelected(position);
//    }


}
