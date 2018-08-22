package com.atmaram.jp.model.rest;

import com.atmaram.jp.RestClient;
import com.atmaram.jp.ValueStore;
import com.atmaram.jp.model.Unit;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;

public class PutUnit extends BodiedUnit {
    public PutUnit(RestClient restClient) {
        super(restClient);
    }

    @Override
    public HttpResponse<String> fire(RestClient restClient){
        HttpResponse<String> output = null;
        try {
            output = restClient.put(urlTemplate, requestHeaders, requestTemplate);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return output;
    }

    @Override
    public Unit fill(ValueStore valueStore) {
        return fillObject(new PutUnit(restClient),valueStore);
    }
}