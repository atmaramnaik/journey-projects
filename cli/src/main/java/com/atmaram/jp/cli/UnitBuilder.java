package com.atmaram.jp.cli;

import com.atmaram.jp.model.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.*;

public class UnitBuilder {

    private static VerbProcessor<GetUnit> get=new VerbProcessor<GetUnit>(".get",(File file)->{
        Request request=buildRequestFromFile(file,false);
        GetUnit getUnit = new GetUnit();
        getUnit.setUrlTemplate(request.getUrl());
        getUnit.setResponseTemplate(request.getResponse());
        getUnit.setRequestHeaders(request.getRequestHeaders());
        getUnit.setResponseHeaders(request.getResponseHeaders());
        getUnit.setWait(request.getWait());
        getUnit.setName(file.getName());
        return getUnit;
    });
    private static VerbProcessor<PostUnit> post=new VerbProcessor<PostUnit>(".post",(File file)->{
        Request request=buildRequestFromFile(file,true);
        PostUnit postUnit = new PostUnit();
        postUnit.setUrlTemplate(request.url);
        postUnit.setRequestTemplate(request.body);
        postUnit.setResponseTemplate(request.response);
        postUnit.setRequestHeaders(request.requestHeaders);
        postUnit.setResponseHeaders(request.responseHeaders);
        postUnit.setWait(request.wait);
        postUnit.setName(file.getName());
        return postUnit;
    });
    private static VerbProcessor<PutUnit> put=new VerbProcessor<PutUnit>(".put",(File file)->{
        Request request=buildRequestFromFile(file,true);
        PutUnit putUnit = new PutUnit();
        putUnit.setUrlTemplate(request.url);
        putUnit.setRequestTemplate(request.body);
        putUnit.setResponseTemplate(request.response);
        putUnit.setRequestHeaders(request.requestHeaders);
        putUnit.setResponseHeaders(request.responseHeaders);
        putUnit.setWait(request.wait);
        putUnit.setName(file.getName());
        return putUnit;
    });
    private static VerbProcessor<PatchUnit> patch=new VerbProcessor<PatchUnit>(".patch",(File file)->{
        Request request=buildRequestFromFile(file,true);
        PatchUnit patchUnit = new PatchUnit();
        patchUnit.setUrlTemplate(request.url);
        patchUnit.setRequestTemplate(request.body);
        patchUnit.setResponseTemplate(request.response);
        patchUnit.setRequestHeaders(request.requestHeaders);
        patchUnit.setResponseHeaders(request.responseHeaders);
        patchUnit.setWait(request.wait);
        patchUnit.setName(file.getName());
        return patchUnit;
    });
    private static VerbProcessor<DeleteUnit> delete=new VerbProcessor<DeleteUnit>(".delete",(File file)->{
        Request request=buildRequestFromFile(file,true);
        DeleteUnit deleteUnit = new DeleteUnit();
        deleteUnit.setUrlTemplate(request.url);
        deleteUnit.setRequestTemplate(request.body);
        deleteUnit.setResponseTemplate(request.response);
        deleteUnit.setRequestHeaders(request.requestHeaders);
        deleteUnit.setResponseHeaders(request.responseHeaders);
        deleteUnit.setWait(request.wait);
        deleteUnit.setName(file.getName());
        return deleteUnit;
    });
    private static VerbProcessor<BlockUnit> block=new VerbProcessor<BlockUnit>(".block",(File file)->{
        if(file.isDirectory() && file.getName().endsWith(".block")){
            return readBlockUnit(file);
        } else {
            return null;
        }
    });
    public static Request buildRequestFromFile(File file,boolean withBody) throws FileNotFoundException, ParseException {
        Request request=new Request();
        Scanner scanner = new Scanner(file);
        request.setUrl(scanner.nextLine());
        String body = "";
        if (withBody) {
            String[] bodyData=scanner.nextLine().split("=");
            if(bodyData.length>1){
                body=bodyData[1];
            }
        }
        request.setBody(body);
        String requestHeader = scanner.nextLine().split("=")[1];
        JSONParser jsonParser = new JSONParser();
        JSONObject rqjo = (JSONObject) jsonParser.parse(requestHeader);
        List<RequestHeader> requestHeaders = new ArrayList<>();
        for (Object key :
                rqjo.keySet()) {
            RequestHeader rh = new RequestHeader();
            rh.setName((String) key);
            rh.setValueTemplate((String) rqjo.get(key));
            requestHeaders.add(rh);
        }
        request.setRequestHeaders(requestHeaders);
        String[] response = scanner.nextLine().split("=");
        request.setResponse(response.length > 1 ? response[1] : "");
        String responseHeader = scanner.nextLine().split("=")[1];
        JSONObject rsjo = (JSONObject) jsonParser.parse(responseHeader);
        List<ResponseHeader> responseHeaders = new ArrayList<>();
        for (Object key :
                rsjo.keySet()) {
            ResponseHeader rh = new ResponseHeader();
            rh.setName((String) key);
            rh.setVariable((String) rsjo.get(key));
            responseHeaders.add(rh);
        }
        request.setResponseHeaders(responseHeaders);
        int wait = 0;
        if (scanner.hasNextLine()) {
            wait = Integer.parseInt(scanner.nextLine().split("=")[1]);
        }
        request.setWait(wait);
        return request;
    }
    private static List<VerbProcessor> verbProcessors=Arrays.asList(get,post,delete,patch,put,block);
    public static Unit buildFromFile(File file) throws FileNotFoundException, ParseException {
        for (VerbProcessor<Unit> processor:
                verbProcessors) {
            if(file.getName().endsWith(processor.verb)){
                return processor.transformer.transform(file);
            }
        }
        return null;
    }
    private static BlockUnit readBlockUnit(File dir) throws FileNotFoundException, ParseException {
        BlockUnit blockUnit=new BlockUnit();
        blockUnit.setName(dir.getName());
        File[] infoFiles=dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".info");
            }
        });
        Arrays.sort(infoFiles);
        for (File file:
                infoFiles) {
            Scanner scanner=new Scanner(file);
            blockUnit.setCounterVariable(scanner.nextLine().split("=")[1]);
            if (scanner.hasNextLine()){
                blockUnit.setWait(Integer.parseInt(scanner.nextLine().split("=")[1]));
            }
        }
        List<Unit> units=new ArrayList<>();
        File[] varFiles=dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".var");
            }
        });
        Arrays.sort(varFiles);

        File[] files=dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".unit");
            }
        });
        Arrays.sort(files,new Comparator()
        {
            @Override
            public int compare(Object f1, Object f2) {
                return ((File) f1).getName().compareTo(((File) f2).getName());
            }
        });
        for(File file:files){
            units.add(buildFromFile(file));
        }
        blockUnit.setUnits(units);
        blockUnit.setVariables(getCommandVariables(varFiles));
        return blockUnit;
    }
    public static List<EnvironmentVariable> getCommandVariables(File[] files) throws FileNotFoundException {
        List<EnvironmentVariable> variables=new ArrayList<>();
        for (File file:
                files) {
            Scanner scanner=new Scanner(file);
            while (scanner.hasNextLine()){
                String line=scanner.nextLine();
                String[] strings=line.split("=");
                EnvironmentVariable environmentVariable=new EnvironmentVariable(strings[0],strings[1]);
                variables.add(environmentVariable);
            }
        }
        return variables;
    }
}
