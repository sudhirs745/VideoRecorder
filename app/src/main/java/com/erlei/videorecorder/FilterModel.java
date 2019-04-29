package com.erlei.videorecorder;

public class FilterModel {

    String FilterName;
    String FilterKey;
    String FilterType;

    public FilterModel(String filterName, String filterKey, String filterType) {
        FilterName = filterName;
        FilterKey = filterKey;
        FilterType = filterType;
    }

    public String getFilterName() {
        return FilterName;
    }

    public String getFilterKey() {
        return FilterKey;
    }

    public String getFilterType() {
        return FilterType;
    }
}
