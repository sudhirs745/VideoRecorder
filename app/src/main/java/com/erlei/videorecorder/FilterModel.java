package com.erlei.videorecorder;

public class FilterModel {

    String category_name;
    int category_image;

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public int getCategory_image() {
        return category_image;
    }

    public void setCategory_image(int category_image) {
        this.category_image = category_image;
    }

    public FilterModel(String category_name, int category_image) {
        this.category_name = category_name;
        this.category_image = category_image;
    }

    public FilterModel(String category_name) {
        this.category_name = category_name;

    }
}
