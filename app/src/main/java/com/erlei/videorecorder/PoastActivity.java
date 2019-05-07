package com.erlei.videorecorder;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.erlei.videorecorder.network.ApiService;
import com.erlei.videorecorder.network.RetrofitInstance;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PoastActivity extends AppCompatActivity {

    ProgressDialog dialog ;

    String  image_or_video_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poast);

        image_or_video_path=getIntent().getStringExtra("file_path");
        Log.e("video_come",image_or_video_path +" ");

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

}
