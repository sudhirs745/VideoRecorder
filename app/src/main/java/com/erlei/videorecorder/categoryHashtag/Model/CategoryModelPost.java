package com.erlei.videorecorder.categoryHashtag.Model;

public class CategoryModelPost {

    String id,CategoryName, CategoryUrl,colorCodebg,colorcodeText;

    public CategoryModelPost(String id, String categoryName, String categoryUrl , String colorCodebg, String colorcodeText) {
        this.id = id;
        this.CategoryName = categoryName;
        this.CategoryUrl = categoryUrl;
        this.colorCodebg=colorCodebg;
        this.colorcodeText=colorcodeText;
    }

    public String getColorCodebg() {
        return colorCodebg;
    }

    public String getColorcodeText() {
        return colorcodeText;
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
