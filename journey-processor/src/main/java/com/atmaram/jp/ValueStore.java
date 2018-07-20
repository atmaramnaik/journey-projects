package com.atmaram.jp;

import org.json.simple.JSONObject;

import java.util.HashMap;

public class ValueStore {
    HashMap<String,Object> values;

    public ValueStore() {
        values=new HashMap<>();
    }
    public void add(HashMap<String,Object> additional){
        values.putAll(additional);
    }
    public void add(String name,Object value){
        values.put(name,value);
    }

    public HashMap<String, Object> getValues() {
        return values;
    }

    public void setValues(HashMap<String, Object> values) {
        this.values = values;
    }
    @Override
    public String toString()
    {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("data",values);
        return jsonObject.toJSONString();
    }
}
