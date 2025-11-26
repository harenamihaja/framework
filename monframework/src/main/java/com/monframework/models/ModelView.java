package com.monframework.models;

import java.util.HashMap;

public class ModelView {
    private String view;
    private HashMap<String, Object> mapData = new HashMap<>();

    public ModelView() {}

    public ModelView(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }
      public HashMap<String, Object> getMapData() {
        return mapData;
    }

    public void addObject(String key, Object value) {
        this.mapData.put(key, value);
    }
}