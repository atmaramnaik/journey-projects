package com.atmaram.tp.common;

import java.util.*;

public class VariableValueProcessor {
    private Executable executable;
    private Matchable matchable;

    public VariableValueProcessor(Matchable matchable,Executable executable) {
        this.executable = executable;
        this.matchable = matchable;
    }

    static VariableValueProcessor timestamp=new VariableValueProcessor((String expression)->expression.startsWith("_timestamp"),(String expression,HashMap<String,Object> data)->new Date().getTime());
    static VariableValueProcessor eval=new VariableValueProcessor((String expression)->expression.startsWith("_eval"),(String expression,HashMap<String,Object> data)->{
        String newExpr=expression.split(">")[0];
        return getVal(newExpr.substring(6,newExpr.length()-1));
    });
    static VariableValueProcessor uuid=new VariableValueProcessor((String expression)->expression.startsWith("_uuid"), (String expression,HashMap<String,Object> data)->UUID.randomUUID().toString());
    static VariableValueProcessor concat=new VariableValueProcessor((String expression)->expression.startsWith("_+"), (String expression,HashMap<String,Object> data)->{
        String newExpr=expression.split(">")[0];
        return concat(newExpr.substring(3,newExpr.length()-1).split(","),data);
    });
    public static String concat(String[] args,HashMap<String,Object> data){
        String res="";
        for(int i=0;i<args.length;i++){
            res+=getValue(args[i],data);
        }
        return res;
    }
    static List<VariableValueProcessor> allProcessors=Arrays.asList(timestamp,eval,uuid,concat);
    public static void addProcessor(VariableValueProcessor variableValueProcessor){
        allProcessors.add(variableValueProcessor);
    }
    public static Object getValue(String name, HashMap<String,Object> data) {
        if(data !=null && data.containsKey(name)) {
            return data.get(name);
        }
        if(name.startsWith("_")){
            if(name.equals("_this")){
                return data.get("_this");
            } else {
                for (VariableValueProcessor variableValueProcessor:
                     allProcessors) {
                    if(variableValueProcessor.matchable.match(name)){
                        Object value=variableValueProcessor.executable.execute(name,data);
                        String[] processorargs=name.split(">");
                        if(processorargs.length>1){
                            data.put(processorargs[1],value);
                        }
                        return value;
                    }
                }
            }
        }
        return "${"+name+"}";
    }
    private static String getVal(String pattern){
        if(pattern.equals(""))
            return "";
        if(pattern.contains("(") && pattern.contains(")")){
            String newpat=pattern.substring(1,pattern.length()-1);
            String start=newpat.split(",")[0];
            String end=newpat.split(",")[1];
            return between(start,end);
        } else if(pattern.contains("[") && pattern.contains("]")){
            String newpat=pattern.substring(1,pattern.length()-2);
            return among(newpat);
        }else {
            String value=pattern;
            Random random=new Random();
            while(value.contains("#"))
            {
                value=value.replaceFirst("#", Integer.toString(random.nextInt(9)));
            }
            return value;
        }
    }
    public static String between(String start,String end){
        start=getVal(start);
        end=getVal(end);
        int int1=Integer.parseInt(start);
        int int2=Integer.parseInt(end);
        return Integer.toString(new Random().nextInt(int2-int1)+int1);

    }
    public static String among(String list){
        String [] sList=list.split(",");
        int index=new Random().nextInt(sList.length-1);
        return getVal(sList[index]);
    }
}
