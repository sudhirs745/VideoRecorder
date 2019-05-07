package com.erlei.videorecorder.network;



import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import rx.Observable;

public interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("users/login")
    Call<JsonObject> getlogin1(@Body String body);

    @Headers("Content-Type: application/json")
    @POST("users/login")
    Call<JsonObject> getlogin(@Body JsonObject jsonObject);

    @Headers("Content-Type: application/json")
    @POST("appuser/login")
    Call<JsonObject> getlogin2(@Body JsonObject jsonObject);


    @Headers("Content-Type: application/json")
    @POST("users/signup")
    Call<JsonObject> getsignup(@Body JsonObject jsonObject);

    @Headers("Content-Type: application/json")
    @POST("appuser/signup")
    Call<JsonObject> getsignup2(@Body JsonObject jsonObject);

    @Headers("Content-Type: application/json")
    @POST("appuser/verification")
    Call<JsonObject> verification(@Body JsonObject jsonObject);


    @GET("appuser/sendotp")
    Call<JsonObject> SendOtp(@Query("userid") String usersId);


    @GET("appuser/skilllist")
    Call<JsonObject> SkillList(@Query("skillid") String skillId);


    @GET("category/getVideoCategory")
    Call<JsonObject> VideoCategory();




    @Headers("Content-Type: application/json")
    @POST("users/profile-details")
    Observable<JsonObject> getProfileDetails(@Body JsonObject body);


    @Headers("Content-Type: application/json")
    @POST("appuser/profile-details")
    Observable<JsonObject> getProfileDetails2(@Body JsonObject body);


    @Headers("Content-Type: application/json")
    @POST("appuser/emailupdate")
    Call<JsonObject> emailupdate(@Body JsonObject jsonObject);


    @Headers("Content-Type: application/json")
    @POST("appuser/userpassword")
    Call<JsonObject> userpassword(@Body JsonObject jsonObject);

    @Headers("Content-Type: application/json")
    @POST("appuser/profile-details")
    Call<JsonObject> profiledetails(@Body JsonObject jsonObject);


    @Headers("Content-Type: application/json")
    @POST("appuser/verify-email-update")
    Call<JsonObject> verifyemailupdate(@Body JsonObject jsonObject);


    @Headers("Content-Type: application/json")
    @POST("appuser/username-update")
    Call<JsonObject> verifyusernameupdate(@Body JsonObject jsonObject);


    @Multipart
    @POST("appuser/uploadprofileimage")
    Call<JsonObject> uploadImage(@Part MultipartBody.Part file,
                                 @Part("user_id") RequestBody users_id,
                                 @Part("name") RequestBody name,
                                 @Part("bio") RequestBody bio,
                                 @Part("skill") RequestBody skill


    );

    @Multipart
    @POST("video/videoupload")
    Call<JsonObject> Videoupload(
                                 @Part MultipartBody.Part file,
                                 @Part("user_id") RequestBody user_id,
                                 @Part("title") RequestBody title,
                                 @Part("description") RequestBody description,
                                 @Part("type") RequestBody type,
                                 @Part("latitute") RequestBody latitute,
                                 @Part("longitute") RequestBody longitute,
                                 @Part("counry_id") RequestBody counry_id,
                                 @Part("state_id") RequestBody state_id,
                                 @Part("city_id") RequestBody city_id,
                                 @Part("view_status") RequestBody view_status,
                                 @Part("can_likes") RequestBody can_likes,
                                 @Part("can_comment") RequestBody can_comment,
                                 @Part("category") RequestBody category,
                                 @Part("can_shared") RequestBody can_shared,
                                 @Part("audio_id") RequestBody audio_id
    );



    @Headers("Content-Type: application/json")
    @POST("location/getCoutry")
    Call<JsonObject> getcountry1(@Body JsonObject jsonObject);


    @Headers("Content-Type: application/json")
    @POST("appuser/signup-login-social")
    Call<JsonObject> socialsignin(@Body JsonObject jsonObject);

  //  var name    = req.body.name;
//        var bio    = req.body.bio;
//        var age    = req.body.age;
//        var skill    = req.body.skill;
//        var gendar    = req.body.gendar;



//    @FormUrlEncoded
//    @POST("userSignup")
//    Call<JsonObject> postSignup(
//            @FieldMap Map<String, String> option
//    );

//    /**
//     * URL MANIPULATION
//     * @since Not used, Just to know how to use @query to get JSONObject
//     * */
//    @GET("bins/path/")
//    Call<NoticeList> getNoticeDataData(@Query("company_no") int companyNo);
//
//
//
//    /**
//     * URL MANIPULATION
//     * A request URL can be updated dynamically using replacement blocks and parameters on the method.
//     * A replacement block is an alphanumeric string surrounded by { and }
//     * A corresponding parameter must be annotated with @Path using the same string.
//     * */
//    @GET("group/{id}/users")
//    Call<List<Notice>> groupList(@Path("id") int groupId);
//
//
//
//    /**
//     * URL MANIPULATION
//     * Using Query parameters.
//     * */
//    @GET("group/{id}/users")
//    Call<List<Notice>> groupList(@Path("id") int groupId, @Query("sort") String sort);
//
//
//
//
//    /**
//     * URL MANIPULATION
//     * complex query parameter combinations a Map can be used
//     * */
//    @GET("group/{id}/noticelist")
//    Call<List<Notice>> groupList(@Path("id") int groupId, @QueryMap Map<String, String> options);
//
//
//
//
//    /**
//     * URL MANIPULATION
//     * HTTP request body with the @Body annotation
//     */
//    @POST("notice/new")
//    Call<Notice> createNotice(@Body Notice notice);
//
//
//
//
//    /**
//     * FORM ENCODED AND MULTIPART
//     * Form-encoded data is sent when @FormUrlEncoded is present on the method.
//     * Each key-value pair is annotated with @Field containing the name and the object providing the value
//     * */
//    @FormUrlEncoded
//    @POST("notice/edit")
//    Call<Notice> updateNotice(@Field("id") String id, @Field("title") String title);
//
//
//
//
//    /**
//     * FORM ENCODED AND MULTIPART
//     * Multipart requests are used when @Multipart is present on the method.
//     * Parts are declared using the @Part annotation.
//     * */
//    @Multipart
//    @PUT("notice/photo")
//    Call<Notice> updateNotice(@Part("photo") RequestBody photo, @Part("description") RequestBody description);
//
//
//
//
//    /**
//     * HEADER MANIPULATION
//     * Set static headers for a method using the @Headers annotation.
//     * */
//    @Headers("Cache-Control: max-age=640000")
//    @GET("notice/list")
//    Call<List<Notice>> NoticeList();
//
//
//
//    /**
//     * HEADER MANIPULATION
//     * */
//    @Headers({
//            "Accept: application/vnd.github.v3.full+json",
//            "User-Agent: Retrofit-Sample-App"
//    })
//    @GET("noticelist/{title}")
//    Call<Notice> getNotice(@Path("title") String title);
//
//
//
//
//    /**
//     * HEADER MANIPULATION
//     * A request Header can be updated dynamically using the @Header annotation.
//     * A corresponding parameter must be provided to the @Header.
//     * If the value is null, the header will be omitted. Otherwise, toString will be called on the value, and the result used.
//     * */
//    @GET("notice")
//    Call<Notice> getNoticeUsingHeader(@Header("Authorization") String authorization);

}