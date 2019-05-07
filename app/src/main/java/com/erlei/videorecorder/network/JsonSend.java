package com.erlei.videorecorder.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;

public class JsonSend {
    public static JsonObject getParam(Map<String, String> postParam) {
        Gson gsonObj = new Gson();

        String ja = gsonObj.toJson(postParam);

        JsonParser parser = new JsonParser();
        JsonObject ja1 = parser.parse(ja).getAsJsonObject();
        return ja1;


    }

}