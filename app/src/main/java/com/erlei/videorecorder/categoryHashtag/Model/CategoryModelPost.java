package com.erlei.videorecorder.categoryHashtag.Model;

public class CategoryModelPost {

    String id,CategoryName, CategoryUrl;

    public CategoryModelPost(String id, String categoryName, String categoryUrl) {


        this.id = id;
        CategoryName = categoryName;
        CategoryUrl = categoryUrl;
    }

    public String getId() {
        return id;
    }

    public String getCategoryName() {
        return CategoryName;
    }

    public String getCategoryUrl() {
        return CategoryUrl;
    }
}
